package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.request.ScheduleByDepartmentRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleCreationRequest;
import com.UserSchedule.UserSchedule.dto.request.SchedulePatchRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleUpdateRequest;
import com.UserSchedule.UserSchedule.dto.response.ScheduleResponse;
import com.UserSchedule.UserSchedule.dto.response.UserResponse;
import com.UserSchedule.UserSchedule.dto.scheduleRepository.FreeTimeSlot;
import com.UserSchedule.UserSchedule.dto.scheduleRepository.ScheduleConflictInfo;
import com.UserSchedule.UserSchedule.entity.Department;
import com.UserSchedule.UserSchedule.entity.Room;
import com.UserSchedule.UserSchedule.entity.Schedule;
import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.ScheduleMapper;
import com.UserSchedule.UserSchedule.mapper.UserMapper;
import com.UserSchedule.UserSchedule.repository.DepartmentRepository;
import com.UserSchedule.UserSchedule.repository.RoomRepository;
import com.UserSchedule.UserSchedule.repository.ScheduleRepository;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import com.UserSchedule.UserSchedule.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleService {
    ScheduleRepository scheduleRepository;
    UserRepository userRepository;
    RoomRepository roomRepository;
    ScheduleMapper scheduleMapper;
    UserMapper userMapper;
    SecurityUtils securityUtils;
    DepartmentRepository departmentRepository;

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreationRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        List<User> userParticipants = userRepository.findAllByKeycloakIdIn(request.getParticipantIds());
        // Kiểm tra xem user hiện tại có trùng lịch không
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesByParticipants(
                userParticipants.stream().map(User::getUserId).toList(), request.getStartTime(), request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        // Lấy danh sách participants từ ID
        Set<User> participants = new HashSet<>(userParticipants);
        if (participants.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Ánh xạ request → entity
        Schedule schedule = scheduleMapper.toSchedule(request);
        schedule.setParticipants(participants);
        schedule.setCreatedBy(currentUser);
        // Nếu là OFFLINE thì phải check phòng và set phòng
        if (request.getType() == ScheduleType.OFFLINE) {
            // Check room có bị trùng lịch không
            List<Schedule> conflictRoom = scheduleRepository
                    .findConflictingSchedulesByRoom(request.getRoomId(), request.getStartTime(), request.getEndTime());
            if (!conflictRoom.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }

            // Tìm room theo ID
            Room room = roomRepository.findByRoomId(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
            schedule.setRoom(room);
        }

        // Lưu và trả response
        return scheduleMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @Transactional
    public ScheduleResponse createScheduleByDepartment(ScheduleByDepartmentRequest request, String departmentName) {
        User currentUser = securityUtils.getCurrentUser();
        Department department = departmentRepository.findByName(departmentName)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        int departmentId = department.getDepartmentId();
        boolean isAdmin = securityUtils.isAdmin();
        boolean isSameDepartment = Objects.equals(currentUser.getDepartment().getDepartmentId(), departmentId);

        if (!isAdmin && !isSameDepartment) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<User> departmentUsers = userRepository.findByDepartment_DepartmentId(departmentId);
        if (departmentUsers.isEmpty()) {
            throw new AppException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        // Lấy danh sách userId
        List<Integer> userIds = departmentUsers.stream().map(User::getUserId).toList();

        // Lấy các lịch bị trùng của các user này
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesByParticipants(
                userIds, request.getStartTime(), request.getEndTime()
        );

        // Lấy userId bị trùng lịch
        Set<Integer> conflictedUserIds = conflicts.stream()
                .flatMap(schedule -> schedule.getParticipants().stream())
                .map(User::getUserId)
                .collect(Collectors.toSet());

        // Loại bỏ user bị trùng lịch
        Set<User> availableParticipants = departmentUsers.stream()
                .filter(user -> !conflictedUserIds.contains(user.getUserId()))
                .collect(Collectors.toSet());

        if (availableParticipants.isEmpty()) {
            throw new AppException(ErrorCode.NO_AVAILABLE_PARTICIPANTS);
        }

        Schedule schedule = Schedule.builder()
                .title(request.getTitle())
                .type(request.getType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .participants(availableParticipants)
                .createdBy(currentUser)
                .build();

        if (request.getType() == ScheduleType.OFFLINE) {
            Room room = roomRepository.findByName(request.getRoomName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
            List<Schedule> roomConflicts = scheduleRepository.findConflictingSchedulesByRoom(
                    room.getRoomId(), request.getStartTime(), request.getEndTime());
            if (!roomConflicts.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }
            schedule.setRoom(room);
        }

        return scheduleMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }


    public List<ScheduleResponse> getSchedulesByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Schedule> schedules = scheduleRepository.findSchedulesByUserId(user.getUserId());
        return scheduleMapper.toScheduleResponseList(schedules);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ScheduleResponse updateSchedule(Integer scheduleId, ScheduleUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Tìm lịch hiện tại
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        checkPermission(currentUser, schedule);
        List<User> userParticipants = userRepository.findAllByKeycloakIdIn(request.getParticipantIds());
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesByParticipantsExceptCurrent(
                userParticipants.stream().map(User::getUserId).toList(), request.getStartTime(), request.getEndTime(), scheduleId);


        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        Set<User> newParticipants = new HashSet<>(userParticipants);
        if (newParticipants.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        schedule.setTitle(request.getTitle());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setParticipants(newParticipants);
        schedule.setType(request.getType());

        if (request.getType() == ScheduleType.OFFLINE) {
            // Check room có bị trùng lịch không
            List<Schedule> conflictRoom = scheduleRepository
                    .findConflictingSchedulesByRoomExceptCurrent(
                            request.getRoomId(),
                            request.getStartTime(),
                            request.getEndTime(),
                            scheduleId
                    );
            if (!conflictRoom.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }

            // Tìm room theo ID
            Room room = roomRepository.findByRoomId(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
            schedule.setRoom(room);
        } else {
            schedule.setRoom(null);
        }
        // Lưu và trả về response
        return scheduleMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }

    public ScheduleResponse getScheduleById(Integer id) {
        return scheduleMapper.toScheduleResponse(scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND)));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public void deleteSchedule(Integer id) {
        User currentUser = securityUtils.getCurrentUser();

        boolean isAdmin = securityUtils.isAdmin();

        // Tìm lịch hiện tại
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        checkPermission(currentUser, schedule);

        scheduleRepository.delete(schedule);
    }
    private void checkPermission(User currentUser, Schedule schedule) {
        if (!securityUtils.isAdmin() && !Objects.equals(currentUser.getUserId(), schedule.getCreatedBy().getUserId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    public List<FreeTimeSlot> getAvailableSlotsBetween(String roomName, LocalDateTime startTime, LocalDateTime endTime) {
        return  scheduleRepository.getAvailableSlotsBetween(roomName, startTime, endTime);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ScheduleResponse createSimpleSchedule(ScheduleByDepartmentRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Khởi tạo lịch
        Schedule.ScheduleBuilder builder = Schedule.builder()
                .title(request.getTitle())
                .type(request.getType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(currentUser)
                .participants(Collections.singleton(currentUser)); // Không cần participants

        // Nếu là OFFLINE thì cần room
        if (request.getType() == ScheduleType.OFFLINE) {
            Room room = roomRepository.findByName(request.getRoomName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

            // Kiểm tra phòng đã bị đặt chưa
            List<Schedule> roomConflicts = scheduleRepository.findConflictingSchedulesByRoom(
                    room.getRoomId(), request.getStartTime(), request.getEndTime());

            if (!roomConflicts.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }

            builder.room(room);
        }

        Schedule saved = scheduleRepository.save(builder.build());
        return scheduleMapper.toScheduleResponse(saved);
    }

    public List<ScheduleConflictInfo> getScheduleConflictInfo(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new AppException(ErrorCode.MIN_MAX_CAPACITY_CONFLICT);
        }
        return scheduleRepository.findConflictingSchedulesWithRoomAndUserInfo(startTime, endTime);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public ScheduleResponse patchScheduleByRoomAndStartTime(String roomName, LocalDateTime startTime, SchedulePatchRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Schedule schedule = scheduleRepository.findByRoomAndStartTime(room, startTime)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        checkPermission(currentUser, schedule);

        // Cập nhật nếu có
        if (request.getTitle() != null) {
            schedule.setTitle(request.getTitle());
        }

        if (request.getStartTime() != null) {
            schedule.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            schedule.setEndTime(request.getEndTime());
        }

        if (request.getType() != null) {
            schedule.setType(request.getType());
            if (request.getType() != ScheduleType.OFFLINE) {
                schedule.setRoom(null);
            }
        }

        if (request.getRoomName() != null && schedule.getType() == ScheduleType.OFFLINE) {
            Room newRoom = roomRepository.findByName(request.getRoomName())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
            schedule.setRoom(newRoom);
        }
        // Kiểm tra conflict nếu là OFFLINE và có thay đổi về thời gian hoặc phòng
        if (schedule.getType() == ScheduleType.OFFLINE) {
            Room effectiveRoom = schedule.getRoom(); // đã cập nhật rồi nếu roomId có
            if (effectiveRoom == null) {
                throw new AppException(ErrorCode.ROOM_NOT_FOUND); // hoặc ROOM_REQUIRED nếu type = OFFLINE
            }
            LocalDateTime effectiveStartTime = schedule.getStartTime();
            LocalDateTime effectiveEndTime = schedule.getEndTime();

            List<Schedule> conflictSchedules = scheduleRepository
                    .findConflictingSchedulesByRoomExceptCurrent(
                            effectiveRoom.getRoomId(),
                            effectiveStartTime,
                            effectiveEndTime,
                            schedule.getScheduleId()
                    );

            if (!conflictSchedules.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }
        }
        return scheduleMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public void deleteScheduleByRoomAndStartTime(String roomName, LocalDateTime startTime) {
        User currentUser = securityUtils.getCurrentUser();

        // Tìm room theo tên
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        // Tìm lịch theo room và startTime
        Schedule schedule = scheduleRepository.findByRoomAndStartTime(room, startTime)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        // Kiểm tra quyền
        checkPermission(currentUser, schedule);

        // Xoá
        scheduleRepository.delete(schedule);
    }

}

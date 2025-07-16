package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.request.ScheduleByDepartmentRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleCreationRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleUpdateRequest;
import com.UserSchedule.UserSchedule.dto.response.ScheduleResponse;
import com.UserSchedule.UserSchedule.entity.Room;
import com.UserSchedule.UserSchedule.entity.Schedule;
import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.enum_type.RoleType;
import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.ScheduleMapper;
import com.UserSchedule.UserSchedule.repository.RoomRepository;
import com.UserSchedule.UserSchedule.repository.ScheduleRepository;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import com.UserSchedule.UserSchedule.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleService {
    ScheduleRepository scheduleRepository;
    UserRepository userRepository;
    RoomRepository roomRepository;
    ScheduleMapper scheduleMapper;
    SecurityUtils securityUtils;

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreationRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        // Kiểm tra xem user hiện tại có trùng lịch không
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesByParticipants(
                request.getParticipantIds(), request.getStartTime(), request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        // Lấy danh sách participants từ ID
        Set<User> participants = new HashSet<>(userRepository.findAllById(request.getParticipantIds()));
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
    public ScheduleResponse createScheduleByDepartment(ScheduleByDepartmentRequest request, Integer departmentId) {
        User currentUser = securityUtils.getCurrentUser();

        boolean isAdmin = securityUtils.isAdmin();
        boolean isSameDepartment = Objects.equals(currentUser.getDepartment().getDepartmentId(), departmentId);

        if (!isAdmin && !isSameDepartment) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<User> departmentUsers = userRepository.findByDepartment_DepartmentId(departmentId);
        if (departmentUsers.isEmpty()) {
            throw new AppException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        Set<User> participants = new HashSet<>(departmentUsers);

        List<Integer> userIds = departmentUsers.stream()
                .map(User::getUserId)
                .toList();

        List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesByParticipants(
                userIds, request.getStartTime(), request.getEndTime());

        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        Schedule schedule = Schedule.builder()
                .title(request.getTitle())
                .type(request.getType())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .participants(participants)
                .createdBy(currentUser)
                .build();

        if (request.getType() == ScheduleType.OFFLINE) {
            List<Schedule> roomConflicts = scheduleRepository.findConflictingSchedulesByRoom(
                    request.getRoomId(), request.getStartTime(), request.getEndTime());

            if (!roomConflicts.isEmpty()) {
                throw new AppException(ErrorCode.ROOM_ALREADY_BOOKED);
            }

            Room room = roomRepository.findByRoomId(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
            schedule.setRoom(room);
        }

        return scheduleMapper.toScheduleResponse(scheduleRepository.save(schedule));
    }

    public List<ScheduleResponse> getSchedulesByUserId(Integer userId) {
        userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Schedule> schedules = scheduleRepository.findSchedulesByUserId(userId);
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
        List<Schedule> conflicts = scheduleRepository.findConflictingSchedulesByParticipantsExceptCurrent(
                request.getParticipantIds(), request.getStartTime(), request.getEndTime(), scheduleId);


        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        Set<User> newParticipants = new HashSet<>(userRepository.findAllById(request.getParticipantIds()));
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
}

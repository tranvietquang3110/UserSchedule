package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.request.RoomRequest;
import com.UserSchedule.UserSchedule.dto.response.RoomResponse;
import com.UserSchedule.UserSchedule.dto.response.ScheduleResponse;
import com.UserSchedule.UserSchedule.dto.roomRepository.RoomWithStatus;
import com.UserSchedule.UserSchedule.entity.Room;
import com.UserSchedule.UserSchedule.entity.Schedule;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.RoomMapper;
import com.UserSchedule.UserSchedule.mapper.ScheduleMapper;
import com.UserSchedule.UserSchedule.repository.RoomRepository;
import com.UserSchedule.UserSchedule.repository.ScheduleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RoomService {
    RoomRepository roomRepository;
    RoomMapper roomMapper;
    ScheduleMapper scheduleMapper;
    private final ScheduleRepository scheduleRepository;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public RoomResponse createRoom(RoomRequest roomRequest) {
        Optional<Room> roomOptional = roomRepository.findByName(roomRequest.getName());

        Room room;
        if (roomOptional.isPresent()) {
            Room existingRoom = roomOptional.get();
            if (Boolean.TRUE.equals(existingRoom.getIsUsed())) {
                throw new AppException(ErrorCode.ROOM_EXISTED);
            } else {
                existingRoom.setIsUsed(true);
                room = existingRoom;
            }
        } else {
            room = roomMapper.toRoom(roomRequest);
            room.setIsUsed(true);
        }

        return roomMapper.toRoomResponse(roomRepository.save(room));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public void deleteRoom(int roomId) {
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        room.setIsUsed(false);
        roomRepository.save(room);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public RoomResponse updateRoom(RoomRequest request, int id) {
        Room room = roomRepository.findByRoomId(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        if (roomRepository.existsByNameAndRoomIdNot(request.getName(), id)) {
            throw new AppException(ErrorCode.ROOM_EXISTED);
        }
        room.setName(request.getName());
        room.setCapacity(request.getCapacity());
        room.setLocation(request.getLocation());
        return roomMapper.toRoomResponse(roomRepository.save(room));
    }

    public RoomResponse getRoomById(int id) {
        Room room = roomRepository.findByRoomId(id)
                .filter(Room::getIsUsed)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toRoomResponse(room);
    }

    public List<RoomResponse> getAll() {
        return roomMapper.toRoomResponseList(roomRepository.findAllByIsUsedTrue());
    }

    public List<RoomWithStatus> getRoomWithStatus() {
        return roomRepository.findRoomsWithStatus(LocalDateTime.now());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public List<ScheduleResponse> getRoomReservationHistory(int roomId) {
        return scheduleMapper.toScheduleResponseList(scheduleRepository.findByRoom_RoomId(roomId));
    }
}

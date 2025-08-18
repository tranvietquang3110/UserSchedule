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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private RoomService roomService;

    private RoomRequest roomRequest;
    private Room room;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roomRequest = new RoomRequest();
        roomRequest.setName("Conference Room A");
        roomRequest.setCapacity(10);
        roomRequest.setLocation("Building 1");

        room = new Room(1, "Conference Room A", "Building 1", 10, false);

        roomResponse = RoomResponse.builder()
                .name("Conference Room A")
                .location("Building 1")
                .capacity(10)
                .build();
    }

    // ------------------ createRoom ------------------
    @Test
    void testCreateRoom_NewRoom() {
        when(roomRepository.findByName(roomRequest.getName())).thenReturn(Optional.empty());
        when(roomMapper.toRoom(roomRequest)).thenReturn(room);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toRoomResponse(room)).thenReturn(roomResponse);

        RoomResponse result = roomService.createRoom(roomRequest);

        assertNotNull(result);
        assertEquals("Conference Room A", result.getName());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testCreateRoom_RoomAlreadyUsed() {
        room.setIsUsed(true);
        when(roomRepository.findByName(roomRequest.getName())).thenReturn(Optional.of(room));

        AppException exception = assertThrows(AppException.class, () -> roomService.createRoom(roomRequest));

        assertEquals(ErrorCode.ROOM_EXISTED, exception.getErrorCode());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void testCreateRoom_RoomExistsButNotUsed() {
        when(roomRepository.findByName(roomRequest.getName())).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toRoomResponse(room)).thenReturn(roomResponse);

        RoomResponse result = roomService.createRoom(roomRequest);

        assertTrue(room.getIsUsed());
        assertEquals("Conference Room A", result.getName());
        verify(roomRepository, times(1)).save(room);
    }

    // ------------------ deleteRoom ------------------
    @Test
    void testDeleteRoom_Success() {
        room.setIsUsed(true);
        when(roomRepository.findByRoomId(1)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1);

        assertFalse(room.getIsUsed());
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    void testDeleteRoom_NotFound() {
        when(roomRepository.findByRoomId(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.deleteRoom(1));

        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    // ------------------ updateRoom ------------------
    @Test
    void testUpdateRoom_Success() {
        RoomRequest request = new RoomRequest();
        request.setName("NewName");
        request.setLocation("NewLocation");
        request.setCapacity(20);

        when(roomRepository.findByRoomId(1)).thenReturn(Optional.of(room));
        when(roomRepository.existsByNameAndRoomIdNot("NewName", 1)).thenReturn(false);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toRoomResponse(room))
                .thenReturn(new RoomResponse("NewName", "NewLocation", 20));

        RoomResponse response = roomService.updateRoom(request, 1);

        assertEquals("NewName", response.getName());
        assertEquals(20, response.getCapacity());
    }

    @Test
    void testUpdateRoom_NotFound() {
        when(roomRepository.findByRoomId(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.updateRoom(roomRequest, 1));

        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testUpdateRoom_DuplicateName() {
        RoomRequest request = new RoomRequest();
        request.setName("DupName");
        request.setCapacity(5);
        request.setLocation("L1");

        when(roomRepository.findByRoomId(1)).thenReturn(Optional.of(room));
        when(roomRepository.existsByNameAndRoomIdNot("DupName", 1)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> roomService.updateRoom(request, 1));

        assertEquals(ErrorCode.ROOM_EXISTED, ex.getErrorCode());
    }

    // ------------------ getRoomById ------------------
    @Test
    void testGetRoomById_Success() {
        room.setIsUsed(true);
        when(roomRepository.findByRoomId(1)).thenReturn(Optional.of(room));
        when(roomMapper.toRoomResponse(room)).thenReturn(roomResponse);

        RoomResponse response = roomService.getRoomById(1);

        assertEquals("Conference Room A", response.getName());
    }

    @Test
    void testGetRoomById_NotFound() {
        when(roomRepository.findByRoomId(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.getRoomById(1));

        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void testGetRoomById_FoundButNotUsed() {
        room.setIsUsed(false);
        when(roomRepository.findByRoomId(1)).thenReturn(Optional.of(room));

        AppException ex = assertThrows(AppException.class, () -> roomService.getRoomById(1));

        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    // ------------------ getAll ------------------
    @Test
    void testGetAllRooms() {
        List<Room> rooms = Arrays.asList(room);
        List<RoomResponse> responses = Arrays.asList(roomResponse);

        when(roomRepository.findAllByIsUsedTrue()).thenReturn(rooms);
        when(roomMapper.toRoomResponseList(rooms)).thenReturn(responses);

        List<RoomResponse> result = roomService.getAll();

        assertEquals(1, result.size());
    }

    // ------------------ getRoomWithStatus ------------------
    @Test
    void testGetRoomWithStatus() {
        List<RoomWithStatus> mockList = new ArrayList<>();
        when(roomRepository.findRoomsWithStatus(any())).thenReturn(mockList);

        List<RoomWithStatus> result = roomService.getRoomWithStatus();

        assertEquals(0, result.size());
    }

    // ------------------ getRoomReservationHistory ------------------
    @Test
    void testGetRoomReservationHistory() {
        List<Schedule> schedules = new ArrayList<>();
        List<ScheduleResponse> responses = new ArrayList<>();

        when(scheduleRepository.findByRoom_RoomId(1)).thenReturn(schedules);
        when(scheduleMapper.toScheduleResponseList(schedules)).thenReturn(responses);

        List<ScheduleResponse> result = roomService.getRoomReservationHistory(1);

        assertEquals(0, result.size());
    }

    // ------------------ getAvailableRoom ------------------
    @Test
    void testGetAvailableRoom() {
        List<Room> rooms = List.of(room);
        List<RoomResponse> responses = List.of(roomResponse);

        when(roomRepository.findAvailableRoomsBetween(any(), any())).thenReturn(rooms);
        when(roomMapper.toRoomResponseList(rooms)).thenReturn(responses);

        List<RoomResponse> result = roomService.getAvailableRoom(
                LocalDateTime.now(), LocalDateTime.now().plusHours(2));

        assertEquals(1, result.size());
        assertEquals("Conference Room A", result.get(0).getName());
    }

    // ------------------ getRoomsByRangeCapacity ------------------
    @Test
    void testGetRoomsByRangeCapacity_MinGreaterThanMax_ShouldThrowException() {
        AppException ex = assertThrows(AppException.class,
                () -> roomService.getRoomsByRangeCapacity(20, 10));

        assertEquals(ErrorCode.MIN_MAX_CAPACITY_CONFLICT, ex.getErrorCode());
        verify(roomRepository, never()).findRoomsByRangeCapacity(anyInt(), anyInt());
    }

    @Test
    void testGetRoomsByRangeCapacity_ValidRange_ShouldReturnRooms() {
        List<Room> rooms = Arrays.asList(room);
        List<RoomResponse> responses = Arrays.asList(roomResponse);

        when(roomRepository.findRoomsByRangeCapacity(5, 15)).thenReturn(rooms);
        when(roomMapper.toRoomResponseList(rooms)).thenReturn(responses);

        List<RoomResponse> result = roomService.getRoomsByRangeCapacity(5, 15);

        assertEquals(1, result.size());
        assertEquals("Conference Room A", result.get(0).getName());
    }
}

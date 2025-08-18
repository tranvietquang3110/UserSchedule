package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.request.ScheduleCreationRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleUpdateRequest;
import com.UserSchedule.UserSchedule.dto.request.SchedulePatchRequest;
import com.UserSchedule.UserSchedule.dto.response.ScheduleResponse;
import com.UserSchedule.UserSchedule.entity.Room;
import com.UserSchedule.UserSchedule.entity.Schedule;
import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.ScheduleMapper;
import com.UserSchedule.UserSchedule.repository.RoomRepository;
import com.UserSchedule.UserSchedule.repository.ScheduleRepository;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import com.UserSchedule.UserSchedule.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleServiceTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private SecurityUtils securityUtils;

    private User mockUser;
    private Room mockRoom;
    private Schedule mockSchedule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = User.builder()
                .userId(1)
                .keycloakId("kc-123")
                .username("john")
                .build();

        mockRoom = Room.builder()
                .roomId(10)
                .name("Conference Room")
                .capacity(20)
                .build();

        mockSchedule = Schedule.builder()
                .scheduleId(100)
                .title("Team Meeting")
                .type(ScheduleType.OFFLINE)
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .createdBy(mockUser)
                .room(mockRoom)
                .participants(new HashSet<>(Set.of(mockUser)))
                .build();
    }

    // ---------------- createSchedule ----------------
    @Test
    void createSchedule_success() {
        ScheduleCreationRequest request = new ScheduleCreationRequest();
        request.setTitle("New Meeting");
        request.setType(ScheduleType.OFFLINE);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setRoomId(10);
        request.setParticipantIds(Set.of("kc-123"));

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(userRepository.findAllByKeycloakIdIn(Set.of("kc-123"))).thenReturn(List.of(mockUser));
        when(scheduleRepository.findConflictingSchedulesByParticipants(any(), any(), any())).thenReturn(Collections.emptyList());
        when(scheduleRepository.findConflictingSchedulesByRoom(eq(10), any(), any())).thenReturn(Collections.emptyList());
        when(roomRepository.findByRoomId(10)).thenReturn(Optional.of(mockRoom));
        when(scheduleMapper.toSchedule(request)).thenReturn(mockSchedule);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(mockSchedule);
        when(scheduleMapper.toScheduleResponse(mockSchedule)).thenReturn(new ScheduleResponse());

        ScheduleResponse response = scheduleService.createSchedule(request);

        assertNotNull(response);
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    void createSchedule_conflictParticipant_throwsException() {
        ScheduleCreationRequest request = new ScheduleCreationRequest();
        request.setTitle("Conflict Meeting");
        request.setType(ScheduleType.ONLINE);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        request.setRoomId(10);
        request.setParticipantIds(Set.of("kc-123"));

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(userRepository.findAllByKeycloakIdIn(any())).thenReturn(List.of(mockUser));
        when(scheduleRepository.findConflictingSchedulesByParticipants(any(), any(), any()))
                .thenReturn(List.of(mockSchedule));

        AppException ex = assertThrows(AppException.class, () -> scheduleService.createSchedule(request));
        assertEquals(ErrorCode.SCHEDULE_CONFLICT, ex.getErrorCode());
    }

    // ---------------- updateSchedule ----------------
    @Test
    void updateSchedule_success() {
        ScheduleUpdateRequest request = new ScheduleUpdateRequest();
        request.setTitle("Updated Meeting");
        request.setType(ScheduleType.ONLINE);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        request.setParticipantIds(Set.of("kc-123"));

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(scheduleRepository.findById(100)).thenReturn(Optional.of(mockSchedule));
        when(userRepository.findAllByKeycloakIdIn(any())).thenReturn(List.of(mockUser));
        when(scheduleRepository.findConflictingSchedulesByParticipantsExceptCurrent(any(), any(), any(), eq(100)))
                .thenReturn(Collections.emptyList());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(mockSchedule);
        when(scheduleMapper.toScheduleResponse(mockSchedule)).thenReturn(new ScheduleResponse());

        ScheduleResponse response = scheduleService.updateSchedule(100, request);

        assertNotNull(response);
        verify(scheduleRepository, times(1)).save(any(Schedule.class));
    }

    @Test
    void updateSchedule_scheduleNotFound_throwsException() {
        when(scheduleRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> scheduleService.updateSchedule(999, new ScheduleUpdateRequest()));
        assertEquals(ErrorCode.SCHEDULE_NOT_FOUND, ex.getErrorCode());
    }

    // ---------------- patchScheduleByRoomAndStartTime ----------------
    @Test
    void patchSchedule_success() {
        SchedulePatchRequest request = new SchedulePatchRequest();
        request.setTitle("Patched Title");

        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(roomRepository.findByName("Conference Room")).thenReturn(Optional.of(mockRoom));
        when(scheduleRepository.findByRoomAndStartTime(eq(mockRoom), any())).thenReturn(Optional.of(mockSchedule));
        when(scheduleRepository.findConflictingSchedulesByRoomExceptCurrent(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(mockSchedule);
        when(scheduleMapper.toScheduleResponse(mockSchedule)).thenReturn(new ScheduleResponse());

        ScheduleResponse response = scheduleService.patchScheduleByRoomAndStartTime(
                "Conference Room", mockSchedule.getStartTime(), request);

        assertNotNull(response);
        verify(scheduleRepository, times(1)).save(mockSchedule);
    }

    @Test
    void patchSchedule_roomNotFound_throwsException() {
        when(roomRepository.findByName("Invalid Room")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> scheduleService.patchScheduleByRoomAndStartTime("Invalid Room", LocalDateTime.now(), new SchedulePatchRequest()));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    // ---------------- deleteSchedule ----------------
    @Test
    void deleteSchedule_success() {
        when(securityUtils.getCurrentUser()).thenReturn(mockUser);
        when(securityUtils.isAdmin()).thenReturn(true);
        when(scheduleRepository.findById(100)).thenReturn(Optional.of(mockSchedule));

        scheduleService.deleteSchedule(100);

        verify(scheduleRepository, times(1)).delete(mockSchedule);
    }

    @Test
    void deleteSchedule_notFound_throwsException() {
        when(scheduleRepository.findById(999)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> scheduleService.deleteSchedule(999));
        assertEquals(ErrorCode.SCHEDULE_NOT_FOUND, ex.getErrorCode());
    }
}

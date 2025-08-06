package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.dto.scheduleRepository.FreeTimeSlot;
import com.UserSchedule.UserSchedule.dto.scheduleRepository.ScheduleConflictInfo;
import com.UserSchedule.UserSchedule.entity.Room;
import com.UserSchedule.UserSchedule.entity.Schedule;
import com.UserSchedule.UserSchedule.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    @Query("""
    SELECT s FROM Schedule s
    WHERE s.room.roomId = :roomId
      AND s.startTime < :endTime
      AND s.endTime > :startTime
""")
    List<Schedule> findConflictingSchedulesByRoom(
            @Param("roomId") Integer roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
    SELECT s FROM Schedule s
    JOIN s.participants p
    WHERE p.userId IN :userIds
      AND s.startTime < :endTime
      AND s.endTime > :startTime
""")
    List<Schedule> findConflictingSchedulesByParticipants(
            @Param("userIds") Collection<Integer> userIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    @Query("""
    SELECT s FROM Schedule s
    JOIN s.participants p
    WHERE p.userId = :userId
""")
    List<Schedule> findSchedulesByUserId(@Param("userId") Integer userId);

    @Query("""
    SELECT s FROM Schedule s
    JOIN s.participants p
    WHERE p.userId IN :participantIds
      AND s.scheduleId <> :scheduleId
      AND s.startTime < :endTime
      AND s.endTime > :startTime
""")
    List<Schedule> findConflictingSchedulesByParticipantsExceptCurrent(
            @Param("participantIds") Collection<Integer> participantIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("scheduleId") Integer scheduleId
    );

    @Query("""
    SELECT s FROM Schedule s
    WHERE s.room.roomId = :roomId
      AND s.startTime < :endTime
      AND s.endTime > :startTime
      AND s.scheduleId <> :currentScheduleId
""")
    List<Schedule> findConflictingSchedulesByRoomExceptCurrent(
            @Param("roomId") Integer roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("currentScheduleId") Integer currentScheduleId
    );
    List<Schedule> findByRoom_RoomId(Integer roomId);


    @Query(value = """
WITH schedule_with_bounds AS (
    SELECT 
        s.start_time,
        s.end_time
    FROM schedules s
    JOIN rooms r ON s.room_id = r.room_id
    WHERE r.name = :roomName
      AND s.start_time < :endTime
      AND s.end_time > :startTime
),
ordered_schedules AS (
    SELECT *,
           LAG(end_time) OVER (ORDER BY start_time) AS previous_end_time
    FROM schedule_with_bounds
),
free_slots AS (
    SELECT 
        previous_end_time AS start_time,
        start_time AS end_time
    FROM ordered_schedules
    WHERE previous_end_time IS NOT NULL
),
first_slot AS (
    SELECT 
        CAST(:startTime AS datetime) AS start_time,
        MIN(start_time) AS end_time
    FROM schedule_with_bounds
),
last_slot AS (
    SELECT 
        MAX(end_time) AS start_time,
        CAST(:endTime AS datetime) AS end_time
    FROM schedule_with_bounds
),
all_slots AS (
    SELECT 
        start_time AS startTime,
        end_time AS endTime
    FROM free_slots
    WHERE start_time >= :startTime AND end_time <= :endTime

    UNION

    SELECT 
        start_time AS startTime,
        end_time AS endTime
    FROM first_slot
    WHERE end_time > start_time AND end_time <= :endTime

    UNION

    SELECT 
        start_time AS startTime,
        end_time AS endTime
    FROM last_slot
    WHERE end_time > start_time AND start_time >= :startTime
)
SELECT 
    startTime, 
    endTime
FROM all_slots

UNION

-- TH1: Không có lịch nào => trả nguyên đoạn trống
SELECT 
    CAST(:startTime AS datetime) AS startTime,
    CAST(:endTime AS datetime) AS endTime
WHERE NOT EXISTS (
    SELECT 1
    FROM schedule_with_bounds
)
ORDER BY startTime
""", nativeQuery = true)
    List<FreeTimeSlot> getAvailableSlotsBetween(
            @Param("roomName") String roomName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query(value = """
    SELECT 
        r.name AS roomName,
        CONCAT(u.firstname, ' ', u.lastname) AS fullName,
        u.email AS email,
        s.start_time AS startTime,
        s.end_time AS endTime
    FROM (
        SELECT * FROM schedules 
        WHERE start_time < :endTime AND end_time > :startTime
    ) s
    INNER JOIN (
        SELECT * FROM rooms WHERE is_used = 1
    ) r ON r.room_id = s.room_id
    INNER JOIN users u ON u.user_id = s.created_by
""", nativeQuery = true)
    List<ScheduleConflictInfo> findConflictingSchedulesWithRoomAndUserInfo(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
    SELECT DISTINCT u FROM Schedule s
    JOIN s.participants u
    WHERE u.department.name = :departmentName
      AND s.startTime < :endTime
      AND s.endTime > :startTime
    """)
    List<User> findConflictedUsersByDepartment(
            @Param("departmentName") String departmentName,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    Optional<Schedule> findByRoomAndStartTime(Room room, LocalDateTime startTime);
}

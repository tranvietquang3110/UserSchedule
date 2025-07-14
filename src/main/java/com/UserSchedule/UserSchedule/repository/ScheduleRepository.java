package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
}

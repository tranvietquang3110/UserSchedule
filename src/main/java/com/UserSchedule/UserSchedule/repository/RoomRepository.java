package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.dto.roomRepository.RoomWithStatus;
import com.UserSchedule.UserSchedule.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByRoomId(Integer id);
    Optional<Room> findByName(String name);
    List<Room> findAllByIsUsedTrue();
    boolean existsByNameAndRoomIdNot(String name, int roomId);

    @Query("""
    SELECT new com.UserSchedule.UserSchedule.dto.roomRepository.RoomWithStatus(
        r.name,
        r.location,
        r.capacity,
        CASE 
            WHEN COUNT(s) = 0 THEN 'UNBOOKED' 
            ELSE 'BOOKED' 
        END
    )
    FROM Room r
    LEFT JOIN Schedule s ON s.room = r AND s.endTime > :now
    GROUP BY r.name, r.location, r.capacity
    """)
    List<RoomWithStatus> findRoomsWithStatus(@Param("now") LocalDateTime now);

    @Query("""
        SELECT r FROM Room r
        WHERE r.roomId NOT IN (
            SELECT s.room.roomId
            FROM Schedule s
            WHERE s.startTime < :endTime
              AND s.endTime > :startTime
        )
    """)
    List<Room> findAvailableRoomsBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}

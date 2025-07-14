package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByRoomId(Integer id);
    Optional<Room> findByName(String name);
    List<Room> findAllByIsUsedTrue();
    boolean existsByNameAndRoomIdNot(String name, int roomId);
}

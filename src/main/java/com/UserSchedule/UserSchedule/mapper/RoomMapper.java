package com.UserSchedule.UserSchedule.mapper;

import com.UserSchedule.UserSchedule.dto.request.RoomRequest;
import com.UserSchedule.UserSchedule.dto.response.RoomResponse;
import com.UserSchedule.UserSchedule.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    // Request -> Entity
    @Mapping(target = "roomId", ignore = true) // Không set roomId khi tạo mới
    @Mapping(target = "isUsed", constant = "true") // Mặc định là true khi tạo mới
    Room toRoom(RoomRequest request);

    // Entity -> Response
    RoomResponse toRoomResponse(Room room);

    List<RoomResponse> toRoomResponseList(List<Room> rooms);
}

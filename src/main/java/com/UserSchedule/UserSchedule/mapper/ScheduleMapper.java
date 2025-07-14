package com.UserSchedule.UserSchedule.mapper;
import com.UserSchedule.UserSchedule.dto.request.ScheduleCreationRequest;
import com.UserSchedule.UserSchedule.dto.response.*;
import com.UserSchedule.UserSchedule.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {
    @Mapping(target = "room", source = "room")
    @Mapping(target = "participants", source = "participants")
    ScheduleResponse toScheduleResponse(Schedule schedule);

    List<ScheduleResponse> toScheduleResponseList(List<Schedule> schedule);

    RoomResponse toRoomResponse(Room room);

    UserResponse toUserResponse(User user);

    @Mapping(target = "scheduleId", ignore = true) // Vì là tạo mới
    @Mapping(target = "room", ignore = true) // set trong service
    @Mapping(target = "participants", ignore = true) // set trong service
    @Mapping(target = "createdBy", ignore = true) // set trong service
    Schedule toSchedule(ScheduleCreationRequest request);
}

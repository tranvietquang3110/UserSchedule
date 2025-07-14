package com.UserSchedule.UserSchedule.dto.response;

import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    Integer scheduleId;
    String title;
    ScheduleType type;
    LocalDateTime startTime;
    LocalDateTime endTime;

    RoomResponse room; // Room info
    List<UserResponse> participants;
}

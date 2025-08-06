package com.UserSchedule.UserSchedule.dto.request;

import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SchedulePatchRequest {
    String title;
    ScheduleType type;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String roomName;
}

package com.UserSchedule.UserSchedule.dto.request;

import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleCreationRequest {
    @NotBlank(message = "TITLE_NOT_FILL")
    String title;

    @NotNull(message = "TYPE_NOT_FILL")
    ScheduleType type;

    @NotNull(message = "START_TIME_NOT_FILL")
    @Future(message = "START_TIME_MUST_BE_FUTURE")
    LocalDateTime startTime;

    @NotNull(message = "END_TIME_NOT_FILL")
    @Future(message = "END_TIME_MUST_BE_FUTURE")
    LocalDateTime endTime;

    @NotNull(message = "ROOM_ID_NOT_FILL")
    Integer roomId;

    @NotEmpty(message = "PARTICIPANT_IDS_NOT_FILL")
    Set<Integer> participantIds; // userId của các participant
}

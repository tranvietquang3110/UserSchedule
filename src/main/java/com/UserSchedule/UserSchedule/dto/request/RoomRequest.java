package com.UserSchedule.UserSchedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    @NotBlank(message = "ROOM_NAME_NOT_BLANK")
    String name;

    @NotBlank(message = "ROOM_LOCATION_NOT_BLANK")
    String location;

    @NotNull(message = "ROOM_CAPACITY_NOT_NULL")
    @Min(value = 1, message = "ROOM_CAPACITY_MUST_BE_POSITIVE")
    Integer capacity;
}

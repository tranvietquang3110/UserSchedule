package com.UserSchedule.UserSchedule.dto.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AIResponse {
    String message;
    String role;
    Integer order;
}

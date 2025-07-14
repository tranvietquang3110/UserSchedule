package com.UserSchedule.UserSchedule.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer userId;
    String keycloakId;
    String username;
    String firstname;
    String lastname;
    LocalDate dob;
    String email;
    DepartmentResponse department;
}

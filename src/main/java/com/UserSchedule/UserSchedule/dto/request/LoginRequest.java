package com.UserSchedule.UserSchedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotBlank(message = "USERNAME_NOT_FILL")
    String username;
    @NotBlank(message = "PASSWORD_NOT_FILL")
    String password;
}

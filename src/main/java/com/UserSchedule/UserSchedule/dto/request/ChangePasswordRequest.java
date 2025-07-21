package com.UserSchedule.UserSchedule.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "OLD_PASSWORD_NOT_FILL")
    String oldPassword;
    @NotBlank(message = "NEW_PASSWORD_NOT_FILL")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    String newPassword;
}

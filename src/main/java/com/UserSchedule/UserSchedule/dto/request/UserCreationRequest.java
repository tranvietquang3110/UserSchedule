package com.UserSchedule.UserSchedule.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "USERNAME_NOT_FILL")
    private String username;

    @NotBlank(message = "PASSWORD_NOT_FILL")
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    private String password;

    @NotBlank(message = "FIRSTNAME_NOT_FILL")
    private String firstname;

    @NotBlank(message = "LASTNAME_NOT_FILL")
    private String lastname;

    @NotNull(message = "DOB_NOT_FILL")
    @Past(message = "DOB_MUST_BE_PAST")
    private LocalDate dob;

    private Integer departmentId;

    @NotBlank(message = "EMAIL_NOT_FILL")
    @Email(message = "EMAIL_INVALID")
    private String email;
}

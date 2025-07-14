package com.UserSchedule.UserSchedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @NotBlank(message = "FIRSTNAME_NOT_FILL")
    private String firstname;

    @NotBlank(message = "LASTNAME_NOT_FILL")
    private String lastname;

    @NotNull(message = "DOB_NOT_FILL")
    @Past(message = "DOB_MUST_BE_PAST")
    private LocalDate dob;

    private Integer departmentId;
}

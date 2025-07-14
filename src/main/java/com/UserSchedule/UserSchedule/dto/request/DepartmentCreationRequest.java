package com.UserSchedule.UserSchedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentCreationRequest {
    @NotBlank(message = "DEPARTMENT_NAME_NOT_FILL")
    String name;
}

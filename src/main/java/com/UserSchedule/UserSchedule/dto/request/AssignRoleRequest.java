package com.UserSchedule.UserSchedule.dto.request;

import com.UserSchedule.UserSchedule.enum_type.RoleType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotNull(message = "TYPE_NOT_FILL")
    RoleType roleName;
}

package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.AssignRoleRequest;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Tag(name = "Role", description = "Phân quyền và quản lý vai trò người dùng")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    RoleService roleService;

    @PostMapping("/assign/{userId}")
    @Operation(
            summary = "Gán role cho user",
            description = """
            Gán một vai trò cụ thể (VD: ADMIN, USER, MANAGER) cho người dùng theo userId.
            **Yêu cầu quyền: ADMIN**
            """
    )
    public ApiResponse<String> assignRoleToUser(
            @Parameter(description = "ID của người dùng cần gán role", example = "3")
            @PathVariable int userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        roleService.assignRole(request, userId);
        return ApiResponse.<String>builder()
                .message("Assigned role successfully")
                .data("Role " + request.getRoleName().name() + " assigned to userId " + userId)
                .build();
    }

    @DeleteMapping("/unassign/{userId}")
    @Operation(
            summary = "Huỷ role khỏi user",
            description = """
            Xoá một vai trò đã gán khỏi người dùng theo userId.
            **Yêu cầu quyền: ADMIN**
            """
    )
    public ApiResponse<String> unassignRoleFromUser(
            @Parameter(description = "ID của người dùng cần huỷ role", example = "3")
            @PathVariable int userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        roleService.unassignRole(request, userId);
        return ApiResponse.<String>builder()
                .message("Unassigned role successfully")
                .data("Role " + request.getRoleName().name() + " removed from userId " + userId)
                .build();
    }
}
package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.DepartmentCreationRequest;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.dto.response.DepartmentResponse;
import com.UserSchedule.UserSchedule.dto.response.RoomResponse;
import com.UserSchedule.UserSchedule.dto.response.UserResponse;
import com.UserSchedule.UserSchedule.service.DepartmentService;
import com.UserSchedule.UserSchedule.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Department", description = "Quản lý phòng ban")
public class DepartmentController {

    DepartmentService departmentService;
    UserService userService;

    @PostMapping
    @Operation(
            summary = "Tạo phòng ban",
            description = "Tạo mới một phòng ban. **Yêu cầu quyền: ADMIN**"
    )
    public ApiResponse<DepartmentResponse> createDepartment(
            @RequestBody @Valid DepartmentCreationRequest request) {
        return ApiResponse.<DepartmentResponse>builder()
                .message("Department created successfully")
                .data(departmentService.createDepartment(request))
                .build();
    }

    @DeleteMapping("/{departmentId}")
    @Operation(
            summary = "Xoá phòng ban",
            description = "Xoá phòng ban theo ID. **Yêu cầu quyền: ADMIN**"
    )
    public ApiResponse<Void> deleteDepartment(
            @Parameter(description = "ID của phòng ban cần xoá", example = "1")
            @PathVariable int departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ApiResponse.<Void>builder()
                .message("Department deleted successfully")
                .build();
    }

    @GetMapping("/all")
    @Operation(
            summary = "Lấy tất cả phòng ban",
            description = "Trả về danh sách tất cả phòng ban đang hoạt động"
    )
    public ApiResponse<List<DepartmentResponse>> getAllDepartments() {
        return ApiResponse.<List<DepartmentResponse>>builder()
                .message("Get all departments successfully")
                .data(departmentService.getDepartments())
                .build();
    }

    @GetMapping("/{departmentId}/users")
    @Operation(
            summary = "Lấy danh sách user theo phòng ban",
            description = """
                    Trả về danh sách người dùng thuộc một phòng ban cụ thể.  
                    **Yêu cầu quyền: ADMIN hoặc MANAGER cùng phòng ban**
                    """
    )
    public ApiResponse<List<UserResponse>> getUsersByDepartment(
            @Parameter(description = "ID phòng ban", example = "2")
            @PathVariable int departmentId) {
        return ApiResponse.<List<UserResponse>>builder()
                .message("Get users by department successfully")
                .data(userService.getUserByDepartment(departmentId))
                .build();
    }

    @GetMapping("/{departmentId}")
    @Operation(
            summary = "Lấy thông tin phòng ban theo ID",
            description = "Trả về thông tin chi tiết của phòng ban theo ID"
    )
    public ApiResponse<DepartmentResponse> getDepartmentById(
            @Parameter(description = "ID phòng ban", example = "3")
            @PathVariable int departmentId) {
        return ApiResponse.<DepartmentResponse>builder()
                .message("Get department by ID successfully")
                .data(departmentService.getDepartmentById(departmentId))
                .build();
    }
}

package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.ScheduleByDepartmentRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleCreationRequest;
import com.UserSchedule.UserSchedule.dto.request.ScheduleUpdateRequest;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.dto.response.ScheduleResponse;
import com.UserSchedule.UserSchedule.dto.scheduleRepository.FreeTimeSlot;
import com.UserSchedule.UserSchedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Schedule", description = "Quản lý lịch họp, lịch làm việc")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController {
    ScheduleService scheduleService;

    @PostMapping()
    @Operation(
            summary = "Tạo lịch",
            description = """
            Tạo một lịch họp hoặc làm việc mới.
            **Yêu cầu quyền: MANAGER hoặc ADMIN**
            """)
    public ApiResponse<ScheduleResponse> createSchedule(@RequestBody @Valid ScheduleCreationRequest request) {
        return ApiResponse.<ScheduleResponse>builder()
                .message("Create schedule successfully")
                .data(scheduleService.createSchedule(request))
                .build();
    }

    @PostMapping("/departments/{departmentName}")
    @Operation(
            summary = "Tạo lịch cho phòng ban",
            description = """
            Tạo lịch họp/làm việc áp dụng cho toàn bộ phòng ban.
            **Yêu cầu quyền: MANAGER (trong cùng phòng ban) hoặc ADMIN**
            """)
    public ApiResponse<ScheduleResponse> createScheduleForDepartment(
            @PathVariable String departmentName,
            @RequestBody @Valid ScheduleByDepartmentRequest request) {
        return ApiResponse.<ScheduleResponse>builder()
                .message("Schedule created for department successfully")
                .data(scheduleService.createScheduleByDepartment(request, departmentName))
                .build();
    }

    @GetMapping("/users/{keycloakId}")
    @Operation(
            summary = "Lấy danh sách lịch theo keycloakId",
            description = """
            Trả về tất cả lịch của người dùng dựa trên userId.
            **Không yêu cầu quyền đặc biệt (chỉ cần đăng nhập).**
            """)
    public ApiResponse<List<ScheduleResponse>> getScheduleByUserId(@PathVariable String keycloakId) {
        return ApiResponse.<List<ScheduleResponse>>builder()
                .message("Get schedules by userId successfully")
                .data(scheduleService.getSchedulesByKeycloakId(keycloakId))
                .build();
    }

    @PutMapping("/{scheduleId}")
    @Operation(
            summary = "Cập nhật lịch",
            description = """
            Cập nhật nội dung của một lịch cụ thể.
            **Yêu cầu quyền: MANAGER (chỉ với lịch do mình tạo) hoặc ADMIN**
            """)
    public ApiResponse<ScheduleResponse> updateSchedule(
            @PathVariable int scheduleId,
            @RequestBody @Valid ScheduleUpdateRequest request) {
        return ApiResponse.<ScheduleResponse>builder()
                .message("Update schedule successfully")
                .data(scheduleService.updateSchedule(scheduleId, request))
                .build();
    }

    @GetMapping("/{scheduleId}")
    @Operation(
            summary = "Lấy lịch theo ID",
            description = """
            Trả về thông tin chi tiết của một lịch theo ID.
            **Không yêu cầu quyền đặc biệt (chỉ cần đăng nhập).**
            """)
    public ApiResponse<ScheduleResponse> getScheduleById(@PathVariable int scheduleId) {
        return ApiResponse.<ScheduleResponse>builder()
                .message("Get schedule by id successfully")
                .data(scheduleService.getScheduleById(scheduleId))
                .build();
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(
            summary = "Xoá lịch",
            description = """
            Xoá lịch theo ID.
            **Yêu cầu quyền: MANAGER (chỉ được xoá lịch do mình tạo) hoặc ADMIN**
            """)
    public ApiResponse<?> deleteSchedule(@PathVariable int scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ApiResponse.builder()
                .message("Delete schedule successfully")
                .build();
    }

    @GetMapping("/free/{roomName}")
    public  ApiResponse<List<FreeTimeSlot>> getFreeTimeSlot
            (@PathVariable String roomName, @RequestParam("startDate")LocalDateTime startDate
                    , @RequestParam("endDate") LocalDateTime endDate) {
        return ApiResponse.<List<FreeTimeSlot>>builder()
                .message("Get successfully")
                .data(scheduleService.getAvailableSlotsBetween(roomName, startDate, endDate))
                .build();
    }

    @PostMapping("/simple")
    @Operation(
            summary = "Tạo lịch đơn giản (không có người tham gia/phòng ban)",
            description = """
        Tạo một lịch cá nhân hoặc lịch đơn giản không cần participant hoặc phòng ban.
        **Yêu cầu quyền: bất kỳ người dùng đã đăng nhập.**
        """)
    public ApiResponse<ScheduleResponse> createSimpleSchedule(
            @RequestBody @Valid ScheduleByDepartmentRequest request
    ) {
        return ApiResponse.<ScheduleResponse>builder()
                .message("Simple schedule created successfully")
                .data(scheduleService.createSimpleSchedule(request))
                .build();
    }
}
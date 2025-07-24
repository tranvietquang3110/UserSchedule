package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.RoomRequest;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.dto.response.RoomResponse;
import com.UserSchedule.UserSchedule.dto.response.ScheduleResponse;
import com.UserSchedule.UserSchedule.dto.roomRepository.RoomWithStatus;
import com.UserSchedule.UserSchedule.service.RoomService;
import com.UserSchedule.UserSchedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Room", description = "Quản lý phòng họp")
public class RoomController {
    RoomService roomService;
    ScheduleService scheduleService;
    @PostMapping
    @Operation(
            summary = "Tạo phòng",
            description = """
            Tạo mới một phòng họp hoặc phòng làm việc.
            **Yêu cầu quyền: ADMIN**
            """
    )
    public ApiResponse<RoomResponse> createRoom(@RequestBody @Valid RoomRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .message("Room created successfully")
                .data(roomService.createRoom(request))
                .build();
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Cập nhật phòng",
            description = """
            Cập nhật thông tin phòng theo ID.
            **Yêu cầu quyền: ADMIN**
            """)
    public ApiResponse<RoomResponse> updateRoom(
            @Parameter(description = "ID của phòng cần cập nhật", example = "1") @PathVariable int id,
            @RequestBody @Valid RoomRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .message("Room updated successfully")
                .data(roomService.updateRoom(request, id))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoá phòng",
            description = """
            Xoá (ẩn) phòng theo ID bằng cách đánh dấu không còn được sử dụng.
            **Yêu cầu quyền: ADMIN**
            """
    )
    public ApiResponse<Void> deleteRoom(
            @Parameter(description = "ID của phòng cần xoá", example = "2") @PathVariable int id) {
        roomService.deleteRoom(id);
        return ApiResponse.<Void>builder()
                .message("Room deleted successfully")
                .build();
    }

    @GetMapping("/all")
    @Operation(
            summary = "Lấy danh sách tất cả phòng",
            description = """
            Trả về danh sách tất cả phòng đang hoạt động (isUsed = true).
            **Không yêu cầu quyền đặc biệt (chỉ cần đăng nhập).**
            """
    )
    public ApiResponse<List<RoomResponse>> getAllRooms() {
        return ApiResponse.<List<RoomResponse>>builder()
                .message("Get all rooms successfully")
                .data(roomService.getAll())
                .build();
    }

    @GetMapping("/{roomId}")
    @Operation(
            summary = "Lấy phòng theo ID",
            description = """
            Trả về thông tin phòng dựa trên roomId.
            Chỉ trả về phòng có trạng thái isUsed = true.
            **Không yêu cầu quyền đặc biệt (chỉ cần đăng nhập).**
            """
    )
    public ApiResponse<RoomResponse> getRoomById(@PathVariable int roomId) {
        return ApiResponse.<RoomResponse>builder()
                .message("Get room by ID successfully")
                .data(roomService.getRoomById(roomId))
                .build();
    }

    @GetMapping("/with-status")
    @Operation(
            summary = "Lấy danh sách phòng kèm trạng thái BOOKED / UNBOOKED",
            description = """
    Trả về danh sách tất cả phòng với trạng thái hiện tại:
    - `BOOKED`: đang có lịch sử dụng (endTime > hiện tại)
    - `UNBOOKED`: không có lịch nào sắp tới

    **Không yêu cầu quyền đặc biệt (chỉ cần đăng nhập).**
    """
    )
    public ApiResponse<List<RoomWithStatus>> getRoomsWithStatus() {
        return ApiResponse.<List<RoomWithStatus>>builder()
                .message("Get all rooms with status successfully")
                .data(roomService.getRoomWithStatus())
                .build();
    }

    @GetMapping("/{roomId}/reservations/history")
    @Operation(
            summary = "Lấy lịch sử đặt phòng",
            description = """
    Trả về danh sách các lịch đã từng đặt cho phòng theo roomId.
    Có thể bao gồm cả quá khứ và tương lai.
    **Yêu cầu quyền: ADMIN hoặc MANAGER**
    """
    )
    public ApiResponse<List<ScheduleResponse>> getRoomReservationHistory(@PathVariable int roomId) {
        return ApiResponse.<List<ScheduleResponse>>builder()
                .message("Get room reservation history successfully")
                .data(roomService.getRoomReservationHistory(roomId))
                .build();
    }

    @GetMapping("/available")
    public ApiResponse<List<RoomResponse>> getAvailableRooms(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<RoomResponse> availableRooms = roomService.getAvailableRoom(startDate, endDate);

        return ApiResponse.<List<RoomResponse>>builder()
                .message("Available rooms fetched successfully")
                .data(availableRooms)
                .build();
    }

    @GetMapping("/rooms/by-capacity")
    public List<RoomResponse> getRoomsByCapacityRange(
            @RequestParam int min,
            @RequestParam int max
    ) {
        return roomService.getRoomsByRangeCapacity(min, max);
    }
}
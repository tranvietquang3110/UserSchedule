package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.RoomRequest;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.dto.response.RoomResponse;
import com.UserSchedule.UserSchedule.service.RoomService;
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
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Room", description = "Quản lý phòng họp")
public class RoomController {

    RoomService roomService;

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
}
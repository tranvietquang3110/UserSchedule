package com.UserSchedule.UserSchedule.controller;

import com.UserSchedule.UserSchedule.dto.request.*;
import com.UserSchedule.UserSchedule.dto.response.ApiResponse;
import com.UserSchedule.UserSchedule.dto.response.TokenResponse;
import com.UserSchedule.UserSchedule.dto.response.UserResponse;
import com.UserSchedule.UserSchedule.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User", description = "Quản lý người dùng (User Management)")
public class UserController {
    UserService userService;

    @PostMapping()
    @Operation(summary = "API kiểm tra", description = "API test đơn giản, không yêu cầu phân quyền")
    public ApiResponse hello() {
        return ApiResponse.builder().message("hello world").statusCode(200).build();
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = """
            Tạo người dùng mới trong hệ thống và tài khoản trên Keycloak.
            **Không yêu cầu xác thực hoặc phân quyền.**
            """)
    public ApiResponse<UserResponse> registerUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("User register successfully")
                .data(userService.createUser(request)).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = """
            Đăng nhập hệ thống, trả về access token sau khi xác thực thành công.
            **Không yêu cầu token.**
            """)
    public ApiResponse<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return  ApiResponse.<TokenResponse>builder()
                .message("Login successfully")
                .data(userService.login(request))
                .build();
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy danh sách tất cả người dùng", description = """
            Trả về toàn bộ danh sách người dùng.
            **Yêu cầu phân quyền ADMIN.**
            """)
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .message("Get Users Successfully")
                .data(userService.getUsers()).build();
    }

    @GetMapping("/my-profile")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy thông tin cá nhân", description = """
            Trả về thông tin người dùng hiện tại từ token.
            **Yêu cầu đã đăng nhập (ROLE_USER trở lên).**
            """)
    public ApiResponse<UserResponse> getMyProfile() {
        return ApiResponse.<UserResponse>builder()
                .message("Get my profile successfully")
                .data(userService.getMyProfile()).build();
    }

    @GetMapping("/{keycloakId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Lấy thông tin user theo ID",
            description = """
                Trả về thông tin chi tiết của một người dùng bất kỳ.
                **Yêu cầu đã đăng nhập (ROLE_USER trở lên).**
                """,
            parameters = {
                    @Parameter(name = "keycloakId", description = "ID của người dùng cần lấy thông tin", example = "1")
            }
    )
    public ApiResponse<UserResponse> getUser(@PathVariable String keycloakId) {
        return  ApiResponse.<UserResponse>builder()
                .message("Get user successfully")
                .data(userService.getUserById(keycloakId))
                .build();
    }

    @PutMapping("/{keycloakId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Cập nhât thông tin user theo ID",
            description = """
                Cập nhật thông tin chi tiết của một người dùng bất kỳ.
                **Yêu cầu là chính USER đó hoặc ADMIN**
                """,
            parameters = {
                    @Parameter(name = "keycloakId", description = "ID của người dùng cần lấy thông tin", example = "1")
            }
    )
    public ApiResponse<UserResponse> updateProfile(@PathVariable String keycloakId, @RequestBody @Valid UserUpdateRequest request) {
        return  ApiResponse.<UserResponse>builder()
                .message("Update user successfully")
                .data(userService.updateUserProfile(request, keycloakId))
                .build();
    }

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable String userId,
            @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }
}


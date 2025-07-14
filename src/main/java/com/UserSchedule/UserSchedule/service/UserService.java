package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.identity.*;
import com.UserSchedule.UserSchedule.dto.request.AssignRoleRequest;
import com.UserSchedule.UserSchedule.dto.request.LoginRequest;
import com.UserSchedule.UserSchedule.dto.request.UserCreationRequest;
import com.UserSchedule.UserSchedule.dto.request.UserUpdateRequest;
import com.UserSchedule.UserSchedule.dto.response.TokenResponse;
import com.UserSchedule.UserSchedule.dto.response.UserResponse;
import com.UserSchedule.UserSchedule.entity.Department;
import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.mapper.UserMapper;
import com.UserSchedule.UserSchedule.repository.DepartmentRepository;
import com.UserSchedule.UserSchedule.repository.IdentityClient;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import com.UserSchedule.UserSchedule.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserMapper userMapper;
    UserRepository userRepository;
    DepartmentRepository departmentRepository;
    IdentityClient identityClient;
    SecurityUtils securityUtils;
    @Value("${keycloak.client-id}")
    @NonFinal
    String clientId;

    @Value("${keycloak.client-secret}")
    @NonFinal
    String clientSecret;

    @Value("${keycloak.scope}")
    @NonFinal
    String scope;

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User user = userMapper.toUser(request);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", scope);
        TokenExchangeResponse token = identityClient.getClientToken(form);

        if (request.getDepartmentId()!=null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
            user.setDepartment(department);
        }
        var creationResponse = identityClient.createUser(
                "Bearer " + token.getAccessToken(),
                UserCreationParam.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstname())
                        .lastName(request.getLastname())
                        .email(request.getEmail())
                        .enabled(true)
                        .emailVerified(false)
                        .credentials(List.of(Credential.builder()
                                .type("password")
                                .temporary(false)
                                .value(request.getPassword())
                                .build()))
                        .build());
        String userId = extractUserId(creationResponse);
        user.setKeycloakId(userId);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public TokenResponse login(LoginRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", request.getUsername());
        form.add("password", request.getPassword());
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", scope);
        TokenExchangeResponse token = identityClient.getClientToken(form);
        return TokenResponse.builder().accessToken(token.getAccessToken()).build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getUsers() {
        return userMapper.toUserResponseList(userRepository.findAll());
    }

    @PreAuthorize("hasAuthority('USER')")
    public UserResponse getMyProfile() {
        String keycloakId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    private String extractUserId(ResponseEntity<?> response){
        String location = response.getHeaders().get("Location").getFirst();
        String[] splitStr = location.split("/");
        return splitStr[splitStr.length - 1];
    }

    @PreAuthorize("hasAuthority('USER')")
    public UserResponse getUserById(int id) {
        User user = userRepository.findByUserId(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    public List<UserResponse> getUserByDepartment(Integer id) {
        User currentUser = securityUtils.getCurrentUser();

        boolean isAdmin = securityUtils.isAdmin();
        boolean isSameDepartment = Objects.equals(currentUser.getDepartment().getDepartmentId(), id);

        if (!isAdmin && !isSameDepartment) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<User> users = departmentRepository.findUsersByDepartmentId(id);
        return userMapper.toUserResponseList(users);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public UserResponse updateUserProfile(UserUpdateRequest request, int userId) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getUserId() != userId && !securityUtils.isAdmin()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUserFromUpdateRequest(request, user);
        Department department = departmentRepository.findByDepartmentId(request.getDepartmentId())
                .orElseThrow(()-> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        user.setDepartment(department);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", scope);
        TokenExchangeResponse token = identityClient.getClientToken(form);
        String accessToken = "Bearer " + token.getAccessToken();

        KeycloakUserUpdateRequest keycloakUserUpdateRequest = KeycloakUserUpdateRequest.builder()
                        .lastName(request.getLastname())
                        .firstName(request.getFirstname())
                        .build();

        identityClient.updateUserProfile(accessToken, user.getKeycloakId(), keycloakUserUpdateRequest);
        return userMapper.toUserResponse(userRepository.save(user));
    }
}

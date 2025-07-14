package com.UserSchedule.UserSchedule.service;

import com.UserSchedule.UserSchedule.dto.identity.RoleRepresentation;
import com.UserSchedule.UserSchedule.dto.identity.TokenExchangeResponse;
import com.UserSchedule.UserSchedule.dto.request.AssignRoleRequest;
import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.repository.IdentityClient;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    UserRepository userRepository;
    IdentityClient identityClient;

    @Value("${keycloak.client-id}")
    @NonFinal
    String clientId;

    @Value("${keycloak.client-secret}")
    @NonFinal
    String clientSecret;

    @Value("${keycloak.scope}")
    @NonFinal
    String scope;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public void assignRole(AssignRoleRequest request, int userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", scope);

        TokenExchangeResponse token = identityClient.getClientToken(form);
        String accessToken = "Bearer " + token.getAccessToken();

        RoleRepresentation roleRepresentation = identityClient
                .getRealmRole(accessToken, request.getRoleName());
        identityClient.assignRealmRoleToUser(accessToken, user.getKeycloakId(), List.of(roleRepresentation));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public void unassignRole(AssignRoleRequest request, int userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy token từ Keycloak
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("scope", scope);

        TokenExchangeResponse token = identityClient.getClientToken(form);
        String accessToken = "Bearer " + token.getAccessToken();

        // Lấy thông tin role
        RoleRepresentation role = identityClient
                .getRealmRole(accessToken, request.getRoleName());

        // Gọi API để unassign role
        identityClient.unassignRealmRoleFromUser(accessToken, user.getKeycloakId(), List.of(role));
    }
}

package com.UserSchedule.UserSchedule.repository;

import com.UserSchedule.UserSchedule.dto.identity.*;
import com.UserSchedule.UserSchedule.enum_type.RoleType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "identity-client", url = "${keycloak.server-url}")
public interface IdentityClient {
    @PostMapping(value = "/realms/UserSchedule/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse getClientToken(@RequestBody MultiValueMap<String, String> form);

    @PostMapping(value = "/admin/realms/UserSchedule/users",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(
            @RequestHeader("authorization") String token,
            @RequestBody UserCreationParam param);

    @GetMapping(value = "/admin/realms/UserSchedule/roles/{roleName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    RoleRepresentation getRealmRole(
            @RequestHeader("Authorization") String token,
            @PathVariable("roleName") RoleType roleName);

    @PostMapping(value = "/admin/realms/UserSchedule/users/{userId}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void assignRealmRoleToUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String keycloakId,
            @RequestBody List<RoleRepresentation> roles);

    @DeleteMapping(value = "/admin/realms/UserSchedule/users/{userId}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    void unassignRealmRoleFromUser(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId,
            @RequestBody List<RoleRepresentation> roles);

    @PutMapping("/admin/realms/UserSchedule/users/{userId}")
    void updateUserProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId,
            @RequestBody KeycloakUserUpdateRequest request
    );
    @PutMapping("/admin/realms/UserSchedule/users/{userId}/reset-password")
    void resetUserPassword(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId,
            @RequestBody CredentialRepresentation passwordRequest
    );

    @GetMapping(value = "/admin/realms/UserSchedule/users/{userId}/role-mappings/realm/composite",
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<RoleRepresentation> getUserRealmRoles(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") String userId);
}

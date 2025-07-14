package com.UserSchedule.UserSchedule.utils;

import com.UserSchedule.UserSchedule.entity.User;
import com.UserSchedule.UserSchedule.enum_type.RoleType;
import com.UserSchedule.UserSchedule.exception.AppException;
import com.UserSchedule.UserSchedule.exception.ErrorCode;
import com.UserSchedule.UserSchedule.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByKeycloakId(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(RoleType.ADMIN.name()));
    }

    public boolean hasRole(RoleType role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(role.name()));
    }
}
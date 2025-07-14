package com.UserSchedule.UserSchedule.mapper;

import com.UserSchedule.UserSchedule.dto.request.UserCreationRequest;
import com.UserSchedule.UserSchedule.dto.request.UserUpdateRequest;
import com.UserSchedule.UserSchedule.dto.response.UserResponse;
import com.UserSchedule.UserSchedule.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "keycloakId", ignore = true) // bạn sẽ set sau khi gọi Keycloak
    @Mapping(target = "department", ignore = true) // set trong service
    User toUser(UserCreationRequest request);

    @Mapping(source = "department", target = "department")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "department", ignore = true)
    void updateUserFromUpdateRequest(UserUpdateRequest request, @MappingTarget User user);
}

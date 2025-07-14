package com.UserSchedule.UserSchedule.dto.identity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class KeycloakUserUpdateRequest {
    String firstName;
    String lastName;
}

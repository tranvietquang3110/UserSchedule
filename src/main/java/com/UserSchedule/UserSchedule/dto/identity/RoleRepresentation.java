package com.UserSchedule.UserSchedule.dto.identity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRepresentation {
     String id;
     String name;
     String description;
     boolean composite;
     boolean clientRole;
     String containerId;
}

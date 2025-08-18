package com.UserSchedule.UserSchedule.dto.roomRepository;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomWithStatus {
    int roomId;
    String name;
    String location;
    Integer capacity;
    String status;
}

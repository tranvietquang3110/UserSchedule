package com.UserSchedule.UserSchedule.dto.scheduleRepository;

import java.time.LocalDateTime;

public interface ScheduleConflictInfo {
    String getRoomName();
    String getFullName();
    String getEmail();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}

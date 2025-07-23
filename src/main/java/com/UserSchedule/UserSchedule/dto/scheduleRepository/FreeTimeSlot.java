package com.UserSchedule.UserSchedule.dto.scheduleRepository;

import java.time.LocalDateTime;
public interface FreeTimeSlot {
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
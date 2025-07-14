package com.UserSchedule.UserSchedule.entity;

import com.UserSchedule.UserSchedule.enum_type.ScheduleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "schedules")
@Builder
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer scheduleId;
    String title;
    @Enumerated(EnumType.STRING)
    ScheduleType type;
    LocalDateTime startTime;
    LocalDateTime endTime;
    @ManyToOne
    @JoinColumn(name = "room_id")
    Room room;

    @ManyToMany
    @JoinTable(
            name = "participants",
            joinColumns = @JoinColumn(name = "schedule_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    Set<User> participants = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "created_by")
    User createdBy;
}

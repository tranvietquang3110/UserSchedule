package com.UserSchedule.UserSchedule.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer userId;

    @Column(nullable = false)
    String keycloakId;

    @Column(nullable = false)
    String username;

    String firstname;
    String lastname;

    LocalDate dob;
    String email;

    @ManyToOne
    @JoinColumn(name = "department_id")
    Department department;
}

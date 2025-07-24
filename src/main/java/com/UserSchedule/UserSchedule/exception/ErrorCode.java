package com.UserSchedule.UserSchedule.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ==== Validate Input Errors (2000+) ====
    USERNAME_NOT_FILL(2000, "Username must not be blank", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_FILL(2001, "Password must not be blank", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(2002, "Password must at least {min} characters", HttpStatus.BAD_REQUEST),
    FIRSTNAME_NOT_FILL(2003, "First name must not be blank", HttpStatus.BAD_REQUEST),
    LASTNAME_NOT_FILL(2004, "Last name must not be blank", HttpStatus.BAD_REQUEST),
    DOB_NOT_FILL(2005, "Date of birth must not be null", HttpStatus.BAD_REQUEST),
    DOB_MUST_BE_PAST(2006, "Date of birth must be in the past", HttpStatus.BAD_REQUEST),
    ROLE_ID_NOT_FILL(2007, "Role ID must not be null", HttpStatus.BAD_REQUEST),
    DEPARTMENT_ID_NOT_FILL(2008, "Department ID must not be null", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FILL(2009, "Email must not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(2010, "Email format is invalid", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NAME_NOT_FILL(2011, "Department name must not be blank", HttpStatus.BAD_REQUEST),
    TITLE_NOT_FILL(2012, "Title must not be blank", HttpStatus.BAD_REQUEST),
    TYPE_NOT_FILL(2013, "Schedule type must not be null", HttpStatus.BAD_REQUEST),
    START_TIME_NOT_FILL(2014, "Start time must not be null", HttpStatus.BAD_REQUEST),
    END_TIME_NOT_FILL(2015, "End time must not be null", HttpStatus.BAD_REQUEST),
    START_TIME_MUST_BE_FUTURE(2016, "Start time must be in the future", HttpStatus.BAD_REQUEST),
    END_TIME_MUST_BE_FUTURE(2017, "End time must be in the future", HttpStatus.BAD_REQUEST),
    ROOM_ID_NOT_FILL(2018, "Room ID must not be null", HttpStatus.BAD_REQUEST),
    PARTICIPANT_IDS_NOT_FILL(2019, "Participant list must not be empty", HttpStatus.BAD_REQUEST),
    ROOM_NAME_NOT_BLANK(2020, "Room name must not be blank", HttpStatus.BAD_REQUEST),
    ROOM_LOCATION_NOT_BLANK(2021, "Room location must not be blank", HttpStatus.BAD_REQUEST),
    ROOM_CAPACITY_NOT_NULL(2022, "Room capacity must not be null", HttpStatus.BAD_REQUEST),
    ROOM_CAPACITY_MUST_BE_POSITIVE(2023, "Room capacity must be greater than 0", HttpStatus.BAD_REQUEST),
    CREATED_BY_NOT_FILL(2024, "Created by must not be blank", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_NOT_FILL(2025, "New password must not be blank", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_NOT_FILL(2026, "Old password must not be blank", HttpStatus.BAD_REQUEST),
    // ==== Business Logic Errors (2100+) ====
    USERNAME_EXISTED(2100, "Username already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(2101, "Email already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(2102, "Role not found", HttpStatus.NOT_FOUND),
    DEPARTMENT_NOT_FOUND(2103, "Department not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(2104, "User not found", HttpStatus.NOT_FOUND),
    DEPARTMENT_EXISTED(2105, "Department existed", HttpStatus.BAD_REQUEST),
    SCHEDULE_CONFLICT(2106, "Schedule conflict", HttpStatus.CONFLICT),
    ROOM_NOT_FOUND(2107, "Room not found", HttpStatus.BAD_REQUEST),
    ROOM_ALREADY_BOOKED(2108, "Room is already booked in this time range", HttpStatus.CONFLICT),
    SCHEDULE_NOT_FOUND(2109, "Schedule not found", HttpStatus.NOT_FOUND),
    ROOM_EXISTED(2110, "Room existed", HttpStatus.BAD_REQUEST),
    MIN_MAX_CAPACITY_CONFLICT(2111, "min, max conflict!", HttpStatus.BAD_REQUEST),
    // ==== Auth Errors (2200+) ====
    UNAUTHENTICATED(2200, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2201, "You don't have permission", HttpStatus.FORBIDDEN),
    INVALID_OLD_PASSWORD(2202, "Invalid old password", HttpStatus.UNAUTHORIZED),

    // ==== System Errors (2900+) ====
    INVALID_KEY(2900, "Invalid key", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED(2999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private int code;
    private  String message;
    private HttpStatusCode statusCode;


}

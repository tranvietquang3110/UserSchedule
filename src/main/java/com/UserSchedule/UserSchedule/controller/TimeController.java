package com.UserSchedule.UserSchedule.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
// @RestController không nhận path, dùng @RequestMapping để đặt base path
@RequestMapping("/time")
public class TimeController {

    @GetMapping
    public String getCurrentTime() {
        // Lấy thời gian hiện tại theo múi giờ hệ thống
        ZonedDateTime now = ZonedDateTime.now();

        // Format thời gian đẹp
        String formattedTime = now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy, HH:mm:ss"));

        return formattedTime;
    }
}

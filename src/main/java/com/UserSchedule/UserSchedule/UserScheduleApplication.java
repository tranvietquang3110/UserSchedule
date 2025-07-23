package com.UserSchedule.UserSchedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableFeignClients
@SpringBootApplication
@EnableAsync
public class UserScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserScheduleApplication.class, args);
	}

}

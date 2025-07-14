package com.UserSchedule.UserSchedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class UserScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserScheduleApplication.class, args);
	}

}

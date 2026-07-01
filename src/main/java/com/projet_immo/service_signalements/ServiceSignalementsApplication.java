package com.projet_immo.service_signalements;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@EnableFeignClients
public class ServiceSignalementsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceSignalementsApplication.class, args);
	}

}

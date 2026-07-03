package com.microservices.serviceadmin.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

// service-signalements valide le rôle à partir du JWT — on le retransmet tel quel.
@Bean
public RequestInterceptor authorizationForwardingInterceptor() {
return requestTemplate -> {
ServletRequestAttributes attributes =
(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
if (attributes != null) {
String authHeader = attributes.getRequest().getHeader("Authorization");
if (authHeader != null) {
requestTemplate.header("Authorization", authHeader);
}
}
};
}
}

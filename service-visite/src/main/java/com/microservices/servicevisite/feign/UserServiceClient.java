package com.microservices.servicevisite.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Eureka normalise les noms de service en majuscules ; le vrai nom enregistré par
// service-user est "SERVICE-UTILISATEUR" (spring.application.name), pas "user-service".
@FeignClient(name = "service-utilisateur")
public interface UserServiceClient {

@GetMapping("/api/users/by-email/{email}")
Long getAuthUserIdByEmail(@PathVariable("email") String email);

@GetMapping("/api/users/{authUserId}")
UserServiceResponse getProfile(@PathVariable("authUserId") Long authUserId);

@GetMapping("/api/users/{authUserId}/email")
String getEmailById(@PathVariable("authUserId") Long authUserId);
}

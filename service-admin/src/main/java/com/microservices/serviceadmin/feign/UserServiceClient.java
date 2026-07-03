package com.microservices.serviceadmin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "service-utilisateur")
public interface UserServiceClient {

@GetMapping("/internal/users")
List<UserProfileResponse> getAllProfiles();

@PutMapping("/internal/users/{authUserId}/deactivate")
void deactivateProfile(@PathVariable("authUserId") Long authUserId);

@PutMapping("/internal/users/{authUserId}/reactivate")
void reactivateProfile(@PathVariable("authUserId") Long authUserId);
}

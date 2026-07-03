package com.microservices.serviceadmin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

@PostMapping("/api/auth/admin/register")
void registerAdmin(@RequestBody RegisterAdminRequest request);

@PutMapping("/api/auth/users/{id}/role")
void updateRole(@PathVariable("id") Long id, @RequestBody RoleUpdateRequest request);
}

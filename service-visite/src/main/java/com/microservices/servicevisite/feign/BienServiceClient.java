package com.microservices.servicevisite.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bien-service")
public interface BienServiceClient {

@GetMapping("/api/biens/{id}")
BienResponse getBienById(@PathVariable("id") Long id);
}


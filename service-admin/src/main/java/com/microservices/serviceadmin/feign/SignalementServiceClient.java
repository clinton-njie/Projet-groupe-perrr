package com.microservices.serviceadmin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "service-signalements")
public interface SignalementServiceClient {

@GetMapping("/signalements")
List<SignalementResponse> getAllSignalements();

@GetMapping("/signalements/{id}")
SignalementResponse getSignalement(@PathVariable("id") Long id);

@PutMapping("/signalements/{id}/statut")
SignalementResponse updateStatut(@PathVariable("id") Long id, @RequestBody StatutRequest request);
}

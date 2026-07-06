package com.projet_immo.service_signalements.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.projet_immo.service_signalements.dto.NotificationRequest;

// Notifie via l'endpoint générique de service-notification (nom Eureka en majuscules).
@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    void notifier(@RequestBody NotificationRequest request);
}

package com.microservices.paymentservice.feign;

import com.microservices.paymentservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/paiement")
    void notifierPaiement(@RequestBody NotificationRequest request);
}

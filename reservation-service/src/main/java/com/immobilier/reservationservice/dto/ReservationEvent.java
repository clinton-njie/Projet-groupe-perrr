package com.immobilier.reservationservice.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEvent {

    private String eventType;

    private Long reservationId;

    private Long userId;

    private Long propertyId;

    private Long agentId;

    private String message;
}
package com.microservices.servicevisite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SlotResponse {
private LocalDateTime dateHeureDebut;
private LocalDateTime dateHeureFin;
}
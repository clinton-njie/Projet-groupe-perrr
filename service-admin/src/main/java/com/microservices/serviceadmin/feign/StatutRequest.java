package com.microservices.serviceadmin.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatutRequest {
// "EN_ATTENTE", "RESOLU" ou "REJETE"
private String statut;
}

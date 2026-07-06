package com.microservices.servicevisite.feign;

import lombok.Data;

@Data
public class BienResponse {
private Long id;
private String nom;
private String agentEmail;
private String categorie;
}

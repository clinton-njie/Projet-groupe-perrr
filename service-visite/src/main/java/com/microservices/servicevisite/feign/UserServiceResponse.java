package com.microservices.servicevisite.feign;

import lombok.Data;

@Data
public class UserServiceResponse {
private Long authUserId;
private String firstname;
private String lastname;
}

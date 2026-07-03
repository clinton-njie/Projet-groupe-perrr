package com.microservices.serviceadmin.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAdminRequest {
private String email;
private String password;
private String firstname;
private String lastname;
private String role;
}

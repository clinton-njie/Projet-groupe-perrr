package com.immobilier.userservice.dto.request;

import com.immobilier.userservice.enums.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank
    private Role role;
}
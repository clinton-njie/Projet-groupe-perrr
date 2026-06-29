package com.immobilier.userservice.dto.request;

import com.immobilier.userservice.enums.UserStatus;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotBlank
    private UserStatus status;
}
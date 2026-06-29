package com.immobilier.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateNomRequest {

    @NotBlank
    private String nom;
}
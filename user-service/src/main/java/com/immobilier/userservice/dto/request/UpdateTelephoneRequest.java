package com.immobilier.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTelephoneRequest {

    @NotBlank
    private String telephone;
}
package com.immobilier.reservationservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Body attendu par bien-service (PUT /api/biens/{id}/statut) : DISPONIBLE, RESERVE ou LOUE
public class StatutBienUpdateRequest {
    private String statut;
}

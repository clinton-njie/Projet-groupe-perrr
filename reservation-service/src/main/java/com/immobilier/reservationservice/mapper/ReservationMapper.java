package com.immobilier.reservationservice.mapper;

import com.immobilier.reservationservice.dto.ReservationResponse;
import com.immobilier.reservationservice.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation saved) {

        ReservationResponse response = new ReservationResponse();

        response.setId(saved.getId());
        response.setUserId(saved.getUserId());
        response.setPropertyId(saved.getPropertyId());
        response.setStartDate(saved.getStartDate());
        response.setEndDate(saved.getEndDate());
        response.setAmount(saved.getAmount());
        response.setStatus(saved.getStatus() != null ? saved.getStatus().name() : null);
        response.setNomBien(saved.getNomBien());
        response.setNomUtilisateur(saved.getNomUtilisateur());
        return response;
    }

}

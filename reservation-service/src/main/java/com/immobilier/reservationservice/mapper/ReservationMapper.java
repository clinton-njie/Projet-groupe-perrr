package com.immobilier.reservationservice.mapper;

import com.immobilier.reservationservice.dto.ReservationResponse;
import com.immobilier.reservationservice.entity.Reservation;

public class ReservationMapper {

   public ReservationResponse toResponse(Reservation saved) {

    ReservationResponse response = new ReservationResponse();

    response.setId(saved.getId());
    response.setUserId(saved.getUserId());
    response.setPropertyId(saved.getPropertyId());
    response.setStartDate(saved.getStartDate());
    response.setEndDate(saved.getEndDate());
    response.setAmount(saved.getAmount());
    return response;
}
    
}
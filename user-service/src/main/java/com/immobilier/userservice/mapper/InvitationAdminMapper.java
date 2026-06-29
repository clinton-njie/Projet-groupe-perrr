package com.immobilier.userservice.mapper;

import com.immobilier.userservice.Acorbeille.InvitationAdminRequest;
import com.immobilier.userservice.Acorbeille.InvitationAdminResponse;
import com.immobilier.userservice.dto.request.InvitationAdmin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapper pour convertir entre les entités InvitationAdmin et les DTOs InvitationAdminRequest et InvitationAdminResponse. Utilise MapStruct pour générer le code de mapping.
@Mapper(componentModel = "spring")
public interface InvitationAdminMapper {

    // Entity → Response
    InvitationAdminResponse toResponse(InvitationAdmin invitation);

    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "token", ignore = true) // généré automatiquement
    @Mapping(target = "creePar", ignore = true)
    @Mapping(target = "expireLe", ignore = true)
    @Mapping(target = "utilisee", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    InvitationAdmin toEntity(InvitationAdminRequest request);
}
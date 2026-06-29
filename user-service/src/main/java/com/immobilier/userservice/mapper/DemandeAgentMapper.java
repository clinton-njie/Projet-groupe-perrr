package com.immobilier.userservice.mapper;

import com.immobilier.userservice.dto.request.DemandeAgentRequest;
import com.immobilier.userservice.dto.response.DemandeAgentResponse;
import com.immobilier.userservice.entity.DemandeAgent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapper pour convertir entre les entités DemandeAgent et les DTOs DemandeAgentRequest et DemandeAgentResponse. Utilise MapStruct pour générer le code de mapping.
@Mapper(componentModel = "spring")
public interface DemandeAgentMapper {

    // Entity → Response
    DemandeAgentResponse toResponse(DemandeAgent demandeAgent);

    // Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "utilisateur", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "dateTrait", ignore = true)
    @Mapping(target = "admin", ignore = true)
    DemandeAgent toEntity(DemandeAgentRequest request);
}
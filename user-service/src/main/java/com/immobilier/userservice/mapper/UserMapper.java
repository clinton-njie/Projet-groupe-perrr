package com.immobilier.userservice.mapper;

import com.immobilier.userservice.dto.request.RegisterUserRequest;
import com.immobilier.userservice.dto.response.UserResponse;
import com.immobilier.userservice.entity.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// Mapper pour convertir entre l'entité Utilisateur et les DTOs de requête/réponse. 
// Utilise MapStruct pour générer le code de mapping.
@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity → Response DTO (API output)
    UserResponse toResponse(Utilisateur utilisateur);

    // Request DTO → Entity (création utilisateur)
    @Mapping(target = "id", ignore = true) // ID généré par DB
    @Mapping(target = "role", ignore = true) // défini dans service
    @Mapping(target = "actif", ignore = true) // par défaut true
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    @Mapping(target = "cniRectoUrl", ignore = true)
    @Mapping(target = "cniVersoUrl", ignore = true)
    @Mapping(target = "adresseProfessionnelle", ignore = true)
    Utilisateur toEntity(RegisterUserRequest request);
}
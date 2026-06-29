package com.immobilier.userservice.repository;

import com.immobilier.userservice.entity.Utilisateur;
import com.immobilier.userservice.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

// Repository pour l'entité Utilisateur, fournissant des méthodes de recherche personnalisées. 
public interface UserRepository extends JpaRepository<Utilisateur, Long> {

    // Recherche utilisateur par email
    @NonNull
    List<Utilisateur> findAll();

    // Recherche utilisateur par email
    Optional<Utilisateur> findByEmail(String email);

    // Vérifie si email existe déjà
    boolean existsByEmail(String email);

    // Liste des utilisateurs par rôle
    List<Utilisateur> findByRole(Role role);

    // Liste des utilisateurs actifs
    List<Utilisateur> findByActifTrue();
}
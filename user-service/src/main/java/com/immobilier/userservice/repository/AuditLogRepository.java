package com.immobilier.userservice.repository;

import com.immobilier.userservice.entity.AuditLog;
import com.immobilier.userservice.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository pour l'entité AuditLog, fournissant des méthodes de recherche personnalisées.
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Historique des actions d’un utilisateur
    List<AuditLog> findByUtilisateur(Utilisateur utilisateur);

    // Historique par type d’action (ex: UPDATE_EMAIL)
    List<AuditLog> findByAction(String action);
}
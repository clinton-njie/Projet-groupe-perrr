package com.microservices.bien.repository;

import com.microservices.bien.entity.Bien;
import com.microservices.bien.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienRepository extends JpaRepository<Bien, Long> {

    List<Bien> findByAgentId(Long agentId);

    List<Bien> findByCategorie(Categorie categorie);

    List<Bien> findByLocalisationContainingIgnoreCase(String localisation);

    List<Bien> findByDisponible(Boolean disponible);
}

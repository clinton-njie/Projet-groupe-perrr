package com.microservices.bien.repository;

import com.microservices.bien.entity.Bien;
import com.microservices.bien.entity.Categorie;
import com.microservices.bien.entity.StatutBien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienRepository extends JpaRepository<Bien, Long> {

    List<Bien> findByAgentEmail(String agentEmail);

    List<Bien> findByCategorie(Categorie categorie);

    List<Bien> findByVilleContainingIgnoreCaseOrQuartierContainingIgnoreCase(String ville, String quartier);

    List<Bien> findByStatut(StatutBien statut);
}

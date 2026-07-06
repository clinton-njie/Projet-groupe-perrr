package com.microservices.bien.repository;

import com.microservices.bien.entity.Categorie;
import com.microservices.bien.entity.PrixVisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrixVisiteRepository extends JpaRepository<PrixVisite, Long> {

    Optional<PrixVisite> findByCategorie(Categorie categorie);
}

package com.plateforme.immo.avis.repository;

import com.plateforme.immo.avis.model.Avis;
import com.plateforme.immo.avis.model.TypeCible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {
    List<Avis> findAllByAgentId(Long agentId);
    List<Avis> findAllByBienId(Long bienId);
    Page<Avis> findByAgentId(Long agentId, Pageable pageable);
    Page<Avis> findByBienId(Long bienId, Pageable pageable);
    Page<Avis> findByTypeCible(TypeCible typeCible, Pageable pageable);
}
package com.projet_immo.service_signalements.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.enums.SignalementType;

@Repository
public interface ObjetSignalementRepository extends JpaRepository<ObjetSignalement, Long>{
    boolean exiexistsByLibelleAndType(String libelle, SignalementType type);
}

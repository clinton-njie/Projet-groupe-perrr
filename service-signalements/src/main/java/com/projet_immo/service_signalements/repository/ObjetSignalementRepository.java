package com.projet_immo.service_signalements.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.enums.SignalementType;

@Repository
public interface ObjetSignalementRepository extends JpaRepository<ObjetSignalement, Long> {

    List<ObjetSignalement> findByActifTrue();

    List<ObjetSignalement> findByActifTrueAndType(SignalementType type);
}

package com.projet_immo.service_signalements.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.projet_immo.service_signalements.dto.ObjetSignalementRequest;
import com.projet_immo.service_signalements.dto.ObjetSignalementResponse;
import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.repository.ObjetSignalementRepository;

@Service
public class ObjetSignalementService {

    @Autowired
    private ObjetSignalementRepository repository;

    // Création (admin) : actif = true par défaut.
    public ObjetSignalementResponse creer(ObjetSignalementRequest request) {
        valider(request);
        ObjetSignalement objet = new ObjetSignalement(request.getLibelle(), request.getType(), true);
        return ObjetSignalementResponse.from(repository.save(objet));
    }

    // Modification (admin) : libellé et/ou type.
    public ObjetSignalementResponse modifier(Long id, ObjetSignalementRequest request) {
        valider(request);
        ObjetSignalement objet = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objet de signalement introuvable"));
        objet.setLibelle(request.getLibelle());
        objet.setType(request.getType());
        return ObjetSignalementResponse.from(repository.save(objet));
    }

    // Jamais de suppression : on désactive (actif = false).
    public ObjetSignalementResponse desactiver(Long id) {
        ObjetSignalement objet = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objet de signalement introuvable"));
        objet.setActif(false);
        return ObjetSignalementResponse.from(repository.save(objet));
    }

    // Liste destinée aux utilisateurs : uniquement les objets actifs (filtrable par type).
    public List<ObjetSignalementResponse> listerActifs(SignalementType type) {
        List<ObjetSignalement> objets = (type == null)
                ? repository.findByActifTrue()
                : repository.findByActifTrueAndType(type);
        return objets.stream().map(ObjetSignalementResponse::from).collect(Collectors.toList());
    }

    private void valider(ObjetSignalementRequest request) {
        if (request.getLibelle() == null || request.getLibelle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le libellé est requis");
        }
        if (request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le type est requis");
        }
    }
}

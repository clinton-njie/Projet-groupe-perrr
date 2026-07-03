package com.plateforme.immo.avis.service;

import com.plateforme.immo.avis.dto.AvisPlatformeDTO;
import com.plateforme.immo.avis.dto.AvisResponseDTO;
import com.plateforme.immo.avis.dto.AvisTransactionDTO;
import com.plateforme.immo.avis.model.*;
import com.plateforme.immo.avis.repository.AvisRepository;
import com.plateforme.immo.avis.repository.OpportuniteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvisService {

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private OpportuniteRepository opportuniteRepository;

    public Avis soumettreAvisTransaction(Long clientId, AvisTransactionDTO dto) {
        OpportuniteNotation opp = opportuniteRepository.findById(dto.getOpportuniteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunité non trouvée"));

        if (opp.getStatut() == StatutOpportunite.UTILISEE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Avis déjà soumis pour cette opportunité");
        }
        if (opp.getDateExpiration() != null && opp.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "L'opportunité a expiré");
        }
        if (!opp.getClientId().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette opportunité ne vous appartient pas");
        }

        Avis avis = Avis.builder()
                .opportuniteId(opp.getId())
                .clientId(clientId)
                .agentId(opp.getAgentId())
                .bienId(opp.getBienId())
                .typeCible(TypeCible.AGENT_ET_BIEN)
                .noteCommunication(dto.getNoteCommunication())
                .noteProfessionnalisme(dto.getNoteProfessionnalisme())
                .noteDisponibilite(dto.getNoteDisponibilite())
                .noteConformiteAnnonce(dto.getNoteConformiteAnnonce())
                .noteEtatGeneral(dto.getNoteEtatGeneral())
                .noteRapportQualitePrix(dto.getNoteRapportQualitePrix())
                .commentaire(dto.getCommentaire())
                .dateAvis(LocalDateTime.now())
                .build();

        opp.setStatut(StatutOpportunite.UTILISEE);
        opportuniteRepository.save(opp);
        return avisRepository.save(avis);
    }

    public Avis soumettreAvisPlateforme(Long clientId, AvisPlatformeDTO dto) {
        Avis avis = Avis.builder()
                .clientId(clientId)
                .typeCible(TypeCible.PLATEFORME)
                .notePlateforme(dto.getNotePlateforme())
                .commentaire(dto.getCommentaire())
                .dateAvis(LocalDateTime.now())
                .build();
        return avisRepository.save(avis);
    }

    public AvisResponseDTO getAvisAgent(Long agentId, Pageable pageable) {
        Page<Avis> page = avisRepository.findByAgentId(agentId, pageable);
        List<Avis> totalAvis = avisRepository.findAllByAgentId(agentId);

        double mComm = totalAvis.stream().mapToInt(Avis::getNoteCommunication).average().orElse(0.0);
        double mProf = totalAvis.stream().mapToInt(Avis::getNoteProfessionnalisme).average().orElse(0.0);
        double mDisp = totalAvis.stream().mapToInt(Avis::getNoteDisponibilite).average().orElse(0.0);
        double globale = (mComm + mProf + mDisp) / 3.0;

        return new AvisResponseDTO(page.getContent(), globale, mComm, mProf, mDisp, 0.0, 0.0, 0.0);
    }

    public AvisResponseDTO getAvisBien(Long bienId, Pageable pageable) {
        Page<Avis> page = avisRepository.findByBienId(bienId, pageable);
        List<Avis> totalAvis = avisRepository.findAllByBienId(bienId);

        double mConf = totalAvis.stream().mapToInt(Avis::getNoteConformiteAnnonce).average().orElse(0.0);
        double mEtat = totalAvis.stream().mapToInt(Avis::getNoteEtatGeneral).average().orElse(0.0);
        double mPrix = totalAvis.stream().mapToInt(Avis::getNoteRapportQualitePrix).average().orElse(0.0);
        double globale = (mConf + mEtat + mPrix) / 3.0;

        return new AvisResponseDTO(page.getContent(), globale, 0.0, 0.0, 0.0, mConf, mEtat, mPrix);
    }

    public Page<Avis> getAvisPlateforme(Pageable pageable) {
        return avisRepository.findByTypeCible(TypeCible.PLATEFORME, pageable);
    }
}
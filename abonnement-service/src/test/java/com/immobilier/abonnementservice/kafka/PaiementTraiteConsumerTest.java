package com.immobilier.abonnementservice.kafka;

import com.immobilier.abonnementservice.dto.event.PaiementTraiteEvent;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementTraiteConsumerTest {

    @Mock private AbonnementRepository repo;
    @Mock private AbonnementEventProducer producer;
    @Mock private EvenementHelper evenements;

    @InjectMocks private PaiementTraiteConsumer consumer;

    private Abonnement abonnement(StatutAbonnement statut) {
        return Abonnement.builder().id(1L).userId(1L).email("a@t.com")
                .type(TypeAbonnement.MENSUEL).requestId("r1").statut(statut).build();
    }

    private PaiementTraiteEvent event(String statut) {
        return PaiementTraiteEvent.builder().requestId("r1").paiementId(9L).statut(statut).reference("TX").build();
    }

    @Test
    void success_activeAbonnementEtCalculeDates() {
        Abonnement a = abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        when(repo.findByRequestId("r1")).thenReturn(Optional.of(a));
        when(repo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(a)); // 1 seul -> activation

        consumer.consommer(event("SUCCESS"));

        assertEquals(StatutAbonnement.ACTIF, a.getStatut());
        assertNotNull(a.getDateDebut());
        assertNotNull(a.getDateFin());
        verify(repo).save(a);
        verify(producer).abonnementActivated(any());
    }

    @Test
    void success_avecHistorique_publieRenewed() {
        Abonnement a = abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        when(repo.findByRequestId("r1")).thenReturn(Optional.of(a));
        when(repo.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(a, abonnement(StatutAbonnement.EXPIRE)));

        consumer.consommer(event("SUCCESS"));

        verify(producer).abonnementRenewed(any());
    }

    @Test
    void echec_passeEnEchecPaiement() {
        Abonnement a = abonnement(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        when(repo.findByRequestId("r1")).thenReturn(Optional.of(a));

        consumer.consommer(event("ECHEC"));

        assertEquals(StatutAbonnement.ECHEC_PAIEMENT, a.getStatut());
        verify(producer).paymentFailed(any());
    }

    @Test
    void idempotent_dejaActif_ignore() {
        Abonnement a = abonnement(StatutAbonnement.ACTIF);
        when(repo.findByRequestId("r1")).thenReturn(Optional.of(a));

        consumer.consommer(event("SUCCESS"));

        verify(repo, never()).save(any());
        verify(producer, never()).abonnementActivated(any());
    }

    @Test
    void requestIdInconnu_ignore() {
        when(repo.findByRequestId("r1")).thenReturn(Optional.empty());

        consumer.consommer(event("SUCCESS"));

        verify(repo, never()).save(any());
    }
}

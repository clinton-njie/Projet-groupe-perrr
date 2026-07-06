package com.immobilier.abonnementservice.service;

import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.PaiementResponse;
import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import com.immobilier.abonnementservice.exception.AbonnementException;
import com.immobilier.abonnementservice.feign.PaiementFeignClient;
import com.immobilier.abonnementservice.kafka.AbonnementEventProducer;
import com.immobilier.abonnementservice.kafka.EvenementHelper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.impl.SouscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SouscriptionServiceImplTest {

    @Mock private JwtService jwtService;
    @Mock private PaiementFeignClient paiementFeignClient;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private AbonnementEventProducer producer;
    @Mock private EvenementHelper evenements;

    @InjectMocks private SouscriptionServiceImpl service;

    private UtilisateurConnecteResponse user(String role) {
        UtilisateurConnecteResponse u = new UtilisateurConnecteResponse();
        u.setId(1L); u.setEmail("agent@test.com"); u.setNom("N"); u.setPrenom("P"); u.setRole(role);
        return u;
    }

    private CreerAbonnementRequest req() {
        CreerAbonnementRequest r = new CreerAbonnementRequest();
        r.setType(TypeAbonnement.MENSUEL);
        return r;
    }

    private void stubSaveWithId() {
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(inv -> {
            Abonnement a = inv.getArgument(0);
            if (a.getId() == null) a.setId(1L);
            return a;
        });
    }

    @Test
    void souscrire_agent_creeEnAttentePaiement() {
        when(jwtService.getUser("tok")).thenReturn(user("AGENT"));
        stubSaveWithId();
        PaiementResponse ack = new PaiementResponse();
        ack.setPaiementId(99L); ack.setStatut("EN_ATTENTE");
        when(paiementFeignClient.creerPaiement(any())).thenReturn(ack);

        AbonnementResponse resp = service.souscrire("tok", req());

        assertEquals(StatutAbonnement.EN_ATTENTE_PAIEMENT, resp.getStatut());
        verify(producer).abonnementCreated(any());
        verify(evenements).notifier(any(), any(), any(), any(), any());
    }

    @Test
    void souscrire_paiementIndisponible_resteEnAttente() {
        when(jwtService.getUser("tok")).thenReturn(user("AGENT"));
        stubSaveWithId();
        when(paiementFeignClient.creerPaiement(any())).thenThrow(new RuntimeException("payment down"));

        AbonnementResponse resp = service.souscrire("tok", req());

        assertEquals(StatutAbonnement.EN_ATTENTE_PAIEMENT, resp.getStatut()); // best-effort
    }

    @Test
    void souscrire_nonAgent_leveException() {
        when(jwtService.getUser("tok")).thenReturn(user("CLIENT"));
        assertThrows(AbonnementException.class, () -> service.souscrire("tok", req()));
        verify(abonnementRepository, never()).save(any());
    }

    @Test
    void reessayer_pasProprietaire_leveException() {
        when(jwtService.getUser("tok")).thenReturn(user("AGENT"));
        Abonnement a = Abonnement.builder().id(5L).userId(2L)
                .statut(StatutAbonnement.ECHEC_PAIEMENT).type(TypeAbonnement.MENSUEL).build();
        when(abonnementRepository.findById(5L)).thenReturn(Optional.of(a));
        assertThrows(AbonnementException.class, () -> service.reessayerPaiement("tok", 5L));
    }

    @Test
    void reessayer_mauvaisStatut_leveException() {
        when(jwtService.getUser("tok")).thenReturn(user("AGENT"));
        Abonnement a = Abonnement.builder().id(5L).userId(1L)
                .statut(StatutAbonnement.ACTIF).type(TypeAbonnement.MENSUEL).build();
        when(abonnementRepository.findById(5L)).thenReturn(Optional.of(a));
        assertThrows(AbonnementException.class, () -> service.reessayerPaiement("tok", 5L));
    }

    @Test
    void reessayer_echec_regenereRequestIdEtRepasseEnAttente() {
        when(jwtService.getUser("tok")).thenReturn(user("AGENT"));
        Abonnement a = Abonnement.builder().id(5L).userId(1L).email("agent@test.com")
                .statut(StatutAbonnement.ECHEC_PAIEMENT).type(TypeAbonnement.MENSUEL).requestId("old").build();
        when(abonnementRepository.findById(5L)).thenReturn(Optional.of(a));
        lenient().when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(inv -> inv.getArgument(0));

        AbonnementResponse resp = service.reessayerPaiement("tok", 5L);

        assertEquals(StatutAbonnement.EN_ATTENTE_PAIEMENT, resp.getStatut());
    }
}

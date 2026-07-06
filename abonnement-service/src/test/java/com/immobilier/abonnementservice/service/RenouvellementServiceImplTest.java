package com.immobilier.abonnementservice.service;

import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
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
import com.immobilier.abonnementservice.service.impl.RenouvellementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenouvellementServiceImplTest {

    @Mock private JwtService jwtService;
    @Mock private PaiementFeignClient paiementFeignClient;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private AbonnementEventProducer producer;
    @Mock private EvenementHelper evenements;

    @InjectMocks private RenouvellementServiceImpl service;

    private UtilisateurConnecteResponse agent() {
        UtilisateurConnecteResponse u = new UtilisateurConnecteResponse();
        u.setId(1L); u.setEmail("agent@test.com"); u.setRole("AGENT");
        return u;
    }

    private RenouvelerAbonnementRequest req() {
        RenouvelerAbonnementRequest r = new RenouvelerAbonnementRequest();
        r.setType(TypeAbonnement.ANNUEL);
        return r;
    }

    @Test
    void renouveler_actifEncoreValide_interdit() {
        when(jwtService.getUser("tok")).thenReturn(agent());
        Abonnement actif = Abonnement.builder().id(1L).userId(1L)
                .statut(StatutAbonnement.ACTIF).dateFin(LocalDate.now().plusDays(10)).build();
        when(abonnementRepository.findTopByUserIdOrderByDateFinDesc(1L)).thenReturn(Optional.of(actif));

        assertThrows(AbonnementException.class, () -> service.renouveler("tok", req()));
        verify(abonnementRepository, never()).save(any());
    }

    @Test
    void renouveler_expire_autorise() {
        when(jwtService.getUser("tok")).thenReturn(agent());
        Abonnement expire = Abonnement.builder().id(1L).userId(1L)
                .statut(StatutAbonnement.EXPIRE).dateFin(LocalDate.now().minusDays(5)).build();
        when(abonnementRepository.findTopByUserIdOrderByDateFinDesc(1L)).thenReturn(Optional.of(expire));
        when(abonnementRepository.save(any(Abonnement.class))).thenAnswer(inv -> {
            Abonnement a = inv.getArgument(0);
            if (a.getId() == null) a.setId(2L);
            return a;
        });

        AbonnementResponse resp = service.renouveler("tok", req());

        assertEquals(StatutAbonnement.EN_ATTENTE_PAIEMENT, resp.getStatut());
        verify(producer).abonnementCreated(any());
    }

    @Test
    void renouveler_nonAgent_interdit() {
        UtilisateurConnecteResponse u = agent();
        u.setRole("CLIENT");
        when(jwtService.getUser("tok")).thenReturn(u);
        assertThrows(AbonnementException.class, () -> service.renouveler("tok", req()));
    }
}

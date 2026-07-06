package com.projet_immo.service_signalements.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.projet_immo.service_signalements.dto.SignalementRequest;
import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.entity.Signalement;
import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.feignclient.NotificationClient;
import com.projet_immo.service_signalements.feignclient.ReservationClient;
import com.projet_immo.service_signalements.feignclient.UtilisateurClient;
import com.projet_immo.service_signalements.feignclient.VisiteClient;
import com.projet_immo.service_signalements.repository.ObjetSignalementRepository;
import com.projet_immo.service_signalements.repository.SignalementRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalementServiceTest {

    @Mock private SignalementRepository signalementRepository;
    @Mock private ObjetSignalementRepository objetSignalementRepository;
    @Mock private VisiteClient visiteClient;
    @Mock private ReservationClient reservationClient;
    @Mock private UtilisateurClient utilisateurClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private SignalementService service;

    private static final List<String> CLIENT = List.of("ROLE_CLIENT");
    private static final Long USER = 10L;
    private static final Long AGENT = 20L;

    private ObjetSignalement objet(Long id, SignalementType type, boolean actif) {
        ObjetSignalement o = new ObjetSignalement("motif", type, actif);
        o.setId(id);
        return o;
    }

    private SignalementRequest req(SignalementType type, Long objetId, Long agentId) {
        SignalementRequest r = new SignalementRequest();
        r.setType(type);
        r.setObjetSignalementId(objetId);
        r.setAgentId(agentId);
        r.setDescription("desc");
        return r;
    }

    private ResponseStatusException call(SignalementRequest r) {
        return assertThrows(ResponseStatusException.class, () -> service.addSignalement(r, USER, CLIENT));
    }

    @Test
    void agentAvecInteraction_creeLeSignalement() {
        when(signalementRepository.countByUtilisateurIdAndDateSignalement(eq(USER), any())).thenReturn(0L);
        when(objetSignalementRepository.findById(1L)).thenReturn(Optional.of(objet(1L, SignalementType.AGENT, true)));
        when(reservationClient.existeInteraction(USER, AGENT)).thenReturn(true);
        when(signalementRepository.existsByUtilisateurIdAndAgentIdAndObjetSignalement_IdAndStatut(
                USER, AGENT, 1L, SignalementStatut.EN_ATTENTE)).thenReturn(false);

        service.addSignalement(req(SignalementType.AGENT, 1L, AGENT), USER, CLIENT);

        verify(signalementRepository).save(any(Signalement.class));
    }

    @Test
    void agentSansInteraction_403() {
        when(signalementRepository.countByUtilisateurIdAndDateSignalement(eq(USER), any())).thenReturn(0L);
        when(objetSignalementRepository.findById(1L)).thenReturn(Optional.of(objet(1L, SignalementType.AGENT, true)));
        when(visiteClient.existeInteraction(USER, AGENT)).thenReturn(false);
        when(reservationClient.existeInteraction(USER, AGENT)).thenReturn(false);

        assertEquals(HttpStatus.FORBIDDEN, call(req(SignalementType.AGENT, 1L, AGENT)).getStatusCode());
        verify(signalementRepository, never()).save(any());
    }

    @Test
    void limiteTroisParJour_429() {
        when(signalementRepository.countByUtilisateurIdAndDateSignalement(eq(USER), any())).thenReturn(3L);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, call(req(SignalementType.TECHNIQUE, 1L, null)).getStatusCode());
    }

    @Test
    void objetInactif_409() {
        when(signalementRepository.countByUtilisateurIdAndDateSignalement(eq(USER), any())).thenReturn(0L);
        when(objetSignalementRepository.findById(1L)).thenReturn(Optional.of(objet(1L, SignalementType.AGENT, false)));
        assertEquals(HttpStatus.CONFLICT, call(req(SignalementType.AGENT, 1L, AGENT)).getStatusCode());
    }

    @Test
    void objetTypeIncoherent_400() {
        when(signalementRepository.countByUtilisateurIdAndDateSignalement(eq(USER), any())).thenReturn(0L);
        when(objetSignalementRepository.findById(1L)).thenReturn(Optional.of(objet(1L, SignalementType.TECHNIQUE, true)));
        assertEquals(HttpStatus.BAD_REQUEST, call(req(SignalementType.AGENT, 1L, AGENT)).getStatusCode());
    }

    @Test
    void doublonEnAttente_409() {
        when(signalementRepository.countByUtilisateurIdAndDateSignalement(eq(USER), any())).thenReturn(0L);
        when(objetSignalementRepository.findById(1L)).thenReturn(Optional.of(objet(1L, SignalementType.AGENT, true)));
        when(reservationClient.existeInteraction(USER, AGENT)).thenReturn(true);
        when(signalementRepository.existsByUtilisateurIdAndAgentIdAndObjetSignalement_IdAndStatut(
                USER, AGENT, 1L, SignalementStatut.EN_ATTENTE)).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT, call(req(SignalementType.AGENT, 1L, AGENT)).getStatusCode());
    }

    @Test
    void mauvaisRole_403() {
        assertThrows(ResponseStatusException.class,
                () -> service.addSignalement(req(SignalementType.TECHNIQUE, 1L, null), USER, List.of("ROLE_ADMIN")));
    }

    @Test
    void sanctionAgent_desactiveLeCompte() {
        Signalement s = new Signalement();
        s.setId(5L);
        s.setType(SignalementType.AGENT);
        s.setAgentId(AGENT);
        s.setObjetSignalement(objet(1L, SignalementType.AGENT, true));
        s.setStatut(SignalementStatut.EN_ATTENTE);
        when(signalementRepository.findById(5L)).thenReturn(Optional.of(s));

        service.updateStatut(5L, SignalementStatut.TERMINE_AVEC_SANCTION);

        verify(utilisateurClient).deactivate(AGENT);
    }

    @Test
    void sansSanction_neDesactivePas() {
        Signalement s = new Signalement();
        s.setId(6L);
        s.setType(SignalementType.AGENT);
        s.setAgentId(AGENT);
        s.setObjetSignalement(objet(1L, SignalementType.AGENT, true));
        s.setStatut(SignalementStatut.EN_ATTENTE);
        when(signalementRepository.findById(6L)).thenReturn(Optional.of(s));

        service.updateStatut(6L, SignalementStatut.TERMINE_SANS_SANCTION);

        verify(utilisateurClient, never()).deactivate(anyLong());
    }

    @Test
    void dejaTraite_409() {
        Signalement s = new Signalement();
        s.setId(7L);
        s.setStatut(SignalementStatut.TERMINE_SANS_SANCTION);
        when(signalementRepository.findById(7L)).thenReturn(Optional.of(s));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateStatut(7L, SignalementStatut.TERMINE_AVEC_SANCTION));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void transitionVersEnAttente_400() {
        Signalement s = new Signalement();
        s.setId(8L);
        s.setStatut(SignalementStatut.EN_ATTENTE);
        when(signalementRepository.findById(8L)).thenReturn(Optional.of(s));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateStatut(8L, SignalementStatut.EN_ATTENTE));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}

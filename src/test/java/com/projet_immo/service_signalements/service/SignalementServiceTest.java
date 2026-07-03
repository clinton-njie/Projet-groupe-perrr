package com.projet_immo.service_signalements.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projet_immo.service_signalements.dto.GetSignalementResponse;
import com.projet_immo.service_signalements.dto.SignalementRequest;
import com.projet_immo.service_signalements.dto.SignalementResponse;
import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.entity.Signalement;
import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.exception.AgentNotSelectedException;
import com.projet_immo.service_signalements.exception.DailyLimitExceededException;
import com.projet_immo.service_signalements.exception.NoInteractionException;
import com.projet_immo.service_signalements.exception.ObjetSignalementNotFoundException;
import com.projet_immo.service_signalements.exception.SignalementAlreadyProcessedException;
import com.projet_immo.service_signalements.exception.SignalementNotFoundException;
import com.projet_immo.service_signalements.feignclient.ReservationClient;
import com.projet_immo.service_signalements.feignclient.UtilisateurClient;
import com.projet_immo.service_signalements.feignclient.VisiteClient;
import com.projet_immo.service_signalements.repository.ObjetSignalementRepository;
import com.projet_immo.service_signalements.repository.SignalementRepository;

@ExtendWith(MockitoExtension.class)
class SignalementServiceTest {

    @Mock
    private SignalementRepository signalementRepository;

    @Mock
    private ObjetSignalementRepository objetSignalementRepository;

    @Mock
    private VisiteClient visiteClient;

    @Mock
    private ReservationClient reservationClient;

    @Mock
    private UtilisateurClient utilisateurClient;

    @InjectMocks
    private SignalementService signalementService;

    private SignalementRequest request;
    private ObjetSignalement objet;

    @BeforeEach
    void setUp() {
        request = new SignalementRequest();
        request.setAgentId(2L);
        request.setObjetSignalement(1L);
        request.setDescription("Test signalement");
        request.setType(SignalementType.AGENT);

        objet = new ObjetSignalement();
        objet.setId(1L);
        objet.setLibelle("Objet test");
        objet.setType(SignalementType.AGENT);
    }

    // TEST 1 : succès
    @Test
    void shouldAddSignalementAgentSuccessfully() {

        Long utilisateurId = 1L;
        Long agentId = 2L;

        when(signalementRepository.countByUtilisateurIdAndDateSignalement(
                eq(utilisateurId), any(LocalDate.class)))
                .thenReturn(0L);

        when(visiteClient.existeInteraction(utilisateurId, agentId))
                .thenReturn(true);

        when(objetSignalementRepository.findById(1L))
                .thenReturn(Optional.of(objet));

        when(utilisateurClient.getEmailById(utilisateurId))
                .thenReturn("client@test.com");

        when(utilisateurClient.getEmailById(agentId))
                .thenReturn("agent@test.com");

        SignalementResponse response =
                signalementService.addSignalementAgent(request, utilisateurId);

        assertNotNull(response);
        assertEquals("client@test.com", response.getUtilisateurEmail());
        assertEquals("agent@test.com", response.getAgentEmail());
        assertEquals("Signalement envoye", response.getMessage());

        verify(signalementRepository).save(any());
    }

    // TEST 2 : limite atteinte
    @Test
    void shouldThrowDailyLimitExceededExceptionWhenLimitReached() {

        Long utilisateurId = 1L;

        when(signalementRepository.countByUtilisateurIdAndDateSignalement(
                eq(utilisateurId), any(LocalDate.class)))
                .thenReturn(3L);

        assertThrows(DailyLimitExceededException.class,
                () -> signalementService.addSignalementAgent(request, utilisateurId));

        verify(signalementRepository, never()).save(any());
    }

    // TEST 3 : agent non sélectionné
    @Test
    void shouldThrowAgentNotSelectedExceptionWhenAgentIsNull() {

        Long utilisateurId = 1L;
        request.setAgentId(null);

        when(signalementRepository.countByUtilisateurIdAndDateSignalement(
                eq(utilisateurId), any(LocalDate.class)))
                .thenReturn(0L);

        assertThrows(AgentNotSelectedException.class,
                () -> signalementService.addSignalementAgent(request, utilisateurId));

        verify(signalementRepository, never()).save(any());
    }

    // TEST 4 : pas d'interaction
    @Test
    void shouldThrowNoInteractionExceptionWhenNoInteractionExists() {

        Long utilisateurId = 1L;
        Long agentId = 2L;

        when(signalementRepository.countByUtilisateurIdAndDateSignalement(
                eq(utilisateurId), any(LocalDate.class)))
                .thenReturn(0L);

        when(visiteClient.existeInteraction(utilisateurId, agentId))
                .thenReturn(false);

        when(reservationClient.existeInteraction(utilisateurId, agentId))
                .thenReturn(false);

        assertThrows(NoInteractionException.class,
                () -> signalementService.addSignalementAgent(request, utilisateurId));

        verify(signalementRepository, never()).save(any());
    }

    @Test
void shouldThrowObjetSignalementNotFoundExceptionWhenObjetDoesNotExist() {

    Long utilisateurId = 1L;
    Long agentId = 2L;

    when(signalementRepository.countByUtilisateurIdAndDateSignalement(
            eq(utilisateurId), any(LocalDate.class)))
            .thenReturn(0L);

    when(visiteClient.existeInteraction(utilisateurId, agentId))
            .thenReturn(true);

    when(objetSignalementRepository.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(
            ObjetSignalementNotFoundException.class,
            () -> signalementService.addSignalementAgent(request, utilisateurId)
    );

    verify(signalementRepository, never()).save(any());
}

@Test
void shouldAddTechnicalSignalementSuccessfully() {

    Long utilisateurId = 1L;

    request.setAgentId(null);
    request.setType(SignalementType.TECHNIQUE);

    when(signalementRepository.countByUtilisateurIdAndDateSignalement(
            eq(utilisateurId), any(LocalDate.class)))
            .thenReturn(0L);

    when(objetSignalementRepository.findById(1L))
            .thenReturn(Optional.of(objet));

    when(utilisateurClient.getEmailById(utilisateurId))
            .thenReturn("client@test.com");

    SignalementResponse response =
            signalementService.addSignalementTechnique(request, utilisateurId);

    assertNotNull(response);
    assertEquals("client@test.com", response.getUtilisateurEmail());
    assertNull(response.getAgentEmail());

    verify(signalementRepository).save(any());
}

@Test
void shouldThrowDailyLimitExceededExceptionForTechnicalSignalement() {

    Long utilisateurId = 1L;

    request.setAgentId(null);
    request.setType(SignalementType.TECHNIQUE);

    when(signalementRepository.countByUtilisateurIdAndDateSignalement(
            eq(utilisateurId), any(LocalDate.class)))
            .thenReturn(3L);

    assertThrows(
            DailyLimitExceededException.class,
            () -> signalementService.addSignalementTechnique(request, utilisateurId)
    );

    verify(signalementRepository, never()).save(any());
}

@Test
void shouldThrowObjetSignalementNotFoundExceptionForTechnicalSignalement() {

    Long utilisateurId = 1L;

    request.setAgentId(null);
    request.setType(SignalementType.TECHNIQUE);

    when(signalementRepository.countByUtilisateurIdAndDateSignalement(
            eq(utilisateurId), any(LocalDate.class)))
            .thenReturn(0L);

    when(objetSignalementRepository.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(
            ObjetSignalementNotFoundException.class,
            () -> signalementService.addSignalementTechnique(request, utilisateurId)
    );

    verify(signalementRepository, never()).save(any());
}

@Test
void shouldReturnSignalementWhenItExists() {

    ObjetSignalement objet = new ObjetSignalement();
    objet.setLibelle("Objet test");

    Signalement signalement = new Signalement();
    signalement.setId(1L);
    signalement.setUtilisateurId(1L);
    signalement.setAgentId(2L);
    signalement.setObjetSignalement(objet);
    signalement.setDescription("Description");
    signalement.setType(SignalementType.AGENT);
    signalement.setStatut(SignalementStatut.EN_ATTENTE);

    when(signalementRepository.findById(1L))
            .thenReturn(Optional.of(signalement));

    when(utilisateurClient.getEmailById(1L))
            .thenReturn("client@test.com");

    when(utilisateurClient.getEmailById(2L))
            .thenReturn("agent@test.com");

    GetSignalementResponse response =
            signalementService.getSignalement(1L);

    assertNotNull(response);
    assertEquals("client@test.com", response.getUtilisateurEmail());
    assertEquals("agent@test.com", response.getAgentEmail());
    assertEquals("Objet test", response.getTitre());
}

@Test
void shouldThrowSignalementNotFoundException() {

    when(signalementRepository.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(
            SignalementNotFoundException.class,
            () -> signalementService.getSignalement(1L)
    );
}

@Test
void shouldReturnEmptyList() {

    when(signalementRepository.findAll())
            .thenReturn(List.of());

    List<GetSignalementResponse> result =
            signalementService.getAllSignalements();

    assertTrue(result.isEmpty());
}

@Test
void shouldReturnAllSignalements() {

    Signalement signalement = new Signalement();
    signalement.setId(1L);
    signalement.setUtilisateurId(1L);
    signalement.setAgentId(2L);
    signalement.setObjetSignalement(objet);
    signalement.setType(SignalementType.AGENT);
    signalement.setDescription("Description");
    signalement.setStatut(SignalementStatut.EN_ATTENTE);

    when(signalementRepository.findAll())
            .thenReturn(List.of(signalement));

    when(utilisateurClient.getEmailById(1L))
            .thenReturn("client@test.com");

    when(utilisateurClient.getEmailById(2L))
            .thenReturn("agent@test.com");

    List<GetSignalementResponse> result =
            signalementService.getAllSignalements();

    assertEquals(1, result.size());
}

@Test
void shouldUpdateSignalementStatus() {

    Signalement signalement = new Signalement();
    signalement.setId(1L);
    signalement.setUtilisateurId(1L);
    signalement.setObjetSignalement(objet);
    signalement.setType(SignalementType.AGENT);
    signalement.setStatut(SignalementStatut.EN_ATTENTE);

    when(signalementRepository.findById(1L))
            .thenReturn(Optional.of(signalement));

    when(utilisateurClient.getEmailById(1L))
            .thenReturn("client@test.com");

    GetSignalementResponse response =
            signalementService.updateStatut(
                    1L,
                    SignalementStatut.TRAITE);

    assertEquals(SignalementStatut.TRAITE, response.getStatut());

    verify(signalementRepository).save(signalement);
}

@Test
void shouldThrowSignalementNotFoundExceptionWhenUpdatingStatus() {

    when(signalementRepository.findById(1L))
            .thenReturn(Optional.empty());

    assertThrows(
            SignalementNotFoundException.class,
            () -> signalementService.updateStatut(
                    1L,
                    SignalementStatut.TRAITE)
    );
}

@Test
void shouldThrowSignalementAlreadyProcessedException() {

    Signalement signalement = new Signalement();
    signalement.setStatut(SignalementStatut.TRAITE);

    when(signalementRepository.findById(1L))
            .thenReturn(Optional.of(signalement));

    assertThrows(
            SignalementAlreadyProcessedException.class,
            () -> signalementService.updateStatut(
                    1L,
                    SignalementStatut.REJETE)
    );

    verify(signalementRepository, never()).save(any());
}
}
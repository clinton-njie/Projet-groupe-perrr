package com.plateforme.immo.avis.controller;

import com.plateforme.immo.avis.dto.AvisPlatformeDTO;
import com.plateforme.immo.avis.dto.AvisTransactionDTO;
import com.plateforme.immo.avis.feign.UserServiceClient;
import com.plateforme.immo.avis.model.Avis;
import com.plateforme.immo.avis.model.OpportuniteNotation;
import com.plateforme.immo.avis.service.AvisService;
import com.plateforme.immo.avis.service.OpportuniteService;
import com.plateforme.immo.avis.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvisControllerTest {

    @Mock private AvisService avisService;
    @Mock private OpportuniteService opportuniteService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks private AvisController controller;

    private static final String HEADER = "Bearer token-abc";

    // --- cleanToken ---

    @Test
    void headerManquant_leve401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getOpportunites(1L, null));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void headerSansBearer_leve401() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getOpportunites(1L, "token-sans-prefixe"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    // --- resolveUserId (via getOpportunites) ---

    @Test
    void getOpportunites_idResoluEtEgal_retourne200() {
        when(jwtUtil.extractEmail(anyString())).thenReturn("client@test.com");
        when(userServiceClient.getAuthUserIdByEmail("client@test.com")).thenReturn(42L);
        List<OpportuniteNotation> opps = List.of(new OpportuniteNotation());
        when(opportuniteService.getEnAttente(42L)).thenReturn(opps);

        ResponseEntity<List<OpportuniteNotation>> res = controller.getOpportunites(42L, HEADER);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(opps, res.getBody());
    }

    @Test
    void getOpportunites_idResoluDifferent_retourne403() {
        when(jwtUtil.extractEmail(anyString())).thenReturn("client@test.com");
        when(userServiceClient.getAuthUserIdByEmail("client@test.com")).thenReturn(42L);

        ResponseEntity<List<OpportuniteNotation>> res = controller.getOpportunites(99L, HEADER);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        verifyNoInteractions(opportuniteService);
    }

    @Test
    void resolveUserId_userIntrouvable_leve401() {
        when(jwtUtil.extractEmail(anyString())).thenReturn("inconnu@test.com");
        when(userServiceClient.getAuthUserIdByEmail("inconnu@test.com")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getOpportunites(1L, HEADER));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    // --- soumettreAvisTransaction (contrôle de rôle) ---

    @Test
    void soumettreAvisTransaction_roleNonClient_retourne403SansResoudreNiPersister() {
        when(jwtUtil.hasRole(anyString(), eq("ROLE_CLIENT"))).thenReturn(false);

        ResponseEntity<Avis> res = controller.soumettreAvisTransaction(HEADER, new AvisTransactionDTO());

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        verifyNoInteractions(userServiceClient);
        verifyNoInteractions(avisService);
    }

    @Test
    void soumettreAvisTransaction_client_retourne201() {
        AvisTransactionDTO dto = new AvisTransactionDTO();
        when(jwtUtil.hasRole(anyString(), eq("ROLE_CLIENT"))).thenReturn(true);
        when(jwtUtil.extractEmail(anyString())).thenReturn("client@test.com");
        when(userServiceClient.getAuthUserIdByEmail("client@test.com")).thenReturn(42L);
        Avis avis = new Avis();
        when(avisService.soumettreAvisTransaction(42L, dto)).thenReturn(avis);

        ResponseEntity<Avis> res = controller.soumettreAvisTransaction(HEADER, dto);

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertEquals(avis, res.getBody());
        verify(avisService).soumettreAvisTransaction(42L, dto);
    }

    // --- soumettreAvisPlateforme ---

    @Test
    void soumettreAvisPlateforme_retourne201AvecIdResolu() {
        AvisPlatformeDTO dto = new AvisPlatformeDTO();
        when(jwtUtil.extractEmail(anyString())).thenReturn("client@test.com");
        when(userServiceClient.getAuthUserIdByEmail("client@test.com")).thenReturn(7L);
        Avis avis = new Avis();
        when(avisService.soumettreAvisPlateforme(7L, dto)).thenReturn(avis);

        ResponseEntity<Avis> res = controller.soumettreAvisPlateforme(HEADER, dto);

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(avisService).soumettreAvisPlateforme(7L, dto);
    }
}

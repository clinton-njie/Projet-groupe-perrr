package com.microservices.bien.service;

import com.microservices.bien.dto.BienRequest;
import com.microservices.bien.dto.BienResponse;
import com.microservices.bien.dto.MessageResponse;
import com.microservices.bien.dto.PrixVisiteUpdateRequest;
import com.microservices.bien.dto.StatutUpdateRequest;
import com.microservices.bien.entity.Bien;
import com.microservices.bien.entity.Categorie;
import com.microservices.bien.entity.PrixVisite;
import com.microservices.bien.entity.StatutBien;
import com.microservices.bien.repository.BienRepository;
import com.microservices.bien.repository.PrixVisiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BienServiceTest {

    @Mock
    private BienRepository bienRepository;

    @Mock
    private PrixVisiteRepository prixVisiteRepository;

    @InjectMocks
    private BienService bienService;

    private Authentication agentAuth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_AGENT")));
    }

    private Authentication adminAuth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private BienRequest villaRequest() {
        return BienRequest.builder()
                .categorie(Categorie.VILLA)
                .nom("Villa Bella")
                .ville("Douala")
                .quartier("Bonapriso")
                .photoPrincipale("photo.jpg")
                .prixMensuel(250000f)
                .description("Belle villa")
                .build();
    }

    private Bien savedBien(String agentEmail) {
        return Bien.builder()
                .id(1L)
                .categorie(Categorie.VILLA)
                .nom("Villa Bella")
                .ville("Douala")
                .quartier("Bonapriso")
                .photoPrincipale("photo.jpg")
                .prixMensuel(250000f)
                .agentEmail(agentEmail)
                .statut(StatutBien.DISPONIBLE)
                .build();
    }

    @Test
    void creerBien_sauvegardeAvecAgentDepuisAuthentification() {
        Authentication auth = agentAuth("agent@mail.com");
        when(bienRepository.save(any(Bien.class))).thenAnswer(inv -> {
            Bien b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        BienResponse response = bienService.creerBien(villaRequest(), auth);

        ArgumentCaptor<Bien> captor = ArgumentCaptor.forClass(Bien.class);
        verify(bienRepository).save(captor.capture());
        assertEquals("agent@mail.com", captor.getValue().getAgentEmail());
        assertEquals(StatutBien.DISPONIBLE, captor.getValue().getStatut());
        assertEquals("Villa Bella", response.getNom());
    }

    @Test
    void creerBien_citeSansNombrePieces_estRefusee() {
        BienRequest request = villaRequest();
        request.setCategorie(Categorie.CITE);
        request.setNombrePiecesDisponibles(null);

        assertThrows(IllegalStateException.class, () -> bienService.creerBien(request, agentAuth("agent@mail.com")));
        verify(bienRepository, never()).save(any());
    }

    @Test
    void modifierBien_parUnAutreAgentNonAdmin_estRefuse() {
        Bien bien = savedBien("proprietaire@mail.com");
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));

        assertThrows(IllegalStateException.class,
                () -> bienService.modifierBien(1L, villaRequest(), agentAuth("intrus@mail.com")));
        verify(bienRepository, never()).save(any());
    }

    @Test
    void modifierBien_parAdmin_estAutorisee() {
        Bien bien = savedBien("proprietaire@mail.com");
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));
        when(bienRepository.save(any(Bien.class))).thenAnswer(inv -> inv.getArgument(0));

        BienRequest request = villaRequest();
        request.setNom("Villa Renovee");

        BienResponse response = bienService.modifierBien(1L, request, adminAuth("admin@mail.com"));

        assertEquals("Villa Renovee", response.getNom());
    }

    @Test
    void supprimerBien_introuvable_leveException() {
        when(bienRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> bienService.supprimerBien(99L, adminAuth("admin@mail.com")));
    }

    @Test
    void supprimerBien_parProprietaire_supprimeEtRetourneMessage() {
        Bien bien = savedBien("proprietaire@mail.com");
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));

        MessageResponse response = bienService.supprimerBien(1L, agentAuth("proprietaire@mail.com"));

        verify(bienRepository, times(1)).delete(bien);
        assertEquals("Bien supprimé avec succès", response.getMessage());
    }

    @Test
    void changerStatut_appelePourInteractionInterServices() {
        Bien bien = savedBien("agent@mail.com");
        when(bienRepository.findById(1L)).thenReturn(Optional.of(bien));
        when(bienRepository.save(any(Bien.class))).thenAnswer(inv -> inv.getArgument(0));

        BienResponse response = bienService.changerStatut(1L, StatutUpdateRequest.builder().statut(StatutBien.LOUE).build());

        assertEquals(StatutBien.LOUE, response.getStatut());
    }

    @Test
    void getPrixVisiteByCategorie_categorieNonConfiguree_leveException() {
        when(prixVisiteRepository.findByCategorie(Categorie.TERRAIN)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> bienService.getPrixVisiteByCategorie("TERRAIN"));
    }

    @Test
    void updatePrixVisite_parNonAdmin_estRefuse() {
        assertThrows(IllegalStateException.class, () -> bienService.updatePrixVisite("VILLA",
                PrixVisiteUpdateRequest.builder().prixVisite(5000f).build(), agentAuth("agent@mail.com")));
        verify(prixVisiteRepository, never()).findByCategorie(any());
    }

    @Test
    void updatePrixVisite_parAdmin_metAJourEtRetourneNouveauPrix() {
        PrixVisite prixVisite = PrixVisite.builder().id(1L).categorie(Categorie.VILLA).prixVisite(5000f).build();
        when(prixVisiteRepository.findByCategorie(Categorie.VILLA)).thenReturn(Optional.of(prixVisite));
        lenient().when(prixVisiteRepository.save(any(PrixVisite.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = bienService.updatePrixVisite("VILLA",
                PrixVisiteUpdateRequest.builder().prixVisite(7500f).build(), adminAuth("admin@mail.com"));

        assertEquals(7500f, response.getPrixVisite());
        verify(prixVisiteRepository).save(prixVisite);
    }

    @Test
    void mesBiens_filtreParEmailDeLAgentConnecte() {
        when(bienRepository.findByAgentEmail("agent@mail.com"))
                .thenReturn(List.of(savedBien("agent@mail.com")));

        List<BienResponse> result = bienService.mesBiens(agentAuth("agent@mail.com"));

        assertEquals(1, result.size());
        assertEquals("agent@mail.com", result.get(0).getAgentEmail());
        verify(bienRepository).findByAgentEmail("agent@mail.com");
    }

    @Test
    void listerBiensDisponibles_delegueAuRepository() {
        when(bienRepository.findByStatut(StatutBien.DISPONIBLE)).thenReturn(List.of(savedBien("agent@mail.com")));

        List<BienResponse> result = bienService.listerBiensDisponibles();

        assertEquals(1, result.size());
        assertEquals(StatutBien.DISPONIBLE, result.get(0).getStatut());
    }
}

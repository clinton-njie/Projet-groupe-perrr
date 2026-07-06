package com.microservices.bien.config;

import com.microservices.bien.entity.Categorie;
import com.microservices.bien.entity.PrixVisite;
import com.microservices.bien.repository.PrixVisiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrixVisiteSeeder implements CommandLineRunner {

    private final PrixVisiteRepository prixVisiteRepository;

    @Override
    public void run(String... args) {
        seed(Categorie.APPARTEMENT, 5000f);
        seed(Categorie.VILLA, 10000f);
        seed(Categorie.CITE, 3000f);
        seed(Categorie.TERRAIN, 7000f);
    }

    private void seed(Categorie categorie, Float prixParDefaut) {
        prixVisiteRepository.findByCategorie(categorie).orElseGet(() ->
                prixVisiteRepository.save(PrixVisite.builder()
                        .categorie(categorie)
                        .prixVisite(prixParDefaut)
                        .build()));
    }
}

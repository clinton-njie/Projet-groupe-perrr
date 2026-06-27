package com.immobilier.reservationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication

@EnableFeignClients // Enable Feign clients pour la communication avec d'autres services
@EnableDiscoveryClient // Enable service discovery pour permettre à ce service d'être découvert par d'autres services
@EnableScheduling // Enable scheduling pour permettre l'exécution périodique de tâches

public class ReservationServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ReservationServiceApplication.class,
                args
        ); // Démarre l'application Spring Boot
    }
}
# # KwattaImmo

Plateforme de mise en relation entre agents immobiliers et clients. Projet de fin d'études de licence en informatique (Université de Dschang, 2026), réalisé par une équipe de 11 étudiants. Note obtenue : 17/20, mention Très Bien.

## Pourquoi ce projet

À Dschang, trouver un logement se fait surtout par le bouche-à-oreille et les annonces collées sur les murs. Nous avons voulu une plateforme simple : l'agent publie ses biens, le client cherche, réserve une visite et suit ses demandes au même endroit.

## Ce qu'on a fait techniquement

Nous sommes partis sur une architecture microservices en Spring Boot, chaque service a sa base PostgreSQL et s'enregistre auprès d'un serveur Eureka. Toutes les requêtes passent par une gateway.

L'authentification est gérée par un service dédié avec des tokens JWT. Nous avons choisi JWT plutôt qu'une session classique parce que l'application est exposée sur Internet, et ça réduit la surface d'attaque.

La communication entre services se fait en asynchrone via RabbitMQ pour les notifications et les événements métier (une réservation déclenche une notification sans que les services s'appellent directement).

Le frontend est en React.

## Organisation du dépôt

Nous travaillons par branches : main contient l'infrastructure (eureka-server, service-api-gateway), chaque service vit sur sa branche.

- service-auth : authentification JWT
- service-utilisateur : gestion des utilisateurs
- service-client : espace client
- service-agent-immobilier : espace agent
- service-gestion-bien : publication et gestion des biens
- reservation : réservation de visites
- service-gestion-visites : suivi des visites
- service-paiement : paiements
- service-notification : notifications
- service-messagerie : messagerie interne
- service-signalements : signalements
- service-gestion-comptes : gestion des comptes
- feature/migration-rabbitmq : migration vers RabbitMQ en cours

## Lancer le projet

Il vous faut Java 17+, Maven, Node.js, PostgreSQL et RabbitMQ.

```bash
git clone https://github.com/clinton-njie/Projet-groupe-perrr.git
cd Projet-groupe-perrr
```

Infrastructure depuis main :

```bash
cd eureka-server && ./mvnw spring-boot\:run
cd ../service-api-gateway && ./mvnw spring-boot\:run
```

Puis chaque service depuis sa branche :

```bash
git switch service-auth
./mvnw spring-boot\:run
```

Le dashboard Eureka est sur http://localhost:8761.

## Déploiement

La plateforme tourne sur un VPS. Chaque service est indépendant, on peut mettre à jour l'un sans couper les autres.

## Équipe

Projet réalisé par 11 étudiants de l'Université de Dschang. Conception et coordination : William Nwambo Nkumenyi.

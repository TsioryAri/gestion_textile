# Gestion Textile — Projet Spring Boot

Application de gestion de commandes pour une entreprise textile. Elle suit une commande de sa création à sa livraison, à travers un workflow de production en 5 étapes.

## Architecture

Deux points d'entrée (API REST et interface web Thymeleaf) partagent exactement la même couche métier :

    Navigateur  -> Contrôleur MVC  --+
                                     +--> Service --> Repository --> PostgreSQL
    Client REST -> Contrôleur REST --+

Packages (`com.example.textile`) :

- `controller/rest` : API REST (JSON)
- `controller/web` : contrôleurs MVC (vues Thymeleaf)
- `service` / `service/impl` : logique métier, partagée par les deux types de contrôleurs
- `repository` : accès aux données (Spring Data JPA) et `repository/specification` (filtres dynamiques)
- `entity` : entités JPA
- `dto/request` / `dto/response` : objets d'échange (les entités ne sont jamais exposées)
- `mapper` : conversion Entity <-> DTO
- `exception` : exceptions métier et gestion centralisée des erreurs
- `config` : configuration Spring Security et données de démonstration

## Choix techniques

- Spring Boot 4.1 avec Java 21
- PostgreSQL 16 lancé via Docker Compose
- Spring Data JPA avec Specifications pour les filtres combinables et la pagination
- Spring Security : rôles ADMIN, MANAGER, OPERATOR, USER ; connexion par formulaire pour le web, HTTP Basic pour l'API ; droits par méthode avec `@PreAuthorize`
- Thymeleaf avec `thymeleaf-extras-springsecurity6` pour masquer les actions selon le rôle
- Bean Validation sur les DTO, erreurs REST centralisées dans un `@RestControllerAdvice`
- Transactions (`@Transactional`) sur toutes les opérations critiques du workflow
- Lombok pour réduire le code répétitif

## Démarrage

    docker compose up -d
    ./mvnw spring-boot:run

L'application est disponible sur http://localhost:8081

## Comptes de démonstration (données de test uniquement)

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| operator | operator123 | OPERATOR |
| user | user123 | USER |

Droits : ADMIN et MANAGER créent des commandes ; ADMIN, MANAGER et OPERATOR font avancer la production ; USER consulte uniquement.

## Workflow de production

COUPE -> COUTURE -> FINITION -> CONTROLE_QUALITE -> LIVRAISON

- Une étape ne démarre que si la précédente est terminée.
- Les quantités traitées ne peuvent pas dépasser la quantité commandée ni celle de l'étape précédente (ex. coupe 500, couture 480, finition 450).
- Un contrôle qualité NON_CONFORME bloque la livraison.
- Le statut de la commande suit le workflow : EN_ATTENTE, EN_PRODUCTION, CONTROLE_QUALITE, LIVREE.
- Le retard est calculé par rapport à la date de livraison prévue.

## Principaux endpoints REST

- `POST /api/orders` : créer une commande
- `GET /api/orders` : lister avec pagination, tri et filtres (`?page=0&size=10&sort=dateCommande,desc&status=EN_ATTENTE&priorite=URGENTE&dateDebut=2026-09-01`)
- `GET /api/orders/{id}` : détail d'une commande
- `GET /api/orders/delayed` : commandes en retard
- `GET /api/orders/statistics` : statistiques de production
- `POST /api/orders/{id}/quality-control` : valider le contrôle qualité
- `GET /api/orders/{id}/production` : étapes d'une commande
- `POST /api/orders/{id}/production/steps/{step}/start` : démarrer une étape
- `POST /api/orders/{id}/production/steps/{step}/complete` : terminer une étape

## Interface web

- `/commandes` : liste avec filtres et pagination
- `/commandes/{id}` : détail et actions de production
- `/dashboard` : tableau de bord de production

## Tests

    ./mvnw test

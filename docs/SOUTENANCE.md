# Notes de soutenance — Gestion Textile

## Comptes de démo
| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| manager | manager123 | Créer commandes, gérer production |
| operator | operator123 | Production uniquement |
| user | user123 | Consultation seule |

## Commandes de démo préparées
- Commande CONFORME (workflow complet réussi) : id = 167
- Commande NON_CONFORME (blocage livraison) : id = 168

## Plan de démo (10-12 min)
1. Architecture (1 min) — montrer le schéma REST + Thymeleaf → Service partagé
2. Login + rôles (1 min) — se connecter en USER, montrer qu'aucun bouton d'action n'apparaît
3. Création de commande (1 min) — se reconnecter en MANAGER, créer une commande
4. Workflow complet (3 min) — faire avancer la commande CONFORME jusqu'à LIVREE
5. Blocage qualité (2 min) — montrer la commande NON_CONFORME, tenter de livrer, montrer l'erreur
6. Filtres et pagination (1 min) — sur /commandes, filtrer par statut
7. Dashboard (1 min) — statistiques, taux de retard
8. Code REST (1 min) — appel curl, montrer le JSON et un 403
9. Questions

## Points à mentionner si demandé
- Architecture en couches, DTO jamais d'entité exposée
- Specifications JPA pour les filtres combinables
- @Transactional sur les opérations critiques
- 24 tests JUnit couvrant transitions, quantités, contrôle qualité, sécurité, pagination

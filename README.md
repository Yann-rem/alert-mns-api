# Alerte — API

Messagerie interne et diffusion d'alertes pour un établissement d'enseignement.

**En production : [alerte.yannickremy.fr](https://alerte.yannickremy.fr)**
Client Angular : [alert-mns-front](https://github.com/Yann-rem/alert-mns-front)

Projet conçu et développé seul, de février à août 2026, dans le cadre du titre
professionnel Concepteur Développeur d'Applications.

---

## Ce que fait l'application

Une école communique avec ses étudiants via des outils grand public dont elle ne
maîtrise ni l'hébergement, ni le coût, ni la capacité à répondre à une demande
d'effacement. Alerte répond à ce besoin par une application auto-hébergée :

- des **conversations** de groupe et des échanges directs, en temps réel ;
- des **alertes** diffusées à toute l'organisation ou à un groupe, avec un niveau de
  gravité, poussées instantanément aux seuls destinataires concernés ;
- une **administration** des membres : invitation, rôles, suspension, anonymisation.

Trois rôles : administrateur, gestionnaire, membre.

---

## En bref

| | |
|---|---|
| Langage | Java 21 |
| Framework | Spring Boot 3.4 |
| Base | PostgreSQL 16, schéma versionné par Flyway |
| Temps réel | WebSocket / STOMP |
| Tests | 904 côté serveur |
| Déploiement | continu, sur VPS, à chaque fusion sur `main` |

---

## Architecture

L'application suit une **architecture hexagonale** organisée en trois couches, et tient
en une règle : *le domaine ne connaît rien de ce qui l'entoure* — ni la base, ni le
framework, ni HTTP. Les dépendances vont toujours de l'extérieur vers l'intérieur.

Cette règle est vérifiable plutôt qu'affirmée :

```bash
grep -rlE "^import (org\.springframework|jakarta\.)" \
  src/main/java/com/alertmns/*/domain src/main/java/com/alertmns/*/application | wc -l
# 0, sur 211 fichiers
```

Le domaine se teste donc sans rien démarrer : les 727 tests de domaine et d'application
tournent en quelques secondes, sans base de données ni serveur.

### Quatre contextes bornés

| Contexte | Responsabilité |
|---|---|
| `identity` | comptes, authentification, activation, profil, anonymisation RGPD |
| `organisation` | organisations, membres, rôles, groupes, invitations |
| `messaging` | conversations et messages |
| `alerting` | diffusion d'alertes et audiences |

Chacun possède son vocabulaire : le mot *utilisateur* désigne un compte qui
s'authentifie dans `identity`, et un membre qui appartient à une école dans
`organisation`. Les contextes communiquent par **23 événements de domaine** et par des
ports de traduction à leurs frontières ; les dépendances entre eux ne forment aucun
cycle.

---

## Choix notables

Ces décisions sont assumées et discutables — elles sont ici pour être discutées.

**Aucune clé étrangère en base.** Une contrainte référentielle imposerait d'écrire deux
agrégats dans la même transaction, et relierait physiquement les tables de deux
contextes. L'intégrité est donc vérifiée dans le service applicatif, *avant* l'écriture,
ce qui permet de répondre 404 ou 409 avec un message métier plutôt que de laisser
remonter une violation de contrainte. Le prix : une écriture directe en base, hors de
l'application, pourrait produire un état incohérent. Les contraintes d'unicité qui
portent de vraies règles métier, elles, sont conservées.

**Statut de compte et statut d'adhésion sont orthogonaux.** Un compte peut être actif
alors que son adhésion à l'organisation est suspendue. Les deux vivent dans des
contextes différents, et les confondre a produit le défaut le plus instructif du projet.

**Session côté serveur plutôt que jeton.** Le client et l'API sont servis sur le même
domaine, un jeton portable n'apportait rien. Le critère décisif est la révocation :
suspendre un membre doit le déconnecter immédiatement, ce qu'un jeton ne permet pas sans
réintroduire l'état qu'il prétendait supprimer.

**Un invariant qui porte sur une collection ne vit pas dans l'agrégat.** Une organisation
ne peut pas perdre son dernier administrateur ; un `Member` ne peut pas savoir combien
d'autres existent, la règle se place donc au-dessus, dans le service applicatif.

---

## Démarrer en local

Prérequis : JDK 21 et Docker.

```bash
docker compose up -d
```

```bash
./mvnw spring-boot:run
```

L'API écoute sur `http://localhost:8080`. La documentation OpenAPI, générée depuis les
annotations, est servie sur `/swagger-ui.html`.

Le `docker-compose.yml` de la racine est la **pile de développement** : une base
PostgreSQL seule, avec des identifiants triviaux. La pile de production est décrite dans
[`deploy/compose.yaml`](deploy/compose.yaml), et ses valeurs réelles vivent dans un
fichier d'environnement présent sur le serveur uniquement
(gabarit : [`deploy/.env.example`](deploy/.env.example)).

---

## Tests

```bash
./mvnw verify
```

904 tests, répartis selon un principe simple : *un test se place au niveau le plus bas où
le défaut peut apparaître*. Une règle métier se teste sans base ; une requête paginée se
teste contre un vrai PostgreSQL, et non contre une base en mémoire dont le dialecte
diffère.

- 727 sur le domaine et la couche application, sans rien démarrer ;
- 177 d'intégration, contre une base réelle.

S'y ajoutent 143 tests côté client, et un jeu d'essai de 19 scénarios joué à la main
contre la production — qui a révélé cinq anomalies qu'aucun test automatisé n'avait vues,
toutes aux frontières : entre deux contextes, entre le code et son ordonnancement, entre
une couche et l'interface qui l'appelle, entre le dépôt et l'artefact livré.

---

## Déploiement

Trois conteneurs sur un VPS hébergé en France, dont **un seul est exposé** : Nginx assure
TLS, en-têtes de sécurité, fichiers statiques du client et relais de l'API sur le même
domaine — d'où l'absence totale de configuration CORS. La base n'a aucun port publié :
ce n'est pas le pare-feu qui la protège, c'est qu'elle n'existe pas depuis l'extérieur.

La chaîne ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) se déclenche à chaque
fusion sur `main` et se déroule en quatre temps, dont l'ordre n'est pas décoratif :

1. **construction et tests** — un échec arrête tout ;
2. **publication de l'image**, étiquetée `latest` et par empreinte, pour permettre un
   retour arrière sans reconstruction ;
3. **déploiement** par SSH, avec une clé restreinte à une seule commande côté serveur :
   une clé volée ne permettrait que de redéclencher un déploiement ;
4. **vérification** — le déploiement n'est déclaré réussi que si l'application répond
   réellement, et non si les conteneurs ont démarré. Un seul appel attendant un 401
   contrôle à la fois le certificat, le relais et l'application.

Sauvegarde quotidienne de la base, conservée quatorze jours
([`deploy/backup.sh`](deploy/backup.sh)), et restauration effectivement rejouée
([`deploy/restore.sh`](deploy/restore.sh)) — une sauvegarde jamais restaurée n'est pas
une sauvegarde, c'est une hypothèse.

---

## Limites assumées

- **Aucun envoi de courriel** : le lien d'activation est journalisé. C'est la première
  évolution prévue, et elle débloque la réinitialisation de mot de passe, absente elle
  aussi.
- **Les archives de sauvegarde vivent sur le serveur de la base** : cela protège d'une
  fausse manœuvre, pas de la perte de la machine.
- **Un seul serveur**, aucun test de charge.
- **Répondre à un message** est implémenté du domaine jusqu'au service client, validé des
  deux côtés, mais aucun bouton ne le déclenche encore.

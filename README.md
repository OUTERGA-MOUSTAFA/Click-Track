# ClickTrack

🇫🇷 Français | 🇬🇧 [English](README.en.md)

## 1. Présentation

**ClickTrack** est une API REST développée avec **Spring MVC** qui permet aux *publishers* (influenceurs, YouTubeurs, blogueurs) de créer des **liens de tracking** vers les offres qu'ils promeuvent et de **suivre le nombre de clics** par lien et par offre.

Projet réalisé dans le cadre de la formation **YouCode** (Sprint 3 – Brief 1).

### Règles de gestion

- Un publisher crée un lien de tracking vers une URL de destination, rattaché à une **offre**.
- Une offre peut avoir **plusieurs liens** : ses clics sont la **somme** des clics de ses liens.
- À chaque ouverture d'un lien (`GET /t/{id}`), le clic est compté, puis les informations du lien sont affichées.
- **Aucun clic ne doit être perdu** lors de clics simultanés.

## 2. Technologies

| Technologie | Usage |
|---|---|
| Java 17 | Langage |
| Maven (packaging WAR) | Build et dépendances |
| Spring Core 6.1.14 | Conteneur IoC, injection de dépendances |
| Spring MVC 6.1.14 | API REST (DispatcherServlet, controllers) |
| Jackson 2.17.2 | Conversion JSON ↔ objets Java |
| Jakarta Servlet API 6.0 | Fournie par Tomcat (`provided`) |
| Tomcat 10.1+ | Serveur d'application |
| JUnit 5 (Jupiter) 5.10.3 | Tests unitaires |
| Postman | Tests manuels de l'API |
| Stockage en mémoire (`ArrayList`) | Pas de base de données |

## 3. Architecture

Architecture **en couches**. Chaque couche dépend de l'**interface** de la couche du dessous (injection par constructeur).

```
Client (Postman)
      │  HTTP / JSON
      ▼
Controller  ──►  Service (interface)  ──►  Repository (interface)
 (HTTP)          (logique métier)          (stockage ArrayList)
      │                 │
     DTO              Model
```

| Couche | Rôle |
|---|---|
| **controller** | Reçoit la requête HTTP, appelle le service, renvoie le DTO et le code HTTP. Aucune logique métier. |
| **service** | Logique métier : vérifications, regroupement des clics par offre, incrémentation. |
| **repository** | Stockage en mémoire dans des `ArrayList`, protégé contre les accès simultanés. |
| **model** | Entités `Publisher`, `Lien`, enum `Categories`. |
| **dto** | Objets échangés avec le client (`CreerLienRequest`, `LienResponse`, `OffreStatsResponse`, `TrackingResponse`). |
| **exception** | Gestion centralisée des erreurs (`GlobalExceptionHandler`). |
| **init** | `DataInitializer` : données de test au démarrage. |

### Structure du projet

```
clicktrack/
├── pom.xml
├── README.md / README.en.md
├── .gitignore
├── Brief/                              énoncé du brief
└── src/
    ├── main/
    │   ├── java/ma/youcode/clickTrack/
    │   │   ├── controller/             PublisherController, LienController,
    │   │   │                           OffreController, TrackingController
    │   │   ├── dto/
    │   │   ├── exception/              GlobalExceptionHandler
    │   │   ├── init/                   DataInitializer
    │   │   ├── model/                  Publisher, Lien, Categories
    │   │   ├── repository/             interfaces
    │   │   │   └── implimentation/     ArrayList
    │   │   └── service/                interfaces
    │   │       └── implimentation/
    │   └── webapp/WEB-INF/             web.xml, applicationContext.xml,
    │                                   dispatcher-servlet.xml
    └── test/java/ma/youcode/clickTrack/
        ├── model/
        └── service/implimentation/
```

## 4. Endpoints

| Méthode | URL | Description | Réponses |
|---|---|---|---|
| POST | `/api/publishers/{id}/liens` | Crée un lien de tracking | 201 / 400 / 404 |
| GET | `/api/publishers` | Liste les publishers | 200 |
| GET | `/api/publishers/{id}/liens` | Liste les liens avec leurs clics | 200 / 404 |
| GET | `/api/publishers/{id}/offres` | Liste les offres avec leurs clics | 200 / 404 |
| GET | `/t/{id}` | Compte un clic et affiche les infos du lien | 200 / 404 |

### Exemples

**POST `/api/publishers/1/liens`** (Body → raw → JSON)

```json
{ "offre": "Casque audio X200", "urlDestination": "https://www.exemple.com/casque-audio" }
```

Réponse `201 Created` :

```json
{
  "id": 1,
  "offre": "Casque audio X200",
  "urlDestination": "https://www.exemple.com/casque-audio",
  "clicks": 0,
  "publisherId": 1
}
```

**GET `/t/1`** → `200 OK` (le clic est compté)

```json
{
  "urlRedirection": "https://www.exemple.com/casque-audio",
  "publisher": "Sara Alami",
  "offre": "Casque audio X200",
  "clicks": 1
}
```

**GET `/api/publishers/1/offres`** → `200 OK`

```json
[
  { "offre": "Casque audio X200", "nombreLiens": 1, "clicks": 10 },
  { "offre": "Montre connectée Z5", "nombreLiens": 1, "clicks": 2 }
]
```

### Gestion des erreurs

Les services lancent des **exceptions Java standard**. Le `GlobalExceptionHandler` (`@RestControllerAdvice`) les traduit en réponses HTTP :

| Exception | Code HTTP | Exemple |
|---|---|---|
| `NoSuchElementException` (publisher ou lien introuvable) | **404** | `GET /t/999` |
| `IllegalArgumentException` (offre ou URL vide) | **400** | `POST` avec `"offre": ""` |
| Corps absent ou JSON invalide | **400** | `POST` sans body |

Format de l'erreur :

```json
{ "status": 404, "message": "Lien introuvable avec l'id 999" }
```

## 5. Gestion de la concurrence

Plusieurs visiteurs peuvent cliquer en même temps : aucun clic ni aucune donnée ne doit être perdu.

| Risque | Protection |
|---|---|
| Deux visiteurs lisent `10` puis écrivent `11` : un clic est perdu | `Lien.clicks` est un **`AtomicInteger`** ; `incrementerClicks()` est atomique |
| Deux visiteurs reçoivent le même numéro de clic | Le service utilise la valeur **retournée** par `incrementAndGet()`, pas une relecture |
| Ajouts simultanés dans l'`ArrayList` (éléments perdus, exceptions) | Méthodes des repositories **`synchronized`** |
| Lire la liste pendant qu'un autre thread la modifie (`ConcurrentModificationException`) | Les repositories retournent une **copie** de la liste |
| Deux liens avec le même id | Génération des ids avec un **`AtomicInteger`** (les ids sont des `Integer`) |

**Validation** : `ConcurrenceTest` lance 100 threads qui démarrent en même temps (`CountDownLatch`) et vérifie que le compteur vaut exactement 100, puis des tests plus intensifs (plusieurs millions d'incrémentations, 10 000 créations simultanées, lectures pendant écritures). J'ai aussi vérifié que ces tests **échouent** quand on retire la protection.

> Les bugs de concurrence sont aléatoires : 100 threads qui font un seul clic passent souvent même sans protection. Il faut des tests très denses pour les révéler.

## 6. Configuration Spring (XML)

Le projet est livré avec la **configuration XML**.

```
Tomcat charge le WAR
  └─ web.xml
       ├─ ContextLoaderListener → applicationContext.xml   (contexte racine)
       │     repositories, services, DataInitializer
       └─ DispatcherServlet      → dispatcher-servlet.xml  (contexte web)
             <mvc:annotation-driven/>, controllers, GlobalExceptionHandler
```

- Les beans sont déclarés avec `<bean>` et `<constructor-arg ref="..."/>` ; **pas de `<context:component-scan>`**.
- Le contexte web voit les beans du contexte racine (les controllers reçoivent les services par `ref`).
- `@Controller` n'est utilisé que comme **marqueur** : depuis Spring 6, il est obligatoire pour qu'une classe soit détectée comme handler. Le bean reste déclaré en XML.
- Les annotations de mapping (`@RequestMapping`, `@GetMapping`, `@PathVariable`...) sont utilisées.

### Les trois configurations possibles (seule la première est implémentée)

| Config | Déclaration des beans | Démarrage de Spring MVC |
|---|---|---|
| **config-xml** *(implémentée)* | `<bean>` + `<constructor-arg>` dans `applicationContext.xml` | `web.xml` + `dispatcher-servlet.xml` |
| config-java | Méthodes `@Bean` dans des classes `@Configuration` | `AbstractAnnotationConfigDispatcherServletInitializer` |
| config-annotation | `@Service`, `@Repository`, `@RestController` + `@ComponentScan` | `AbstractAnnotationConfigDispatcherServletInitializer` |

## 7. Installation et lancement

**Prérequis** : JDK 17+, Maven 3.8+, Tomcat 10.1+ (Jakarta EE).

```bash
git clone <url-du-repo>
cd clicktrack
mvn clean package          # génère target/clicktrack.war
```

1. Copier `target/clicktrack.war` dans le dossier `webapps/` de Tomcat (ou le déployer depuis VS Code avec *Community Server Connectors*).
2. Démarrer Tomcat.
3. Tester : `http://localhost:8080/clicktrack/api/publishers` (adapter le port ; 8081 dans mon environnement).

> Tomcat 10.1 utilise le namespace **`jakarta.*`** (et non `javax.*`).
> Les données sont en mémoire : elles sont **réinitialisées à chaque redéploiement**.

## 8. Tests

### Tests unitaires (JUnit 5)

Les tests sont dans `src/test/java` (la dépendance JUnit a le scope `test`).

| Classe de test | Ce qui est vérifié |
|---|---|
| `LienTest` | Compteur de clics du modèle |
| `LienServiceListerLiensTest` | Liste des liens, isolation entre publishers, 404 |
| `LienServiceOffresTest` | Regroupement par offre, somme des clics (2 liens = somme) |
| `LienServiceTrackingTest` | Clic compté, infos du lien, 404 |
| `ConcurrenceTest` | 100 threads → 100 clics, créations simultanées, lectures pendant écritures |

```bash
mvn clean test                    # tous les tests
mvn -Dtest=LienTest test          # une seule classe
```

### Scénarios Postman

Pour les requêtes avec body : **Body → raw → JSON**.

| # |               Requête            |        Résultat attendu         |
|---|----------------------------------|---------------------------------|
| 1 | `GET /api/publishers`            |   200 + 3 publishers            |
| 2 | `POST /api/publishers/1/liens` (body valide) | 201 + lien à 0 clic |
| 3 | `POST /api/publishers/999/liens` | 404                             |
| 4 | `POST` avec `"offre": ""`        | 400                             |
| 5 | `POST` sans body                 | 400                             |
| 6 | `GET /t/1` (deux fois)           | 200, `clicks` = 1 puis 2        |
| 7 | `GET /t/999` | 404 |
| 8 | `GET /api/publishers/1/liens`    | 200, clics à jour               |
| 9 | `GET /api/publishers/1/offres`   | 200, somme des clics par offre  |
| 10 | `GET /api/publishers/999/offres` | 404                            |

## 9. Bonus

- ✅ **`DataInitializer`** : crée les 3 publishers de test au démarrage (méthode `init()` déclarée avec `init-method="init"`).
- ⬜ **Redirection** : `GET /t/{id}` renvoie aujourd'hui les infos du lien. Prochaine étape : compter le clic puis rediriger (`302`) vers `urlDestination`.
- ⬜ **Spring ORM / Hibernate** : non réalisé.

## 10. Difficultés rencontrées

| Symptôme | Cause | Solution |
|---|---|---|
| `404 : No endpoint GET /...` (message Spring) | Depuis Spring 6, `@RequestMapping` seul ne suffit plus | Ajouter `@Controller` sur le controller |
| `404` Tomcat « ressource non disponible » sur **toutes** les URL | `applicationContext.xml` mal formé : l'application ne démarre pas | Lire `logs/localhost.<date>.log` (chercher `SEVERE`), corriger le XML |
| `500 : Required path variable 'x' is not present` | Le nom dans `{x}` et dans `@PathVariable("x")` diffère | Utiliser exactement le même nom |
| `500` au lieu de `404` | `GlobalExceptionHandler` non déclaré dans `dispatcher-servlet.xml` | Déclarer le bean dans le contexte web |
| `201` attendu mais `500` renvoyé | `@ResponseStatus` sans valeur | `@ResponseStatus(HttpStatus.CREATED)` |
| `405 Method Not Allowed` | `GET` envoyé sur un endpoint `POST` | Utiliser la bonne méthode HTTP |
| `400 Requête invalide` | Body absent ou non JSON | Body → raw → **JSON** dans Postman |
| `The import org.junit cannot be resolved` / `No sources to compile` | Test placé dans `src/main/java` | Le placer dans `src/test/java` |
| Liste des liens toujours vide | `Lien.publisherId` typé `Publisher` alors qu'on comparait avec un `Integer` | Typer `publisherId` en `Integer` |

## 11. Ce que j'ai appris

Ce projet a été ma première vraie utilisation de **Spring Framework**, et j'y ai appris beaucoup.

### Spring Core
- **Inversion de contrôle** : je ne fais plus `new` moi-même ; Spring crée les objets (beans) et me les donne.
- **Injection de dépendances par constructeur**, avec des dépendances vers des **interfaces** : le code est plus propre, et on peut le tester sans Spring.
- Un bean est un **singleton** par défaut : c'est pour cela que tous les controllers partagent le même repository.
- **Cycle de vie** d'un bean : `init-method` pour `DataInitializer`.
- La configuration **XML** (`<bean>`, `<constructor-arg ref>`) et la différence avec les configurations Java et annotations.

### Spring MVC
- Le parcours d'une requête : Tomcat → `web.xml` → `DispatcherServlet` → mapping → controller → réponse JSON.
- **Deux contextes** : racine (`applicationContext.xml`) pour les services et web (`dispatcher-servlet.xml`) pour les controllers.
- `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestBody`, `@ResponseBody`, `@ResponseStatus`.
- Les changements de Spring 6 / 6.1 : `@Controller` obligatoire, nom explicite dans `@PathVariable("id")`.
- **`@RestControllerAdvice`** pour gérer les erreurs au même endroit et traduire une exception en code HTTP.
- **Jackson** et les **DTO** : ne pas exposer les entités directement ; les `record` Java 17 pour des DTO courts.

### Java et concurrence
- `AtomicInteger`, `synchronized`, retourner des **copies** des listes, `ExecutorService`, `CountDownLatch`.
- Comprendre **pourquoi** un compteur normal perd des clics, et le voir en pratique.
- `Optional`, les streams (`groupingBy`, `map`, `toList`), les `record`.

### Tests
- **JUnit 5** : `@Test`, `@BeforeEach`, `assertEquals`, `assertThrows`, le schéma *Arrange / Act / Assert*.
- Les tests vont dans `src/test/java` et doivent être **indépendants** les uns des autres.
- Tester la concurrence est difficile : il faut des tests très intensifs, et vérifier qu'ils échouent bien sans la protection.

### Outils et méthode
- **Maven** (packaging WAR, scopes des dépendances), **Tomcat 10.1** (Jakarta), **Postman**.
- **Lire les logs** de Tomcat (`SEVERE`) : c'est là qu'on trouve la vraie cause d'un 404 ou d'un 500.
- Distinguer un **404 de Spring** d'un **404 de Tomcat** (l'application n'a pas démarré).
- **Git / Jira** : un commit par tâche, avec la clé du ticket (`CLIC-12`).
- Les codes HTTP et leur sens : 200, 201, 400, 404, 405, 500.

## 12. Améliorations possibles

- Redirection `302` vers l'URL de destination dans `GET /t/{id}`.
- Remplacer les `ArrayList` par une base de données (Spring ORM / Hibernate).
- Valider l'URL de destination (format `http://` ou `https://`).
- Implémenter les configurations Java et annotations.
- Ajouter l'authentification des publishers.

## Auteur

### MOUSTAFA OUTERGA: formation YouCode, Sprint 3.
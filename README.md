# ClickTrack

🇫🇷 Français

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



# ClickTrack

🇬🇧 English

## 1. Overview

**ClickTrack** is a REST API built with **Spring MVC** that lets *publishers* (influencers, YouTubers, bloggers) create **tracking links** to the offers they promote and **follow the number of clicks** per link and per offer.

Built as part of the **YouCode** training program (Sprint 3 – Brief 1).

### Business rules

- A publisher creates a tracking link to a destination URL, attached to an **offer**.
- An offer can have **several links**: its clicks are the **sum** of its links' clicks.
- Each time a link is opened (`GET /t/{id}`), the click is counted, then the link's information is displayed.
- **No click may be lost** when several visitors click at the same time.

## 2. Technologies

| Technology | Purpose |
|---|---|
| Java 17 | Language |
| Maven (WAR packaging) | Build and dependencies |
| Spring Core 6.1.14 | IoC container, dependency injection |
| Spring MVC 6.1.14 | REST API (DispatcherServlet, controllers) |
| Jackson 2.17.2 | JSON ↔ Java object conversion |
| Jakarta Servlet API 6.0 | Provided by Tomcat (`provided`) |
| Tomcat 10.1+ | Application server |
| JUnit 5 (Jupiter) 5.10.3 | Unit tests |
| Postman | Manual API testing |
| In-memory storage (`ArrayList`) | No database |

## 3. Architecture

**Layered** architecture. Each layer depends on the **interface** of the layer below it (constructor injection).

```
Client (Postman)
      │  HTTP / JSON
      ▼
Controller  ──►  Service (interface)  ──►  Repository (interface)
 (HTTP)          (business logic)          (ArrayList storage)
      │                 │
     DTO              Model
```

| Layer | Role |
|---|---|
| **controller** | Receives the HTTP request, calls the service, returns the DTO and the HTTP status. No business logic. |
| **service** | Business logic: checks, grouping clicks by offer, incrementing. |
| **repository** | In-memory storage in `ArrayList`s, protected against simultaneous access. |
| **model** | `Publisher` and `Lien` entities, `Categories` enum. |
| **dto** | Objects exchanged with the client (`CreerLienRequest`, `LienResponse`, `OffreStatsResponse`, `TrackingResponse`). |
| **exception** | Centralised error handling (`GlobalExceptionHandler`). |
| **init** | `DataInitializer`: test data at startup. |

### Project structure

```
clicktrack/
├── pom.xml
├── README.md / README.en.md
├── .gitignore
├── Brief/                              project brief
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

| Method | URL | Description | Responses |
|---|---|---|---|
| POST | `/api/publishers/{id}/liens` | Creates a tracking link | 201 / 400 / 404 |
| GET | `/api/publishers` | Lists the publishers | 200 |
| GET | `/api/publishers/{id}/liens` | Lists the links with their clicks | 200 / 404 |
| GET | `/api/publishers/{id}/offres` | Lists the offers with their clicks | 200 / 404 |
| GET | `/t/{id}` | Counts a click and shows the link's info | 200 / 404 |

### Examples

**POST `/api/publishers/1/liens`** (Body → raw → JSON)

```json
{ "offre": "Casque audio X200", "urlDestination": "https://www.exemple.com/casque-audio" }
```

Response `201 Created`:

```json
{
  "id": 1,
  "offre": "Casque audio X200",
  "urlDestination": "https://www.exemple.com/casque-audio",
  "clicks": 0,
  "publisherId": 1
}
```

**GET `/t/1`** → `200 OK` (the click is counted)

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

### Error handling

Services throw **standard Java exceptions**. The `GlobalExceptionHandler` (`@RestControllerAdvice`) turns them into HTTP responses:

| Exception | HTTP status | Example |
|---|---|---|
| `NoSuchElementException` (publisher or link not found) | **404** | `GET /t/999` |
| `IllegalArgumentException` (empty offer or URL) | **400** | `POST` with `"offre": ""` |
| Missing body or invalid JSON | **400** | `POST` without a body |

Error format:

```json
{ "status": 404, "message": "Lien introuvable avec l'id 999" }
```

## 5. Concurrency handling

Several visitors can click at the same time: no click and no data may be lost.

| Risk | Protection |
|---|---|
| Two visitors read `10` and both write `11`: a click is lost | `Lien.clicks` is an **`AtomicInteger`**; `incrementerClicks()` is atomic |
| Two visitors get the same click number | The service uses the value **returned** by `incrementAndGet()`, not a second read |
| Simultaneous adds to the `ArrayList` (lost elements, exceptions) | Repository methods are **`synchronized`** |
| Reading the list while another thread modifies it (`ConcurrentModificationException`) | Repositories return a **copy** of the list |
| Two links with the same id | Ids generated with an **`AtomicInteger`** (ids are `Integer`) |

**Validation**: `ConcurrenceTest` starts 100 threads at the same time (`CountDownLatch`) and checks that the counter is exactly 100, followed by heavier tests (millions of increments, 10,000 simultaneous creations, reads during writes). I also checked that these tests **fail** when the protection is removed.

> Concurrency bugs are random: 100 threads doing one click each often pass even without protection. Very dense tests are needed to expose them.

## 6. Spring configuration (XML)

The project is delivered with the **XML configuration**.

```
Tomcat loads the WAR
  └─ web.xml
       ├─ ContextLoaderListener → applicationContext.xml   (root context)
       │     repositories, services, DataInitializer
       └─ DispatcherServlet      → dispatcher-servlet.xml  (web context)
             <mvc:annotation-driven/>, controllers, GlobalExceptionHandler
```

- Beans are declared with `<bean>` and `<constructor-arg ref="..."/>`; there is **no `<context:component-scan>`**.
- The web context can see the root context's beans (controllers receive services through `ref`).
- `@Controller` is only used as a **marker**: since Spring 6 it is required for a class to be detected as a handler. The bean is still declared in XML.
- Mapping annotations (`@RequestMapping`, `@GetMapping`, `@PathVariable`...) are used.

### The three possible configurations (only the first one is implemented)

| Config | Bean declaration | Spring MVC startup |
|---|---|---|
| **config-xml** *(implemented)* | `<bean>` + `<constructor-arg>` in `applicationContext.xml` | `web.xml` + `dispatcher-servlet.xml` |
| config-java | `@Bean` methods in `@Configuration` classes | `AbstractAnnotationConfigDispatcherServletInitializer` |
| config-annotation | `@Service`, `@Repository`, `@RestController` + `@ComponentScan` | `AbstractAnnotationConfigDispatcherServletInitializer` |

## 7. Installation and running

**Prerequisites**: JDK 17+, Maven 3.8+, Tomcat 10.1+ (Jakarta EE).

```bash
git clone <repo-url>
cd clicktrack
mvn clean package          # builds target/clicktrack.war
```

1. Copy `target/clicktrack.war` into Tomcat's `webapps/` folder (or deploy it from VS Code with *Community Server Connectors*).
2. Start Tomcat.
3. Try: `http://localhost:8080/clicktrack/api/publishers` (adjust the port; 8081 on my machine).

> Tomcat 10.1 uses the **`jakarta.*`** namespace (not `javax.*`).
> Data is stored in memory: it is **reset on every redeployment**.

## 8. Tests

### Unit tests (JUnit 5)

Tests live in `src/test/java` (the JUnit dependency has the `test` scope).

| Test class | What it checks |
|---|---|
| `LienTest` | The model's click counter |
| `LienServiceListerLiensTest` | Listing links, isolation between publishers, 404 |
| `LienServiceOffresTest` | Grouping by offer, sum of clicks (2 links = sum) |
| `LienServiceTrackingTest` | Click counted, link info, 404 |
| `ConcurrenceTest` | 100 threads → 100 clicks, simultaneous creations, reads during writes |

```bash
mvn clean test                    # all tests
mvn -Dtest=LienTest test          # a single class
```

### Postman scenarios

For requests with a body: **Body → raw → JSON**.

| # | Request | Expected result |
|---|---|---|
| 1 | `GET /api/publishers` | 200 + 3 publishers |
| 2 | `POST /api/publishers/1/liens` (valid body) | 201 + link with 0 clicks |
| 3 | `POST /api/publishers/999/liens` | 404 |
| 4 | `POST` with `"offre": ""` | 400 |
| 5 | `POST` without a body | 400 |
| 6 | `GET /t/1` (twice) | 200, `clicks` = 1 then 2 |
| 7 | `GET /t/999` | 404 |
| 8 | `GET /api/publishers/1/liens` | 200, up-to-date clicks |
| 9 | `GET /api/publishers/1/offres` | 200, sum of clicks per offer |
| 10 | `GET /api/publishers/999/offres` | 404 |

## 9. Bonus

- ✅ **`DataInitializer`**: creates the 3 test publishers at startup (`init()` method declared with `init-method="init"`).
- ⬜ **Redirection**: `GET /t/{id}` currently returns the link's info. Next step: count the click, then redirect (`302`) to `urlDestination`.
- ⬜ **Spring ORM / Hibernate**: not done.

## 10. Problems I ran into

| Symptom | Cause | Fix |
|---|---|---|
| `404: No endpoint GET /...` (Spring message) | Since Spring 6, `@RequestMapping` alone is no longer enough | Add `@Controller` to the controller |
| Tomcat `404` "resource not available" on **every** URL | Malformed `applicationContext.xml`: the application did not start | Read `logs/localhost.<date>.log` (look for `SEVERE`), fix the XML |
| `500: Required path variable 'x' is not present` | The name in `{x}` and in `@PathVariable("x")` differ | Use exactly the same name |
| `500` instead of `404` | `GlobalExceptionHandler` not declared in `dispatcher-servlet.xml` | Declare the bean in the web context |
| `201` expected but `500` returned | `@ResponseStatus` with no value | `@ResponseStatus(HttpStatus.CREATED)` |
| `405 Method Not Allowed` | `GET` sent to a `POST` endpoint | Use the right HTTP method |
| `400 Bad Request` | Missing or non-JSON body | Body → raw → **JSON** in Postman |
| `The import org.junit cannot be resolved` / `No sources to compile` | Test placed in `src/main/java` | Move it to `src/test/java` |
| Link list always empty | `Lien.publisherId` typed `Publisher` but compared with an `Integer` | Type `publisherId` as `Integer` |

## 11. What I learned

This project was my first real use of the **Spring Framework**, and I learned a lot.

### Spring Core
- **Inversion of control**: I no longer call `new` myself; Spring creates the objects (beans) and hands them to me.
- **Constructor dependency injection**, depending on **interfaces**: cleaner code that can be tested without Spring.
- A bean is a **singleton** by default: that is why all controllers share the same repository.
- Bean **lifecycle**: `init-method` for `DataInitializer`.
- **XML** configuration (`<bean>`, `<constructor-arg ref>`) and how it differs from the Java and annotation configurations.

### Spring MVC
- The path of a request: Tomcat → `web.xml` → `DispatcherServlet` → mapping → controller → JSON response.
- **Two contexts**: root (`applicationContext.xml`) for services and web (`dispatcher-servlet.xml`) for controllers.
- `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestBody`, `@ResponseBody`, `@ResponseStatus`.
- What changed in Spring 6 / 6.1: `@Controller` is required, explicit name in `@PathVariable("id")`.
- **`@RestControllerAdvice`** to handle errors in one place and turn an exception into an HTTP status.
- **Jackson** and **DTOs**: don't expose entities directly; Java 17 `record`s for short DTOs.

### Java and concurrency
- `AtomicInteger`, `synchronized`, returning **copies** of lists, `ExecutorService`, `CountDownLatch`.
- Understanding **why** a plain counter loses clicks, and seeing it happen.
- `Optional`, streams (`groupingBy`, `map`, `toList`), `record`s.

### Testing
- **JUnit 5**: `@Test`, `@BeforeEach`, `assertEquals`, `assertThrows`, the *Arrange / Act / Assert* pattern.
- Tests go in `src/test/java` and must be **independent** of each other.
- Testing concurrency is hard: it needs very intensive tests, and I need to check they really fail without the protection.

### Tools and method
- **Maven** (WAR packaging, dependency scopes), **Tomcat 10.1** (Jakarta), **Postman**.
- **Reading Tomcat's logs** (`SEVERE`): that is where the real cause of a 404 or a 500 is.
- Telling a **Spring 404** from a **Tomcat 404** (the application did not start).
- **Git / Jira**: one commit per task, with the ticket key (`CLIC-12`).
- HTTP status codes and what they mean: 200, 201, 400, 404, 405, 500.

## 12. Possible improvements

- `302` redirection to the destination URL in `GET /t/{id}`.
- Replace the `ArrayList`s with a database (Spring ORM / Hibernate).
- Validate the destination URL (`http://` or `https://` format).
- Implement the Java and annotation configurations.
- Add publisher authentication.

## Auteur

### MOUSTAFA OUTERGA:YouCode training, Sprint 3.
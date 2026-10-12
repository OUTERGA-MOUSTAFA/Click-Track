# ClickTrack

## 1. Présentation

ClickTrack est une API Spring MVC permettant aux publishers (influenceurs, YouTubeurs, blogueurs) de créer des liens de tracking vers leurs offres et de suivre le nombre de clics associés à leurs liens et à leurs offres.

Règles de gestion principales

- Une offre peut avoir plusieurs liens : ses clics sont la somme des clics de ses liens.
- À chaque ouverture d'un lien (`GET /t/{id}`), le clic est compté, puis les informations du lien sont affichées.
- Aucun clic ne doit être perdu lors de clics simultanés.

## 2. Technologies

- Java 17+
- Maven (packaging WAR)
- Spring Core 6.1.x
- Spring MVC 6.1.x
- Jackson (sérialisation JSON)
- Jakarta Servlet API 6.0 (Tomcat 10.1+)
- JUnit 5 (tests)
- Stockage en mémoire avec `ArrayList`

## 3. Architecture

Le projet suit une architecture en couches. Chaque couche dépend de l'interface de la couche inférieure, avec injection par constructeur.

    
Controller  →  Service (interface)  →  Repository (interface)
                   ↓                          ↓
                  DTO                       Model
    

- Controller : reçoit les requêtes HTTP, appelle le service, retourne les DTO et les codes HTTP.
- Service : logique métier (création de lien, regroupement des clics par offre, incrémentation).
- Repository : stockage et accès aux données dans des `ArrayList`.
- Model : entités `Publisher`, `Lien`, enum `Categorie`.
- DTO : objets retournés au client.

### Structure du projet

    
clicktrack/
├── pom.xml
└── src/main/
    ├── java/ma/youcode/clicktrack/
    │   ├── model/
    │   ├── dto/
    │   ├── repository/          (interfaces)
    │   │   └── impl/            (implémentations ArrayList)
    │   ├── service/             (interfaces)
    │   │   └── impl/
    │   ├── controller/
    │   ├── exception/
    │   └── init/                (DataInitializer)
    └── webapp/WEB-INF/
        ├── web.xml
        ├── applicationContext.xml
        └── dispatcher-servlet.xml
    

## 4. Endpoints

| Méthode | URL                              | Description                          | Réponse     |
|---------|----------------------------------|--------------------------------------|-------------|
| POST    | `/api/publishers/{id}/liens`     | Crée un lien de tracking             | 201 / 404   |
| GET     | `/api/publishers`                | Liste les publishers                 | 200         |
| GET     | `/api/publishers/{id}/liens`     | Liste les liens avec leurs clics     | 200 / 404   |
| GET     | `/api/publishers/{id}/offres`    | Liste les offres avec leurs clics    | 200 / 404   |
| GET     | `/t/{id}`                        | Compte le clic et affiche le lien    | 200 / 404   |

### Exemples

POST `/api/publishers/1/liens`

Requête :
    json
{
  "offre": "Casque audio X200",
  "urlDestination": "https://www.exemple.com/casque-audio"
}
    
Réponse `201 Created` :
    json
{
  "id": 1,
  "offre": "Casque audio X200",
  "urlDestination": "https://www.exemple.com/casque-audio",
  "clicks": 0,
  "publisherId": 1
}
    

GET `/api/publishers` → `200 OK`
    json
[
  { "id": 1, "nom": "Alami", "prenom": "Sara", "email": "sara.alami@example.com", "categorie": "INFLUENCEUR" }
]
    

GET `/api/publishers/1/offres` → `200 OK`
    json
[
  { "offre": "Casque audio X200", "nombreLiens": 1, "clicks": 10 },
  { "offre": "Montre connectée Z5", "nombreLiens": 1, "clicks": 2 }
]
    

GET `/t/1` (après incrémentation du clic) → `200 OK`
    json
{
  "urlRedirection": "https://www.exemple.com/casque-audio",
  "publisher": "Sara Alami",
  "offre": "Casque audio X200",
  "clicks": 11
}
    

### Codes d'erreur

| Cas                                   | Code |
|---------------------------------------|------|
| Publisher inexistant                  | 404  |
| Lien de tracking inexistant (`/t/{id}`) | 404  |
| Corps de requête invalide (champ manquant) | 400  |

## 5. Installation et lancement

### Prérequis

- JDK 17 ou supérieur (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Apache Tomcat 10.1+ (Jakarta EE)

### Étapes

1. Cloner le projet et se placer à la racine :
      bash
   git clone <url-du-repo>
   cd clicktrack
      
2. Compiler et générer le WAR :
      bash
   mvn clean package
      
3. Le fichier `target/clicktrack.war` est généré.
4. Copier le WAR dans le dossier `webapps/` de Tomcat 10.1+ (ou le déployer depuis VS Code avec l'extension *Community Server Connectors*).
5. Démarrer Tomcat, puis tester : `http://localhost:8080/clicktrack/api/publishers`
6. Tester les endpoints avec Postman.

> Remarque : Tomcat 10.1 utilise le namespace `jakarta.*` (et non `javax.*`).

## 6. Tests

### Tests manuels (Postman)

| # | Scénario                                         | Résultat attendu | Résultat obtenu |
|---|--------------------------------------------------|------------------|-----------------|
| 1 | `GET /api/publishers`                            | 200 + liste      | ☐               |
| 2 | `POST /api/publishers/1/liens` avec corps valide | 201 + lien créé  | ☐               |
| 3 | `POST /api/publishers/999/liens`                 | 404              | ☐               |
| 4 | `GET /api/publishers/1/liens`                    | 200 + clics      | ☐               |
| 5 | `GET /api/publishers/1/offres`                   | 200 + somme des clics par offre | ☐ |
| 6 | `GET /t/1` (appelé 2 fois)                       | 200, clicks +1 à chaque appel | ☐ |
| 7 | `GET /t/999`                                     | 404              | ☐               |

### Tests unitaires (JUnit 5)

- Tests du service : création d'un lien, regroupement des clics par offre, publisher inexistant.
- Test de concurrence : 100 threads (`ExecutorService`) appellent l'incrémentation sur le même lien → le compteur final doit être exactement 100.

Lancer les tests :
    bash
mvn test
    

## 7. Gestion de la concurrence

Plusieurs visiteurs peuvent cliquer en même temps ; aucun clic ni aucune donnée ne doit être perdu.

- Incrémentation des clics : le compteur `clicks` du lien est protégé (`AtomicInteger` ou méthode `synchronized`). L'incrémentation et la lecture de la nouvelle valeur sont atomiques, donc le nombre de clics renvoyé est toujours exact.
- Collections partagées : les méthodes du repository qui lisent ou modifient l'`ArrayList` sont `synchronized` (ou protégées par un `ReentrantLock`). Les méthodes de lecture retournent une copie de la liste (`new ArrayList<>(...)`) pour éviter les `ConcurrentModificationException`.
- Génération des identifiants : `AtomicLong` pour éviter les doublons d'id.
- Validation : un test avec 100 threads vérifie que le compteur final vaut exactement 100.

## 8. Configurations Spring

Le projet est livré avec la configuration XML. Les deux autres configurations sont documentées ci-dessous.

| Config            | Déclaration des beans                                                        | Démarrage de Spring MVC                                  |
|-------------------|------------------------------------------------------------------------------|----------------------------------------------------------|
| config-xml (livrée) | `<bean>` et `<constructor-arg>` dans `applicationContext.xml`, aucune annotation de stéréotype | `web.xml` + `dispatcher-servlet.xml` |
| config-java       | Méthodes `@Bean` dans des classes `@Configuration`, aucun scan               | `AbstractAnnotationConfigDispatcherServletInitializer`   |
| config-annotation | `@Service`, `@Repository`, `@RestController`, détection par `@ComponentScan` | `AbstractAnnotationConfigDispatcherServletInitializer`   |

### config-xml (utilisée)

- `applicationContext.xml` : déclare les repositories, les services (avec `<constructor-arg ref="..."/>`) et le `DataInitializer`.
- `dispatcher-servlet.xml` : `<mvc:annotation-driven/>` et déclaration des controllers en `<bean>`.
- `web.xml` : déclare le `DispatcherServlet` et le `ContextLoaderListener`.
- Les annotations de mapping (`@RequestMapping`, `@GetMapping`, `@PathVariable`...) restent autorisées.

### config-java

    java
@Configuration
public class AppConfig {
    @Bean
    public LienRepository lienRepository() { return new InMemoryLienRepository(); }

    @Bean
    public LienService lienService() { return new LienServiceImpl(lienRepository(), publisherRepository()); }
}
    

### config-annotation

    java
@Service
public class LienServiceImpl implements LienService { ... }

@Configuration
@ComponentScan("ma.youcode.clicktrack")
public class AppConfig { }
    

## 9. Bonus

### Redirection vers l'URL de destination

`GET /t/{id}` compte le clic puis redirige le visiteur vers l'URL de destination :

    java
return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(lien.getUrlDestination()))
        .build();
    

### Initialisation des données

Un bean `DataInitializer` crée les publishers de test au démarrage de l'application, dans sa méthode d'initialisation (phase *init* du cycle de vie du bean) :

    xml
<bean class="ma.youcode.clicktrack.init.DataInitializer" init-method="init">
    <constructor-arg ref="publisherRepository"/>
</bean>
    

## Auteur

Projet réalisé dans le cadre de la formation YouCode - Sprint 3, Brief 1.

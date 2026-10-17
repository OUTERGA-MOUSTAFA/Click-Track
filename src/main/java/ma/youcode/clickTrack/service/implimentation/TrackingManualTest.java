package ma.youcode.clickTrack.service.implimentation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.TrackingResponse;
import ma.youcode.clickTrack.model.Categories;
import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.repository.LienRepository;
import ma.youcode.clickTrack.repository.PublisherRepository;
import ma.youcode.clickTrack.repository.implimentation.ImplimentationLienRepository;
import ma.youcode.clickTrack.repository.implimentation.ImplimentationPublisherRepository;
import ma.youcode.clickTrack.service.LienService;

/** Test SANS JUnit (classe avec main). Lancer avec le bouton "Run" de VS Code. */
public class TrackingManualTest {

    private static int reussis = 0;
    private static int echoues = 0;
    private static LienService lienService;
    private static Integer sara;

    private static void verifier(String nomDuTest, boolean condition) {
        if (condition) {
            reussis++;
            System.out.println("OK    " + nomDuTest);
        } else {
            echoues++;
            System.out.println("ECHEC " + nomDuTest);
        }
    }

    private static void preparer() {
        PublisherRepository publisherRepository = new ImplimentationPublisherRepository();
        LienRepository lienRepository = new ImplimentationLienRepository();
        lienService = new LienServiceImpl(lienRepository, publisherRepository);
        sara = publisherRepository.save(
                new Publisher(null, "Alami", "Sara", "sara.alami@example.com", Categories.INFLUENCEUR)).getId();
    }

    private static Integer ajouterLien(String offre, String url) {
        CreerLienRequest request = new CreerLienRequest();
        request.setOffre(offre);
        request.setUrlDestination(url);
        return lienService.creeLien(sara, request).id();
    }

    public static void main(String[] args) throws Exception {

        // --- premier click : infos du lien ---
        preparer();
        Integer lienId = ajouterLien("Casque audio X200", "https://www.exemple.com/casque-audio");
        TrackingResponse r = lienService.enregistrerClick(lienId);
        verifier("click : urlRedirection", r.urlRedirection().equals("https://www.exemple.com/casque-audio"));
        verifier("click : publisher = \"Prénom Nom\"", r.publisher().equals("Sara Alami"));
        verifier("click : offre", r.offre().equals("Casque audio X200"));
        verifier("click : 1er click = 1", r.clicks() == 1);

        // --- incrémentation à chaque ouverture ---
        verifier("click : 2e ouverture = 2", lienService.enregistrerClick(lienId).clicks() == 2);
        verifier("click : 3e ouverture = 3", lienService.enregistrerClick(lienId).clicks() == 3);

        // --- 404 ---
        boolean introuvable = false;
        try {
            lienService.enregistrerClick(999);
        } catch (NoSuchElementException e) {
            introuvable = true;
        }
        verifier("click : lien 999 -> NoSuchElementException (404)", introuvable);

        // --- compteurs indépendants ---
        preparer();
        Integer casque = ajouterLien("Casque audio X200", "https://exemple.com/casque");
        Integer montre = ajouterLien("Montre connectée Z5", "https://exemple.com/montre");
        lienService.enregistrerClick(casque);
        lienService.enregistrerClick(casque);
        lienService.enregistrerClick(casque);
        verifier("click : 2 liens = 2 compteurs indépendants", lienService.enregistrerClick(montre).clicks() == 1);

        // --- concurrence : 100 visiteurs en même temps ---
        preparer();
        Integer lienConcurrent = ajouterLien("Casque audio X200", "https://exemple.com/casque");
        int nombre = 100;
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch depart = new CountDownLatch(1);
        Set<Integer> valeursRecues = ConcurrentHashMap.newKeySet();
        List<Future<?>> taches = new ArrayList<>();
        for (int i = 0; i < nombre; i++) {
            taches.add(pool.submit(() -> {
                try {
                    depart.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                valeursRecues.add(lienService.enregistrerClick(lienConcurrent).clicks());
            }));
        }
        depart.countDown();
        for (Future<?> tache : taches) {
            tache.get();
        }
        pool.shutdown();
        verifier("concurrence : 100 visiteurs -> 100 numéros différents", valeursRecues.size() == nombre);
        verifier("concurrence : le click suivant = 101", lienService.enregistrerClick(lienConcurrent).clicks() == nombre + 1);

        System.out.println();
        System.out.println(reussis + " OK, " + echoues + " ECHEC");
        if (echoues > 0) {
            System.exit(1);
        }
    }
}
package ma.youcode.clickTrack.service.implimentation;

import java.util.List;
import java.util.NoSuchElementException;
 
import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;
import ma.youcode.clickTrack.dto.OffreStatsResponse;
import ma.youcode.clickTrack.model.Categories;
import ma.youcode.clickTrack.model.Lien;
import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.repository.LienRepository;
import ma.youcode.clickTrack.repository.PublisherRepository;
import ma.youcode.clickTrack.repository.implimentation.ImplimentationLienRepository;
import ma.youcode.clickTrack.repository.implimentation.ImplimentationPublisherRepository;
import ma.youcode.clickTrack.service.LienService;

public class LienServiceManualTest {
 
    private static int reussis = 0;
    private static int echoues = 0;
 
    private static LienRepository lienRepository;
    private static LienService lienService;
    private static Integer sara;
    private static Integer youssef;
 
    // ---------- petits outils ----------
 
    private static void verifier(String nomDuTest, boolean condition) {
        if (condition) {
            reussis++;
            System.out.println("OK    " + nomDuTest);
        } else {
            echoues++;
            System.out.println("ECHEC " + nomDuTest);
        }
    }
 
    /** Vrai si l'action lance exactement ce type d'exception. */
    private static boolean lanceException(Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
            return false;
        } catch (RuntimeException e) {
            return type.isInstance(e);
        }
    }
 
    // repart d'un état propre avant chaque groupe de tests
    private static void preparer() {
        PublisherRepository publisherRepository = new ImplimentationPublisherRepository();
        LienRepository lienRepository = new ImplimentationLienRepository();
        lienService = new LienServiceImpl(lienRepository, publisherRepository);
 
        sara = publisherRepository.save(
                new Publisher(null, "Alami", "Sara", "sara.alami@example.com", Categories.INFLUENCEUR)).getId();
        youssef = publisherRepository.save(
                new Publisher(null, "Bennani", "Youssef", "youssef.bennani@example.com", Categories.YOUTUBEUR)).getId();
    }
 
    private static LienResponse ajouterLien(Integer publisherId, String offre, String url) {
        CreerLienRequest request = new CreerLienRequest();
        request.setOffre(offre);
        request.setUrlDestination(url);
        return lienService.creeLien(publisherId, request);
    }
 
    // simule des clicks (le vrai endpoint /t/{id} viendra plus tard)
    private static void simulerClicks(Integer lienId, int nombre) {
        Lien lien = lienRepository.findById(lienId).orElseThrow();
        for (int i = 0; i < nombre; i++) {
            lien.incrementerClicks();
        }
    }
 
    // ---------- les tests ----------
 
    public static void main(String[] args) {
 
        // --- creeLien ---
        preparer();
        LienResponse lien = ajouterLien(sara, "Casque audio X200", "https://exemple.com/casque");
        verifier("creeLien : id attribué", lien.id() != null);
        verifier("creeLien : démarre à 0 click", lien.clicks() == 0);
        verifier("creeLien : publisherId correct", lien.publisherId().equals(sara));
        verifier("creeLien : publisher 999 -> NoSuchElementException",
                lanceException(NoSuchElementException.class, () -> ajouterLien(999, "Offre", "https://a.com")));
        verifier("creeLien : offre vide -> IllegalArgumentException",
                lanceException(IllegalArgumentException.class, () -> ajouterLien(sara, "   ", "https://a.com")));
        verifier("creeLien : url null -> IllegalArgumentException",
                lanceException(IllegalArgumentException.class, () -> ajouterLien(sara, "Offre", null)));
 
        // --- listerOffres : 2 liens de la même offre = somme des clicks ---
        preparer();
        LienResponse l1 = ajouterLien(sara, "Casque audio X200", "https://exemple.com/1");
        LienResponse l2 = ajouterLien(sara, "Casque audio X200", "https://exemple.com/2");
        simulerClicks(l1.id(), 3);
        simulerClicks(l2.id(), 4);
        List<OffreStatsResponse> offres = lienService.listOffres(sara);
        verifier("offres : une seule ligne pour la même offre", offres.size() == 1);
        verifier("offres : nombreLiens = 2", offres.get(0).nombreLiens() == 2);
        verifier("offres : clicks = 3 + 4 = 7", offres.get(0).clicks() == 7);
 
        // --- listerOffres : deux offres différentes ---
        preparer();
        LienResponse casque = ajouterLien(sara, "Casque audio X200", "https://exemple.com/casque");
        LienResponse montre = ajouterLien(sara, "Montre connectée Z5", "https://exemple.com/montre");
        simulerClicks(casque.id(), 10);
        simulerClicks(montre.id(), 2);
        offres = lienService.listOffres(sara);
        verifier("offres : 2 lignes", offres.size() == 2);
        verifier("offres : casque = 1 lien, 10 clicks",
                offres.get(0).equals(new OffreStatsResponse("Casque audio X200", 1, 10)));
        verifier("offres : montre = 1 lien, 2 clicks",
                offres.get(1).equals(new OffreStatsResponse("Montre connectée Z5", 1, 2)));
 
        // --- listerOffres : isolation entre publishers ---
        preparer();
        ajouterLien(sara, "Casque audio X200", "https://exemple.com/1");
        ajouterLien(youssef, "Montre connectée Z5", "https://exemple.com/2");
        offres = lienService.listOffres(sara);
        verifier("offres : les liens d'un autre publisher n'apparaissent pas",
                offres.size() == 1 && offres.get(0).offre().equals("Casque audio X200"));
 
        // --- listerOffres : cas limites ---
        preparer();
        verifier("offres : publisher sans lien -> liste vide", lienService.listOffres(sara).isEmpty());
        verifier("offres : publisher 999 -> NoSuchElementException",
                lanceException(NoSuchElementException.class, () -> lienService.listOffres(999)));
 
        // ---------- résumé ----------
        System.out.println();
        System.out.println(reussis + " OK, " + echoues + " ECHEC");
        if (echoues > 0) {
            System.exit(1);
        }
    }
}
 
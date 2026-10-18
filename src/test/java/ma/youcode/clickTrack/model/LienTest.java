package ma.youcode.clickTrack.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Mon premier test JUnit : on teste la classe Lien (modèle), sans Spring ni repository.
 * Même package que Lien => pas besoin de l'importer.
 */
class LienTest {

    private Lien lien;

    // S'exécute AVANT CHAQUE @Test : chaque test repart avec un lien neuf (tests indépendants)
    @BeforeEach
    void setUp() {
        lien = new Lien(1, "Casque audio X200", "https://www.exemple.com/casque-audio", 0, 1);
    }

    @Test
    void nouveauLien_demarreAZeroClick() {
        // ASSERT : "j'attends 0, et la vraie valeur est lien.getClicks()"
        assertEquals(0, (int) lien.getClicks());
    }

    @Test
    void incrementerClicks_retourneLaNouvelleValeur() {
        // ACT : on fait l'action à tester
        int resultat = lien.incrementerClicks();

        // ASSERT : on vérifie le résultat
        assertEquals(1, resultat);
        assertEquals(1, (int) lien.getClicks());
    }

    @Test
    void troisIncrementations_donnentTroisClicks() {
        lien.incrementerClicks();
        lien.incrementerClicks();
        lien.incrementerClicks();

        assertEquals(3, (int) lien.getClicks());
    }
}
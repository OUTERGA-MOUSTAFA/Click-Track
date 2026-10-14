package ma.youcode.clickTrack.service.implimentation;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;
import ma.youcode.clickTrack.model.Lien;
import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.repository.LienRepository;
import ma.youcode.clickTrack.repository.PublisherRepository;
import ma.youcode.clickTrack.service.LienService;

public class LienServiceImpl implements LienService {

    private final LienRepository lienRepository;
    private final PublisherRepository publisherRepository;

    public LienServiceImpl(LienRepository lienRepository, PublisherRepository publisherRepository) {

        this.lienRepository = lienRepository;
        this.publisherRepository = publisherRepository;
    }

    @Override
    public LienResponse creeLien(Integer publisherId, CreerLienRequest lienRequest) {

        // Vérifie que le publisher existe sinon -> 404 Not Found
        if (publisherRepository.findById(publisherId).isEmpty()) {
            throw new IllegalArgumentException("Publisher introuvable avec l'id " + publisherId);
        }

        // Validation du corps -> sinon 400
        if (lienRequest == null || estVide(lienRequest.getOffre()) || estVide(lienRequest.getUrlDestination())) {
            throw new IllegalArgumentException("Les champs 'offre' et 'urlDestination' sont obligatoires");
        }

        //  Création : un nouveau lien démarre à 0 click, l'id est attribué par le repository
        Lien lien = new Lien (null, lienRequest.getOffre().trim(), lienRequest.getUrlDestination().trim(), 0, publisherId);
        return versResponse(lienRepository.save(lien));
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
 
    private LienResponse versResponse(Lien lien) {
        return new LienResponse(lien.getId(), lien.getOffre(), lien.getUrlDestination(), lien.getClicks(), lien.getPublisherId());
    }

}

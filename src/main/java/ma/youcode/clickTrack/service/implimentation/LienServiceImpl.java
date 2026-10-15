package ma.youcode.clickTrack.service.implimentation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;
import ma.youcode.clickTrack.dto.OffreStatsResponse;
import ma.youcode.clickTrack.dto.TrackingResponse;
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


    @Override 
    public List<OffreStatsResponse> listOffres(Integer publisherId){

        // check publisher if exist sinon -> 404
        if (publisherRepository.findById(publisherId).isEmpty()) {
            throw new IllegalArgumentException("Publisher introuvable avec l'id " + publisherId);
        }

        Map<String, List<Lien>> lienParOffre = lienRepository.findLienByPublisherId(publisherId)
                                                            .stream()
                                                            .collect(Collectors.groupingBy(Lien::getOffre, 
                                                                                            LinkedHashMap::new, 
                                                                                            Collectors.toList()));
        return lienParOffre.entrySet().stream().map(i -> new OffreStatsResponse(i.getKey(),
                                                                                i.getValue().size(),
                                                                                i.getValue().stream().mapToInt(Lien::getClicks).sum())).toList();
                                                            
        
    }



    @Override 
    public  TrackingResponse enregistrerClick(Integer lienId){
        // Le lien doit exister sinon 404
        Lien lien = lienRepository.findById(lienId)
                .orElseThrow(() -> new NoSuchElementException("Lien introuvable avec l'id " + lienId));

        // On récupère le publisher AVANT de compter : si quelque chose manque, aucun click n'est compté pour rien
        Publisher publisher = publisherRepository.findById(lien.getPublisherId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Publisher introuvable avec l'id " + lien.getPublisherId()));
        

        //  Incrémentation ATOMIQUE. On utilise la valeur RETOURNÉE par incrementerClicks()
        //  (et pas lien.getClicks() après coup) : avec des clicks simultanés, chaque visiteur
        //    reçoit ainsi exactement le numéro de SON click, sans doublon ni click perdu.
        int clicks = lien.incrementerClicks();

        return new TrackingResponse(lien.getUrlDestination(), publisher.getNom() +" " + publisher.getPrenom(), lien.getOffre(), clicks);
    }



}

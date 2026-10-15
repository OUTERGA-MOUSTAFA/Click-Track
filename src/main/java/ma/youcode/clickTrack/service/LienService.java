package ma.youcode.clickTrack.service;

import java.util.List;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;
import ma.youcode.clickTrack.dto.OffreStatsResponse;
import ma.youcode.clickTrack.dto.TrackingResponse;

public interface LienService {

    // Crée un lien de tracking rattaché à une offre pour le publisher donné
    LienResponse creeLien(Integer publisherId, CreerLienRequest lienRequest);
    // list dyale les offers avec sum dyale clicks dyale kela lien
    List<OffreStatsResponse> listOffres (Integer publisherId);

    // Compte un click sur le lien puis retourne ses informations (url, publisher, offre, clicks).
    TrackingResponse enregistrerClick(Integer lienId);

}
package ma.youcode.clickTrack.service;

import java.util.List;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;
import ma.youcode.clickTrack.dto.OffreStatsResponse;

public interface LienService {

    //Crée un lien de tracking rattaché à une offre pour le publisher donné
    LienResponse creeLien(Integer publisherId, CreerLienRequest lienRequest);
    List<OffreStatsResponse> listOffres (Integer publisherId);
}
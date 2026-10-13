package ma.youcode.clickTrack.service;

import ma.youcode.clickTrack.dto.CreerLienRequest;
import ma.youcode.clickTrack.dto.LienResponse;

public interface LienService {

    //Crée un lien de tracking rattaché à une offre pour le publisher donné
    LienResponse creeLien(Integer publisherId, CreerLienRequest lienRequest);
    
}
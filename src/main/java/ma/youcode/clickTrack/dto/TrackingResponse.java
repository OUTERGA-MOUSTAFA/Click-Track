package ma.youcode.clickTrack.dto;

// Réponse de GET /t/{id} : infos du lien après comptage du click. publisher = "Prénom Nom".
public record TrackingResponse(String urlRedirection, String publisher, String offre, int clicks) {
}

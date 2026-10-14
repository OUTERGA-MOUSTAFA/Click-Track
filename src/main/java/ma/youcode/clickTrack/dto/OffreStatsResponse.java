package ma.youcode.clickTrack.dto;

// GET /api/publishers/{id}/offres :   offre   | nombre de liens  | somme des clicks
public record OffreStatsResponse(String offre, int nombreLiens, int clicks) {}

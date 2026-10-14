package ma.youcode.clickTrack.dto;

public class CreerLienRequest {
    private String offre;
    private String urlDestination;

    public CreerLienRequest() {}//Jackson kay-créer l-objet b constructeur khawi

     public CreerLienRequest(String offre, String urlDestination) {
        this.offre = offre;
        this.urlDestination = urlDestination;
    }
    
    public String getOffre() {
        return offre;
    }
    public void setOffre(String offre) {
        this.offre = offre;
    }
    public String getUrlDestination() {
        return urlDestination;
    }
    public void setUrlDestination(String urlDestination) {
        this.urlDestination = urlDestination;
    }

    
}

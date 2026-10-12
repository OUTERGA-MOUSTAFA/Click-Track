package ma.youcode.clickTrack.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Lien {
    

    private Integer id;
    private String offre;
    private String urlDestination;
    private final AtomicInteger clicks = new AtomicInteger();
    private Publisher publisherId;

    public Lien(){}
    public Lien(Integer id, String offre, String urlDestination, int clicks, Publisher publisherId){
        
        this.id = id;
        this.offre = offre;
        this.urlDestination = urlDestination;
        this.clicks.set(clicks);
        this.publisherId = publisherId;
    }

    public Integer getId() {
        return id;
    }
    public String getOffre() {
        return offre;
    }
    public String getUrlDestination() {
        return urlDestination;
    }
    public Integer getClicks() {
        return clicks.get();
    }
    public Publisher getPublisherId() {
        return publisherId;
    }


     public void setId(Integer id) {
        this.id = id;
    }
    public void setOffre(String offre) {
        this.offre = offre;
    }
    public void setUrlDestination(String urlDestination) {
        this.urlDestination = urlDestination;
    }
  
    /** Incrémente le compteur de façon atomique et retourne la nouvelle valeur.
    Aucun clic n'est perdu même si plusieurs visiteurs cliquent en même temps.  */
     
    public int incrementerClicks() {
        return clicks.incrementAndGet();
    }

    public void setPublisherId(Publisher publisherId) {
        this.publisherId = publisherId;
    }

}

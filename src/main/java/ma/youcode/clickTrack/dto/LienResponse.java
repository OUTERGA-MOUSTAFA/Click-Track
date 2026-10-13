package ma.youcode.clickTrack.dto;

public class LienResponse{

    private Integer id;
    private String offre;
    private String urlDestination;
    private int clicks;
    private Integer publisherId;
    public LienResponse(Integer id, String offre, String urlDestination, int clicks, Integer publisherId) {
        this.id = id;
        this.offre = offre;
        this.urlDestination = urlDestination;
        this.clicks = clicks;
        this.publisherId = publisherId;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
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


    public int getClicks() {
        return clicks;
    }
    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    
    public Integer getPublisherId() {
        return publisherId;
    }
    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    
}

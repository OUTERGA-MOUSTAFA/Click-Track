package ma.youcode.clickTrack.model;

public class Publisher {
    
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private Categories categorie;

    public Publisher(){  }

    public Publisher(Integer id, String nom, String prenom, String email, Categories categorie){

        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.categorie = categorie;
    }

    public Integer getId(){
        return id;
    }
    public String getNom(){
        return nom;
    }
    public String getPrenom(){
        return prenom;
    }
    public String getEmail(){
        return email;
    }
    public Categories getCategorie(){
        return categorie;
    }

    
    public void  setId(Integer id){
        this.id = id;
    }
    public void  setNom(String nom){
        this.nom = nom;
    }
    public void  setPrenom(String prenom){
        this.prenom = prenom;
    }
    public void  setEmail(String email){
        this.email = email;
    }
    public void  setCategorie(Categories categorie){
        this.categorie = categorie;
    }


}

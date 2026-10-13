package ma.youcode.clickTrack.init;

import ma.youcode.clickTrack.model.Categories;
import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.repository.PublisherRepository;

// Data for Test
public class DataInitializer {
    
    private final PublisherRepository publisherRepository;

    public DataInitializer(PublisherRepository publisherRepository){
        this.publisherRepository = publisherRepository;
    }

    public void init(){
        publisherRepository.save(new Publisher(null, "Alami", "Sara", "sara.alami@example.com", Categories.INFLUENCEUR));
        publisherRepository.save(new Publisher(null, "Bennani", "Youssef", "youssef.bennani@example.com", Categories.YOUTUBEUR));
        publisherRepository.save(new Publisher(null, "El Idrissi", "Imane", "imane.elidrissi@example.com", Categories.BLOGUEUR));
    }
}

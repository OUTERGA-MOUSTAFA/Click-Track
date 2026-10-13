package ma.youcode.clickTrack.service.implimentation;

import java.util.List;

import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.repository.PublisherRepository;
import ma.youcode.clickTrack.service.PublisherService;

public class PublisherServiceImpl implements PublisherService{
    //dépendance vers l'INTERFACE
    // injecté par constructor f ApplicationContext.xml
    private final PublisherRepository publisherRepository;


    public PublisherServiceImpl(PublisherRepository publisherRepository){
        this.publisherRepository = publisherRepository;
    }

    @Override 
    public List<Publisher> listPublisers() {
        return publisherRepository.findAll();
    }
}

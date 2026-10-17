package ma.youcode.clickTrack.repository;

import java.util.List;
import java.util.Optional;

import ma.youcode.clickTrack.model.Publisher;

/**
 * PublisherRepository
 */
public interface PublisherRepository {

    // Get all Publishers
    List<Publisher> findAll();

    // Get spesifique Publisher by id
    //                  find publisher by id
    Optional<Publisher> findById(Integer id);

    //  Enregistre un publisher et lui attribue un id (utilisé par DataInitializer). 
    Publisher save(Publisher publisher);
    
}
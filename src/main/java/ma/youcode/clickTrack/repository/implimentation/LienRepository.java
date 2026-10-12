package ma.youcode.clickTrack.repository.implimentation;

import java.util.List;
import java.util.Optional;

import ma.youcode.clickTrack.model.Lien;
import ma.youcode.clickTrack.model.Publisher;

public interface LienRepository {
    List<Publisher> findLienByPublisherId(Integer publisherId);

    Optional<Publisher> findById(Integer id);

    Lien save(Lien lien);

}
package ma.youcode.clickTrack.repository;

import java.util.List;
import java.util.Optional;

import ma.youcode.clickTrack.model.Lien;

public interface LienRepository {
    List<Lien> findLienByPublisherId(Integer publisherId);

    Optional<Lien> findById(Integer id);

    Lien save(Lien lien);

}
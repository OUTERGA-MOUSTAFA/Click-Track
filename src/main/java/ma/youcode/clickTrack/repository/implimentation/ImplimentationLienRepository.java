package ma.youcode.clickTrack.repository.implimentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import ma.youcode.clickTrack.model.Lien;
import  ma.youcode.clickTrack.repository.LienRepository;
public class ImplimentationLienRepository implements LienRepository {
    
    private final List<Lien> liens = new ArrayList<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    @Override
     public synchronized Lien save(Lien lien) {
        lien.setId(sequence.incrementAndGet());
        liens.add(lien);
        return lien;
    }
 
    @Override
    public synchronized Optional<Lien> findById(Integer id) {
        // On retourne le vrai objet (pas une copie) : le compteur de clics
        // incrémenté par le service doit modifier le lien stocké.
        return liens.stream()
                .filter(l -> l.getId().equals(id))
                .findFirst();
    }
 
    @Override
    public synchronized List<Lien> findLienByPublisherId(Integer publisherId) {
        return liens.stream()
                .filter(l -> l.getPublisherId().equals(publisherId))
                .toList();
    }

}

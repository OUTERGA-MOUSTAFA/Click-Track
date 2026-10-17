package ma.youcode.clickTrack.repository.implimentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import ma.youcode.clickTrack.model.Publisher;
import ma.youcode.clickTrack.repository.PublisherRepository;

public class ImplimentationPublisherRepository implements PublisherRepository{
    private final List<Publisher> publishers = new ArrayList<>();
    private final AtomicInteger sequence = new AtomicInteger(0);


    @Override 
    //synchronized -> bash n Protéger l’ArrayList Lien khass tcheck
    public synchronized List<Publisher> findAll() {
        // copie : l'appelant ne touche jamais à la liste interne
        return new ArrayList<>(publishers);
    }

    @Override
    public synchronized Optional<Publisher> findById(Integer id) {
        return publishers.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }
 
    @Override
    public synchronized Publisher save(Publisher publisher) {
        publisher.setId(sequence.incrementAndGet());
        publishers.add(publisher);
        return publisher;
    }

}

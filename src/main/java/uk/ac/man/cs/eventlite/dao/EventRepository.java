package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;
import uk.ac.man.cs.eventlite.entities.Event;
import org.springframework.data.domain.Sort;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventRepository extends CrudRepository<Event, Long> {
    public long count();
    public Iterable<Event> findAll(Sort sort);
    public Event save(Event event);

    public Event findById(long id);
    public Iterable<Event> findByName(String name);
}

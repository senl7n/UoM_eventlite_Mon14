package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface EventService {

    public long count();

    public Iterable<Event> findAll();

    public void save(Event event);

//    public void addEvent(Event event);

    public void deleteById(long id);

    public boolean update(long id, String name, LocalDate date, LocalTime time, long venueId);

    public Event findById(long id);

}

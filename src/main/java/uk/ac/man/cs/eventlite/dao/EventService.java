package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventService {

    public long count();

    public Iterable<Event> findAll();

    public void save(Event event);

    public void deleteById(long id);

    public boolean update(long id, String name, LocalDate date, LocalTime time, long venueId, String description);

    public Event findById(long id);
    
    public Iterable<Event> findByNameContainingIgnoreCase(String name);
    
    public boolean add(String name, LocalDate date, LocalTime time, long venueId, String description);

    public Iterable<Event> findUpcomingEvents();

    public Iterable<Event> findPreviousEvents();

    //home
	public Iterable<Event> findUpcoming3Events();

	//public Iterable<Venue> findTop3Venues();

}

package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import uk.ac.man.cs.eventlite.entities.Event;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EventRepository extends CrudRepository<Event, Long> {
    public long count();
    public Iterable<Event> findAll(Sort sort);
    public Event save(Event event);

    public Event findById(long id);
    public Iterable<Event> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT e FROM Event e WHERE e.date >= :today ORDER BY e.date ASC, e.time ASC")
    public Iterable<Event> findUpcomingEvents(@Param("today") LocalDate today);

    @Query("SELECT e FROM Event e WHERE e.date < :today ORDER BY e.date DESC, e.time DESC")
    public Iterable<Event> findPreviousEvents(@Param("today") LocalDate today);
	
    public Iterable<Event> findByDateAfterOrDateEqualsAndTimeAfterOrderByDateAscTimeAsc(LocalDate currentDate,
			LocalDate currentDate2, LocalTime currentTime);
	
    public Iterable<Event> findByDateBeforeOrDateEqualsAndTimeBeforeOrderByDateDescTimeDesc(LocalDate currentDate,
			LocalDate currentDate2, LocalTime currentTime);

}

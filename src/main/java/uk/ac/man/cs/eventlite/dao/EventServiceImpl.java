package uk.ac.man.cs.eventlite.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Override
    public long getNumberOfEvent() {
        return eventRepository.count();
	}
    
    @Override
    public Iterable<Event> findUpcomingEvents() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Event> upcomingEvents = (List<Event>) eventRepository.findByDateAfterOrDateEqualsAndTimeAfterOrderByDateAscTimeAsc(currentDate, currentDate, currentTime);
        List<Event> nullTimeEvents = (List<Event>) eventRepository.findByDateEqualsAndTimeIsNull(currentDate);
        upcomingEvents.addAll(nullTimeEvents);
        upcomingEvents.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getName).thenComparing(Event::getTime));
        return upcomingEvents;
    }

    @Override
    public Iterable<Event> findPreviousEvents() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Event> previousEvents = (List<Event>) eventRepository.findByDateBeforeOrDateEqualsAndTimeBeforeOrderByDateDescTimeDesc(currentDate, currentDate, currentTime);
        previousEvents.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getName).thenComparing(Event::getTime));
        return previousEvents;
        
    }

    //home
    @Override
    public Iterable<Event> findUpcoming3Events() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Event> upcomingEvents = (List<Event>) eventRepository.findByDateAfterOrDateEqualsAndTimeAfterOrderByDateAscTimeAsc(currentDate, currentDate, currentTime);
        List<Event> nullTimeEvents = (List<Event>) eventRepository.findByDateEqualsAndTimeIsNull(currentDate);
        upcomingEvents.addAll(nullTimeEvents);
        upcomingEvents.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getName).thenComparing(Event::getTime));

        List<Event> upcoming3Events = (List<Event>) upcomingEvents.subList(0,3);
        return upcoming3Events;
    }

    @Override
	public Iterable<Event> findAll() {
		return eventRepository.findAll(Sort.by("date").and(Sort.by("name")));
	}

    @Override
    public void save(Event event) {
        eventRepository.save(event);
    }

    @Override
    public void deleteById(long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public boolean update(long id, String name, LocalDate date, LocalTime time, long venueId, String description) {
        if (eventRepository.findById(id) == null || name.isEmpty() || venueRepository.findById(venueId).isEmpty()) {
            return false;
        }
        deleteById(id);
        Event event = new Event();
        event.setName(name);
        event.setDate(date);
        event.setTime(time);
        event.setVenue(venueRepository.findById(venueId).get());
        event.setDescription(description);
        eventRepository.save(event);
        return true;
    }

    @Override
    public Event findById(long id) {
        return eventRepository.findById(id);
    }
    
    @Override
    public Iterable<Event> findByNameContainingIgnoreCase(String name) {
        return eventRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public boolean add(String name, LocalDate date, LocalTime time, long venueId, String description) {
        if (name.isEmpty() || venueRepository.findById(venueId).isEmpty()) {
            return false;
        }
        Event event = new Event();
        event.setName(name);
        event.setDate(date);
        event.setTime(time);
        event.setVenue(venueRepository.findById(venueId).get());
        event.setDescription(description);
        eventRepository.save(event);
        return true;
    }


}

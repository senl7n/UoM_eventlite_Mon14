package uk.ac.man.cs.eventlite.dao;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    public long count() {
        return eventRepository.count();
	}

    @Override
	public Iterable<Event> findAll() {
		return eventRepository.findAll(Sort.by("date").and(Sort.by("time")));
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
        if (eventRepository.findById(id) == null || venueRepository.findById(venueId).isEmpty()) {
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
    public Iterable<Event> findByName(String name) {
        return eventRepository.findByName(name);
    }

    @Override
    public boolean add(String name, LocalDate date, LocalTime time, long venueId, String description) {
        if (venueRepository.findById(venueId).isEmpty()) {
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

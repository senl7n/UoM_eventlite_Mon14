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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;


@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;
    
    private Event event;
    public long count() {
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
    public Iterable<Event> findPreviousEvents() {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        List<Event> previousEvents = (List<Event>) eventRepository.findByDateBeforeOrDateEqualsAndTimeBeforeOrderByDateDescTimeDesc(currentDate, currentDate, currentTime);
        previousEvents.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getName).thenComparing(Event::getTime));
        return previousEvents;
        
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

//	@Override
//	public Iterable<Venue> findTop3Venues() {
//		int numVenues = 3;
//		
//		// getting all instances of each venue
//		Iterable<Event> events = eventRepository.findAll();
//		
//		// map each venue to its frequency
//		Map<Venue, Integer> mapOfVenues = new HashMap<>();
//		for(Event event: events) {
//			mapOfVenues.put(event.getVenue(), mapOfVenues.getOrDefault(event.getVenue(),0)+1);
//		}
//		
//		// keep track of x most significant entries
//		PriorityQueue<Map.Entry<Venue, Integer>> maxHeap =
//				new PriorityQueue<>((v,i)->(i.getValue()-v.getValue()));
//		for(Map.Entry<Venue, Integer> entry: mapOfVenues.entrySet()) {
//			maxHeap.add(entry);
//		}
//		
//		// get the top 3 venues
//		List<Venue> venues = new ArrayList<>();
//		while(venues.size() < numVenues) {
//			Map.Entry<Venue, Integer> entry = maxHeap.poll();
//			venues.add(entry.getKey());
//		}
//		
//		return venues;
//	}


}

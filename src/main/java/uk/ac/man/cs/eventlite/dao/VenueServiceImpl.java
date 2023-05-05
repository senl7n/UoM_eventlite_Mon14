package uk.ac.man.cs.eventlite.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.*;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventService EventService;

    public long count() {
        return venueRepository.count();
	}


    @Override
   	public Iterable<Venue> findAll() {
   		return venueRepository.findAll((Sort.by("name")));
   	}

    @Override
    public Venue save(Venue venue) {
        return venueRepository.save(venue);
    }

    @Override
    public Optional<Venue> findById(long id) {
        return venueRepository.findById(id);
    }

    public void deleteById(long id) {
        venueRepository.deleteById(id);
    }

    @Override
    public boolean update(long id, String name, int capacity, String address, String postcode) {
        Optional<Venue> venue = venueRepository.findById(id);
        if (venue.isPresent()) {
            venue.get().setName(name);
            venue.get().setCapacity(capacity);
            venue.get().setAddress(address);
            venue.get().setPostcode(postcode);
            venueRepository.save(venue.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean add(String name, int capacity, String address, String postcode) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setCapacity(capacity);
        venue.setAddress(address);
        venue.setPostcode(postcode);
        venueRepository.save(venue);
        return true;
    }

    @Override
    public Iterable<Venue> findByNameContainingIgnoreCase(String name){
    	return venueRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public boolean checkVenueOccupied(long venueId) {
        Iterable<Event> events = EventService.findAll();
        for (Event e : events) {
            if (e.getVenue().getId() == venueId) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterable<Venue> findPopular3Venues() {
        ArrayList<Venue> popular3Venues = new ArrayList<>();
        ArrayList<Integer> numOfEvents = new ArrayList<>();
        ArrayList<Venue> venues = (ArrayList<Venue>) venueRepository.findAll();
        long max_id = venues.get(venues.size() - 1).getId();
        for (long i = 0; i < max_id; i++) {
            numOfEvents.add(0);
        }
        for (Event event : eventRepository.findAll()) {
            if (event.getVenue() != null) {
                int index = (int) event.getVenue().getId() - 1;
                numOfEvents.set(index, numOfEvents.get(index) + 1);
            }
        }

        Comparator<Venue> venueComparator = (v1, v2) -> numOfEvents.get((int) v2.getId() - 1) - numOfEvents.get((int) v1.getId() - 1);
        PriorityQueue<Venue> venueQueue = new PriorityQueue<>(venueComparator);

        for (Venue venue : venues) {
            int index = (int) venue.getId() - 1;
            if (numOfEvents.get(index) > 0) {
                venueQueue.offer(venue);
            }
        }

        for (int i = 0; i < 3 && !venueQueue.isEmpty(); i++) {
            popular3Venues.add(venueQueue.poll());
        }
        return (Iterable<Venue>) popular3Venues;
    }

}

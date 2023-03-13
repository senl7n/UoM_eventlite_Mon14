package uk.ac.man.cs.eventlite.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Optional;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Autowired
    private VenueRepository VenueRepository;

    @Autowired
    private EventService EventService;

    public long count() {
        return VenueRepository.count();
	}


    @Override
   	public Iterable<Venue> findAll() {
   		return VenueRepository.findAll((Sort.by("name")));
   	}


    @Override
    public Venue save(Venue venue) {
        return VenueRepository.save(venue);
    }

    @Override
    public Optional<Venue> findById(long id) {
        return VenueRepository.findById(id);
    }

    public void deleteById(long id) {
        VenueRepository.deleteById(id);
    }

    @Override
    public boolean update(long id, String name, int capacity, String address, String postcode) {
        Optional<Venue> venue = VenueRepository.findById(id);
        if (venue.isPresent()) {
            venue.get().setName(name);
            venue.get().setCapacity(capacity);
            venue.get().setAddress(address);
            venue.get().setPostcode(postcode);
            VenueRepository.save(venue.get());
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
        VenueRepository.save(venue);
        return true;
    }
    
    @Override
    public Iterable<Venue> findByNameContainingIgnoreCase(String name){
    	return VenueRepository.findByNameContainingIgnoreCase(name);
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


}

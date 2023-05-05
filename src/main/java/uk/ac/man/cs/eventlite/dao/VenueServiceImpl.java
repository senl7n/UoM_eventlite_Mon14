package uk.ac.man.cs.eventlite.dao;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    //get the geolocation of the venue
    @Override
    public void getGeoLocation(Venue venue) {
        if (venue.getAddress() == null || venue.getPostcode() == null) {
            return;
        }
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken("pk.eyJ1IjoiN3Nlbmxpbi1taWFvIiwiYSI6ImNsZjhnOTBnNTBncm4zc252anM4ZHhmYmEifQ.8Xkazn-qXfFkT0yk_SDb8g")
                .query(venue.getAddress() + ", " + venue.getPostcode())
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                List<CarmenFeature> results = response.body().features();
                if (results.size() > 0) {
                    CarmenFeature feature = results.get(0);
                    Point point = feature.center();
                    venue.setLongitude(point.longitude());
                    venue.setLatitude(point.latitude());
                } else {
                    log.debug("No results found");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                log.debug("Geocoding Failure: " + throwable.getMessage());
            }
        });
    }
}

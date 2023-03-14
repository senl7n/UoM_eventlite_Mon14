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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VenueServiceImpl implements VenueService {

	private final static Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

	private final static String DATA = "data/venues.json";

	@Autowired
    private VenueRepository VenueRepository;

    @Autowired
    private EventRepository EventRepository;

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


    @Override
    public Iterable<Venue> findPopular3Venues() {
        ArrayList<Venue> popular3Venues = new ArrayList<>();
        ArrayList<Integer> numOfEvents = new ArrayList<>();
        ArrayList<Venue> venues = (ArrayList<Venue>) VenueRepository.findAll();
        long max_id = venues.get(venues.size() - 1).getId();
        for (long i = 0; i < max_id; i++) {
            numOfEvents.add(0);
        }
        for (Event event : EventRepository.findAll()) {
            if (event.getVenue() != null) {
                int index = (int) event.getVenue().getId() - 1;
                numOfEvents.set(index, numOfEvents.get(index) + 1);
            }
        }
        for (int i = 0; i < 3; i++) {
            int max = 0;
            int maxIndex = 0;
            for (int j = 0; j < numOfEvents.size(); j++) {
                if (numOfEvents.get(j) > max) {
                    max = numOfEvents.get(j);
                    maxIndex = j;
                }
            }
            popular3Venues.add(VenueRepository.findById((long) maxIndex + 1).get());
            numOfEvents.set(maxIndex, 0);
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

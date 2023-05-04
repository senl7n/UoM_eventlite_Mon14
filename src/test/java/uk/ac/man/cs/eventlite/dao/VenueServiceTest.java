package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService EventServiceImpl;

    @Autowired
    private VenueService VenueServiceImpl;
	
	@Test
    public void testLongitudeAndLatitude() {
        
        Venue venue = new Venue();
        double expectedLongitude = 45.1234;
        double expectedLatitude = 43.8989;
        venue.setLongitude(expectedLongitude);
        venue.setLatitude(expectedLatitude);
        double actualLongitude = venue.getLongitude();
        double actualLatitude = venue.getLatitude();
        assertEquals(expectedLongitude, actualLongitude, 1e-9, "The set and retrieved longitude values should be equal.");
        assertEquals(expectedLatitude, actualLatitude, 1e-9, "The set and retrieved latitude values should be equal.");
    }

	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFindAll() {
		// Retrieve all events using the findAll() method
		Iterable<Venue> allVenues = VenueServiceImpl.findAll();
		int venueCount = 0;

		// Iterate through all events and increment the counter for each event
		for (Venue venue : allVenues) {
			venueCount++;
		}

		// Compare the event count with the expected value
		assertEquals(3, venueCount, "Expected to find 3 venues in the repository");
	}

	@Test
	public void testFindByNameContainingIgnoreCase() {
		Iterable<Venue> foundVenues = VenueServiceImpl.findByNameContainingIgnoreCase("red");

//      Iterate through the foundEvents and make sure that all of them contain the search keyword
		for (Venue venue : foundVenues) {
			assertTrue(venue.getName().toLowerCase().contains("red".toLowerCase()));
		}
	}

	@Test
	public void testFindByID() {
		long expectedId = 1;
		Optional<Venue> foundVenue = VenueServiceImpl.findById(expectedId);

//      Iterate through the foundEvents and make sure that all of them contain the search keyword

		assertTrue(foundVenue.isPresent(), "Expected venue not found");
		assertEquals(expectedId, foundVenue.get().getId(), "Found venue has a different ID than expected");
	}
}

package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void testDelete() {
        // create a new venue and check the venues amount before and after deleting
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        venue.setAddress("Test Address");
        venue.setPostcode("Test Postcode");
        VenueServiceImpl.save(venue);
        long venueCountBefore = VenueServiceImpl.count();
        VenueServiceImpl.deleteById(venue.getId());
        long venueCountAfter = VenueServiceImpl.count();
        assertEquals(venueCountBefore - 1, venueCountAfter);
    }

    @Test
    public void testUpdateVenueWithValidId() {
        // create a new venue
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        venue.setAddress("123 Main Street");
        venue.setPostcode("12345");
        VenueServiceImpl.save(venue);

        // update the venue
        boolean result = VenueServiceImpl.update(venue.getId(), "New Venue Name", 200, "456 Second Street", "67890");

        // check if the update was successful
        Assertions.assertTrue(result);

        // check if the venue was updated correctly
        Venue updatedVenue = VenueServiceImpl.findById(venue.getId()).get();
        Assertions.assertEquals("New Venue Name", updatedVenue.getName());
        Assertions.assertEquals(200, updatedVenue.getCapacity());
        Assertions.assertEquals("456 Second Street", updatedVenue.getAddress());
        Assertions.assertEquals("67890", updatedVenue.getPostcode());
    }

    @Test
    public void testUpdateVenueWithInvalidId() {
        // update a venue with an invalid ID
        boolean result = VenueServiceImpl.update(-1L, "New Venue Name", 200, "456 Second Street", "67890");

        // check if the update failed
        Assertions.assertFalse(result);
    }

    @Test
    public void testAdd() {
        String name = "Test Venue";
        int capacity = 100;
        String address = "123 Test Street";
        String postcode = "ABC123";
        boolean added = VenueServiceImpl.add(name, capacity, address, postcode);
        assertTrue(added);

        Venue newVenue = VenueServiceImpl.findByNameContainingIgnoreCase(name).iterator().next();
        assertEquals(name, newVenue.getName());
        assertEquals(capacity, newVenue.getCapacity());
        assertEquals(address, newVenue.getAddress());
        assertEquals(postcode, newVenue.getPostcode());
    }

    @Test
    public void testCheckVenueOccupied() {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        venue.setAddress("123 Main Street");
        venue.setPostcode("12345");
        VenueServiceImpl.save(venue);

        Event event = new Event();
        event.setName("Test Event");
        event.setDate(LocalDate.parse("2077-03-01"));
        event.setTime(LocalTime.parse("12:00:00"));
        event.setVenue(venue);
        EventServiceImpl.save(event);

        boolean occupied = VenueServiceImpl.checkVenueOccupied(venue.getId());
        Assertions.assertFalse(occupied);
    }

    @Test
    public void testCheckVenueNotOccupied() {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        venue.setAddress("123 Main Street");
        venue.setPostcode("12345");
        VenueServiceImpl.save(venue);

        boolean occupied = VenueServiceImpl.checkVenueOccupied(venue.getId());
        Assertions.assertTrue(occupied);
    }


}


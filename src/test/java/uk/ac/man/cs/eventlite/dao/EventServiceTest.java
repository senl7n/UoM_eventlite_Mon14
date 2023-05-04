package uk.ac.man.cs.eventlite.dao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Optional;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private EventService EventServiceImpl;

    @Autowired
    private VenueService VenueServiceImpl;

    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdate() {
        Iterator<Event> events = EventServiceImpl.findByNameContainingIgnoreCase("COMP23412 Showcase 01").iterator();
        Event event = events.next();
        assertEquals("COMP23412 Showcase 01", event.getName());

        LocalDate oldDate = event.getDate();

        Event newEvent = new Event();
        newEvent.setId(event.getId());
        newEvent.setName("Test Update Name");
        newEvent.setDate(LocalDate.parse("2077-01-01"));
        newEvent.setTime(LocalTime.parse("12:00"));
        newEvent.setVenue(event.getVenue());
        newEvent.setDescription("Test Update Description");
        EventServiceImpl.save(newEvent);

        Event AlteredEvent = EventServiceImpl.findById(event.getId());

        assertEquals("Test Update Name", AlteredEvent.getName());
        assertEquals(event.getId(), AlteredEvent.getId());
        assertNotEquals(oldDate, AlteredEvent.getDate());
        assertEquals("Test Update Description", AlteredEvent.getDescription());
    }

    @Test
    public void testAdd() {
        Event event = new Event();
        event.setName("Test Add Name");
        event.setDate(LocalDate.parse("2077-01-01"));
        event.setTime(LocalTime.parse("12:00"));
        event.setVenue(new Venue());
        event.setDescription("Test Add Description");
        EventServiceImpl.save(event);

        Event newEvent = EventServiceImpl.findById(event.getId());

        assertEquals("Test Add Name", newEvent.getName());
        assertEquals(LocalDate.parse("2077-01-01"), newEvent.getDate());
        assertEquals(LocalTime.parse("12:00"), newEvent.getTime());
        assertEquals("Test Add Description", newEvent.getDescription());
        assertEquals(LocalDateTime.parse("2077-01-01T12:00"), newEvent.getDateTime());
    }

    @Test
    public void testDelete() {
        // create a new event and check the events amount before and after deleting
        long eventsAmountBefore;
        long eventsAmountAfter;
        Venue venue = VenueServiceImpl.findAll().iterator().next();
        Event event = new Event();
        event.setName("Test Delete Name");
        event.setDate(LocalDate.parse("2077-01-01"));
        event.setTime(LocalTime.parse("12:00"));
        event.setVenue(venue);
        event.setDescription("Test Delete Description");
        EventServiceImpl.save(event);
        eventsAmountBefore = EventServiceImpl.getNumberOfEvent();
        EventServiceImpl.deleteById(event.getId());
        eventsAmountAfter = EventServiceImpl.getNumberOfEvent();
        assertEquals(eventsAmountBefore - 1, eventsAmountAfter);
    }

}
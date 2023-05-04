package uk.ac.man.cs.eventlite.dao;

import org.junit.jupiter.api.Assertions;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

    @Test
    public void testFindUpcoming3Events() {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        VenueServiceImpl.save(venue);

        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("COMP23412 Showcase 01");
        event1.setDate(LocalDate.parse("2077-01-01"));
        event1.setTime(LocalTime.parse("12:00"));
        event1.setVenue(venue);
        EventServiceImpl.save(event1);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("COMP23412 Showcase 02");
        event2.setDate(LocalDate.parse("2077-01-02"));
        event2.setTime(LocalTime.parse("12:00"));
        event2.setVenue(venue);
        EventServiceImpl.save(event2);

        Event event3 = new Event();
        event3.setId(3L);
        event3.setName("COMP23412 Showcase 03");
        event3.setDate(LocalDate.parse("2077-01-03"));
        event3.setTime(LocalTime.parse("12:00"));
        event3.setVenue(venue);
        EventServiceImpl.save(event3);

        Event event4 = new Event();
        event4.setId(4L);
        event4.setName("COMP23412 Showcase 04");
        event4.setDate(LocalDate.parse("2077-01-04"));
        event4.setTime(LocalTime.parse("12:00"));
        event4.setVenue(venue);
        EventServiceImpl.save(event4);

        List<Event> expected = Arrays.asList(event1, event2, event3);
        Iterable<Event> actualEvents = EventServiceImpl.findUpcoming3Events();
        List<Event> actual = new ArrayList<>();
        for (Event event : actualEvents) {
            actual.add(event);
        }

        Assertions.assertEquals(expected.get(0).getName(), actual.get(0).getName());
    }

}
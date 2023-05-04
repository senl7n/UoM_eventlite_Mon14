package uk.ac.man.cs.eventlite.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void testUpdate() {
        Event event = new Event();
        event.setName("Test Update Name");
        event.setDate(LocalDate.parse("2077-01-01"));
        event.setTime(LocalTime.parse("12:00"));
        event.setVenue(new Venue());
        event.setDescription("Test Update Description");
        EventServiceImpl.save(event);

        Event newEvent = EventServiceImpl.findById(event.getId());
        boolean updateSuccess = EventServiceImpl.update(event.getId(), "New Name"
                , LocalDate.parse("2077-01-02")
                , LocalTime.parse("13:00")
                , 1L
                , "New Description");
        assertTrue(updateSuccess);

        Event updatedEvent = EventServiceImpl.findById(event.getId());
        assertEquals("New Name", updatedEvent.getName());
        assertEquals(LocalDate.parse("2077-01-02"), updatedEvent.getDate());
        assertEquals(LocalTime.parse("13:00"), updatedEvent.getTime());
        assertEquals("New Description", updatedEvent.getDescription());
        assertEquals(LocalDateTime.parse("2077-01-02T13:00"), updatedEvent.getDateTime());
    }

    @Test
    public void testUpdateError() {
        String name = "Test Update Name";
        LocalDate date = LocalDate.parse("2077-01-01");
        LocalTime time = LocalTime.parse("12:00");
        long venueId = 1L;
        String description = "Test Update Description";

        // Add an event first
        Event event = new Event();
        event.setName("Test Event");
        event.setDate(LocalDate.parse("2077-01-01"));
        event.setTime(LocalTime.parse("12:00"));
        event.setVenue(new Venue());
        event.setDescription("Test Event Description");
        EventServiceImpl.save(event);

        boolean updateSuccess = EventServiceImpl.update(event.getId() + 1, name, date, time, venueId, description);
        assertFalse(updateSuccess);
    }

    @Test
    public void testAdd() {
        String name = "Test Add Name";
        LocalDate date = LocalDate.parse("2077-01-01");
        LocalTime time = LocalTime.parse("12:00");
        long venueId = 1L;
        String description = "Test Add Description";
        boolean result = EventServiceImpl.add(name, date, time, venueId, description);
        assertTrue(result);

        Event newEvent = EventServiceImpl.findByNameContainingIgnoreCase("Test Add Name").iterator().next();

        assertEquals("Test Add Name", newEvent.getName());
        assertEquals(LocalDate.parse("2077-01-01"), newEvent.getDate());
        assertEquals(LocalTime.parse("12:00"), newEvent.getTime());
        assertEquals("Test Add Description", newEvent.getDescription());
    }

    @Test
    public void testAddError() {
        String name = "";
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        long venueId = 0L;
        String description = "";
        boolean result = EventServiceImpl.add(name, date, time, venueId, description);
        assertFalse(result);
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
        Iterable<Event> actualEvents = EventServiceImpl.findUpcoming3Events();
        List<Event> actual = new ArrayList<>();
        for (Event event : actualEvents) {
            actual.add(event);
        }

        Assertions.assertEquals(3, actual.size());
    }

    @Test
    public void testFindUpcomingLessThan3Events() {
        Iterable<Event> upcomingEvents = EventServiceImpl.findUpcomingEvents();
        long numOfUpcomingEvents = upcomingEvents.spliterator().getExactSizeIfKnown();
        long deleteNeed = numOfUpcomingEvents - 2;
        long counter = 0;
        for (Event event : upcomingEvents) {
            if (counter >= deleteNeed) {
                break;
            }
            EventServiceImpl.deleteById(event.getId());
            counter++;
        }
        Iterable<Event> actualEvents = EventServiceImpl.findUpcoming3Events();
        List<Event> actual = new ArrayList<>();
        for (Event event : actualEvents) {
            actual.add(event);
        }
        Assertions.assertEquals(numOfUpcomingEvents - deleteNeed, actual.size());
    }

    @Test
    public void testFindUpcomingEvents() {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        VenueServiceImpl.save(venue);

        Event event1 = new Event();
        event1.setName("COMP23412 Showcase 01");
        event1.setDate(LocalDate.parse("2017-01-01"));
        event1.setTime(LocalTime.parse("12:00"));
        event1.setVenue(venue);
        EventServiceImpl.save(event1);

        Event event2 = new Event();
        event2.setName("COMP23412 Showcase 02");
        event2.setDate(LocalDate.parse("2077-01-02"));
        event2.setTime(LocalTime.parse("12:00"));
        event2.setVenue(venue);
        EventServiceImpl.save(event2);

        Event event3 = new Event();
        event3.setName("COMP23412 Showcase 03");
        event3.setDate(LocalDate.parse("2077-01-03"));
        event3.setTime(LocalTime.parse("12:00"));
        event3.setVenue(venue);
        EventServiceImpl.save(event3);

        List<Event> expected = Arrays.asList(event2, event3);
        Iterable<Event> actualEvents = EventServiceImpl.findUpcomingEvents();
        List<Event> actual = new ArrayList<>();
        for (Event event : actualEvents) {
            actual.add(event);
        }

        Assertions.assertEquals(expected.size() + 3, actual.size());
        Assertions.assertFalse(actual.contains(event1));
        Assertions.assertTrue(actual.contains(event2));
    }


    @Test
    public void testFindPreviousEvents() {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        VenueServiceImpl.save(venue);

        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("COMP23412 Showcase 01");
        event1.setDate(LocalDate.parse("2007-01-01"));
        event1.setTime(LocalTime.parse("12:00"));
        event1.setVenue(venue);
        EventServiceImpl.save(event1);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("COMP23412 Showcase 02");
        event2.setDate(LocalDate.parse("2017-01-02"));
        event2.setTime(LocalTime.parse("12:00"));
        event2.setVenue(venue);
        EventServiceImpl.save(event2);

        Event event3 = new Event();
        event3.setId(3L);
        event3.setName("COMP23412 Showcase 03");
        event3.setDate(LocalDate.parse("2027-01-03"));
        event3.setTime(LocalTime.parse("12:00"));
        event3.setVenue(venue);
        EventServiceImpl.save(event3);

        List<Event> expected = Arrays.asList(event1, event2);
        Iterable<Event> actualEvents = EventServiceImpl.findPreviousEvents();
        List<Event> actual = new ArrayList<>();
        for (Event event : actualEvents) {
            actual.add(event);
        }

        Assertions.assertEquals(expected.get(0).getName(), actual.get(0).getName());
    }

    @Test
    public void testFindAll() {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setCapacity(100);
        VenueServiceImpl.save(venue);

        Event event1 = new Event();
        event1.setId(1L);
        event1.setName("COMP23412 Showcase 01");
        event1.setDate(LocalDate.parse("2007-01-01"));
        event1.setTime(LocalTime.parse("12:00"));
        event1.setVenue(venue);
        EventServiceImpl.save(event1);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setName("COMP23412 Showcase 02");
        event2.setDate(LocalDate.parse("2017-01-02"));
        event2.setTime(LocalTime.parse("12:00"));
        event2.setVenue(venue);
        EventServiceImpl.save(event2);

        Event event3 = new Event();
        event3.setId(3L);
        event3.setName("COMP23412 Showcase 03");
        event3.setDate(LocalDate.parse("2027-01-03"));
        event3.setTime(LocalTime.parse("12:00"));
        event3.setVenue(venue);
        EventServiceImpl.save(event3);

        List<Event> expected = Arrays.asList(event1, event2, event3);
        Iterable<Event> actualEvents = EventServiceImpl.findAll();
        List<Event> actual = new ArrayList<>();
        for (Event event : actualEvents) {
            actual.add(event);
        }
    }

    @Test
    public void testSetDescribtion () {
        Event event = new Event();
        String expected_Describtion = "Hello this is a test";
        event.setDescription("Hello this is a test");
        String actual_Describtion = event.getDescription();
        assertEquals(expected_Describtion, actual_Describtion);
        Assertions.assertEquals(expected_Describtion, actual_Describtion);
    }

}

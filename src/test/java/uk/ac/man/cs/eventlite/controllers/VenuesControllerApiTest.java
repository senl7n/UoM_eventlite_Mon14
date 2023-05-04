package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class })
public class VenuesControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    // Helper method to create a new event
    private Event createEvent(long id, String name, LocalDate date, LocalTime time, Venue venue) {
        Event event = new Event();
        event.setId(id);
        event.setName(name);
        event.setDate(date);
        event.setTime(time);
        event.setVenue(venue);
        return event;
    }

    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

        verify(venueService).findAll();
    }

    @Test
    public void getIndexWithVenues() throws Exception {
        Venue venue = new Venue();
        venue.setName("Venue");
        venue.setCapacity(200);
        when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
                .andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));

        verify(venueService).findAll();
    }


    @Test
    public void getVenueNotFound() throws Exception {
        mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("venue 99")))
                .andExpect(jsonPath("$.id", equalTo((99)))).andExpect(handler().methodName("getVenue"));
    }

    @Test
    public void getVenueFound() throws Exception {
        Venue venue = new Venue();
        venue.setId(10);
        venue.setName("Test Venue");
        venue.setCapacity(100);
        venue.setAddress("Test Address");
        venue.setPostcode("TE5 7PC");

        when(venueService.findById(10)).thenReturn(Optional.of(venue));

        mvc.perform(get("/api/venues/10").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", equalTo("Test Venue")))
                .andExpect(jsonPath("$.capacity", equalTo(100)))
                .andExpect(jsonPath("$.address", equalTo("Test Address")))
                .andExpect(jsonPath("$.postcode", equalTo("TE5 7PC")));

        verify(venueService).findById(10);
    }

    @Test
    public void getVenueEventsFound() throws Exception {
        Venue venue = new Venue();
        venue.setId(15);
        venue.setName("Test Venue");
        venue.setCapacity(200);
        venue.setAddress("Test Address");
        venue.setPostcode("TE5 7PC");

        Event event = createEvent(20, "Test Event", LocalDate.parse("2024-01-01"), LocalTime.parse("12:00:00"),  venue);

        when(venueService.findById(15)).thenReturn(Optional.of(venue));
        when(eventService.findUpcomingEvents()).thenReturn(Collections.singletonList(event));

        mvc.perform(get("/api/venues/15/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
                .andExpect(jsonPath("$._embedded.events[0].name", equalTo("Test Event")))
                .andExpect(jsonPath("$._embedded.events[0].date", equalTo("2024-01-01")))
                .andExpect(jsonPath("$._embedded.events[0].time", equalTo("12:00:00")));

        verify(venueService).findById(15);
        verify(eventService).findUpcomingEvents();
    }

    @Test
    public void getVenueNext3EventsFound() throws Exception {
        Venue v = new Venue();
        v.setId(1L);
        v.setName("Venue");
        v.setCapacity(200);
        v.setAddress("Address");
        v.setPostcode("Postcode");
        when(venueService.findById(1L)).thenReturn(java.util.Optional.of(v));

        List<Event> allEvents = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Event e = new Event();
            e.setId((long) i);
            e.setName("Event" + i);
            e.setDate(LocalDate.now().plusDays(i));
            e.setTime(LocalTime.now());
            e.setVenue(v);
            allEvents.add(e);
        }

        when(eventService.findUpcomingEvents()).thenReturn(allEvents);

        mvc.perform(get("/api/venues/1/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.events.length()", equalTo(3)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues/1/next3events")));

        verify(venueService).findById(1L);
        verify(eventService).findUpcomingEvents();
    }


}

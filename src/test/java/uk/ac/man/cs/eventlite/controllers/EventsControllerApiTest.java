package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class })
@AutoConfigureMockMvc
public class EventsControllerApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;

    @Test
    public void getIndexWhenNoEvents() throws Exception {
        when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

        mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

        verify(eventService).findAll();
    }

    @Test
    public void getIndexWithEvents() throws Exception {
        Venue v = new Venue();
        v.setName("Venue");
        v.setCapacity(200);
        venueService.save(v);
        Event e = new Event();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

        mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
                .andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

        verify(eventService).findAll();
    }

    @Test
    public void getEventNotFound() throws Exception {
        mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
                .andExpect(handler().methodName("getEvent"));
    }

    @Test
    public void getEventFound() throws Exception {
        Venue v = new Venue();
        v.setId(15);
        v.setName("Venue");
        v.setCapacity(200);
        v.setAddress("Address");
        v.setPostcode("Postcode");
        venueService.save(v);
        Event e = new Event();
        e.setId(10);
        e.setName("Event");
        e.setDate(LocalDate.parse("2024-01-01"));
        e.setTime(LocalTime.parse("12:00:00"));
        e.setDescription("Description");
        e.setVenue(v);
        eventService.save(e);
        when(eventService.findById(10)).thenReturn(e);
        when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

        mvc.perform(get("/api/events/10").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getEvent"))
                .andExpect(jsonPath("$.name", equalTo("Event")))
                .andExpect(jsonPath("$.date", equalTo("2024-01-01")))
                .andExpect(jsonPath("$.time", equalTo("12:00:00")))
                .andExpect(jsonPath("$.description", equalTo("Description")));
    }

    @Test
    public void getEventVenueFound() throws Exception {
        Venue v = new Venue();
        v.setId(99);
        v.setName("Venue");
        v.setCapacity(200);
        v.setAddress("Address");
        v.setPostcode("Postcode");
        venueService.save(v);
        Event e = new Event();
        e.setId(99);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        eventService.save(e);
        when(eventService.findById(99)).thenReturn(e);
        when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

        mvc.perform(get("/api/events/99/venue").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getEventVenue"))
                .andExpect(jsonPath("$.name", equalTo("Venue")))
                .andExpect(jsonPath("$.capacity", equalTo(200)))
                .andExpect(jsonPath("$.address", equalTo("Address")))
                .andExpect(jsonPath("$.postcode", equalTo("Postcode")));

        verify(eventService).findById(99);
    }

}

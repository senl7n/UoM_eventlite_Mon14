package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

	}

	@Test
	public void getSearchResultPageMessageTest() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events/searchResult").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/searchResult")).andExpect(handler().methodName("getSearchResultPageMessage"));

		verify(eventService).findUpcomingEvents();
		verify(eventService).findPreviousEvents();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
	}

	@Test
	public void searchForExistingEvent() throws Exception {
		Venue venue = new Venue();
		venue.setName("Example Venue");
		venue.setCapacity(100);
		venue.setAddress("123 Example Street");
		venue.setPostcode("EX1 2PL");

		Event testEvent = new Event();
		testEvent.setName("Eating");

		// Set a date for the mock event
		LocalDate eventDate = LocalDate.of(2024, 6, 01);
		testEvent.setDate(eventDate);
		testEvent.setVenue(venue);

		List<Event> events = Collections.singletonList(testEvent);

		when(eventService.findByNameContainingIgnoreCase("eat")).thenReturn(events);

		mvc.perform(get("/events/search?q=eat").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/searchResult"))
				.andExpect(handler().methodName("search"))
				.andExpect(model().attribute("found", true))
				.andExpect(model().attribute("searchMessage", "EVENT CONTAINING 'eat' FOUND"))
				.andExpect(model().attribute("upcomingEvents", events));
		verify(eventService).findByNameContainingIgnoreCase("eat");
	}

	@Test
	public void searchForUnrealEvent() throws Exception {
		Venue venue = new Venue();
		venue.setName("Example Venue");
		venue.setCapacity(100);
		venue.setAddress("123 Example Street");
		venue.setPostcode("EX1 2PL");

		Event testEvent = new Event();
		testEvent.setName("Football");

		// Set a date for the mock event
		LocalDate eventDate = LocalDate.of(2024, 6, 01);
		testEvent.setDate(eventDate);
		testEvent.setVenue(venue);

		List<Event> events = Collections.singletonList(testEvent);

		when(eventService.findByNameContainingIgnoreCase("basketball")).thenReturn(Collections.emptyList());

		mvc.perform(get("/events/search?q=basketball").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/searchResult"))
				.andExpect(handler().methodName("search"))
				.andExpect(model().attribute("found", false))
				.andExpect(model().attribute("searchMessage", "EVENT CONTAINING 'basketball' NOT FOUND, HERE IS ALL THE EVENTS WE HAVE"));
		verify(eventService).findByNameContainingIgnoreCase("basketball");
	}

	@Test
	public void searchWithEmptyQuery() throws Exception {
		mvc.perform(get("/events/search?q=").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/searchResult"))
				.andExpect(handler().methodName("search"))
				.andExpect(model().attribute("found", false));
	}

	@Test
	public void searchWithNullQuery() throws Exception {
		mvc.perform(get("/events/search").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/searchResult"))
				.andExpect(handler().methodName("search"))
				.andExpect(model().attribute("found", false));
	}

	@Test
	public void testEventTimeHandlingInSearch() throws Exception {
		Venue venue = new Venue();
		venue.setName("Example Venue");
		venue.setCapacity(100);
		venue.setAddress("123 Example Street");
		venue.setPostcode("EX1 2PL");

		Event eventWithTime = new Event();
		eventWithTime.setName("Upcoming Event With Time");
		eventWithTime.setDate(LocalDate.now().plusDays(1));
		eventWithTime.setTime(LocalTime.now());
		eventWithTime.setVenue(venue);

		Event eventWithoutTime = new Event();
		eventWithoutTime.setName("Upcoming Event Without Time");
		eventWithoutTime.setDate(LocalDate.now().plusDays(2));
		eventWithoutTime.setVenue(venue);

		Event pastEventWithTime = new Event();
		pastEventWithTime.setName("Past Event With Time");
		pastEventWithTime.setDate(LocalDate.now().minusDays(1));
		pastEventWithTime.setTime(LocalTime.now());
		pastEventWithTime.setVenue(venue);

		Event pastEventWithoutTime = new Event();
		pastEventWithoutTime.setName("Past Event Without Time");
		pastEventWithoutTime.setDate(LocalDate.now().minusDays(2));
		pastEventWithoutTime.setVenue(venue);

		List<Event> events = Arrays.asList(eventWithTime, eventWithoutTime, pastEventWithTime, pastEventWithoutTime);
		when(eventService.findByNameContainingIgnoreCase("event")).thenReturn(events);

		mvc.perform(get("/events/search?q=event").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(view().name("events/searchResult"))
				.andExpect(handler().methodName("search"))
				.andExpect(model().attribute("found", true))
				.andExpect(model().attribute("searchMessage", "EVENT CONTAINING 'event' FOUND"))
				.andExpect(model().attribute("upcomingEvents", hasSize(2)))
				.andExpect(model().attribute("upcomingEvents", contains(eventWithTime, eventWithoutTime)))
				.andExpect(model().attribute("previousEvents", hasSize(2)))
				.andExpect(model().attribute("previousEvents", contains(pastEventWithoutTime, pastEventWithTime)));
	}


}

package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
public class VenuesControllerTest {

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
	public void getVenueNotFound() throws Exception{
		mvc.perform(get("/venues/99").accept(MediaType.TEXT_HTML))
			.andExpect(status().isNotFound())
			.andExpect(view().name("venues/not_found"))
			.andExpect(handler().methodName("getVenue"));
			// TODO this has more but don't know how to do
	}
	
	@Test
	public void searchVenueBynameWhenNameIsEmpty() throws Exception{
		when(venueService.findByNameContainingIgnoreCase("")).thenReturn(Collections.<Venue>emptyList());
		
		mvc.perform(get("/venues/searchResult? q= ").accept(MediaType.TEXT_HTML))
			.andExpect(status().isOk())
			.andExpect(view().name("venues/searchResult"));
//			.andExpect(model().attribute("allVenues", Collections.<Venue>emptyList()));
		
		verify(venueService).findByNameContainingIgnoreCase("");
	}
	
//	@Test
//	public void searchVenueByNameWhenNameIsNotEmpty() throws Exception{
//		Venue venue = new Venue();
//		venue.setName("Example Venue");
//		venue.setCapacity(100);
//		venue.setAddress("123 Example Street");
//		venue.setPostcode("EX1 2PL");
//
//		Event testEvent = new Event();
//		testEvent.setName("Football");
//
//		// Set a date for the mock event
//		LocalDate eventDate = LocalDate.of(2024, 6, 01);
//		testEvent.setDate(eventDate);
//		testEvent.setVenue(venue);
//
//		List<Event> events = Collections.singletonList(testEvent);
//
//		when(venueService.findByNameContainingIgnoreCase("lallala")).thenReturn(Collections.emptyList());
//
//		mvc.perform(get("/venues/search?q=lallala").accept(MediaType.TEXT_HTML))
//				.andExpect(status().isOk())
//				.andExpect(view().name("venues/searchResult"))
//				.andExpect(handler().methodName("search"))
//				.andExpect(model().attribute("found", false))
//				.andExpect(model().attribute("searchMessage", "VENUE CONTAINING 'basketball' NOT FOUND, HERE IS ALL THE VENUES WE HAVE"));
//	}


}

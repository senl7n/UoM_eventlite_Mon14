package uk.ac.man.cs.eventlite.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import uk.ac.man.cs.eventlite.config.Security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.springframework.http.MediaType;






import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesController.class)
@Import(Security.class)

public class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VenueService venueService;

    @MockBean
    private EventService eventService;

    private Venue venue;
    private List<Venue> venues;

    @BeforeEach
    public void setup() {
        venue = new Venue();
        venue.setId(1);
        venue.setName("Test Venue");
        venue.setAddress("123 Test Road");
        venue.setPostcode("TE1 1ST");
        venue.setCapacity(100);

        venues = new ArrayList<>();
        venues.add(venue);

        when(venueService.findAll()).thenReturn(venues);
        when(venueService.findById(1L)).thenReturn(Optional.of(venue));
        when(venueService.findById(anyLong())).thenReturn(Optional.empty());
        when(eventService.findUpcomingEvents()).thenReturn(new ArrayList<>());
    }

    @Test
    public void testGetAllVenues() throws Exception {
        mockMvc.perform(get("/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/index"))
                .andExpect(model().attributeExists("venues"));
    }
    
    @Test
	public void testGetVenue() throws Exception {
	    mockMvc.perform(get("/venues/{id}", 1L))
	            .andExpect(status().isNotFound())
	            .andExpect(view().name("venues/not_found"))
	            .andExpect(model().attribute("not_found_id", 1L));
	
	    verify(venueService, times(0)).findById(anyLong());
	    verifyNoMoreInteractions(venueService);
	    verifyNoInteractions(eventService);
	}
	
//    @Test
//    public void testDeleteVenueOccupied() throws Exception {
//        when(venueService.checkVenueOccupied(1L)).thenReturn(false);
//
//        mockMvc.perform(delete("/venues/{id}", 1L))
//        		.andExpect(status().isForbidden())
//                .andExpect(redirectedUrl("/venues/description?id=1L&error=1"));
//    }
//
//    @Test
//    public void testDeleteVenueNotOccupied() throws Exception {
//        when(venueService.checkVenueOccupied(1L)).thenReturn(true);
//
//        mockMvc.perform(delete("/venues/{id}", 1L))
//		.andExpect(status().isForbidden())
//		.andExpect(redirectedUrl("/venues"));
//    }
    
    @Test
    public void testEditPageWithErrors() throws Exception {
        // Assuming the venue with id 1 exists
        long venueId = 1;

        // Mock the venueService.findById method to return a sample Venue
        Venue testVenue = new Venue();
        testVenue.setId(venueId);
        testVenue.setName("Test Venue");
        testVenue.setAddress("Test Road");
        testVenue.setPostcode("M15 4UH");
        testVenue.setCapacity(100);

        when(venueService.findById(venueId)).thenReturn(Optional.of(testVenue));

        // Perform a request with an invalid capacity (non-numeric value)
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "1")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Invalid capacity."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "2")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a valid name."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "3")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a valid address."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "4")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a valid postcode."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "5")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a positive capacity."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "6")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please include a road name."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", ""))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("Rob").roles(Security.ADMIN_ROLE))
                .param("error", "unknown_error")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Unknown error."))
                .andExpect(view().name("venues/edit"));

        
    }



}
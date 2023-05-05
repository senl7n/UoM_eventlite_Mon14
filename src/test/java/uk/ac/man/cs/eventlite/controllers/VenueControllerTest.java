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
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

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
import static org.hamcrest.Matchers.samePropertyValuesAs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;

import org.springframework.http.MediaType;






import java.util.ArrayList;
import java.util.Collections;
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
	

    
    @Test
    public void testDeleteVenueWhenOccupied() throws Exception {
        long venueId = 1;

        // Mock the venueService.checkVenueOccupied method to return false
        when(venueService.checkVenueOccupied(venueId)).thenReturn(false);

        // Perform a delete request with the occupied venue ID
        mockMvc.perform(delete("/venues/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/description?id=" + venueId + "&error=1"));
        
        // Verify the venueService.deleteById method was not called
        verify(venueService, never()).deleteById(venueId);
    }
    
    @Test
    public void testDeleteVenueWhenNotOccupied() throws Exception {
        long venueId = 1;

        // Mock the venueService.checkVenueOccupied method to return true
        when(venueService.checkVenueOccupied(venueId)).thenReturn(true);

        // Perform a delete request with the not occupied venue ID
        mockMvc.perform(delete("/venues/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));

        // Verify the venueService.deleteById method was called with the correct ID
        verify(venueService).deleteById(venueId);
    }



    
    @Test
    public void testEditPageWithErrors() throws Exception {
        // Assuming the venue with id 1 exists
        long venueId = 1;

        // Mock the venueService.findById method to return a sample Venue
        Venue testVenue = new Venue();
        testVenue.setId(venueId);
        testVenue.setName("Test Venue");
        testVenue.setAddress("Test Road");
        testVenue.setPostcode("TE5 4ST");
        testVenue.setCapacity(100);

        when(venueService.findById(venueId)).thenReturn(Optional.of(testVenue));

        // Perform a request with an invalid capacity (non-numeric value)
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "1")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Invalid capacity."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "2")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a valid name."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "3")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a valid address."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "4")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a valid postcode."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "5")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please enter a positive capacity."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "6")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Please include a road name."))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", ""))
                .andExpect(view().name("venues/edit"));
        
        mockMvc.perform(get("/venues/edit/{id}", venueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("error", "unknown_error")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Unknown error."))
                .andExpect(view().name("venues/edit"));

        
    }
    
    @Test
    public void testEditPageWithNonExistentVenue() throws Exception {
        // Set an ID for a venue that does not exist
        long nonExistentVenueId = 999;

        // Mock the venueService.findById method to return an empty Optional
        when(venueService.findById(nonExistentVenueId)).thenReturn(Optional.empty());

        // Perform a request with the non-existent venue ID and expect an exception
        mockMvc.perform(get("/venues/edit/{id}", nonExistentVenueId)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof VenueNotFoundException));
    }

    @Test
    public void testEditVenueWithValidInput() throws Exception {
        long id = 1;
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit Road")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit st")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit street")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit rd")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit avenue")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit ave")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
    }

    @Test
    public void testEditVenueWithInvalidInput() throws Exception {
        long id = 1;
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "edit Venue")
                .param("address", "edit Road")
                .param("postcode", "TE5 4ST")
                .param("capacity", "invalid")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=1"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "")
                .param("address", "edit Road")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=2"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "test")
                .param("address", "")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=3"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "test")
                .param("address", "edit Road")
                .param("postcode", "")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=4"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "test")
                .param("address", "edit Road")
                .param("postcode", "TE5 4ST")
                .param("capacity", "0")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=5"));
        
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "test")
                .param("address", "edit")
                .param("postcode", "TE5 4ST")
                .param("capacity", "100")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=6"));
    
        mockMvc.perform(post("/venues/edit/{id}", id)
                .with(user("test").roles(Security.ADMIN_ROLE))
                .param("name", "test")
                .param("address", "edit rd")
                .param("postcode", "TE5 4ST")
                .param("capacity", "-2")
                .with(csrf())
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues/edit/" + id + "?error=5"));
    }
    
    @Test
    public void testAddPageWithError() throws Exception {

    	mockMvc.perform(get("/venues/add")
                .param("error", "1")
                .param("name", "")
                .param("address", "")
                .param("postcode", "")
                .param("capacity", "")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Invalid input."))
                .andExpect(model().attribute("error_id", "1"))
                .andExpect(model().attribute("name", ""))
                .andExpect(model().attribute("address", ""))
                .andExpect(model().attribute("postcode", ""))
                .andExpect(model().attribute("capacity", ""));
    	
    	mockMvc.perform(get("/venues/add")
                .param("error", "2")
                .param("name", "")
                .param("address", "Test st")
                .param("postcode", "AB12 3CD")
                .param("capacity", "100")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Please enter a valid name."))
                .andExpect(model().attribute("error_id", "2"))
                .andExpect(model().attribute("name", ""))
                .andExpect(model().attribute("address", "Test st"))
                .andExpect(model().attribute("postcode", "AB12 3CD"))
                .andExpect(model().attribute("capacity", "100"));
    	
        mockMvc.perform(get("/venues/add")
                .param("error", "3")
                .param("name", "Test Venue")
                .param("address", "Test Address")
                .param("postcode", "AB12 3CD")
                .param("capacity", "100")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Please enter a valid address."))
                .andExpect(model().attribute("error_id", "3"))
                .andExpect(model().attribute("name", "Test Venue"))
                .andExpect(model().attribute("address", "Test Address"))
                .andExpect(model().attribute("postcode", "AB12 3CD"))
                .andExpect(model().attribute("capacity", "100"));

        mockMvc.perform(get("/venues/add")
                .param("error", "4")
                .param("name", "Test Venue")
                .param("address", "Test st")
                .param("postcode", "")
                .param("capacity", "100")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Please enter a valid postcode."))
                .andExpect(model().attribute("error_id", "4"))
                .andExpect(model().attribute("name", "Test Venue"))
                .andExpect(model().attribute("address", "Test st"))
                .andExpect(model().attribute("postcode", ""))
                .andExpect(model().attribute("capacity", "100"));
        
        mockMvc.perform(get("/venues/add")
                .param("error", "5")
                .param("name", "Test Venue")
                .param("address", "Test st")
                .param("postcode", "AB12 3CD")
                .param("capacity", "0")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Please enter a positive capacity."))
                .andExpect(model().attribute("error_id", "5"))
                .andExpect(model().attribute("name", "Test Venue"))
                .andExpect(model().attribute("address", "Test st"))
                .andExpect(model().attribute("postcode", "AB12 3CD"))
                .andExpect(model().attribute("capacity", "0"));
        
        mockMvc.perform(get("/venues/add")
                .param("error", "6")
                .param("name", "")
                .param("address", "Test st")
                .param("postcode", "AB12 3CD")
                .param("capacity", "100")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Please include a road name."))
                .andExpect(model().attribute("error_id", "6"))
                .andExpect(model().attribute("name", ""))
                .andExpect(model().attribute("address", "Test st"))
                .andExpect(model().attribute("postcode", "AB12 3CD"))
                .andExpect(model().attribute("capacity", "100"));
        
        mockMvc.perform(get("/venues/add")
                .param("error", "")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", "Unknown error."));
        
        mockMvc.perform(get("/venues/add")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/add"))
                .andExpect(model().attribute("error", ""));
                
        verifyNoInteractions(venueService);
    }
    
    @Test
    public void testAddVenueWithInvalidInput() throws Exception {
    	    String name = "Test Venue";
    	    String address = "test st";
    	    String postcode = "AB12 3CD";
    	    String capacity_str = "100";
    	    
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", "")
                .param("address", address)
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues/add?error=2&name=" + "" + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "")
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues/add?error=3&name=" + name + "&address=" + "" + "&postcode=" + postcode + "&capacity=" + capacity_str));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", address)
                .param("postcode", "")
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues/add?error=4&name=" + name + "&address=" + address + "&postcode=" + "" + "&capacity=" + capacity_str));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", address)
                .param("postcode", postcode)
                .param("capacity", "0")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues/add?error=5&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + "0"));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", address)
                .param("postcode", postcode)
                .param("capacity", "invalid capacity")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues/add?error=1&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + "invalid capacity"));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "test")
                .param("postcode", postcode)
                .param("capacity", "100")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/venues/add?error=6&name=" + name + "&address=" + "test" + "&postcode=" + postcode + "&capacity=" + "100"));
        
        verify(venueService, never()).add(anyString(), anyInt(), anyString(), anyString());
    }

    @Test
    public void testAddVenueWithValidInput() throws Exception {
    	String name = "Test Venue";
	    String address = "test st";
	    String postcode = "AB12 3CD";
	    String capacity_str = "100";

        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", address)
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "test street")
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));

        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "test road")
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "test rd")
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));
        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "test avenue")
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));

        mockMvc.perform(post("/venues/add")
                .with(user("test").roles(Security.ADMIN_ROLE))
                .with(csrf())
                .param("name", name)
                .param("address", "test ave")
                .param("postcode", postcode)
                .param("capacity", capacity_str)
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(view().name("redirect:/venues"));

        verify(venueService, times(1)).add(name, Integer.parseInt(capacity_str), address, postcode);
    }
    
    @Test
    public void testGetVenueInformation() throws Exception {
        long id = 1L;
        Venue testVenue = new Venue();
        testVenue.setId(id);
        long venueId = 1;
        Venue venue = new Venue();
        venue.setId(venueId);
        List<Event> eventsAtVenue = new ArrayList<>();
        Event testEvent = new Event();
        testEvent.setVenue(testVenue);
        when(venueService.findById(id)).thenReturn(Optional.of(testVenue));
        when(eventService.findUpcomingEvents()).thenReturn(eventsAtVenue);

        mockMvc.perform(get("/venues/description")
                .param("id", String.valueOf(id))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/description"))
                .andExpect(model().attribute("venue", testVenue))
                .andExpect(model().attribute("events", eventsAtVenue));

        when(venueService.findById(id)).thenReturn(Optional.of(venue));
        when(eventService.findUpcomingEvents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/venues/description")
                .param("id", String.valueOf(id))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/description"))
                .andExpect(model().attribute("venue", venue))
                .andExpect(model().attribute("events", Collections.emptyList()))
                .andExpect(model().attribute("error", ""));
        
        mockMvc.perform(get("/venues/description")
                .param("id", String.valueOf(id))
                .param("error", "unknown"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/description"))
                .andExpect(model().attribute("error", "Unknown error."))
                .andExpect(model().attribute("venue", samePropertyValuesAs(testVenue)));

        verify(venueService, times(3)).findById(id);
        verify(eventService, times(3)).findUpcomingEvents();
    
        long nonExistentId = 2L;

        when(venueService.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/venues/description")
                .param("id", String.valueOf(nonExistentId))
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof VenueNotFoundException))
                .andExpect(result -> assertEquals("Could not find venue " + nonExistentId, result.getResolvedException().getMessage()));

        verify(venueService, times(1)).findById(nonExistentId);

        
    }
    
    @Test
    public void testGetVenueInformationWithEventsError() throws Exception {
        // Prepare the test environment
        long venueId = 1;
        Venue testVenue = new Venue();
        testVenue.setId(venueId);
        
        Venue anotherVenue = new Venue();
        anotherVenue.setId(2L);

        Event eventAtTestVenue = new Event();
        eventAtTestVenue.setVenue(testVenue);

        Event eventAtAnotherVenue = new Event();
        eventAtAnotherVenue.setVenue(anotherVenue);

        // Mock the venueService and eventService
        when(venueService.findById(venueId)).thenReturn(Optional.of(testVenue));
        when(eventService.findUpcomingEvents()).thenReturn(Arrays.asList(eventAtTestVenue, eventAtAnotherVenue));

        // Perform the GET request
        mockMvc.perform(get("/venues/description")
                .param("id", String.valueOf(venueId)))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/description"))
                .andExpect(model().attribute("error", ""))
                .andExpect(model().attribute("venue", samePropertyValuesAs(testVenue)))
                .andExpect(model().attribute("events", hasItem(eventAtTestVenue)));
        mockMvc.perform(get("/venues/description")
                .param("id", String.valueOf(venueId))
                .param("error", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/description"))
                .andExpect(model().attribute("error", "The venue cannot be deleted as it is currently being used by one or more events."))
                .andExpect(model().attribute("venue", testVenue));

        verify(venueService, times(2)).findById(venueId);
        verifyNoMoreInteractions(venueService);
        verify(eventService, times(2)).findUpcomingEvents();
        // Verify interactions with the venueService and eventService
    
    }
    
    @Test
    public void testSearch() throws Exception {
        String searchQuery = "Test";
        Venue testVenue = new Venue();
        testVenue.setName("Test Venue");
        // Set other properties as needed

        List<Venue> venueList = Collections.singletonList(testVenue);

        when(venueService.findByNameContainingIgnoreCase(searchQuery)).thenReturn(venueList);
        when(venueService.findAll()).thenReturn(venueList);

        mockMvc.perform(get("/venues/search")
                .param("q", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/searchResult"))
                .andExpect(model().attribute("found", true))
                .andExpect(model().attribute("venues", venueList))
                .andExpect(model().attribute("searchMessage", "VENUE CONTAINING '" + searchQuery + "' FOUND"));
        
        searchQuery = "NonExistent";
        mockMvc.perform(get("/venues/search")
                .param("q", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/searchResult"))
                .andExpect(model().attribute("found", false))
                .andExpect(model().attribute("venues", venueList))
                .andExpect(model().attribute("searchMessage", "VENUE CONTAINING '"+ searchQuery + "' NOT FOUND, HERE IS ALL THE VENUES WE HAVE"));
        
        
        
        verify(venueService, times(1)).findByNameContainingIgnoreCase(searchQuery);
        verify(venueService, times(1)).findAll();
    }

    
    @Test
    public void testSearchEmptyQuery() throws Exception {
        String searchQuery = "";
        Venue testVenue = new Venue();
        testVenue.setName("Test Venue");
        // Set other properties as needed

        List<Venue> venueList = Collections.singletonList(testVenue);

        when(venueService.findAll()).thenReturn(venueList);

        mockMvc.perform(get("/venues/search")
                .param("q", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/searchResult"))
                .andExpect(model().attribute("found", false))
                .andExpect(model().attribute("venues", venueList));

        verify(venueService, times(0)).findByNameContainingIgnoreCase(anyString());
        verify(venueService, times(1)).findAll();
        verifyNoMoreInteractions(venueService);
    }


    
}
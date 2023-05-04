package uk.ac.man.cs.eventlite.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

  

}
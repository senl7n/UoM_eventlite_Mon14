package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;



@Controller
@RequestMapping(value = "/", produces = {MediaType.TEXT_HTML_VALUE})
public class HomepageController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void getJsonRoot() {
    }

    @GetMapping
    public String getHomepage(Model model) {
        Iterable<Event> upcoming3Events = eventService.findUpcoming3Events();
        model.addAttribute("upcoming3Events", upcoming3Events);
        model.addAttribute("popular3Venues", venueService.findPopular3Venues());

        return "homepage/index";
    }

}
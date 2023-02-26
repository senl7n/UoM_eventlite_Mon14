package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import uk.ac.man.cs.eventlite.dao.EventRepository;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {
		throw new EventNotFoundException(id);
	}

	@GetMapping
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
//        model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}

	@GetMapping("/description")
	public String getEventInfomation(@RequestParam(name="id") long id, Model model) {
		ArrayList<Event> events = (ArrayList<Event>) eventService.findAll();
		Event event = null;
		for(Event e: events) {
			if(e.getId()==id) {
				event = e;
			}
		}

		model.addAttribute("event", event);
		return "/events/description/description";
	}

    //delete event
    @DeleteMapping("/{id}")
    public String deleteEvent(@PathVariable("id") long id) {
        eventService.deleteById(id);
        return "redirect:/events";
    }

    //edit event
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") long id,
                           @RequestParam(value = "error", required = false) String error,
                           Model model) {
        model.addAttribute("event", eventService.findById(id));
        if (error != null) {
            model.addAttribute("error", "Something went wrong! Please try again.");
        }
        return "events/edit";
    }

    @PostMapping("/edit/{id}")
    public String editEvent(@PathVariable("id") long id,
                            @RequestParam("name") String name,
                            @RequestParam("date") String date,
                            @RequestParam("time") String time,
                            @RequestParam("description") String description,
                            @RequestParam("venue_id") long venue_id,
                            @ModelAttribute Event event) {
        LocalDate date1 = LocalDate.parse(date);
        LocalTime time1 = LocalTime.parse(time);
        if (eventService.update(id, name, date1, time1, venue_id, description)) {
            return "redirect:/events";
        }
        else {
            return "redirect:/events/edit/" + id + "?error=1";
        }
    }

    //add event
    @GetMapping("/add")
    public String addPage(@RequestParam(value = "error", required = false) String error,
                          Model model) {
        if (error != null) {
            model.addAttribute("error", "Something went wrong! Please try again.");
        }
        else {
            model.addAttribute("error", "");
        }
        return "events/add";
    }

    @PostMapping("/add")
    public String addEvent(@RequestParam("name") String name,
                           @RequestParam("date") String date,
                           @RequestParam("time") String time,
                           @RequestParam("description") String description,
                           @RequestParam("venue_id") long venue_id) {
        try {
            LocalDate date1 = LocalDate.parse(date);
            LocalTime time1 = LocalTime.parse(time);
        }
        catch (Exception e) {
            return "redirect:/events/add?error=1";
        }
        LocalDate date1 = LocalDate.parse(date);
        LocalTime time1 = LocalTime.parse(time);
        if (eventService.add(name, date1, time1, venue_id, description)) {
            return "redirect:/events";
        }
        else {
            return "redirect:/events/add?error=1";
        }
    }
    
    //search event    
    @GetMapping("/search")
    public String search(@RequestParam(name="q") String query, Model model) {
    	Iterable<Event> events = eventService.findByName(query);
		if (events.iterator().hasNext()) {
		        model.addAttribute("events", events);
		    	model.addAttribute("found", true);
		        return "/events/searchResult";
		    } else {
		    	model.addAttribute("found", false);
		        model.addAttribute("events", eventService.findAll());
		        return "/events/searchResult";
		    }
    }
 
    

}

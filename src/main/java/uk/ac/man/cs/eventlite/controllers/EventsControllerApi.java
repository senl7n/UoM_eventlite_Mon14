package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping(value = "/api/events", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class EventsControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private EventService eventService;

	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(EventNotFoundException.class)
	public ResponseEntity<?> eventNotFoundHandler(EventNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Event> getEvent(@PathVariable("id") long id) {
		throw new EventNotFoundException(id);
	}

	@GetMapping
	public CollectionModel<EntityModel<Event>> getAllEvents() {
		return eventAssembler.toCollectionModel(eventService.findAll())
				.add(linkTo(methodOn(EventsControllerApi.class).getAllEvents()).withSelfRel());
	}

    //delete event
    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable("id") long id) {
        eventService.deleteById(id);
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
}

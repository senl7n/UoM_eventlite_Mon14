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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

@RestController
@RequestMapping(value = "/api/events", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class EventsControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private EventService eventService;

    @Autowired
    private VenueService venueService;

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
	
//	//home
//	@GetMapping("/home")
//	//CollectionModel<EntityModel<Event>>
//	public String getNextThreeEvents() {
////		return eventAssembler.toCollectionModel(eventService.findUpcoming3Events())
////				.add(linkTo(methodOn(EventsControllerApi.class).getNextThreeEvents()).withSelfRel());
//		
//		return "/events/home";
//	}

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
    public void deleteEvent(@PathVariable("id") long id) {
        eventService.deleteById(id);
    }

    //edit event
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") long id,
                           @RequestParam(value = "error", required = false) String error,
                           Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("venues", venueService.findAll());
        if (error == null) {
            model.addAttribute("error", "");
        }
        else if (error.equals("1")) {
            model.addAttribute("error", "Please enter a valid name/venue ID.");
        }
        else if(error.equals("2")) {
            model.addAttribute("error", "Please enter a valid date.");
        }
        else if(error.equals("3")) {
            model.addAttribute("error", "Please enter a future date.");
        }
        return "events/edit";
    }

    @PostMapping("/edit/{id}")
    public String editEvent(@PathVariable("id") long id,
                            @RequestParam("name") String name,
                            @RequestParam("date") String date,
                            @RequestParam("time") String time,
                            @RequestParam("description") String description,
                            @RequestParam("venue_id") String venue_id_str,
                            Model model) {
        try {
            long venue_id = Long.parseLong(venue_id_str);
        }
        catch (Exception e) {
            return "redirect:/events/edit/" + id + "?error=1";
        }
        long venue_id = Long.parseLong(venue_id_str);
        if (venue_id==99) return "redirect:/events/edit/" + id + "?error=1";
        LocalTime time1 = null;
        try {
            LocalDate.parse(date);
            if (!time.isEmpty()){
                time1 = LocalTime.parse(time);
            }
        }
        catch (DateTimeParseException e) {
            return "redirect:/events/edit/" + id + "?error=2";
        }
        if (!time.isEmpty()){
            time1 = LocalTime.parse(time);
        }
        LocalDate date1 = LocalDate.parse(date);
        if (date1.isBefore(LocalDate.now())) {
            return "redirect:/events/edit/" + id + "?error=3";
        }
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
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "date", required = false) String date,
                          @RequestParam(value = "time", required = false) String time,
                          @RequestParam(value = "description", required = false) String description,
                          @RequestParam(value = "venue_id", required = false) String venue_id,
                          Model model) {
        model.addAttribute("venues", venueService.findAll());
        if (error == null) {
            model.addAttribute("error", "");
        }
        else if (error.equals("1")) {
            model.addAttribute("error", "Please enter a valid name/venue ID.");
        }
        else if(error.equals("2")) {
            model.addAttribute("error", "Please enter a valid date.");
        }
        else if(error.equals("3")) {
            model.addAttribute("error", "Please enter a future date.");
        }
        model.addAttribute("name", name);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("description", description);
        model.addAttribute("venue_id", venue_id);
        return "events/add";
    }

    @PostMapping("/add")
    public String addEvent(@RequestParam("name") String name,
                           @RequestParam("date") String date,
                           @RequestParam("time") String time,
                           @RequestParam("description") String description,
                           @RequestParam("venue_id") String venue_id_str) {
        try {
            long venue_id = Long.parseLong(venue_id_str);
        }
        catch (Exception e) {
            return "redirect:/events/add?error=1&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id_str;
        }
        long venue_id = Long.parseLong(venue_id_str);
        if (venue_id==99) return "redirect:/events/add?error=1&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id_str;
        LocalTime time1 = null;
        try {
            LocalDate date1 = LocalDate.parse(date);
            if (!time.isEmpty()){
                time1 = LocalTime.parse(time);
            }
        }
        catch (Exception e) {
            return "redirect:/events/add?error=2&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id;
        }
        LocalDate date1 = LocalDate.parse(date);
        if (date1.isBefore(LocalDate.now())) {
            return "redirect:/events/add?error=3&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id;
        }
        if (!time.isEmpty()){
            time1 = LocalTime.parse(time);
        }
        if (eventService.add(name, date1, time1, venue_id, description)) {
            return "redirect:/events";
        }
        else {
            return "redirect:/events/add?error=1&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id;
        }
    }

    //search event
    @GetMapping("/search")
    public String search(@RequestParam(name="q") String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("found", false);
            model.addAttribute("events", eventService.findAll());
        }
        else {
            Iterable<Event> events = eventService.findByNameContainingIgnoreCase(query);
            if (events.iterator().hasNext()) {
                model.addAttribute("events", events);
                model.addAttribute("found", true);
            } else {
                model.addAttribute("found", false);
                model.addAttribute("events", eventService.findAll());
            }
        }
        return "/events/searchResult";
    }

}

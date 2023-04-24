package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

    private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueModelAssembler venueAssembler;

    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
    }

    @GetMapping("/{id}")
    public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
        return venueAssembler.toModel(venue);
    }


    @GetMapping
    public CollectionModel<EntityModel<Venue>> getAllVenues() {
        String profileLinkHref = linkTo(VenuesControllerApi.class).toUriComponentsBuilder().replacePath("/api/profile/venues").build().toUriString();

        return venueAssembler.toCollectionModel(venueService.findAll())
                .add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel())
                .add(Link.of(profileLinkHref, "profile"));
    }


    @GetMapping("/{id}/events")
    public ResponseEntity<CollectionModel<EntityModel<Event>>> getVenueEvents(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
        Iterable<Event> allEvents = eventService.findUpcomingEvents();
        List<Event> eventsAtVenue = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.getVenue().equals(venue)) {
                eventsAtVenue.add(event);
            }
        }

        List<EntityModel<Event>> eventModels = new ArrayList<>();
        for (Event event : eventsAtVenue) {
            EntityModel<Event> eventModel = EntityModel.of(event,
                    linkTo(methodOn(EventsControllerApi.class).getEvent(event.getId())).withSelfRel());

            eventModels.add(eventModel);
        }

        CollectionModel<EntityModel<Event>> eventCollectionModel = CollectionModel.of(eventModels,
                linkTo(methodOn(VenuesControllerApi.class).getVenueEvents(id)).withSelfRel());

        return ResponseEntity.ok(eventCollectionModel);
    }


    @GetMapping("/{id}/next3events")
    public ResponseEntity<CollectionModel<EntityModel<Event>>> getVenueNext3Events(@PathVariable("id") long id) {
        Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
        Iterable<Event> allEvents = eventService.findUpcomingEvents();
        List<Event> eventsAtVenue = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.getVenue().equals(venue)) {
                eventsAtVenue.add(event);
            }
        }

        // Sort the events by date and get the next 3 events
        eventsAtVenue.sort(Comparator.comparing(Event::getDate));
        List<Event> next3Events = eventsAtVenue.stream().limit(3).collect(Collectors.toList());

        List<EntityModel<Event>> eventModels = new ArrayList<>();
        for (Event event : next3Events) {
            EntityModel<Event> eventModel = EntityModel.of(event,
                    linkTo(methodOn(EventsControllerApi.class).getEvent(event.getId())).withSelfRel());

            eventModels.add(eventModel);
        }

        CollectionModel<EntityModel<Event>> eventCollectionModel = CollectionModel.of(eventModels,
                linkTo(methodOn(VenuesControllerApi.class).getVenueNext3Events(id)).withSelfRel());

        return ResponseEntity.ok(eventCollectionModel);
    }
}

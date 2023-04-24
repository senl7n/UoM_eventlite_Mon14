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
import org.springframework.web.bind.annotation.*;

import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

    private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueModelAssembler venueAssembler;

    @ExceptionHandler(VenueNotFoundException.class)
    public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
    }

    @GetMapping("/{id}")
    public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
        throw new VenueNotFoundException(id);
    }

    @GetMapping
    public CollectionModel<EntityModel<Venue>> getAllVenues() {
        return venueAssembler.toCollectionModel(venueService.findAll())
                .add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel());
    }

}

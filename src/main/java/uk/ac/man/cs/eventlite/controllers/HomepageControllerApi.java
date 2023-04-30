package uk.ac.man.cs.eventlite.controllers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api", produces = { MediaType.APPLICATION_JSON_VALUE })
public class HomepageControllerApi {

    @GetMapping
    public RepresentationModel<?> getApiLinks() {
        Link venuesLink = WebMvcLinkBuilder.linkTo(VenuesControllerApi.class).withRel("venues");
        Link eventsLink = WebMvcLinkBuilder.linkTo(EventsControllerApi.class).withRel("events");
        Link profileLink = WebMvcLinkBuilder.linkTo(HomepageControllerApi.class).slash("profile").withRel("profile");

        RepresentationModel<?> model = new RepresentationModel<>();
        model.add(venuesLink, eventsLink, profileLink);

        return model;
    }
}

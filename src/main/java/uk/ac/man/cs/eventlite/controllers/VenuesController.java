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
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

    @Autowired
    private VenueService venueService;

    @Autowired
    private EventService eventService;

    @ExceptionHandler(VenueNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
        model.addAttribute("not_found_id", ex.getId());

        return "venues/not_found";
    }

    @GetMapping("/{id}")
    public String getVenue(@PathVariable("id") long id, Model model) {
        throw new VenueNotFoundException(id);
    }

    @GetMapping
    public String getAllVenues(Model model) {

        model.addAttribute("venues", venueService.findAll());
        return "venues/index";
    }

    //delete venue
    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable("id") long id) {
        if (!venueService.checkVenueOccupied(id)) {
            return "redirect:/venues/description?id=" + id + "&error=1";
        }
        venueService.deleteById(id);
        return "redirect:/venues";
    }


    //edit venue
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") long id,
                           @RequestParam(value = "error", required = false) String error,
                           Model model) throws VenueNotFoundException {
        Optional<Venue> venue = venueService.findById(id);

        if (!venue.isPresent()) {
            throw new VenueNotFoundException(id);
        }

        model.addAttribute("venue", venue.get());
        if (error == null) {
            model.addAttribute("error", "");
        } else if (error.equals("1")) {
            model.addAttribute("error", "Invalid capacity.");
        } else if (error.equals("2")) {
            model.addAttribute("error", "Please enter a valid name.");
        } else if (error.equals("3")) {
            model.addAttribute("error", "Please enter a valid address.");
        } else if (error.equals("4")) {
            model.addAttribute("error", "Please enter a valid postcode.");
        } else if (error.equals("5")) {
            model.addAttribute("error", "Please enter a positive capacity.");
        } else if (error.equals("6")) {
            model.addAttribute("error", "Please include a road name.");
        } else {
            model.addAttribute("error", "Unknown error.");
        }
        model.addAttribute("error_id", error);

        return "venues/edit";
    }

    @PostMapping("/edit/{id}")
    public String editVenue(@PathVariable("id") long id,
                            @RequestParam("name") String name,
                            @RequestParam("address") String address,
                            @RequestParam("postcode") String postcode,
                            @RequestParam("capacity") String capacity_str,
                            Model model) {
        int capacity = 0;
        try {
            capacity = Integer.parseInt(capacity_str);
        }
        catch (NumberFormatException e) {
            return "redirect:/venues/edit/" + id + "?error=1";
        }
        if (name.equals("")) {
            return "redirect:/venues/edit/" + id + "?error=2";
        }
        if (address.equals("")) {
            return "redirect:/venues/edit/" + id + "?error=3";
        }
        if (postcode.equals("")) {
            return "redirect:/venues/edit/" + id + "?error=4";
        }
        if (capacity <= 0) {
            return "redirect:/venues/edit/" + id + "?error=5";
        }
        if (!address.toLowerCase().contains(" road") && !address.toLowerCase().contains(" rd") && !address.toLowerCase().contains(" street") && !address.toLowerCase().contains(" st") && !address.toLowerCase().contains(" avenue") && !address.toLowerCase().contains(" ave")) {
            return "redirect:/venues/edit/" + id + "?error=6";
        }
        venueService.update(id, name, capacity, address, postcode);
        return "redirect:/venues";
    }

    //add venue
    @GetMapping("/add")
    public String addPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "address", required = false) String address,
                          @RequestParam(value = "postcode", required = false) String postcode,
                          @RequestParam(value = "capacity", required = false) String capacity_str,
                          Model model) {
        if (error == null) {
            model.addAttribute("error", "");
        }
        else if(error.equals("1")) {
            model.addAttribute("error", "Invalid input.");
        }
        else if(error.equals("2")) {
            model.addAttribute("error", "Please enter a valid name.");
        }
        else if(error.equals("3")) {
            model.addAttribute("error", "Please enter a valid address.");
        }
        else if(error.equals("4")) {
            model.addAttribute("error", "Please enter a valid postcode.");
        }
        else if(error.equals("5")) {
            model.addAttribute("error", "Please enter a positive capacity.");
        }
        else if(error.equals("6")) {
            model.addAttribute("error", "Please include a road name.");
        }
        else {
            model.addAttribute("error", "Unknown error.");
        }
        model.addAttribute("error_id", error);
        model.addAttribute("name", name);
        model.addAttribute("address", address);
        model.addAttribute("postcode", postcode);
        model.addAttribute("capacity", capacity_str);
        return "venues/add";
    }

    @PostMapping("/add")
    public String addVenue(@RequestParam("name") String name,
                           @RequestParam("address") String address,
                           @RequestParam("postcode") String postcode,
                           @RequestParam("capacity") String capacity_str,
                           Model model) {
        int capacity = 0;
        try {
            capacity = Integer.parseInt(capacity_str);
        }
        catch (NumberFormatException e) {
            return "redirect:/venues/add?error=1&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str;
        }
        if (name.equals("")) {
            return "redirect:/venues/add?error=2&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str;
        }
        if (address.equals("")) {
            return "redirect:/venues/add?error=3&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str;
        }
        if (postcode.equals("")) {
            return "redirect:/venues/add?error=4&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str;
        }
        if (capacity <= 0) {
            return "redirect:/venues/add?error=5&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str;
        }
        if (!address.toLowerCase().contains(" road") && !address.toLowerCase().contains(" rd") && !address.toLowerCase().contains(" street") && !address.toLowerCase().contains(" st") && !address.toLowerCase().contains(" avenue") && !address.toLowerCase().contains(" ave")) {
            return "redirect:/venues/add?error=6&name=" + name + "&address=" + address + "&postcode=" + postcode + "&capacity=" + capacity_str;
        }
        venueService.add(name, capacity, address, postcode);
        return "redirect:/venues";
    }

    @GetMapping("/description")
    public String getVenueInformation(@RequestParam("id") long id,
                                      @RequestParam(value = "error", required = false) String error,
                                      Model model) throws VenueNotFoundException {
        Optional<Venue> venue = venueService.findById(id);

        if (!venue.isPresent()) {
            throw new VenueNotFoundException(id);
        }

        Iterable<Event> allEvents = eventService.findUpcomingEvents();
        ArrayList<Event> eventsAtVenue = new ArrayList<Event>();
        for (Event e : allEvents) {
            if (e.getVenue().equals(venue.get())) {
                eventsAtVenue.add(e);
            }
        }

        if (error == null) {
            model.addAttribute("error", "");
        } else if (error.equals("1")) {
            model.addAttribute("error", "The venue cannot be deleted as it is currently being used by one or more events.");
        } else {
            model.addAttribute("error", "Unknown error.");
        }
        model.addAttribute("venue", venue.get());
        model.addAttribute("events", eventsAtVenue);
        return "venues/description";
    }


    //search venue
    @GetMapping("/search")
    public String search(@RequestParam(name = "q") String query, Model model) {
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("found", false);
            model.addAttribute("venues", venueService.findAll());
        }
        else {
            Iterable<Venue> venues = venueService.findByNameContainingIgnoreCase(query);
            if (venues.iterator().hasNext()) {
                model.addAttribute("venues", venues);
                model.addAttribute("found", true);
                model.addAttribute("searchMessage", "VENUE CONTAINING '" + query + "' FOUND");
            }
            else {
                model.addAttribute("found", false);
                model.addAttribute("venues",venueService.findAll());
                model.addAttribute("searchMessage", "VENUE CONTAINING '"+ query + "' NOT FOUND, HERE IS ALL THE VENUES WE HAVE");
            }
        }
        return "venues/searchResult";
    }

}

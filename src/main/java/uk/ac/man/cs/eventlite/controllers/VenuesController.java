package uk.ac.man.cs.eventlite.controllers;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

import java.util.Optional;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

    @Autowired
    private VenueService venueService;


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
        venueService.deleteById(id);
        return "redirect:/venues";
    }

    //edit venue
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable("id") long id,
                           @RequestParam(value = "error", required = false) String error,
                           Model model) {
        Optional<Venue> venue = venueService.findById(id);
        if (venue.isPresent()) {
            model.addAttribute("venue", venue.get());
            if (error != null) {
                model.addAttribute("error", error);
            }
            return "venues/edit";
        }
        return "redirect:/venues";
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
        if (name.equals("") || address.equals("") || postcode.equals("") || capacity_str.equals("")) {
            return "redirect:/venues/edit/" + id + "?error=1";
        }
        venueService.update(id, name, capacity, address, postcode);
        return "redirect:/venues";
    }

    //add venue
    @GetMapping("/add")
    public String addPage(@RequestParam(value = "error", required = false) String error,
                          Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "venues/add";
    }

    @PostMapping("/add")
    public String addEvent(@RequestParam("name") String name,
                            @RequestParam("address") String address,
                            @RequestParam("postcode") String postcode,
                            @RequestParam("capacity") String capacity_str,
                            Model model) {
          int capacity = 0;
          try {
                capacity = Integer.parseInt(capacity_str);
          }
          catch (NumberFormatException e) {
                return "redirect:/venues/add?error=1";
          }
          if (name.equals("") || address.equals("") || postcode.equals("") || capacity_str.equals("")) {
                return "redirect:/venues/add?error=1";
          }
          venueService.add(name, capacity, address, postcode);
          return "redirect:/venues";
     }

}

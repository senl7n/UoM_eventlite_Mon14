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
            if (error == null) {
                model.addAttribute("error", "");
            }
            else if(error.equals("1")) {
                model.addAttribute("error", "Invalid capacity.");
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
            else {
                model.addAttribute("error", "Unknown error.");
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
        venueService.update(id, name, capacity, address, postcode);
        return "redirect:/venues";
    }

    //add venue
    @GetMapping("/add")
    public String addPage(@RequestParam(value = "error", required = false) String error,
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
        else {
            model.addAttribute("error", "Unknown error.");
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
          if (name.equals("")) {
                return "redirect:/venues/add?error=2";
          }
          if (address.equals("")) {
                return "redirect:/venues/add?error=3";
          }
          if (postcode.equals("")) {
                return "redirect:/venues/add?error=4";
          }
          if (capacity <= 0) {
                return "redirect:/venues/add?error=5";
          }
          venueService.add(name, capacity, address, postcode);
          return "redirect:/venues";
     }

}

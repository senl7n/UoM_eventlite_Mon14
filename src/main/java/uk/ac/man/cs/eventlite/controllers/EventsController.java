package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;

import okhttp3.OkHttpClient;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

    private static final Logger logger = LoggerFactory.getLogger(EventsController.class);

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
        Iterable<Event> upcomingEvents = eventService.findUpcomingEvents();
        Iterable<Event> previousEvents = eventService.findPreviousEvents();

        model.addAttribute("events", eventService.findAll());
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("previousEvents", previousEvents);
        prepareModelAttributes(model);

        return "events/index";
    }

    @GetMapping("/description")
    public String getEventInfomation(@RequestParam(name="id") long id,
                                     @RequestParam(name="error", required=false) String error,
                                     @RequestParam(name="comment", required=false) String comment,
                                     Model model) {
		Event event = eventService.findById(id);
        if (event == null) {
            throw new EventNotFoundException(id);
        }
        model.addAttribute("error", error);
		model.addAttribute("event", event);
        model.addAttribute("comment", comment);
		return "/events/description";
	}

    //delete event
    @DeleteMapping("/{id}")
    public String deleteEvent(@PathVariable("id") long id) {
        if (eventService.findById(id) == null) {
            throw new EventNotFoundException(id);
        }else{
            eventService.deleteById(id);
            return "redirect:/events";
        }
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
        else {
            model.addAttribute("error", "Unknown error.");
        }
        model.addAttribute("error_id", error);

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
        if (venue_id==99) {
            return "redirect:/events/edit/" + id + "?error=1";
        }else {
            LocalTime time1 = null;
            try {
                LocalDate.parse(date);
                if (!time.isEmpty()) {
                    time1 = LocalTime.parse(time);
                }
            } catch (DateTimeParseException e) {
                return "redirect:/events/edit/" + id + "?error=2";
            }
            if (!time.isEmpty()) {
                time1 = LocalTime.parse(time);
            }
            LocalDate date1 = LocalDate.parse(date);
            if (date1.isBefore(LocalDate.now())) {
                return "redirect:/events/edit/" + id + "?error=3";
            }
            if (eventService.update(id, name, date1, time1, venue_id, description)) {
                return "redirect:/events";
            } else {
                return "redirect:/events/edit/" + id + "?error=1";
            }
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
        else {
            model.addAttribute("error", "Unknown error.");
        }
        model.addAttribute("error_id", error);
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
        if (venue_id==99) {
            return "redirect:/events/add?error=1&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id_str;
        }else {
            LocalTime time1 = null;
            try {
                LocalDate date1 = LocalDate.parse(date);
                if (!time.isEmpty()) {
                    time1 = LocalTime.parse(time);
                }
            } catch (Exception e) {
                return "redirect:/events/add?error=2&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id;
            }
            LocalDate date1 = LocalDate.parse(date);
            if (date1.isBefore(LocalDate.now())) {
                return "redirect:/events/add?error=3&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id;
            }
            if (!time.isEmpty()) {
                time1 = LocalTime.parse(time);
            }
            if (eventService.add(name, date1, time1, venue_id, description)) {
                return "redirect:/events";
            } else {
                return "redirect:/events/add?error=1&name=" + name + "&date=" + date + "&time=" + time + "&description=" + description + "&venue_id=" + venue_id;
            }
        }
    }

    //search event    
    @GetMapping("/search")
    public String search(@RequestParam(name="q", defaultValue="") String query, Model model) {
        prepareModelAttributes(model);
        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("found", false);
            Iterable<Event> upcomingEvents = eventService.findUpcomingEvents();
            Iterable<Event> previousEvents = eventService.findPreviousEvents();
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("previousEvents", previousEvents);
        }
        else {
            Iterable<Event> events = eventService.findByNameContainingIgnoreCase(query);
            if (events.iterator().hasNext()) {
                List<Event> upcomingEvents = new ArrayList<>();
                List<Event> previousEvents = new ArrayList<>();

                for (Event event : events) {
                    if (event.getTime() != null) {
                        if (event.getDateTime().isAfter(LocalDateTime.now())) {
                            upcomingEvents.add(event);
                        } else {
                            previousEvents.add(event);
                        }
                    } else {
                        if (event.getDate().isBefore(LocalDate.now())) {
                            previousEvents.add(event);
                        } else {
                            upcomingEvents.add(event);
                        }
                    }
                }
                upcomingEvents.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getName).thenComparing(Event::getTime));
                previousEvents.sort(Comparator.comparing(Event::getDate).thenComparing(Event::getName).thenComparing(Event::getTime));
                model.addAttribute("upcomingEvents", upcomingEvents);
                model.addAttribute("previousEvents", previousEvents);
                model.addAttribute("found", true);
                model.addAttribute("searchMessage", "EVENT CONTAINING '" + query + "' FOUND");
            } else {
                model.addAttribute("found", false);
                Iterable<Event> upcomingEvents = eventService.findUpcomingEvents();
                Iterable<Event> previousEvents = eventService.findPreviousEvents();
                model.addAttribute("upcomingEvents", upcomingEvents);
                model.addAttribute("previousEvents", previousEvents);
                model.addAttribute("searchMessage", "EVENT CONTAINING '"+ query + "' NOT FOUND, HERE IS ALL THE EVENTS WE HAVE");

            }
        }
        return "events/searchResult";
    }

    @PostMapping("/postComment/{id}")
    public String postComment(@PathVariable("id") long id, @RequestParam("comment") String comment, Model model, RedirectAttributes redirectAttributes) throws Mastodon4jRequestException {
        String accessToken = "AQHbd7AEwgaFHDARmaYPSPjkIvFrIMu-ZWhnpU2AIN0";

        MastodonClient mastodonClient = new MastodonClient.Builder("mastodon.online", new OkHttpClient.Builder(), new Gson())
                .accessToken(accessToken)
                .useStreamingApi()
                .build();

        Statuses statuses = new Statuses(mastodonClient);
        try {
            statuses.postStatus(comment, null, null, false, null, Status.Visibility.Private).execute();
        }
        catch (Exception e) {
            return "redirect:/events/description?id=" + id + "&error=1";
        }
        return "redirect:/events/description?id=" + id + "&error=0" + "&comment=" + comment;
    }


    public void prepareModelAttributes(Model model) {
        // Mastodon timeline
        List<Status> timeline = new ArrayList<>();
        MastodonClient client = new MastodonClient.Builder("mastodon.online", new OkHttpClient.Builder(), new Gson())
                .accessToken("AQHbd7AEwgaFHDARmaYPSPjkIvFrIMu-ZWhnpU2AIN0")
                .useStreamingApi()
                .build();

        try {
            // Retrieve the home timeline for the authenticated user
            Timelines homeTimeline = new Timelines(client);
            Pageable<Status> pageableStatuses = homeTimeline.getHome(new Range()).execute();
            timeline = pageableStatuses.getPart();
        } catch (Mastodon4jRequestException e) {
            e.printStackTrace();
        }

        // Get the latest three messages from the timeline
        List<Status> latest3Messages = timeline.subList(0, Math.min(3, timeline.size()));

        // Create a list of message contents
        List<String> messageContents = new ArrayList<>();
        List<String> messageURLs = new ArrayList<>();
        List<String> messageDates = new ArrayList<>();
        List<String> messageTimes = new ArrayList<>();
        Pattern pattern = Pattern.compile("<.*?>");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        for (Status message : latest3Messages) {
            ZonedDateTime messageDateTime = ZonedDateTime.parse(message.getCreatedAt(), inputFormatter).withZoneSameInstant(ZoneId.systemDefault());

            String messageContent = unescapeHtml(message.getContent());
            messageContents.add(pattern.matcher(messageContent).replaceAll(""));
            messageURLs.add(message.getUrl());
            messageDates.add(messageDateTime.format(dateFormatter));
            messageTimes.add(messageDateTime.format(timeFormatter));
        }


        // Add the message attributes to the model
        model.addAttribute("messageContents", messageContents);
        model.addAttribute("messageTimes", messageTimes);
        model.addAttribute("messageDates", messageDates);
        model.addAttribute("messageURLs", messageURLs);

        Iterable<Event> upcomingEvents = eventService.findUpcomingEvents();
        Iterable<Event> previousEvents = eventService.findPreviousEvents();
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("previousEvents", previousEvents);

        if (!model.containsAttribute("found")) {
            model.addAttribute("found", false);
        }
    }

    @GetMapping("/searchResult")
    public String getSearchResultPageMessage(Model model) {
        prepareModelAttributes(model);
        return "events/searchResult";
    }

    private static String unescapeHtml(String input) {
        return input.replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'");
    }

}

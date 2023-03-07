package uk.ac.man.cs.eventlite.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;

import uk.ac.man.cs.eventlite.entities.Event;

import java.time.LocalDate;
import java.time.LocalTime;

import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
                Venue venue = new Venue();
                venue.setId(1);
                venue.setName("Kilburn Building");
                venue.setAddress("Oxford Road");
                venue.setPostcode("M13 9PL");
                venue.setCapacity(250);
                venueService.save(venue);

                Venue venue2 = new Venue();
                venue2.setId(2);
                venue2.setName("Engineering Building");
                venue2.setAddress("Oxford Road");
                venue2.setPostcode("M13 9PL");
                venue2.setCapacity(250);
                venueService.save(venue2);

                Venue venue3 = new Venue();
                venue3.setId(3);
                venue3.setName("Stopford Building");
                venue3.setAddress("Oxford Road");
                venue3.setPostcode("M13 9PL");
                venue3.setCapacity(250);
                venueService.save(venue3);

                for (int i = 1; i < 4; i++) {
                    Event event = new Event();
                    event.setName("COMP23412 Showcase 0" + i);
                    event.setVenue(venue);
                    event.setDate(LocalDate.parse("2023-05-0" + i));
                    event.setTime(LocalTime.parse("12:00"));
                    eventService.save(event);
                }
                
                for (int i = 1; i < 4; i++) {
                    Event event = new Event();
                    event.setName("COMP23412 Showcase 4" + i);
                    event.setVenue(venue);
                    event.setDate(LocalDate.parse("2023-01-0" + i));
                    event.setTime(LocalTime.parse("12:00"));
                    eventService.save(event);
                }

			}
		};
	}
}

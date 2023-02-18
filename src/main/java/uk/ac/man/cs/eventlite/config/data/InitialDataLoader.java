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
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
				
					Venue venue = new Venue();
					venue.setId(1);
					venue.setName("Kilburn Building");
					venue.setCapacity(250);
					venueService.save(venue);
                

			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				Venue venue = new Venue();
				venue.setId(1);
				venue.setName("Kilburn Building");
				venue.setCapacity(250);
                for (int i = 1; i < 4; i++) {
                    Event event = new Event();
                    event.setName("COMP23412 Showcase 0" + i);
                    event.setVenue(venue);
                    event.setDate(LocalDate.parse("2023-05-0" + i));
                    event.setTime(LocalTime.parse("12:00"));
                    eventService.save(event);
                }

			}
		};
	}
}

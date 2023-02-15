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

@Configuration
@Profile("test")
public class TestDataLoader {

    private final static Logger log = LoggerFactory.getLogger(TestDataLoader.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Build and save test events and venues here.
            // The test database is configured to reside in memory, so must be initialized
            // every time.
            for (int i = 1; i < 4; i++) {
                Event event = new Event();
                event.setName("COMP23412 Showcase 0" + i);
                event.setVenue(1);
                event.setDate(LocalDate.parse("2023-05-0" + i));
                event.setTime(LocalTime.parse("12:00"));
                eventService.save(event);
            }
        };
    }
}

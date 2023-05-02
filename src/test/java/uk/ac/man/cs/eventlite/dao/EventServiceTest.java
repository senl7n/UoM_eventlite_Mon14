package uk.ac.man.cs.eventlite.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import org.springframework.test.web.servlet.MockMvc;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")

public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService EventServiceImpl;

	@Autowired
	private VenueService VenueServiceImpl;

	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFindAll() {
		// Retrieve all events using the findAll() method
		Iterable<Event> allEvents = EventServiceImpl.findAll();
		int eventCount = 0;

		// Iterate through all events and increment the counter for each event
		for (Event event : allEvents) {
			eventCount++;
		}

		// Compare the event count with the expected value
		assertEquals(6, eventCount, "Expected to find 6 events in the repository");
	}

	@Test
	public void testFindUpcomingEvents(){
		Iterable<Event> upcomingEvents = EventServiceImpl.findUpcomingEvents();

//		Iterate through the upcomingEvents and make sure that they are in the future
		LocalDate currentDate = LocalDate.now();
		LocalTime currentTime = LocalTime.now();
		for (Event event : upcomingEvents) {
			assertTrue(event.getDate().isAfter(currentDate) || (event.getDate().isEqual(currentDate) && event.getTime().isAfter(currentTime)));
		}

	}

	@Test
	public void testFindPreviousEvents(){
		Iterable<Event> previousEvents = EventServiceImpl.findPreviousEvents();

//		Iterate through the previousEvents and make sure that they are in the past
		LocalDate currentDate = LocalDate.now();
		LocalTime currentTime = LocalTime.now();
		for (Event event : previousEvents) {
			assertTrue(event.getDate().isBefore(currentDate) || (event.getDate().isEqual(currentDate) && event.getTime().isBefore(currentTime)));
		}

	}

	@Test
	public void testFindByNameContainingIgnoreCase() {
		Iterable<Event> foundEvents = EventServiceImpl.findByNameContainingIgnoreCase("show");

//      Iterate through the foundEvents and make sure that all of them contain the search keyword
		for (Event event : foundEvents) {
			assertTrue(event.getName().toLowerCase().contains("show".toLowerCase()));
		}
	}



}

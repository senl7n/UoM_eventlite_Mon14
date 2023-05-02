package uk.ac.man.cs.eventlite.dao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
//@Disabled
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
}

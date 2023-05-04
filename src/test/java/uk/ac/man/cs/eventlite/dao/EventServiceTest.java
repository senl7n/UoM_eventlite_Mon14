package uk.ac.man.cs.eventlite.dao;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import static org.junit.jupiter.api.Assertions.assertEquals;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
//@Disabled
public class EventServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private EventService eventService;
	
	@Test
	public void testSetDescribtion() {
		Event event = new Event();
		String expected_Describtion = "Hello this is a test";
		event.setDescription("Hello this is a test");
		String actual_Describtion = event.getDescription();
		assertEquals(expected_Describtion, actual_Describtion);
	}
	// This class is here as a starter for testing any custom methods within the
	// EventService. Note: It is currently @Disabled!
}

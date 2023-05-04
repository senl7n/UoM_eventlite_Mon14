package uk.ac.man.cs.eventlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@DirtiesContext
@ActiveProfiles("test")
//@Disabled
public class VenueServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private VenueService venueService;
	
	
	@Test
    public void testLongitudeAndLatitude() {
        
        Venue venue = new Venue();
        double expectedLongitude = 45.1234;
        double expectedLatitude = 43.8989;
        venue.setLongitude(expectedLongitude);
        venue.setLatitude(expectedLatitude);
        double actualLongitude = venue.getLongitude();
        double actualLatitude = venue.getLatitude();
        assertEquals(expectedLongitude, actualLongitude, 1e-9, "The set and retrieved longitude values should be equal.");
        assertEquals(expectedLatitude, actualLatitude, 1e-9, "The set and retrieved latitude values should be equal.");

        
	}
	// This class is here as a starter for testing any custom methods within the
	// VenueService. Note: It is currently @Disabled!
	

	
	
	
	
	
	
	
	
	
	
}
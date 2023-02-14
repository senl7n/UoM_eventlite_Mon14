package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Venue;
import org.springframework.data.repository.CrudRepository;

public interface VenueRepository extends CrudRepository<Venue, Long>
{
	public long count();

	public Iterable<Venue> findAll();
	public Venue save(Venue venue);
	
}

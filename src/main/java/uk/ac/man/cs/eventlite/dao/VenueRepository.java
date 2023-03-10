package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.domain.Sort;
import uk.ac.man.cs.eventlite.entities.Venue;
import org.springframework.data.repository.CrudRepository;

public interface VenueRepository extends CrudRepository<Venue, Long>
{
	public long count();

	public Iterable<Venue> findAll(Sort sort);
	
	public Venue save(Venue venue);
	
	public Iterable<Venue> findByNameContainingIgnoreCase(String name);
}

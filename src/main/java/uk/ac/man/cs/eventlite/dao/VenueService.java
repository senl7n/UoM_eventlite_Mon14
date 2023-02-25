package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Optional;

public interface VenueService  {

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue venue);

    public Optional<Venue> findById(long id);
}
 
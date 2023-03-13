package uk.ac.man.cs.eventlite.dao;

import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.Optional;

public interface VenueService  {

	public long count();

	public Iterable<Venue> findAll();
	
	public Venue save(Venue venue);

    public Optional<Venue> findById(long id);

    public void deleteById(long id);

    public boolean update(long id, String name, int capacity, String address, String postcode);

    public boolean add(String name, int capacity, String address, String postcode);
    
    public Iterable<Venue> findByNameContainingIgnoreCase(String name);

    public boolean checkVenueOccupied(long venueId);
}
 
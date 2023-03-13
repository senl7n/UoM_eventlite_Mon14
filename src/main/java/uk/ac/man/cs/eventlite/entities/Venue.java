package uk.ac.man.cs.eventlite.entities;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Entity
@Table(name ="venues")
public class Venue {
    @Id
    @GeneratedValue
    private long id;
    @NotEmpty
    @Max(255)
    private String name;

    @Positive
    private int capacity;

    @NotEmpty
    @Max(299)
    private String address;
    private String postcode;

    public Venue() {
    }

    @OneToMany(mappedBy = "venue")
    private List<Event> events;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAddress() {
        return address;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    // get all events at this venue
    public Iterable<Event> getEventsByVenue() {
    	return events;
    }
}

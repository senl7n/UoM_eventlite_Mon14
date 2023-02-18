package uk.ac.man.cs.eventlite.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name ="venues")
public class Venue {
    @Id
    @GeneratedValue
	private long id;
    @NotEmpty
    @Max(255)
    @Column(name = "name")
	private String name;
    
    @Column(name = "capacity")
	private int capacity;
    
    @OneToMany(mappedBy = "Event")
    private Set<Event> venue = new HashSet<Event>();
    
	public Venue() {
	}

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
}

package uk.ac.man.cs.eventlite.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Entity
@Table(name ="venues")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
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
    private double longitude;
    private double latitude;

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

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}

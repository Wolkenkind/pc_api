package model;

import java.time.LocalDate;
import java.util.List;

public class Pet {
    private String name;
    private LocalDate birthDate;
    private PetType type;
    private int id;
    private int ownerId;
    private List<Visit> visits;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name.length() <= 30) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Name length less than 31 characters");
        }
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        if (id < 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
        this.ownerId = ownerId;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }
}

package model;

import java.time.LocalDate;

public class Visit {
    private LocalDate date;
    private String description;
    private int id;
    private int petId;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (!description.isEmpty() && description.length() <= 255) {
            this.description = description;
        } else {
            throw new IllegalArgumentException("Description length must be between 0 and 256 characters");
        }
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

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        if (id < 0) {
            throw new IllegalArgumentException("Id must be positive");
        }
        this.petId = petId;
    }
}

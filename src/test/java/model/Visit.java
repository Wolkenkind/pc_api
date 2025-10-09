package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class Visit {
    public static final String FIELD_DATE = "date";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ID = "id";
    public static final String FIELD_PETID = "petId";

    @JsonProperty("date")
    private LocalDate date;
    @JsonProperty("description")
    private String description;
    @JsonProperty("id")
    private int id;
    @JsonProperty("petId")
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

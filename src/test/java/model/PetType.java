package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PetType {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";

    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!name.isEmpty() && name.length() <= 80) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Name length must be between 0 and 81 characters");
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
}

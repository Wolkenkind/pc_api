package model;

public class PetType {
    private String name;
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

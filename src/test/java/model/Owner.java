package model;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Owner {
    private final static Pattern LASTNAME_PATTERN = Pattern.compile("^[\\p{L}]+([ '-][\\p{L}]+){0,2}\\.?$");
    private final static Pattern FIRSTNAME_PATTERN = Pattern.compile("^[\\p{L}]+([ '-][\\p{L}]+){0,2}$");
    private final static Pattern TELEPHONE_PATTERN = Pattern.compile("^[0-9]*$");
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String telephone;
    private int id;
    private List<Pet> pets;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        Matcher matcher = FIRSTNAME_PATTERN.matcher(firstName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("First name doesn't match the needed format (" + FIRSTNAME_PATTERN.pattern() + ")");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        Matcher matcher = LASTNAME_PATTERN.matcher(lastName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Last name doesn't match the needed format (" + LASTNAME_PATTERN.pattern() + ")");
        }
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (!address.isEmpty() && address.length() <= 255) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Address length must be between 0 and 256 characters");
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (!address.isEmpty() && address.length() <= 80) {
            this.city = city;
        } else {
            throw new IllegalArgumentException("City length must be between 0 and 81 characters");
        }
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        Matcher matcher = TELEPHONE_PATTERN.matcher(telephone);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Telephone doesn't match the needed format (" + TELEPHONE_PATTERN.pattern() + ")");
        }
        this.telephone = telephone;
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

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
}

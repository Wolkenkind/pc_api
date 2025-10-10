package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Owner {
    public static final String FIELD_FIRSTNAME = "firstName";
    public static final String FIELD_LASTNAME = "lastName";
    public static final String FIELD_ADDRESS = "address";
    public static final String FIELD_CITY = "city";
    public static final String FIELD_TELEPHONE = "telephone";
    public static final String FIELD_ID = "id";
    public static final String FIELD_PETS = "pets";

    private final static Pattern LASTNAME_PATTERN = Pattern.compile("^[\\p{L}]+([ '-][\\p{L}]+){0,2}\\.?$");
    private final static Pattern FIRSTNAME_PATTERN = Pattern.compile("^[\\p{L}]+([ '-][\\p{L}]+){0,2}$");
    private final static Pattern TELEPHONE_PATTERN = Pattern.compile("^[0-9]*$");
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("address")
    private String address;
    @JsonProperty("city")
    private String city;
    @JsonProperty("telephone")
    private String telephone;
    @JsonProperty("id")
    private int id;
    @JsonProperty("pets")
    private List<Pet> pets;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName.isEmpty() || firstName.length() > 30) {
            throw new IllegalArgumentException("First name length must be between 0 and 30 characters, given's value length is " + firstName.length());
        }
        Matcher matcher = FIRSTNAME_PATTERN.matcher(firstName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("First name doesn't match the needed format (" + FIRSTNAME_PATTERN.pattern() + ")");
        }
        this.firstName = firstName;
    }

    public void setFirstNameNoValidation(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName.isEmpty() || lastName.length() > 30) {
            throw new IllegalArgumentException("Last name length must be between 0 and 30 characters, given's value length is " + lastName.length());
        }
        Matcher matcher = LASTNAME_PATTERN.matcher(lastName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Last name doesn't match the needed format (" + LASTNAME_PATTERN.pattern() + ")");
        }
        this.lastName = lastName;
    }

    public void setLastNameNoValidation(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (!address.isEmpty() && address.length() <= 255) {
            this.address = address;
        } else {
            throw new IllegalArgumentException("Address length must be between 0 and 256 characters, given's value length is " + address.length());
        }
    }

    public void setAddressNoValidation(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (!city.isEmpty() && city.length() <= 80) {
            this.city = city;
        } else {
            throw new IllegalArgumentException("City length must be between 0 and 81 characters, given's value length is " + city.length());
        }
    }

    public void setCityNoValidation(String city) {
        this.city = city;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        if (telephone.isEmpty() || telephone.length() > 20) {
            throw new IllegalArgumentException("Telephone length must be between 0 and 20 characters, given's value length is " + telephone.length());
        }
        Matcher matcher = TELEPHONE_PATTERN.matcher(telephone);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Telephone doesn't match the needed format (" + TELEPHONE_PATTERN.pattern() + ")");
        }
        this.telephone = telephone;
    }

    public void setTelephoneNoValidation(String telephone) {
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

    public void setIdNoValidation(int id) {
        this.id = id;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
}

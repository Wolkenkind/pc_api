package mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Owner;
import model.Pet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static db.DatabaseConstants.OwnerTable.*;

public class OwnerMapper {

    private final static ObjectMapper mapper = new ObjectMapper();

    public static Owner fromKeyValues(Map<String, Object> data) {
        Owner owner = new Owner();
        Optional.ofNullable((String)data.get(Owner.FIELD_FIRSTNAME)).ifPresent(owner::setFirstName);
        Optional.ofNullable((String)data.get(Owner.FIELD_LASTNAME)).ifPresent(owner::setLastName);
        Optional.ofNullable((String)data.get(Owner.FIELD_ADDRESS)).ifPresent(owner::setAddress);
        Optional.ofNullable((String)data.get(Owner.FIELD_CITY)).ifPresent(owner::setCity);
        Optional.ofNullable((String)data.get(Owner.FIELD_TELEPHONE)).ifPresent(owner::setTelephone);
        Optional.ofNullable((int)data.get(Owner.FIELD_ID)).ifPresent(owner::setId);

        Optional.ofNullable((String)data.get(Owner.FIELD_PETS))
                .map(petsData -> {
                    try {
                        return mapper.readValue(petsData, new TypeReference<List<Pet>>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .ifPresent(owner::setPets);
        return owner;
    }

    public static Owner fromDbKeyValues(Map<String, Object> data) {
        Owner owner = new Owner();
        Optional.ofNullable((String)data.get(FIRSTNAME_COL_NAME)).ifPresent(owner::setFirstName);
        Optional.ofNullable((String)data.get(LASTNAME_COL_NAME)).ifPresent(owner::setLastName);
        Optional.ofNullable((String)data.get(ADDRESS_COL_NAME)).ifPresent(owner::setAddress);
        Optional.ofNullable((String)data.get(CITY_COL_NAME)).ifPresent(owner::setCity);
        Optional.ofNullable((String)data.get(TELEPHONE_COL_NAME)).ifPresent(owner::setTelephone);
        Optional.ofNullable((int)data.get(ID_COL_NAME)).ifPresent(owner::setId);

        return owner;
    }
}

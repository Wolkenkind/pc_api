package mapper;

import model.Owner;

import java.util.Map;
import java.util.Optional;

import static db.DatabaseConstants.OwnerTable.*;

public class OwnerMapper {

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

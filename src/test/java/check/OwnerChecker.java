package check;

import model.Owner;
import org.assertj.core.api.SoftAssertions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static db.DatabaseConstants.OwnerTable.*;
import static db.DatabaseConstants.OwnerTable.ID_COL_NAME;

public class OwnerChecker {
    private final static Set<String> READ_IGNORED_KEYS = Set.of(Owner.FIELD_PETS);

    public static void assertOwnerData(Map<String, Object> actualData, Map<String, Object> expectedData, SoftAssertions softly) {
        for (String keyName: actualData.keySet()) {
            if (!READ_IGNORED_KEYS.contains(keyName)) {
                Object actualValue = actualData.get(keyName);
                Object expectedValue = expectedData.get(keyName);
                if (expectedValue == null) {
                    softly.fail("Unexpected key '%s' : '%s'".formatted(keyName, actualValue));
                }
                softly.assertThat(actualValue).isEqualTo(expectedValue);
            }
        }
        List<Object> pets = (List<Object>) actualData.get(Owner.FIELD_PETS);
        if (!pets.isEmpty()) {
            softly.fail("Pets should be empty");
        }
    }

    public static void assertOwnerData(Map<String, Object> actualData, Owner expectedData, SoftAssertions softly) {
        softly.assertThat(actualData.get(Owner.FIELD_ADDRESS)).isEqualTo(expectedData.getAddress());
        softly.assertThat(actualData.get(Owner.FIELD_CITY)).isEqualTo(expectedData.getCity());
        softly.assertThat(actualData.get(Owner.FIELD_ID)).isEqualTo(expectedData.getId());
        softly.assertThat(actualData.get(Owner.FIELD_TELEPHONE)).isEqualTo(expectedData.getTelephone());
        softly.assertThat(actualData.get(Owner.FIELD_LASTNAME)).isEqualTo(expectedData.getLastName());
        softly.assertThat(actualData.get(Owner.FIELD_FIRSTNAME)).isEqualTo(expectedData.getFirstName());

        //TODO: Pet, PetType, Visit checkers to assert Pets field
        //softly.assertThat(actualData.get(Owner.FIELD_PETS)).isEqualTo(expectedData.getPets());
    }

    public static void assertOwnerDbData(Map<String, Object> actualDatabaseData, Map<String, Object> expectedData, SoftAssertions softly) {
        for (String columnName: actualDatabaseData.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME -> softly.assertThat(actualDatabaseData.get(FIRSTNAME_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_FIRSTNAME));
                case LASTNAME_COL_NAME -> softly.assertThat(actualDatabaseData.get(LASTNAME_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_LASTNAME));
                case ADDRESS_COL_NAME -> softly.assertThat(actualDatabaseData.get(ADDRESS_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_ADDRESS));
                case CITY_COL_NAME -> softly.assertThat(actualDatabaseData.get(CITY_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_CITY));
                case TELEPHONE_COL_NAME -> softly.assertThat(actualDatabaseData.get(TELEPHONE_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_TELEPHONE));
                case ID_COL_NAME -> softly.assertThat(actualDatabaseData.get(ID_COL_NAME)).isEqualTo(expectedData.get(Owner.FIELD_ID));
                default -> softly.fail("Unexpected database table column: " + columnName);
            }
        }
    }

    public static void checkOwnerData(Map<String, Object> actualData, Map<String, Object> expectedData) {
        final String exceptionMessage = "Problem preparing data, please check database and test";
        //rewrite for keys
        for (String columnName: actualData.keySet()) {
            switch (columnName) {
                case FIRSTNAME_COL_NAME: if (!actualData.get(FIRSTNAME_COL_NAME).equals(expectedData.get(Owner.FIELD_FIRSTNAME))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case LASTNAME_COL_NAME: if (!actualData.get(LASTNAME_COL_NAME).equals(expectedData.get(Owner.FIELD_LASTNAME))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case ADDRESS_COL_NAME: if (!actualData.get(ADDRESS_COL_NAME).equals(expectedData.get(Owner.FIELD_ADDRESS))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case CITY_COL_NAME: if (!actualData.get(CITY_COL_NAME).equals(expectedData.get(Owner.FIELD_CITY))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case TELEPHONE_COL_NAME: if (!actualData.get(TELEPHONE_COL_NAME).equals(expectedData.get(Owner.FIELD_TELEPHONE))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                case ID_COL_NAME: if (!actualData.get(ID_COL_NAME).equals(expectedData.get(Owner.FIELD_ID))) {
                    throw new IllegalStateException(exceptionMessage);
                }
                    break;
                default: throw new IllegalStateException("Unexpected database table column " + columnName);
            }
        }
    }
}

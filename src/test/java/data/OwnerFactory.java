package data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import model.Owner;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import util.DatabaseUtils;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static db.DatabaseConstants.OwnerTable.*;
import static model.Owner.*;
import static net.logstash.logback.argument.StructuredArguments.kv;

public class OwnerFactory {

    private final static Logger logger = LoggerFactory.getLogger(OwnerFactory.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    private final static Faker faker = new Faker();

    private final static String GEN_TEST_DATA = "Generated test data field";

    public static Stream<Arguments> getNegativeTestData() throws JsonProcessingException {
        Map<String, Object> randomCorrectData = getRandomOwnerTestData();

        //deep copy through serialization
        Map<String, Object> onlyLongFirstName = deepCopy(randomCorrectData);
        onlyLongFirstName.put(FIELD_FIRSTNAME, getOwnerTooLongFirstName());

        Map<String, Object> onlyShortFirstName = deepCopy(randomCorrectData);
        onlyShortFirstName.put(FIELD_FIRSTNAME, getOwnerTooShortFirstName());

        Map<String, Object> onlyInvalidFirstName = deepCopy(randomCorrectData);
        onlyInvalidFirstName.put(FIELD_FIRSTNAME, getOwnerAgainstPatternFirstName());

        Map<String, Object> onlyLongLastName = deepCopy(randomCorrectData);
        onlyLongLastName.put(FIELD_LASTNAME, getOwnerTooLongLastName());

        Map<String, Object> onlyShortLastName = deepCopy(randomCorrectData);
        onlyShortLastName.put(FIELD_LASTNAME, getOwnerTooShortLastName());

        Map<String, Object> onlyInvalidLastName = deepCopy(randomCorrectData);
        onlyInvalidLastName.put(FIELD_LASTNAME, getOwnerAgainstPatternLastName());

        Map<String, Object> onlyShortAddress = deepCopy(randomCorrectData);
        onlyShortAddress.put(FIELD_ADDRESS, getOwnerTooShortAddress());

        Map<String, Object> onlyLongAddress = deepCopy(randomCorrectData);
        onlyLongAddress.put(FIELD_ADDRESS, getOwnerTooLongAddress());

        Map<String, Object> onlyShortCity = deepCopy(randomCorrectData);
        onlyShortCity.put(FIELD_CITY, getOwnerTooShortCity());

        Map<String, Object> onlyLongCity = deepCopy(randomCorrectData);
        onlyLongCity.put(FIELD_CITY, getOwnerTooLongCity());

        Map<String, Object> onlyLongTelephone = deepCopy(randomCorrectData);
        onlyLongTelephone.put(FIELD_TELEPHONE, getOwnerTooLongTelephone());

        Map<String, Object> onlyShortTelephone = deepCopy(randomCorrectData);
        onlyShortTelephone.put(FIELD_TELEPHONE, getOwnerTooShortTelephone());

        Map<String, Object> onlyInvalidTelephone = deepCopy(randomCorrectData);
        onlyInvalidTelephone.put(FIELD_TELEPHONE, getOwnerAgainstPatternTelephone());

        return Stream.of(
                Arguments.of(
                        Named.named("Valid data with invalid first name (too long)", onlyLongFirstName), 400),
                Arguments.of(
                        Named.named("Valid data with invalid first name (too short)", onlyShortFirstName), 400),
                Arguments.of(
                        Named.named("Valid data with invalid first name (doesn't comply with pattern)", onlyInvalidFirstName), 400),
                Arguments.of(
                        Named.named("Valid data with invalid last name (too long)", onlyLongLastName), 400),
                Arguments.of(
                        Named.named("Valid data with invalid last name (too short)", onlyShortLastName), 400),
                Arguments.of(
                        Named.named("Valid data with invalid last name (doesn't comply with pattern)", onlyInvalidLastName), 400),
                Arguments.of(
                        Named.named("Valid data with invalid address (too long)", onlyLongAddress), 400),
                Arguments.of(
                        Named.named("Valid data with invalid address (too short)", onlyShortAddress), 400),
                Arguments.of(
                        Named.named("Valid data with invalid city (too long)", onlyLongCity), 400),
                Arguments.of(
                        Named.named("Valid data with invalid city (too short)", onlyShortCity), 400),
                Arguments.of(
                        Named.named("Valid data with invalid telephone (too long)", onlyLongTelephone), 400),
                Arguments.of(
                        Named.named("Valid data with invalid telephone (too short)", onlyShortTelephone), 400),
                Arguments.of(
                        Named.named("Valid data with invalid telephone (doesn't comply with pattern)", onlyInvalidTelephone), 400)
        );
    }

    private static Map<String, Object> deepCopy(Map<String, Object> map) throws JsonProcessingException {
        return mapper.readValue(mapper.writeValueAsString(map), Map.class);
    }

    public static Map<String, Object> getRandomOwnerTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_FIRSTNAME, faker.name().firstName());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_FIRSTNAME), kv("value", data.get(FIELD_FIRSTNAME)));
        data.put(FIELD_LASTNAME, faker.name().lastName());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_LASTNAME), kv("value", data.get(FIELD_LASTNAME)));
        data.put(FIELD_ADDRESS, faker.address().fullAddress());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_ADDRESS), kv("value", data.get(FIELD_ADDRESS)));
        data.put(FIELD_CITY, faker.address().city());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_CITY), kv("value", data.get(FIELD_CITY)));
        data.put(FIELD_TELEPHONE, faker.number().digits(10));
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_TELEPHONE), kv("value", data.get(FIELD_TELEPHONE)));
        return data;
    }

    public static Map<String, Object> getOwnerTestInvalidData() {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_FIRSTNAME, getOwnerAgainstPatternFirstName());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_FIRSTNAME), kv("value", data.get(FIELD_FIRSTNAME)));
        data.put(FIELD_LASTNAME, getOwnerTooLongLastName());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_LASTNAME), kv("value", data.get(FIELD_LASTNAME)));
        data.put(FIELD_ADDRESS, getOwnerTooLongAddress());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_ADDRESS), kv("value", data.get(FIELD_ADDRESS)));
        data.put(FIELD_CITY, getOwnerTooShortCity());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_CITY), kv("value", data.get(FIELD_CITY)));
        data.put(FIELD_TELEPHONE, getOwnerTooLongTelephone());
        logger.debug(GEN_TEST_DATA, kv("field", FIELD_TELEPHONE), kv("value", data.get(FIELD_TELEPHONE)));
        return data;
    }

    public static String getOwnerTooShortFirstName() {
        return "";
    }

    public static String getOwnerTooLongFirstName() {
        return "Firstname longerthanthirtycharacters";
    }

    public static String getOwnerAgainstPatternFirstName() {
        return "John'john'john'johnny";
    }

    public static String getOwnerTooShortLastName() {
        return "";
    }

    public static String getOwnerTooLongLastName() {
        return "Lastname longerthanthirtycharacters";
    }

    public static String getOwnerAgainstPatternLastName() {
        return "Doe-doe-doe-doe-doe";
    }

    public static String getOwnerTooShortAddress() {
        return "";
    }

    public static String getOwnerTooLongAddress() {
        return "Longest-Cityname-longer-than-Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch, Taumatawhakatangihangakoauauotamateaturipukakapikimaungahoronukupokaiwhenuakitanatahu str. 4729805379023485643090853840234785639056483024820438320, bld. 2147483647, fl. 13";
    }

    public static String getOwnerTooShortCity() {
        return "";
    }

    public static String getOwnerTooLongCity() {
        return "Longest-Cityname-longer-than-Llanfairpwllgwyngyllgogerychwyrndrobwllllantysiliogogogoch";
    }

    public static String getOwnerTooShortTelephone() {
        return "";
    }

    public static String getOwnerTooLongTelephone() {
        return "1234567890123456789012345";
    }

    public static String getOwnerAgainstPatternTelephone() {
        return "1234567890*/-a";
    }

    public static int getOwnerNegativeId() {
        return -42;
    }

    public static Map<String, Object> getOwnerDataFromDatabase(int ownerId) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        String sql = "SELECT * FROM owners WHERE id = ?";
        Map<String, Object> res;

        try {
            res = template.queryForMap(sql, ownerId);
        } catch (EmptyResultDataAccessException emptyException) {
            return Map.of();
        }

        return res;
    }

    public static int createOwnerInDatabase(Map<String, Object> ownerData) {
        JdbcTemplate template = DatabaseUtils.createTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO owners (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?)"
                .formatted(FIRSTNAME_COL_NAME, LASTNAME_COL_NAME, ADDRESS_COL_NAME, CITY_COL_NAME, TELEPHONE_COL_NAME);
        template.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, (String) ownerData.get(Owner.FIELD_FIRSTNAME));
            ps.setString(2, (String) ownerData.get(Owner.FIELD_LASTNAME));
            ps.setString(3, (String) ownerData.get(Owner.FIELD_ADDRESS));
            ps.setString(4, (String) ownerData.get(Owner.FIELD_CITY));
            ps.setString(5, (String) ownerData.get(Owner.FIELD_TELEPHONE));
            return ps;
        }, keyHolder);

        return keyHolder.getKeyAs(Integer.class);
    }
}

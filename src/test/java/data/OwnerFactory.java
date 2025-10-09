package data;

import com.github.javafaker.Faker;
import model.Owner;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import util.DatabaseUtils;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import static model.Owner.*;
import static db.DatabaseConstants.OwnerTable.*;

public class OwnerFactory {

    private final static Logger logger = LoggerFactory.getLogger(OwnerFactory.class);

    private final static Faker faker = new Faker();

    public static Map<String, Object> getRandomOwnerTestData() {
        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_FIRSTNAME, faker.name().firstName());
        logger.debug(() -> "Generated following KV: '" + FIELD_FIRSTNAME + "' = '" + data.get(FIELD_FIRSTNAME) + "'");
        data.put(FIELD_LASTNAME, faker.name().lastName());
        logger.debug(() -> "Generated following KV: '" + FIELD_LASTNAME + "' = '" + data.get(FIELD_LASTNAME) + "'");
        data.put(FIELD_ADDRESS, faker.address().fullAddress());
        logger.debug(() -> "Generated following KV: '" + FIELD_ADDRESS + "' = '" + data.get(FIELD_ADDRESS) + "'");
        data.put(FIELD_CITY, faker.address().city());
        logger.debug(() -> "Generated following KV: '" + FIELD_CITY + "' = '" + data.get(FIELD_CITY) + "'");
        data.put(FIELD_TELEPHONE, faker.number().digits(10));
        logger.debug(() -> "Generated following KV: '" + FIELD_TELEPHONE + "' = '" + data.get(FIELD_TELEPHONE) + "'");
        return data;
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

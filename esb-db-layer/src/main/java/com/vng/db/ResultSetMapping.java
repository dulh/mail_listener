package com.vng.db;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 * The interface for mapping between a record in result set and object.
 * </p>
 * @author bac.nh <br>
 * @version 1.0 <br>
 */

public interface ResultSetMapping<T> {
    /**
     * <p>
     * Map a record in result set into a object.
     * </p>
     * @param <T>
     * @param rs ResultSet
     * @param rowNum long
     * @return T
     * @throws SQLException
     */
    public T mapRow(ResultSet rs, long rowNum) throws SQLException;
}

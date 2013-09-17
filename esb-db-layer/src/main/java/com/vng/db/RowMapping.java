package com.vng.db;
import java.util.Map;

/**
 * <p>
 * The interface for mapping columns of a specific table.
 * </p>
 * @author bac.nh <br>
 * @version 1.0 <br>
 */

public interface RowMapping {
    /**
     * <p>
     * Get mapping of DB columns and data object fields.
     * </p>
     * @return Map<String, String>
     */
    public Map<String, String> mapColumns();
}

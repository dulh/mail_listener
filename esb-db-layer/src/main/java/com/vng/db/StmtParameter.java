package com.vng.db;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>
 * The interface for binding parameters.
 * </p>
 * @author bac.nh <br>
 * @version 1.0 <br>
 */

public interface StmtParameter {
    /**
     * <p>
     * Bind parameters for a specific prepared statement.
     * </p>
     * @param pStmt PreparedStatement
     * @throws SQLException
     */
    public void bind(PreparedStatement pStmt) throws SQLException;
}

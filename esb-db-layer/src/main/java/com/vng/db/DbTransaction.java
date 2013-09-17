package com.vng.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>
 * Class description.
 * </p>
 * @author bac.nh <br>
 * @version Class version <br>
 * @see (Related item)
 */

public class DbTransaction {
    private static final Logger logger = LogManager.getLogger(DbTransaction.class);
    
    private final Connection conn;
    private final String dsName;
    /**
     * <p>
     * Constructor. <br/>
     * Note: The constructor also begins transaction.
     * </p>
     * @param dsName String
     * @throws SQLException
     */
    public DbTransaction(final String dsName) throws SQLException {
        this(dsName, false);
    }
    /**
     * <p>
     * Constructor.
     * </p>
     * @param dsName String
     * @param beginTran True to begin transaction, otherwise non-begin transaction
     * @throws SQLException
     */
    public DbTransaction(final String dsName, final boolean beginTran) throws SQLException {
        this.dsName = dsName;
        conn = ConnectionPool.getConnection(this.dsName);
        if (beginTran) {
            conn.setAutoCommit(false);
        }
    }
    /**
     * <p>
     * Roll back transaction.
     * </p>
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        logger.info("Rollback connection for data source " + dsName);
        conn.rollback();        
    }
    /**
     * <p>
     * Commit transaction.
     * </p>
     * @throws SQLException
     */
    public void commit() throws SQLException {
        logger.debug("Commit connection for data source " + dsName);
        conn.commit();
    }
    /**
     * <p>
     * Get connection.
     * </p>
     * @return java.sql.Connection
     */
    public Connection getConnection() {
        return conn;
    }
    /**
     * <p>
     * Close connection.
     * </p>
     * @throws SQLException
     * @see (Related item)
     */
    public void closeConnection() throws SQLException {
        logger.debug("Close connection for data source " + dsName);
        conn.close();
    }
    /**
     * End the transaction.
     * 
     * @param success true if the transaction success, otherwise false
     * @return true if end the transaction success, otherwise false
     */
    public boolean endTran(final boolean success) {
        final String method = String.format("endTran(dsName=%s, success=%s)", dsName, success);
        logger.debug(method + "<--BEGIN");
        boolean result = false;
        try {
            if (success) {
                conn.commit();
            } else {
                conn.rollback();
            }
            result = true;
        } catch (Exception ex) {
            logger.error("End transaction error", ex);
        } finally {
            if (conn != null) {
                try {
                    logger.debug("Close connection");
                    conn.close();
                } catch (SQLException sqlEx) {}
            }
            logger.debug(method + "-->END");
        }
        return result;
    }
    /**
     * Get the dsName attribute.
     * @return the dsName
     */
    public final String getDsName() {
        return dsName;
    }
    
}

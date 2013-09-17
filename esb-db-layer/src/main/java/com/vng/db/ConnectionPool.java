package com.vng.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

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

public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger(ConnectionPool.class);
    /**
     * The map contains pools.
     */
    private static final Map<String, DataSource> mapPools = new HashMap<String, DataSource>();
    /**
     * Default key for getting pool.
     */
    private static String defaultKey = null;
    /**
     * <p>
     * Add data source.
     * </p>
     * @param key String
     * @param ds DataSource
     */
    public static void addDataSource(final String key, final DataSource ds) {
        final String method = String.format("addDataSource(key=%s)", key);
        logger.info(method + DbConstants.BEGIN_METHOD);
        synchronized (mapPools) {
            mapPools.put(key, ds);
            if (defaultKey == null) {
                defaultKey = key;
            }
        }
        logger.info(method + DbConstants.END_METHOD);
    }
    
    /**
     * Checks if is data source exist.
     *
     * @param ds the data source
     * @return true, if is data source exist
     */
    public static boolean isDataSourceExist(final String ds) {
        return mapPools.containsKey(ds);
    }
    
    /**
     * Get the defaultKey attribute.
     * @return the defaultKey
     */
    public static String getDefaultKey() {
        return defaultKey;
    }
    /**
     * Set the defaultKey attribute.
     * @param defaultKey the defaultKey to set
     */
    public static void setDefaultKey(String defaultKey) {
        ConnectionPool.defaultKey = defaultKey;
    }
    /**
     * <p>
     * Get connection of default pool.
     * </p>
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(defaultKey);
    }
    /**
     * <p>
     * Get connection of a specific data source.
     * </p>
     * @param dsKey String
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection(final String dsKey) throws SQLException {
        final DataSource ds = mapPools.get(dsKey);
        if (ds == null) {
            throw new SQLException("Not found data source " + dsKey);
        }
        final Connection conn = ds.getConnection();
//        conn.setAutoCommit(true);
        return conn;
    }
    /**
     * <p>
     * Destroy connection pool.
     * </p>
     */
    public static void destroy() {
        for (Entry<String, DataSource> e : mapPools.entrySet()) {
            try {
                e.setValue(null);
            } catch (Exception sqlEx) {
                logger.error("", sqlEx);
            }
        }
    }
}

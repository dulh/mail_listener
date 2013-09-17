package com.vng.db;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>
 * Database utilities.
 * </p>
 * @author bac.nh <br>
 * @version 1.0 <br>
 */

public class DbUtils {
    private static final Logger logger = LogManager.getLogger(DbUtils.class);
    /**
     * <p>
     * Close a connection.
     * </p>
     * @param conn Connection
     */
    public static void closeConnection(final Connection conn) {
        try {
            if (conn == null)
                return;
            conn.close();
        } catch (SQLException e) {
            logger.error("Close connection error", e);
        }
    }
    /**
     * <p>
     * Close a statement.
     * </p>
     * @param stmt Statement
     */
    public static void closeStatement(final Statement stmt) {
        try {
            if (stmt == null)
                return;
            stmt.close();
        } catch (SQLException e) {
            logger.error("Close statement error", e);
        }
    }
    /**
     * <p>
     * Close a result set.
     * </p>
     * @param rs ResultSet
     */
    public static void closeResultSet(final ResultSet rs) {
        try {
            if (rs == null)
                return;
            rs.close();
        } catch (SQLException e) {
            logger.error("Close resultset error", e);
        }
    }
    /**
     * <p>
     * Begin a transaction.
     * </p>
     * @param conn Connection
     * @throws SQLException
     */
    public static void beginTrans(final Connection conn) throws SQLException {
        if (conn == null) {
            return;
        }
        conn.setAutoCommit(false);
    }
    /**
     * <p>
     * Commit a transaction.
     * </p>
     * @param conn Connection
     * @throws SQLException
     */
    public static void commit(final Connection conn) throws SQLException {
        if (conn == null) {
            return;
        }
        conn.setAutoCommit(true);
        conn.commit();
    }
    /**
     * <p>
     * Rollback a transaction.
     * </p>
     * @param conn Connection
     * @throws SQLException
     */
    public static void rollback(final Connection conn) throws SQLException {
        if (conn == null) {
            return;
        }
        conn.rollback();
    }
    
    public static Map<String, Object> getValues(final RowMapping src) {
        final Map<String, String> columns = src.mapColumns();
        if (columns == null || columns.isEmpty()) {
            throw new RuntimeException("Not found mapping for " + src.getClass());
        }
        final Map<String, Object> values = new HashMap<String, Object>();
        for (Entry<String, String> e : columns.entrySet()) {
            final String dbCol = e.getKey();
            final String objFld = e.getValue();
            try {
                final Field fld = src.getClass().getDeclaredField(objFld);
                fld.setAccessible(true);
                values.put(dbCol, fld.get(src));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return values;
    }
    
    public static void setValues(final ResultSet src, final RowMapping target) throws SQLException {
        final Map<String, String> columns = target.mapColumns();
        for (Entry<String, String> e : columns.entrySet()) {
            final String dbCol = e.getKey();
            final String objFld = e.getValue();
            try {
                final Field fld = target.getClass().getDeclaredField(objFld);
                fld.setAccessible(true);
                final Type fldType = fld.getType();
                Object val = null;
                if (fldType.equals(java.sql.Date.class)) {
                    val = src.getDate(dbCol);
                } else if (fldType.equals(java.sql.Timestamp.class)) {
                    val = src.getTimestamp(dbCol);
                } else if (fldType.equals(int.class)) {
                    val = src.getInt(dbCol);
                } else if (fldType.equals(double.class)) {
                    val = src.getDouble(dbCol);
                } else if (fldType.equals(long.class)) {
                    val = src.getLong(dbCol);
                } else if (fldType.equals(boolean.class)) {
                    val = src.getBoolean(dbCol);
		} else if (fldType.equals(short.class)) {
                    val = src.getShort(dbCol);
		} else if (fldType.equals(byte.class)) {
                    val = src.getByte(dbCol);
		} else if (fldType.equals(float.class)) {
                    val = src.getFloat(dbCol);
                } else if (fldType.equals(String.class)) {
                    val = src.getString(dbCol);
                } else if (fldType.equals(java.util.Date.class)) {
                    final Date dbDate = src.getDate(dbCol);
                    if (dbDate != null) {
                        val = new java.util.Date(dbDate.getTime());
                    }
                } else {
                    throw new RuntimeException("Not support type " + fldType);
                }
                fld.set(target, val);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public static String buildFieldsForSelect(final RowMapping rowMapping) {
        final Map<String, String> map = rowMapping.mapColumns();
        final StringBuilder fields = new StringBuilder();
        for (Entry<String, String> e : map.entrySet()) {
            if (fields.length() == 0) {
                fields.append(e.getKey());
            } else {
                fields.append(", ").append(e.getKey());
            }
        }
        fields.append(" ");
        return fields.toString();
    }
    
}

package com.vng.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class BaseDao {
	protected static final Logger logger = LogManager.getLogger(BaseDao.class);
	private DbTransaction tran;
	private String dataSource;

	public void registerTran(final DbTransaction tran) {
		this.tran = tran;
	}

	public DbTransaction getTran() {
		return tran;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * <p>
	 * Execute a query on a specific connection.
	 * </p>
	 * 
	 * @param <T>
	 * @param conn
	 *            Connection
	 * @param sql
	 *            String
	 * @param stmtParams
	 *            StmtParameter - It maybe null
	 * @param resultMapping
	 *            ResultSetMapping
	 * @return Collection
	 * @throws SQLException
	 */
	public final <T> List<T> query(final Connection conn, final String sql,
			final StmtParameter stmtParams,
			final ResultSetMapping<T> resultMapping) throws SQLException {
		final long start = System.currentTimeMillis();
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql);
			if (stmtParams != null) {
				stmtParams.bind(pStmt);
			}
			if (pStmt.execute()) {
				rs = pStmt.getResultSet();
				final List<T> list = new ArrayList<T>();
				int rowNum = 1;
				while (rs.next()) {
					list.add(resultMapping.mapRow(rs, rowNum++));
				}
				return list;
			}
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeResultSet(rs);
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Finish query in %d(ms)",
						System.currentTimeMillis() - start));
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Execute a query on a specific connection of default pool.
	 * </p>
	 * 
	 * @param <T>
	 * @param sql
	 *            String
	 * @param stmtParams
	 *            StmtParameter - It maybe null
	 * @param resultMapping
	 *            ResultSetMapping
	 * @return Collection
	 * @throws SQLException
	 */
	public final <T> List<T> query(final String sql,
			final StmtParameter stmtParams,
			final ResultSetMapping<T> resultMapping) throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return query(conn, sql, stmtParams, resultMapping);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	public final boolean isExits(final String sql,
			final StmtParameter stmtParams) throws SQLException {
		Connection conn = null;
		PreparedStatement prStm = null;
		boolean result = false;

		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}

			if (conn != null) {
				prStm = conn.prepareStatement(sql);
				if (stmtParams != null) {
	                stmtParams.bind(prStm);
	            }
				ResultSet rs = prStm.executeQuery();
				if (rs != null && rs.next()) {
					result = true;
				}
			}

		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
		return result;
	}

	public final int getColumn(final String sql, String columnName,
			final StmtParameter stmtParams) throws SQLException {
		Connection conn = null;
		PreparedStatement prStm = null;
		int result = 0;

		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}

			if (conn != null) {
				prStm = conn.prepareStatement(sql);
				ResultSet rs = prStm.executeQuery();
				if (rs != null && rs.next()) {
					result = rs.getInt(columnName);
				}
			}

		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
		return result;
	}

	/**
	 * Execute query statement on a specific connection then return only one
	 * row.
	 * 
	 * @param <T>
	 *            T
	 * @param conn
	 *            java.sql.Connection
	 * @param sql
	 *            String
	 * @param stmtParams
	 *            StmtParameter - It maybe null
	 * @param resultMapping
	 *            ResultSetMapping
	 * @return T
	 * @throws SQLException
	 *             if access database error
	 */
	public final <T> T querySingleRow(final Connection conn, final String sql,
			final StmtParameter stmtParams,
			final ResultSetMapping<T> resultMapping) throws SQLException {
		final long start = System.currentTimeMillis();
		T retValue = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql);
			if (stmtParams != null) {
				stmtParams.bind(pStmt);
			}
			if (pStmt.execute()) {
				rs = pStmt.getResultSet();
				if (rs.next()) {
					retValue = resultMapping.mapRow(rs, 1);
				}
			}
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeResultSet(rs);
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Finish query single row in %d(ms)",
						System.currentTimeMillis() - start));
			}
		}
		return retValue;
	}

	/**
	 * Execute query statement on a specific connection of default pool then
	 * return only one row.
	 * 
	 * @param <T>
	 *            T
	 * @param sql
	 *            String
	 * @param stmtParams
	 *            StmtParameter - It maybe null
	 * @param resultMapping
	 *            ResultSetMapping
	 * @return T
	 * @throws SQLException
	 *             if access database error
	 */
	public final <T> T querySingleRow(final String sql,
			final StmtParameter stmtParams,
			final ResultSetMapping<T> resultMapping) throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return querySingleRow(conn, sql, stmtParams, resultMapping);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	/**
	 * Execute DELETE/UPDATE/INSERT statement on a specific connection.
	 * 
	 * @param conn
	 *            java.sql.Connection
	 * @param sql
	 *            String
	 * @param stmtParameter
	 *            StmtParameter
	 * @return int - Number of effected row
	 * @throws SQLException
	 */
	public final int update(final Connection conn, final String sql,
			final StmtParameter stmtParameter) throws SQLException {
		PreparedStatement pStmt = null;
		int num = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql);
			if (stmtParameter != null) {
				stmtParameter.bind(pStmt);
			}
			num = pStmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeStatement(pStmt);
		}
		return num;
	}

	/**
	 * Execute DELETE/UPDATE/INSERT statement on a specific connection of
	 * default pool.
	 * 
	 * @param sql
	 *            String
	 * @param stmtParameter
	 *            StmtParameter
	 * @return int - Number of effected row
	 * @throws SQLException
	 */
	public final int update(final String sql, final StmtParameter stmtParameter)
			throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return update(conn, sql, stmtParameter);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	/**
	 * Execute batches of statements on a specific connection.
	 * 
	 * @param conn
	 *            java.sql.Connection
	 * @param sql
	 *            String
	 * @param stmtParameter
	 *            StmtParameter
	 * @return int - Number of effected row
	 * @throws SQLException
	 */
	public final int updateBatch(final Connection conn, final String sql,
			final List<StmtParameter> stmtParameters) throws SQLException {
		final long start = System.currentTimeMillis();
		PreparedStatement pStmt = null;
		int num = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql);
			if (stmtParameters != null) {
				for (StmtParameter stmtParam : stmtParameters) {
					stmtParam.bind(pStmt);
					pStmt.addBatch();
				}
			}
			pStmt.executeBatch();
			num = pStmt.getUpdateCount();
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format(
						"Execute batch %d records in %d(ms)", num,
						System.currentTimeMillis() - start));
			}
		}
		return num;
	}

	/**
	 * Execute batches of statements on a specific connection of default pool.
	 * 
	 * @param sql
	 *            String
	 * @param stmtParameter
	 *            StmtParameter
	 * @return int - Number of effected row
	 * @throws SQLException
	 */
	public final int updateBatch(final String sql,
			final List<StmtParameter> stmtParameters) throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return updateBatch(conn, sql, stmtParameters);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	/**
	 * <p>
	 * Add a row into a specific table.
	 * </p>
	 * 
	 * @param conn
	 *            Connection
	 * @param tableName
	 *            String
	 * @param rowMapping
	 *            RowMapping
	 * @return int - Number of added record
	 * @throws SQLException
	 */
	public final int addRow(final Connection conn, final String tableName,
			final RowMapping rowMapping) throws SQLException {
		final long start = System.currentTimeMillis();
		final StringBuilder sql = new StringBuilder();
		PreparedStatement pStmt = null;
		int num = 0;
		try {
			final Map<String, Object> map = DbUtils.getValues(rowMapping);
			final StringBuilder columns = new StringBuilder();
			final StringBuilder bindParams = new StringBuilder();
			final List<Object> values = new ArrayList<Object>();
			for (Entry<String, Object> e : map.entrySet()) {
				if (columns.length() == 0) {
					columns.append(e.getKey());
					bindParams.append("?");
				} else {
					columns.append(", ").append(e.getKey());
					bindParams.append(", ?");
				}
				values.add(e.getValue());
			}
			sql.append("insert into ").append(tableName);
			sql.append("(").append(columns).append(") values(")
					.append(bindParams).append(")");
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql.toString());
			int index = 1;
			for (Object val : values) {
				pStmt.setObject(index++, val);
			}
			num = pStmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL Error>>" + sql.toString());
			throw e;
		} finally {
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Add %d record in %d(ms)", num,
						System.currentTimeMillis() - start));
			}
		}
		return num;
	}

	/**
	 * <p>
	 * Add a row into a specific table.
	 * </p>
	 * 
	 * @param tableName
	 *            String
	 * @param rowMapping
	 *            RowMapping
	 * @return int - Number of added record
	 * @throws SQLException
	 */
	public final int addRow(final String tableName, final RowMapping rowMapping)
			throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) { // Transaction
				conn = tran.getConnection();
			} else { // Auto commit
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return addRow(conn, tableName, rowMapping);
		} finally {
			if (tran == null) { // Auto commit => Close connection
				DbUtils.closeConnection(conn);
			}
		}
	}

	public final int insertBatch(final Connection conn, final String sql,
			final List<StmtParameter> stmtParameters) throws SQLException {
		final long start = System.currentTimeMillis();
		PreparedStatement pStmt = null;
		int num = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql);
			if (stmtParameters != null) {
				for (StmtParameter stmtParam : stmtParameters) {
					stmtParam.bind(pStmt);
					pStmt.addBatch();
				}
			}
			pStmt.executeBatch();
			num = pStmt.getUpdateCount();
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format(
						"Execute batch %d records in %d(ms)", num,
						System.currentTimeMillis() - start));
			}
		}
		return num;
	}

	public final int insertBatch(final String sql,
			final List<StmtParameter> stmtParameters) throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return insertBatch(conn, sql, stmtParameters);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	public final int insert(final Connection conn, final String sql,
			final StmtParameter stmtParameter) throws SQLException {
		final long start = System.currentTimeMillis();
		PreparedStatement pStmt = null;
		int num = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			pStmt = conn.prepareStatement(sql);
			if (stmtParameter != null) {
				stmtParameter.bind(pStmt);
			}
			pStmt.executeUpdate();
			num = pStmt.getUpdateCount();
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Execute %d records in %d(ms)", num,
						System.currentTimeMillis() - start));
			}
		}
		return num;
	}

	public final int insert(final String sql, final StmtParameter stmtParameter)
			throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return insert(conn, sql, stmtParameter);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	public final int insert(final String sql,
			final StmtParameter stmtParameter, final boolean returnInsertId)
			throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return insert(conn, sql, stmtParameter, returnInsertId);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}
	}

	public final int insert(final Connection conn, final String sql,
			final StmtParameter stmtParameter, final boolean returnInsertId)
			throws SQLException {
		final long start = System.currentTimeMillis();
		PreparedStatement pStmt = null;
		int num = 0;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			if (returnInsertId) {
				pStmt = conn.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS);
			} else {
				pStmt = conn.prepareStatement(sql);
			}

			if (stmtParameter != null) {
				stmtParameter.bind(pStmt);
			}
			pStmt.executeUpdate();
			if (returnInsertId) {
				final ResultSet rs = pStmt.getGeneratedKeys();
				rs.next();
				num = rs.getInt(1);
			} else {
				num = pStmt.getUpdateCount();
			}
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeStatement(pStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Execute %d records in %d(ms)", num,
						System.currentTimeMillis() - start));
			}
		}
		return num;
	}

	public final Date mapDateColumn(final ResultSet rs,
			final String columnName, final SimpleDateFormat sdf) {
		try {
			final String createdDate = rs.getString(columnName);
			if (!rs.wasNull()) {
				return sdf.parse(createdDate);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public final long mapLongColumn(final ResultSet rs, final String columnName) {
		try {
			final String longString = rs.getString(columnName);
			if (!rs.wasNull()) {
				return Long.parseLong(longString);
			} else {
				return Long.MAX_VALUE;
			}
		} catch (Exception e) {
			return 0;
		}
	}

	public final int mapIntColumn(final ResultSet rs, final String columnName) {
		try {
			final String intString = rs.getString(columnName);
			if (!rs.wasNull()) {
				return Integer.parseInt(intString);
			} else {
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}
	}

	public final <T> List<T> callStoreProdure(final Connection conn,
			final String sql, final CallableStatementParameter cStateParam,
			final ResultSetMapping<T> resultMapping, final int cursorIndex)
			throws SQLException {
		final long start = System.currentTimeMillis();
		CallableStatement cStmt = null;
		ResultSet rs = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			cStmt = conn.prepareCall(sql);
			if (cStateParam != null) {
				cStateParam.bind(cStmt);
			}
			cStmt.execute();
			// if (cStmt.execute()) {
			rs = (ResultSet) cStmt.getObject(cursorIndex);
			final List<T> list = new ArrayList<T>();
			int rowNum = 1;
			while (rs.next()) {
				list.add(resultMapping.mapRow(rs, rowNum++));
			}
			return list;
			// }
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeResultSet(rs);
			DbUtils.closeStatement(cStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Finish query in %d(ms)",
						System.currentTimeMillis() - start));
			}
		}
		// return null;

	}

	public final <T> List<T> callStoreProdure(final String sql,
			final CallableStatementParameter cStateParam,
			final ResultSetMapping<T> resultMapping, final int cursorIndex)
			throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return callStoreProdure(conn, sql, cStateParam, resultMapping,
					cursorIndex);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final List<List> callStoreProdure(final Connection conn,
			final String sql, final CallableStatementParameter cStateParam,
			final List<ResultSetMapping> listMapping,
			final List<Integer> listCursorIndex) throws SQLException {
		final long start = System.currentTimeMillis();
		CallableStatement cStmt = null;
		ResultSet rs = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			cStmt = conn.prepareCall(sql);
			if (cStateParam != null) {
				cStateParam.bind(cStmt);
			}
			cStmt.execute();
			// if (cStmt.execute()) {
			final List<List> list = new ArrayList<List>();
			if (listCursorIndex != null && listCursorIndex.size() > 0
					&& listMapping != null && listMapping.size() > 0
					&& listCursorIndex.size() == listMapping.size()) {
				for (int i = 0; i < listCursorIndex.size(); i++) {

					List listRs = new ArrayList();
					rs = (ResultSet) cStmt.getObject(listCursorIndex.get(i));
					int rowNum = 1;
					while (rs.next()) {
						listRs.add(listMapping.get(i).mapRow(rs, rowNum++));
					}

					list.add(listRs);
					DbUtils.closeResultSet(rs);
				}
			}

			return list;
			// }
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeResultSet(rs);
			DbUtils.closeStatement(cStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Finish query in %d(ms)",
						System.currentTimeMillis() - start));
			}
		}
		// return null;
	}

	@SuppressWarnings("rawtypes")
	public final List<List> callStoreProdure(final String sql,
			final CallableStatementParameter cStateParam,
			final List<ResultSetMapping> listMapping,
			final List<Integer> listCursorIndex) throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return callStoreProdure(conn, sql, cStateParam, listMapping,
					listCursorIndex);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}

	}

	/**
     * TODO: Move two method to BaseDao later
     * Call sql server store procedure.
     *
     * @param sql the sql
     * @param cStateParam the c state param
     * @param resultMapping the result mapping
     * @return the list
     * @throws SQLException the sQL exception
     */
    public final <T> List<T> callStoreProcedureWithoutCursor(final String sql, final CallableStatementParameter cStateParam,
            final ResultSetMapping<T> resultMapping) throws SQLException {
        Connection conn = null;
        try {
            if (getTran() != null) {
                conn = getTran().getConnection();
            } else {
                conn = ConnectionPool.getConnection(this.getDataSource());
            }
            return callStoreProdureWithoutCursor(conn, sql, cStateParam, resultMapping);
        } finally {
            if (getTran() == null) {
                DbUtils.closeConnection(conn);
            }
        }

    }
    
    public final <T> List<T> callStoreProdureWithoutCursor(final Connection conn, final String sql, 
            final CallableStatementParameter cStateParam,
            final ResultSetMapping<T> resultMapping) throws SQLException {
        final long start = System.currentTimeMillis();
        CallableStatement cStmt = null;
        ResultSet rs = null;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("SQL>>" + sql);
            }
            cStmt = conn.prepareCall(sql);
            if (cStateParam != null) {
                cStateParam.bind(cStmt);
            }

            rs = (ResultSet) cStmt.executeQuery();
            final List<T> list = new ArrayList<T>();
            int rowNum = 1;
            while (rs.next()) {
                list.add(resultMapping.mapRow(rs, rowNum++));
            }
            return list;
        } catch (SQLException e) {
            logger.error("Error on SQLserver: " + sql);
            throw e;
        } finally {
            DbUtils.closeResultSet(rs);
            DbUtils.closeStatement(cStmt);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Finish query in %d(ms)", System.currentTimeMillis() - start));
            }
        }
    }
    
	public final int executeStoreProcedure(final Connection conn,
			final String sql, final CallableStatementParameter cStateParam,
			int resultIndex, int errorMsg) throws SQLException {
		final long start = System.currentTimeMillis();
		CallableStatement cStmt = null;
		int rs = 0;
		String error = "";
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("SQL>>" + sql);
			}
			cStmt = conn.prepareCall(sql);
			if (cStateParam != null) {
				cStateParam.bind(cStmt);
			}
			cStmt.execute();
			rs = cStmt.getInt(resultIndex);
			error = cStmt.getString(errorMsg);
			if (rs <= 0) {
				logger.error("Error return from Store: " + error);
			}

			return rs;
		} catch (SQLException e) {
			logger.error("Error on SQL: " + sql);
			throw e;
		} finally {
			DbUtils.closeStatement(cStmt);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Finish query in %d(ms)",
						System.currentTimeMillis() - start));
			}
		}
	}

	public final int executeStoreProcedure(final String sql,
			final CallableStatementParameter cStateParam,
			final int resultIndex, final int errorMsg) throws SQLException {
		Connection conn = null;
		try {
			if (tran != null) {
				conn = tran.getConnection();
			} else {
				conn = ConnectionPool.getConnection(this.dataSource);
			}
			return executeStoreProcedure(conn, sql, cStateParam, resultIndex,
					errorMsg);
		} finally {
			if (tran == null) {
				DbUtils.closeConnection(conn);
			}
		}

	}

}

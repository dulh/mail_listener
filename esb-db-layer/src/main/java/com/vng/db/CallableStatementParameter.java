package com.vng.db;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface CallableStatementParameter {
	public void bind(CallableStatement cStatement) throws SQLException;
}

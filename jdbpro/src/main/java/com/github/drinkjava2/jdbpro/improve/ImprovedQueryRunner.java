/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro.improve;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.DbProLogger;
import com.github.drinkjava2.jdbpro.DbRuntimeException;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * ImprovedQueryRunner made below improvements compare DbUtils's QueryRunner:
 * 
 * 
 * 1) Override close() and prepareConnection() method of QueryRunner, use a
 * ConnectionManager to manage connection, ConnectionManager can get connection
 * from DataSource or ThreadLocal or some other 3rd party tools like Spring.
 * <br/>
 * 2) Override some methods to add logger support 3) Override some
 * execute/update methods to support batch operation
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ImprovedQueryRunner extends QueryRunner {
	/**
	 * The ConnectionManager determine how to get and release connections from
	 * DataSource or ThreadLocal or container
	 */
	protected ConnectionManager cm;
	protected Boolean allowShowSQL = false;
	private static final DbProLogger staticlogger = DbProLogger.getLog(ImprovedQueryRunner.class);
	protected DbProLogger logger = staticlogger;
	protected boolean batchEnable = false;
	protected Integer batchSize = 100;
	protected ThreadLocal<ArrayList<Object[]>> sqlBatch = new ThreadLocal<ArrayList<Object[]>>() {
		@Override
		protected ArrayList<Object[]> initialValue() {
			return new ArrayList<Object[]>();
		}
	};

	public ImprovedQueryRunner() {
		super();
	}

	public ImprovedQueryRunner(DataSource ds) {
		super(ds);
	}

	public ImprovedQueryRunner(DataSource ds, ConnectionManager cm) {
		super(ds);
		this.cm = cm;
	}

	@Override
	public void close(Connection conn) throws SQLException {
		if (cm == null)
			super.close(conn);
		else
			cm.releaseConnection(conn, this.getDataSource());
	}

	@Override
	public Connection prepareConnection() throws SQLException {
		if (cm == null)
			return super.prepareConnection();
		else
			return cm.getConnection(this.getDataSource());
	}

	@Override
	protected CallableStatement prepareCall(Connection conn, String sql) throws SQLException {
		if (this.getAllowShowSQL())
			logger.info("SQL: " + sql);
		return super.prepareCall(conn, sql);
	}

	@Override
	protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
		if (this.getAllowShowSQL())
			logger.info(formatSql(sql));
		return super.prepareStatement(conn, sql);
	}

	@Override
	public void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {
		if (this.getAllowShowSQL())
			logger.info(formatParameters(params));
		super.fillStatement(stmt, params);
	}

	/**
	 * Format SQL, subClass can override this method to customise SQL format
	 */
	protected String formatSql(String sql) {
		return "SQL: " + sql;
	}

	/**
	 * Format parameters, subClass can override this method to customise parameters
	 * format
	 */
	protected String formatParameters(Object... params) {
		return "Parameters: " + Arrays.deepToString(params);
	}

	// === Batch execute methods======
	/**
	 * Force flush cached SQLs
	 */
	public void batchFlush() throws SQLException {
		List<Object[]> sqlCacheList = sqlBatch.get();
		if (sqlCacheList.size() == 0)
			return;
		Object[] f = sqlCacheList.get(0);// first row
		if (f.length != 6)
			throw new DbRuntimeException("Unexpected batch cached SQL format");
		int paramLenth;
		if ("i1".equals(f[0]) || "i3".equals(f[0]) || "u1".equals(f[0]) || "u4".equals(f[0]))
			paramLenth = 0;
		if ("u2".equals(f[0]) || "u5".equals(f[0]))
			paramLenth = 1;
		else
			paramLenth = ((Object[]) sqlCacheList.get(0)[5]).length;
		Object[][] allParams = new Object[sqlCacheList.size()][paramLenth];
		int i = 0;
		for (Object[] c : sqlCacheList) {// cached parameters
			Object param = c[2];
			Object[] params = (Object[]) c[5];
			if ("i1".equals(f[0]) || "i3".equals(f[0]) || "u1".equals(f[0]) || "u4".equals(f[0]))
				allParams[i] = new Object[0];
			if ("u2".equals(f[0]) || "u5".equals(f[0]))
				allParams[i] = new Object[] { param };
			else
				allParams[i] = params;
			i++;
		}
		String sql = (String) f[3];
		Connection conn = (Connection) f[4];
		ResultSetHandler rsh = (ResultSetHandler) f[1];
		if ("e1".equals(f[0]) || "i1".equals(f[0]) || "u1".equals(f[0]) || "u2".equals(f[0]) || "u3".equals(f[0]))
			super.batch(conn, sql, allParams);
		else if ("e3".equals(f[0]) || "i3".equals(f[0]) || "u4".equals(f[0]) || "u5".equals(f[0]) || "u6".equals(f[0]))
			super.batch(sql, allParams);
		else if ("e2".equals(f[0]) || "i2".equals(f[0]))
			super.insertBatch(conn, sql, rsh, allParams);
		else if ("e4".equals(f[0]) || "i4".equals(f[0]))
			super.insertBatch(sql, rsh, allParams);
		else
			throw new DbRuntimeException("unknow batch sql operation type +'" + f[0] + "'");
		sqlBatch.get().clear();
	}

	/** Start batch sql */
	public void batchBegin() throws SQLException {
		if (!sqlBatch.get().isEmpty())
			batchFlush();
		this.batchEnable = true;
	}

	/** Stop batch sql */
	public void batchEnd() throws SQLException {
		if (!sqlBatch.get().isEmpty())
			batchFlush();
		this.batchEnable = false;
	}

	/**
	 * Force flush cached SQLs
	 */
	public void nBatchFlush() {
		try {
			batchFlush();
		} catch (Exception e) {
			throw new DbRuntimeException(e);
		}
	}

	/** Start batch sql */
	public void nBatchBegin() {
		try {
			batchBegin();
		} catch (Exception e) {
			throw new DbRuntimeException(e);
		}
	}

	/** Stop batch sql */
	public void nBatchEnd() {
		try {
			batchEnd();
		} catch (Exception e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Add SQL to cache, if full (reach batchSize) then call batchFlush() <br/>
	 * @throws SQLException
	 * 
	 */
	private <T> T addToCacheIfFullFlush(String execteType, ResultSetHandler<T> rsh, Object param, String sql,
			Connection conn, Object... params) throws SQLException {
		Object[] forCache = new Object[] { execteType, rsh, param, sql, conn, params };
		List<Object[]> cached = sqlBatch.get();
		if (cached.size() >= this.getBatchSize())
			this.batchFlush();
		else if (cached.size() > 0) {
			Object[] last = cached.get(cached.size() - 1);
			if (!last[0].equals(forCache[0]) || !last[3].equals(forCache[3]) || !(last[1] == forCache[1])
					|| !(last[4] == forCache[4]))
				this.batchFlush();
		}
		sqlBatch.get().add(forCache);
		return null;
	}

	// ===override execute/insert/update methods to support batch ======
	// BTW, some method in QueryRunner is private, otherwise no need override so
	// many methods

	@Override
	public int execute(Connection conn, String sql, Object... params) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("e1", null, null, sql, conn, params);
			return 0;
		} else
			return super.execute(conn, sql, params);
	}

	@Override
	public <T> List<T> execute(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params)
			throws SQLException {
		if (batchEnable) {
			return (List<T>) addToCacheIfFullFlush("e2", rsh, null, sql, conn, params);
		} else
			return super.execute(conn, sql, rsh, params);
	}

	@Override
	public int execute(String sql, Object... params) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("e3", null, null, sql, null, params);
			return 0;
		} else
			return super.execute(sql, params);
	}

	@Override
	public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		if (batchEnable)
			return (List<T>) addToCacheIfFullFlush("e4", rsh, null, sql, null, params);
		return super.execute(sql, rsh, params);
	}

	@Override
	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		if (batchEnable)
			return addToCacheIfFullFlush("i1", rsh, null, sql, conn, null);
		return super.insert(conn, sql, rsh);
	}

	@Override
	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		if (batchEnable)
			return addToCacheIfFullFlush("i2", rsh, null, sql, conn, params);
		return super.insert(conn, sql, rsh, params);
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
		if (batchEnable)
			return addToCacheIfFullFlush("i3", rsh, null, sql, null, null);
		return super.insert(sql, rsh);
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		if (batchEnable)
			return addToCacheIfFullFlush("i4", rsh, null, sql, null, params);
		return super.insert(sql, rsh, params);
	}

	@Override
	public int update(Connection conn, String sql) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("u1", null, null, sql, conn, null);
			return 0;
		} else
			return super.update(conn, sql);
	}

	@Override
	public int update(Connection conn, String sql, Object param) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("u2", null, param, sql, conn, null);
			return 0;
		} else
			return super.update(conn, sql, param);
	}

	@Override
	public int update(Connection conn, String sql, Object... params) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("u3", null, null, sql, conn, params);
			return 0;
		} else
			return super.update(conn, sql, params);
	}

	@Override
	public int update(String sql) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("u4", null, null, sql, null, null);
			return 0;
		} else
			return super.update(sql);
	}

	@Override
	public int update(String sql, Object param) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("u5", null, param, sql, null, null);
			return 0;
		} else
			return super.update(sql, param);
	}

	@Override
	public int update(String sql, Object... params) throws SQLException {
		if (batchEnable) {
			addToCacheIfFullFlush("u6", null, null, sql, null, params);
			return 0;
		} else
			return super.update(sql, params);
	}

	// ==========getter & setter==========
	public ConnectionManager getConnectionManager() {
		return cm;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.cm = connectionManager;
	}

	public Boolean getAllowShowSQL() {
		return allowShowSQL;
	}

	public void setAllowShowSQL(Boolean allowShowSQL) {
		this.allowShowSQL = allowShowSQL;
	}

	public DbProLogger getLogger() {
		return logger;
	}

	public void setLogger(DbProLogger logger) {
		this.logger = logger;
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isBatchEnable() {
		return batchEnable;
	}
}

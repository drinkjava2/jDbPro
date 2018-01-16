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

import static com.github.drinkjava2.jdbpro.improve.SqlInterceptor.ARRAY_PARAM;
import static com.github.drinkjava2.jdbpro.improve.SqlInterceptor.NO_PARAM;
import static com.github.drinkjava2.jdbpro.improve.SqlInterceptor.SINGLE_PARAM;

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
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * ImprovedQueryRunner made below improvements compare DbUtils's QueryRunner:
 * 
 * 
 * 1) Override close() and prepareConnection() method of QueryRunner, use a
 * ConnectionManager to manage connection, ConnectionManager can get connection
 * from DataSource or ThreadLocal or some other 3rd party tools like Spring.
 * <br/>
 * 2) Override some methods to add logger support <br/>
 * 3) Override some execute/update/query methods to support batch operation and
 * SqlInterceptor <br/>
 * 4) Add a dialect property to support dialect features like pagination, DDL...
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "all" })
public class ImprovedQueryRunner extends QueryRunner {
	private static final DbProLogger defaultlogger = DbProLogger.getLog(ImprovedQueryRunner.class);
	/**
	 * The ConnectionManager determine how to get and release connections from
	 * DataSource or ThreadLocal
	 */
	protected ConnectionManager connectionManager;

	/** If set true will output SQL and parameters in logger */
	protected Boolean allowShowSQL = false;

	/** Logger of current ImprovedQueryRunner */
	protected DbProLogger logger = defaultlogger;

	/** Default Batch Size, current fixed to 100 */
	protected Integer batchSize = 100;

	/** SqlTemplateEngine of current ImprovedQueryRunner */
	protected SqlTemplateEngine sqlTemplateEngine = BasicSqlTemplate.instance();

	/**
	 * Dialect of current ImprovedQueryRunner, default guessed from DataSource, can
	 * use setDialect() method to change to other dialect
	 */
	protected Dialect dialect;

	/**
	 * A ThreadLocal type cache to store SqlInterceptor instances, all instance will
	 * be cleaned after this thread close
	 */
	private static ThreadLocal<ArrayList<SqlInterceptor>> explainerSupportCache = new ThreadLocal<ArrayList<SqlInterceptor>>() {
		@Override
		protected ArrayList<SqlInterceptor> initialValue() {
			return new ArrayList<SqlInterceptor>();
		}
	};

	/**
	 * A ThreadLocal type tag to indicate current all SQL operations should be
	 * cached
	 */
	private ThreadLocal<Boolean> batchEnabled = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	/**
	 * A ThreadLocal type cache to store batch SQL and parameters
	 */
	private ThreadLocal<ArrayList<Object[]>> sqlBatchCache = new ThreadLocal<ArrayList<Object[]>>() {
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
		this.dialect = Dialect.guessDialect(ds);
	}

	public ImprovedQueryRunner(DataSource ds, ConnectionManager cm) {
		super(ds);
		this.connectionManager = cm;
		this.dialect = Dialect.guessDialect(ds);
	}

	@Override
	public void close(Connection conn) throws SQLException {
		if (connectionManager == null)
			super.close(conn);
		else
			connectionManager.releaseConnection(conn, this.getDataSource());
	}

	@Override
	public Connection prepareConnection() throws SQLException {
		if (connectionManager == null)
			return super.prepareConnection();
		else
			return connectionManager.getConnection(this.getDataSource());
	}

	@Override
	protected CallableStatement prepareCall(Connection conn, String sql) throws SQLException {
		if (this.getAllowShowSQL() && !batchEnabled.get())
			logger.info("SQL: " + sql);
		return super.prepareCall(conn, sql);
	}

	@Override
	protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
		if (this.getAllowShowSQL() && !batchEnabled.get())
			logger.info(formatSqlForLoggerOutput(sql));
		return super.prepareStatement(conn, sql);
	}

	@Override
	public void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {
		if (this.getAllowShowSQL() && !batchEnabled.get())
			logger.info(formatParametersForLoggerOutput(params));
		super.fillStatement(stmt, params);
	}

	// ========== Dialect about methods ===============
	private void assertDialectNotNull() {
		if (dialect == null)
			throw new DbProRuntimeException("Try use a dialect method but dialect is null");
	}

	/** Shortcut call to dialect.paginate method */
	public String paginate(int pageNumber, int pageSize, String sql) {
		assertDialectNotNull();
		return dialect.paginate(pageNumber, pageSize, sql);
	}

	/** Shortcut call to dialect.translate method */
	public String translate(int pageNumber, int pageSize, String sql) {
		assertDialectNotNull();
		return dialect.paginate(pageNumber, pageSize, sql);
	}

	/** Shortcut call to dialect.paginAndTrans method */
	public String paginAndTrans(int pageNumber, int pageSize, String sql) {
		assertDialectNotNull();
		return dialect.paginAndTrans(pageNumber, pageSize, sql);
	}

	/** Shortcut call to dialect.toCreateDDL method */
	public String[] toCreateDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		return dialect.toCreateDDL(entityClasses);
	}

	/** Shortcut call to dialect.toDropDDL method */
	public String[] toDropDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		return dialect.toDropDDL(entityClasses);
	}

	/** Shortcut call to dialect.toDropAndCreateDDL method */
	public String[] toDropAndCreateDDL(Class<?>... entityClasses) {
		assertDialectNotNull();
		return dialect.toDropAndCreateDDL(entityClasses);
	}

	/** Shortcut call to dialect.toCreateDDL method */
	public String[] toCreateDDL(TableModel... tables) {
		assertDialectNotNull();
		return dialect.toCreateDDL(tables);
	}

	/** Shortcut call to dialect.toDropDDL method */
	public String[] toDropDDL(TableModel... tables) {
		assertDialectNotNull();
		return dialect.toDropDDL(tables);
	}

	/** Shortcut call to dialect.toDropAndCreateDDL method */
	public String[] toDropAndCreateDDL(TableModel... tables) {
		assertDialectNotNull();
		return dialect.toDropAndCreateDDL(tables);
	}

	// =========== Explain SQL about methods========================
	/**
	 * Format SQL for logger output, subClass can override this method to customise
	 * SQL format
	 */
	protected String formatSqlForLoggerOutput(String sql) {
		return "SQL: " + sql;
	}

	/**
	 * Format parameters for logger output, subClass can override this method to
	 * customise parameters format
	 */
	protected String formatParametersForLoggerOutput(Object... params) {
		return "Parameters: " + Arrays.deepToString(params);
	}

	/**
	 * Add a explainer
	 */
	public static ArrayList<SqlInterceptor> getCurrentExplainers() {
		return explainerSupportCache.get();
	}

	/**
	 * Explain SQL to add extra features like pagination...
	 */
	public String explainSql(String sql, int paramType, Object paramOrParams) {
		String newSQL = sql;
		for (SqlInterceptor explainer : getCurrentExplainers())
			newSQL = explainer.handleSql(this, newSQL, paramType, paramOrParams);
		return newSQL;
	}

	public Object explainResult(Object result) {
		Object newObj = result;
		for (SqlInterceptor explainer : getCurrentExplainers())
			newObj = explainer.handleResult(result);
		return newObj;
	}

	// === Batch execute methods======
	/**
	 * Force flush cached SQLs
	 */
	public void batchFlush() throws SQLException {
		List<Object[]> sqlCacheList = sqlBatchCache.get();
		if (sqlCacheList.isEmpty())
			return;
		Object[] f = sqlCacheList.get(0);// first row
		if (f.length != 6)
			throw new DbProRuntimeException("Unexpected batch cached SQL format");
		int paramLenth = 0;
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
		if (this.getAllowShowSQL()) {
			logger.info("Batch execute " + sqlCacheList.size() + " SQLs");
			logger.info(formatSqlForLoggerOutput(sql));
			logger.info("First row " + formatParametersForLoggerOutput(allParams[0]));
			logger.info("Last row " + formatParametersForLoggerOutput(allParams[allParams.length - 1]));
		}
		if ("e1".equals(f[0]) || "i1".equals(f[0]) || "u1".equals(f[0]) || "u2".equals(f[0]) || "u3".equals(f[0]))
			super.batch(conn, sql, allParams);
		else if ("e3".equals(f[0]) || "i3".equals(f[0]) || "u4".equals(f[0]) || "u5".equals(f[0]) || "u6".equals(f[0]))
			super.batch(sql, allParams);
		else if ("e2".equals(f[0]) || "i2".equals(f[0]))
			super.insertBatch(conn, sql, rsh, allParams);
		else if ("e4".equals(f[0]) || "i4".equals(f[0]))
			super.insertBatch(sql, rsh, allParams);
		else
			throw new DbProRuntimeException("unknow batch sql operation type +'" + f[0] + "'");
		sqlBatchCache.get().clear();
	}

	/** Start batch sql */
	public void batchBegin() throws SQLException {
		if (!sqlBatchCache.get().isEmpty())
			batchFlush();
		this.batchEnabled.set(true);
	}

	/** Stop batch sql */
	public void batchEnd() throws SQLException {
		if (!sqlBatchCache.get().isEmpty())
			batchFlush();
		this.batchEnabled.set(false);
	}

	/**
	 * Force flush cached SQLs
	 */
	public void nBatchFlush() {
		try {
			batchFlush();
		} catch (Exception e) {
			throw new DbProRuntimeException(e);
		}
	}

	/** Start batch sql */
	public void nBatchBegin() {
		try {
			batchBegin();
		} catch (Exception e) {
			throw new DbProRuntimeException(e);
		}
	}

	/** Stop batch sql */
	public void nBatchEnd() {
		try {
			batchEnd();
		} catch (Exception e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Add SQL to cache, if full (reach batchSize) then call batchFlush() <br/>
	 * 
	 * @throws SQLException
	 * 
	 */
	private <T> T addToCacheIfFullFlush(String execteType, ResultSetHandler<T> rsh, Object param, String sql,
			Connection conn, Object... params) throws SQLException {
		Object[] forCache = new Object[] { execteType, rsh, param, sql, conn, params };
		List<Object[]> cached = sqlBatchCache.get();
		if (cached.size() >= this.getBatchSize())
			this.batchFlush();
		else if (!cached.isEmpty()) {
			Object[] last = cached.get(cached.size() - 1);
			if (!last[0].equals(forCache[0]) || !last[3].equals(forCache[3]) || (last[1] != forCache[1])
					|| (last[4] != forCache[4]))
				this.batchFlush();
		}
		sqlBatchCache.get().add(forCache);
		return null;
	}

	// ===override execute/insert/update methods to support batch and explainSql
	// BTW, some methods in QueryRunner are private, otherwise no need override
	// so many methods

	@Override
	public int execute(Connection conn, String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("e1", null, null, explainedSql, conn, params);
				return 0;
			} else {
				int result = super.execute(conn, explainedSql, params);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> List<T> execute(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params)
			throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get()) {
				return (List<T>) addToCacheIfFullFlush("e2", rsh, null, explainedSql, conn, params);
			} else {
				List<T> result = super.execute(conn, explainedSql, rsh, params);
				return (List<T>) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int execute(String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("e3", null, null, explainedSql, null, params);
				return 0;
			} else {
				int result = super.execute(explainedSql, params);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get())
				return (List<T>) addToCacheIfFullFlush("e4", rsh, null, explainedSql, null, params);
			List<T> result = super.execute(explainedSql, rsh, params);
			return (List<T>) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(sql, NO_PARAM, null);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i1", rsh, null, explainedSql, conn, null);
			T result = super.insert(conn, explainedSql, rsh);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T insert(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i2", rsh, null, explainedSql, conn, params);
			T result = super.insert(conn, explainedSql, rsh, params);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(sql, NO_PARAM, null);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i3", rsh, null, explainedSql, null, null);
			T result = super.insert(explainedSql, rsh);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get())
				return addToCacheIfFullFlush("i4", rsh, null, explainedSql, null, params);
			T result = super.insert(explainedSql, rsh, params);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int update(Connection conn, String sql) throws SQLException {
		try {
			String explainedSql = explainSql(sql, NO_PARAM, null);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u1", null, null, explainedSql, conn, null);
				return 0;
			} else {
				int result = super.update(conn, explainedSql);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int update(Connection conn, String sql, Object param) throws SQLException {
		try {
			String explainedSql = explainSql(sql, SINGLE_PARAM, param);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u2", null, param, explainedSql, conn, null);
				return 0;
			} else {
				int result = super.update(conn, explainedSql, param);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int update(Connection conn, String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u3", null, null, explainedSql, conn, params);
				return 0;
			} else {
				int result = super.update(conn, explainedSql, params);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int update(String sql) throws SQLException {
		try {
			String explainedSql = explainSql(sql, NO_PARAM, null);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u4", null, null, explainedSql, null, null);
				return 0;
			} else {
				int result = super.update(explainedSql);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int update(String sql, Object param) throws SQLException {
		try {
			String explainedSql = explainSql(sql, SINGLE_PARAM, param);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u5", null, param, explainedSql, null, null);
				return 0;
			} else {
				int result = super.update(explainedSql, param);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public int update(String sql, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			if (batchEnabled.get()) {
				addToCacheIfFullFlush("u6", null, null, explainedSql, null, params);
				return 0;
			} else {
				int result = super.update(explainedSql, params);
				return (Integer) explainResult(result);
			}
		} finally {
			getCurrentExplainers().clear();
		}
	}

	/**
	 * Convert paramList to 2d array for insertBatch use, insertBatch's last
	 * parameter is a 2d array, not easy to use
	 */
	private static Object[][] toArray(List<List<?>> paramList) {
		Object[][] array = new Object[paramList.size()][];
		int i = 0;
		for (List<?> item : paramList)
			array[i++] = item.toArray(new Object[item.size()]);
		return array;
	}

	/**
	 * Executes the given batch of INSERT SQL statements, connection is get from
	 * current dataSource
	 * 
	 * @param sql The SQL statement to execute.
	 * @param rsh The handler used to create the result object
	 * @param paramList the parameters for all SQLs, list.get(0) is the first SQL's
	 *            parameters
	 * @return The result generated by the handler.
	 * @throws SQLException if a database access error occurs
	 */
	public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, List<List<?>> paramList) throws SQLException {
		return insertBatch(sql, rsh, toArray(paramList));
	}

	/**
	 * Executes the given batch of INSERT SQL statements
	 * 
	 * @param conn The connection
	 * @param sql The SQL statement to execute.
	 * @param rsh The handler used to create the result object
	 * @param paramList the parameters for all SQLs, list.get(0) is the first SQL's
	 *            parameters
	 * @return The result generated by the handler.
	 * @throws SQLException if a database access error occurs
	 */
	public <T> T insertBatch(Connection conn, String sql, ResultSetHandler<T> rsh, List<List<?>> paramList)
			throws SQLException {
		return this.insertBatch(conn, sql, rsh, toArray(paramList));
	}

	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			T result = super.query(conn, explainedSql, rsh, params);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(sql, NO_PARAM, null);
			T result = super.query(conn, explainedSql, rsh);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		try {
			String explainedSql = explainSql(sql, ARRAY_PARAM, params);
			T result = super.query(explainedSql, rsh, params);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	@Override
	public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		try {
			String explainedSql = explainSql(sql, NO_PARAM, null);
			T result = super.query(explainedSql, rsh);
			return (T) explainResult(result);
		} finally {
			getCurrentExplainers().clear();
		}
	}

	// ==========getter & setter==========
	/**
	 * Return current SqlTemplateEngine
	 */
	public SqlTemplateEngine getSqlTemplateEngine() {
		return sqlTemplateEngine;
	}

	/**
	 * Set a SqlTemplateEngine, if not set will default use a BasicSqlTemplate
	 * instance as SQL template engine
	 * 
	 * @param sqlTemplateEngine
	 */
	public void setSqlTemplateEngine(SqlTemplateEngine sqlTemplateEngine) {
		this.sqlTemplateEngine = sqlTemplateEngine;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
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

	public boolean isBatchEnabled() {
		return batchEnabled.get();
	}

	public Dialect getDialect() {
		return dialect;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

}

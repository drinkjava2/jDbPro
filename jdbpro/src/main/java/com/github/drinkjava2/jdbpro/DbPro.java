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
package com.github.drinkjava2.jdbpro;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.OutParameter;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.inline.InlineQueryRunner;
import com.github.drinkjava2.jdbpro.template.TemplateQueryRunner;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * DbPro is the enhanced version of Apache Commons DbUtils's QueryRunner, add below improvements:
 * 
 * <pre>
 * 1)Use ConnectionManager to manage connection for better transaction support
 * 2)normal style methods but no longer throw SQLException, methods named as nXxxxx() format
 * 3)In-line style methods, methods named as iXxxxx() format
 * 4)SQL Template style methods, methods named as tXxxxx() format
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DbPro extends TemplateQueryRunner implements NormalJdbcTool {
	public DbPro() {
		super();
	}

	public DbPro(DataSource ds) {
		super(ds);
	}

	public DbPro(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
	}

	/**
	 * Clear all In-Line parameters and Template parameters stored in ThreadLocal
	 */
	public static void clearAll() {
		TemplateQueryRunner.clearBind();
		InlineQueryRunner.clearParams();
	}

	// ==========================================================
	// DbUtils style methods, throw SQLException

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found, SQLException may be threw if some SQL operation Exception
	 * happen. Note: The caller is responsible for closing the connection.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 * @throws SQLException
	 */
	public <T> T queryForObject(Connection conn, String sql, Object... params) throws SQLException {
		return query(conn, sql, new ScalarHandler<T>(1), params);
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found, SQLException may be threw if some SQL operation Exception
	 * happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 * @throws SQLException
	 */
	public <T> T queryForObject(String sql, Object... params) throws SQLException {
		return query(sql, new ScalarHandler<T>(1), params);
	}

	// ==========================================================
	// Normal style methods but transfer SQLException to DbRuntimeException

	/**
	 * Executes the given SELECT SQL query and returns a result object. Transaction
	 * mode is determined by connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param sql the SQL
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param params the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(String sql, ResultSetHandler<T> rsh, Object... params) {
		try {
			return query(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found , DbRuntimeException may be threw if some SQL operation
	 * Exception happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	@Override
	public <T> T nQueryForObject(String sql, Object... params) {
		return nQuery(sql, new ScalarHandler<T>(1), params);
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Transaction mode
	 * is determined by connectionManager property.
	 * @param sql the SQL
	 * @param params the parameters if have
	 * @return The number of rows updated.
	 */
	@Override
	public int nUpdate(String sql, Object... params) {
		try {
			return update(sql, params);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT SQL statement. Transaction mode is determined by
	 * connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param sql the SQL
	 * @param params the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nInsert(String sql, ResultSetHandler<T> rsh, Object... params) {
		try {
			return insert(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Executes the given Batch INSERT SQL statement. Transaction mode is determined
	 * by connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param sql the SQL
	 * @param params the parameter array
	 * @return An object generated by the handler.
	 */
	public <T> T nInsertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) {
		try {
			return insertBatch(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets. If you are not invoking a stored procedure,
	 * or the stored procedure has no OUT parameters, consider using
	 * {@link #Update(java.lang.String...) }. If the stored procedure returns result
	 * sets, use
	 * {@link #iExecute(org.apache.commons.dbutils.ResultSetHandler, java.lang.String...) }.
	 * @param sql the SQL
	 * @return The number of rows updated.
	 */
	@Override
	public int nExecute(String sql, Object... params) {
		try {
			return execute(sql, params);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters.Transaction mode is determined by
	 * connectionManager property.
	 * 
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters. Otherwise you may wish to use
	 * {@link #iQuery(org.apache.commons.dbutils.ResultSetHandler, java.lang.String...) }
	 * (if there are no OUT parameters) or {@link #iExecute(java.lang.String...) }
	 * (if there are no result sets).
	 *
	 * @param <T> The type of object that the handler returns
	 * @param rsh The result set handler
	 * @param sql the SQL
	 * @return A list of objects generated by the handler
	 * 
	 */
	public <T> List<T> nExecute(String sql, ResultSetHandler<T> rsh, Object... params) {
		try {
			return execute(sql, rsh, params);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	// ====================================================================
	// In-Line style and transfer SQLException to DbRuntimeException
	/**
	 * Executes the given SELECT SQL query and returns a result object. Transaction
	 * mode is determined by connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param inlineSQL the in-line style SQL
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T iQuery(ResultSetHandler<T> rsh, String... inlineSQL) {
		try {
			return this.inlineQuery(rsh, inlineSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an In-line style query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned, a
	 * null object may return if no result found , DbRuntimeException may be threw
	 * if some SQL operation Exception happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T iQueryForObject(String... inlineSQL) {
		return iQuery(new ScalarHandler<T>(1), inlineSQL);
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Transaction mode
	 * is determined by connectionManager property.
	 * @param inlineSQL the in-line style SQL *
	 * @return The number of rows updated.
	 */
	public int iUpdate(String... inlineSQL) {
		try {
			return this.inlineUpdate(inlineSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT SQL statement. Transaction mode is determined by
	 * connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param inlineSQL the in-line style SQL
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T iInsert(ResultSetHandler<T> rsh, String... inlineSQL) {
		try {
			return this.inlineInsert(rsh, inlineSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets. If you are not invoking a stored procedure,
	 * or the stored procedure has no OUT parameters, consider using
	 * {@link #inlineUpdate(java.lang.String...) }. If the stored procedure returns
	 * result sets, use
	 * {@link #iExecute(org.apache.commons.dbutils.ResultSetHandler, java.lang.String...) }.
	 * <p>
	 *
	 * @param inlineSQL the in-line style SQL.
	 * @return The number of rows updated.
	 */
	public int iExecute(String... inlineSQL) {
		try {
			return this.inlineExecute(inlineSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters.Transaction mode is determined by
	 * connectionManager property.
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters. Otherwise you may wish to use
	 * {@link #iQuery(org.apache.commons.dbutils.ResultSetHandler, java.lang.String...) }
	 * (if there are no OUT parameters) or {@link #iExecute(java.lang.String...) }
	 * (if there are no result sets).
	 *
	 * @param <T> The type of object that the handler returns
	 * @param rsh The result set handler
	 * @param inlineSQL the in-line style SQL
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> iExecute(ResultSetHandler<T> rsh, String... inlineSQL) {
		try {
			return this.inlineExecute(rsh, inlineSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	// ====================================================================
	// SQL Template style and transfer SQLException to DbRuntimeException
	/**
	 * Executes the template style given SELECT SQL query and returns a result
	 * object. Transaction mode is determined by connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param templateSQL the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T tQuery(ResultSetHandler<T> rsh, String... templateSQL) {
		try {
			return this.templateQuery(rsh, templateSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL Template query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned, a
	 * null object may return if no result found , DbRuntimeException may be threw
	 * if some SQL operation Exception happen.
	 * 
	 * @param templateSQL
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T tQueryForObject(String... templateSQL) {
		return tQuery(new ScalarHandler<T>(), templateSQL);
	}

	/**
	 * Executes the template style given INSERT, UPDATE, or DELETE SQL statement.
	 * Transaction mode is determined by connectionManager property.
	 * @param templateSQL the SQL template
	 * @return The number of rows updated.
	 */
	public int tUpdate(String... templateSQL) {
		try {
			return this.templateUpdate(templateSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Executes the template style given INSERT SQL statement. Transaction mode is
	 * determined by connectionManager property.
	 * @param <T> The type of object that the handler returns
	 * @param rsh The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param templateSQL the SQL template
	 * @return An object generated by the handler.
	 */
	public <T> T tInsert(ResultSetHandler<T> rsh, String... templateSQL) {
		try {
			return this.templateInsert(rsh, templateSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL template statement, including a stored procedure call, which
	 * does not return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets. If you are not invoking a stored procedure,
	 * or the stored procedure has no OUT parameters, consider using
	 * {@link #iemplateUpdate(java.lang.String...) }. If the stored procedure
	 * returns result sets, use
	 * {@link #iExecute(org.apache.commons.dbutils.ResultSetHandler, java.lang.String...) }.
	 * <p>
	 *
	 * @param templateSQL the SQL template.
	 * @return The number of rows updated.
	 */
	public int tExecute(String... templateSQL) {
		try {
			return this.templateExecute(templateSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}

	/**
	 * Execute an SQL template statement, including a stored procedure call, which
	 * returns one or more result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters. Otherwise you may wish to use
	 * {@link #iQuery(org.apache.commons.dbutils.ResultSetHandler, java.lang.String...) }
	 * (if there are no OUT parameters) or {@link #iExecute(java.lang.String...) }
	 * (if there are no result sets).
	 *
	 * @param <T> The type of object that the handler returns
	 * @param rsh The result set handler
	 * @param templateSQL the SQL template
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> tExecute(ResultSetHandler<T> rsh, String... templateSQL) {
		try {
			return this.templateExecute(rsh, templateSQL);
		} catch (SQLException e) {
			throw new DbRuntimeException(e);
		}
	}
}

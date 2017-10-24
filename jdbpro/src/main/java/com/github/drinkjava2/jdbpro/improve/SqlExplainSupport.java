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
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.PaginateSupport;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * SqlExplainSupport should have a explain method to explain SQL
 * 
 * @since 1.7.0.1
 */
@SuppressWarnings({ "all" })
public interface SqlExplainSupport {
	public static final int NO_PARAM = 0;
	public static final int ARRAY_PARAM = 1;
	public static final int OBJECT_PARAM = 2;

	/**
	 * Explain SQL to add extra features like pagination, logging...
	 * 
	 * @param query
	 *            The ImprovedQueryRunner
	 * @param Sql
	 *            The original SQL
	 * @param isArrayParams
	 *            return true if paramOrParams is Array
	 * @param paramOrParams
	 *            param object of params array
	 * @return explained SQL
	 */
	public String explainSql(ImprovedQueryRunner query, String Sql, int paramType, Object paramOrParams);

	public Object explainResult(Object result);
}

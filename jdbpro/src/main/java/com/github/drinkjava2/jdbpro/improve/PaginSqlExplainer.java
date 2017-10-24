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

import com.github.drinkjava2.jdbpro.DbRuntimeException;

/**
 * Explain pagin() method, a normal SQL will be explained to a SQL with
 * pageNumber and pageSize condition, for example "select * from users" explain
 * to "select * from users limit 2, 10"
 * 
 * @since 1.7.0.1
 */
public class PaginSqlExplainer implements SqlExplainSupport {
	private int pageNumber;
	private int pageSize;

	public PaginSqlExplainer(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	@Override
	public String explainSql(ImprovedQueryRunner query, String sql, int paramType, Object paramOrParams) {
		if (query.getPaginator() == null)
			throw new DbRuntimeException("Can not explain pagin() method before a paginator be set.");
		return query.getPaginator().paginate(pageNumber, pageSize, sql);
	}

	@Override
	public Object explainResult(Object result) {
		return result;
	}
}

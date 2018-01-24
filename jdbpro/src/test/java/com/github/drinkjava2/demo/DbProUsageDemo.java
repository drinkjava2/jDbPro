package com.github.drinkjava2.demo;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.inline;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.inline0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.valuesQuesions;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put0;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.replace;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.template.NamedParamSqlTemplate;

/**
 * This is DbPro usage demo, show different SQL style
 * 
 * <pre>
 * query(Connection, String sql, Object... params):   Original DbUtils methods,  need close Connection and catch SQLException
 * query(String sql, Object... params):   Original DbUtils methods, need catch SQLException
 * nQuery(String sql, Object... params):  normal style, no need catch SQLException
 * iQuery(String... inlineSQLs):  In-line style
 * tQuery(String... sqlTemplate):  SQL Template style
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.7.0
 * 
 */
public class DbProUsageDemo {

	@Before
	public void setupDB() {
		DbPro db = new DbPro((DataSource) BeanBox.getBean(DataSourceBox.class));
		try {
			db.nExecute("drop table users");
		} catch (Exception e) {
		}
		db.nExecute("create table users (name varchar(40), address varchar(40))");
	}

	@After
	public void cleanUp() {
		BeanBox.defaultContext.close();
	}

	public static class User {
		String name;
		String address;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	@Test
	public void executeTest() {
		DataSource ds = (DataSource) BeanBox.getBean(DataSourceBox.class);
		DbPro dbPro = new DbPro(ds);
		dbPro.setGlobalAllowShowSQL(true);
		User user = new User();
		user.setName("Sam");
		user.setAddress("Canada");

		System.out.println("Example#1: DbUtils old style methods, need close connection and catch SQLException");
		Connection conn = null;
		try {
			conn = dbPro.prepareConnection();
			dbPro.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
			dbPro.execute(conn, "update users set name=?, address=?", "Sam", "Canada");
			Assert.assertEquals(1, ((Number) dbPro.queryForObject(conn,
					"select count(*) from users where name=? and address=?", "Sam", "Canada")).longValue());
			dbPro.execute(conn, "delete from users where name=? or address=?", "Sam", "Canada");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				dbPro.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Example#2: DbUtils old style methods, need catch SQLException");
		try {
			dbPro.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
			dbPro.execute("update users set name=?, address=?", "Sam", "Canada");
			Assert.assertEquals(1,
					dbPro.queryForLongValue("select count(*) from users where name=? and address=?", "Sam", "Canada"));
			dbPro.execute("delete from users where name=? or address=?", "Sam", "Canada");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Example#3: nXxxx methods no need catch SQLException");
		dbPro.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
		dbPro.nExecute("update users set name=?, address=?", "Sam", "Canada");
		Assert.assertEquals(1,
				dbPro.nQueryForLongValue("select count(*) from users where name=? and address=?", "Sam", "Canada"));
		dbPro.nExecute("delete from users where name=? or address=?", "Sam", "Canada");

		System.out.println("Example#4: iXxxx In-line style methods");
		dbPro.iExecute("insert into users (", //
				" name ,", param0("Sam"), //
				" address ", param("Canada"), //
				") ", valuesQuesions());
		param0("Sam", "Canada");
		dbPro.iExecute("update users set name=?,address=?");
		Assert.assertEquals(1, dbPro.iQueryForLongValue("select count(*) from users where name=" + question0("Sam")));
		dbPro.iExecute("delete from users where name=", question0("Sam"), " and address=", question("Canada"));

		System.out.println("Example#5: Another usage of iXxxx inline style");
		dbPro.iExecute("insert into users (", inline0(user, "", ", ") + ") ", valuesQuesions());
		dbPro.iExecute("update users set ", inline0(user, "=?", ", "));
		Assert.assertEquals(1,
				dbPro.iQueryForLongValue("select count(*) from users where ", inline0(user, "=?", " and ")));
		dbPro.iExecute(param0(), "delete from users where ", inline(user, "=?", " or "));

		System.out.println("Example#6: tXxxx Template style methods use default BasicSqlTemplate engine ");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("user", user);
		dbPro.tExecute(params, "insert into users (name, address) values(#{user.name},#{user.address})");
		params.clear();
		params.put("name", "Sam");
		params.put("addr", "Canada");
		dbPro.tExecute(params, "update users set name=#{name}, address=#{addr}");
		Assert.assertEquals(1,
				dbPro.tQueryForLongValue(params, "select count(*) from users where name=#{name} and address=#{addr}"));
		params.clear();
		params.put("name", "Sam");
		params.put("addr", "Canada");
		dbPro.tExecute(params, "delete from users where name=#{name} or address=#{addr}");

		System.out.println("Example#7: tXxxx Template + Inline style ");
		put0("user", user);
		dbPro.tExecute("insert into users (name, address) values(#{user.name},#{user.address})");
		put0("name", "Sam");
		put("addr", "Canada");
		dbPro.tExecute("update users set name=#{name}, address=#{addr}");
		Assert.assertEquals(1,
				dbPro.tQueryForLongValue("select count(*) from users where ${col}=#{name} and address=#{addr}",
						put0("name", "Sam"), put("addr", "Canada"), replace("col", "name")));
		dbPro.tExecute("delete from users where name=#{name} or address=#{addr}", put0("name", "Sam"),
				put("addr", "Canada"));

		System.out.println("Example#8: tXxxx Template style but use 'NamedParamSqlTemplate' template engine");
		dbPro = new DbPro(ds, NamedParamSqlTemplate.instance());
		dbPro.setGlobalAllowShowSQL(true);
		put0("user", user);
		dbPro.tExecute("insert into users (name, address) values(:user.name, :user.address)");
		put0("name", "Sam");
		put("addr", "Canada");
		dbPro.tExecute("update users set name=:name, address=:addr");
		Assert.assertEquals(1,
				dbPro.tQueryForLongValue("select count(*) from users where ${col}=:name and address=:addr",
						put0("name", "Sam"), put("addr", "Canada"), replace("col", "name")));
		dbPro.tExecute("delete from users where name=:name or address=:addr", put0("name", "Sam"),
				put("addr", "Canada"));
	}

}

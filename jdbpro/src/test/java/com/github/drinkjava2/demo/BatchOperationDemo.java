package com.github.drinkjava2.demo;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.inline0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.valuesQuesions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.DbPro;

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
 */
public class BatchOperationDemo {

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
		int repeat = 1000;
		DbPro.setGlobalAllowShowSql(true);
		DbPro dbPro = new DbPro((DataSource) BeanBox.getBean(DataSourceBox.class));

		User user = new User();
		user.setName("Sam");
		user.setAddress("Canada");

		dbPro.nExecute("delete from users");
		long start = System.currentTimeMillis();
		for (long i = 0; i < repeat; i++) {
			user.setName("Name" + i);
			user.setAddress("Address" + i);
			dbPro.iExecute("insert into users (", inline0(user, "", ", ") + ") ", valuesQuesions());
		}
		long end = System.currentTimeMillis();
		String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("Non-Batch execute " + repeat + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(repeat, dbPro.nQueryForLongValue("select count(*) from users"));

		dbPro.nExecute("delete from users");
		start = System.currentTimeMillis();
		dbPro.nBatchBegin();
		for (long i = 0; i < repeat; i++) {
			user.setName("Name" + i);
			user.setAddress("Address" + i);
			dbPro.iExecute("insert into users (", inline0(user, "", ", ") + ") ", valuesQuesions());
		}

		dbPro.nBatchEnd();
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out
				.println(String.format("nBatchBegin/nBatchEnd execute " + repeat + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(repeat, dbPro.nQueryForLongValue("select count(*) from users"));

		dbPro.nExecute("delete from users");
		start = System.currentTimeMillis();
		List<List<?>> params = new ArrayList<List<?>>();
		for (long i = 0; i < repeat; i++) {
			List<Object> oneRow = new ArrayList<Object>();
			oneRow.add("Name" + i);
			oneRow.add("Address" + i);
			params.add(oneRow);
		}
		dbPro.nBatch("insert into users (name, address) values(?,?)", params);
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(
				String.format("nBatch(Sql, List<List)) method execute " + repeat + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(repeat, dbPro.nQueryForLongValue("select count(*) from users"));

		dbPro.nExecute("delete from users");
		start = System.currentTimeMillis();
		Object[][] paramsArray = new Object[repeat][2];
		for (int i = 0; i < repeat; i++) {
			paramsArray[i][0] = "Name" + i;
			paramsArray[i][1] = "Address" + i;
		}
		try {
			dbPro.batch("insert into users (name, address) values(?,?)", paramsArray);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(
				String.format("nBatch(Sql, Object[][]) method execute " + repeat + " SQLs time used: %6s s", timeused));
		Assert.assertEquals(repeat, dbPro.nQueryForLongValue("select count(*) from users"));
	}

}

package demo.usuage;

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

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.template.NamedParamSqlTemplate;

import demo.DataSourceConfig.DataSourceBox;

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
		DbPro dbPro = new DbPro((DataSource) BeanBox.getBean(DataSourceBox.class));
		dbPro.setAllowShowSQL(true);
		User user = new User();
		user.setName("Sam");
		user.setAddress("Canada");

		dbPro.nBatchBegin();
		for (int i = 0; i < 200; i++) {
			user.setName("Name" + i);
			user.setAddress("Address" + i);
			dbPro.iExecute("insert into users (", inline0(user, "", ", ") + ") ", valuesQuesions());
		}
		dbPro.nBatchEnd();

	}

}

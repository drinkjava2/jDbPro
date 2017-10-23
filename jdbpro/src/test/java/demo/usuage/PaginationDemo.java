package demo.usuage;

import static com.github.drinkjava2.jdbpro.improve.ImprovedQueryRunner.pagin;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.DbPro;

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
public class PaginationDemo {

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

	@Test
	public void executeTest() {
		DbPro dbPro = new DbPro((DataSource) BeanBox.getBean(DataSourceBox.class));
		dbPro.setAllowShowSQL(false);
		for (int i = 0; i < 20; i++)
			dbPro.nExecute("insert into users (name,address) values(?,?)", "Name" + i, "Address" + i);
		List<Map<String, Object>> result = dbPro.nQuery(new MapListHandler(), pagin(3, 5) + "select * from users");
		Assert.assertEquals(5, result.size());
		for (Map<String, Object> map : result)
			System.out.println(map);

		System.out.println();
		List<Map<String, Object>> result2 = dbPro.nQuery(new MapListHandler(),
				dbPro.paginate(3, 5, "select * from users"));
		Assert.assertEquals(5, result2.size());
		for (Map<String, Object> map : result2)
			System.out.println(map); 
	}

}

/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.test;

import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectLogger;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class LoggerTest {
	DialectLogger logger = DialectLogger.getLog(LoggerTest.class);

	@Test
	public void doLoggerTest() {
		Dialect.setAllowShowDialectLog(true);
		Dialect.MySQL55Dialect.paginate(10, 10, "select * from sometable"); 
		logger.info("Message output");
		System.out.println("Message output");
	}
}
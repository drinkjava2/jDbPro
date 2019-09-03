/*
 * Logger Test
 */
package com.github.drinkjava2.test;

import org.junit.Test;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class LoggerTest {
	DbProLog logger = DbProLogFactory.getLog(LoggerTest.class);

	@Test
	public void doLoggerTest() {
		logger.info("Message output");
		System.out.println("Message output");
	}
}
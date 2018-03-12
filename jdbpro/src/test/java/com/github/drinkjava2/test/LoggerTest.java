/*
 * Logger Test
 */
package com.github.drinkjava2.test;

import org.junit.Test;

import com.github.drinkjava2.jdbpro.DbProLogger;

/**
 * This is unit test for DDL
 * 
 * @author Yong Z.
 * @since 1.0.2
 */
public class LoggerTest {
	DbProLogger logger = DbProLogger.DefaultDbProLogger.getLog(LoggerTest.class);

	@Test
	public void doLoggerTest() {
		logger.info("Message output");
		System.out.println("Message output");
	}
}
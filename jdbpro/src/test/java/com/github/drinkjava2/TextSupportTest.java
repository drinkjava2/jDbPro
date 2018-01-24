package com.github.drinkjava2;

import org.junit.Test;

import com.github.drinkjava2.jdbpro.TextUtils;

import test.TextSample2;
import text.TextSample1;

/**
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TextSupportTest { 

	@Test
	public void dotest() {
		System.out.println(TextUtils.getJavaSourceCodeUTF8(TextSample1.class));
		System.out.println("==================================================================================");
		System.out.println(TextUtils.getJavaSourceCodeUTF8(TextSample2.class));
	}
}

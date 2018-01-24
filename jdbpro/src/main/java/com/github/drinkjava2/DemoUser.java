package com.github.drinkjava2;

import com.github.drinkjava2.jdialects.annotation.jpa.Table;

/**
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@Table(name = "user_tb")
public class DemoUser {
	private String id;
	private String name;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
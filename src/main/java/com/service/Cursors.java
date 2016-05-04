package com.service;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.mongodb.ReflectionDBObject;

@JsonIgnoreProperties
public class Cursors extends ReflectionDBObject {
	String before;
	String after;
	
	public Cursors() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getBefore() {
		return before;
	}
	public void setBefore(String before) {
		this.before = before;
	}
	public String getAfter() {
		return after;
	}
	public void setAfter(String after) {
		this.after = after;
	}
	
	
}

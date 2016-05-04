package com.service;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.mongodb.ReflectionDBObject;

@JsonIgnoreProperties
public class Paging  {

	Cursors cursors;
	String next;
	String previous;
	
	public Paging() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Cursors getCursors() {
		return cursors;
	}
	public void setCursors(Cursors cursors) {
		this.cursors = cursors;
	}
	public String getNext() {
		return next;
	}
	public void setNext(String next) {
		this.next = next;
	}
	
	public String getPrevious() {
		return previous;
	}
	public void setPrevious(String next) {
		this.previous = next;
	}
	
	
}

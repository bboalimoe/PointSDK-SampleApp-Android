package com.bluedot.pointapp.list;

public class HeaderItem {

	private String name = "";

	public HeaderItem(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}

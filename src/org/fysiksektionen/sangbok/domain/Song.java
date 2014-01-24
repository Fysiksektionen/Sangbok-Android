package org.fysiksektionen.sangbok.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class Song extends DomainObject {

	private String title;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	private String chapter;
	public String getChapter() {
		return chapter;
	}
	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	private String number;
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	private String year;
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
	
	public Song(JSONObject json) throws JSONException{
		assertJsonPropertiesExist(json, "title", "chapter", "number", "year");
		
		this.title = json.getString("title");
		this.chapter = json.getString("chapter");
		this.number = json.getString("number");
		this.year = json.getString("year");
	}
	
}

package org.fysiksektionen.sangbok.domain;

import org.json.JSONObject;

public abstract class DomainObject {

	protected void assertJsonPropertyExists(JSONObject json, String name) {
		if (!json.has(name)) {
			throw new IllegalArgumentException(String.format("The json object to construct the Song from must have a '{0}' property.", name));
		}
	}
	
	protected void assertJsonPropertiesExist(JSONObject json, String... names){
		for(String name : names) {
			assertJsonPropertyExists(json, name);
		}
	}

}

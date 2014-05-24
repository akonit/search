package search.html.json;

import org.json.simple.JSONObject;

public class News {

	private String text;
	private JSONObject json;
	
	public News() {
		
	}
	
	public News(String text, JSONObject json) {
		this.text = text;
		this.json = json;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
}

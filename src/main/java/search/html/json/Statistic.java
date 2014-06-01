package search.html.json;

public class Statistic {

	private String source;
	private int newsNumber;
	
	public Statistic(String source, int newsNumber) {
		this.setSource(source);
		this.newsNumber = newsNumber;
	}
	
	public void increment() {
		newsNumber++;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getNewsNumber() {
		return newsNumber;
	}

	public void setNewsNumber(int newsNumber) {
		this.newsNumber = newsNumber;
	}
}

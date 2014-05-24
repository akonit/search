package search.html.json;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.json.simple.JSONObject;

public class HtmlUtils {

	private static final Logger logger = Logger.getLogger(HtmlUtils.class.getName());
	
	public static final String url = "http://polpred.com/news/?&page=";
	
	private final Connection connection;
	
	public HtmlUtils(Connection connection) throws Exception {
		this.connection = connection;
	}

	public void loadHtml(int pagesNumber) throws Exception {
		for (int i = 1; i <= pagesNumber; i ++) {
			List<News> news = parsePage(url + i);
			DbUtils.putNewsToDb(news, connection);
		}
	}
	
	/**
	 * парсинг страницы с новостями
	 */
	private List<News> parsePage(String pageUrl) throws Exception {
		logger.info("start parsing " + pageUrl);
		Parser parser = new Parser(pageUrl);
		parser.setEncoding("windows-1251");
		NodeList nodeList = parser.parse(new TagNameFilter("div"));
        int listSize = nodeList.size();
        
		List<News> newsList = new ArrayList<>();
		for (int i = 0; i < listSize; i++) {
            Node node = nodeList.elementAt(i);
			if (node.getText().startsWith("div id=\"news")
					&& !node.getText().startsWith("div id=\"news_")
					&& !node.getText().startsWith("div id=\"newscom")) {
				try {
					News news = parseNewsNode(node);
					newsList.add(news);
				} catch (Exception e) {
					logger.warning("failed to parse node: " + node.getText());
					continue;
				}
            }
		}
		
		logger.info("stop parsing " + pageUrl);
		return newsList;
	}
	
	/**
	 * парсинг новости
	 */
	private News parseNewsNode(Node node) {
		StringBuilder text = new StringBuilder();
		JSONObject json = new JSONObject();
		NodeList children = node.getChildren();

		boolean regionProcessed = false;
		for (int i = 0; i < children.size(); i++) {
			Node child = children.elementAt(i);
			String region = "";
			String sourceLink = "";
			String date = "";
			String branch = "";
			String link = "";
			
			if (child.getText().startsWith("span") && !regionProcessed) {
				region = getRegion(child);
				json.put("region", region);
				regionProcessed = true;
				
				branch = getBranch(child);
				json.put("branch", branch);
				
				link = getLink(child);
				json.put("link", link);
			} else if (child.getText().startsWith("span") && regionProcessed) {
				sourceLink = getSourceLink(child);
				json.put("source", sourceLink);
				
				date = getDate(child);
				json.put("date", date);
			}
			
			if (child.getText().startsWith("p ") || child.getText().equals("p")) {
				text.append(child.toPlainTextString().trim()).append(" ");
			}
		}
		
		News news = new News(text.toString(), json);
		return news;
	}
	
	/**
	 * @return регион
	 */
	private String getRegion(Node node) {
		StringBuilder region = new StringBuilder();
		boolean isRegion = false;
		NodeList children = node.getChildren();
		for(int j = 0; j < children.size(); j++) {
			if (children.elementAt(j).getText().startsWith("/i")) {
				isRegion = false;
			}
			if (isRegion) {
				region.append(children.elementAt(j).toPlainTextString());
			}
			if (children.elementAt(j).getText().startsWith("i")) {
				isRegion = true;
			} else if (children.elementAt(j).getText().startsWith("/i")) {
				isRegion = false;
			}
		}
		return region.toString();
	}
	
	/**
	 * @return ссылка на источник
	 */
	private String getSourceLink(Node node) {
		String rawLink = node.toPlainTextString();
		if (rawLink.contains(",")) {
			String link = rawLink.split(",")[0];
			link = link.substring(link.lastIndexOf("/") + 1);
			link = link.trim();
			return link;
		} else {
			NodeList children = node.getChildren();
			String link = "";
			for(int j = 0; j < children.size(); j++) {
				if (children.elementAt(j).getText().startsWith("a ")) {
					link = children.elementAt(j).toPlainTextString().trim();
					break;
				}
			}
			return link;
		}
	}
	
	/**
	 * @return дата
	 */
	private String getDate(Node node) {
		String rawDate = node.toPlainTextString();
		if (rawDate.contains(",")) {
			String date = rawDate.split(",")[1];
			date = date.substring(0, date.lastIndexOf("№"));
			date = date.trim();
			return date;
		} else {
			NodeList children = node.getChildren();
			String date = "";
			boolean metLink = false;
			for(int j = 0; j < children.size(); j++) {
				if (children.elementAt(j).getText().startsWith("a ")) {
					metLink = true;
				} else if (metLink) {
					date = children.elementAt(j).toPlainTextString().trim();
					break;
				}
			}
			return date;
		}
	}
	
	/**
	 * @return отрасль
	 */
	/**
	 * @param node
	 * @return
	 */
	private String getBranch(Node node) {
		NodeList children = node.getChildren();
		String branch = "";
		for(int j = 0; j < children.size(); j++) {
			if (children.elementAt(j).getText().startsWith("a ")) {
				branch = children.elementAt(j).toPlainTextString().trim();
			}
		}
		return branch;
	}
	
	/**
	 * @return ссылка на новость.
	 */
	private String getLink(Node node) {
		NodeList children = node.getChildren();
		String link= "";
		for(int j = 0; j < children.size(); j++) {
			if (children.elementAt(j).getText().startsWith("a ")) {
				link = children.elementAt(j).getText().trim();
			}
		}
		link = link.substring(link.lastIndexOf("href"));
		link = link.substring(6, link.length() - 1);
		return link;
	}
}

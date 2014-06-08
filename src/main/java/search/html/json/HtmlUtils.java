package search.html.json;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.json.JSONObject;

public class HtmlUtils {

	private static final Logger logger = Logger.getLogger(HtmlUtils.class.getName());
	
	public static final String url = "http://polpred.com/news/?&page=";
	
	public static final String triple = "'''";
	public static final String spaced_triple = "' ' '";
	
	private final Connection connection;
	
	private static final int titleLimit = 60;
	
	public HtmlUtils(Connection connection) throws Exception {
		this.connection = connection;
	}

	public void loadHtml(int pagesNumber) throws Exception {
		for (int i = 1; i <= pagesNumber; i ++) {
			List<News> news = parsePage(url + i);
			List<Statistic> stats = new ArrayList<>();
			collectStatistic(stats, news);
			DbUtils.putNewsToDb(news, connection);
			DbUtils.putStatsToDb(stats, connection);
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
					e.printStackTrace();
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
				json.put("region", postProcessing(region));
				regionProcessed = true;
				
				branch = getBranch(child);
				json.put("branch", postProcessing(branch));
				
				link = getLink(child);
				json.put("link", postProcessing(link));
			} else if (child.getText().startsWith("span") && regionProcessed) {
				sourceLink = getSourceLink(child);
				json.put("source", postProcessing(sourceLink));
				
				date = getDate(child);
				json.put("date", postProcessing(date));
			}
			
			if (child.getText().startsWith("p ") || child.getText().equals("p")) {
				text.append(child.toPlainTextString().trim()).append(" ");
				
				// костыль для криво оформленных новостей №1
				boolean badHmtl = checkBadHtml(child);				
				if (badHmtl) {
					for (int j = 0; j < child.getChildren().size(); j++) {
						Node subChild = child.getChildren().elementAt(j);
						if (subChild != null 
								&& subChild.getText().startsWith("span") 
								&& regionProcessed
								&& subChild.toPlainTextString().contains("№")) {
							sourceLink = getSourceLink(subChild);
							json.put("source", postProcessing(sourceLink));
							
							date = getDate(subChild);
							json.put("date", postProcessing(date));
						}
					}
				}
			}
		}
		
		json.put("title", postProcessing(extractTitle(text.toString())));
		News news = new News(postProcessing(text.toString()), json);
		return news;
	}
	
	private String postProcessing(String text) {
		text = text.replace(triple, spaced_triple);
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		text = text.replace("&amp;", "&");
		text = text.replace("&quot;", "'");
		text = text.replace("&nbsp;", " ");
		text = text.replace("&frasl;", "/");
		text = text.replace("&ndash;", "-");
		text = text.replace("&mdash;", "-");
		text = text.replace("&hellip;", "...");
		text = text.replace("&laquo;", "\"");
		text = text.replace("&raquo;", "\"");
		text = text.replace("&ldquo;", "\"");
		text = text.replace("&rdquo;", "\"");
		text = text.replace("&auml;", "a");
		text = text.replace("&aacute;", "a");
		text = text.replace("&Aacute;", "A");
		text = text.replace("&Auml;", "A");
		return text;
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
	
	private boolean checkBadHtml(Node node) {
        for (int j = 0; j < node.getChildren().size(); j++) {
			if (node.getChildren().elementAt(j).getText().contains("span")) {
				return true;
			}
        }
		return false;
	}
	
	/**
	 * Извлечение заголовка новости из ее текста.
	 * @param text текст новости.
	 */
	private String extractTitle(String text) {
		int length = text.length();
		if (length < 1) {
			return "";
		}

		for (int i = 1; i < length; i++) {
			if (Character.toString(text.charAt(i)).equals(".")
					&& !Character.toString(text.charAt(i - 1)).toUpperCase()
							.equals(Character.toString(text.charAt(i - 1)))) {
				String title = text.substring(0, i);
				if (title.length() > titleLimit) {
					title = title.substring(0, titleLimit - 3).concat("...");
				}
				return title;
			}
		}

		return "";
	}
	
	public void collectStatistic(List<Statistic> stats, List<News> newsList) {
		if (newsList != null) {
			for (News news: newsList) {
				String source = (String) news.getJson().get("source");
				boolean presents = false;
				for (Statistic stat : stats) {
					if (stat.getSource().equals(source)) {
						stat.increment();
						presents = true;
					}
				}
				
				if (!presents) {
				    stats.add(new Statistic(source, 1));
				}
			}
		}
	}
}

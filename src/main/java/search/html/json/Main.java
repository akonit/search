package search.html.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;

public class Main {
	
	private static final Logger logger = Logger.getLogger(Main.class.getName());
	
	public static void main(String[] args) {
		
		if (args.length < 1) {
			logger.warning("No config file is specified");
			return;
		}
		
		Properties props = loadProperties(args[0]);
		if (props == null) {
			return;
		}
		
		Connection c = null;
		try {
			c = DbUtils.getConnection(props);
			HtmlUtils htmlUtils = new HtmlUtils(c);
			htmlUtils.loadHtml(Integer.parseInt(String.valueOf((props.get("pages_number")))));
		} catch (Exception e) {
			logger.warning("error while executing program");
			e.printStackTrace();
		} finally {
			DbUtils.closeConnection(c);
		}
	}
	
	private static Properties loadProperties(String fileName) {
		Properties props = new Properties();
	 
		try (InputStream inputStream = 
				new FileInputStream(new File(fileName))) {
	 
		  props.load(inputStream);
		  return props;	 
		} catch (Exception e) {
			logger.warning("failed to parse config file: " + fileName);
			return null;
		}
	}
}

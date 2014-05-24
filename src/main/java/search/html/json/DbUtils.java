package search.html.json;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class DbUtils {
	
	private static final Logger logger = Logger.getLogger(DbUtils.class.getName());

	public static Connection getConnection(Properties props) throws Exception {
		Class.forName("org.postgresql.Driver");
		String host = props.getProperty("host");
		String port = props.getProperty("port");
		String dbName = props.getProperty("db_name");
		String userName = props.getProperty("user_name");
		String userPass = props.getProperty("user_pass");
		Connection connection = DriverManager.getConnection(
		   "jdbc:postgresql://" + host + ":" + port + "/" + dbName, userName, userPass);
		
		logger.info("open connection: " + "jdbc:postgresql://" + host + ":" + port + "/" + dbName
				+ ", " + userName + ", " + userPass);
		return connection;
	}
	
	public static void closeConnection(Connection connection) {
		try {
			connection.close();
			logger.info("close connection");
		} catch (SQLException e) {
			logger.warning("failed to close connection");
		}
	}
	
	public static void putNewsToDb(List<News> newsList, Connection c) {
		try (PreparedStatement ps = c
				.prepareStatement("INSERT INTO news (news_text, data_type, json)"
						+ " VALUES (?, ?, ?)");) {

			for (News news : newsList) {
				ps.setString(1, news.getText());
				ps.setString(2, "");
				ps.setString(3, news.getJson().toString());
				ps.addBatch();
			}

			ps.executeBatch();
		} catch (SQLException e) {
			logger.warning("failed to add data to DB");
		}
	}
}

package net.greypanther.logparser.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ConnectionSource {
	@SneakyThrows
	public static Connection getConnection() {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost/logparser";
		Properties props = new Properties();
		props.setProperty("user", "logparser");
		props.setProperty("password", "Yp9pd246A5hb72pRtCJb");
		return DriverManager.getConnection(url, props);
	}
}

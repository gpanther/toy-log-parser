package net.greypanther.logparser.db.report;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import lombok.Builder;
import lombok.Value;
import net.greypanther.logparser.db.ConnectionSource;

public final class Main {
	private static final String UNIQUE_IPS_PER_DOMAIN_PER_DAY_QUERY = "SELECT referrer_domain, date_trunc('day', \"timestamp\") AS date_day, COUNT(1) AS count "
			+ "FROM access_log GROUP BY referrer_domain, date_day ORDER BY referrer_domain, date_day";

	public static void main(String[] args) throws Exception {
		try (Connection dbConnection = ConnectionSource.getConnection()) {
			JdbcTemplate template = new JdbcTemplate(new SingleConnectionDataSource(dbConnection, false));

			Report.builder().jdbcTemplate(template).title("Unique IPs / domain / day:")
					.query(UNIQUE_IPS_PER_DOMAIN_PER_DAY_QUERY).callbackHandler(UniqueIpsReportHandler.INSTANCE).build()
					.run();

			Report.builder().jdbcTemplate(template).title("Unique URLS:")
					.query("SELECT DISTINCT url FROM access_log ORDER BY url")
					.callbackHandler(StringReportHandler.INSTANCE).build().run();

			Report.builder().jdbcTemplate(template).title("Unique user agents:")
					.query("SELECT DISTINCT user_agent FROM access_log ORDER BY user_agent")
					.callbackHandler(StringReportHandler.INSTANCE).build().run();
		}
	}

	private static final class UniqueIpsReportHandler implements RowCallbackHandler {
		private static final UniqueIpsReportHandler INSTANCE = new UniqueIpsReportHandler();

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			String referrerDomain = rs.getString(1);
			LocalDate day = rs.getDate(2).toLocalDate();
			int count = rs.getInt(3);

			System.out.format("%s\t%s\t%d%n", referrerDomain, day, count);
		}
	}

	private static final class StringReportHandler implements RowCallbackHandler {
		private static final StringReportHandler INSTANCE = new StringReportHandler();

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			String string = rs.getString(1);
			System.out.println(string);
		}
	}

	@Value
	@Builder
	private static final class Report {
		private final JdbcTemplate jdbcTemplate;
		private final String title, query;
		private final RowCallbackHandler callbackHandler;

		private void run() {
			System.out.println(title);
			jdbcTemplate.query(query, callbackHandler);
			System.out.println();
			System.out.println();
		}
	}
}

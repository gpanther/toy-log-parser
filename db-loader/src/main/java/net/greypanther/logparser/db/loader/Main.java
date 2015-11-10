package net.greypanther.logparser.db.loader;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.greypanther.logparser.LogEntry;
import net.greypanther.logparser.db.ConnectionSource;
import net.greypanther.logparser.input.Input;

public final class Main {
	private static final int INSERT_BATCH_SIZE = 1;

	public static void main(@Nonnull String[] uris) throws Exception {
		try (Connection dbConnection = ConnectionSource.getConnection()) {
			NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(
					new SingleConnectionDataSource(dbConnection, false));

			Iterator<LogEntry> it = new Input(Arrays.asList(uris)).iterator();
			while (it.hasNext()) {
				List<LogEntry> logEntries = take(it, INSERT_BATCH_SIZE);
				Map<String, ?>[] parameters = createParameters(logEntries);
				jdbcTemplate.batchUpdate(
						"INSERT INTO access_log(host, \"timestamp\", url, referrer, referrer_domain, user_agent)"
								+ " VALUES (:host::inet, to_timestamp(:timestamp), :url, :referrer, :referrer_domain, :user_agent)",
						parameters);
			}
		}
	}

	private static Map<String, ?>[] createParameters(List<LogEntry> logEntries) {
		@SuppressWarnings("unchecked")
		Map<String, ?>[] result = new Map[logEntries.size()];
		for (int i = 0; i < logEntries.size(); ++i) {
			LogEntry entry = logEntries.get(i);
			Map<String, ?> map = ImmutableMap.<String, Object> builder()
					.put("host", entry.getSourceAddress().getHostAddress())
					.put("timestamp", entry.getTimestamp().getEpochSecond()).put("url", entry.getUrl())
					.put("referrer", entry.getHost()).put("referrer_domain", entry.getDomain())
					.put("user_agent", entry.getUserAgent()).build();
			result[i] = map;
		}
		return result;
	}

	private static <T> List<T> take(Iterator<T> iterator, int count) {
		List<T> result = Lists.newArrayListWithExpectedSize(count);
		for (int i = 0; i < count; ++i) {
			if (iterator.hasNext()) {
				result.add(iterator.next());
			} else {
				break;
			}
		}
		return result;
	}
}

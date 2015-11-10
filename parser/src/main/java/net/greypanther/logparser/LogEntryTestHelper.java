package net.greypanther.logparser;

import java.net.InetAddress;
import java.time.Instant;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@Value
@Builder
public final class LogEntryTestHelper {
	private final String ip;
	private final String timestamp;
	private final String url;
	private final String referrerHost;
	private final String referrerDomain;
	private final String userAgent;

	@SneakyThrows
	public LogEntry toLogEntry() {
		return new LogEntry(InetAddress.getByName(ip), Instant.parse(timestamp), url, referrerHost, referrerDomain,
				userAgent);
	}

	public static LogEntryTestHelper.LogEntryTestHelperBuilder builderWithDefaults() {
		return builder().ip("127.0.0.1").timestamp("2015-04-05T00:00:00.00Z").url("/url")
				.referrerHost("www.example.com").referrerDomain("example.com").userAgent("User Agent");
	}
}

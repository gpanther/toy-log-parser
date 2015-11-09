package net.greypanther.logparser;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

public final class ParserTest {
	private Parser parser;

	@Test
	public void testParser() throws Exception {
		String line = logLineBuilder().build().toString();
		LogEntry expected = logEntryBuilder().build().toLogEntry();
		assertEquals(expected, parser.parse(line));
	}

	@Test
	public void testDomainsOnThePublicSuffixListAreProperlyParsed() throws Exception {
		String line = logLineBuilder().referrerHost("www.example.co.uk").build().toString();
		LogEntry expected = logEntryBuilder().referrerHost("www.example.co.uk").referrerDomain("example.co.uk").build()
				.toLogEntry();
		assertEquals(expected, parser.parse(line));
	}

	@Before
	public void setUp() {
		parser = new Parser();
	}

	private static LogLine.LogLineBuilder logLineBuilder() {
		return LogLine.builder().ip("127.0.0.1").timestamp("05/Apr/2015:00:00:00 +0000").url("/url")
				.referrerHost("www.example.com");
	}

	private static ExpectedLogEntry.ExpectedLogEntryBuilder logEntryBuilder() {
		return ExpectedLogEntry.builder().ip("127.0.0.1").timestamp("2015-04-05T00:00:00.00Z").url("/url")
				.referrerHost("www.example.com").referrerDomain("example.com");
	}

	@Value
	@Builder
	private static final class LogLine {
		private final String ip;
		private final String timestamp;
		private final String url;
		private final String referrerHost;

		@Override
		public String toString() {
			return String.format("%s - - [%s] \"GET %s HTTP/1.0\" 200 - \"https://%s/\" \"User Agent\" 1", ip,
					timestamp, url, referrerHost);
		}
	}

	@Value
	@Builder
	private static final class ExpectedLogEntry {
		private final String ip;
		private final String timestamp;
		private final String url;
		private final String referrerHost;
		private final String referrerDomain;

		@SneakyThrows
		private LogEntry toLogEntry() {
			return new LogEntry(InetAddress.getByName(ip), Instant.parse(timestamp), url, referrerHost, referrerDomain,
					"User Agent");
		}
	}
}

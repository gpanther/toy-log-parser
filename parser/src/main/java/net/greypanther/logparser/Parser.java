package net.greypanther.logparser;

import java.net.InetAddress;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.net.InternetDomainName;

import lombok.SneakyThrows;
import nl.basjes.parse.core.Field;
import nl.basjes.parse.httpdlog.ApacheHttpdLoglineParser;

@NotThreadSafe
public final class Parser {
	private static final String APACHE_LOG_FORMAT = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" 1";

	private final nl.basjes.parse.core.Parser<LogRecord> parser = new ApacheHttpdLoglineParser<>(LogRecord.class,
			APACHE_LOG_FORMAT);
	private final LogRecord record = new LogRecord();

	@Nonnull
	@SneakyThrows
	public LogEntry parse(@Nonnull String line) {
		record.clear();
		parser.parse(record, line);
		return record.toLogEntry();
	}

	public final static class LogRecord {
		private final DateTimeFormatter dateTimeParser = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

		private String ip;
		private String timestamp;
		private String uri;
		private String referer;
		private String userAgent;

		private LogRecord() {
		}

		private void clear() {
			ip = null;
			timestamp = null;
			uri = null;
			referer = null;
			userAgent = null;
		}

		@Field("IP:connection.client.host")
		public void setIP(final String value) {
			ip = value;
		}

		@Field("TIME.STAMP:request.receive.time")
		public void setTimestamp(final String value) {
			timestamp = value;
		}

		@Field("HTTP.URI:request.referer")
		public void setReferer(final String value) {
			referer = value;
		}

		@Field("HTTP.URI:request.firstline.uri")
		public void setUri(final String value) {
			uri = value;
		}

		@Field("HTTP.USERAGENT:request.user-agent")
		public void setUserAgent(final String value) {
			userAgent = value;
		}

		@SneakyThrows
		private LogEntry toLogEntry() {
			String host = URI.create(referer).getHost();
			InternetDomainName domain = InternetDomainName.from(host).topPrivateDomain();

			return new LogEntry(InetAddress.getByName(ip),
					dateTimeParser.parse(timestamp, ZonedDateTime::from).toInstant(), uri, host, domain.toString(),
					userAgent);
		}
	}
}

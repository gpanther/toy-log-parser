package net.greypanther.logparser;

import java.net.InetAddress;
import java.time.Instant;

import lombok.Value;

@Value
public final class LogEntry {
	private final InetAddress sourceAddress;
	private final Instant timestamp;
	private final String url, host, domain, userAgent;
}

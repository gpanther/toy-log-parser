package net.greypanther.logparser.inmemory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.greypanther.logparser.LogEntry;

@NotThreadSafe
final class StatisticsCollector implements Consumer<LogEntry> {
	private final ZoneId zoneId;
	private final Map<String, Map<LocalDate, Integer>> dailyUniqueVisitorsPerDomain = Maps.newTreeMap();
	private final Set<String> uniqueUrls = Sets.newTreeSet();
	private final Set<String> uniqueUserAgents = Sets.newTreeSet();

	StatisticsCollector(ZoneId zoneId) {
		this.zoneId = zoneId;

	}

	@Override
	public void accept(LogEntry accessLogEntry) {
		Map<LocalDate, Integer> counters = getCountersFor(accessLogEntry.getDomain());
		LocalDate date = convertToLocalDate(accessLogEntry.getTimestamp());
		incrementCounter(counters, date);

		uniqueUrls.add(accessLogEntry.getUrl());
		uniqueUserAgents.add(accessLogEntry.getUserAgent());
	}

	private void incrementCounter(Map<LocalDate, Integer> counters, LocalDate date) {
		Integer counter = counters.get(date);
		if (counter == null) {
			counter = 0;
		}
		counters.put(date, counter + 1);
	}

	private LocalDate convertToLocalDate(Instant timestamp) {
		return LocalDateTime.ofInstant(timestamp, zoneId).toLocalDate();
	}

	private Map<LocalDate, Integer> getCountersFor(String host) {
		Map<LocalDate, Integer> result = dailyUniqueVisitorsPerDomain.get(host);
		if (result == null) {
			result = Maps.newTreeMap();
			dailyUniqueVisitorsPerDomain.put(host, result);
		}
		return result;
	}

	Map<String, Map<LocalDate, Integer>> getDailyUniqueVisitorsPerDomain() {
		return dailyUniqueVisitorsPerDomain;
	}

	Set<String> getUniqueUrls() {
		return uniqueUrls;
	}

	Set<String> getUniqueUserAgents() {
		return uniqueUserAgents;
	}
}

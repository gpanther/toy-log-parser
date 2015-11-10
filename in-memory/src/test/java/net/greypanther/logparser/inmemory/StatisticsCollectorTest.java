package net.greypanther.logparser.inmemory;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import net.greypanther.logparser.LogEntryTestHelper;

public final class StatisticsCollectorTest {
	private StatisticsCollector collector;

	@Test
	public void testUrlsAreCollected() {
		collector.accept(LogEntryTestHelper.builderWithDefaults().url("foo").build().toLogEntry());

		assertEquals(Collections.singleton("foo"), collector.getUniqueUrls());
	}

	@Test
	public void testUrlsAreDeduplicated() {
		collector.accept(LogEntryTestHelper.builderWithDefaults().url("foo").build().toLogEntry());
		collector.accept(LogEntryTestHelper.builderWithDefaults().url("foo").build().toLogEntry());

		assertEquals(Collections.singleton("foo"), collector.getUniqueUrls());
	}

	@Test
	public void testUserAgentsAreCollected() {
		collector.accept(LogEntryTestHelper.builderWithDefaults().userAgent("foo").build().toLogEntry());

		assertEquals(Collections.singleton("foo"), collector.getUniqueUserAgents());
	}

	@Test
	public void testUserAgentsAreDeduplicated() {
		collector.accept(LogEntryTestHelper.builderWithDefaults().userAgent("foo").build().toLogEntry());
		collector.accept(LogEntryTestHelper.builderWithDefaults().userAgent("foo").build().toLogEntry());

		assertEquals(Collections.singleton("foo"), collector.getUniqueUserAgents());
	}

	@Test
	public void testUniqueVisitorsAreCounted() {
		collector.accept(LogEntryTestHelper.builderWithDefaults().referrerDomain("foo.com").build().toLogEntry());

		LocalDate accessDate = parse("2015-04-05T00:00:00.00Z");
		assertEquals(Collections.singletonMap("foo.com", Collections.singletonMap(accessDate, 1)),
				collector.getDailyUniqueVisitorsPerDomain());
	}

	@Test
	public void testUniqueVisitorsOnSameDayAreAddedUp() {
		collector.accept(LogEntryTestHelper.builderWithDefaults().referrerDomain("foo.com").build().toLogEntry());
		collector.accept(LogEntryTestHelper.builderWithDefaults().referrerDomain("foo.com").build().toLogEntry());

		LocalDate accessDate = parse("2015-04-05T00:00:00.00Z");
		assertEquals(Collections.singletonMap("foo.com", Collections.singletonMap(accessDate, 2)),
				collector.getDailyUniqueVisitorsPerDomain());
	}

	@Before
	public void setUp() {
		collector = new StatisticsCollector(ZoneId.of("Z"));
	}

	private static LocalDate parse(String utcTimestamp) {
		return LocalDateTime.ofInstant(Instant.parse(utcTimestamp), ZoneId.of("Z")).toLocalDate();
	}
}

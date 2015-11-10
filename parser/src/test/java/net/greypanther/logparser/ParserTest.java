package net.greypanther.logparser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public final class ParserTest {
	private Parser parser;

	@Test
	public void testParser() throws Exception {
		String line = LogLineTestHelper.builderWithDefaults().build().toString();
		LogEntry expected = LogEntryTestHelper.builderWithDefaults().build().toLogEntry();
		assertEquals(expected, parser.parse(line));
	}

	@Test
	public void testLinesWithEmptyReferrersGenerateNull() throws Exception {
		String line = LogLineTestHelper.builderWithDefaults().fullReferrer("-").build().toString();
		assertNull(parser.parse(line));
	}

	@Test
	public void testUnencodedUrlsAreHandledProperly() throws Exception {
		String line = LogLineTestHelper.builderWithDefaults().referrer("www.example.com/test?origin=|mckv|")
				.build().toString();
		LogEntry expected = LogEntryTestHelper.builderWithDefaults().build().toLogEntry();
		assertEquals(expected, parser.parse(line));
	}

	@Test
	public void testDomainsOnThePublicSuffixListAreProperlyParsed() throws Exception {
		String line = LogLineTestHelper.builderWithDefaults().referrer("www.example.co.uk").build().toString();
		LogEntry expected = LogEntryTestHelper.builderWithDefaults().referrerHost("www.example.co.uk")
				.referrerDomain("example.co.uk").build().toLogEntry();
		assertEquals(expected, parser.parse(line));
	}

	@Before
	public void setUp() {
		parser = new Parser();
	}
}

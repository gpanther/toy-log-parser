package net.greypanther.logparser.input;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;

import net.greypanther.logparser.LogEntry;

public final class InputTest {
	@Test
	public void testInput() {
		String testFile = InputTest.class.getResource("/test.gz").getFile();
		Input input = new Input(Arrays.asList(testFile));
		Collection<LogEntry> logEntries = Lists.newArrayList(input);

		assertEquals(100, logEntries.size());
	}

	@Test
	public void testInputWithMultipleFiles() {
		String testFile = InputTest.class.getResource("/test.gz").getFile();
		Input input = new Input(Arrays.asList(testFile, testFile));
		Collection<LogEntry> logEntries = Lists.newArrayList(input);

		assertEquals(200, logEntries.size());
	}
}

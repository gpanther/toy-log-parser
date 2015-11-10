package net.greypanther.logparser.inmemory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.greypanther.logparser.LogEntry;
import net.greypanther.logparser.Parser;

public class Main {
	private final static Logger LOG = LoggerFactory.getLogger(Main.class);
	private final static int STREAM_BUFFER_SIZE = 1024 * 1024; // 1M bytes
	private final static Charset INPUT_CHARSET = Charset.forName("UTF-8");

	private static StatisticsCollector collector;
	private static int totalLines, totalErrorLines;

	public static void main(@Nonnull String[] uris) throws Exception {
		Stopwatch globalStopwatch = Stopwatch.createStarted();
		parse(uris);
		printResults();
		LOG.info(String.format("Finished in %s", globalStopwatch.stop()));
		LOG.info(String.format("Total %d lines, %d unparsable lines", totalLines, totalErrorLines));
	}

	private static void printResults() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.format("Unique IPs / domain / day:%n");
		for (Map.Entry<String, Map<LocalDate, Integer>> entry : collector.getDailyUniqueVisitorsPerDomain()
				.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(gson.toJson(entry.getValue()));
		}
		System.out.println();

		System.out.format("Uniuqe URLS (%d):%n", collector.getUniqueUrls().size());
		collector.getUniqueUrls().stream().forEach(System.out::println);
		System.out.println();

		System.out.format("Uniuqe user agents (%d):%n", collector.getUniqueUserAgents().size());
		collector.getUniqueUserAgents().stream().forEach(System.out::println);
		System.out.println();
	}

	private static void parse(String[] uris) throws Exception {
		collector = new StatisticsCollector(ZoneId.systemDefault());
		for (String uri : uris) {
			LOG.info("Parsing " + uri);
			Stopwatch stopwatch = Stopwatch.createStarted();
			try (BufferedReader reader = getReaderFor(uri)) {
				Iterator<String> lines = reader.lines().iterator();
				Parser parser = new Parser();
				int errorLines = 0;

				while (lines.hasNext()) {
					totalLines++;
					String line = lines.next();
					LogEntry logEntry = parser.parse(line);
					if (logEntry == null) {
						errorLines++;
						totalErrorLines++;
						continue;
					}
					collector.accept(logEntry);
				}

				if (errorLines > 0) {
					LOG.warn(String.format("There were %d unparsable lines in %s", errorLines, uri));
				}
			}
			LOG.info(String.format("Finished parsing %s in %s", uri, stopwatch.stop()));
		}
	}

	@Nonnull
	private static BufferedReader getReaderFor(@Nonnull String uri) throws Exception {
		InputStream stream = new URL(uri).openStream();
		if (uri.toLowerCase().endsWith(".gz")) {
			// transparent decompression of gzipped inputs
			stream = new GZIPInputStream(stream, STREAM_BUFFER_SIZE);
		}
		return new BufferedReader(new InputStreamReader(stream, INPUT_CHARSET), STREAM_BUFFER_SIZE);
	}
}

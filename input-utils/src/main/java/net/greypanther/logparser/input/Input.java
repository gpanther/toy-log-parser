package net.greypanther.logparser.input;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import net.greypanther.logparser.LogEntry;
import net.greypanther.logparser.Parser;

public final class Input implements Iterable<LogEntry> {
	private final static Logger LOG = LoggerFactory.getLogger(Input.class);
	private final static int STREAM_BUFFER_SIZE = 1024 * 1024; // 1M bytes
	private final static Charset INPUT_CHARSET = Charset.forName("UTF-8");
	private final static Pattern URI_PROTOCOL = Pattern.compile("^\\w+://");

	private final Collection<String> sourceUris;

	public Input(@Nonnull Collection<String> sourceUris) {
		this.sourceUris = Lists.newArrayList(sourceUris);
	}

	@Override
	public LogEntriesIterator iterator() {
		return new LogEntriesIterator();
	}

	public final class LogEntriesIterator implements Iterator<LogEntry> {
		private final Stopwatch globalStopwatch = Stopwatch.createStarted();
		private final Parser parser = new Parser();
		private final Iterator<String> urisIt = sourceUris.iterator();

		private String currentUri;
		private Stopwatch stopwatch;
		private BufferedReader fileReader;
		private Iterator<String> fileLines = Collections.<String> emptySet().iterator();

		private LogEntry next = extractNext();
		private int totalLines;
		private int errorLines;
		private int totalErrorLines;

		@Override
		public boolean hasNext() {
			if (next != null) {
				return true;
			}
			LOG.info(String.format("Finished in %s", globalStopwatch.stop()));
			LOG.info(String.format("Total %d lines, %d unparsable lines", totalLines, totalErrorLines));
			return false;
		}

		@Override
		public LogEntry next() {
			LogEntry result = next;
			next = extractNext();
			return result;
		}

		@SneakyThrows
		private LogEntry extractNext() {
			while (true) {
				while (fileLines.hasNext()) {
					String line = fileLines.next();
					totalLines++;
					LogEntry logEntry = parser.parse(line);
					if (logEntry != null) {
						return logEntry;
					} else {
						errorLines++;
						totalErrorLines++;
					}
				}

				if (currentUri != null) {
					stopwatch.stop();
					fileReader.close();
					if (errorLines > 0) {
						LOG.warn(String.format("There were %d unparsable lines in %s", errorLines, currentUri));
					}
					LOG.info(String.format("Finished parsing %s in %s", currentUri, stopwatch));
				}

				if (!urisIt.hasNext()) {
					return null;
				}

				currentUri = urisIt.next();
				LOG.info("Parsing " + currentUri);
				stopwatch = Stopwatch.createStarted();
				fileReader = getReaderFor(currentUri);
				fileLines = fileReader.lines().iterator();
				errorLines = 0;
			}
		}

		@Nonnull
		private BufferedReader getReaderFor(@Nonnull String uri) throws Exception {
			if (!URI_PROTOCOL.matcher(uri).find()) {
				uri = "file://" + uri;
			}
			InputStream stream = new URL(uri).openStream();
			if (uri.toLowerCase().endsWith(".gz")) {
				// transparent decompression of gzipped inputs
				stream = new GZIPInputStream(stream, STREAM_BUFFER_SIZE);
			}
			return new BufferedReader(new InputStreamReader(stream, INPUT_CHARSET), STREAM_BUFFER_SIZE);
		}
	}
}

package net.greypanther.logparser.inmemory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.greypanther.logparser.LogEntry;
import net.greypanther.logparser.input.Input;

public class Main {
	private static StatisticsCollector collector;

	public static void main(@Nonnull String[] uris) throws Exception {
		parse(uris);
		printResults();
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

		System.out.format("Unique URLS (%d):%n", collector.getUniqueUrls().size());
		collector.getUniqueUrls().stream().forEach(System.out::println);
		System.out.println();

		System.out.format("Unique user agents (%d):%n", collector.getUniqueUserAgents().size());
		collector.getUniqueUserAgents().stream().forEach(System.out::println);
		System.out.println();
	}

	private static void parse(String[] uris) throws Exception {
		collector = new StatisticsCollector(ZoneId.systemDefault());
		for (LogEntry logEntry : new Input(Arrays.asList(uris))) {
			collector.accept(logEntry);
		}
	}
}

package net.greypanther.logparser;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public final class LogLineTestHelper {
	private final String ip;
	private final String timestamp;
	private final String url;
	private final String referrer;
	private final String fullReferrer;

	@Override
	public String toString() {
		String fullReferrer = this.fullReferrer;
		if (fullReferrer == null) {
			fullReferrer = "http://" + referrer;
		}
		return String.format("%s - - [%s] \"GET %s HTTP/1.0\" 200 - \"%s\" \"User Agent\" 1", ip, timestamp, url,
				fullReferrer);
	}

	public static LogLineTestHelper.LogLineTestHelperBuilder builderWithDefaults() {
		return builder().ip("127.0.0.1").timestamp("05/Apr/2015:00:00:00 +0000").url("/url")
				.referrer("www.example.com");
	}
}

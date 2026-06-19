package com.duszek.pindrop.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private final Ai ai = new Ai();
	private final Places places = new Places();
	private final Weather weather = new Weather();

	@Getter
	@Setter
	public static class Ai {
		private String provider = "stub";
		private String model = "gpt-4o";
		private String apiKey = "";
		private String baseUrl = "http://localhost:11434";
	}

	@Getter
	@Setter
	public static class Places {
		private String provider = "stub";
		private String apiKey = "";
	}

	@Getter
	@Setter
	public static class Weather {
		private String provider = "stub";
		private String apiKey = "";
	}
}

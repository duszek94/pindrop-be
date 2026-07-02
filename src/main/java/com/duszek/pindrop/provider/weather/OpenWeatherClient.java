package com.duszek.pindrop.provider.weather;

import com.duszek.pindrop.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.weather.provider", havingValue = "openweather")
public class OpenWeatherClient {

	private final AppProperties appProperties;
	private final WebClient webClient;

	public OpenWeatherClient(AppProperties appProperties) {
		this.appProperties = appProperties;
		this.webClient = WebClient.builder()
				.baseUrl("https://api.openweathermap.org")
				.build();
	}

	public boolean isConfigured() {
		return appProperties.getWeather().getApiKey() != null
				&& !appProperties.getWeather().getApiKey().isBlank();
	}

	@SuppressWarnings("unchecked")
	public List<WeatherForecast> getForecast(double lat, double lng, LocalDate startDate, LocalDate endDate) {
		if (!isConfigured()) {
			log.warn("OpenWeather API key is not configured");
			return List.of();
		}
		try {
			Map<String, Object> response = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/data/2.5/forecast")
							.queryParam("lat", lat)
							.queryParam("lon", lng)
							.queryParam("appid", appProperties.getWeather().getApiKey())
							.queryParam("units", "metric")
							.build())
					.retrieve()
					.bodyToMono(Map.class)
					.block();

			if (response == null || response.isEmpty()) {
				return List.of();
			}
			return OpenWeatherForecastParser.parse(response, startDate, endDate);
		} catch (Exception ex) {
			log.warn("OpenWeather forecast failed for {}, {}: {}", lat, lng, ex.getMessage());
			return List.of();
		}
	}
}

package com.duszek.pindrop.provider.weather;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.weather.provider", havingValue = "openweather")
public class OpenWeatherWeatherProvider implements WeatherProvider {

	private final OpenWeatherClient openWeatherClient;

	public OpenWeatherWeatherProvider(OpenWeatherClient openWeatherClient) {
		this.openWeatherClient = openWeatherClient;
	}

	@Override
	public List<WeatherForecast> getForecast(double lat, double lng, LocalDate startDate, LocalDate endDate) {
		return openWeatherClient.getForecast(lat, lng, startDate, endDate);
	}
}

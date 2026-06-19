package com.duszek.pindrop.provider.weather;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.weather.provider", havingValue = "stub", matchIfMissing = true)
public class StubWeatherProvider implements WeatherProvider {

	private static final String[] ICONS = {"sun", "cloud", "rain", "sun", "cloud"};

	@Override
	public List<WeatherForecast> getForecast(double lat, double lng, LocalDate startDate, LocalDate endDate) {
		List<WeatherForecast> forecasts = new ArrayList<>();
		LocalDate current = startDate;
		int index = 0;
		while (!current.isAfter(endDate) && index < 7) {
			int base = 20 + (index % 4);
			forecasts.add(new WeatherForecast(
					current,
					ICONS[index % ICONS.length],
					base,
					base + 4));
			current = current.plusDays(1);
			index++;
		}
		return forecasts;
	}
}

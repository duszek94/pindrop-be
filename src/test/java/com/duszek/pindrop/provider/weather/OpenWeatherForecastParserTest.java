package com.duszek.pindrop.provider.weather;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenWeatherForecastParserTest {

	@Test
	void parsesDailyForecastsWithinTripRange() {
		Map<String, Object> response = Map.of(
				"list", List.of(
						entry("2026-06-20 09:00:00", 18.2, 17.0, 19.0, "Clouds"),
						entry("2026-06-20 15:00:00", 24.8, 22.0, 26.0, "Clear"),
						entry("2026-06-21 12:00:00", 16.1, 15.0, 17.0, "Rain")));

		List<WeatherForecast> forecasts = OpenWeatherForecastParser.parse(
				response,
				LocalDate.of(2026, 6, 20),
				LocalDate.of(2026, 6, 21));

		assertEquals(2, forecasts.size());
		assertEquals(LocalDate.of(2026, 6, 20), forecasts.get(0).date());
		assertEquals("sun", forecasts.get(0).icon());
		assertEquals(17, forecasts.get(0).tempMinC());
		assertEquals(26, forecasts.get(0).tempMaxC());
		assertEquals(LocalDate.of(2026, 6, 21), forecasts.get(1).date());
		assertEquals("rain", forecasts.get(1).icon());
	}

	@Test
	void returnsEmptyListForMissingEntries() {
		assertTrue(OpenWeatherForecastParser.parse(Map.of(), LocalDate.now(), LocalDate.now().plusDays(2)).isEmpty());
	}

	private static Map<String, Object> entry(
			String dtTxt,
			double temp,
			double tempMin,
			double tempMax,
			String condition) {
		return Map.of(
				"dt_txt", dtTxt,
				"main", Map.of(
						"temp", temp,
						"temp_min", tempMin,
						"temp_max", tempMax),
				"weather", List.of(Map.of("main", condition)));
	}
}

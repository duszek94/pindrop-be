package com.duszek.pindrop.provider.weather;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OpenWeatherForecastParser {

	private static final DateTimeFormatter DT_TXT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private OpenWeatherForecastParser() {
	}

	@SuppressWarnings("unchecked")
	static List<WeatherForecast> parse(
			Map<String, Object> response,
			LocalDate startDate,
			LocalDate endDate) {
		Object listObject = response.get("list");
		if (!(listObject instanceof List<?> entries) || entries.isEmpty()) {
			return List.of();
		}

		Map<LocalDate, List<Map<String, Object>>> byDate = new LinkedHashMap<>();
		for (Object entryObject : entries) {
			if (!(entryObject instanceof Map<?, ?> entry)) {
				continue;
			}
			LocalDate date = parseEntryDate((Map<String, Object>) entry);
			if (date == null || date.isBefore(startDate) || date.isAfter(endDate)) {
				continue;
			}
			byDate.computeIfAbsent(date, ignored -> new ArrayList<>()).add((Map<String, Object>) entry);
		}

		List<WeatherForecast> forecasts = new ArrayList<>();
		for (Map.Entry<LocalDate, List<Map<String, Object>>> dayEntry : byDate.entrySet()) {
			forecasts.add(toDailyForecast(dayEntry.getKey(), dayEntry.getValue()));
		}
		forecasts.sort(Comparator.comparing(WeatherForecast::date));
		return List.copyOf(forecasts);
	}

	private static WeatherForecast toDailyForecast(LocalDate date, List<Map<String, Object>> entries) {
		int tempMin = Integer.MAX_VALUE;
		int tempMax = Integer.MIN_VALUE;
		Map<String, Object> representative = entries.getFirst();

		for (Map<String, Object> entry : entries) {
			Map<String, Object> main = asMap(entry.get("main"));
			if (main != null) {
				int entryTemp = roundTemp(main.get("temp"), null);
				tempMin = Math.min(tempMin, roundTemp(main.get("temp_min"), main.get("temp")));
				tempMax = Math.max(tempMax, roundTemp(main.get("temp_max"), main.get("temp")));
				Map<String, Object> representativeMain = asMap(representative.get("main"));
				int representativeTemp = representativeMain != null
						? roundTemp(representativeMain.get("temp"), null)
						: Integer.MIN_VALUE;
				if (entryTemp >= representativeTemp) {
					representative = entry;
				}
			}
		}

		if (tempMin == Integer.MAX_VALUE) {
			tempMin = 0;
		}
		if (tempMax == Integer.MIN_VALUE) {
			tempMax = tempMin;
		}
		return new WeatherForecast(date, extractIcon(representative), tempMin, tempMax);
	}

	@SuppressWarnings("unchecked")
	private static String extractIcon(Map<String, Object> entry) {
		Object weatherObject = entry.get("weather");
		if (!(weatherObject instanceof List<?> weatherList) || weatherList.isEmpty()) {
			return "cloud";
		}
		Object first = weatherList.getFirst();
		if (!(first instanceof Map<?, ?> weatherMap)) {
			return "cloud";
		}
		Object main = weatherMap.get("main");
		return WeatherIconMapper.toAppIcon(main != null ? String.valueOf(main) : null);
	}

	private static LocalDate parseEntryDate(Map<String, Object> entry) {
		Object dtTxt = entry.get("dt_txt");
		if (dtTxt != null) {
			try {
				return LocalDateTime.parse(String.valueOf(dtTxt), DT_TXT).toLocalDate();
			} catch (DateTimeParseException ignored) {
				// fall through to unix timestamp
			}
		}
		Object dt = entry.get("dt");
		if (dt instanceof Number number) {
			return LocalDateTime.ofEpochSecond(number.longValue(), 0, java.time.ZoneOffset.UTC).toLocalDate();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		if (value instanceof Map<?, ?> map) {
			return (Map<String, Object>) map;
		}
		return null;
	}

	private static int roundTemp(Object primary, Object fallback) {
		if (primary instanceof Number number) {
			return (int) Math.round(number.doubleValue());
		}
		if (fallback instanceof Number number) {
			return (int) Math.round(number.doubleValue());
		}
		return 0;
	}
}

package com.duszek.pindrop.provider.weather;

import java.util.Locale;

public final class WeatherIconMapper {

	private WeatherIconMapper() {
	}

	public static String toAppIcon(String openWeatherMain) {
		if (openWeatherMain == null || openWeatherMain.isBlank()) {
			return "cloud";
		}
		return switch (openWeatherMain.toLowerCase(Locale.ROOT)) {
			case "clear" -> "sun";
			case "clouds" -> "cloud";
			case "rain", "drizzle", "thunderstorm" -> "rain";
			case "snow" -> "cloud";
			case "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> "cloud";
			default -> "cloud";
		};
	}
}

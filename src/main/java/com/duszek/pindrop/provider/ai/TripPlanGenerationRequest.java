package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.provider.weather.WeatherForecast;

import java.time.LocalDate;
import java.util.List;

public record TripPlanGenerationRequest(
		String destination,
		double lat,
		double lng,
		LocalDate startDate,
		LocalDate endDate,
		PreferenceProfile preferenceProfile,
		List<String> interests,
		List<WeatherForecast> weatherForecast) {
}

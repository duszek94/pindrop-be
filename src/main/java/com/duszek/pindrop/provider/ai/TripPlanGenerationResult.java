package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.entity.ActivityType;
import com.duszek.pindrop.entity.ProposalType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TripPlanGenerationResult(List<GeneratedProposal> proposals) {

	public record GeneratedProposal(
			ProposalType type,
			String title,
			String summary,
			int estimatedCostUsd,
			boolean recommended,
			List<WeatherDayForecast> weatherForecast,
			List<String> highlights,
			List<GeneratedDay> days) {
	}

	public record WeatherDayForecast(String dayLabel, String icon, int tempC) {
	}

	public record GeneratedDay(int dayNumber, LocalDate date, List<GeneratedActivity> activities) {
	}

	public record GeneratedActivity(
			LocalTime startTime,
			ActivityType activityType,
			String title,
			String description,
			String placeName,
			Double lat,
			Double lng,
			Integer tempC) {
	}
}

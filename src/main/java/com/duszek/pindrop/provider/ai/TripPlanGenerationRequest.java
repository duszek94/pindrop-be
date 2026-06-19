package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.entity.ActivityType;
import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.ProposalType;
import com.duszek.pindrop.entity.TravelPace;
import com.duszek.pindrop.provider.weather.WeatherForecast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TripPlanGenerationRequest(
		String destination,
		double lat,
		double lng,
		LocalDate startDate,
		LocalDate endDate,
		BudgetTier budgetTier,
		TravelPace pace,
		List<String> interests,
		List<WeatherForecast> weatherForecast) {
}

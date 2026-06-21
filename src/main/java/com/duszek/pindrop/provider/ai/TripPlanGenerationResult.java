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
			List<GeneratedDay> days,
			ProposalCostBreakdown costBreakdown) {
	}

	public record ProposalCostBreakdown(
			EstimatedTotal estimatedTotal,
			Breakdown breakdown) {
	}

	public record EstimatedTotal(int min, int max, String currency, String confidence) {
	}

	public record Breakdown(
			CostRange accommodation,
			TransportCost transport,
			CostRange food,
			CostRange attractions) {
	}

	public record CostRange(int min, int max) {
	}

	public record TransportCost(int min, int max, boolean includesCarCosts, TransportSubCost sub) {
	}

	public record TransportSubCost(
			CostRange publicTransit,
			CostRange fuel,
			CostRange parking,
			CostRange carRental) {
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

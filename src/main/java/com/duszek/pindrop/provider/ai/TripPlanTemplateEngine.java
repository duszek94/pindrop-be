package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.entity.ActivityType;
import com.duszek.pindrop.entity.ProposalType;
import com.duszek.pindrop.provider.weather.WeatherForecast;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class TripPlanTemplateEngine {

	public TripPlanGenerationResult generateProposals(TripPlanGenerationRequest request) {
		String destination = request.destination() != null ? request.destination() : "Your Destination";
		int durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
		List<TripPlanGenerationResult.WeatherDayForecast> weatherCards = buildWeatherCards(request.weatherForecast());

		List<TripPlanGenerationResult.GeneratedProposal> proposals = List.of(
				buildProposal(ProposalType.RELAXED, destination, durationDays, 1250, false,
						"Take it easy, enjoy the moments", weatherCards,
						List.of("Leisurely morning temple visits", "Traditional tea ceremonies",
								"Scenic garden walks", "Local cooking classes"),
						request),
				buildProposal(ProposalType.BALANCED, destination, durationDays, 1850, true,
						"Perfect mix of activity & rest", weatherCards,
						List.of("Morning shrine visits", "Afternoon city exploration",
								"Evening food tours", "Day trip to nearby highlights"),
						request),
				buildProposal(ProposalType.INTENSE, destination, durationDays, 2450, false,
						"Maximum experiences, action-packed", weatherCards,
						List.of("Early morning market tour", "Multiple neighborhood tours",
								"Adventure activities", "Night photography walks"),
						request));

		return new TripPlanGenerationResult(proposals);
	}

	public TripPlanGenerationResult.GeneratedActivity regenerateActivity(
			TripPlanGenerationRequest tripContext,
			TripPlanGenerationResult.GeneratedActivity currentActivity) {
		return new TripPlanGenerationResult.GeneratedActivity(
				currentActivity.startTime(),
				currentActivity.activityType(),
				currentActivity.title() + " (weather-friendly)",
				"Updated for current conditions: " + currentActivity.description(),
				currentActivity.placeName(),
				currentActivity.lat(),
				currentActivity.lng(),
				22);
	}

	private TripPlanGenerationResult.GeneratedProposal buildProposal(
			ProposalType type,
			String destination,
			int durationDays,
			int cost,
			boolean recommended,
			String summary,
			List<TripPlanGenerationResult.WeatherDayForecast> weather,
			List<String> highlights,
			TripPlanGenerationRequest request) {
		String label = switch (type) {
			case RELAXED -> "Relaxed";
			case BALANCED -> "Balanced";
			case INTENSE -> "Intense";
		};
		return new TripPlanGenerationResult.GeneratedProposal(
				type,
				label + " " + destination.split(",")[0].trim() + " Adventure",
				summary,
				cost,
				recommended,
				weather,
				highlights,
				buildDays(type, request.startDate(), durationDays, destination));
	}

	private List<TripPlanGenerationResult.GeneratedDay> buildDays(
			ProposalType type,
			LocalDate startDate,
			int durationDays,
			String destination) {
		int days = Math.min(durationDays, 7);
		List<TripPlanGenerationResult.GeneratedDay> result = new ArrayList<>();
		for (int i = 0; i < days; i++) {
			LocalDate date = startDate.plusDays(i);
			result.add(new TripPlanGenerationResult.GeneratedDay(
					i + 1,
					date,
					buildActivitiesForDay(type, i + 1, destination)));
		}
		return result;
	}

	private List<TripPlanGenerationResult.GeneratedActivity> buildActivitiesForDay(
			ProposalType type,
			int dayNumber,
			String destination) {
		String city = destination.split(",")[0].trim();
		int activityCount = switch (type) {
			case RELAXED -> 3;
			case BALANCED -> 4;
			case INTENSE -> 5;
		};
		return IntStream.range(0, activityCount)
				.mapToObj(i -> {
					LocalTime time = LocalTime.of(9 + i * 3, 0);
					boolean isFood = i == 1 || i == activityCount - 1;
					return new TripPlanGenerationResult.GeneratedActivity(
							time,
							isFood ? ActivityType.FOOD : ActivityType.ACTIVITY,
							isFood ? "Local cuisine in " + city : city + " highlight " + dayNumber + "." + (i + 1),
							isFood ? "Authentic local dining experience." : "Explore a must-see spot in " + city + ".",
							city,
							null,
							null,
							24 + (i % 3));
				})
				.toList();
	}

	private List<TripPlanGenerationResult.WeatherDayForecast> buildWeatherCards(List<WeatherForecast> forecast) {
		if (forecast == null || forecast.isEmpty()) {
			return List.of(
					new TripPlanGenerationResult.WeatherDayForecast("Mon", "sun", 24),
					new TripPlanGenerationResult.WeatherDayForecast("Tue", "cloud", 22),
					new TripPlanGenerationResult.WeatherDayForecast("Wed", "rain", 20),
					new TripPlanGenerationResult.WeatherDayForecast("Thu", "sun", 26),
					new TripPlanGenerationResult.WeatherDayForecast("Fri", "cloud", 23));
		}
		String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
		return IntStream.range(0, Math.min(5, forecast.size()))
				.mapToObj(i -> new TripPlanGenerationResult.WeatherDayForecast(
						labels[i % labels.length],
						forecast.get(i).icon(),
						forecast.get(i).tempMaxC()))
				.toList();
	}
}

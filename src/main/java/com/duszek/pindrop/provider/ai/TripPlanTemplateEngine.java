package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.entity.ActivityType;
import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.ProposalType;
import com.duszek.pindrop.entity.TransportMode;
import com.duszek.pindrop.entity.TravelPace;
import com.duszek.pindrop.provider.weather.WeatherForecast;
import com.duszek.pindrop.util.PreferenceProfileUtils;
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
				buildProposal(ProposalType.RELAXED, destination, durationDays, 0.85, false,
						"Take it easy — pools, long meals, and unhurried exploration", weatherCards,
						List.of("Leisurely neighborhood strolls", "Spa or pool time",
								"Evening social spots", "Local food experiences"),
						request),
				buildProposal(ProposalType.BALANCED, destination, durationDays, 1.0, true,
						"Mix of nature, culture, and time to recharge", weatherCards,
						List.of("Scenic walks and viewpoints", "Museum or cultural highlight",
								"Wellness break", "One signature local experience"),
						request),
				buildProposal(ProposalType.INTENSE, destination, durationDays, 1.15, false,
						"Outdoors and movement — trails, long days, adventure", weatherCards,
						List.of("Hiking or trail day", "Active sightseeing blocks",
								"Outdoor adventure activity", "Early start, packed days"),
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
			double costMultiplier,
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
		TripPlanGenerationResult.ProposalCostBreakdown costBreakdown =
				buildCostBreakdown(request, durationDays, costMultiplier);
		int estimatedCostUsd = costBreakdown.estimatedTotal().max();

		return new TripPlanGenerationResult.GeneratedProposal(
				type,
				label + " " + destination.split(",")[0].trim() + " Adventure",
				summary,
				estimatedCostUsd,
				recommended,
				weather,
				highlights,
				buildDays(type, request, durationDays, destination),
				costBreakdown);
	}

	private TripPlanGenerationResult.ProposalCostBreakdown buildCostBreakdown(
			TripPlanGenerationRequest request,
			int durationDays,
			double costMultiplier) {
		PreferenceProfile profile = request.preferenceProfile();
		int baseDaily = switch (profile != null && profile.getBudgetStyle() != null
				? profile.getBudgetStyle()
				: BudgetTier.MID_RANGE) {
			case ECO -> 90;
			case MID_RANGE -> 150;
			case PREMIUM -> 260;
		};

		int accommodation = (int) (baseDaily * 0.42 * durationDays * costMultiplier);
		int food = (int) (baseDaily * 0.28 * durationDays * costMultiplier);
		int attractions = (int) (baseDaily * 0.18 * durationDays * costMultiplier);
		int publicTransit = (int) (baseDaily * 0.12 * durationDays * costMultiplier);

		boolean includesCarCosts = PreferenceProfileUtils.hasCarMode(profile);
		int fuel = 0;
		int parking = 0;
		int carRental = 0;
		if (includesCarCosts && profile != null) {
			if (profile.getTransportModes().contains(TransportMode.OWN_CAR)) {
				fuel = (int) (baseDaily * 0.1 * durationDays * costMultiplier);
				parking = (int) (baseDaily * 0.05 * durationDays * costMultiplier);
			}
			if (profile.getTransportModes().contains(TransportMode.BUDGET_CAR_RENTAL)) {
				carRental = (int) (baseDaily * 0.22 * durationDays * costMultiplier);
				fuel = (int) (baseDaily * 0.08 * durationDays * costMultiplier);
				parking = (int) (baseDaily * 0.04 * durationDays * costMultiplier);
			}
			if (profile.getTransportModes().contains(TransportMode.PREMIUM_CAR_RENTAL)) {
				carRental = (int) (baseDaily * 0.35 * durationDays * costMultiplier);
				fuel = (int) (baseDaily * 0.1 * durationDays * costMultiplier);
				parking = (int) (baseDaily * 0.06 * durationDays * costMultiplier);
			}
		}

		int transportMin = publicTransit + fuel + parking + carRental;
		int transportMax = (int) (transportMin * 1.15);
		int totalMin = accommodation + food + attractions + transportMin;
		int totalMax = (int) ((accommodation + food + attractions) * 1.1 + transportMax);

		TripPlanGenerationResult.TransportSubCost sub = includesCarCosts
				? new TripPlanGenerationResult.TransportSubCost(
						range(publicTransit, 1.1),
						range(fuel, 1.15),
						range(parking, 1.2),
						range(carRental, 1.1))
				: new TripPlanGenerationResult.TransportSubCost(
						range(publicTransit, 1.1),
						range(0, 0),
						range(0, 0),
						range(0, 0));

		return new TripPlanGenerationResult.ProposalCostBreakdown(
				new TripPlanGenerationResult.EstimatedTotal(totalMin, totalMax, "EUR", "estimated"),
				new TripPlanGenerationResult.Breakdown(
						range(accommodation, 1.1),
						new TripPlanGenerationResult.TransportCost(
								transportMin,
								transportMax,
								includesCarCosts,
								sub),
						range(food, 1.1),
						range(attractions, 1.15)));
	}

	private TripPlanGenerationResult.CostRange range(int min, double maxMultiplier) {
		return new TripPlanGenerationResult.CostRange(min, (int) Math.max(min, min * maxMultiplier));
	}

	private List<TripPlanGenerationResult.GeneratedDay> buildDays(
			ProposalType type,
			TripPlanGenerationRequest request,
			int durationDays,
			String destination) {
		int days = Math.min(durationDays, 7);
		TravelPace pace = request.preferenceProfile() != null ? request.preferenceProfile().getPace() : null;
		List<TripPlanGenerationResult.GeneratedDay> result = new ArrayList<>();
		for (int i = 0; i < days; i++) {
			LocalDate date = request.startDate().plusDays(i);
			result.add(new TripPlanGenerationResult.GeneratedDay(
					i + 1,
					date,
					buildActivitiesForDay(type, pace, i + 1, destination)));
		}
		return result;
	}

	private List<TripPlanGenerationResult.GeneratedActivity> buildActivitiesForDay(
			ProposalType type,
			TravelPace pace,
			int dayNumber,
			String destination) {
		String city = destination.split(",")[0].trim();
		int activityCount = switch (type) {
			case RELAXED -> 3;
			case BALANCED -> 4;
			case INTENSE -> 5;
		};
		if (pace == TravelPace.RELAXED) {
			activityCount = Math.max(2, activityCount - 1);
		} else if (pace == TravelPace.ACTIVE) {
			activityCount = Math.min(6, activityCount + 1);
		}
		final int dayActivityCount = activityCount;
		return IntStream.range(0, dayActivityCount)
				.mapToObj(i -> {
					LocalTime time = LocalTime.of(Math.min(9 + i * 2, 20), 0);
					boolean isFood = i == 1 || i == dayActivityCount - 1;
					String title = isFood
							? "Local cuisine in " + city
							: pace == TravelPace.ACTIVE
									? city + " trail / outdoor block " + dayNumber + "." + (i + 1)
									: pace == TravelPace.RELAXED
											? city + " leisure stop " + dayNumber + "." + (i + 1)
											: city + " highlight " + dayNumber + "." + (i + 1);
					return new TripPlanGenerationResult.GeneratedActivity(
							time,
							isFood ? ActivityType.FOOD : ActivityType.ACTIVITY,
							title,
							isFood ? "Authentic local dining experience." : "Explore a spot matched to your pace in " + city + ".",
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

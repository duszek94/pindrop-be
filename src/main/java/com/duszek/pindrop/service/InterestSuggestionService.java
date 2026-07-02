package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.planning.InterestSuggestionResponse;
import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.PreferenceCategory;
import com.duszek.pindrop.entity.SpendingPriority;
import com.duszek.pindrop.entity.TravelPace;
import com.duszek.pindrop.entity.Trip;
import com.duszek.pindrop.entity.TripInterest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class InterestSuggestionService {

	private static final int SUGGESTION_COUNT = 6;

	private static final int RECOMMENDED_COUNT = 2;

	private static final int BOOST = 10;

	public List<InterestSuggestionResponse> suggest(Trip trip) {
		String placeType = normalizePlaceType(trip.getPlaceType());
		String destination = trip.getDestination() != null ? trip.getDestination().toLowerCase(Locale.ROOT) : "";
		PreferenceProfile profile = trip.getPreferenceProfile();

		Map<TripInterest, Integer> scores = new EnumMap<>(TripInterest.class);
		for (TripInterest interest : eligibleInterests(placeType, destination)) {
			scores.put(interest, scoreInterest(interest, placeType, destination, profile));
		}

		List<Map.Entry<TripInterest, Integer>> ranked = scores.entrySet().stream()
				.sorted(Comparator
						.<Map.Entry<TripInterest, Integer>>comparingInt(Map.Entry::getValue).reversed()
						.thenComparing(entry -> entry.getKey().name()))
				.limit(SUGGESTION_COUNT)
				.toList();

		List<InterestSuggestionResponse> responses = new ArrayList<>(ranked.size());
		for (int i = 0; i < ranked.size(); i++) {
			TripInterest interest = ranked.get(i).getKey();
			int score = ranked.get(i).getValue();
			boolean recommended = i < RECOMMENDED_COUNT && score > 0;
			responses.add(new InterestSuggestionResponse(
					interest.name(),
					interest.getLabelKey(),
					interest.getIcon(),
					recommended));
		}
		return responses;
	}

	private Set<TripInterest> eligibleInterests(String placeType, String destination) {
		Set<TripInterest> eligible = EnumSet.allOf(TripInterest.class);

		switch (placeType) {
			case "city" -> eligible.removeAll(Set.of(
					TripInterest.BEACHES,
					TripInterest.MOUNTAINS,
					TripInterest.WATER_ACTIVITIES));
			case "mountain" -> eligible.removeAll(Set.of(
					TripInterest.NIGHTLIFE,
					TripInterest.BEACHES,
					TripInterest.SHOPPING));
			case "lake" -> {
				eligible.removeAll(Set.of(TripInterest.NIGHTLIFE, TripInterest.SHOPPING));
			}
			case "region" -> applyRegionExclusions(eligible, destination);
			default -> {
			}
		}

		return eligible;
	}

	private void applyRegionExclusions(Set<TripInterest> eligible, String destination) {
		if (containsAny(destination, "beach", "coast", "sea", "ocean", "riviera")) {
			eligible.remove(TripInterest.NIGHTLIFE);
			return;
		}
		if (containsAny(destination, "mountain", "alps", "dolomite", "tatra", "peak", "summit")) {
			eligible.removeAll(Set.of(TripInterest.NIGHTLIFE, TripInterest.BEACHES, TripInterest.SHOPPING));
		}
	}

	private int scoreInterest(
			TripInterest interest,
			String placeType,
			String destination,
			PreferenceProfile profile) {
		int score = 0;

		if ("lake".equals(placeType)) {
			score += boostIf(interest, Set.of(
					TripInterest.WATER_ACTIVITIES,
					TripInterest.SCENIC_VIEWS,
					TripInterest.WILDLIFE,
					TripInterest.WELLNESS));
		}

		if ("region".equals(placeType)) {
			score += regionDestinationBoost(interest, destination);
		}

		if (profile != null) {
			score += paceBoost(interest, profile.getPace());
			score += categoryBoost(interest, profile);
			if (profile.getBudgetStyle() == BudgetTier.ECO) {
				score += boostIf(interest, Set.of(TripInterest.LOCAL_MARKETS, TripInterest.SCENIC_VIEWS));
			}
		}

		return score;
	}

	private int regionDestinationBoost(TripInterest interest, String destination) {
		if (containsAny(destination, "beach", "coast", "sea", "ocean", "riviera", "baltic")) {
			return boostIf(interest, Set.of(TripInterest.BEACHES, TripInterest.WATER_ACTIVITIES, TripInterest.SCENIC_VIEWS));
		}
		if (containsAny(destination, "mountain", "alps", "dolomite", "tatra", "peak", "summit", "highland")) {
			return boostIf(interest, Set.of(TripInterest.MOUNTAINS, TripInterest.HIKING, TripInterest.SCENIC_VIEWS));
		}
		if (containsAny(destination, "lake", "mazury", "tarn", "loch")) {
			return boostIf(interest, Set.of(TripInterest.WATER_ACTIVITIES, TripInterest.SCENIC_VIEWS, TripInterest.WELLNESS));
		}
		return boostIf(interest, Set.of(TripInterest.CULTURE, TripInterest.FOOD, TripInterest.SCENIC_VIEWS));
	}

	private int paceBoost(TripInterest interest, TravelPace pace) {
		if (pace == null) {
			return 0;
		}
		return switch (pace) {
			case ACTIVE -> boostIf(interest, Set.of(TripInterest.HIKING, TripInterest.ADVENTURE, TripInterest.SCENIC_VIEWS));
			case RELAXED -> boostIf(interest, Set.of(TripInterest.WELLNESS, TripInterest.FOOD, TripInterest.NIGHTLIFE));
			case BALANCED -> boostIf(interest, Set.of(TripInterest.CULTURE, TripInterest.SCENIC_VIEWS, TripInterest.FOOD));
		};
	}

	private int categoryBoost(TripInterest interest, PreferenceProfile profile) {
		int score = 0;
		Map<PreferenceCategory, SpendingPriority> priorities = profile.getCategoryPriorities();
		if (priorities == null) {
			return 0;
		}
		if (priorities.get(PreferenceCategory.ATTRACTIONS) == SpendingPriority.INVEST) {
			score += boostIf(interest, Set.of(TripInterest.CULTURE, TripInterest.MUSEUMS));
		}
		if (priorities.get(PreferenceCategory.FOOD) == SpendingPriority.INVEST) {
			score += boostIf(interest, Set.of(TripInterest.FOOD, TripInterest.LOCAL_MARKETS));
		}
		return score;
	}

	private int boostIf(TripInterest interest, Set<TripInterest> boosted) {
		return boosted.contains(interest) ? BOOST : 0;
	}

	private boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private String normalizePlaceType(String placeType) {
		if (placeType == null || placeType.isBlank()) {
			return "region";
		}
		return placeType.trim().toLowerCase(Locale.ROOT);
	}
}

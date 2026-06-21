package com.duszek.pindrop.service;

import com.duszek.pindrop.config.AppProperties;
import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.provider.ai.PopularDestinationSuggestion;
import com.duszek.pindrop.provider.ai.PopularDestinationsAiProvider;
import com.duszek.pindrop.provider.ai.PopularDestinationsRequest;
import com.duszek.pindrop.provider.places.PhotoUrlValidator;
import com.duszek.pindrop.provider.places.PlaceFormatting;
import com.duszek.pindrop.provider.places.PlacePhotoEnricher;
import com.duszek.pindrop.provider.places.PlaceSearchProvider;
import com.duszek.pindrop.provider.places.PlaceSearchResult;
import com.duszek.pindrop.util.AppLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PopularDestinationsService {

	private static final int REQUIRED_COUNT = 6;

	private final PopularDestinationsAiProvider popularDestinationsAiProvider;
	private final PlaceSearchProvider placeSearchProvider;
	private final PlacePhotoEnricher placePhotoEnricher;
	private final PlaceLocalizationService placeLocalizationService;
	private final AppProperties appProperties;

	private final ConcurrentHashMap<String, List<PlaceResponse>> cache = new ConcurrentHashMap<>();

	public List<PlaceResponse> getPopular(int limit, String language) {
		LocalDate today = LocalDate.now();
		String lang = AppLanguage.resolve(language);
		String cacheKey = today + ":" + lang;
		int targetCount = Math.max(limit, REQUIRED_COUNT);

		return cache.computeIfAbsent(cacheKey, key -> loadPopular(today, targetCount, lang));
	}

	private List<PlaceResponse> loadPopular(LocalDate today, int targetCount, String language) {
		LocalDate periodStart = today.withDayOfMonth(1);
		PopularDestinationsRequest request = new PopularDestinationsRequest(
				periodStart, today, targetCount + 4, language);
		List<PopularDestinationSuggestion> suggestions = popularDestinationsAiProvider.suggest(request);

		List<PlaceResponse> resolved = new ArrayList<>();
		Set<String> usedKeys = new HashSet<>();

		for (PopularDestinationSuggestion suggestion : suggestions) {
			if (resolved.size() >= targetCount) {
				break;
			}
			String key = suggestionKey(suggestion);
			if (!usedKeys.add(key)) {
				continue;
			}
			buildPopularResponse(suggestion, language).ifPresent(resolved::add);
		}

		return List.copyOf(resolved);
	}

	private java.util.Optional<PlaceResponse> buildPopularResponse(
			PopularDestinationSuggestion suggestion,
			String language) {
		PlaceSearchResult place = resolvePlace(suggestion);
		if (place == null) {
			return java.util.Optional.empty();
		}

		String photoQuery = suggestion.name() + ", " + suggestion.country();
		if (appProperties.getPlaces().isPhotosEnabled()) {
			place = placePhotoEnricher.enrichWithQuery(place, photoQuery);
		}

		String photoUrl = resolvePhotoUrl(place.photoUrl(), suggestion.photoUrl());
		place = place.withPhotoUrl(photoUrl);
		place = placeLocalizationService.localize(place, language);

		return java.util.Optional.of(toResponse(suggestion, place));
	}

	private static String resolvePhotoUrl(String enrichedPhotoUrl, String curatedPhotoUrl) {
		boolean curatedUsable = hasUsablePhoto(curatedPhotoUrl)
				&& !PhotoUrlValidator.isRemovedUnsplashPhoto(curatedPhotoUrl);
		boolean enrichedUsable = hasUsablePhoto(enrichedPhotoUrl);

		if (curatedUsable) {
			return curatedPhotoUrl;
		}
		if (enrichedUsable) {
			return enrichedPhotoUrl;
		}
		return curatedPhotoUrl;
	}

	private static boolean hasUsablePhoto(String photoUrl) {
		return photoUrl != null
				&& !photoUrl.isBlank()
				&& !PhotoUrlValidator.isLikelyMapImage(photoUrl);
	}

	private PlaceSearchResult resolvePlace(PopularDestinationSuggestion suggestion) {
		String combinedQuery = suggestion.name() + ", " + suggestion.country();
		List<PlaceSearchResult> results = placeSearchProvider.search(combinedQuery, 10);
		PlaceSearchResult match = pickBestMatch(suggestion, results);
		if (match != null) {
			return match;
		}
		results = placeSearchProvider.search(suggestion.name(), 10);
		return pickBestMatch(suggestion, results);
	}

	private PlaceSearchResult pickBestMatch(
			PopularDestinationSuggestion suggestion,
			List<PlaceSearchResult> results) {
		if (results == null || results.isEmpty()) {
			return null;
		}

		return results.stream()
				.max(Comparator.comparingInt(place -> scoreMatch(suggestion, place)))
				.filter(place -> scoreMatch(suggestion, place) > 0)
				.orElse(null);
	}

	private static int scoreMatch(PopularDestinationSuggestion suggestion, PlaceSearchResult place) {
		int score = 0;
		String targetName = suggestion.name().toLowerCase(Locale.ROOT);
		String targetCountry = suggestion.country().toLowerCase(Locale.ROOT);
		String placeName = place.name() != null ? place.name().toLowerCase(Locale.ROOT) : "";
		String displayName = place.displayName() != null ? place.displayName().toLowerCase(Locale.ROOT) : "";

		if (placeName.equals(targetName) || displayName.startsWith(targetName + ",")) {
			score += 300;
		} else if (placeName.contains(targetName) || targetName.contains(placeName)) {
			score += 140;
		} else {
			score += tokenOverlapScore(targetName, placeName, displayName);
		}

		if (countryMatches(place.country(), targetCountry)) {
			score += 200;
		} else {
			score -= 400;
		}

		if (place.countryCode() != null && targetCountry.length() == 2
				&& place.countryCode().equalsIgnoreCase(targetCountry)) {
			score += 250;
		}

		if (suggestion.placeType() != null && suggestion.placeType().equals(place.placeType())) {
			score += 80;
		}

		if ("city".equals(suggestion.placeType())
				&& ("hamlet".equals(place.placeType()) || "village".equals(place.placeType()))) {
			score -= 180;
		}

		if ("region".equals(suggestion.placeType()) && "city".equals(place.placeType())) {
			score -= 40;
		}

		if (placeName.contains("path") || placeName.contains("località") || placeName.contains("localita")) {
			score -= 120;
		}

		return score;
	}

	private static int tokenOverlapScore(String targetName, String placeName, String displayName) {
		int score = 0;
		for (String token : targetName.split("\\s+")) {
			if (token.length() < 4) {
				continue;
			}
			if (placeName.contains(token) || displayName.contains(token)) {
				score += 70;
			}
		}
		return score;
	}

	private static boolean countryMatches(String placeCountry, String targetCountry) {
		if (placeCountry == null) {
			return false;
		}
		String normalizedPlace = placeCountry.toLowerCase(Locale.ROOT);
		String normalizedTarget = targetCountry.toLowerCase(Locale.ROOT);
		if (normalizedPlace.contains(normalizedTarget) || normalizedTarget.contains(normalizedPlace)) {
			return true;
		}
		return countryAliases(normalizedTarget).stream().anyMatch(normalizedPlace::contains);
	}

	private static List<String> countryAliases(String country) {
		return switch (country) {
			case "united states", "usa", "us" -> List.of("united states", "usa");
			case "united kingdom", "uk", "great britain" -> List.of("united kingdom", "great britain");
			case "greece", "hellas" -> List.of("greece", "hellas", "elláda");
			case "spain", "españa" -> List.of("spain", "españa");
			case "italy", "italia" -> List.of("italy", "italia");
			case "switzerland", "schweiz", "suisse" -> List.of("switzerland", "schweiz", "suisse", "svizzera");
			case "poland", "polska" -> List.of("poland", "polska");
			case "indonesia" -> List.of("indonesia");
			default -> List.of(country);
		};
	}

	private PlaceResponse toResponse(PopularDestinationSuggestion suggestion, PlaceSearchResult place) {
		String country = place.country() != null ? place.country() : suggestion.country();
		String displayName = PlaceFormatting.formatDisplayName(suggestion.name(), place.region(), country);
		String placeType = place.placeType() != null ? place.placeType() : suggestion.placeType();

		return PlaceResponse.builder()
				.name(suggestion.name())
				.region(place.region())
				.country(country)
				.countryCode(place.countryCode())
				.displayName(displayName)
				.lat(place.lat())
				.lng(place.lng())
				.photoUrl(place.photoUrl())
				.placeType(placeType)
				.build();
	}

	private static String suggestionKey(PopularDestinationSuggestion suggestion) {
		return suggestion.name().toLowerCase(Locale.ROOT) + "|" + suggestion.country().toLowerCase(Locale.ROOT);
	}
}

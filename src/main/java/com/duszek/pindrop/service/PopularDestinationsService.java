package com.duszek.pindrop.service;

import com.duszek.pindrop.config.AppProperties;
import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.provider.ai.PopularDestinationSuggestion;
import com.duszek.pindrop.provider.ai.PopularDestinationsAiProvider;
import com.duszek.pindrop.provider.ai.PopularDestinationsRequest;
import com.duszek.pindrop.provider.places.CuratedPlaceCatalog;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PopularDestinationsService {

	private static final String PLACEHOLDER_PHOTO =
			"https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=720&q=80";

	private static final String CACHE_VERSION = "v5";

	private final PopularDestinationsAiProvider popularDestinationsAiProvider;
	private final PlaceSearchProvider placeSearchProvider;
	private final PlacePhotoEnricher placePhotoEnricher;
	private final PlaceLocalizationService placeLocalizationService;
	private final AppProperties appProperties;

	private final ConcurrentHashMap<String, List<PlaceResponse>> cache = new ConcurrentHashMap<>();

	public List<PlaceResponse> getPopular(int limit, String language) {
		LocalDate today = LocalDate.now();
		String lang = AppLanguage.resolve(language);
		int resolvedLimit = Math.clamp(limit, 1, 4);
		String cacheKey = CACHE_VERSION + ":" + today + ":" + lang + ":" + resolvedLimit;

		return cache.computeIfAbsent(cacheKey, key -> loadPopular(today, resolvedLimit, lang));
	}

	private List<PlaceResponse> loadPopular(LocalDate today, int limit, String language) {
		LocalDate periodStart = today.withDayOfMonth(1);
		PopularDestinationsRequest request = new PopularDestinationsRequest(periodStart, today, limit, language);
		List<PopularDestinationSuggestion> suggestions = popularDestinationsAiProvider.suggest(request);

		List<PlaceResponse> resolved = new ArrayList<>();
		for (PopularDestinationSuggestion suggestion : suggestions) {
			PlaceSearchResult place = resolvePlace(suggestion);
			if (place == null) {
				continue;
			}
			if (appProperties.getPlaces().isPhotosEnabled()) {
				String photoQuery = suggestion.name() + ", " + suggestion.country();
				place = placePhotoEnricher.enrichWithQuery(place, photoQuery);
			}
			place = placeLocalizationService.localizeForSearch(place, language);
			resolved.add(toResponse(place, suggestion));
			if (resolved.size() >= limit) {
				break;
			}
		}
		return List.copyOf(resolved);
	}

	private PlaceSearchResult resolvePlace(PopularDestinationSuggestion suggestion) {
		PlaceSearchResult curated = CuratedPlaceCatalog.findForSuggestion(suggestion.name(), suggestion.country());
		if (curated != null) {
			return withSuggestionIdentity(suggestion, curated);
		}

		PlaceSearchResult coordinates = findCoordinates(suggestion);
		if (coordinates == null) {
			return null;
		}

		return withSuggestionIdentity(suggestion, coordinates);
	}

	private PlaceSearchResult findCoordinates(PopularDestinationSuggestion suggestion) {
		String combinedQuery = suggestion.name() + ", " + suggestion.country();
		List<PlaceSearchResult> results = placeSearchProvider.search(combinedQuery, 8);
		PlaceSearchResult match = pickByCountry(suggestion, results);
		if (match != null) {
			return match;
		}

		return pickByCountry(suggestion, placeSearchProvider.search(suggestion.name(), 8));
	}

	private PlaceSearchResult pickByCountry(
			PopularDestinationSuggestion suggestion,
			List<PlaceSearchResult> results) {
		if (results == null || results.isEmpty()) {
			return null;
		}

		String targetCountry = suggestion.country().toLowerCase(Locale.ROOT);
		return results.stream()
				.filter(place -> countryMatches(place.country(), targetCountry))
				.findFirst()
				.orElse(null);
	}

	private static PlaceSearchResult withSuggestionIdentity(
			PopularDestinationSuggestion suggestion,
			PlaceSearchResult source) {
		String region = source.region();
		String country = source.country() != null ? source.country() : suggestion.country();
		return new PlaceSearchResult(
				suggestion.name(),
				region,
				country,
				source.countryCode(),
				PlaceFormatting.formatDisplayName(suggestion.name(), region, country),
				source.lat(),
				source.lng(),
				source.photoUrl(),
				suggestion.placeType() != null ? suggestion.placeType() : source.placeType());
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
			case "united kingdom", "uk", "great britain" -> List.of("united kingdom", "great britain", "scotland");
			case "greece", "hellas" -> List.of("greece", "hellas", "elláda");
			case "spain", "españa" -> List.of("spain", "españa");
			case "italy", "italia" -> List.of("italy", "italia");
			case "switzerland", "schweiz", "suisse" -> List.of("switzerland", "schweiz", "suisse", "svizzera");
			case "poland", "polska" -> List.of("poland", "polska");
			case "indonesia" -> List.of("indonesia");
			case "canada" -> List.of("canada");
			case "argentina" -> List.of("argentina");
			default -> List.of(country);
		};
	}

	private PlaceResponse toResponse(PlaceSearchResult place, PopularDestinationSuggestion suggestion) {
		String placeType = suggestion.placeType() != null ? suggestion.placeType() : place.placeType();
		return PlaceResponse.builder()
				.name(place.name())
				.region(place.region())
				.country(place.country())
				.countryCode(place.countryCode())
				.displayName(place.displayName())
				.lat(place.lat())
				.lng(place.lng())
				.photoUrl(resolvePhotoUrl(place.photoUrl()))
				.placeType(placeType)
				.build();
	}

	private static String resolvePhotoUrl(String photoUrl) {
		if (photoUrl == null || photoUrl.isBlank()) {
			return PLACEHOLDER_PHOTO;
		}
		if (PhotoUrlValidator.isLikelyMapImage(photoUrl) || PhotoUrlValidator.isRemovedUnsplashPhoto(photoUrl)) {
			return PLACEHOLDER_PHOTO;
		}
		return photoUrl;
	}
}

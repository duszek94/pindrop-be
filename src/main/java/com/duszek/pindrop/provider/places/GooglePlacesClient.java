package com.duszek.pindrop.provider.places;

import com.duszek.pindrop.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class GooglePlacesClient {

	private static final String FIELD_MASK =
			"places.displayName,places.formattedAddress,places.location,places.addressComponents,places.photos";

	private final AppProperties appProperties;
	private final WebClient webClient;

	public GooglePlacesClient(AppProperties appProperties) {
		this.appProperties = appProperties;
		this.webClient = WebClient.builder()
				.baseUrl("https://places.googleapis.com")
				.defaultHeader("Content-Type", "application/json")
				.build();
	}

	public boolean isConfigured() {
		return appProperties.getPlaces().getApiKey() != null
				&& !appProperties.getPlaces().getApiKey().isBlank();
	}

	public List<PlaceSearchResult> search(String query, int limit) {
		if (!isConfigured() || query == null || query.isBlank()) {
			return List.of();
		}
		try {
			Map<String, Object> body = Map.of(
					"textQuery", query,
					"maxResultCount", Math.min(limit, 20));

			@SuppressWarnings("unchecked")
			Map<String, Object> response = webClient.post()
					.uri("/v1/places:searchText")
					.header("X-Goog-Api-Key", appProperties.getPlaces().getApiKey())
					.header("X-Goog-FieldMask", FIELD_MASK)
					.bodyValue(body)
					.retrieve()
					.bodyToMono(Map.class)
					.block();

			if (response == null || !(response.get("places") instanceof List<?> places)) {
				return List.of();
			}

			List<PlaceSearchResult> results = new ArrayList<>();
			for (Object placeObject : places) {
				if (placeObject instanceof Map<?, ?> placeMap) {
					@SuppressWarnings("unchecked")
					PlaceSearchResult result = toPlaceSearchResult((Map<String, Object>) placeMap);
					if (result != null) {
						results.add(result);
					}
				}
				if (results.size() >= limit) {
					break;
				}
			}
			return results;
		} catch (Exception ex) {
			log.warn("Google Places search failed for '{}': {}", query, ex.getMessage());
			return List.of();
		}
	}

	public Optional<String> findPhotoUrl(String query) {
		return search(query, 1).stream()
				.map(PlaceSearchResult::photoUrl)
				.filter(url -> url != null && !url.isBlank())
				.findFirst();
	}

	@SuppressWarnings("unchecked")
	private PlaceSearchResult toPlaceSearchResult(Map<String, Object> place) {
		String name = extractDisplayName(place.get("displayName"));
		if (name == null || name.isBlank()) {
			return null;
		}

		Map<String, Object> location = place.get("location") instanceof Map<?, ?> loc
				? (Map<String, Object>) loc
				: Map.of();
		double lat = parseDouble(location.get("latitude"));
		double lng = parseDouble(location.get("longitude"));

		List<Map<String, Object>> addressComponents = place.get("addressComponents") instanceof List<?> list
				? (List<Map<String, Object>>) list
				: List.of();

		String country = extractAddressComponent(addressComponents, "country");
		String countryCode = extractShortAddressComponent(addressComponents, "country");
		String region = firstNonBlank(
				extractAddressComponent(addressComponents, "administrative_area_level_1"),
				extractAddressComponent(addressComponents, "administrative_area_level_2"));

		String photoUrl = extractPhotoUrl(place.get("photos"));

		return PlaceSearchResult.of(name, region, country, countryCode, lat, lng, photoUrl);
	}

	@SuppressWarnings("unchecked")
	private String extractPhotoUrl(Object photosObject) {
		if (!(photosObject instanceof List<?> photos) || photos.isEmpty()) {
			return null;
		}
		Object firstPhoto = photos.get(0);
		if (!(firstPhoto instanceof Map<?, ?> photoMap)) {
			return null;
		}
		String photoName = PlaceFormatting.blankToNull(String.valueOf(photoMap.get("name")));
		return photoName != null ? resolvePhotoMediaUrl(photoName).orElse(null) : null;
	}

	private Optional<String> resolvePhotoMediaUrl(String photoResourceName) {
		try {
			return webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/v1/{photoResourceName}/media")
							.queryParam("maxHeightPx", 480)
							.queryParam("maxWidthPx", 720)
							.queryParam("skipHttpRedirect", true)
							.queryParam("key", appProperties.getPlaces().getApiKey())
							.build(photoResourceName))
					.exchangeToMono(response -> {
						if (response.statusCode().is3xxRedirection()) {
							return Mono.justOrEmpty(response.headers().header(HttpHeaders.LOCATION).stream().findFirst());
						}
						return response.bodyToMono(Void.class).then(Mono.empty());
					})
					.blockOptional()
					.filter(url -> !url.isBlank());
		} catch (Exception ex) {
			log.debug("Failed to resolve Google photo URL: {}", ex.getMessage());
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private static String extractDisplayName(Object displayNameObject) {
		if (displayNameObject instanceof Map<?, ?> displayNameMap) {
			return PlaceFormatting.blankToNull(String.valueOf(displayNameMap.get("text")));
		}
		return PlaceFormatting.blankToNull(String.valueOf(displayNameObject));
	}

	private static String extractAddressComponent(List<Map<String, Object>> components, String type) {
		for (Map<String, Object> component : components) {
			if (component.get("types") instanceof List<?> types && types.contains(type)) {
				return PlaceFormatting.blankToNull(String.valueOf(component.get("longText")));
			}
		}
		return null;
	}

	private static String extractShortAddressComponent(List<Map<String, Object>> components, String type) {
		for (Map<String, Object> component : components) {
			if (component.get("types") instanceof List<?> types && types.contains(type)) {
				return PlaceFormatting.blankToNull(String.valueOf(component.get("shortText")));
			}
		}
		return null;
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private static double parseDouble(Object value) {
		if (value == null) {
			return 0;
		}
		try {
			return Double.parseDouble(String.valueOf(value));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
}

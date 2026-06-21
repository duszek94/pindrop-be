package com.duszek.pindrop.service;

import com.duszek.pindrop.config.AppProperties;
import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.provider.places.NominatimGeocodingClient;
import com.duszek.pindrop.provider.places.PlacePhotoEnricher;
import com.duszek.pindrop.provider.places.PlaceSearchProvider;
import com.duszek.pindrop.provider.places.PlaceSearchResult;
import com.duszek.pindrop.util.DebugSessionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceService {

	private final PlaceSearchProvider placeSearchProvider;
	private final PlacePhotoEnricher placePhotoEnricher;
	private final PlaceLocalizationService placeLocalizationService;
	private final NominatimGeocodingClient nominatimGeocodingClient;
	private final AppProperties appProperties;

	public List<PlaceResponse> search(String query, int limit, String language) {
		List<PlaceSearchResult> results = placeSearchProvider.search(query, limit, language);
		List<PlaceResponse> response = results.stream()
				.map(place -> placeLocalizationService.localizeForSearch(place, language))
				.map(this::toResponse)
				.toList();

		// #region agent log
		DebugSessionLog.log("H5", "PlaceService.search", "place search response", new LinkedHashMap<>(Map.of(
				"query", query,
				"language", language,
				"resultCount", response.size(),
				"topName", response.isEmpty() ? "" : response.getFirst().getName())));
		// #endregion

		return response;
	}

	public PlaceResponse reverse(double lat, double lng, String language) {
		PlaceSearchResult place = nominatimGeocodingClient.reverse(lat, lng, language)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
		if (appProperties.getPlaces().isPhotosEnabled()) {
			place = placePhotoEnricher.enrich(List.of(place)).getFirst();
		}
		place = placeLocalizationService.localize(place, language);
		return toResponse(place);
	}

	private PlaceResponse toResponse(PlaceSearchResult place) {
		return PlaceResponse.builder()
				.name(place.name())
				.region(place.region())
				.country(place.country())
				.countryCode(place.countryCode())
				.displayName(place.displayName())
				.lat(place.lat())
				.lng(place.lng())
				.photoUrl(place.photoUrl())
				.placeType(place.placeType())
				.build();
	}
}

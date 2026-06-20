package com.duszek.pindrop.service;

import com.duszek.pindrop.config.AppProperties;
import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.provider.places.GooglePlacePhotoEnricher;
import com.duszek.pindrop.provider.places.PlaceSearchProvider;
import com.duszek.pindrop.provider.places.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

	private final PlaceSearchProvider placeSearchProvider;
	private final GooglePlacePhotoEnricher googlePlacePhotoEnricher;
	private final AppProperties appProperties;

	public List<PlaceResponse> search(String query, int limit) {
		List<PlaceSearchResult> results = placeSearchProvider.search(query, limit);
		if (appProperties.getPlaces().isPhotosEnabled()) {
			results = googlePlacePhotoEnricher.enrich(results);
		}
		return results.stream()
				.map(this::toResponse)
				.toList();
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
				.build();
	}
}

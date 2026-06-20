package com.duszek.pindrop.provider.places;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.places.provider", havingValue = "google")
public class GooglePlaceSearchProvider implements PlaceSearchProvider {

	private final GooglePlacesClient googlePlacesClient;

	public GooglePlaceSearchProvider(GooglePlacesClient googlePlacesClient) {
		this.googlePlacesClient = googlePlacesClient;
	}

	@Override
	public List<PlaceSearchResult> search(String query, int limit) {
		return googlePlacesClient.search(query, limit);
	}
}

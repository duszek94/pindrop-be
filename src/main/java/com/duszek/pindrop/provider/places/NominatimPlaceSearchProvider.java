package com.duszek.pindrop.provider.places;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.places.provider", havingValue = "nominatim")
public class NominatimPlaceSearchProvider implements PlaceSearchProvider {

	private final NominatimGeocodingClient nominatimGeocodingClient;

	public NominatimPlaceSearchProvider(NominatimGeocodingClient nominatimGeocodingClient) {
		this.nominatimGeocodingClient = nominatimGeocodingClient;
	}

	@Override
	public List<PlaceSearchResult> search(String query, int limit) {
		return nominatimGeocodingClient.search(query, limit);
	}
}

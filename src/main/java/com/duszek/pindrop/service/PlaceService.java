package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.provider.places.PlaceSearchProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {

	private final PlaceSearchProvider placeSearchProvider;

	public List<PlaceResponse> search(String query, int limit) {
		return placeSearchProvider.search(query, limit).stream()
				.map(place -> new PlaceResponse(
						place.name(),
						place.country(),
						place.displayName(),
						place.lat(),
						place.lng()))
				.toList();
	}
}

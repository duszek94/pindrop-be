package com.duszek.pindrop.provider.places;

import java.util.List;

public interface PlaceSearchProvider {

	List<PlaceSearchResult> search(String query, int limit);
}

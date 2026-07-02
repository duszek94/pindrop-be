package com.duszek.pindrop.service;

import com.duszek.pindrop.config.AppProperties;
import com.duszek.pindrop.dto.planning.PlaceResponse;
import com.duszek.pindrop.provider.ai.PopularDestinationSuggestion;
import com.duszek.pindrop.provider.ai.PopularDestinationsAiProvider;
import com.duszek.pindrop.provider.ai.PopularDestinationsRequest;
import com.duszek.pindrop.provider.places.PlacePhotoEnricher;
import com.duszek.pindrop.provider.places.PlaceSearchProvider;
import com.duszek.pindrop.provider.places.PlaceSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PopularDestinationsServiceTest {

	@Mock
	private PopularDestinationsAiProvider popularDestinationsAiProvider;

	@Mock
	private PlaceSearchProvider placeSearchProvider;

	@Mock
	private PlacePhotoEnricher placePhotoEnricher;

	@Mock
	private PlaceLocalizationService placeLocalizationService;

	private PopularDestinationsService service;

	@BeforeEach
	void setUp() {
		AppProperties appProperties = new AppProperties();
		appProperties.getPlaces().setPhotosEnabled(false);

		service = new PopularDestinationsService(
				popularDestinationsAiProvider,
				placeSearchProvider,
				placePhotoEnricher,
				placeLocalizationService,
				appProperties);
	}

	@Test
	void keepsAiSuggestionNameWhenGeocoderReturnsJunk() {
		when(popularDestinationsAiProvider.suggest(any(PopularDestinationsRequest.class)))
				.thenReturn(List.of(new PopularDestinationSuggestion("Zermatt", "Switzerland", "mountain")));

		when(placeSearchProvider.search(anyString(), anyInt()))
				.thenReturn(List.of(
						PlaceSearchResult.of("Saint-Sulpice", "Vaud", "Switzerland", "ch", 46.77, 6.71)));

		when(placeLocalizationService.localize(any(), anyString())).thenAnswer(invocation -> invocation.getArgument(0));

		List<PlaceResponse> popular = service.getPopular(1, "en");

		assertThat(popular).hasSize(1);
		assertThat(popular.get(0).getName()).isEqualTo("Zermatt");
	}

	@Test
	void keepsJuneTemplateNamesInsteadOfGeocoderJunk() {
		when(popularDestinationsAiProvider.suggest(any(PopularDestinationsRequest.class)))
				.thenReturn(List.of(
						new PopularDestinationSuggestion("Santorini", "Greece", "city"),
						new PopularDestinationSuggestion("Bali", "Indonesia", "city"),
						new PopularDestinationSuggestion("Lake Geneva", "Switzerland", "lake"),
						new PopularDestinationSuggestion("Scottish Highlands", "United Kingdom", "region")));

		when(placeLocalizationService.localize(any(), anyString())).thenAnswer(invocation -> invocation.getArgument(0));

		List<PlaceResponse> popular = service.getPopular(4, "en");

		assertThat(popular).extracting(PlaceResponse::getName)
				.containsExactly("Santorini", "Bali", "Lake Geneva", "Scottish Highlands");
	}

	@Test
	void replacesMapImagesWithPlaceholder() {
		when(popularDestinationsAiProvider.suggest(any(PopularDestinationsRequest.class)))
				.thenReturn(List.of(new PopularDestinationSuggestion("Barcelona", "Spain", "city")));

		when(placeLocalizationService.localize(any(), anyString())).thenAnswer(invocation -> {
			PlaceSearchResult place = invocation.getArgument(0);
			return place.withPhotoUrl(
					"https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Spain_location_map.svg/720px-Spain_location_map.svg.png");
		});

		List<PlaceResponse> popular = service.getPopular(1, "en");

		assertThat(popular).hasSize(1);
		assertThat(popular.get(0).getPhotoUrl()).doesNotContain("location_map");
		assertThat(popular.get(0).getPhotoUrl()).contains("unsplash.com");
	}
}

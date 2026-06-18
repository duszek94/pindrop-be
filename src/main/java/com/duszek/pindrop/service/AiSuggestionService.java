package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.dashboard.DashboardResponse;
import com.duszek.pindrop.dto.dashboard.SuggestedDestinationResponse;
import com.duszek.pindrop.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiSuggestionService {

	private static final List<SuggestedDestinationResponse> ALL_SUGGESTIONS = List.of(
			SuggestedDestinationResponse.builder()
					.name("Kyoto").country("Japan").reason("Temples, gardens, and culture")
					.imageUrl("https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800")
					.lat(35.0116).lng(135.7681).build(),
			SuggestedDestinationResponse.builder()
					.name("Lisbon").country("Portugal").reason("Coastal charm and great food")
					.imageUrl("https://images.unsplash.com/photo-1555881400-74d7aca8cce8?w=800")
					.lat(38.7223).lng(-9.1393).build(),
			SuggestedDestinationResponse.builder()
					.name("Banff").country("Canada").reason("Stunning nature and hiking trails")
					.imageUrl("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800")
					.lat(51.1784).lng(-115.5708).build(),
			SuggestedDestinationResponse.builder()
					.name("Marrakech").country("Morocco").reason("Markets, architecture, and cuisine")
					.imageUrl("https://images.unsplash.com/photo-1518548419970-58e050314966?w=800")
					.lat(31.6295).lng(-7.9811).build(),
			SuggestedDestinationResponse.builder()
					.name("Queenstown").country("New Zealand").reason("Adventure capital with scenic views")
					.imageUrl("https://images.unsplash.com/photo-1507692049794-0acdacaa340e?w=800")
					.lat(-45.0312).lng(168.6626).build());

	@Transactional(readOnly = true)
	public List<SuggestedDestinationResponse> getSuggestedDestinations(User user) {
		List<String> interests = user.getInterests() != null ? user.getInterests() : List.of();
		if (interests.isEmpty()) {
			return ALL_SUGGESTIONS.subList(0, Math.min(3, ALL_SUGGESTIONS.size()));
		}

		Set<SuggestedDestinationResponse> matched = new LinkedHashSet<>();
		for (String interest : interests) {
			String normalized = interest.toLowerCase();
			for (SuggestedDestinationResponse suggestion : ALL_SUGGESTIONS) {
				if (matchesInterest(normalized, suggestion)) {
					matched.add(suggestion);
				}
			}
		}

		if (matched.isEmpty()) {
			return ALL_SUGGESTIONS.subList(0, Math.min(3, ALL_SUGGESTIONS.size()));
		}
		return new ArrayList<>(matched);
	}

	private boolean matchesInterest(String interest, SuggestedDestinationResponse suggestion) {
		String haystack = (suggestion.getName() + " " + suggestion.getCountry() + " " + suggestion.getReason())
				.toLowerCase();
		return switch (interest) {
			case "culture", "history" -> haystack.contains("culture") || haystack.contains("architecture")
					|| haystack.contains("temple");
			case "food", "cuisine" -> haystack.contains("food") || haystack.contains("cuisine");
			case "nature", "hiking", "adventure" -> haystack.contains("nature") || haystack.contains("hiking")
					|| haystack.contains("adventure") || haystack.contains("scenic");
			default -> haystack.contains(interest);
		};
	}
}

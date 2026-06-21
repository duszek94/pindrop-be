package com.duszek.pindrop.provider.places;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class WikimediaPhotoEnricher {

	private final WebClient webClient = WebClient.builder()
			.baseUrl("https://en.wikipedia.org")
			.defaultHeader("User-Agent", "Pindrop/1.0 (trip-planning-dev)")
			.build();

	public Optional<String> findPhotoUrl(String name, String country) {
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		String searchTerm = country != null && !country.isBlank() ? name + " " + country : name;
		return findPhotoUrl(searchTerm);
	}

	public Optional<String> findPhotoUrl(String searchTerm) {
		if (searchTerm == null || searchTerm.isBlank()) {
			return Optional.empty();
		}
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> response = webClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/w/api.php")
							.queryParam("action", "query")
							.queryParam("format", "json")
							.queryParam("generator", "search")
							.queryParam("gsrsearch", searchTerm)
							.queryParam("gsrlimit", 5)
							.queryParam("prop", "pageimages")
							.queryParam("piprop", "thumbnail")
							.queryParam("pithumbsize", 720)
							.build())
					.retrieve()
					.bodyToMono(Map.class)
					.block();

			return extractBestThumbnail(response);
		} catch (Exception ex) {
			log.debug("Wikimedia photo lookup failed for '{}': {}", searchTerm, ex.getMessage());
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<String> extractBestThumbnail(Map<String, Object> response) {
		if (response == null) {
			return Optional.empty();
		}
		Object queryObject = response.get("query");
		if (!(queryObject instanceof Map<?, ?> queryMap)) {
			return Optional.empty();
		}
		Object pagesObject = queryMap.get("pages");
		if (!(pagesObject instanceof Map<?, ?> pagesMap) || pagesMap.isEmpty()) {
			return Optional.empty();
		}

		for (Object pageObject : pagesMap.values()) {
			if (!(pageObject instanceof Map<?, ?> pageMap)) {
				continue;
			}
			Object thumbnailObject = pageMap.get("thumbnail");
			if (thumbnailObject instanceof Map<?, ?> thumbnailMap) {
				Object source = thumbnailMap.get("source");
				if (source != null) {
					String url = String.valueOf(source).trim();
					if (!url.isBlank() && !PhotoUrlValidator.isLikelyMapImage(url)) {
						return Optional.of(url);
					}
				}
			}
		}
		return Optional.empty();
	}
}

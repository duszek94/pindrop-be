package com.duszek.pindrop.provider.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

public final class PopularDestinationsJsonParser {

	private PopularDestinationsJsonParser() {
	}

	public static List<PopularDestinationSuggestion> parse(
			JsonMapper jsonMapper,
			String raw,
			PopularDestinationsRequest request,
			PopularDestinationsTemplateEngine templateEngine) {
		try {
			String json = extractJson(raw);
			JsonNode root = jsonMapper.readTree(json);
			JsonNode destinations = root.get("destinations");
			if (destinations == null || !destinations.isArray() || destinations.isEmpty()) {
				return templateEngine.suggest(request);
			}

			List<PopularDestinationSuggestion> suggestions = new ArrayList<>();
			for (JsonNode node : destinations) {
				String name = text(node, "name");
				String country = text(node, "country");
				if (name == null || country == null) {
					continue;
				}
				suggestions.add(new PopularDestinationSuggestion(name, country, text(node, "placeType")));
			}

			if (suggestions.isEmpty()) {
				return templateEngine.suggest(request);
			}
			return suggestions.stream().limit(request.limit()).toList();
		} catch (Exception ex) {
			return templateEngine.suggest(request);
		}
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		String text = value.asString().trim();
		return text.isEmpty() ? null : text;
	}

	private static String extractJson(String raw) {
		int start = raw.indexOf('{');
		int end = raw.lastIndexOf('}');
		if (start >= 0 && end > start) {
			return raw.substring(start, end + 1);
		}
		return raw;
	}
}

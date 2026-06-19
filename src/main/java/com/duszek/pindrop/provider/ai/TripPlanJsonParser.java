package com.duszek.pindrop.provider.ai;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class TripPlanJsonParser {

	private TripPlanJsonParser() {
	}

	public static TripPlanGenerationResult parse(
			JsonMapper jsonMapper,
			String raw,
			TripPlanGenerationRequest request,
			TripPlanTemplateEngine templateEngine) {
		try {
			String json = extractJson(raw);
			JsonNode root = jsonMapper.readTree(json);
			if (!root.has("proposals")) {
				return templateEngine.generateProposals(request);
			}
			return jsonMapper.treeToValue(root, TripPlanGenerationResult.class);
		} catch (Exception ex) {
			return templateEngine.generateProposals(request);
		}
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

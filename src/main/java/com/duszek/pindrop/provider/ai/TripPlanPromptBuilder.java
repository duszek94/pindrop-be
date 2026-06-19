package com.duszek.pindrop.provider.ai;

public final class TripPlanPromptBuilder {

	private TripPlanPromptBuilder() {
	}

	public static String buildGenerationPrompt(TripPlanGenerationRequest request) {
		return """
				You are a travel planner. Return JSON only with this shape:
				{"proposals":[{"type":"RELAXED|BALANCED|INTENSE","title":"...","summary":"...","estimatedCostUsd":1250,"recommended":false,"highlights":["..."],"days":[{"dayNumber":1,"date":"%s","activities":[{"startTime":"09:00","activityType":"ACTIVITY|FOOD","title":"...","description":"...","placeName":"...","tempC":24}]}]}]}
				Generate exactly 3 proposals (RELAXED, BALANCED, INTENSE). Mark BALANCED as recommended.
				Destination: %s
				Dates: %s to %s
				Budget: %s
				Pace: %s
				Interests: %s
				""".formatted(
				request.startDate(),
				request.destination(),
				request.startDate(),
				request.endDate(),
				request.budgetTier(),
				request.pace(),
				request.interests());
	}
}

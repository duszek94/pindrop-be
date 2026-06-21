package com.duszek.pindrop.provider.ai;

import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.entity.BudgetTier;
import com.duszek.pindrop.entity.PaceIntensity;
import com.duszek.pindrop.entity.PreferenceCategory;
import com.duszek.pindrop.entity.SpendingPriority;
import com.duszek.pindrop.entity.TransportMode;
import com.duszek.pindrop.entity.TravelPace;
import com.duszek.pindrop.util.PreferenceProfileUtils;

public final class TripPlanPromptBuilder {

	private TripPlanPromptBuilder() {
	}

	public static String buildGenerationPrompt(TripPlanGenerationRequest request) {
		PreferenceProfile profile = request.preferenceProfile();
		boolean includesCarCosts = PreferenceProfileUtils.hasCarMode(profile);
		String additionalRequirements = buildAdditionalRequirementsSection(profile);
		String preferenceBlock = buildPreferenceBlock(profile, includesCarCosts);

		return """
				You are a travel planner. Return JSON only with this shape:
				{"proposals":[{"type":"RELAXED|BALANCED|INTENSE","title":"...","summary":"...","estimatedCostUsd":1250,"recommended":false,"highlights":["..."],"costBreakdown":{"estimatedTotal":{"min":800,"max":1200,"currency":"EUR","confidence":"estimated"},"breakdown":{"accommodation":{"min":300,"max":450},"transport":{"min":120,"max":180,"includesCarCosts":%s,"sub":{"publicTransit":{"min":40,"max":60},"fuel":{"min":0,"max":0},"parking":{"min":0,"max":0},"carRental":{"min":0,"max":0}}},"food":{"min":200,"max":280},"attractions":{"min":80,"max":150}}},"days":[{"dayNumber":1,"date":"%s","activities":[{"startTime":"09:00","activityType":"ACTIVITY|FOOD|TRANSPORT|ACCOMMODATION","title":"...","description":"...","placeName":"...","tempC":24}]}]}]}
				Generate exactly 3 proposals (RELAXED, BALANCED, INTENSE). Mark BALANCED as recommended.
				Proposal types are itinerary variants; user travel pace constrains activity density and style inside each variant.
				Destination: %s
				Dates: %s to %s
				%s
				%s
				Interests: %s
				When includesCarCosts is false, omit fuel and parking sub-costs or set them to zero and excludesCarCosts in transport.
				Treat additional requirements as hard constraints where feasible; mention in summary if any cannot be met.
				""".formatted(
				includesCarCosts,
				request.startDate(),
				request.destination(),
				request.startDate(),
				request.endDate(),
				preferenceBlock,
				additionalRequirements,
				request.interests());
	}

	private static String buildPreferenceBlock(PreferenceProfile profile, boolean includesCarCosts) {
		if (profile == null) {
			return "Preferences: not specified";
		}
		StringBuilder builder = new StringBuilder("Preferences:\n");
		builder.append("- Budget style: ").append(formatBudgetStyle(profile.getBudgetStyle())).append('\n');
		builder.append("- Travel pace: ").append(profile.getPace());
		if (profile.getPace() == TravelPace.ACTIVE && profile.getPaceIntensity() != null) {
			builder.append(" (").append(profile.getPaceIntensity()).append(')');
		}
		builder.append('\n');
		builder.append("- Transport modes: ").append(profile.getTransportModes()).append('\n');
		builder.append("- Avoid flying when train is reasonable: ")
				.append(profile.isAvoidFlyingWhenTrainReasonable())
				.append('\n');
		builder.append("- Includes car-related costs (fuel/parking): ").append(includesCarCosts).append('\n');
		if (profile.getCategoryPriorities() != null) {
			for (PreferenceCategory category : PreferenceCategory.values()) {
				SpendingPriority priority = profile.getCategoryPriorities().get(category);
				builder.append("- ").append(category).append(" priority: ")
						.append(priority != null ? priority : SpendingPriority.BALANCED)
						.append('\n');
			}
		}
		return builder.toString().trim();
	}

	private static String buildAdditionalRequirementsSection(PreferenceProfile profile) {
		if (profile == null || profile.getAdditionalRequirements() == null
				|| profile.getAdditionalRequirements().isBlank()) {
			return "";
		}
		return "Additional user requirements (must respect):\n- " + profile.getAdditionalRequirements();
	}

	private static String formatBudgetStyle(BudgetTier budgetStyle) {
		if (budgetStyle == null) {
			return "unspecified";
		}
		return switch (budgetStyle) {
			case ECO -> "budget-conscious";
			case MID_RANGE -> "balanced";
			case PREMIUM -> "splurge-friendly";
		};
	}
}

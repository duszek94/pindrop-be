package com.duszek.pindrop.provider.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubTripPlanProvider implements TripPlanAiProvider {

	private final TripPlanTemplateEngine templateEngine;

	public StubTripPlanProvider(TripPlanTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	public TripPlanGenerationResult generateProposals(TripPlanGenerationRequest request) {
		return templateEngine.generateProposals(request);
	}

	@Override
	public TripPlanGenerationResult.GeneratedActivity regenerateActivity(
			TripPlanGenerationRequest tripContext,
			TripPlanGenerationResult.GeneratedActivity currentActivity) {
		return templateEngine.regenerateActivity(tripContext, currentActivity);
	}
}

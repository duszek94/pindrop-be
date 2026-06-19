package com.duszek.pindrop.provider.ai;

public interface TripPlanAiProvider {

	TripPlanGenerationResult generateProposals(TripPlanGenerationRequest request);

	TripPlanGenerationResult.GeneratedActivity regenerateActivity(
			TripPlanGenerationRequest tripContext,
			TripPlanGenerationResult.GeneratedActivity currentActivity);
}

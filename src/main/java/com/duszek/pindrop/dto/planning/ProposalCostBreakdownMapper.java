package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.provider.ai.TripPlanGenerationResult;

public final class ProposalCostBreakdownMapper {

	private ProposalCostBreakdownMapper() {
	}

	public static ProposalCostBreakdownResponse toResponse(TripPlanGenerationResult.ProposalCostBreakdown source) {
		if (source == null) {
			return null;
		}
		return ProposalCostBreakdownResponse.builder()
				.estimatedTotal(ProposalCostBreakdownResponse.EstimatedTotalResponse.builder()
						.min(source.estimatedTotal().min())
						.max(source.estimatedTotal().max())
						.currency(source.estimatedTotal().currency())
						.confidence(source.estimatedTotal().confidence())
						.build())
				.breakdown(ProposalCostBreakdownResponse.ProposalBreakdownResponse.builder()
						.accommodation(toRange(source.breakdown().accommodation()))
						.transport(toTransport(source.breakdown().transport()))
						.food(toRange(source.breakdown().food()))
						.attractions(toRange(source.breakdown().attractions()))
						.build())
				.build();
	}

	private static CostRangeResponse toRange(TripPlanGenerationResult.CostRange range) {
		if (range == null) {
			return null;
		}
		return CostRangeResponse.builder().min(range.min()).max(range.max()).build();
	}

	private static TransportCostBreakdownResponse toTransport(TripPlanGenerationResult.TransportCost transport) {
		if (transport == null) {
			return null;
		}
		TransportSubCostResponse sub = null;
		if (transport.sub() != null) {
			sub = TransportSubCostResponse.builder()
					.publicTransit(toRange(transport.sub().publicTransit()))
					.fuel(toRange(transport.sub().fuel()))
					.parking(toRange(transport.sub().parking()))
					.carRental(toRange(transport.sub().carRental()))
					.build();
		}
		return TransportCostBreakdownResponse.builder()
				.min(transport.min())
				.max(transport.max())
				.includesCarCosts(transport.includesCarCosts())
				.sub(sub)
				.build();
	}
}

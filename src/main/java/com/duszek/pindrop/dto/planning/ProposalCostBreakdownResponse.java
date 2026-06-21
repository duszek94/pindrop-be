package com.duszek.pindrop.dto.planning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalCostBreakdownResponse {

	private EstimatedTotalResponse estimatedTotal;
	private ProposalBreakdownResponse breakdown;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EstimatedTotalResponse {
		private int min;
		private int max;
		private String currency;
		private String confidence;
	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProposalBreakdownResponse {
		private CostRangeResponse accommodation;
		private TransportCostBreakdownResponse transport;
		private CostRangeResponse food;
		private CostRangeResponse attractions;
	}
}

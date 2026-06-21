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
public class TransportSubCostResponse {

	private CostRangeResponse publicTransit;
	private CostRangeResponse fuel;
	private CostRangeResponse parking;
	private CostRangeResponse carRental;
}

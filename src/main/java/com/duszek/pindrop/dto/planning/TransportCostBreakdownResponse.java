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
public class TransportCostBreakdownResponse {

	private int min;
	private int max;
	private boolean includesCarCosts;
	private TransportSubCostResponse sub;
}

package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.entity.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripProposalResponse {

	private Long id;
	private ProposalType type;
	private String title;
	private String summary;
	private int estimatedCostUsd;
	private boolean recommended;
	private ProposalCostBreakdownResponse costBreakdown;
	private List<WeatherDayResponse> weatherForecast;
	private List<String> highlights;
}

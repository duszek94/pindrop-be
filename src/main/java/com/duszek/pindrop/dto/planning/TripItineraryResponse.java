package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.entity.ProposalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripItineraryResponse {

	private Long tripId;
	private String title;
	private String destination;
	private ProposalType selectedProposalType;
	private int totalDays;
	private List<ItineraryDaySummary> days;
	private List<ItineraryActivityResponse> activities;
}

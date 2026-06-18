package com.duszek.pindrop.dto.dashboard;

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
public class DashboardResponse {

	private MeResponse me;
	private NotificationSummaryResponse notificationsSummary;
	private List<TripResponse> myTrips;
	private List<ItineraryResponse> favoriteItineraries;
}

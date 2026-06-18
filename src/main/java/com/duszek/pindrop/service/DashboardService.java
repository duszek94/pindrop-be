package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.dashboard.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private static final int DASHBOARD_TRIPS_LIMIT = 5;

	private final MeService meService;
	private final NotificationService notificationService;
	private final TripService tripService;
	private final ItineraryService itineraryService;

	@Transactional(readOnly = true)
	public DashboardResponse getDashboard(Long userId) {
		return DashboardResponse.builder()
				.me(meService.getMe(userId))
				.notificationsSummary(notificationService.getSummary(userId))
				.myTrips(tripService.getRecentTrips(userId, DASHBOARD_TRIPS_LIMIT))
				.favoriteItineraries(itineraryService.getFavorites(userId))
				.build();
	}
}

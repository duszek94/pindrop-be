package com.duszek.pindrop.controller;

import com.duszek.pindrop.dto.planning.SelectProposalRequest;
import com.duszek.pindrop.dto.planning.TripItineraryResponse;
import com.duszek.pindrop.dto.planning.TripProposalResponse;
import com.duszek.pindrop.dto.planning.UpdateDestinationRequest;
import com.duszek.pindrop.dto.planning.UpdateInterestsRequest;
import com.duszek.pindrop.dto.planning.UpdatePreferencesRequest;
import com.duszek.pindrop.security.SecurityUtils;
import com.duszek.pindrop.service.TripPlanningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}")
@RequiredArgsConstructor
public class TripPlanningController {

	private final TripPlanningService tripPlanningService;

	@PatchMapping("/wizard/destination")
	public void updateDestination(
			@PathVariable Long tripId,
			@Valid @RequestBody UpdateDestinationRequest request) {
		tripPlanningService.updateDestination(SecurityUtils.getCurrentUserId(), tripId, request);
	}

	@PatchMapping("/wizard/preferences")
	public void updatePreferences(
			@PathVariable Long tripId,
			@Valid @RequestBody UpdatePreferencesRequest request) {
		tripPlanningService.updatePreferences(SecurityUtils.getCurrentUserId(), tripId, request);
	}

	@PatchMapping("/wizard/interests")
	public void updateInterests(
			@PathVariable Long tripId,
			@Valid @RequestBody UpdateInterestsRequest request) {
		tripPlanningService.updateInterests(SecurityUtils.getCurrentUserId(), tripId, request);
	}

	@PostMapping("/proposals/generate")
	public List<TripProposalResponse> generateProposals(@PathVariable Long tripId) {
		return tripPlanningService.generateProposals(SecurityUtils.getCurrentUserId(), tripId);
	}

	@GetMapping("/proposals")
	public List<TripProposalResponse> getProposals(@PathVariable Long tripId) {
		return tripPlanningService.getProposals(SecurityUtils.getCurrentUserId(), tripId);
	}

	@PostMapping("/proposals/select")
	public void selectProposal(
			@PathVariable Long tripId,
			@Valid @RequestBody SelectProposalRequest request) {
		tripPlanningService.selectProposal(SecurityUtils.getCurrentUserId(), tripId, request);
	}

	@GetMapping("/itinerary")
	public TripItineraryResponse getItinerary(
			@PathVariable Long tripId,
			@RequestParam(required = false) Integer day) {
		return tripPlanningService.getItinerary(SecurityUtils.getCurrentUserId(), tripId, day);
	}

	@PostMapping("/itinerary/activities/{activityId}/regenerate")
	public TripItineraryResponse regenerateActivity(
			@PathVariable Long tripId,
			@PathVariable Long activityId) {
		return tripPlanningService.regenerateActivity(SecurityUtils.getCurrentUserId(), tripId, activityId);
	}

	@PostMapping("/itinerary/regenerate")
	public List<TripProposalResponse> regenerateItinerary(@PathVariable Long tripId) {
		return tripPlanningService.regenerateItinerary(SecurityUtils.getCurrentUserId(), tripId);
	}

	@PostMapping("/save")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void saveTrip(@PathVariable Long tripId) {
		tripPlanningService.saveTrip(SecurityUtils.getCurrentUserId(), tripId);
	}
}

package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.planning.InterestSuggestionResponse;
import com.duszek.pindrop.dto.planning.PreferenceProfile;
import com.duszek.pindrop.dto.planning.SelectProposalRequest;
import com.duszek.pindrop.dto.planning.TripItineraryResponse;
import com.duszek.pindrop.dto.planning.TripProposalResponse;
import com.duszek.pindrop.dto.planning.UpdateDestinationRequest;
import com.duszek.pindrop.dto.planning.UpdateInterestsRequest;
import com.duszek.pindrop.dto.planning.UpdatePreferencesRequest;
import com.duszek.pindrop.entity.ProposalType;
import com.duszek.pindrop.entity.Trip;
import com.duszek.pindrop.entity.TripInterest;
import com.duszek.pindrop.entity.TripStatus;
import com.duszek.pindrop.exception.BadRequestException;
import com.duszek.pindrop.repository.TripItineraryActivityRepository;
import com.duszek.pindrop.repository.TripRepository;
import com.duszek.pindrop.util.PreferenceProfileUtils;
import com.duszek.pindrop.util.TripUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPlanningService {

	private final TripRepository tripRepository;
	private final TripItineraryActivityRepository tripItineraryActivityRepository;
	private final ItineraryGenerationService itineraryGenerationService;
	private final InterestSuggestionService interestSuggestionService;

	@Transactional
	public void updateDestination(Long userId, Long tripId, UpdateDestinationRequest request) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		TripUtils.validateDateRange(request.getStartDate(), request.getEndDate());

		trip.setDestination(request.getDestination());
		trip.setPlaceType(request.getPlaceType());
		trip.setLat(request.getLat());
		trip.setLng(request.getLng());
		trip.setStartDate(request.getStartDate());
		trip.setEndDate(request.getEndDate());
		trip.setWizardStep((short) 2);
		tripRepository.save(trip);
	}

	@Transactional
	public void updatePreferences(Long userId, Long tripId, UpdatePreferencesRequest request) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		PreferenceProfile profile = request.getPreferenceProfile();
		profile.setAdditionalRequirements(
				PreferenceProfileUtils.sanitizeAdditionalRequirements(profile.getAdditionalRequirements()));
		trip.setPreferenceProfile(profile);
		trip.setBudgetTier(profile.getBudgetStyle());
		trip.setPace(profile.getPace());
		trip.setWizardStep((short) 3);
		tripRepository.save(trip);
	}

	@Transactional(readOnly = true)
	public List<InterestSuggestionResponse> getInterestSuggestions(Long userId, Long tripId) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		if (trip.getDestination() == null || trip.getDestination().isBlank()) {
			throw new BadRequestException("Destination is required");
		}
		if (trip.getPreferenceProfile() == null) {
			throw new BadRequestException("Preferences are required");
		}
		return interestSuggestionService.suggest(trip);
	}

	@Transactional
	public void updateInterests(Long userId, Long tripId, UpdateInterestsRequest request) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		List<String> interests = request.getInterests() != null ? request.getInterests() : List.of();
		if (interests.isEmpty() || interests.size() > 3) {
			throw new BadRequestException("Select between 1 and 3 interests");
		}

		List<String> validated = new ArrayList<>(interests.size());
		for (String interest : interests) {
			TripInterest parsed = TripInterest.fromName(interest);
			if (parsed == null) {
				throw new BadRequestException("Unknown interest: " + interest);
			}
			validated.add(parsed.name());
		}

		trip.setInterests(validated);
		tripRepository.save(trip);
	}

	@Transactional
	public List<TripProposalResponse> generateProposals(Long userId, Long tripId) {
		return itineraryGenerationService.generateProposals(userId, tripId);
	}

	@Transactional(readOnly = true)
	public List<TripProposalResponse> getProposals(Long userId, Long tripId) {
		itineraryGenerationService.loadOwnedTrip(userId, tripId);
		return itineraryGenerationService.listProposals(tripId);
	}

	@Transactional
	public void selectProposal(Long userId, Long tripId, SelectProposalRequest request) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		trip.setSelectedProposalType(request.getType());
		trip.setWizardStep((short) 5);
		tripRepository.save(trip);
		itineraryGenerationService.persistSelectedProposalItinerary(trip, request.getType());
	}

	@Transactional(readOnly = true)
	public TripItineraryResponse getItinerary(Long userId, Long tripId, Integer day) {
		itineraryGenerationService.loadOwnedTrip(userId, tripId);
		return itineraryGenerationService.getItinerary(tripId, day);
	}

	@Transactional
	public TripItineraryResponse regenerateActivity(Long userId, Long tripId, Long activityId) {
		itineraryGenerationService.regenerateActivity(userId, tripId, activityId);
		int dayNumber = tripItineraryActivityRepository.findByIdAndDay_Trip_Id(activityId, tripId)
				.map(a -> a.getDay().getDayNumber())
				.orElse(1);
		return itineraryGenerationService.getItinerary(tripId, dayNumber);
	}

	@Transactional
	public List<TripProposalResponse> regenerateItinerary(Long userId, Long tripId) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		List<TripProposalResponse> proposals = itineraryGenerationService.regenerateFullItinerary(userId, tripId);
		if (trip.getSelectedProposalType() != null) {
			itineraryGenerationService.persistSelectedProposalItinerary(trip, trip.getSelectedProposalType());
		}
		return proposals;
	}

	@Transactional
	public void saveTrip(Long userId, Long tripId) {
		Trip trip = itineraryGenerationService.loadOwnedTrip(userId, tripId);
		if (trip.getSelectedProposalType() == null) {
			trip.setSelectedProposalType(ProposalType.BALANCED);
		}
		if (trip.getDestination() != null && !trip.getDestination().isBlank()) {
			String city = trip.getDestination().split(",")[0].trim();
			if (trip.getTitle() == null || trip.getTitle().startsWith("Untitled")) {
				trip.setTitle(city + " Adventure");
			}
		}
		trip.setStatus(TripStatus.UPCOMING);
		trip.setWizardStep((short) 6);
		tripRepository.save(trip);
	}
}

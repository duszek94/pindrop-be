package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.planning.ItineraryActivityResponse;
import com.duszek.pindrop.dto.planning.ItineraryDaySummary;
import com.duszek.pindrop.dto.planning.ProposalCostBreakdownMapper;
import com.duszek.pindrop.dto.planning.ProposalCostBreakdownResponse;
import com.duszek.pindrop.dto.planning.TripItineraryResponse;
import com.duszek.pindrop.dto.planning.TripProposalResponse;
import com.duszek.pindrop.dto.planning.WeatherDayResponse;
import com.duszek.pindrop.entity.ActivityType;
import com.duszek.pindrop.entity.Notification;
import com.duszek.pindrop.entity.NotificationType;
import com.duszek.pindrop.entity.ProposalType;
import com.duszek.pindrop.entity.Trip;
import com.duszek.pindrop.entity.TripItineraryActivity;
import com.duszek.pindrop.entity.TripItineraryDay;
import com.duszek.pindrop.entity.TripProposal;
import com.duszek.pindrop.entity.TripStatus;
import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.exception.BadRequestException;
import com.duszek.pindrop.exception.ForbiddenException;
import com.duszek.pindrop.provider.ai.TripPlanAiProvider;
import com.duszek.pindrop.provider.ai.TripPlanGenerationRequest;
import com.duszek.pindrop.provider.ai.TripPlanGenerationResult;
import com.duszek.pindrop.provider.weather.WeatherForecast;
import com.duszek.pindrop.provider.weather.WeatherProvider;
import com.duszek.pindrop.repository.NotificationRepository;
import com.duszek.pindrop.repository.TripItineraryActivityRepository;
import com.duszek.pindrop.repository.TripItineraryDayRepository;
import com.duszek.pindrop.repository.TripProposalRepository;
import com.duszek.pindrop.repository.TripRepository;
import com.duszek.pindrop.repository.UserRepository;
import com.duszek.pindrop.util.TripUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryGenerationService {

	private final TripRepository tripRepository;
	private final TripProposalRepository tripProposalRepository;
	private final TripItineraryDayRepository tripItineraryDayRepository;
	private final TripItineraryActivityRepository tripItineraryActivityRepository;
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final TripPlanAiProvider tripPlanAiProvider;
	private final WeatherProvider weatherProvider;
	private final JsonMapper jsonMapper;

	@Transactional
	public List<TripProposalResponse> generateProposals(Long userId, Long tripId) {
		Trip trip = loadOwnedTrip(userId, tripId);
		validateReadyForGeneration(trip);

		double lat = trip.getLat() != null ? trip.getLat() : 0;
		double lng = trip.getLng() != null ? trip.getLng() : 0;
		List<WeatherForecast> weather = weatherProvider.getForecast(
				lat, lng, trip.getStartDate(), trip.getEndDate());

		TripPlanGenerationRequest request = new TripPlanGenerationRequest(
				trip.getDestination(),
				lat,
				lng,
				trip.getStartDate(),
				trip.getEndDate(),
				trip.getPreferenceProfile(),
				trip.getInterests() != null ? trip.getInterests() : List.of(),
				weather);

		TripPlanGenerationResult result = tripPlanAiProvider.generateProposals(request);

		tripProposalRepository.deleteByTripId(tripId);
		tripItineraryActivityRepository.deleteByDay_Trip_Id(tripId);
		tripItineraryDayRepository.deleteByTripId(tripId);

		List<TripProposalResponse> responses = new ArrayList<>();
		for (TripPlanGenerationResult.GeneratedProposal generated : result.proposals()) {
			TripProposal entity = new TripProposal();
			entity.setTrip(trip);
			entity.setProposalType(generated.type());
			entity.setTitle(generated.title());
			entity.setSummary(generated.summary());
			entity.setEstimatedCostUsd(generated.estimatedCostUsd());
			entity.setRecommended(generated.recommended());
			entity.setWeatherJson(writeJson(generated.weatherForecast()));
			entity.setHighlightsJson(writeJson(generated.highlights()));
			entity.setCostBreakdownJson(writeJson(generated.costBreakdown()));
			tripProposalRepository.save(entity);

			responses.add(toProposalResponse(
					entity,
					generated.weatherForecast(),
					generated.highlights(),
					generated.costBreakdown()));
		}

		if (trip.getSelectedProposalType() == null) {
			trip.setSelectedProposalType(ProposalType.BALANCED);
		}
		trip.setWizardStep((short) 4);
		tripRepository.save(trip);

		notifyProposalReady(userId, trip);

		return responses;
	}

	@Transactional
	public ItineraryActivityResponse regenerateActivity(Long userId, Long tripId, Long activityId) {
		Trip trip = loadOwnedTrip(userId, tripId);
		TripItineraryActivity activity = tripItineraryActivityRepository
				.findByIdAndDay_Trip_Id(activityId, tripId)
				.orElseThrow(() -> new ForbiddenException("Activity not found"));

		TripPlanGenerationRequest context = buildContextFromTrip(trip);
		TripPlanGenerationResult.GeneratedActivity current = new TripPlanGenerationResult.GeneratedActivity(
				activity.getStartTime(),
				activity.getActivityType(),
				activity.getTitle(),
				activity.getDescription(),
				activity.getPlaceName(),
				activity.getLat(),
				activity.getLng(),
				readTemp(activity.getWeatherJson()));

		TripPlanGenerationResult.GeneratedActivity updated =
				tripPlanAiProvider.regenerateActivity(context, current);

		activity.setTitle(updated.title());
		activity.setDescription(updated.description());
		activity.setWeatherJson(writeJson(updated.tempC()));

		return toActivityResponse(tripItineraryActivityRepository.save(activity));
	}

	@Transactional
	public List<TripProposalResponse> regenerateFullItinerary(Long userId, Long tripId) {
		return generateProposals(userId, tripId);
	}

	void persistSelectedProposalItinerary(Trip trip, ProposalType type) {
		tripItineraryActivityRepository.deleteByDay_Trip_Id(trip.getId());
		tripItineraryDayRepository.deleteByTripId(trip.getId());

		TripProposal proposal = tripProposalRepository.findByTripIdAndProposalType(trip.getId(), type)
				.orElseThrow(() -> new BadRequestException("Selected proposal not found"));

		TripPlanGenerationRequest context = buildContextFromTrip(trip);
		TripPlanGenerationResult result = tripPlanAiProvider.generateProposals(context);
		TripPlanGenerationResult.GeneratedProposal selected = result.proposals().stream()
				.filter(p -> p.type() == type)
				.findFirst()
				.orElseThrow(() -> new BadRequestException("Proposal type not available"));

		persistItineraryDays(trip, selected.days());
		trip.setTitle(proposal.getTitle());
	}

	private void persistItineraryDays(Trip trip, List<TripPlanGenerationResult.GeneratedDay> days) {
		for (TripPlanGenerationResult.GeneratedDay day : days) {
			TripItineraryDay dayEntity = new TripItineraryDay();
			dayEntity.setTrip(trip);
			dayEntity.setDayNumber(day.dayNumber());
			dayEntity.setDayDate(day.date());
			tripItineraryDayRepository.save(dayEntity);

			int order = 0;
			for (TripPlanGenerationResult.GeneratedActivity activity : day.activities()) {
				TripItineraryActivity activityEntity = new TripItineraryActivity();
				activityEntity.setDay(dayEntity);
				activityEntity.setStartTime(activity.startTime());
				activityEntity.setActivityType(activity.activityType());
				activityEntity.setTitle(activity.title());
				activityEntity.setDescription(activity.description());
				activityEntity.setPlaceName(activity.placeName());
				activityEntity.setLat(activity.lat());
				activityEntity.setLng(activity.lng());
				activityEntity.setWeatherJson(writeJson(activity.tempC()));
				activityEntity.setSortOrder(order++);
				tripItineraryActivityRepository.save(activityEntity);
			}
		}
	}

	private TripPlanGenerationRequest buildContextFromTrip(Trip trip) {
		double lat = trip.getLat() != null ? trip.getLat() : 0;
		double lng = trip.getLng() != null ? trip.getLng() : 0;
		List<WeatherForecast> weather = weatherProvider.getForecast(
				lat, lng, trip.getStartDate(), trip.getEndDate());
		return new TripPlanGenerationRequest(
				trip.getDestination(),
				lat,
				lng,
				trip.getStartDate(),
				trip.getEndDate(),
				trip.getPreferenceProfile(),
				trip.getInterests() != null ? trip.getInterests() : List.of(),
				weather);
	}

	private void validateReadyForGeneration(Trip trip) {
		if (trip.getDestination() == null || trip.getDestination().isBlank()) {
			throw new BadRequestException("Destination is required");
		}
		if (trip.getStartDate() == null || trip.getEndDate() == null) {
			throw new BadRequestException("Start and end dates are required");
		}
		TripUtils.validateDateRange(trip.getStartDate(), trip.getEndDate());
		if (trip.getPreferenceProfile() == null
				&& (trip.getBudgetTier() == null || trip.getPace() == null)) {
			throw new BadRequestException("Preferences are required");
		}
	}

	private void notifyProposalReady(Long userId, Trip trip) {
		User user = userRepository.getReferenceById(userId);
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setType(NotificationType.AI_PROPOSAL_READY);
		notification.setMessage("Your AI proposals for " + trip.getDestination() + " are ready!");
		notification.setRead(false);
		notificationRepository.save(notification);
	}

	Trip loadOwnedTrip(Long userId, Long tripId) {
		return tripRepository.findByIdAndUserId(tripId, userId)
				.orElseThrow(() -> new ForbiddenException("Trip not found or access denied"));
	}

	TripProposalResponse toProposalResponse(
			TripProposal entity,
			List<TripPlanGenerationResult.WeatherDayForecast> weather,
			List<String> highlights,
			TripPlanGenerationResult.ProposalCostBreakdown costBreakdown) {
		List<WeatherDayResponse> weatherResponse = weather.stream()
				.map(w -> new WeatherDayResponse(w.dayLabel(), w.icon(), w.tempC()))
				.toList();
		ProposalCostBreakdownResponse costBreakdownResponse =
				ProposalCostBreakdownMapper.toResponse(costBreakdown);
		return TripProposalResponse.builder()
				.id(entity.getId())
				.type(entity.getProposalType())
				.title(entity.getTitle())
				.summary(entity.getSummary())
				.estimatedCostUsd(entity.getEstimatedCostUsd())
				.recommended(entity.isRecommended())
				.costBreakdown(costBreakdownResponse)
				.weatherForecast(weatherResponse)
				.highlights(highlights)
				.build();
	}

	ItineraryActivityResponse toActivityResponse(TripItineraryActivity activity) {
		return ItineraryActivityResponse.builder()
				.id(activity.getId())
				.startTime(activity.getStartTime())
				.type(activity.getActivityType())
				.title(activity.getTitle())
				.description(activity.getDescription())
				.placeName(activity.getPlaceName())
				.lat(activity.getLat())
				.lng(activity.getLng())
				.tempC(readTemp(activity.getWeatherJson()))
				.build();
	}

	List<TripProposalResponse> listProposals(Long tripId) {
		return tripProposalRepository.findByTripIdOrderByProposalTypeAsc(tripId).stream()
				.map(entity -> {
					List<TripPlanGenerationResult.WeatherDayForecast> weather =
							readJson(entity.getWeatherJson(), new TypeReference<>() {});
					List<String> highlights = readJson(entity.getHighlightsJson(), new TypeReference<>() {});
					TripPlanGenerationResult.ProposalCostBreakdown costBreakdown =
							readJson(entity.getCostBreakdownJson(), new TypeReference<>() {});
					return toProposalResponse(
							entity,
							weather != null ? weather : List.of(),
							highlights != null ? highlights : List.of(),
							costBreakdown);
				})
				.toList();
	}

	TripItineraryResponse getItinerary(Long tripId, Integer dayNumber) {
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(() -> new ForbiddenException("Trip not found"));

		List<TripItineraryDay> days = tripItineraryDayRepository.findByTripIdOrderByDayNumberAsc(tripId);
		List<ItineraryDaySummary> daySummaries = days.stream()
				.map(d -> new ItineraryDaySummary(d.getDayNumber(), d.getDayDate()))
				.toList();

		int selectedDay = dayNumber != null ? dayNumber : (days.isEmpty() ? 1 : days.get(0).getDayNumber());
		List<ItineraryActivityResponse> activities = days.stream()
				.filter(d -> d.getDayNumber() == selectedDay)
				.findFirst()
				.map(day -> tripItineraryActivityRepository.findByDayIdOrderBySortOrderAsc(day.getId()).stream()
						.map(this::toActivityResponse)
						.toList())
				.orElse(List.of());

		return TripItineraryResponse.builder()
				.tripId(trip.getId())
				.title(trip.getTitle())
				.destination(trip.getDestination())
				.selectedProposalType(trip.getSelectedProposalType())
				.totalDays(days.size())
				.days(daySummaries)
				.activities(activities)
				.build();
	}

	private String writeJson(Object value) {
		try {
			return jsonMapper.writeValueAsString(value);
		} catch (JacksonException ex) {
			throw new BadRequestException("Failed to serialize data");
		}
	}

	private Integer readTemp(String json) {
		if (json == null || json.isBlank()) {
			return null;
		}
		try {
			return jsonMapper.readValue(json, Integer.class);
		} catch (JacksonException ex) {
			return null;
		}
	}

	private <T> T readJson(String json, TypeReference<T> type) {
		if (json == null || json.isBlank()) {
			return null;
		}
		try {
			return jsonMapper.readValue(json, type);
		} catch (JacksonException ex) {
			log.warn("Failed to parse JSON: {}", ex.getMessage());
			return null;
		}
	}
}

package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.common.CursorPage;
import com.duszek.pindrop.dto.dashboard.CreateDraftTripResponse;
import com.duszek.pindrop.dto.dashboard.GeoMarkerResponse;
import com.duszek.pindrop.dto.dashboard.TripResponse;
import com.duszek.pindrop.entity.Trip;
import com.duszek.pindrop.entity.TripStatus;
import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.exception.BadRequestException;
import com.duszek.pindrop.exception.ForbiddenException;
import com.duszek.pindrop.repository.TripRepository;
import com.duszek.pindrop.repository.UserRepository;
import com.duszek.pindrop.util.CursorPaginationUtils;
import com.duszek.pindrop.util.TripUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

	private final TripRepository tripRepository;
	private final UserRepository userRepository;

	@Transactional
	public CreateDraftTripResponse createDraftTrip(Long userId) {
		User user = userRepository.getReferenceById(userId);
		Trip trip = new Trip();
		trip.setUser(user);
		trip.setTitle("Untitled Trip");
		trip.setStatus(TripStatus.PLANNING);
		trip.setTravelerCount(1);
		Trip saved = tripRepository.save(trip);
		return new CreateDraftTripResponse(saved.getId());
	}

	@Transactional(readOnly = true)
	public CursorPage<TripResponse> listTrips(Long userId, String scope, Long cursor, Integer limit) {
		if (!"mine".equalsIgnoreCase(scope)) {
			throw new BadRequestException("Unsupported scope: " + scope);
		}
		int pageSize = CursorPaginationUtils.resolveLimit(limit);
		List<Trip> fetched = tripRepository.findByUserIdWithCursor(
				userId, cursor, PageRequest.of(0, pageSize + 1));
		return CursorPaginationUtils.toCursorPage(fetched, pageSize, this::toResponse, Trip::getId);
	}

	@Transactional(readOnly = true)
	public TripResponse getTrip(Long userId, Long tripId) {
		Trip trip = tripRepository.findByIdAndUserId(tripId, userId)
				.orElseThrow(() -> new ForbiddenException("Trip not found or access denied"));
		return toResponse(trip);
	}

	@Transactional(readOnly = true)
	public List<GeoMarkerResponse> getGeoMarkers(Long userId) {
		return tripRepository.findGeoMarkersByUserId(userId).stream()
				.map(trip -> new GeoMarkerResponse(
						trip.getId(),
						trip.getTitle(),
						trip.getDestination(),
						trip.getLat(),
						trip.getLng()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TripResponse> getRecentTrips(Long userId, int limit) {
		return tripRepository.findByUserIdWithCursor(userId, null, PageRequest.of(0, limit))
				.stream()
				.map(this::toResponse)
				.toList();
	}

	private TripResponse toResponse(Trip trip) {
		TripUtils.validateDateRange(trip.getStartDate(), trip.getEndDate());
		return TripResponse.builder()
				.id(trip.getId())
				.title(trip.getTitle())
				.destination(trip.getDestination())
				.lat(trip.getLat())
				.lng(trip.getLng())
				.startDate(trip.getStartDate())
				.endDate(trip.getEndDate())
				.status(TripUtils.deriveStatus(trip))
				.travelerCount(trip.getTravelerCount())
				.coverImageUrl(trip.getCoverImageUrl())
				.durationDays(TripUtils.durationDays(trip))
				.createdAt(trip.getCreatedAt())
				.updatedAt(trip.getUpdatedAt())
				.build();
	}
}

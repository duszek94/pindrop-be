package com.duszek.pindrop.util;

import com.duszek.pindrop.entity.Trip;
import com.duszek.pindrop.entity.TripStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class TripUtils {

	private TripUtils() {
	}

	public static TripStatus deriveStatus(Trip trip) {
		if (trip.getStatus() == TripStatus.CANCELLED) {
			return TripStatus.CANCELLED;
		}
		if (trip.getStartDate() == null || trip.getEndDate() == null) {
			return TripStatus.PLANNING;
		}

		LocalDate today = LocalDate.now();
		if (today.isBefore(trip.getStartDate())) {
			return TripStatus.UPCOMING;
		}
		if (!today.isAfter(trip.getEndDate())) {
			return TripStatus.ACTIVE;
		}
		return TripStatus.COMPLETED;
	}

	public static Integer durationDays(Trip trip) {
		if (trip.getStartDate() == null || trip.getEndDate() == null) {
			return null;
		}
		if (trip.getEndDate().isBefore(trip.getStartDate())) {
			return null;
		}
		return (int) ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1;
	}

	public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
			throw new com.duszek.pindrop.exception.BadRequestException("End date must be on or after start date");
		}
	}
}

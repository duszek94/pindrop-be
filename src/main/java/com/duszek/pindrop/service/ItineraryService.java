package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.common.CursorPage;
import com.duszek.pindrop.dto.dashboard.ItineraryResponse;
import com.duszek.pindrop.entity.Itinerary;
import com.duszek.pindrop.entity.ItineraryLike;
import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.exception.BadRequestException;
import com.duszek.pindrop.exception.ResourceNotFoundException;
import com.duszek.pindrop.repository.ItineraryLikeRepository;
import com.duszek.pindrop.repository.ItineraryRepository;
import com.duszek.pindrop.repository.UserRepository;
import com.duszek.pindrop.util.CursorPaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryService {

	private final ItineraryRepository itineraryRepository;
	private final ItineraryLikeRepository itineraryLikeRepository;
	private final UserRepository userRepository;

	@Cacheable(cacheNames = "favorites", key = "#userId")
	@Transactional(readOnly = true)
	public List<ItineraryResponse> getFavorites(Long userId) {
		return itineraryRepository.findFavoritesByUserId(userId).stream()
				.map(itinerary -> toResponse(itinerary, true))
				.toList();
	}

	@Cacheable(cacheNames = "explore", key = "#userId + '-' + #cursor + '-' + #limit")
	@Transactional(readOnly = true)
	public CursorPage<ItineraryResponse> explore(Long userId, Long cursor, Integer limit) {
		int pageSize = CursorPaginationUtils.resolveLimit(limit);
		List<Itinerary> fetched = itineraryRepository.findPublicWithCursor(
				cursor, PageRequest.of(0, pageSize + 1));
		return CursorPaginationUtils.toCursorPage(
				fetched,
				pageSize,
				itinerary -> toResponse(itinerary, itineraryLikeRepository.existsByItineraryIdAndUserId(itinerary.getId(), userId)),
				Itinerary::getId);
	}

	@Transactional
	@CacheEvict(cacheNames = {"favorites", "explore"}, allEntries = true)
	public ItineraryResponse likeItinerary(Long userId, Long itineraryId) {
		Itinerary itinerary = itineraryRepository.findByIdAndIsPublicTrue(itineraryId)
				.orElseThrow(() -> new ResourceNotFoundException("Itinerary not found"));
		if (itineraryLikeRepository.existsByItineraryIdAndUserId(itineraryId, userId)) {
			return toResponse(itinerary, true);
		}

		User user = userRepository.getReferenceById(userId);
		ItineraryLike like = new ItineraryLike();
		like.setItinerary(itinerary);
		like.setUser(user);
		itineraryLikeRepository.save(like);
		itineraryRepository.incrementLikeCount(itineraryId);

		itinerary.setLikeCount(itinerary.getLikeCount() + 1);
		return toResponse(itinerary, true);
	}

	@Transactional
	@CacheEvict(cacheNames = {"favorites", "explore"}, allEntries = true)
	public ItineraryResponse unlikeItinerary(Long userId, Long itineraryId) {
		Itinerary itinerary = itineraryRepository.findById(itineraryId)
				.orElseThrow(() -> new ResourceNotFoundException("Itinerary not found"));

		ItineraryLike like = itineraryLikeRepository.findByItineraryIdAndUserId(itineraryId, userId)
				.orElseThrow(() -> new BadRequestException("Itinerary is not liked"));

		itineraryLikeRepository.delete(like);
		itineraryRepository.decrementLikeCount(itineraryId);

		boolean stillLiked = false;
		itinerary.setLikeCount(Math.max(0, itinerary.getLikeCount() - 1));
		return toResponse(itinerary, stillLiked);
	}

	private ItineraryResponse toResponse(Itinerary itinerary, boolean likedByCurrentUser) {
		return ItineraryResponse.builder()
				.id(itinerary.getId())
				.tripId(itinerary.getTrip() != null ? itinerary.getTrip().getId() : null)
				.title(itinerary.getTitle())
				.isPublic(itinerary.isPublic())
				.likeCount(itinerary.getLikeCount())
				.coverImageUrl(itinerary.getCoverImageUrl())
				.likedByCurrentUser(likedByCurrentUser)
				.createdAt(itinerary.getCreatedAt())
				.build();
	}
}

package com.duszek.pindrop.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryResponse {

	private Long id;
	private Long tripId;
	private String title;
	private boolean isPublic;
	private int likeCount;
	private String coverImageUrl;
	private boolean likedByCurrentUser;
	private LocalDateTime createdAt;
}

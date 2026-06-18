package com.duszek.pindrop.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {

	private Long id;
	private String fullName;
	private String initials;
	private String email;
	private String avatarUrl;
}

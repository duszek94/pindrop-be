package com.duszek.pindrop.service;

import com.duszek.pindrop.dto.dashboard.MeResponse;
import com.duszek.pindrop.entity.User;
import com.duszek.pindrop.exception.ResourceNotFoundException;
import com.duszek.pindrop.repository.UserRepository;
import com.duszek.pindrop.util.UserDisplayUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public MeResponse getMe(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return toMeResponse(user);
	}

	public MeResponse toMeResponse(User user) {
		return MeResponse.builder()
				.id(user.getId())
				.fullName(UserDisplayUtils.fullName(user))
				.initials(UserDisplayUtils.initials(user))
				.email(user.getEmail())
				.avatarUrl(user.getAvatarUrl())
				.build();
	}
}

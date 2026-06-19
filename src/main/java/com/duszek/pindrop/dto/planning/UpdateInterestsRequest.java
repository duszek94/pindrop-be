package com.duszek.pindrop.dto.planning;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateInterestsRequest {

	private List<String> interests;
}

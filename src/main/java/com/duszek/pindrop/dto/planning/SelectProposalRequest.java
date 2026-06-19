package com.duszek.pindrop.dto.planning;

import com.duszek.pindrop.entity.ProposalType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectProposalRequest {

	@NotNull
	private ProposalType type;
}

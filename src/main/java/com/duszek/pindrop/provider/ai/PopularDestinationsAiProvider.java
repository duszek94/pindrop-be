package com.duszek.pindrop.provider.ai;

import java.util.List;

public interface PopularDestinationsAiProvider {

	List<PopularDestinationSuggestion> suggest(PopularDestinationsRequest request);
}

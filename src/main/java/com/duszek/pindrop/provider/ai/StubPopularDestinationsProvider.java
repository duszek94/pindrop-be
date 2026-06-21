package com.duszek.pindrop.provider.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubPopularDestinationsProvider implements PopularDestinationsAiProvider {

	private final PopularDestinationsTemplateEngine templateEngine;

	public StubPopularDestinationsProvider(PopularDestinationsTemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	@Override
	public List<PopularDestinationSuggestion> suggest(PopularDestinationsRequest request) {
		return templateEngine.suggest(request);
	}
}

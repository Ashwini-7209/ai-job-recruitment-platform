package com.jobplatform.ai;

import java.util.Optional;

public interface ChatAIProvider {

    Optional<String> chat(String systemPrompt, String userPrompt, int maxTokens);

    String getProviderName();

    boolean isAvailable();
}

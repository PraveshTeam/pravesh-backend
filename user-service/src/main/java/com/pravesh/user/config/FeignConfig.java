package com.pravesh.user.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

	@Value("${pravesh.internal.api-key}")
	private String internalApiKey;

	@Bean
	public RequestInterceptor internalApiKeyInterceptor() {
		return requestTemplate -> requestTemplate.header("X-Internal-Api-Key", internalApiKey);
	}
}
package com.personal.sirestproxy;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SirestproxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SirestproxyApplication.class, args);
	}

	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

	@Value("${app.cors.allowed-methods}")
	private String allowedMethods;

	@Value("${app.cors.allowed-headers}")
	private String allowedHeaders;

	@Value("${app.cors.allow-credentials}")
	private boolean allowCredentials;

	@Bean
	public WebMvcConfigurer corsConfigurer() {

		List<String> originsList = Arrays.asList(allowedOrigins.split(","));
		List<String> methodsList = Arrays.asList(allowedMethods.split(","));
		List<String> headersList = Arrays.asList(allowedHeaders.split(","));

		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(originsList.toArray(new String[0]))
						.allowedMethods(methodsList.toArray(new String[0]))
						.allowedHeaders(headersList.toArray(new String[0]))
						.allowCredentials(allowCredentials);
			}
		};
	}
}

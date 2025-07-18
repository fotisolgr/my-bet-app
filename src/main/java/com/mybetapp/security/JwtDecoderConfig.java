package com.mybetapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

	@Bean
	public JwtDecoder jwtDecoder() {
		// Use the OpenID configuration URL with host.docker.internal
		String jwkSetUri = "http://host.docker.internal:8188/realms/my-quarkus-app/protocol/openid-connect/certs";

		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

		// Create a validator for the issuer claim that matches 'localhost'
		OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(
				"http://localhost:8188/realms/my-quarkus-app");

		// Timestamp validator for exp, nbf etc.
		OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator();

		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(issuerValidator, timestampValidator);

		jwtDecoder.setJwtValidator(validator);

		return jwtDecoder;
	}
}

package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "oidc.token")
@Profile("oidctokenauth")
public class OidcTokenProperties
{
	@Getter
	@Setter
	private String endpoint;
}

package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "oidc.client")
@Profile("oidctokenauth")
public class OidcClientProperties
{

	@Getter
	@Setter
	private String id;

	@Getter
	@Setter
	private String secret;

}

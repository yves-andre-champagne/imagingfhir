package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "httpbasic.auth", ignoreUnknownFields = false, ignoreInvalidFields = false)
@Profile("httpbasicauth")
public class HttpBasicClientProperties
{

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	private String password;

}

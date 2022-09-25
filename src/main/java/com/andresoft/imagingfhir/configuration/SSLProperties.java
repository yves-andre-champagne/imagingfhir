package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "ssl")
public class SSLProperties
{

	@Getter
	@Setter
	private boolean trustAllCerts;
}

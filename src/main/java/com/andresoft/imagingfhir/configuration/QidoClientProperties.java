package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "qidors")
public class QidoClientProperties
{

	@Getter
	@Setter
	private String baseUrl;

}

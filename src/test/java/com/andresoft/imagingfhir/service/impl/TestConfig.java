package com.andresoft.imagingfhir.service.impl;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.andresoft.dicomweb.authorization.HttpAuthorization;
import com.andresoft.dicomweb.authorization.basic.HttpBasicAuthorization;
import com.andresoft.imagingfhir.configuration.HttpBasicClientProperties;
import com.andresoft.imagingfhir.configuration.QidoClientProperties;
import com.andresoft.imagingfhir.configuration.SSLProperties;
import com.andresoft.imagingfhir.service.ImagingStudyService;

import ca.uhn.fhir.context.FhirContext;

@Configuration
@EnableConfigurationProperties
@Import({ HttpBasicClientProperties.class, SSLProperties.class, QidoClientProperties.class })
public class TestConfig
{

	@Bean
	@Profile("httpbasicauth")
	HttpAuthorization basicHttpAuthorization()
	{
		return new HttpBasicAuthorization();
	}

	@Bean(name = "fhirContext")
	public FhirContext fhirContext()
	{
		return FhirContext.forR4();
	}

	@Bean
	ImagingStudyService imagingStudyService()
	{
		return new ImagingStudyServiceImpl();
	}

}

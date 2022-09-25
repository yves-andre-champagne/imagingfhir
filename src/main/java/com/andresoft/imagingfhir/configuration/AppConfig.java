package com.andresoft.imagingfhir.configuration;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.andresoft.dicomweb.authorization.HttpAuthorization;
import com.andresoft.dicomweb.authorization.basic.HttpBasicAuthorization;
import com.andresoft.dicomweb.authorization.oidc.ClientCredentialsTokenRetriever;
import com.andresoft.dicomweb.authorization.oidc.OidcTokenAuthorization;
import com.andresoft.imagingfhir.provider.ImagingStudyResourceProvider;
import com.andresoft.imagingfhir.server.ImagingStudyServer;
import com.andresoft.imagingfhir.service.ImagingStudyService;
import com.andresoft.imagingfhir.service.impl.ImagingStudyServiceImpl;

import ca.uhn.fhir.context.FhirContext;

@Configuration
@ComponentScan(basePackages = "com.andresoft")
public class AppConfig
{

	@Bean(name = "fhirContext")
	public FhirContext fhirContext()
	{
		return FhirContext.forR4();
	}

	@Bean
	ImagingStudyResourceProvider imagingStudyResourceProvider()
	{
		return new ImagingStudyResourceProvider();
	}

	@Bean
	ImagingStudyService imagingStudyService()
	{
		return new ImagingStudyServiceImpl();
	}

	@Bean
	ImagingStudyServer imagingStudyServer()
	{
		return new ImagingStudyServer();
	}

	@Bean
	@Profile("oidctokenauth")
	HttpAuthorization oidcHttpAuthorization()
	{
		return new OidcTokenAuthorization();
	}

	@Bean
	@Profile("httpbasicauth")
	HttpAuthorization basicHttpAuthorization()
	{
		return new HttpBasicAuthorization();
	}

	@Bean
	@Profile("oidctokenauth")
	ClientCredentialsTokenRetriever clientCredentialsTokenRetriever()
	{
		return new ClientCredentialsTokenRetriever();
	}

	@SuppressWarnings("rawtypes")
	@Bean
	public ServletRegistrationBean ImagingStudyServerRegistrationBean()
	{
		@SuppressWarnings("unchecked")
		ServletRegistrationBean registration = new ServletRegistrationBean(imagingStudyServer());
		registration.setLoadOnStartup(1);
		registration.setName("fhir imaging study server");
		registration.addUrlMappings("/fhir/r4/*");
		return registration;
	}

}

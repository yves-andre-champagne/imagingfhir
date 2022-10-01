package com.andresoft.dicomweb.authorization.oidc;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.andresoft.imagingfhir.configuration.OidcClientProperties;
import com.andresoft.imagingfhir.configuration.OidcTokenProperties;
import com.andresoft.imagingfhir.configuration.SSLProperties;

@Configuration
@EnableConfigurationProperties
@Import({ OidcClientProperties.class, OidcTokenProperties.class, SSLProperties.class })
public class TestConfig
{

	@Bean
	@Profile("oidctokenauth")
	ClientCredentialsTokenProvider clientCredentialsTokenRetriever()
	{
		return new ClientCredentialsTokenProvider();
	}

}

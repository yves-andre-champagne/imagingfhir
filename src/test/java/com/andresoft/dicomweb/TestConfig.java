package com.andresoft.dicomweb;

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

@Configuration
@EnableConfigurationProperties
@Import({ HttpBasicClientProperties.class, SSLProperties.class, QidoClientProperties.class })
public class TestConfig
{
	@Bean
	QidoRsClient qidoRsClient()
	{
		return new QidoRsClient();
	}

	@Bean
	@Profile("httpbasicauth")
	HttpAuthorization basicHttpAuthorization()
	{
		return new HttpBasicAuthorization();
	}
}

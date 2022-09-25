package com.andresoft.imagingfhir.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.andresoft.imagingfhir.provider.ImagingStudyResourceProvider;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;

public class ImagingStudyServer extends RestfulServer
{

	private static final long serialVersionUID = 7567151368161054413L;

	@Autowired
	private ApplicationContext appCtx;

	@Override
	public void initialize()
	{
		FhirContext fhirContext = (FhirContext) appCtx.getBean("fhirContext");
		setFhirContext(fhirContext);

		IResourceProvider imagingStudyResourceProvider = appCtx.getBean("imagingStudyResourceProvider",
				ImagingStudyResourceProvider.class);

		this.setResourceProviders(imagingStudyResourceProvider);
	}

}

package com.andresoft.imagingfhir.provider;

import java.util.List;

import org.hl7.fhir.r4.model.ImagingStudy;
import org.springframework.beans.factory.annotation.Autowired;

import com.andresoft.imagingfhir.exception.ForbiddenException;
import com.andresoft.imagingfhir.exception.NotFoundException;
import com.andresoft.imagingfhir.exception.UnprocessableRequestException;
import com.andresoft.imagingfhir.service.ImagingStudyService;

import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImagingStudyResourceProvider implements IResourceProvider
{

	@Autowired
	private ImagingStudyService imagingStudyService;

	@Override
	public Class<ImagingStudy> getResourceType()
	{
		return ImagingStudy.class;
	}

	@Search()
	public List<ImagingStudy> findByPatientId(@RequiredParam(name = "PatientID") StringParam patientId)
	{
		try
		{
			return imagingStudyService.findByPatientId(patientId.getValue());
		}
		catch (NotFoundException e)
		{
			throw new ResourceNotFoundException("Unknown patient id:" + patientId.getValue());
		}
		catch (ForbiddenException e)
		{
			throw new ForbiddenOperationException("Forbidden");
		}
		catch (UnprocessableRequestException e)
		{
			throw new UnprocessableEntityException("unable to process request");
		}

	}

}

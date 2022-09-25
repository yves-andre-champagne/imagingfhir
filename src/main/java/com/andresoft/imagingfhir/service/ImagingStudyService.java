package com.andresoft.imagingfhir.service;

import java.util.List;

import org.hl7.fhir.r4.model.ImagingStudy;

import com.andresoft.imagingfhir.exception.ForbiddenException;
import com.andresoft.imagingfhir.exception.NotFoundException;
import com.andresoft.imagingfhir.exception.UnprocessableRequestException;

public interface ImagingStudyService
{
	List<ImagingStudy> findByPatientId(String patientId)
			throws NotFoundException, ForbiddenException, UnprocessableRequestException;

}

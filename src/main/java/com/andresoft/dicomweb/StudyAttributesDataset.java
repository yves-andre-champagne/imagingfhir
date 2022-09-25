package com.andresoft.dicomweb;

import java.util.List;

import org.dcm4che3.data.Attributes;

public class StudyAttributesDataset
{

	private Attributes studyAttributes;
	private List<SeriesAttributesDataset> seriesAttributesDataset;

	public Attributes getStudyAttributes()
	{
		return studyAttributes;
	}

	public void setStudyAttributes(Attributes studyAttributes)
	{
		this.studyAttributes = studyAttributes;
	}

	public List<SeriesAttributesDataset> getSeriesAttributesDataset()
	{
		return seriesAttributesDataset;
	}

	public void setSeriesAttributesDataset(List<SeriesAttributesDataset> seriesAttributesDataset)
	{
		this.seriesAttributesDataset = seriesAttributesDataset;
	}

}

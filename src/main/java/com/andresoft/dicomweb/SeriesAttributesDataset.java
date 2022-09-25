package com.andresoft.dicomweb;

import java.util.List;

import org.dcm4che3.data.Attributes;

public class SeriesAttributesDataset
{
	private Attributes series;
	private List<Attributes> instances;

	public List<Attributes> getInstances()
	{
		return instances;
	}

	public void setInstances(List<Attributes> instances)
	{
		this.instances = instances;
	}

	public Attributes getSeries()
	{
		return series;
	}

	public void setSeries(Attributes series)
	{
		this.series = series;
	}

}

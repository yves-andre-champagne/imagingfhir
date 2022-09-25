package com.andresoft.dicomweb;

import java.util.List;

import org.dcm4che3.data.Attributes;

public class RequestParameters
{
	private boolean fuzzy = false;
	private boolean timezone = false;
	private boolean returnAll = false;
	private int limit;
	private int offset;
	private Attributes queryAttrs;
	private List<Attributes> returnAttrs;
	private String studyInstanceUId;
	private String seriesInstanceUid;

	private QueryType queryType = QueryType.STUDY;

	public boolean isFuzzy()
	{
		return fuzzy;
	}

	public void setFuzzy(boolean fuzzy)
	{
		this.fuzzy = fuzzy;
	}

	public boolean isTimezone()
	{
		return timezone;
	}

	public void setTimezone(boolean timezone)
	{
		this.timezone = timezone;
	}

	public boolean isReturnAll()
	{
		return returnAll;
	}

	public void setReturnAll(boolean returnAll)
	{
		this.returnAll = returnAll;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public Attributes getQueryAttrs()
	{
		return queryAttrs;
	}

	public void setQueryAttrs(Attributes queryAttrs)
	{
		this.queryAttrs = queryAttrs;
	}

	public List<Attributes> getReturnAttrs()
	{
		return returnAttrs;
	}

	public void setReturnAttrs(List<Attributes> returnAttrs)
	{
		this.returnAttrs = returnAttrs;
	}

	public QueryType getQueryType()
	{
		return queryType;
	}

	public void setQueryType(QueryType queryType)
	{
		this.queryType = queryType;
	}

	public String getStudyInstanceUId()
	{
		return studyInstanceUId;
	}

	public void setStudyInstanceUId(String studyInstanceUId)
	{
		this.studyInstanceUId = studyInstanceUId;
	}

	public String getSeriesInstanceUid()
	{
		return seriesInstanceUid;
	}

	public void setSeriesInstanceUid(String seriesInstanceUid)
	{
		this.seriesInstanceUid = seriesInstanceUid;
	}

}

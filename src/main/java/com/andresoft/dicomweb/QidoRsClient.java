package com.andresoft.dicomweb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.util.TagUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.andresoft.dicomweb.authorization.AuthorizationCredentials;
import com.andresoft.imagingfhir.configuration.QidoClientProperties;
import com.andresoft.imagingfhir.configuration.SSLProperties;
import com.andresoft.util.SSLUtil;

public class QidoRsClient
{

	private static int MAX_LIMIT = 100;

	@Autowired
	private QidoClientProperties qidoClientProperties;

	@Autowired
	private SSLProperties sslProperties;

	private HttpClient httpClient;

	@PostConstruct
	public void init()
	{
		httpClient = buildHttpClient();

	}

	public Response sendRequest(RequestParameters requestParameters, AuthorizationCredentials authorizationCredentials)
			throws URISyntaxException, IOException, InterruptedException
	{
		String queryUrl = qidoClientProperties.getBaseUrl();

		if (requestParameters.getQueryType() == QueryType.SERIES)
		{
			queryUrl += "/" + requestParameters.getStudyInstanceUId() + "/series";
		}
		else if (requestParameters.getQueryType() == QueryType.INSTANCE)
		{
			queryUrl += "/" + requestParameters.getStudyInstanceUId() + "/series/"
					+ requestParameters.getSeriesInstanceUid() + "/instances";
		}
		int limit = requestParameters.getLimit();
		if (limit > 0)
		{
			if (limit > MAX_LIMIT)
			{
				limit = MAX_LIMIT;
			}
			queryUrl = this.addParam(queryUrl, "limit", String.valueOf(limit));
		}
		if (requestParameters.getOffset() > 0)
		{
			queryUrl = addParam(queryUrl, "offset", String.valueOf(requestParameters.getOffset()));
		}

		if (requestParameters.isFuzzy())
		{
			queryUrl = this.addParam(queryUrl, "fuzzymatching", "true");
		}

		if (requestParameters.isTimezone())
		{
			queryUrl = addParam(queryUrl, "timezoneadjustment", "true");
		}

		ElementDictionary dict = ElementDictionary.getStandardElementDictionary();

		if (!requestParameters.isReturnAll())
		{
			if (requestParameters.getReturnAttrs() != null)
			{
				for (Attributes attributes : requestParameters.getReturnAttrs())
				{
					for (int tag : attributes.tags())
					{
						if (!TagUtils.isPrivateCreator(tag))
						{
							queryUrl = addParam(queryUrl, "includefield",
									ElementDictionary.keywordOf(tag, attributes.getPrivateCreator(tag)));
						}
					}
				}
			}
		}
		else
		{
			queryUrl = addParam(queryUrl, "includefield", "all");
		}

		if (requestParameters.getQueryAttrs() != null)
		{
			Attributes attributes = requestParameters.getQueryAttrs();
			for (int i = 0; i < attributes.tags().length; i++)
			{
				int tag = attributes.tags()[i];
				String keyword;
				keyword = keyWordOf(dict, tag, attributes);
				if (attributes.getSequence(tag) != null)
				{
					// is a sequence
					setSequenceQueryAttrs(attributes, queryUrl, attributes.getSequence(tag), keyword);
				}
				else
				{
					queryUrl = addParam(queryUrl, keyword, (String) attributes.getValue(tag));
				}

			}

		}

		//@formatter:off
		HttpRequest httpRequest = null;
		if (authorizationCredentials.isRequired()) 
		{
		httpRequest = HttpRequest.newBuilder()
						.uri(new URI(queryUrl))
						.header("charset", "utf-8")
						.header("Accept", "application/json")
						.header("Authorization",  authorizationCredentials.getType() +  " " + authorizationCredentials.getCredentials())
						.GET()
						.build();
		}
		else
		{
			httpRequest = HttpRequest.newBuilder()
					.uri(new URI(queryUrl))
					.header("charset", "utf-8")
					.header("Accept", "application/json")					
					.GET()
					.build();
		}
		
		//@formatter:on

		HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());

		return new Response(httpResponse.statusCode(), httpResponse.body());

	}

	HttpClient buildHttpClient()
	{

		HttpClient httpClient;
		if (sslProperties.isTrustAllCerts())
		{
			Optional<SSLContext> sslContext = SSLUtil.getTrustAllCertsSSLContext();

			if (sslContext.isPresent())
			{
				httpClient = HttpClient.newBuilder().sslContext(sslContext.get()).build();
			}
			else
			{
				httpClient = HttpClient.newBuilder().build();
			}
		}
		else
		{
			httpClient = HttpClient.newBuilder().build();
		}

		return httpClient;
	}

	private void setSequenceQueryAttrs(Attributes queryAttrs, String queryUrl, Sequence sequence, String seqKeyWork)
	{
		ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
		for (Attributes item : sequence)
		{
			for (int i = 0; i < item.tags().length; i++)
			{
				int tag = item.tags()[i];
				if (item.getSequence(tag) == null)
				{
					queryUrl += (queryUrl.endsWith(".") ? "" : (queryUrl.contains("?") ? "&" : "?"))
							+ keyWordOf(dict, tag, queryAttrs) + "=" + (String) queryAttrs.getValue(tag);
				}
				else
				{
					queryUrl += (queryUrl) + keyWordOf(dict, tag, queryAttrs) + ".";
					setSequenceQueryAttrs(queryAttrs, queryUrl, queryAttrs.getSequence(tag),
							keyWordOf(dict, tag, queryAttrs));
				}
			}

		}
	}

	protected String keyWordOf(ElementDictionary dict, int tag, Attributes attrs)
	{
		String keyword;
		if (attrs.getPrivateCreator(tag) != null)
		{
			keyword = ElementDictionary.keywordOf(tag, attrs.getPrivateCreator(tag));
		}
		else
		{
			keyword = dict.keywordOf(tag);
		}
		return keyword;
	}

	private String addParam(String url, String key, String field) throws UnsupportedEncodingException
	{
		if (url.contains("?"))
			return url += "&" + key + "=" + URLEncoder.encode(field, "UTF-8");
		else
			return url += "?" + key + "=" + URLEncoder.encode(field, "UTF-8");
	}

	private final HashMap<String, String> query = new HashMap<String, String>();

}

/*
 * Copyright 2014, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;

import java.util.List;

/**
 * @author jackpark
 * <p>An interface for dealing with publications</p>
 * 
 */
public interface IPublication {
	public static final String
		DOCUMENT_LOCATOR		= "docLox",
		TITLE_FIELD				= "title",
		PUBLICATION_NAME_FIELD	= "pubName",
		PUBLICATION_DATE_FIELD	= "pubDate",
		PUBLICATION_YEAR_FIELD	= "pubYear",
		PUBLICATION_MONTH_FIELD = "pubMnth",
		PUBLICATION_VOLUME_FIELD	= "pubVol",
		PUBLICATION_NUMBER_FIELD	= "pubNum",
		PUBLICATION_TYPE_FIELD		= "pubType",
		PUBLICATION_LOCATOR_FIELD	= "pubLox",
		PAGES_FIELD					= "pages",
		PUBLISHER_NAME_FIELD	= "pblshrName",
		PUBLISHER_LOCATION_FIELD	= "pblshrLoc",
		PUBLISHER_LOCATOR_FIELD		= "pblshrLox",
		COPYRIGHT_FIELD				= "copyright",
		DOI_FIELD				= "doi",
		ISSN_FIELD				= "issn",
		ISBN_FIELD				= "isbn",
		ISO_ABBREVIATION_FIELD	= "isoAbbr",
		GRANT_LIST				= "grants";
	/**
	 * This document's title
	 * @param title
	 */
	void setTitle(String title);
	String getTitle();
	
	void setCopyright(String c);
	String getCopyright();
	/**
	 * E.g. a conference proceedings, blog name, etc
	 * @param name
	 */
	void setPublicationName(String name);
	String getPublicationName();
	
	/**
	 * e.g. March, 2014
	 * @param date can be <code>null</code>
	 */
	void setPublicationDate(String date);
	String getPublicationDate();
	
	/**
	 * Should never be <code>null</code>
	 * @param year
	 */
	void setPublicationYear(String year);
	String getPublicationYear();
	
	void setPubicationVolume(String volume);
	String getPublicationVolume();
	
	void setPublicationNumber(String number);
	String getPublicationNumber();
	
	void setPublicationLocator(String locator);
	String getPublicationLocator();
	
	/**
	 * e.g. "2-23"
	 * @param pages
	 */
	void setPages(String pages);
	String getPages();
	
	void setPublisherName(String name);
	String getPublisherName();
	
	void setPublisherLocator(String locator);
	String getPublisherLocator();
	
	/**
	 * e.g. city, or city, state, country
	 * @param location
	 */
	void setPublisherLocation(String location);
	String getPublisherLocation();
	
	void setDOI(String doi);
	String getDOI();
	
	void setISSN(String issn);
	String getISSN();
	
	void setISBN(String isbn);
	String getISBN();
	
	void setPublicationType(String type);
	String getPublicationType();
	
	void setISOAbbreviation(String abbrev);
	String getISOAbbreviation();
	
	void setDocumentLocator(String locator);
	String getDocumentLocator();
	
	void addGrant(IGrant g);
	List<IGrant> listGrants();

	void setMonth(String month);
	String getMonth();
}

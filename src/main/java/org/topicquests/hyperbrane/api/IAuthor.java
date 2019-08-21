/*
 * Copyright 2018, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;

import java.util.List;

/**
 * @author jackpark
 * <p>Interface for a JSONObject which deals with
 * everything related to an author</p>
 * <p><ol>
 * <li>Various ways to capture an author's name</li>
 * <li>Author's affiliation</li>
 * <li>Author's affiliation topic locator</li>
 * <li>Author's funding source</li>
 * <li>Author's fundind source topic locator</li>
 * <li>Author's document ID</li>
 * <li>Author's document topic locator</li>
 * <li>Author's publisher name</li>
 * <li>Author's publisher's topic locator</li>
 * <li>Author's document's Journal name</li>
 * <li>Author's document's Journal topic locator</li></ol></p>
 * <p>Obviously, not all of those fields are available at the outset</p>
 * <p>Appelations or titles, e.g. Doctor, Professor, Mr. Ms, etc, are included</p>
 * 
 */
public interface IAuthor {
	public static final String
		ID						= "id",
		FULL_NAME_FIELD			= "fullName",
		FIRST_NAME_FIELD		= "firstName",
		MIDDLE_NAME_FIELD		= "midName",
		LAST_NAME_FIELD			= "lastName",
		TITLE_FIELD				= "title",
		SUFFIX					= "suffix",
		DEGREE_FIELD			= "degr",
		INITIALS_FIELD			= "initials",
		NICK_NAME_FIELD			= "nick",
		EMAIL_FIELD				= "email",
		AUTHOR_LOCATOR			= "lox",
		AFFILIATION_NAME_FIELD	= "affilName",
		AFFILIATION_LOCATOR		= "affilLox",
		DOC_ID_FIELD			= "docID",
		DOC_TITLE_FIELD			= "docTitle",
		DOC_LOCATOR_FIELD		= "docLox",
		PUBLISHER_NAME_FIELD	= "pblshrName",
		PUBLISHER_LOCATOR_FIELD	= "bplshrLox",
		PUBLICATION_NAME_FIELD	= "pubName",
		PUBLICATION_LOCATOR		= "pubLox";
		
	
	void setId(String id);
	String getId();
	
	void setAuthorLocator(String locator);
	String getAuthorLocator();
	
	void setAuthorTitle(String title);
	String getAuthorTitle();
	
	void setAuthorFullName(String fullName);
	String getAuthorFullName();
	
	void addAuthorFirstName(String firstName);
	List<String> listAuthorFirstNames();
	
	/**
	 * Can be full or initals or <code>null</code>
	 * @param middleName
	 */
	void setAuthorMiddleName(String middleName);
	String getAuthorMiddleName();
	
	void setAuthorLastName(String lastName);
	String getAuthorLastName();
	
	/**
	 * For "jr", "sr", "I", "II" etc
	 * Can also be included in lastName
	 * @param t
	 */
	void setAuthorSuffix(String t);
	String getAuthorSuffix();
	
	/**
	 * e.g. M.D., PhD, etc -- can be a collection,
	 * e.g. M.D., FACM, ...
	 * @param deg
	 */
	void setAuthorDegree(String deg);
	String getAuthorDegree();
	
	/**
	 * These are initials for the first/middle name,
	 * e.g. J.D <Powers>
	 * @param initials can be <code>null</code>
	 */
	void setAuthorInitials(String initials);
	String getInitials();
	
	/**
	 * Generally a substitute for firstName
	 * @param name
	 */
	void setAuthorNickName(String name);
	String getAuthorNickName();
	
	void setAuthorEmail(String email);
	String getAuthorEmail();
	
	void addAffiliationName(String name);
	List<String> listAffiliationNames();
	
	void setAffiliationLocator(String locator);
	String getAffiliationLocator();
	
	void setDocumentId(String id);
	String getDocumentId(String id);
	
	void setDocumentTitle(String title);
	String getDocumentTitle();
		
	void setPublisherName(String name);
	String getPublisherName();
	
	void setPublisherLocator(String locator);
	String getPublisherLocator();
	
	void setPublicationName(String name);
	String getPublicationName();
	
	void setPublicationLocator(String locator);
	String getPublicationLocator();

}

/*
 * Copyright 2014, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;
import java.util.Date;
import java.util.List;

import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;



/**
 * @author park
 * <p>An <code>IDocument</code> is a topic map {@link ICitation}</p>
 */
public interface IDocument {
	public static final String
		CLUSTER_QUERY_METADATA 	= "ClusterQueryMetadata",
		CLUSTER_TITLE_METADATA	= "ClusterTitleMetadata",
		DOCUMENT_WORD_BIN_SORT	= "DocumentWordBinSort";
	/////////////////////////////////////////////
	//NOTES (20141006)
	// A document probably needs these sections
	//   Provenance
	//   Abstract
	//   Body
	//   References
	// That breakdown allows us to harvest documents
	//  as they become available. Some docs are easiest found
	//  as abstracts; that gives us a start, and forms expectations
	//  for later harvesting the entire document
	// NOTES (20170217)
	// An IDocument must track all the ISentence instances that have
	// been successfully read;
	/////////////////////////////////////////////
	
	void setVersion(String version);
	String getVersion();
	IResult doUpdate();
	void setCreatorId(String id);
	String getCreatorId();
	void setDate(Date date);
	void setDate(String date);
	Date getDate();
	String getDateString();
	void setLastEditDate(Date date);
	void setLastEditDate(String date);
	Date getLastEditDate();
	String getLastEditDateString();
	JSONObject getData();
	String toJSONString();
	
	void setPMID(String pmid);
	String getPMID();
	
	void setPMCID(String pmcid);
	String getPMCID();
	
	void addLabel(String label, String language);
	
	/**
	 * Returns the entire Label structure
	 * @return can return <code>null</code>
	 */
	JSONObject getLabels();
	/**
	 * Returns the first label for <code>language</code>
	 * @param language
	 * @return can return <code>null</code>
	 */
	String getLabel(String language);
	
	/**
	 * Lists all label strings in all languages
	 * @return can return <code>null</code>
	 */
	List<String> listLabels();
	
	/**
	 * List all the labels for <code>language</code>
	 * @param language
	 * @return can return <code>null</code>
	 */
	List<String> listLabels(String language);
	void addDetails(String details, String language);
	JSONObject getDetails();
	String getDetails(String language);
	List<String> listDetails();
	List<String> listDetails(String language);
	void addPropertyValue(String key, String value);
	Object getProperty(String key);
	void removeProperty(String key);
	void removePropertyValue(String key, String value);
	void setProperty(String key, Object value);
	void setNodeType(String typeLocator);
	
	/**
	 * All the metadata about this document
	 * @param doc
	 */
	void setPublication(IPublication doc);
	IPublication getPublication();
	
	void setAbstract(String text, String language);
	
	String getAbstract(String language);

	
	/**
	 * 
	 * @param title TODO
	 * @param initials
	 * @param firstName TODO
	 * @param middleName TODO
	 * @param lastName
	 * @param suffix e.g. 2nd, II, III, etc, can be <code>null</code>
	 * @param degree e.g. M.D., PhD, ... can be <code>null</code>
	 * @param fullName TODO
	 * @param authorLocator TODO
	 * @param publicationName TODO
	 * @param publicationLocator TODO
	 * @param publisherName TODO
	 * @param publisherLocator TODO
	 * @param affiliationName TODO
	 * @param affiliationLocator TODO
	 * @return
	 */
	IAuthor addAuthor(String title, String initials, String firstName, String middleName, String lastName, String suffix, String degree, String fullName, String authorLocator, String publicationName, String publicationLocator, String publisherName, String publisherLocator, String affiliationName, String affiliationLocator);

	void addAuthor(IAuthor author);
	
	void setAuthorList(List<IAuthor> authors);
	
	List<IAuthor> listAuthors();
	
	/////////////////////////////////////////////
	// Topic Map Support
	/////////////////////////////////////////////

	/**
	 * An IDocument can be created for any <em>node</em> brought
	 * into the topic map, especially by way of importing (e.g. OWL ontologies)
	 * @param nodeLocator
	 */
	void setTopicLocator(String nodeLocator);
	String getTopicLocator();
	
	/**
	 * This document's ID
	 * @param id
	 */
	void setId(String id);
	String getId();
	/////////////////////////////////////////////
	// Provenance Support
	/////////////////////////////////////////////

	/**
	 * AgentId is the identity of the agent (person) who
	 * is engaging the harvesting exercise
	 * @param id
	 */
	//void setAgentId(String id);
	//String getAgentId();
	
	void setOntologyClassLocator(String locator);
	String getOntologyClassLocator();
	
	//////////////////////////
	// TODO
	// DBpedia:
	//  We grab a DBPedia JSON blob for sentences
	//   which get hits.
	// Those go with sentences, but perhaps whole blobs
	// should go here too?
	//////////////////////////
	
	void addDbPediaURI(String uri);
	
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	List<String> listDbPediaURIs();
	
	/////////////////////////////////////////////
	// Document Support
	// ICitation asks for authorLocator, not authorName
	/////////////////////////////////////////////
	
	void setLanguage(String lang);
	String getLanguage();
		
	/**
	 * Add documents I cite
	 * @param citation
	 */
	void addMyCitation(String citation);
	List<String> listMyCitations();
	void setMyCitationList(List<String>citations);
	
	/**
	 * Documents which cite me. This may be a simple as a PMID
	 * @param citation
	 */
	void addCitation(String citation);
	List<String>listCitations();
	void setCitationList(List<String>citations);
	
	/////////////////////////////////////////////
	// Paragraph Tree Support
	/////////////////////////////////////////////
	
	/**
	 * <p>An {@link IDocument} is composed of {@link IParagraph} objects,
	 * each of which is composed of {@link ISentence} objects.</p>
	 * <p>An {@link IParagraph} can also be the source of {@link IConversationNode}
	 * objects, each based on an {@link ISentence} object and how it is parsed.</p>
	 * @param paragraph
	 */
	void addParagraph(IParagraph paragraph);
	
	/**
	 * Shortcut way to add a paragraph
	 * @param theParagraph
	 * @param language 
	 */
	void addParagraph(String theParagraph, String language);
	
	/**
	 * Paragraphs don't have their own database, so they
	 * are stored as JSONObjects. When changes occur to them,
	 * they must update the document
	 * @param paragraph
	 */
	void updateParagraph(IParagraph paragraph);
		
	JSONObject getAllAbstracts();
	////////////////////////////////////////////
	//Sentence support
	////////////////////////////////////////////
	
	void addSentence(ISentence sentence);
	
	void addSuccessfullyReadSentenceId(String sentenceId);
	
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	List<String> listSuccessfullyReadSentenceIds();
	
//	void addSentence(String sentence, String userId);
	
	void removeSentence(String sentenceLocator);
	
	/**
	 * 
	 * @return can return <code>null</code>
	 */
	List<ISentence> listSentences();
	
	/**
	 * 
	 * @param language defaults to "en" if <code>null</code>
	 * @return
	 */
	List<String> listParagraphStrings(String language);
	
	List<IParagraph> listParagraphs();
	
	// Metadata Support
	/////////////////////////////////////////////
	void setMetadata(String key, Object value);
	
	JSONObject getMetadata();
	
	Object getMetadataValue(String key);
	
	/**
	 * This document is responsible for all traces related
	 * to sentences, tuples, etc
	 * @param traceMessage
	 */
	void traceStatement(String traceMessage);

	////////////////////
	// Tags
	////////////////////
	
	void addTagName(String tag);
	
	List<String> listTagNames();
	
	void addTagWordGramId(String id);
	List<String> listTagWordGramIds();
	
	///////////////////
	// Substancees
	///////////////////
	
	void addSubstanceName(String name);
	
	List<String> listSubstanceNames();
	
	void addSubstanceWordGramId(String id);
	
	List<String> listSubstanceWordGramIds();

//	JSONObject getMap();
	
//	String toJSONPersist();
	
	/////////////////////
	// isA
	// These are local findings while reading
	/////////////////////
	
	void addIsA(String subjectWordGramId, String objectWordGramId);
	
	boolean isA(String subjectWordGramId, String objectWordGramId);
	
	List<String> listIsAs();
	
	void remove(String subjectWordGramId, String objectWordGramId);

	/////////////////////
	// WordGram Histogram
	// These are local to this document
	/////////////////////
	
	void addToHistogram(String wordgramId);
	
	int getHistogramCount(String wordgramId);
	
}

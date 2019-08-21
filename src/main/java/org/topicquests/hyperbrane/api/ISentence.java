/*
 * Copyright 2014, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

//import org.topicquests.ks.tm.api.IProxy;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;


/**
 * @author park
 * <p>A sentence is broken down into word grams.</p>
 * <p>Each word is given a numeric id; each wordgram is
 * given an id composed of the numeric Ids of each word in it</p>
 */
public interface ISentence  {
	public static final String
		FIRST_STAGE 	= "1", 	//harvested, paragraphs
		SECOND_STAGE	= "2",	// sentences and wordgrams
		THIRD_STAGE		= "3",	// parsed (persist parse)
		FOURTH_STAGE	= "4",	// additional parse, e.g. reverb
		FIFTH_STAGE		= "5",	// first structures: tuples, topic maps, conceptual graphs
		SIXTH_STAGE		= "6";  // finished

	void setID(String id);
	String getID();
	
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
	
	void setNodeType(String typeLocator);
	String getNodeType();
	
	

	/**
	 * A <em>working sentence</em> is a list of {@link IWordGram} objects
	 * which are being considered.
	 * @param ws
	 */
	void setWorkingSentence(List<IWordGram> ws);
	
	/**
	 * <p>Return the <em>working sentence</em></p>
	 * <p>If <code>null</code> is returned, caller must call
	 * $getWorkingSentenceIds() and hydrate the sentence and
	 * then call $setWorkingSentence()</p>
	 * @return can return <code>null</code>
	 */
	List<IWordGram> getWorkingSentence();
	
	/**
	 * Can return <code>null</code> if there is no <em>working sentence</em>
	 * @return
	 */
	List<String> getWorkingSentenceIds();
	
	void setSentence(String sentence);
	String getSentence();
	
	void setNormalizedSentence(String normalizedSentence);
	String getNormalizedSentence();
		
	void setParagraphId(String id);
	String getParagraphId();
	
	void setDocumentId(String id);
	String getDocumentId();
	
	void setStage(String stage);
	String updateToNextStage();
	String getStage();
	
	void setLinkGrammarParseResult(Map<String,Object> result);
	Map<String,Object> getLinkGrammarParseResult();
	
	void addDbPediaData(JSONObject dbPedia);
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	JSONObject getDbPediaData();
	/**
	 * We collect <code>gramId</code> values rather than whole {@link IWordGram} objects
	 * @param gramId
	 */
	void addWordGramId(String gramId);
	
	void setPreviousSentenceId(String id);
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	String getPreviousSentenceId();
	
	void setNextSentenceId(String id);
	
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	String getNextSentenceId();
	
	/**
	 * @return can return <code>null</code>
	 */
	List<String> listWordGramIds();
	
	void removeWordGram(String gramId);
	
	/**
	 * A <em>wordgram with locator</em> is one in which
	 * one or more topic map locators exist; that wordgram
	 * is a label for one or more topics in the topic map. 
	 * @param gramId
	 */
	void addWordGramWithLocatorId(String gramId);
	
	/**
	 * <p>Returns <code>null</code> if the sentence parsing process has not
	 * completed. Parsing might have to wait for further events to clarify ambiguities
	 * or POS types on some WordGrams.</p>
	 * <p>Returns a list of the highest-order {@link IWordGram} objects, including
	 * noun and verb phrases, leaving out all unnecessary objects. This means that
	 * the parsing process has completed for this sentence.</p>
	 * @return
	 */
	List<IWordGram> listFinalParse();
	
	/**
	 * @return can return <code>null</code>
	 */
	List<String>listWordGramsWithLocators();

	void addVerbWordGramId(String gramId);

	List<String> listVerbWordGramIds();
	
	void addNounWordGramId(String gramId);
	
	List<String> listNounWordGramIds();
	
	void addTupleId(String tupleId);
	
	/**
	 * 
	 * @return can return <code>null</code>
	 */
	List<String> listTupleIds();
	
	void removeTuple(String tupleId);
	
	
	
	/**
	 * Any {@link ISentenceTriple} could be a compound object which
	 * contains nested instances of {@link ISentenceTriple}
	 * @param tripleId
	 */
	void setSentenceTripleId(String tripleId);
	
	String getSentenceTripleId();
	
	/**
	 * <p>Normalize this sentence, after its WordGrams are built.
	 * This process calls for registered {@link INormalizeAgent} objects
	 * have done their part. Those agents work with {@link IWordGram}s
	 * found in the sentence.</p>
	 * @return
	 */
	IResult normalize();
	
	/**
	 * A sentence can be a question
	 * @return
	 */
	boolean isQuestion();
	
	void setIsQuestion(boolean t);
	////////////////////////
	// A sentence can contain metadata
	// such as figure numbers, synonyms, and data in parens
	///////////////////////
	
	/**
	 * Returns <code>true</code> if this is about metadata
	 * @return
	 */
	boolean isMeta();
	
	void addFigureNumberWordGramId(String id);
	List<String> listFigureNumberWordGramIds();
	boolean hasFigureNumbers();
	
	/**
	 * Acronyms same as synonyms, typically found as (foo) in a sentence
	 * @param id
	 */
	void addSynonymWordGramId(String id);
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	List<String> listSynonymWordGramIds();
	boolean hasSynonyms();
	
	void addDataWordGramId(String id);
	List<String> listDataWordGramIds();
	boolean hasData();

	

}

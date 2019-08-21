/*
 * Copyright 2014, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;

import java.util.List;
import java.util.Stack;


import net.minidev.json.JSONObject;

//import org.topicquests.model.api.node.INode;

/**
 * @author park
 *
 */
public interface IParagraph {

	void setID(String id);
	String getID();

	void setDocumentId(String id);
	String getDocumentId();

	void setParagraph(String paragraph, String language);
	
	String getParagraph();
	
	String getLanguage();
	/**
	 * <p>This method can be called each time something changes in an ISentence.
	 * It is first called after the sentence has been detected and processed, but
	 * if a sentence is revisited to complete parsing, it can be called again.<?p>
	 * <p>The method returns <code>true</code> if anything changed.</p>
	 * @param sentence
	 * @return
	 */
	boolean addSentence(ISentence sentence);
	void removeSentence(String sentenceId);
	List<ISentence> listSentences();
	
	JSONObject getData();
	
	/**
	 * Utility for processing: when sentences are first added, they establish an 
	 * order; that order must be preserved. For processing, one first fetches the list,
	 * then processes individual sentences on that list, then return the list to the paragraph.
	 * @param sentences
	 */
	void setSentencesIds(List<String> sentences);
	
	/**
	 * Nouns in this paragraph are accumulated by <em>WordGram Id</em>
	 * and list the sentences in which they were detected.
	 * 
	 * @param noun
	 * @param sentenceId
	 */
	void addNoun(IWordGram noun, String sentenceId);
	
	JSONObject getNouns();
	
	/**
	 * As Subject nouns are found, they are pushed here.
	 * @param noun
	 * /
	void pushSubjectNoun (IWordGram noun);
	
	IWordGram popSubjectNoun();
	
	/**
	 * Gives a poppable {@link Stack} of <em>WordGram Ids</em>
	 * @return
	 * /
	Stack<String> showSubjectNouns();
	
	void pushObjectNoun(IWordGram noun);
	IWordGram popObjectNoun();
	Stack<IWordGram> showObjectNouns();
	*/
	
	
}

/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.wordgram.api;

import org.topicquests.hyperbrane.api.IWordGram;
import org.topicquests.support.api.IResult;
import java.util.*;
/**
 * @author jackpark
 *
 */
public interface IWordGramAgentModel {

	/**
	 * <code>text</code> be a word, phrase, or single sentence
	 * @param text
	 * @param userId
	 * @param sentenceId
	 * @return a list of wordGramIds made for the input
	 */
	IResult processString(String text, String userId, String sentenceId);

	/**
	 * For individual words only
	 * @param word
	 * @param userId
	 * @param sentenceId
	 * @return a wordGramId for the <code>word</code>
	 */
	IResult processWord(String word, String userId, String sentenceId);
	
	/**
	 * For labels (name strings) on topics which are named entities - always nouns
	 * @param label
	 * @param userId
	 * @param locator
	 * @return
	 */
	IResult processTopicNameString(String label, String userId, String locator);
	
	/**
	 * <p>Convert an ordered list of word identifiers to a
	 * WordGram <em>object identifier</em></p>
	 * <p>Each identifier is a long integer</p>
	 * <p>The final identifier is the concatination of those
	 * numbers with a "." between each</p>
	 * <p>Note that <em>terminals</em> (single words) always
	 * end with a "." when their numeric identifier is used as an
	 * object identifier</p>
	 * @param wordIds
	 * @return
	 */
	String wordGramId(List<String>wordsIds);
	
	/**
	 * <p>Derive a WordGram object identifier from <code>words</code>
	 * which might be a single word or a sequence of words.</p>
	 * <p>NOTE: the ASR platform limits WordGram size to a maxiumum of 8
	 * words. This method is capable of creating a WordGram object identifier
	 * for a WordGram larger than that limit.</p>
	 * @param words
	 * @return
	 */
	String wordsToGramId(String words);

	/**
	 * Create an {@link IWordGram} for a single word identified by <code>wordId</code>
	 * @param wordId
	 * @param word
	 * @param userId
	 * @param sentenceId
	 * @param lexType
	 * @return
	 */
	IWordGram newTerminal(String wordId, String word, String userId, String sentenceId, String lexType);

	
	/**
	 * Can return <code>null</code> if <code>wordIds</code> > 8 words long
	 * @param wordIds
	 * @param words TODO
	 * @param userId
	 * @param topicLocator
	 * @param lexType
	 * @return
	 */
	IWordGram newWordGram(List<String> wordIds, String words, String userId, String topicLocator, String lexType);

	/**
	 * It is possible that <code>label</code> is a phrase rather than a terminal
	 * @param label
	 * @param userId
	 * @param sentenceId
	 * @return
	 */
	IWordGram generateWordGram(String label, String userId, String sentenceId);

	/**
	 * For the case where WordGram is for one word
	 * @param wordId
	 * @return
	 */
	String singletonId(String wordId);

	/**
	 * Return <code>true</code> if an {@link IWordGram} identified
	 * by <code>id</code> exists in the database.
	 * @param id
	 * @return
	 */
	boolean existsWordGram(String id);

	/**
	 * Adds to dictionary. If new word, makes singleton WordGram,
	 * otherwise, adds sentence to the WordGram
	 * @param word
	 * @param sentenceId can be <code>null</code>
	 * @param userId 
	 * @param lexType 
	 * @return id of the terminal
	 */
	String addWord(String word, String sentenceId, String userId, String lexType);

	/**
	 * <p>Will make the wordgram if it doesn't exist; can handle terminal
	 * @param words -- more than one word  (could be a terminal)</p>
	 * <p>NOTE: this method returns precisely the {@link IWordGram} corresponding
	 * to <code>words</code>. It will ignore any redirects.</p>
	 * @return
	 */
	IWordGram getThisWordGramByWords(String words);

	/**
	 * Creates WordGram or adds sentence to existing
	 * @param wordIds
	 * @param words TODO
	 * @param sentenceId
	 * @param userId TODO
	 * @param topicLocator TODO
	 * @param lexType TODO
	 * @return
	 */
	IWordGram addWordGram(List<String>wordIds, String words, String sentenceId, String userId, String topicLocator, String lexType);

	/**
	 * Returns a {@link IWordGram} which is either identified by
	 * <code>id</code> or by a <code>RedirectIdProperty</code> contained in the
	 * object identified by <code>id</code>
	 * @param id
	 * @return can return <code>null</code>
	 */
	IWordGram getWordGram(String id);
	
	/**
	 * Returns only the {@link IWordGram} identified by <code>id</code>
	 * @param id
	 * @return can return <code>null</code>
	 */
	IWordGram getThisWordGram(String id);

	/**
	 * Unbundle <code>wordGramId</code> to a list of individual word identifiers
	 * in that WordGram
	 * @param wordGramId
	 * @return
	 */
	List<String> wordGramId2WordIds(String wordGramId);

	/**
	 * Connect two WordGrams together with a labeled Edge
	 * @param source
	 * @param target
	 * @param relationLabel
	 * @param context
	 */
	void connectWordGrams(String source, String target, String relationLabel, String context);

}

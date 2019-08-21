/*
 * Copyright 2014, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;
import java.util.List;

import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.JSONObject;
/**
 * @author park
 * <p>A <em>WordGram</em> is a <em>Vertex</em> in a graph. In this
 * definition, it is an extension of the TinkerPop Blueprints {@link Vertex} object, which
 * in the present TopicQuests implementation, exists in a TinkerPop graph running
 * on a RethinkDB platform.</p>
 * <p>NOTE:<br/>
 * WordGrams are representations of individual words and phrases; some can be
 * nouns or even named entities; others can be verbs or verb phrases, as well as
 * dates, and so forth.<br/>
 * Were a WordGram represents a named entity, it will eventually be a representation
 * of a label (namestring) for one or more topics in the topic map.</p>
 * <p>This turns out to be the core point of using WordGram objects: to facilitate the
 * fabrication and maintenance of topic maps.</p>
 * <p>Key to topic mapping is this: there is a need to maintain a
 * <em>One and only one location in the topic map for each subject</em>.</p>
 * <p>That requirement means that, on occasion, two topics will be discovered to
 * be <em>about</em> the same subject. When that happens, both topics are merged
 * <em>as if</em> into a single topic. No matter how the topic merge process occurs,
 * there will be one and only one <em>topic locator</em> for any WordGram that was
 * affected by a merge. So, we must provide for surgical updating of WordGrams when
 * merge events happen.</p>
 */
public interface IWordGram extends Vertex {
	/** gram size */
	public static final String 
		COUNT_1 				= "singleton",
		COUNT_2					= "pair",
		COUNT_3					= "triple",
		COUNT_4					= "quad",
		COUNT_5					= "fiver",
		COUNT_6					= "sixer",
		COUNT_7					= "sevener",
		COUNT_8					= "eighter";
	
	String getID();
	/**
	 * A transient value
	 * @return always returns false except when first created.
	 */
	boolean isNew();
	
	void markIsNew();
	
	/**
	 * Stop words do not track sentences
	 */
	void setIsStopWord();
	boolean getIsStopWord();
	
	/**
	 * <p>The cardinality of the built list tells us something about
	 * the importance of this WordGram</p>
	 * <p>Note that if this is used where no stop words are removed,
	 * then a lot of stop words are going to seem terribly important.</p>
	 * @param sentenceId
	 */
	void addSentenceId(String sentenceId);
	
	void removeSentenceId(String sentenceId);
	List<String>listSentenceIds();
	
	/**
	 * <p>The ASR platform has the ability to, while reading, substitute 
	 * a synonym for a word being read. That substitution is a <em>redirect</em></p>
	 * <p>Another substitution is that of swapping predicates; a predicate phrase such as
	 * <em>is caused by</em> will redirect to the predicate <em>cause</em></p>
	 * @param newWordGramId
	 */
	void setRedirectToId(String newWordGramId);
	String getRedirectToId();
	boolean hasRedirectToId();
	
	/**
	 * <p>Absence of setting this means a predicate is not an inverse</p>
	 * <p>An inverse predicate is exemplified by these two statements:<br/>
	 * A causes B<br/>
	 * B is caused by A<br/>
	 * Both say the same thing, but this system grants privilege to the first one
	 * by marking the second predicate phrase as an <em>inverse</em> with a <em>redirect</em>
	 * to the first one together with swapping subject for object when making the change.</p>
	 */
	void setIsInversePredicate();
	boolean getIsInversePredicate();
	
	/**
	 * <p>Absence of setting this means the predicate is positive</p>
	 * <p>A negative predicate is exemplified by these two statements:<br/>
	 * A causes B<br/>
	 * A does not cause B<br/></p>
	 */
	void setIsNegativePredicate();
	boolean getIsNegativePredicate();
	
	/**
	 * <p>It is possible to engage in contradictory claims such as <br/>
	 * A causes B<br/>
	 * A does not cause B<br/>
	 * Here, we facilitate the ability to link contradictory predicates</p>
	 * @param id
	 */
	void setContradictionPredicateId(String id);

	/**
	 *
	 * @return
	 */
	String getContradictionPredicateId();
	
	boolean hasContradictionPredicate();

	/**
	 * <p>A predicatePropertyType is a property type used
	 * in the topic map. Typical is subOF or instanceOf (taxonomic)</p>
	 * @param typeLocator
	 */
	void setPredicatePropertyType(String typeLocator);
	String getPredicatePropertyType();
	boolean hasPredicatePropertyType();
	

	
	void setPredicateTense(String pastPresentFuture);
	
	/**
	 * Can return <code>null</code> if is not a predicate
	 * @return
	 */
	String getPredicateTense();
	
	/**
	 * A given WordGram can be a label (name string) for one or more
	 * topics in the topic map.
	 * @param topicLocator
	 */
	void addTopicLocator(String topicLocator);
	
	/**
	 * 
	 * @param topicLocator
	 */
	void removeTopicLocator(String topicLocator);
	
	/**
	 * <p>Return the count of topic locators in this {@link IWordGram}.
	 * We care about that only in the case where the gram is associated
	 * with <em>labels</em> of topics. Same label across multiple topics
	 * is a possible hint of merge conditions.</p>
	 * @return
	 */
	int getNumberOfTopicLocators();
	
	/**
	 * <p>Topic Map merging changes a reference from one node to a
	 * new virtual node</p>
	 * @param oldLocator
	 * @param newLocator
	 */
	void substituteTopicLocator(String oldLocator, String newLocator);
	
	List<String>listTopicLocators();

	void setGramType(String t);
	String getGramType();
	
	int getGramSize();
	
	void addWordId(String wordId);
	List<String> listWordIds();
	
	//////////////////////////////////////////
	// DBPedia
	//////////////////////////////////////////

	/**
	 * A sentence can have none to several DBpedia objects
	 * @param dbp
	 */
	void addDbPediaJSON(JSONObject dbp);
	
	List<JSONObject> listDbPediaObjects();
	
	/**
	 * Returns <code>true</code> if any objects exist
	 * @return
	 */
	boolean hasDBPedia();
	//////////////////////////////////////////
	// Lens Codes
	// Lenses are collections of OpenSherlock agents which
	// assist in reading processes.
	//////////////////////////////////////////
	/**
	 * A <em>Lens</em> <code>code</code> is an
	 * identifier of a sentence analyzer
	 * @param code
	 */
	void addLensCode(String code);
	List<String> listLensCodes();
	boolean containsLensCode(String code);
	void removeLensCode(String code);

	//////////////////////////////////////////
	// LatticeTypes and CG matching
	//////////////////////////////////////////

	void addLatticeType(String type);
	
	void removeLatticeType(String type);
	
	List<String> listLatticeTypes();
	
	boolean hasLatticeType(String type);

	//////////////////////////////////////////
	// LexTypes and Expectations
	//////////////////////////////////////////

	/**
	 * Picks up whether noun or noun phrase
	 */
	void addIsNounType();
	/**
	 * A kind of noun
	 */
	void addIsProperNounType();
	void addIsGerundType();
	/**
	 * Picks up whether verb or verb phrase
	 */
	void addIsVerbType();
	void addIsAdverbType();
	void addIsAdjectiveType();
	void addIsPronounType();
	void addIsPrepositionType();
	void addIsConjunctionType();
	void addIsQuestionWordType();
	void addIsStopWordType();
	void addIsDeterminerType();
	
	/**
	 * A gram could represent more than one lexType
	 * @param lt
	 */
	void addLexType(String lt);
	
	/**
	 * 
	 * @return can return <code>null</code> if not known
	 */
	List<String> listLexTypes();
	
	boolean containsLexType(String type);
	
	/**
	 * Use Case: "v" will match with "v" or "vp" or "vt",same for nouns
	 * @param type
	 * @return
	 */
	boolean containsLexTypeLike(String type);
	/**
	 * Returns <code>true</code> if any lexType is known
	 * @return
	 */
	boolean hasLexType();
	
	boolean isNoun();
	boolean isGerund();
	boolean isDeterminer();
	boolean isVerb();
	boolean isAdjective();
	boolean isAdverb();
	boolean isPronoun();
	boolean isPreposition();
	boolean isConjunction();
	boolean isConjunctiveAdverb();
	boolean isQuestionWord();
	boolean isStopWord();
	boolean isMeta();
	boolean isNumber();
	/**
	 * e.g. 24.6%
	 * @return
	 */
	boolean isPercentage();

	void addExpectation(String lexType);
	List<String> listExpectations();
	
	boolean expectsNoun();
	boolean expectsVerb();
	boolean expectsAdjective();
	boolean expectsAdverb();
	boolean expectsPronoun();
	boolean expectsPreposition();
	boolean expectsConjunction();
	boolean expectsQuestionWord();

	//////////////////////////////////////////
	// LinkGrammar Parsing support
	// not presently used
	//////////////////////////////////////////

	void setFormulaId(String id);
	
	/**
	 * Can return <code>null</code> if no formula exists
	 * @return
	 */
	String getFormulaId();

	//////////////////////////////////////////
	// hypernym, hyponym, synonym, roles
	// from WordNet processing
	//////////////////////////////////////////
	
	void addHypernymWord(String word);
	List<String>listHypernyms();
	boolean hasHypernyms();
	
	void addHyponymWord(String word);
	List<String> listHyponyms();
	boolean hasHyponyms();
	
	void addSynonym(String word);
	List<String> listSynonyms();
	boolean hasSynonyms();

	/**
	 * SemanticFrame interpretation can assign roles to wordgrams
	 * specific to a given <code>sentenceId</code> (from Verbnet/FrameNet processing)
	 * @param sentenceId
	 * @param role
	 */
	void addRole(String sentenceId, String role);
	
	/**
	 * Can return <code>null</code>
	 * @param sentenceId
	 * @return
	 */
	String getRole(String sentenceId);
	//////////////////////////////////////////
	// SemanticFrame API
	//////////////////////////////////////////

	void addSemanticFrameNodeId(String id);
	List<String> listSemanticFrameIds();
	boolean hasSemanticFrames();
	
	//////////////////////////////////////////
	// The words in this gram
	//////////////////////////////////////////

	/**
	 * Keep the words around for viewing
	 * @param words
	 */
	void setWords(String words);
	
	String getWords();
	
	//////////////////////////////////////////
	// Attributes: possible decorations
	//////////////////////////////////////////

	/**
	 * @see {@link IVocabularyAttributes}
	 * @param attribute
	 */
	void addAttribute(String attribute);
	
	/**
	 * <p>Add the {@link IWordGram} id for a synonym for this gram.</p>
	 * <p>Synonyms are typically discovered as metagrams following noun phrases.</p>
	 * @param id
	 */
	void addSynonymId(String id);
	
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	List<String> listSynonymIds();
	
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	List<String>listAttributes();
	
	boolean hasAttribute(String attribute);
	
	//////////////////////////////////////////
	// Other stuff, likely not used
	//////////////////////////////////////////

	/**
	 * If a singleton has been corrected, we keep it around but use
	 * this API to make <em>transform-time</em> corrections
	 * @param id
	 * @deprecated see setRedirectToId
	 */
	void setCorrectedWordId(String id);
	
	/**
	 * 
	 * @param word
	 * @deprecated see getRedirectToId
	 */
	void setCorrectedWord(String word);
	/**
	 * @deprecated
	 * @return
	 */
	String getCorrectedWordId();
	/**
	 * @deprecated
	 * @return
	 */
	String getCorrectedWord();
	
	/**
	 * <p>A {@link INormalizeAgent} is registered with particular
	 * instances of {@link IWordGram} where they can perform
	 * normalizing computations.</p>
	 * @param a
	 */
//	void registerNounNormalizerAgent(INormalizeAgent a);
	
//	void registerPredicateNormalizeAgent(INormalizeAgent a);
	
//	void registerOtherNormalizeAgent(INormalizeAgent a);
	
	/**
	 * <p>If there are any {@link INormalizeAgent}s registered,
	 * they will fire and produce their results.</p>
	 * <p>Note that any given {@link ISentence} might have many 
	 * different agents; they are internally fired, generally nouns first,
	 * then predicates.</p>
	 * @param doc
	 * @param sentence
	 * @return
	 */
	//IResult fireNormalizeAgents(IDocument doc, ISentence sentence);
	
	/**
	 * <p>A <em>DAEMON</em> is a token which represents a particular
	 * handler for this word. DAEMONs are particularly valuable for
	 * handling the many forms of some predicates, e.g. "causes" and 
	 * "is caused by". These handlers are required for building {@link INTuple}
	 * objects from particular {@link ISentence} and {@link IWordGram} objects.</p>
	 * @param wordId
	 * @param daemonToken
	 */
	void setDaemon(String wordId, String daemonToken);
	
	/**
	 * Return the DAEMON token for this word if it exists
	 * @param wordId
	 * @return can return <code>null</code>
	 */
	String getDaemon(String wordId);
	
	JSONObject getJSONObject();
}

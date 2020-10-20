/*
 * Copyright 2014, 2018, 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane;

import java.util.*;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import org.topicquests.hyperbrane.api.ILexTypes;
import org.topicquests.hyperbrane.api.IWordGram;
import org.topicquests.os.asr.wordgram.WordGramEnvironment;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.impls.sql.SqlGraph;
import com.tinkerpop.blueprints.impls.sql.SqlVertex;

/**
 * @author park
 *
 */
public class ConcordanceWordGram  implements IWordGram {
	private WordGramEnvironment environment;
	private SqlGraph graph;
	private SqlVertex data;
	private static final long serialVersionUID = 1L;
	public static final String GRAM_ID = "gramId";
	public static final String 
		ID			 			= "id",
		SENTENCES 				= "sentences",
		TOPICS					= "topics",
		SYNONYMS				= "synonyms",
		DAEMON					= "daemon",
		GRAM_TYPE				= "gramType",
		GRAM_SIZE				= "gramSize",
		LEX_TYPES				= "lexTypes",
		LATTICE_TYPES			= "latticeTypes",
		EXPECTATIONS			= "expectations",
		WORD_IDS				= "wordids",
		STOP_WORD_BOOLEAN		= "isStopWord",
		//WORDS					= "words",
		HYPERNYM_IDS			= "hyperids",
		HYPONYM_IDS				= "hypoids",
		SYNONYM_IDS				= "synids",
		SEMFRAME_IDS			= "frameids",
		LENS_CODES				= "lensCodes",
		CORRECTED_WORD			= "correctedWord",
		CORRECTED_ID			= "correctedId",
		REDIRECT_ID_PROPERTY	= "RedirectIdProperty",
		ATTRIBUTES				= "Attributes",
		IS_INVERSE_PREDICATE	= "isInversePredicate",
		IS_NEGATIVE_PREDICATE	= "isNegativePredicate",
		CONTRADICTION_ID		= "contradictionId",
		PROPERTY_TYPE			= "propertyType",
		PREDICATE_TENSE			= "predicateTense",
		SEQUENCE_ID			 	= "seqId",
		FOLLOW_LIST_PROPERTY	= "followerL",
		PREVIOUS_LIST_PROPERTY	= "previousL",
		FORMULA_ID				= "fId",
		DB_PEDIA_OBJECT			= "dbpo",
		VERSION					= "version",
		LEMMA					= "lemma";

	private boolean _isNew = false;
	
	private Object synchObject;
	
	public ConcordanceWordGram(SqlVertex v, WordGramEnvironment env) {
		data = v;
		//label is set later
		environment = env;
		environment.logDebug("CWG- "+data);
		graph = environment.getSqlGraph();
		synchObject = new Object();
	}
	
	@Override
	public String getID() {
		return data.getId();
	}
	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IWordGram#addSentenceId(java.lang.String)
	 */
	@Override
	public void addSentenceId(String sentenceId) {
		environment.logDebug("ConcordanceWordGram.addSentenceId "+sentenceId+" | "+getId());
		graph.addToVertexSetProperty((String)getId(), SENTENCES, sentenceId);
		//synchronized(synchObject) {
		//	data.addToSetProperty(SENTENCES, sentenceId);
		//}
	}
	
	@Override
	public void removeSentenceId(String sentenceId) {
		synchronized(synchObject) {
			data.deleteProperty(SENTENCES, sentenceId);
		}
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IWordGram#listSentenceIds()
	 */
	@Override
	public List<String> listSentenceIds() {
		synchronized(synchObject) {
			return data.listProperty(SENTENCES);
		}
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IWordGram#setGramType(java.lang.String)
	 */
	@Override
	public void setGramType(String t) {
		data.setProperty(GRAM_TYPE, t);
		environment.logDebug("ConcordanceWordGram.setGramType "+t);
		if (t.equals(IWordGram.COUNT_1))
			data.setProperty(GRAM_SIZE, "1");
		else if (t.equals(IWordGram.COUNT_2))
			data.setProperty(GRAM_SIZE, "2");
		else if (t.equals(IWordGram.COUNT_3))
			data.setProperty(GRAM_SIZE, "3");
		else if (t.equals(IWordGram.COUNT_4))
			data.setProperty(GRAM_SIZE, "4");
		else if (t.equals(IWordGram.COUNT_5))
			data.setProperty(GRAM_SIZE, "5");
		else if (t.equals(IWordGram.COUNT_6))
			data.setProperty(GRAM_SIZE, "6");
		else if (t.equals(IWordGram.COUNT_7))
			data.setProperty(GRAM_SIZE, "7");
		else 
			data.setProperty(GRAM_SIZE, "8");
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IWordGram#getGramType()
	 */
	@Override
	public String getGramType() {
		return (String)data.getProperty(GRAM_TYPE);
	}
	
	@Override
	public int getGramSize() {
		String x = (String)data.getProperty(GRAM_SIZE);
		return Integer.parseInt(x);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IWordGram#addWordId(java.lang.String)
	 */
	@Override
	public void addWordId(String wordId) {
		synchronized(synchObject) {
			data.addToSetProperty(WORD_IDS, wordId);
		}
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IWordGram#listWordIds()
	 */
	@Override
	public List<String> listWordIds() {
		synchronized(synchObject) {
			return data.listProperty(WORD_IDS);
		}
	}

	@Override
	public void addTopicLocator(String topicLocator) {
		//synchronized(synchObject) {
			data.addToSetProperty(TOPICS, topicLocator);
		//}
	}
	
	@Override
	public void addSynonymId(String id) {
		//synchronized(synchObject) {
			data.addToSetProperty(SYNONYMS, id);
		//}
	}

	@Override
	public List<String> listSynonymIds() {
		//synchronized(synchObject) {
			return data.listProperty(SYNONYM_IDS);
		//}
	}

	@Override
	public void substituteTopicLocator(String oldLocator, String newLocator) {
		synchronized(synchObject) {
			List<String> o = data.listProperty(TOPICS);
			
			if (o.isEmpty())
				data.setProperty(TOPICS, newLocator);
			else {
				if (o.contains(oldLocator))
					data.updateProperty(TOPICS, newLocator, oldLocator);
				else
					data.setProperty(TOPICS, newLocator);
			}		
		}
	}

	@Override
	public void removeTopicLocator(String topicLocator) {
		//synchronized(synchObject) {
			data.deleteProperty(TOPICS, topicLocator);
		//}
	}

	@Override
	public List<String> listTopicLocators() {
		//synchronized(synchObject) {
			return data.listProperty(TOPICS);
		//}
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IDictionary#setDaemon(java.lang.String)
	 */
	@Override
	public void setDaemon(String wordId, String daemonToken) {
		//we reuse the wordId by appending to it
		String did = wordId+".DID";
		data.setProperty(did, daemonToken);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IDictionary#getDaemon(java.lang.String)
	 */
	@Override
	public String getDaemon(String wordId) {
		String did = wordId+".DID";
		return (String)data.getProperty(did);
	}

	@Override
	public void setWords(String words) {
		data.setLabel(words);
	}

	@Override
	public String getWords() {
//		System.out.println("GETWORDS "+getData());
		return data.getLabel();
	}

	@Override
	public void setCorrectedWordId(String id) {
		//synchronized(synchObject) {
			data.setProperty(CORRECTED_ID, id);
		//}
	}

	@Override
	public void setCorrectedWord(String word) {
		//synchronized(synchObject) {
			data.setProperty(CORRECTED_WORD, word);
		//}
	}

	@Override
	public String getCorrectedWordId() {
		//synchronized(synchObject) {
			return (String)data.getProperty(CORRECTED_ID);
		//}
	}

	@Override
	public String getCorrectedWord() {
		//synchronized(synchObject) {
			return (String)data.getProperty(CORRECTED_WORD);
		//}
	}

	@Override
	public int getNumberOfTopicLocators() {
		//synchronized(synchObject) {
			List<String>l = this.listTopicLocators();
			return l.size();
		//}
	}

	@Override
	public void setRedirectToId(String newWordGramId) {
		//synchronized(synchObject) {
			data.setProperty(this.REDIRECT_ID_PROPERTY, newWordGramId);
		//}
	}

	@Override
	public String getRedirectToId() {
		synchronized(synchObject) {
		environment.logDebug("ConcordanceWordGram.getRedirecctToId ");
			Object o = data.getProperty(REDIRECT_ID_PROPERTY);
			environment.logDebug("getRedirecctToId "+o);
			return (String)o;
		}
	}

	@Override
	public boolean hasRedirectToId() {
		return (getRedirectToId() != null);
	}

	@Override
	public void addAttribute(String attribute) {
		//synchronized(synchObject) {
			data.addToSetProperty(ATTRIBUTES, attribute);
		//}
	}

	@Override
	public List<String> listAttributes() {
		//synchronized(synchObject) {
			return data.listProperty(ATTRIBUTES);
		//}
	}

	@Override
	public boolean hasAttribute(String attribute) {
		List<String>a = listAttributes();
		return a.contains(attribute);
	}

	@Override
	public void setIsInversePredicate() {
		data.setProperty(IS_INVERSE_PREDICATE, "true");		
	}

	@Override
	public boolean getIsInversePredicate() {
		String which = (String)data.getProperty(IS_INVERSE_PREDICATE);
		return (which != null);
	}

	@Override
	public void setPredicateTense(String pastPresentFuture) {
		data.setProperty(PREDICATE_TENSE, pastPresentFuture);
	}

	@Override
	public String getPredicateTense() {
		return (String)data.getProperty(PREDICATE_TENSE);
	}

	@Override
	public void setIsNegativePredicate() {
		data.setProperty(IS_NEGATIVE_PREDICATE, "true");		
	}

	@Override
	public boolean getIsNegativePredicate() {
		String which = (String)data.getProperty(IS_NEGATIVE_PREDICATE);
		return (which != null);
	}

	@Override
	public void setContradictionPredicateId(String id) {
		data.setProperty(CONTRADICTION_ID, id);		
	}

	@Override
	public String getContradictionPredicateId() {
		return (String)data.getProperty(CONTRADICTION_ID);
	}

	@Override
	public boolean hasContradictionPredicate() {
		return (data.getProperty(CONTRADICTION_ID) != null);
	}

	@Override
	public void setPredicatePropertyType(String typeLocator) {
		data.setProperty(PROPERTY_TYPE, typeLocator);		
	}

	@Override
	public String getPredicatePropertyType() {
		return (String)data.getProperty(PROPERTY_TYPE);
	}

	@Override
	public boolean hasPredicatePropertyType() {
		return (data.getProperty(PROPERTY_TYPE) != null);
	}

	public void addLexType(String lexType) {
		synchronized(synchObject) {
			data.addToSetProperty(LEX_TYPES, lexType);
		}
	}

	@Override
	public List<String> listLexTypes() {
		synchronized(synchObject) {
			return data.listProperty(LEX_TYPES);
		}
	}

	@Override
	public boolean hasLexType() {
		return listLexTypes() != null;
	}

	@Override
	public boolean isNoun() {
		return checkLexType(ILexTypes.NOUN) || checkLexType(ILexTypes.NOUN_PHRASE) ||
				checkLexType(ILexTypes.PROPER_NOUN) ||
				checkLexType(ILexTypes.INFERRED_NOUN) ||
				checkLexType(ILexTypes.INFERRED_NOUNPHRASE);
		//TODO include gerund?
	}
	@Override
	public boolean isGerund() {
		return checkLexType(ILexTypes.GERUND);
	}
	
	@Override
	public boolean isStopWord() {
		return checkLexType(ILexTypes.STOP_WORD);
	}

	@Override
	public boolean isVerb() {
		return checkLexType(ILexTypes.VERB) || checkLexType(ILexTypes.VERB_PHRASE);
	}

	@Override
	public boolean isAdjective() {
		return checkLexType(ILexTypes.ADJECTIVE);
	}

	@Override
	public boolean isAdverb() {
		return checkLexType(ILexTypes.ADVERB);
	}

	@Override
	public boolean isConjunctiveAdverb() {
		return checkLexType(ILexTypes.CONJUNCTIVE_ADVERB);
	}
	
	@Override
	public boolean isPronoun() {
		return checkLexType(ILexTypes.PRONOUN);
	}

	@Override
	public boolean isPreposition() {
		return checkLexType(ILexTypes.PREPOSITION);
	}

	@Override
	public boolean isDeterminer() {
		return checkLexType(ILexTypes.DETERMINER);
	}

	@Override
	public boolean isConjunction() {
		return (checkLexType(ILexTypes.CONJUNCTION) ||
				checkLexType(ILexTypes.C_CONJUNCTION) ||
				checkLexType(ILexTypes.R_CONJUNCTION));
	}
	
	@Override
	public boolean isMeta() {
		return checkLexType(ILexTypes.META_TYPE);
	}

	@Override
	public boolean isQuestionWord() {
		return checkLexType(ILexTypes.QUESTION_WORD);
	}
	
	boolean checkLexType(String lexType) {
		//synchronized(synchObject) {
			List<String> o = data.listProperty(LEX_TYPES);
			if (o.isEmpty())
				return false;
			return (o.contains(lexType));
		//}
	}

	@Override
	public void addExpectation(String lexType) {
		//synchronized(synchObject) {
			data.addToSetProperty(EXPECTATIONS, lexType);
		//}
	}

	@Override
	public List<String> listExpectations() {
		//synchronized(synchObject) {
			return data.listProperty(EXPECTATIONS);
		//}
	}

	@Override
	public boolean expectsNoun() {
		return checkExpectation(ILexTypes.NOUN) || checkExpectation(ILexTypes.NOUN_PHRASE);
	}
	
	boolean checkExpectation(String lexType) {
		List<String> o = null;
		//synchronized(synchObject) {
			o = data.listProperty(EXPECTATIONS);
		//}
		if (o.isEmpty())
			return false;
		return (o.contains(lexType));
		
	}

	@Override
	public boolean expectsVerb() {
		return checkExpectation(ILexTypes.VERB) || checkExpectation(ILexTypes.VERB_PHRASE);
	}

	@Override
	public boolean expectsAdjective() {
		return checkExpectation(ILexTypes.ADJECTIVE);
	}

	@Override
	public boolean expectsAdverb() {
		return checkExpectation(ILexTypes.ADVERB);
	}

	@Override
	public boolean expectsPronoun() {
		return checkExpectation(ILexTypes.PRONOUN);
	}

	@Override
	public boolean expectsPreposition() {
		return checkExpectation(ILexTypes.PREPOSITION);
	}

	@Override
	public boolean expectsConjunction() {
		return checkExpectation(ILexTypes.CONJUNCTION);
	}

	@Override
	public boolean expectsQuestionWord() {
		return checkExpectation(ILexTypes.QUESTION_WORD);
	}

	@Override
	public void addIsNounType() {
		String t = ILexTypes.NOUN;
		if (this.getGramSize() > 1)
			t = ILexTypes.NOUN_PHRASE;
		addLexType(t);
	}

	@Override
	public void addIsProperNounType() {
		addLexType(ILexTypes.PROPER_NOUN);
	}

	@Override
	public void addIsGerundType() {
		addLexType(ILexTypes.GERUND);
	}

	@Override
	public void addIsVerbType() {
		String t = ILexTypes.VERB;
		if (this.getGramSize() > 1)
			t = ILexTypes.VERB_PHRASE;
		addLexType(t);
	}

	@Override
	public void addIsAdverbType() {
		addLexType(ILexTypes.ADVERB);
	}

	@Override
	public void addIsAdjectiveType() {
		addLexType(ILexTypes.ADJECTIVE);
	}
	
	@Override
	public void addIsDeterminerType() {
		addLexType(ILexTypes.DETERMINER);
	}

	@Override
	public void addIsPronounType() {
		addLexType(ILexTypes.PRONOUN);
	}

	@Override
	public void addIsPrepositionType() {
		addLexType(ILexTypes.PREPOSITION);
	}

	@Override
	public void addIsConjunctionType() {
		addLexType(ILexTypes.CONJUNCTION);
	}

	@Override
	public void addIsQuestionWordType() {
		addLexType(ILexTypes.QUESTION_WORD);
	}
	
	@Override
	public void addIsStopWordType() {
		addLexType(ILexTypes.STOP_WORD);
	}

	@Override
	public void addLensCode(String code) {
		synchronized(synchObject) {
			data.addToSetProperty(LENS_CODES, code);
		}
	}

	@Override
	public List<String> listLensCodes() {
		synchronized(synchObject) {
			 return data.listProperty(LENS_CODES);
		}
	}

	@Override
	public boolean containsLensCode(String code) {
		List<String> o = listLensCodes();
		if (o.isEmpty())
			return false;
		return o.contains(code);
	}

	@Override
	public void removeLensCode(String code) {
		data.deleteProperty(LENS_CODES, code);
	}

	String pluckGramId(String struct) {
		String id = null;
		int where = struct.indexOf(this.GRAM_ID);
		int where2;
		if (where > -1) {
			where2 = struct.indexOf(':', where);
			where = where2; // now past "gramId":
			where2 = struct.indexOf('"', where); // pick up first quote
			where = where2;
			where = struct.indexOf('"', where+1); // pick up last quote
			id = struct.substring(where, where2);		
		}
		return id;
	}

	@Override
	public boolean isNew() {
		return _isNew;
	}

	@Override
	public void markIsNew() {
		_isNew = true;
	}

	@Override
	public void setFormulaId(String id) {
		data.setProperty(FORMULA_ID, id);
	}

	@Override
	public String getFormulaId() {
		return (String)data.getProperty(FORMULA_ID);
	}

	@Override
	public void setDbPediaURI(String uri) {
		data.setProperty(DB_PEDIA_OBJECT, uri);
	}

	@Override
	public String getDbPediaURI() {
		return data.getProperty(DB_PEDIA_OBJECT);
	}

	@Override
	public boolean hasDBPedia() {
		return getDbPediaURI() != null;
	}

	@Override
	public boolean isNumber() {
		return checkLexType(ILexTypes.NUMBER);
	}

	@Override
	public boolean isPercentage() {
		return checkLexType(ILexTypes.PERCENT_NUMBER);
	}

	@Override
	public void addHypernymWord(String word) {
		//synchronized(synchObject) {
			data.addToSetProperty(HYPERNYM_IDS, word);
		//}
	}

	@Override
	public List<String> listHypernyms() {
		//synchronized(synchObject) {
			return data.listProperty(HYPERNYM_IDS);
		//}
	}

	@Override
	public boolean hasHypernyms() {
		return listHypernyms() != null;
	}

	@Override
	public void addHyponymWord(String word) {
		//synchronized(synchObject) {
			data.addToSetProperty(HYPONYM_IDS, word);
		//}
	}

	@Override
	public List<String> listHyponyms() {
		//synchronized(synchObject) {
			return data.listProperty(HYPONYM_IDS);
		//}
	}

	@Override
	public boolean hasHyponyms() {
		return listHyponyms() != null;
	}

	@Override
	public void addSynonym(String word) {
		//synchronized(synchObject) {
			data.addToSetProperty(SYNONYM_IDS, word);
		//}
	}

	@Override
	public List<String> listSynonyms() {
		//synchronized(synchObject) {
			return data.listProperty(SYNONYMS);
		//}
	}

	@Override
	public boolean hasSynonyms() {
		return listSynonymIds() != null;
	}

	@Override
	public void addSemanticFrameNodeId(String id) {
		//synchronized(synchObject) {
			data.addToSetProperty(SEMFRAME_IDS, id);
		//}
	}

	@Override
	public List<String> listSemanticFrameIds() {
		//synchronized(synchObject) {
			return data.listProperty(SEMFRAME_IDS);
		//}
	}

	@Override
	public boolean hasSemanticFrames() {
		return listSemanticFrameIds() != null;
	}

	@Override
	public void addRole(String sentenceId, String role) {
		// TODO Auto-generated method stub
		throw new RuntimeException("ConcordenceWordGram.addRole not implemented");
	}

	@Override
	public String getRole(String sentenceId) {
		// TODO Auto-generated method stub
		throw new RuntimeException("ConcordenceWordGram.getRole not implemented");
	}

	@Override
	public Iterable<Edge> getEdges(Direction direction, String... labels) {
		return data.getEdges(direction, labels);
	}

	@Override
	public Iterable<Vertex> getVertices(Direction direction, String... labels) {
		return data.getVertices(direction, labels);
	}

	@Override
	public VertexQuery query() {
		return data.query();
	}

	@Override
	public Edge addEdge(String label, Vertex inVertex) {
		return data.addEdge(label, inVertex);
	}

	@Override
	public <T> T getProperty(String key) {
		return data.getProperty(key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return data.getPropertyKeys();
	}

	@Override
	public void setProperty(String key, Object value) {
		data.setProperty(key, value);
	}

	@Override
	public <T> T removeProperty(String key) {
		return data.removeProperty(key);
	}

	@Override
	public void remove() {
		data.remove();
	}

	@Override
	public Object getId() {
		return data.getId();
	}

	@Override
	public JSONObject getJSONObject() {
		return data.getData();
	}

	@Override
	public boolean containsLexType(String type) {
		List<String>l = this.listLexTypes();
		if (l == null || l.isEmpty())
			return false;
		return l.contains(type);
	}

	@Override
	public boolean containsLexTypeLike(String type) {
		List<String>l = this.listLexTypes();
		if (l == null || l.isEmpty())
			return false;
		Iterator<String>itr = l.iterator();
		String x;
		while (itr.hasNext()) {
			x = itr.next();
			if (x.startsWith(type))
				return true;
		}
		return false;
	}

	@Override
	public void addLatticeType(String type) {
		//synchronized(synchObject) {
			data.addToSetProperty(LATTICE_TYPES, type);
		//}
	}

	@Override
	public void removeLatticeType(String type) {
		data.deleteProperty(LATTICE_TYPES, type);
	}

	@Override
	public List<String> listLatticeTypes() {
		//synchronized(synchObject) {
			return data.listProperty(LATTICE_TYPES);
		//}
	}

	@Override
	public boolean hasLatticeType(String type) {
		List<String>l = this.listLatticeTypes();
		if (l == null || l.isEmpty())
			return false;
		Iterator<String>itr = l.iterator();
		String x;
		while (itr.hasNext()) {
			x = itr.next();
			if (x.startsWith(type))
				return true;
		}
		return false;
	}

	@Override
	public void setIsStopWord() {
		data.setProperty(STOP_WORD_BOOLEAN, "T");
	}

	@Override
	public boolean getIsStopWord() {
		return data.getProperty(STOP_WORD_BOOLEAN) != null;
	}

	@Override
	public void setVersion(String version) {
		data.addToSetProperty(VERSION, version);
	}

	@Override
	public String getVersion() {
		return data.getProperty(VERSION);
	}

	@Override
	public void setLemma(String lemma) {
		synchronized(synchObject) {
			data.addToSetProperty(LEMMA, lemma);
		}
	}

	@Override
	public String getLemma() {
		return data.getProperty(LEMMA);
	}




}

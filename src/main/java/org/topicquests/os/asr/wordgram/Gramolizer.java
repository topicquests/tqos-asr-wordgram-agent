/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.wordgram;

import java.util.*;

import org.topicquests.hyperbrane.api.IHyperMembraneConstants;
import org.topicquests.hyperbrane.api.ILexTypes;
import org.topicquests.hyperbrane.api.IWordGram;
import org.topicquests.os.asr.wordgram.api.IWordGramAgentModel;

import com.google.common.base.Splitter;

/**
 * @author jackpark
 *
 */
public class Gramolizer {
	private WordGramEnvironment environment;
	private IWordGramAgentModel model;
	private  String 
		commaId = null,
		colonId = null,
		semicolonId = null,
		periodId = null,
		exclaimId = null,
		questionId = null,
		leftParenId = null,
		rightParenId = null,
		leftCurlyId = null,
		rightCurlyId = null,
		leftBrackId = null,
		rightBrackId = null,
		leftCarrotId = null,
		rightCarrotId = null,
		quoteId = null,
		tickId = null;

	/**
	 * 
	 */
	public Gramolizer(WordGramEnvironment env) {
		environment = env;
		model = environment.getModel();
		System.out.println("Gramolizer- "+model);
	}
	
	public void init() {
		//BIG ISSUE
		// We can test for punctuation in the model and add stopword if not already
		// but we need some new lextypes for some of these punctuations
		quoteId = getPunctuationId("\"", ILexTypes.STOP_WORD); //needs another lextype for what it signifes to quote
		commaId = getPunctuationId(",", ILexTypes.C_CONJUNCTION); // also a stopword
		colonId = getPunctuationId(":", ILexTypes.C_CONJUNCTION); // also a stopword
		semicolonId = getPunctuationId(";", ILexTypes.C_CONJUNCTION); //also a stopword
		periodId = getPunctuationId(".", ILexTypes.STOP_WORD); //needs another lextype
		exclaimId = getPunctuationId("!", ILexTypes.STOP_WORD); //needs another lextype
		questionId = getPunctuationId("?", ILexTypes.QUESTION_WORD);//also a stopword
		leftParenId = getPunctuationId("(", ILexTypes.STOP_WORD);//needs another lextype
		rightParenId = getPunctuationId(")", ILexTypes.STOP_WORD);//needs another lextype
		leftCurlyId = getPunctuationId("{", ILexTypes.STOP_WORD); //needs another lextype
		rightCurlyId = getPunctuationId("}", ILexTypes.STOP_WORD); //needs another lextype
		leftBrackId = getPunctuationId("[", ILexTypes.STOP_WORD);//needs another lextype
		rightBrackId = getPunctuationId("]", ILexTypes.STOP_WORD);//needs another lextype
		leftCarrotId = getPunctuationId("<", ILexTypes.STOP_WORD);//needs another lextype
		rightCarrotId = getPunctuationId(">", ILexTypes.STOP_WORD);//needs another lextype
		tickId = getPunctuationId("'", ILexTypes.STOP_WORD);//needs another lextype
		
	}
	
	String getPunctuationId(String p, String lexType) {
		return model.addWord(p, null, "SystemUser", lexType) ;

	}
	
	/**
	 * Returns a collection of WordGramIds from this <code>sentence</code>
	 * @param sentence
	 * @param userId
	 * @param sentenceId can be <code>null</code>
	 * @return
	 */
	public List<String> processSentence(String sentence, String userId, String sentenceId) {
		List<Map<String,Object>> vectors = gramolizeSentence(sentence, userId, sentenceId);
		return this.vectorsToWordGrams(vectors, userId, sentenceId);
	}
	
	List<Map<String,Object>> gramolizeSentence(String sentence, String userId, String sentenceId) {
		environment.logDebug("Gramolizer.gramolizeSentence- "+sentence);
		List<Map<String,Object>> result;
		Iterable<String> ix = Splitter.on(' ')
			       .trimResults()
			       .omitEmptyStrings()
			       .split(sentence);
		List<String>wordIds = new ArrayList<String>();
		List<String>words = new ArrayList<String>();
		//Here, we deal with all the individual words
		//////////////////////////
		//  Walk along a list of words
		//	Detect if they begin or end with punctuation
		//  Build a list of wordIds for every word and any leading or trailing punctuation
		//  	Punctuation is stripped off the word and added as a word itself
		//////////////////////////
		for (String w: ix) {
			if (w.endsWith("%")) {
				String x = w.substring(0, (w.length()-1));
				doWord(x, wordIds, words, userId, sentenceId);
				doWord("%", wordIds, words, userId, sentenceId);
			}
			else doWord(w, wordIds, words, userId, sentenceId);
		}
		result = vectorizeTuples(wordIds, words);
		environment.logDebug("Gramolizer.gramolizeSentence+ "+result);

		return result;
	}
	
	void doWord(String w, List<String>wordIds, List<String>words, String userId, String sentenceId ) {
		String theWordId;
		String theWord;

		boolean endsWithComma = false;
		boolean endsWithQuestionMark = false;
		boolean endsWithColon = false;
		boolean endsWithSemicolon = false;
		boolean endsWithPeriod = false;
		boolean endsWithExclaim = false;
		boolean endsWithTick = false;
		boolean endsWithQuote = false;
		boolean endsWithParen = false;
		boolean endsWithBrack = false;
		boolean endsWithCarrot = false;
		boolean endsWithCurly = false;
		boolean startsWithTick = false;
		boolean startsWithQuote = false;
		boolean startsWithParen = false;
		boolean startsWithBrack = false;
		boolean startsWithCarrot = false;
		boolean startsWithCurly = false;

		if (w != "") {
			endsWithComma = endsWithComma(w);
			endsWithQuestionMark = endsWithQuestionMark(w);
			endsWithColon = endsWithColon(w);
			endsWithSemicolon = endsWithSemicolon(w);
			endsWithTick = endsWithTick(w);
			startsWithTick = startsWithTick(w);
			endsWithPeriod = endsWithPeriod(w);
			endsWithParen = endsWithParen(w);
			startsWithParen = startsWithParen(w);
			endsWithBrack = endsWithBrack(w);
			startsWithBrack = startsWithBrack(w);
			endsWithCurly = endsWithCurly(w);
			startsWithCurly = startsWithCurly(w);
			endsWithCarrot = endsWithCarrot(w);
			startsWithCarrot = startsWithCarrot(w);
			endsWithCarrot = endsWithCarrot(w);
			startsWithCarrot = startsWithCarrot(w);
			//Deal with leading special characters
			if (startsWithTick) {
				wordIds.add(tickId);
				words.add("'");
			}
			else if (startsWithQuote) {
				wordIds.add(quoteId);
				words.add("\"");
			}
			else if (startsWithParen) {
				wordIds.add(leftParenId);
				words.add("(");
			}
			else if (startsWithBrack) {
				wordIds.add(this.leftBrackId);
				words.add("[");
			}
			else if (startsWithCurly) {
				wordIds.add(this.leftCurlyId);
				words.add("{");
			}
			else if (startsWithCarrot) {
				wordIds.add(leftCarrotId);
				words.add(w);
			}
			theWord = cleanWord(w);
			//Deal with the word itself, stripped of special characters
			theWordId = model.addWord(theWord, sentenceId, userId, null);
			wordIds.add(theWordId);
			words.add(theWord);
			//Deal with trailing characters
			if (endsWithComma) {
				wordIds.add(commaId);
				words.add(",");
			} else if (endsWithColon) {
				wordIds.add(colonId);
				words.add(":");
			} else if (endsWithSemicolon) {
				wordIds.add(semicolonId);
				words.add(";");
			} else if (endsWithQuestionMark) {
				wordIds.add(questionId);
				words.add("?");
			} else if (endsWithPeriod) {
				wordIds.add(periodId);
				words.add(".");
			} else if (endsWithExclaim) {
				wordIds.add(exclaimId);
				words.add("!");
			} else if (endsWithTick) {
				wordIds.add(tickId);
				words.add("'");
			} else if (endsWithQuote) {
				wordIds.add(quoteId);
				words.add("\"");
			} else if (endsWithParen) {
				wordIds.add(rightParenId);
				words.add(")");
			} else if (endsWithBrack) {
				wordIds.add(rightBrackId);
				words.add(w);
			} else if (endsWithCurly) {
				wordIds.add(rightCurlyId);
				words.add("]");
			} else if (endsWithCarrot) {
				wordIds.add(rightCarrotId);
				words.add("}");
			}
		}
	}
	
	List<Map<String,Object>> vectorizeTuples(List<String>wordIds, List<String> words) {
		System.out.println("Vectorizing- "+wordIds);
		List<Map<String,Object>>result = new ArrayList<Map<String,Object>>();
		//we don't deal with terminals since they are handled with each word
		//WRONG: sure they are handled with each word, but WE NEED THEM HERE
		result.add(listTerminals(wordIds, words));
		result.add(listPairs(wordIds, words));
		result.add(listTriples(wordIds, words));
		result.add(listQuads(wordIds, words));
		result.add(listFivers(wordIds, words));
		result.add(listSixers(wordIds, words));
		result.add(listSeveners(wordIds, words));
		result.add(listEighters(wordIds, words));
		System.out.println("Vectorizing+ "+result);
		return result;
	}
	
	//////////////////////////////
	// In these lists, the last string in the list is the words themselves
	// all other strings are the wordIds associated with those words
	/////////////////////////////
	
	Map<String,Object> listTerminals(List<String> wordIds, List<String> words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<Object> x = new ArrayList<Object>();
		List<String> y;
		result.put("cargo", x);
		result.put("key", IHyperMembraneConstants.TERMINAL);
		int len = wordIds.size();
		for (int i=0;i<len;i++) {
			y = new ArrayList<String>();
			x.add(y);
			y.add(wordIds.get(i));
			y.add(words.get(i));
		}	
		return result;
	}	
	
	Map<String,Object> listPairs(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<Object> x = new ArrayList<Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.PAIR);
		int len = wordIds.size();
		if (len > 1) {
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+1)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(words.get(i));
					y.add(words.get(i+1));
				}
			}
		}
		return result;
	}
	
	Map<String,Object> listTriples(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<Object> x = new ArrayList<Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.TRIPLE);
		int len = wordIds.size();
		if (len > 2) {
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+2)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(wordIds.get(i+2));
					y.add(words.get(i));
					y.add(words.get(i+1));
					y.add(words.get(i+2));
				}
			}	
		}
		return result;
	}	
	Map<String,Object> listQuads(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<Object> x = new ArrayList<Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.QUAD);
		int len = wordIds.size();
		if (len > 3) {
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+3)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(wordIds.get(i+2));
					y.add(wordIds.get(i+3));
					y.add(words.get(i));
					y.add(words.get(i+1));
					y.add(words.get(i+2));
					y.add(words.get(i+3));
				}
			}	
		}
		return result;
	}
	Map<String,Object> listFivers(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.FIVER);
		int len = wordIds.size();
		if (len > 4) {
			List<Object> x = new ArrayList<Object>();
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+4)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(wordIds.get(i+2));
					y.add(wordIds.get(i+3));
					y.add(wordIds.get(i+4));
					y.add(words.get(i));
					y.add(words.get(i+1));
					y.add(words.get(i+2));
					y.add(words.get(i+3));
					y.add(words.get(i+4));
				}
			}
		}
		
		return result;
	}
	
	Map<String,Object> listSixers(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.SIXER);
		int len =  wordIds.size();
		if (len > 5) {
			List<Object> x = new ArrayList<Object>();
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+5)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(wordIds.get(i+2));
					y.add(wordIds.get(i+3));
					y.add(wordIds.get(i+4));
					y.add(wordIds.get(i+5));
					y.add(words.get(i));
					y.add(words.get(i+1));
					y.add(words.get(i+2));
					y.add(words.get(i+3));
					y.add(words.get(i+4));
					y.add(words.get(i+5));
				}
			}	
		}
		return result;
	}

	Map<String,Object> listSeveners(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.SEVENER);
		int len =  wordIds.size();
		if (len > 6) {
			List<Object> x = new ArrayList<Object>();
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+6)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(wordIds.get(i+2));
					y.add(wordIds.get(i+3));
					y.add(wordIds.get(i+4));
					y.add(wordIds.get(i+5));
					y.add(wordIds.get(i+6));
					y.add(words.get(i));
					y.add(words.get(i+1));
					y.add(words.get(i+2));
					y.add(words.get(i+3));
					y.add(words.get(i+4));
					y.add(words.get(i+5));
					y.add(words.get(i+6));
				}
			}	
		}
		return result;
	}
	
	Map<String,Object> listEighters(List<String> wordIds, List<String>words) {
		Map<String,Object> result = new HashMap<String,Object>();
		List<String> y;
		result.put("key", IHyperMembraneConstants.EIGHTER);
		int len =  wordIds.size();
		if (len > 7) {
			List<Object> x = new ArrayList<Object>();
			result.put("cargo", x);
			for (int i=0;i<len;i++) {
				if ((i+7)<(len)) {
					y = new ArrayList<String>();
					x.add(y);
					y.add(wordIds.get(i));
					y.add(wordIds.get(i+1));
					y.add(wordIds.get(i+2));
					y.add(wordIds.get(i+3));
					y.add(wordIds.get(i+4));
					y.add(wordIds.get(i+5));
					y.add(wordIds.get(i+6));
					y.add(wordIds.get(i+7));
					y.add(words.get(i));
					y.add(words.get(i+1));
					y.add(words.get(i+2));
					y.add(words.get(i+3));
					y.add(words.get(i+4));
					y.add(words.get(i+5));
					y.add(words.get(i+6));
					y.add(words.get(i+7));
				}
			}	
		}
		return result;
	}	
	
	
	/**
	 * Returns a list of all unique WordGramIds 
	 * @param vectors
	 * @param userId
	 * @param sentenceId
	 * @return
	 */
	List<String> vectorsToWordGrams(List<Map<String, Object>> vectors, String userId, String sentenceId) {
		environment.logDebug("Gramolizer.vectorsToWordGrams- "+vectors);
		List<String> result = new ArrayList<String>();
		Iterator<Map<String,Object>> itr = vectors.iterator();
		Map<String,Object>mo;
		List<List<String>>cargo;
		String key;
		int length = 8; // default
		Set<String>foo = new HashSet<String>();
		while (itr.hasNext()) {
			mo = itr.next();
			cargo = (List<List<String>>)mo.get("cargo");
			
			if (cargo != null) {
				key = (String)mo.get("key");
				environment.logDebug("Gramolizer.vectorsToWordGrams-1 "+key);
				if (!key.equals(IHyperMembraneConstants.TERMINAL)) {
					// Terminls were already added to model
					if (key.equals(IHyperMembraneConstants.PAIR))
						length = 2;
					else if (key.equals(IHyperMembraneConstants.TRIPLE))
						length = 3;
					else if (key.equals(IHyperMembraneConstants.QUAD))
						length = 4;
					else if (key.equals(IHyperMembraneConstants.FIVER))
						length = 5;
					else if (key.equals(IHyperMembraneConstants.SIXER))
						length = 6;
					else if (key.equals(IHyperMembraneConstants.SEVENER))
						length = 7;
					else if (key.equals(IHyperMembraneConstants.EIGHTER))
						length = 8;
	
					Set<String> x = new HashSet<String>();
					processCargo(cargo, userId, sentenceId, length, x);
					environment.logDebug("Gramolizer.vectorsToWordGrams-2 "+x);
					foo.addAll(x);
				} else {
					Iterator<List<String>>itx = cargo.iterator();
					//add the terminal Ids
					while (itx.hasNext())
						foo.add(itx.next().get(0));
				}
			}
		}
		result.addAll(foo);
		return result;
	}
	
	/**
	 * 
	 * @param cargo
	 * @param userId
	 * @param sentenceId
	 * @param length -- gramsize
	 * @param s --  collector for gramIds
	 */
	void processCargo(List<List<String>>cargo, String userId, String sentenceId, int length, Set<String> s) {
		environment.logDebug("Gramolizer.processCargo "+cargo+" "+length);
		//[[34., 35., inflammatory, pathways]] 2
		Set<String>result = new HashSet<String>();
		Iterator<List<String>>itr = cargo.iterator();
		List<String>list;
		List<String>wordIds;
		StringBuilder buf = new StringBuilder();
		String words = null;
		int len = 2*length; //-1; // length of "list" element
		// half the list will be ids for each of the other half, the words
		String id;
		IWordGram g;
		while(itr.hasNext()) {
			buf.setLength(0);
			list = itr.next();
			wordIds = new ArrayList<String>();
			// get the wordIds, each a Terminal ID
			for (int i=0;i<length;i++) {
				id = list.get(i);
				wordIds.add(id);
			}
			// get the words and turn them into a phrase
			for (int i=length;i<len;i++) {
				buf.append(list.get(i)+" ");
			}
			words = buf.toString().trim();
			environment.logDebug("GramolizerLRPx-1 "+wordIds+" "+words);
			//[34., 35.] inflammatory <-- that's a bug; fixed by correcting "len"
			g = model.addWordGram(wordIds, words, sentenceId, "SystemUser", null, null);	
			s.add(g.getID());
		}
	}

	///////////////////////////
	// utilities
	///////////////////////////
	boolean endsWithComma(String w) {
		return w.trim().endsWith(",");
	}
	boolean endsWithQuestionMark(String w) {
		return w.trim().endsWith("?");
	}

	boolean endsWithColon(String w) {
		return w.trim().endsWith(":");
	}
	boolean endsWithSemicolon(String w) {
		return w.trim().endsWith(";");
	}
	
	boolean endsWithPeriod(String w) {
		return w.trim().endsWith(".");
	}

	boolean endsWithExclaim(String w) {
		return w.trim().endsWith("!");
	}
	
	boolean endsWithCurly(String w) {
		return w.trim().endsWith("}");
	}
	
	boolean endsWithBrack(String w) {
		return w.trim().endsWith("]");
	}
	
	boolean endsWithParen(String w) {
		return w.trim().endsWith(")");
	}
	
	boolean endsWithCarrot(String w) {
		return w.trim().endsWith(">");
	}
	boolean endsWithQuote(String w) {
		return w.trim().endsWith("\"");
	}
	boolean endsWithTick(String w) {
		return w.trim().endsWith("'");
	}

	boolean startsWithPeriod(String w) {
		return w.trim().startsWith(".");
	}
	boolean startsWithCarrot(String w) {
		return w.trim().startsWith("<");
	}
	boolean startsWithParen(String w) {
		return w.trim().startsWith("(");
	}
	boolean startsWithBrack(String w) {
		return w.trim().startsWith("[");
	}
	boolean startsWithQuote(String w) {
		return w.trim().startsWith("\"");
	}
	boolean startsWithTick(String w) {
		return w.trim().startsWith("'");
	}
	boolean startsWithCurly(String w) {
		return w.trim().startsWith("{");
	}

	/**
	 * Strip of leading and trailing special characters
	 * @param w
	 * @return
	 */
	String cleanWord(String w) {
		String result = w.trim();
		if (result.endsWith(",") ||
			result.endsWith(".") ||
			result.endsWith(";") ||
			result.endsWith("?") ||
			result.endsWith("\"") ||
			result.endsWith("'") ||
			result.endsWith("}") ||
			result.endsWith("]") ||
			result.endsWith(">") ||
			result.endsWith(")") ||
			result.endsWith("!")) {
			result = result.substring(0, (result.length()-1));
		}
		if (result.startsWith("\"") ||
			result.startsWith("<") ||
			result.startsWith("{") ||
			result.startsWith("(") ||
			result.startsWith("[")) {
			result = result.substring(1);
		}
		return result;
	}
}

/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.wordgram;

import java.sql.SQLException;
import java.util.*;

import org.topicquests.hyperbrane.ConcordanceWordGram;
import org.topicquests.hyperbrane.api.IDictionary;
import org.topicquests.hyperbrane.api.IWordGram;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.os.asr.common.api.IASRFields;
import org.topicquests.os.asr.wordgram.api.IWordGramAgentModel;
import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.support.util.LRUCache;

import com.tinkerpop.blueprints.impls.sql.SqlEdge;
import com.tinkerpop.blueprints.impls.sql.SqlGraph;
import com.tinkerpop.blueprints.impls.sql.SqlVertex;

import org.topicquests.hyperbrane.api.ILexTypes;

/**
 * @author jackpark
 *
 */
public class WordGramModel implements IWordGramAgentModel {
	private WordGramEnvironment environment;
	private IStatisticsClient stats;
	private PostgresConnectionFactory database = null;
	private LRUCache wgCache;
	private IDictionary dictionary;
	private Gramolizer gramolizer;
	/////////////////////////////////
	// Huge Conundrum
	//  the quote character is a wordgram like other punctuation
	// It, too, needs a WordGram but it's never made or stored.
	// In fact, all punctuation is a "stopWord"
	/////////////////////////////////
	private SqlGraph graph;
	/**
	 * 
	 */
	public WordGramModel(WordGramEnvironment env) {
		environment = env;
		graph = environment.getSqlGraph();
		database = graph.getProvider();
		stats = environment.getStatisticsClient();
		wgCache = new LRUCache(8192);
		dictionary = environment.getDictionary();
	}
	
	public void setWordNetThread(Gramolizer g) {
		this.gramolizer = g;
	}
	
	@Override
	public IResult processString(String text, String userId, String sentenceId) {
		IResult result = new ResultPojo();
		List<String>gramIds;
		if (text.indexOf(' ') > -1) {
			gramIds = gramolizer.processSentence(text, userId, sentenceId);
		} else {
			gramIds = new ArrayList<String>();
			String gramId = this.addWord(text, sentenceId, userId, null);
			gramIds.add(gramId);
		}
		result.setResultObject(gramIds);
		//for debugging
		//environment.ping();
		return result;
	}
	
	
	
	@Override
	public IResult processTopicNameString(String label, String userId, String locator) {
		IResult result = processString(label, userId, null);
		List<String> gramIds = (List<String>)result.getResultObject();
		IWordGram g = getThisWordGramByWords(label);
		g.addIsNounType();
		return result;
	}

	@Override
	public IResult processWord(String word, String userId, String sentenceId) {
		IResult result = new ResultPojo();
		String gramId = this.addWord(word, sentenceId, userId, null);
		return result;
	}

	@Override
	public String wordGramId(List<String> wordIds) {
		environment.logDebug("Gramolizer.wordGramId "+wordIds);
		int len = wordIds.size();
		if (len == 1)
			return singletonId(wordIds.get(0));
		StringBuilder buf = new StringBuilder();
		//must avoid adding a dot if word already has one
		boolean hasDot = false;
		String ix;
		for (int i=0;i<len;i++) {
			ix = wordIds.get(i);
			hasDot = ix.endsWith(".");
			if (i > 0 && !hasDot)
				buf.append(".");
			buf.append(ix);
			hasDot = false;
		}
		String id = buf.toString();
		if (id.endsWith("."))
			id = id.substring(0, (id.length()-1));
		return buf.toString();
	}

	@Override
	public String wordsToGramId(String words) {
		String [] wx = words.trim().split(" ");
		List<String>l = new ArrayList<String>();
		for (String w:wx) {
			l.add(dictionary.getWordId(w.trim()));
		}
		return this.wordGramId(l);
	}

	@Override
	public IWordGram newTerminal(String wordId, String word, String userId, String sentenceId, String lexType) {
		String gramId = wordId.trim();
		int where = gramId.indexOf(".");
		if (where < 0)
			gramId = this.singletonId(wordId);
		boolean isPunctuation = isPunctuation(word);
		environment.logDebug("WordGramModel.newTerminal- "+wordId+" "+gramId+" "+where);
		IWordGram result = this.getThisWordGram(gramId);
		environment.logDebug("WordGramModel.newTerminal-1 "+gramId+" "+result);
		if (result == null) {
			SqlVertex v = (SqlVertex)graph.addVertex(gramId, word);
			environment.logDebug("WordGramModel.newTerminal-2 "+gramId+" "+v);
			result = new ConcordanceWordGram(v, environment);
			result.setGramType(IWordGram.COUNT_1);
			result.markIsNew();
			if (sentenceId != null)
				result.addSentenceId(sentenceId);
			if (lexType != null)
				result.addLexType(lexType);
			if (isPunctuation)
				result.addLexType(ILexTypes.STOP_WORD);
			String w = "\""; //default for quote character
			if (!wordId.equals("0."))
				w = dictionary.getWord(wordId);
			result.setWords(w);
			
		} else {
			if (lexType != null)
				result.addLexType(lexType);
			if (sentenceId != null)
				result.addSentenceId(sentenceId);
		}
		wgCache.add(gramId, result);
		return result;	
	}

	boolean isPunctuation(String word) {
		return (word.equals("\"") ||
				word.equals(".") ||
				word.equals(",") ||
				word.equals("?") ||
				word.equals("!") ||
				word.equals("'") ||
				word.equals(":") ||
				word.equals(";") ||
				word.equals("(") ||
				word.equals(")") ||
				word.equals("[") ||
				word.equals("]") ||
				word.equals("{") ||
				word.equals("}") ||
				word.equals("<") ||
				word.equals(">"));
	}

	String cleanId(String id) {
		String result = id;
		if (result.endsWith("."))
			result = result.substring(0, (result.length()-1));
		return result;
	}
	@Override
	public IWordGram newWordGram(List<String> wordIds, String words, String userId, String topicLocator, String lexType) {
		environment.logDebug("WordGramModel.newWordGram- "+wordIds);
		///////////////////
		// MODIFIED to return <code>null</code> if too long
		String gramId = this.wordGramId(wordIds);
		if (wordIds.size() > 8) {
			environment.logError("WordGramModel.newWordGram too many words "+wordIds, null);
			//throw new RuntimeException("WordGramModel.newWordGram too many words "+wordIds);
			return null;
		}
		SqlVertex v = (SqlVertex)graph.addVertex(gramId);

		IWordGram result = new ConcordanceWordGram(v, environment);
		result.setGramType(wordGramIdsToCountString(wordIds.size()));
		result.markIsNew();
		environment.logDebug("WordGramModel.newWordGram-X "+gramId+" "+wordIds.size()+" "+result.getGramType());
		if (topicLocator != null)
			result.addTopicLocator(topicLocator);
		if (lexType != null)
			result.addLexType(lexType);

		StringBuilder buf = new StringBuilder();
		for (String idx:wordIds) {
			
			buf.append(this.dictionary.getWord(cleanId(idx))+" ");
		}
		result.setWords(buf.toString().trim());
		String t = result.getGramType().trim();
		//NOTE: COUNT_1 seems to fail a lot: bad count errors on "singleton"
		//added trim(), and added sanity hack
		//TODO spend more time figuring out why singleton fails this test
		environment.logDebug("WordGramModel.newWordGram-1 "+t);
		if (t.equals(IWordGram.COUNT_1) || (wordIds.size()==1))
			stats.addToKey(IASRFields.WG1);
		else if (t.equals(IWordGram.COUNT_2)) //was missing else
			stats.addToKey(IASRFields.WG2);
		else if (t.equals(IWordGram.COUNT_3))
			stats.addToKey(IASRFields.WG3);
		else if (t.equals(IWordGram.COUNT_4))
			stats.addToKey(IASRFields.WG4);
		else if (t.equals(IWordGram.COUNT_5))
			stats.addToKey(IASRFields.WG5);
		else if (t.equals(IWordGram.COUNT_6))
			stats.addToKey(IASRFields.WG6);
		else if (t.equals(IWordGram.COUNT_7))
			stats.addToKey(IASRFields.WG7);
		else if (t.equals(IWordGram.COUNT_8))
			stats.addToKey(IASRFields.WG8);
		else {
			String msg = "WordGramModel.newWordGram bad count: "+gramId+" | "+t;
			environment.logError(msg, null);
			//environment.getEventRegistry().addWordGramEvent(IWordGramEvent.BAD_WORDGRAM, gramId);
		}
		environment.logDebug("WordGramModel.newWordGram+ "+result.getId()+" | "+result.getGramType()+" | "+result.getGramSize());
		return result;
	}

	@Override
	public IWordGram generateWordGram(String label, String userId, String sentenceId) {
		environment.logDebug("WordGramModel.generate- "+label+" "+sentenceId);
		IWordGram result = null;
		String lbl = label.trim();
		if (!lbl.equals("")) {
			int where = lbl.indexOf(' ');
			String id;
			if (where == -1) {
				id = this.addWord(lbl, null, userId, null);
				result = (IWordGram)wgCache.get(id);
				if (sentenceId != null && !sentenceId.equals(""))
					result.addSentenceId(sentenceId);
			} else {
				String [] temp = lbl.split(" ");
				int len = temp.length;
				List<String>wordIds = new ArrayList<String>();
				String w;
				for (int i=0;i<len;i++) {
					w = new String(temp[i].trim());
					id = this.addWord(w, null, userId, null);
					if (i < 8)
						wordIds.add(id);
				}
//List<String> wordIds, String words, String sentenceId, String userId,String topicLocator, String lexType
				result = this.addWordGram(wordIds, label, sentenceId, userId, null, null);
			}
		}
		return result;
	}

	@Override
	public String singletonId(String wordId) {
		if (wordId.endsWith("."))
			return wordId;
		return wordId+".";
	}

	@Override
	public boolean existsWordGram(String id) {
		boolean result = (wgCache.get(id) != null);
		if (result)
			return true;
	    IPostgresConnection conn = null;
	    IResult r = null;
	    try {
	      conn = database.getConnection();
	      r = conn.beginTransaction();
	      conn.setConvRole(r);
	      if (r.hasError())
	    	  environment.logError("DataProvider.existsWord1: "+r.getErrorString(), null);

	      result = graph.vertexExists(conn, id);
	      
	    } catch (SQLException e) {
	      environment.logError(e.getMessage(), e);
	    }
	    conn.endTransaction(r);
	    conn.closeConnection(r);

	    return result;
	 }

	@Override	//l, null, userId, topicLocator, null
	public String addWord(String word, String sentenceId, String userId, String lexType) {
		String wordId = "-1";
		boolean isNew = false;
		if (word == null)
			return wordId;
		//This only tests to see if it is local; if not, returns null
		wordId = dictionary.getWordId(word);
		environment.logDebug("WordGramModel.addWord- "+word+" "+wordId);
		////////////////////
		//There is a scenario in which the word exists, but is not yet in
		// a WordGram form
		String gramId = null;
		
		try {
			IWordGram g;
			IResult r = null;
			if (wordId == null) {
				//it's a new word, result = id 
				if (word.equals("\"")) {
					wordId = "0";
				} else
					r = dictionary.addWord(word);
				wordId = (String)r.getResultObject();
				isNew = ((Boolean)r.getResultObjectA()).booleanValue();
				environment.logDebug("WordGramModel.addWord-1 "+word+" "+wordId+"  "+isNew);
				//MUST SEE IF WE HAVE THIS YET?
				g = this.newTerminal(wordId, word, userId, sentenceId, null);
				//g.setWords(word); words already set with label
				if (isNew)
					stats.addToKey(IASRFields.WG1);
				if (sentenceId != null)
					g.addSentenceId(sentenceId);
				if (lexType != null)
					g.addLexType(lexType);
				gramId = (String)g.getId();
				wgCache.add(gramId, g);
				
				environment.logDebug("WordGramModel.addWord-2 "+word+" "+g.getWords());
			} else {
				//The word exists, but needs to be counted
				stats.addToKey(IASRFields.WORDS_READ); //TODO should always count words read
				gramId = this.singletonId(wordId);
				//now have a singleton id
				//get it as a singleton
				g = (IWordGram)wgCache.get(gramId);
				environment.logDebug("WordGramModel.addWord-3 "+wordId+" "+g);
				if (g != null) {
					//already exists in wgCache
					if (lexType != null)
						g.addLexType(lexType);

					if (sentenceId != null) {
						g.addSentenceId(sentenceId);
					}
				} else {
					// not in cache need to put it in cache
					g = getThisWordGram(gramId);
					if (g != null) {
						environment.logDebug("WordGramModel.addWord-4 "+sentenceId+" "+g);
						if (sentenceId != null)
							g.addSentenceId(sentenceId);
					} else {
						environment.logDebug("WordGramModel.addWord-5 "+sentenceId+" "+g);
						g = newTerminal(gramId, word, userId, sentenceId, lexType);
					}
					wgCache.add(gramId, g);

				}
			}
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
		}
		environment.logDebug("WordGramModel.addWord-5 "+gramId);
		return gramId;
	}

	String validateId(String id) {
		String result = id;
		if (!id.endsWith(".")) {
			if (id.indexOf(".") < 0) {
				result = this.singletonId(id);
			}
		}
		return result;
	}
	
	String getGramId(String phrase) {
		environment.logDebug("GetGramId- "+phrase);
		String result = null;
		String words = phrase.trim();
		String id;
		IResult r;
		if (words.indexOf(' ') > -1) {
			environment.logDebug("GetGramId-1 ");
			String [] wx = words.split(" ");
			StringBuilder buf = new StringBuilder();
			List<String>ids = new ArrayList<String>();
			int counter = 0;
			for (String word:wx) {
				
				r = dictionary.addWord(word.trim());
				id = (String)r.getResultObject();
				ids.add(id);
				if (counter++ > 0)
					buf.append(".");
				buf.append(id);
			}
			// always makes an id ending with NO PERIOD
			result = buf.toString();
			//Sanity check: if this gram doesn't exist, make it
			
		} else {
			r = dictionary.addWord(words);
			environment.logDebug("GetGramId-2 "+r.getResultObject()); //is terminal
			id = (String)r.getResultObject();
			result = this.singletonId(id);
		}
		return result;
	}
	
	@Override
	public IWordGram getThisWordGramByWords(String phrase) {
		String words = phrase.trim();
		environment.logDebug("WordGramModel.getThisWordGramByWords- "+words);
		IWordGram result = null;
		String id;
		IResult r;
		if (words.indexOf(' ') > -1) {
			String [] wx = words.split(" ");
			StringBuilder buf = new StringBuilder();
			List<String>ids = new ArrayList<String>();
			int counter = 0;
			for (String word:wx) {
				
				r = dictionary.addWord(word.trim());
				id = (String)r.getResultObject();
				ids.add(id);
				if (counter++ > 0)
					buf.append(".");
				buf.append(id);
			}
			// always makes an id ending with NO PERIOD
			result = getThisWordGram(buf.toString());
			environment.logDebug("WordGramModel.getThisWordGramByWords "+buf.toString()+" "+ids+" "+result);
			if (result == null)
				result = this.addWordGram(ids, words, null, "SystemUser", null, null);
		} else {
			id = this.getGramId(phrase);
			environment.logDebug("WordGramModel.getThisWordGramByWords-1 "+id);
			result = this.getWordGram(id);
			if (result == null) {
				r = dictionary.addWord(words);
				id = (String)r.getResultObject();
				result = getThisWordGram(this.singletonId(id));
			}
			if (result == null)
				result = this.newTerminal(id, words, "SystemUser", null, null);
			environment.logDebug("WordGramModel.getThisWordGramByWords+ "+id+" "+result);
		}
		return result;
	}

	@Override
	public IWordGram addWordGram(List<String> wordIds, String words, String sentenceId, String userId,
			String topicLocator, String lexType) {
		String gramId = this.wordGramId(wordIds);
		IWordGram result = null;
			result = (IWordGram)wgCache.get(gramId);
			if (result == null)
				result = getThisWordGram(gramId);
//			System.out.println("ASRMa-1 "+result);
			if (result == null) {
				//is new
				result = newWordGram(wordIds, words, userId, topicLocator, lexType);
				if (sentenceId != null) 
					result.addSentenceId(sentenceId);
				//database.putWordGram(result);
				
			} else {
				if (sentenceId != null) {
					result.addSentenceId(sentenceId);
				}
				if (topicLocator != null) {
					result.addTopicLocator(topicLocator);
				}
				if (lexType != null)
					result.addLexType(lexType);

			}
			wgCache.add(gramId, result);
			
			environment.logDebug("ADDWG "+topicLocator+" "+result.getWords());
		return result;
	}

	@Override
	public IWordGram getWordGram(String id) {
		IWordGram g = getThisWordGram(id);
		environment.logDebug("WordGramModel.getWordGram- "+id+" "+g);
		if (g != null && g.getRedirectToId() != null) {
			environment.logDebug("WordGramModel.getWordGram-1 "+id+" "+g);
			return getThisWordGram(g.getRedirectToId());
		}
		environment.logDebug("WordGramModel.getWordGram+ "+id+" "+g);
		return g;
	}

	@Override
	public IWordGram getThisWordGram(String id) {
		IWordGram g = (IWordGram)wgCache.get(id);
		if (g == null) {
		      SqlVertex v = (SqlVertex)graph.getVertex(id);
		      if (v != null)
		    	  g = new ConcordanceWordGram(v, environment);  			
		}
		return g;
	}

	@Override
	public List<String> wordGramId2WordIds(String wordGramId) {
		List<String>result = new ArrayList<String>();
		String [] words = wordGramId.split(".");
		int len = words.length;
		for (int i=0;i<len;i++)
			result.add(new String(words[i]));
		return result;
	}

	
	///////////////////////////
	//Utilities
	private String wordGramIdsToCountString(int count) {
		switch(count) {
		case 1:
			return IWordGram.COUNT_1;
		case 2:
			return IWordGram.COUNT_2;
		case 3:
			return IWordGram.COUNT_3;
		case 4:
			return IWordGram.COUNT_4;
		case 5:
			return IWordGram.COUNT_5;
		case 6:
			return IWordGram.COUNT_6;
		case 7:
			return IWordGram.COUNT_7;
		case 8:
			return IWordGram.COUNT_8;
		}
		return "badshit";
	}

	@Override
	public void connectWordGrams(String source, String target, String relationLabel, String context) {
		environment.logDebug("WGM.connectWordGrams- "+source+" "+target+" "+relationLabel);
		IWordGram fg = getThisWordGramByWords(source);
		IWordGram tg = getThisWordGramByWords(target);
				
		//String fromId = this.getGramId(source);
		//String toId = this.getGramId(target);
		environment.logDebug("WGM.connectWordGrams-1 "+fg+" "+tg);
		SqlVertex from = graph.getVertex(fg.getId());
		SqlVertex to = graph.getVertex(tg.getId());
		environment.logDebug("WGM.connectWordGrams-2 "+from+" "+to);
		if (from != null && to != null) {
			SqlEdge e = graph.addEdge(UUID.randomUUID().toString(), from, to, relationLabel);
			environment.logDebug("WGM.connectWordGrams+ "+e+" "+fg.getID()+" "+tg.getID()+" "+relationLabel);
		} else
			environment.logError("WGM.connect missing gram "+source+" | "+
					target+" | "+fg+" | "+tg+
					" | "+from+" | "+to, null);
	}


}

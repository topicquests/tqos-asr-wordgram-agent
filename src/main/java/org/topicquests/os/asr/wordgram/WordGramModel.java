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

import com.google.common.base.Splitter;
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
	private PostgresConnectionFactory provider;

	/**
	 * 
	 */
	public WordGramModel(WordGramEnvironment env) {
		environment = env;
		graph = environment.getSqlGraph();
		provider = graph.getProvider();
		database = graph.getProvider();
		stats = environment.getStatisticsClient();
		wgCache = new LRUCache(8192);
		dictionary = environment.getDictionary();
		gramolizer = new Gramolizer(environment, this);
		gramolizer.init();

		//if (gramolizer == null)
		//gramolizer = environment.getGramolizer();
		//System.out.println("WGM- "+gramolizer);

	}
	
	
	@Override
	public IResult processString(String text, String userId, String sentenceId) {
		System.out.println("WGM.processString "+" "+text);
		IResult result = new ResultPojo();
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = processString(conn, text, userId, sentenceId, r);
            conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 
		return result;
	}
	
	@Override
	public 	IResult processString(IPostgresConnection conn, String text, String userId, String sentenceId, IResult r)
			throws Exception {
		System.out.println("WGM.ps-1 "+text);
		environment.logDebug("WordGramModel.processString "+text);
		IResult result = new ResultPojo();
		String gramId = null;
		if (text.indexOf(' ') > -1) {
			environment.logDebug("WordGramModel.processString-1 "+text);
			System.out.println("WGM.ps-2 "+text);
			gramId = gramolize(conn, text, userId, sentenceId, r);
			System.out.println("WGM.ps-3 "+gramId);

			environment.logDebug("WordGramModel.processString-2 "+gramId);
		} else {
			environment.logDebug("WordGramModel.processString-3 "+text);
			System.out.println("WGM.ps-4 "+text);
			gramId = this.addWord(conn, text, sentenceId, userId, null, r);
			environment.logDebug("WordGramModel.processString-4 "+gramId);
			System.out.println("WGM.ps-5 "+gramId);
		}
		result.setResultObject(gramId);
		return result;
		
	}
	
	String gramolize(IPostgresConnection conn, String text, String userId, String sentenceId, IResult r) throws Exception {
		environment.logDebug("WordGramModel.gramolize "+"\n"+text);
		String result = null;
		Iterable<String> ix = Splitter.on(' ')
			       .trimResults()
			       .omitEmptyStrings()
			       .split(text);
		List<String>wordIds = new ArrayList<String>();
		List<String>words = new ArrayList<String>();
		for (String w: ix) {
			if (w.endsWith("%")) {
				String x = w.substring(0, (w.length()-1));
				gramolizer.doWord(conn, x, wordIds, words, userId, sentenceId, r);
				gramolizer.doWord(conn, "%", wordIds, words, userId, sentenceId, r);
			}
			else gramolizer.doWord(conn, w, wordIds, words, userId, sentenceId, r);
		}
		environment.logDebug("WordGramModel.gramolize-1\n"+wordIds+"\n"+words);
		IWordGram g = this.newWordGram(conn, wordIds, text, userId, null, null, r);
		environment.logDebug("WordGramModel.gramolize-2\n"+g);
		if (sentenceId != null)
			g.addSentenceId(conn, sentenceId, r);
		result = g.getID();
		environment.logDebug("WordGramModel.gramolize+ "+result);
		this.wgCache.add(result, g);
		return result;
	}

	
	
	@Override
	public IResult processTopicNameString(String label, String userId, String locator) {
		IResult result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = processTopicNameString(conn, label, userId, locator, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 

		return result;
	}
	
	@Override
	public 	IResult processTopicNameString(IPostgresConnection conn, String label, String userId, String locator, IResult r)
			throws Exception {
		IResult result = processString(conn, label, userId, null, r);
		String gramId = (String)result.getResultObject();
		IWordGram g = getThisWordGramByWords(conn, gramId, r);
		g.addIsNounType(conn, r);
		return result;
		
	}


	@Override
	public IResult processWord(String word, String userId, String sentenceId) {
		IResult result = new ResultPojo();
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = processWord(conn, word, userId, sentenceId, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 
		return result;
	}

	@Override
	public 	IResult processWord(IPostgresConnection conn, String word, String userId, String sentenceId, IResult r)
			throws Exception {
		IResult result = new ResultPojo();
		String gramId = this.addWord(conn, word, sentenceId, userId, null, r);
		result.setResultObject(gramId);
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
		return id;
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
		IWordGram result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = newTerminal(conn, wordId, word, userId, sentenceId, lexType, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 

		return result;	
	}

	public IWordGram newTerminal(IPostgresConnection conn, String wordId, String word, String userId, String sentenceId, String lexType, IResult r) throws Exception {
		String gramId = wordId.trim();
		int where = gramId.indexOf(".");
		if (where < 0)
			gramId = this.singletonId(wordId);
		boolean isPunctuation = isPunctuation(word);
		IWordGram result = (ConcordanceWordGram)this.getThisWordGram(conn, gramId, r);
		if (result == null) {
			SqlVertex v = (SqlVertex)graph.addVertex(conn, gramId, word, r);
			environment.logDebug("WordGramModel.newTerminal-2 "+gramId+" "+v);
			result = new ConcordanceWordGram(v, environment);
			result.setWordGramSize(conn, 1, r);
			result.markIsNew();
			if (sentenceId != null)
				result.addSentenceId(conn, sentenceId, r);
			if (lexType != null)
				result.addLexType(conn, lexType, r);
			if (isPunctuation)
				result.addLexType(conn, ILexTypes.STOP_WORD, r);
	//TODO???		String w = "\""; //default for quote character
	//		if (!wordId.equals("0."))
	//			w = dictionary.getWord(wordId);
	//		result.setWords(w);
			
		} else {
			if (lexType != null)
				result.addLexType(conn, lexType, r);
			if (sentenceId != null)
				result.addSentenceId(conn, sentenceId, r);
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
		IWordGram result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = newWordGram(conn, wordIds, words, userId, topicLocator, lexType, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 

		return result;
	}
	
	void updateStats(int wordCount) {
		if (wordCount==1)
			stats.addToKey(IASRFields.WG1);
		else if (wordCount == 2) 
			stats.addToKey(IASRFields.WG2);
		else if (wordCount == 3) 
			stats.addToKey(IASRFields.WG3);
		else if (wordCount == 4) 
			stats.addToKey(IASRFields.WG4);
		else if (wordCount == 5) 
			stats.addToKey(IASRFields.WG5);
		else if (wordCount == 6) 
			stats.addToKey(IASRFields.WG6);
		else if (wordCount == 7) 
			stats.addToKey(IASRFields.WG7);
		else if (wordCount == 8) 
			stats.addToKey(IASRFields.WG8);
		else 
			stats.addToKey("WG"+wordCount);
	}
	
	@Override
	public 	IWordGram newWordGram(IPostgresConnection conn, List<String> wordIds, String words, String userId, String topicLocator, String lexType, IResult r)
					throws Exception {
		int wordCount = wordIds.size();
		environment.logDebug("WordGramModel.newWordGram "+wordCount+" "+wordIds+" "+words);
		String gramId = this.wordGramId(wordIds);
		IWordGram result = (ConcordanceWordGram)this.getThisWordGram(gramId);
		environment.logDebug("WordGramModel.newWordGram-1 "+gramId+" "+result);
		if (result == null) {
			SqlVertex v = (SqlVertex)graph.addVertex(conn, gramId, words, r);
			environment.logDebug("WordGramModel.newWordGram-3 "+v);
			result = new ConcordanceWordGram(v, environment);
			environment.logDebug("WordGramModel.newWordGram-4 "+wordCount+" "+result);
			result.setWordGramSize(conn, wordCount, r);
			result.markIsNew();
			environment.logDebug("WordGramModel.newWordGram-4a "+wordCount+" "+result);
			if (topicLocator != null)
				result.addTopicLocator(conn, topicLocator, r);
			if (lexType != null)
				result.addLexType(conn, lexType, r);
			environment.logDebug("WordGramModel.newWordGram-5 "+result);
			updateStats(wordCount);
		}
		environment.logDebug("WordGramModel.newWordGram+ "+result);
		return result;
	}

	
/*	@Override
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
	*/

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
	      conn.setProxyRole(r);
	      //conn.setConvRole(r);
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
	
	@Override
	public 	boolean existsWordGram(IPostgresConnection conn, String id, IResult r) {
		boolean result = (wgCache.get(id) != null);
		if (result)
			return true;
	    result = graph.vertexExists(conn, id);
		return result;
	}


	@Override	//l, null, userId, topicLocator, null
	public String addWord(String word, String sentenceId, String userId, String lexType) {
		String gramId = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            gramId = addWord(conn, word, sentenceId, userId, lexType, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 

		

		return gramId;
	}
	
	@Override
	public 	String addWord(IPostgresConnection conn, String word, String sentenceId, String userId, String lexType, IResult r)
			throws Exception {
		String wordId = "-1";
		boolean isNew = false;
		if (word == null)
			return wordId;
		String gramId = null;
		//This only tests to see if it is local; if not, returns null
		wordId = dictionary.getWordId(word);
		IWordGram g;
		IResult x = null;
		if (wordId == null) {
			//it's a new word, result = id 
			if (word.equals("\"")) {
				wordId = "0";
			} else
				x = dictionary.addWord(word);
			wordId = (String)x.getResultObject();
			isNew = ((Boolean)x.getResultObjectA()).booleanValue();
			environment.logDebug("WordGramModel.addWord-1 "+word+" "+wordId+"  "+isNew);
			g = this.newTerminal(conn, wordId, word, userId, sentenceId, null, r);
			//g.setWords(word); words already set with label
			if (isNew)
				stats.addToKey(IASRFields.WG1);
			if (sentenceId != null)
				g.addSentenceId(conn, sentenceId, r);
			if (lexType != null)
				g.addLexType(conn,lexType, r);
			gramId = (String)g.getId();
			wgCache.add(gramId, g);
			
			environment.logDebug("WordGramModel.addWord-2 "+word+" "+g.getID());
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
					g.addLexType(conn, lexType, r);

				if (sentenceId != null) {
					g.addSentenceId(conn, sentenceId, r);
				}
			} else {
				// not in cache need to put it in cache
				g = getThisWordGram(gramId);
				if (g != null) {
					environment.logDebug("WordGramModel.addWord-4 "+sentenceId+" "+g);
					if (sentenceId != null)
						g.addSentenceId(conn, sentenceId, r);
				} else {
					environment.logDebug("WordGramModel.addWord-5 "+sentenceId+" "+g);
					g = newTerminal(conn, gramId, word, userId, sentenceId, lexType, r);
				}
				wgCache.add(gramId, g);

			}
		}		
		environment.logDebug("WordGramModel.addWord+ "+gramId);
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
		environment.logDebug("WordGramModel.getThisWordGramByWords- "+phrase);
		IWordGram result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = getThisWordGramByWords(conn, phrase, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 
		return result;
	}

	@Override
	public 	IWordGram getThisWordGramByWords(IPostgresConnection conn, String phrase, IResult r)
			throws Exception {
		String words = phrase.trim();
		IWordGram result = null;
		String id;
		IResult x;
		if (words.indexOf(' ') > -1) {
			String [] wx = words.split(" ");
			StringBuilder buf = new StringBuilder();
			List<String>ids = new ArrayList<String>();
			int counter = 0;
			for (String word:wx) {
				
				x = dictionary.addWord(word.trim());
				id = (String)x.getResultObject();
				ids.add(id);
				if (counter++ > 0)
					buf.append(".");
				buf.append(id);
			}
			// always makes an id ending with NO PERIOD
			result = getThisWordGram(conn, buf.toString(), r);
			environment.logDebug("WordGramModel.getThisWordGramByWords "+buf.toString()+" "+ids+" "+result);
			if (result == null)
				result = this.addWordGram(conn, ids, words, null, "SystemUser", null, null, r);
		} else {
			id = this.getGramId(phrase);
			environment.logDebug("WordGramModel.getThisWordGramByWords-1 "+id);
			result = this.getWordGram(conn, id, r);
			if (result == null) {
				x = dictionary.addWord(words);
				id = (String)x.getResultObject();
				result = getThisWordGram(conn, this.singletonId(id), r);
			}
			if (result == null)
				result = this.newTerminal(conn, id, words, "SystemUser", null, null, r);
			environment.logDebug("WordGramModel.getThisWordGramByWords+ "+id+" "+result);
		}		
		return result;
	}


	@Override
	public IWordGram addWordGram(List<String> wordIds, String words, String sentenceId, String userId,
			String topicLocator, String lexType) {

		IWordGram result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = addWordGram(conn, wordIds, words, sentenceId, userId, topicLocator, lexType, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 		
		return result;
	}
	
	@Override
	public 	IWordGram addWordGram(IPostgresConnection conn, List<String>wordIds, String words, String sentenceId, 
				String userId, String topicLocator, String lexType, IResult r) throws Exception {
		String gramId = this.wordGramId(wordIds);
		IWordGram result = (IWordGram)wgCache.get(gramId);
		if (result == null)
			result = getThisWordGram(gramId);
//			System.out.println("ASRMa-1 "+result);
		if (result == null) {
			//is new
			result = newWordGram(conn, wordIds, words, userId, topicLocator, lexType, r);
			if (sentenceId != null) 
				result.addSentenceId(conn, sentenceId, r);
			//database.putWordGram(result);
			
		} else {
			if (sentenceId != null) {
				result.addSentenceId(conn, sentenceId, r);
			}
			if (topicLocator != null) {
				result.addTopicLocator(conn, topicLocator, r);
			}
			if (lexType != null)
				result.addLexType(conn, lexType, r);

		}
		wgCache.add(gramId, result);
		
		environment.logDebug("ADDWG "+topicLocator+" "+result.getWords());
		return result;		
	}

	@Override
	public IWordGram getWordGram(String id) {
		IWordGram g = null;
		IWordGram result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            result = getWordGram(conn, id, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 
		return g;
	}
	
	public IWordGram getWordGram(IPostgresConnection conn, String id, IResult r) throws Exception {
		IWordGram g = getThisWordGram(id);
		if (g != null && g.getRedirectToId() != null) {
			environment.logDebug("WordGramModel.getWordGram-1 "+id+" "+g);
			return getThisWordGram(conn, g.getRedirectToId(), r);
		} else {
		      SqlVertex v = (SqlVertex)graph.getVertex(id, conn, r);
		      if (v != null) {
		    	  g = new ConcordanceWordGram(v, environment);
		    	  if (g.getRedirectToId() != null)
		  			return getThisWordGram(g.getRedirectToId());
		    	  wgCache.add(id, g);
		      }			
		}
		return g;
	}


	@Override
	public IWordGram getThisWordGram(String id) {
		IWordGram g = (IWordGram)wgCache.get(id);
		if (g == null) {
		      SqlVertex v = (SqlVertex)graph.getVertex(id);
		      if (v != null) {
		    	  g = new ConcordanceWordGram(v, environment); 
		    	  wgCache.add(id, g);
		      }
		}
		return g;
	}

	public IWordGram getThisWordGram(IPostgresConnection conn, String id, IResult r) throws Exception {
		IWordGram g = (IWordGram)wgCache.get(id);
		if (g == null) {
			SqlVertex v = (SqlVertex)graph.getVertex(id, conn, r);
		      if (v != null) {
		    	  g = new ConcordanceWordGram(v, environment); 
		    	  wgCache.add(id, g);
		      }
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


	@Override
	public void connectWordGrams(String source, String target, String relationLabel, String context) {
		environment.logDebug("WGM.connectWordGrams- "+source+" "+target+" "+relationLabel);
		IWordGram result = null;
		IPostgresConnection conn = null;
	    IResult r = new ResultPojo();
        try {
        	conn = provider.getConnection();
           	conn.setProxyRole(r);
            conn.beginTransaction(r);
            connectWordGrams(conn, source, target, relationLabel, context, r);
    		conn.endTransaction(r);
        } catch (Exception e) {
        	e.printStackTrace();
        	environment.logError(e.getMessage(), e);
        } finally {
	    	conn.closeConnection(r);
        } 
	}
	
	@Override
	public 	void connectWordGrams(IPostgresConnection conn, String source, String target, String relationLabel, String context, IResult r)
			throws Exception {
		environment.logDebug("WGM.connectWordGrams- "+source+" "+target+" "+relationLabel);
		//IWordGram fg = getThisWordGramByWords(conn, source, r);
		//IWordGram tg = getThisWordGramByWords(conn, target, r);
				
		String fromId = this.getGramId(source);
		String toId = this.getGramId(target);
		environment.logDebug("WGM.connectWordGrams-1 "+fromId+" "+toId);

		if (fromId != null && toId != null) {
			SqlEdge e = graph.addEdge(conn, UUID.randomUUID().toString(), fromId, toId, relationLabel, r);
			environment.logDebug("WGM.connectWordGrams+ "+e+" "+fromId+" "+toId+" "+relationLabel);
		} else
			environment.logError("WGM.connect missing gram "+source+" | "+
					target+" | "+fromId+" | "+toId, null);
	}



}

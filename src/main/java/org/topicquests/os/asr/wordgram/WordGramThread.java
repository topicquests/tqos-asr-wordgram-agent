/**
 * 
 */
package org.topicquests.os.asr.wordgram;
import java.util.*;

import org.topicquests.os.asr.wordgram.api.IConstants;
import org.topicquests.os.asr.wordgram.api.IWordGramAgentModel;

import net.minidev.json.JSONObject;
/**
 * @author jackpark
 *
 */
public class WordGramThread {
	private WordGramEnvironment environment;
	private IWordGramAgentModel model;
	private boolean isRunning = true;
//	private List<JSONObject> wordnetTasks;
	private List<JSONObject> textTasks;
	private TextWorker textRunner;

	/**
	 * 
	 */
	public WordGramThread(WordGramEnvironment env) {
		environment = env;
		model = environment.getModel();
		//wordnetTasks = new ArrayList<JSONObject>();
		textTasks = new ArrayList<JSONObject>();
		textRunner = new TextWorker();
		textRunner.start();
	}
	
	/**
	 * Used Internally
	 * { gramId, word }
	 * @param t
	 * /
	protected
	void addWordNetTask(JSONObject t) {
		synchronized(wordnetTasks) {
			wordnetTasks.add(t);
			wordnetTasks.notify();
		}
		
	}
	
	/**
	 * This is the public API
	 * @param t
	 */
	public void addTextTask(JSONObject t) {
		environment.logDebug("DICT ADD "+t);
		String wd = t.getAsString(IConstants.THE_WORD);
		synchronized(textTasks) {
			textTasks.add(t);
			textTasks.notify();
		}
		
	}

	public void shutDown() {
		System.out.println("WordGramThread shutting down");
		synchronized(textTasks) {
			isRunning = false;
			textTasks.notify();
		}
	}

/*	class Worker extends Thread {
		
		
		public void run() {
			JSONObject jo = null;
			while (isRunning) {
				synchronized(wordnetTasks) {
					if (wordnetTasks.isEmpty()) {
						try {
							wordnetTasks.wait();
						} catch (Exception e) {}
					} else {
						jo = wordnetTasks.remove(0);
					}
				}
				if (isRunning && jo != null) {
					handleWordnetTask(jo);
					jo = null;
				}
			}
		}
	}
	
	void handleWordnetTask(JSONObject jo) {
		
	}*/
	
	
	class TextWorker extends Thread {
		
		
		public void run() {
			JSONObject jo = null;
			while (isRunning) {
				synchronized(textTasks) {
					if (textTasks.isEmpty()) {
						try {
							textTasks.wait();
						} catch (Exception e) {}
					} else {
						jo = textTasks.remove(0);
					}
				}
				if (isRunning && jo != null) {
					handleTextTask(jo);
					jo = null;
				}
			}
		}
	}
	
	void handleTextTask(JSONObject jo) {
		environment.logDebug("DICT HANDLE "+jo);
		//Build the wordgram(s)
		String word = jo.getAsString(IConstants.THE_WORD);
		String userId = jo.getAsString(IConstants.USER_ID);
		String sentenceId = jo.getAsString(IConstants.SENTENCE_ID);
		model.processString(word, userId, sentenceId);
		//model primes the WordNetThread with each new terminal
		// which may or may not feed back new words here
	}
	

}

/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.wordgram;

import org.topicquests.blueprints.pg.BlueprintsPgEnvironment;
import org.topicquests.hyperbrane.DictionaryEnvironment;
import org.topicquests.hyperbrane.api.IDictionary;
import org.topicquests.os.asr.DictionaryHttpClient;
import org.topicquests.os.asr.StatisticsHttpClient;
import org.topicquests.os.asr.api.IDictionaryClient;
import org.topicquests.os.asr.api.IDictionaryEnvironment;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.os.asr.wordgram.api.IWordGramAgentModel;
import org.topicquests.support.RootEnvironment;
import org.topicquests.support.api.IResult;

import com.tinkerpop.blueprints.impls.sql.SqlGraph;

/**
 * @author jackpark
 *
 */
public class WordGramEnvironment extends RootEnvironment {
	private IStatisticsClient statisticsClient;
	private IDictionaryClient dictionaryClient;
	private SqlGraph sqlGraph;
	private IWordGramAgentModel model;
	private IDictionaryEnvironment dictionaryEnvironment;
	private IDictionary dictionary;
	private WordGramThread wordGram;
	private BlueprintsPgEnvironment blueprints;
	private Gramolizer gramolizer;
	/**
	 * @param configPath
	 * @param logConfigPath
	 */
	public WordGramEnvironment(String configPath, String logConfigPath) {
		super(configPath, logConfigPath);
		//Graph
		blueprints = new BlueprintsPgEnvironment();
		String graphName = getStringProperty("GraphName");
		sqlGraph = blueprints.getGraph(graphName);
		//STATS
		statisticsClient = new StatisticsHttpClient(this);
 		logDebug("Environment-1 "+statisticsClient);
		//Dictionary
		dictionaryEnvironment = new DictionaryEnvironment(configPath, logConfigPath);
		dictionaryEnvironment.setStatisticsClient(statisticsClient);
		dictionaryEnvironment.createDictionaryClient();
		dictionaryEnvironment.initializeDictionary();
		dictionary = dictionaryEnvironment.getDictionary();
		dictionaryClient = dictionaryEnvironment.getDictionaryClient();
		dictionary = dictionaryEnvironment.getDictionary();
		//dictionary = new ConcordanceDictionary(this);
		model = new WordGramModel(this);
		gramolizer = new Gramolizer(this);
		//now build the threads
		
		((WordGramModel)model).setWordNetThread(gramolizer);
		gramolizer.init();
		wordGram = new WordGramThread(this);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				shutDown();
			}
		});

	}
	
	/**
	 * For debugging
	 */
	public void ping() {
		IResult r = dictionaryClient.getDictionary();
		//System.out.println("DICT "+r.getErrorString()+"\n"+r.getResultObject());
		r = statisticsClient.getStatistics();
		//System.out.println("STAT "+r.getErrorString()+"\n"+r.getResultObject());		
	}
	
	/**
	 * This is the public facing thread; it's primary API is
	 * <code>
	 * @return
	 */
	public WordGramThread getWordGramThread () {
		return wordGram;
	}
	

	public SqlGraph getSqlGraph() {
		return sqlGraph;
	}
	
	public IWordGramAgentModel getModel() {
		return model;
	}
	
	public IStatisticsClient getStatisticsClient() {
		return statisticsClient;
	}
	
	public IDictionaryClient getDictionaryClient() {
		return dictionaryClient;
	}

	public IDictionary getDictionary() {
		return dictionary;
	}

	/**
	 * If embedded in a system with the client, use this
	 * @param client
	 */
	public void setStatisticsClient(IStatisticsClient client) {
		statisticsClient = client;
	}
	public void setDictionaryClient(IDictionaryClient client) {
		dictionaryClient = client;
	}

	/**
	 * If this is a stand-alone agent, use this to fire up the client
	 */
	public void createStatisticsClient() {
		statisticsClient = new StatisticsHttpClient(this);
	}
	public void createDictionaryClient() {
		dictionaryClient = new DictionaryHttpClient(this);
	}
	
	public void shutDown() {
		System.out.println("Environment shutting down");
		this.wordGram.shutDown();
	}

}

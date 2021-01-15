/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */

package devtests;

import org.topicquests.os.asr.wordgram.WordGramEnvironment;
import org.topicquests.os.asr.wordgram.WordGramThread;
import org.topicquests.os.asr.wordgram.api.IWordGramAgentModel;

/**
 * @author jackpark
 *
 */
public class TestRoot {
	protected WordGramEnvironment environment;
	protected WordGramThread wordGramThread;
	protected IWordGramAgentModel model;

	/**
	 * 
	 */
	public TestRoot() {
		environment = new WordGramEnvironment("wordgram-props.xml", "logger.properties");
		environment.createStatisticsClient();
		model = environment.getModel();
		wordGramThread = environment.getWordGramThread();
	}

}

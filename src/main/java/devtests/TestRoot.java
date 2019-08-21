/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */

package devtests;

import org.topicquests.os.asr.wordgram.WordGramEnvironment;
import org.topicquests.os.asr.wordgram.WordGramThread;

/**
 * @author jackpark
 *
 */
public class TestRoot {
	protected WordGramEnvironment environment;
	protected WordGramThread wordGramThread;

	/**
	 * 
	 */
	public TestRoot() {
		environment = new WordGramEnvironment("wordgram-props.xml", "logger.properties");
		environment.createStatisticsClient();
		wordGramThread = environment.getWordGramThread();
	}

}

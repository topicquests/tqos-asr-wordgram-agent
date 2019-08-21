/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import org.topicquests.os.asr.wordgram.api.IConstants;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class FirstTest extends TestRoot {
	private final String 
		SINGLE = "dog",
		PAIR = "hello there",
		SENTENCE = "this is a meaningless sentence which is a way to test how well the tqos-asr-wordgram-agent appears to work",
		SENTENCE2 = "this is another meaningless sentence which is a way to test how well the tqos-asr-wordgram-agent appears to work";
		//18 words long
	
	/**
	 * 
	 */
	public FirstTest() {
		environment.logDebug("STARTING "+wordGramThread);
		JSONObject jo = new JSONObject();
		jo.put(IConstants.THE_WORD, SENTENCE2);
		jo.put(IConstants.USER_ID, "SystemUser");
		//skip sentenceId
		wordGramThread.addTextTask(jo);
		//that's it
		// user has to kill thread
	}

}

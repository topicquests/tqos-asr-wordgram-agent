/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;
import java.io.*;

import org.topicquests.os.asr.wordgram.api.IConstants;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;
/**
 * @author jackpark
 *
 */
public class BootstrapVocab extends TestRoot {
	private final String DIR = "data/vocab/en";
	/**
	 * 
	 */
	public BootstrapVocab() {
		//long stime = System.currentTimeMillis();
		File dir = new File(DIR);
		File [] files = dir.listFiles();
		File f;
		int len = files.length;
		for (int i=0; i<len; i++) {
			f = files[i];
			if (f.getName().endsWith("txt")) {
				System.out.println("PROCESSING FILE "+f.getName());
				processFile(f);
			}
		}
		//long etime = System.currentTimeMillis();
		//long delta = (etime - stime)/1000;
		//environment.logDebug("TIME "+delta);
//		environment.shutDown();
//		System.exit(0);

	}
	
	void processFile(File f) {
		try {
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader bis = new InputStreamReader(fis, "UTF-8");
			BufferedReader ir = new BufferedReader(bis);
			IResult r;
			String line;
			while ((line = ir.readLine()) != null) {
				line = line.trim();
				if (!line.startsWith("#")) {
					JSONObject jo = new JSONObject();
					jo.put(IConstants.THE_WORD, line);
					jo.put(IConstants.USER_ID, "SystemUser");
					wordGramThread.addTextTask(jo);
				}
			}
			ir.close();
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			e.printStackTrace();
		}
		
	}

}

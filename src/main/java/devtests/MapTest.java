/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import org.topicquests.ks.utils.PersistentMap;

/**
 * @author jackpark
 *
 */
public class MapTest extends TestRoot {
	private PersistentMap map;
	private final String
		PATH = "data/testmap",
		NAME = "testmap";
	/**
	 * 
	 */
	public MapTest() {
		map = new PersistentMap(PATH, NAME);
		boolean did = map.put("hello", "HelloWorld");
		System.out.println("A "+did);
		Object o = map.get("hello");
		System.out.println("B "+o);
		o = map.remove("hello");
		System.out.println("C "+o);
		o = map.get("hello");
		System.out.println("D "+o);
		map.shutDown();
		environment.shutDown();
		System.exit(0);
	}

}

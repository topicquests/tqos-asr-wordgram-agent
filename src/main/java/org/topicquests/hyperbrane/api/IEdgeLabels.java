/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane.api;

/**
 * @author jackpark
 *
 */
public interface IEdgeLabels {
	public static final String
		IS_A 		= "isA",
		HAS_SYNONYM	= "synonym",
		HAS_ANTONYM	= "antonym",
		CAUSE		= "cause";
}

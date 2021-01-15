/**
 * 
 */
package devtests;

import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class SimpleTest extends TestRoot {
	private final String
		FOO = "Major Ball Player";
	/**
	 * 
	 */
	public SimpleTest() {
		super();
		
		IResult r = model.processString(FOO, "SystemUser", null);
		System.out.println("A "+r.getErrorString());
		System.out.println("B "+r.getResultObject());
		environment.shutDown();
		System.exit(0);
	}

}

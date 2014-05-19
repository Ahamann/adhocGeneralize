package main.save;

import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * Class to save constants and objects
 * @author Bernd Grafe
 *
 */
public class Container {

	static STRtree treeSaveTest;

	public static STRtree getTreeSaveTest() {
		return treeSaveTest;
	}

	public static void setTreeSaveTest(STRtree treeSaveTest) {
		Container.treeSaveTest = treeSaveTest;
	}
}

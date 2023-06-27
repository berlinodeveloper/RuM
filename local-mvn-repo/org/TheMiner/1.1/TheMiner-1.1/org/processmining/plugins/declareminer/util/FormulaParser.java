package org.processmining.plugins.declareminer.util;

import java.util.Vector;

public class FormulaParser {

	public static String[] getParameters(String formulaVerbose) {
		String[] array = formulaVerbose.split("<p>");
		Vector<String> temp = new Vector<String>();
		for (int j = 0; j < array.length; j++) {
			if (array[j].startsWith(" parameter")) {
				temp.addElement(array[j]);
			}
		}
		int diff = array.length - temp.size();
		String[] result = new String[temp.size()];
		if (diff == 3) {
			for (int i = 2; i < (array.length - 1); i++) {
				result[i - 2] = array[i].split("->")[1].split("</p>")[0];
			}
		}
		if ((array.length - temp.size()) == 2) {
			for (int i = 1; i <= temp.size(); i++) {
				result[i - 1] = array[i].split("->")[1].split("</p>")[0];
			}
		}
		return result;
	}

	public static String getTemplateName(String formulaVerbose) {
		String[] array = formulaVerbose.split("<h2>");
		String temp = array[1];
		array = temp.split("</h2>");
		String result = array[0];
		return result;
	}
}
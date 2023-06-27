package org.processmining.plugins.declareminer.util;

import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.util.Pair;

public class DeclareTemplatesHelper {

	public static final Set<String> allTemplates = new HashSet<String>();
	static {
		allTemplates.add("precedences");
		allTemplates.add("responses");
	//	allTemplates.add("notResponses");
		allTemplates.add("successions");
		allTemplates.add("notCoExistences");
		allTemplates.add("respondedExistences");
		allTemplates.add("notSuccessions");
		allTemplates.add("coExistences");
		allTemplates.add("chainResponses");
		allTemplates.add("chainPrecedence");
		allTemplates.add("altResponse");
		allTemplates.add("altPrecedence");
	}
	
	public static Set<Pair<String, String>> combineActivities(String... activities) {
		Set<Pair<String, String>> constraintToPrint = new HashSet<Pair<String, String>>();
		 for (String a1 : activities) {
			 for (String a2 : activities) {
				 constraintToPrint.add(new Pair<String, String>(a1, a2));
			 }
		 }
		 return constraintToPrint;
	}
}

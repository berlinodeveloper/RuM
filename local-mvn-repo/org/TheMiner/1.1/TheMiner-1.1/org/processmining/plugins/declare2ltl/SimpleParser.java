package org.processmining.plugins.declare2ltl;

import java.util.Collection;

import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;


/**
 * <p>Title: DECLARE</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Maja Pesic
 * @version 1.0
 */
public class SimpleParser {


  public SimpleParser() {
    super();
  }

 	protected String replaceParameterDefaultWithActivities(String formula,
			Parameter parameter, Collection<ActivityDefinition> realBranches) {
		String msg = new String(formula);
		String real = "";
		for (ActivityDefinition branch : realBranches) {
			if (!real.equals("")) {
				real += " \\\\/ ";
			}
			Event e = new Event(null, branch, Event.Type.COMPLETED);
			real += "\"" + e.getProposition() + "\"";
		}
		msg = msg.replaceAll("\"" + parameter.getName() + "\"", "(" + real + ")");
		return msg;
	}

  /**
   *
   * @param s String
   * @return IProposition
   */
 /* protected synchronized IProposition proposition(String s) {
    return FormulaFactory.proposition( s);
  }*/
}

package org.processmining.plugins.declare2ltl;

import java.io.PrintStream;

import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
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
public class ConstraintCollection
    extends AbstractCollection {
  public ConstraintCollection(PrintStream out) {
    super(new ConstraintParser(), out);
  }

  void add(ConstraintDefinition constraint) {
    lines.add("");
    lines.add(SEPARATOR);
    lines.add("");
    lines.add(header(constraint));
    ConstraintTemplate template = constraint;
    lines.add("{");
    lines.add("    <h2>" + template.getDisplay() + "</h2>"); // add display
    if (!template.getDescription().equals("")) {
      lines.add("    <p> " + template.getDescription() + "</p>"); // add description
    }
    // add parameters to the formula
    for (Parameter parameter: constraint.getParameters()) {
      String brn = "";
      // collect branches
      for (ActivityDefinition branch: constraint.getBranches(parameter)) {
        if (!brn.equals("")) {
          brn += " or ";
        }
        //brn += branch.getActivityDefinition().getName();
    	if((!branch.getName().contains("-assign")&&!branch.getName().contains("-ate_abort")&&!branch.getName().contains("-suspend")&&!branch.getName().contains("-complete")&&!branch.getName().contains("-autoskip")&&!branch.getName().contains("-manualskip")&&!branch.getName().contains("pi_abort")&&!branch.getName().contains("-reassign")&&!branch.getName().contains("-resume")&&!branch.getName().contains("-schedule")&&!branch.getName().contains("-start")&&!branch.getName().contains("-unknown")&&!branch.getName().contains("-withdraw"))&&(!branch.getName().contains("<center>assign")&&!branch.getName().contains("<center>ate_abort")&&!branch.getName().contains("<center>suspend")&&!branch.getName().contains("<center>complete")&&!branch.getName().contains("<center>autoskip")&&!branch.getName().contains("<center>manualskip")&&!branch.getName().contains("<center>pi_abort")&&!branch.getName().contains("<center>reassign")&&!branch.getName().contains("<center>resume")&&!branch.getName().contains("<center>schedule")&&!branch.getName().contains("<center>start")&&!branch.getName().contains("<center>unknown")&&!branch.getName().contains("<center>withdraw"))){
    		brn += branch.getName().replace(".", "_").replace("/", "_").replace("(", "_").replace(")", "_");	
    	}else{
    		branch.setName(branch.getName().replace("-"+branch.getName().split("-")[branch.getName().split("-").length-1], ""));
    		brn += branch.getName().replace(".", "_").replace("/", "_").replace("(", "_").replace(")", "_");
    	}
        
      }
      lines.add("    <p> parameter(s) [" + parameter.getName() + "] ->" + brn + "</p>");
    }
    lines.add("<p> type: "+(constraint.getMandatory()?"mandatory":"optional")+"</p>");
    lines.add("}   ");
    lines.add(" " + constraintFormula(constraint) + " ;");
  }

  private String header(ConstraintDefinition constraint) {
    String parameters = "";
    for (Parameter parameter: constraint.getParameters()) {
      String brn = "";
      for (ActivityDefinition branch: constraint.getBranches(parameter)) {
        if (!brn.equals("")) {
          brn += "_";
        }
        //brn += branch.getActivityDefinition().getName().replaceAll(" ", "");
        if((!branch.getName().contains("-assign")&&!branch.getName().contains("-ate_abort")&&!branch.getName().contains("-suspend")&&!branch.getName().contains("-complete")&&!branch.getName().contains("-autoskip")&&!branch.getName().contains("-manualskip")&&!branch.getName().contains("pi_abort")&&!branch.getName().contains("-reassign")&&!branch.getName().contains("-resume")&&!branch.getName().contains("-schedule")&&!branch.getName().contains("-start")&&!branch.getName().contains("-unknown")&&!branch.getName().contains("-withdraw"))&&(!branch.getName().contains("<center>assign")&&!branch.getName().contains("<center>ate_abort")&&!branch.getName().contains("<center>suspend")&&!branch.getName().contains("<center>complete")&&!branch.getName().contains("<center>autoskip")&&!branch.getName().contains("<center>manualskip")&&!branch.getName().contains("<center>pi_abort")&&!branch.getName().contains("<center>reassign")&&!branch.getName().contains("<center>resume")&&!branch.getName().contains("<center>schedule")&&!branch.getName().contains("<center>start")&&!branch.getName().contains("<center>unknown")&&!branch.getName().contains("<center>withdraw"))){
        	brn += branch.getName().replaceAll(" ", "").replace(".", "_").replace("/", "_").replace("(", "_").replace(")", "_");
    	}else{
    		branch.setName(branch.getName().replace("-"+branch.getName().split("-")[branch.getName().split("-").length-1], ""));
    		brn += branch.getName().replaceAll(" ", "").replace(".", "_").replace("/", "_").replace("(", "_").replace(")", "_");
    	}
        
      }
      if (!parameters.equals("")) {
        parameters += "_";
      }
      parameters += brn;
    }
    return "formula " +
        constraint.getName().replaceAll(" ", "_") +
        "_" + parameters + " () :=";
  }

  private String constraintFormula(ConstraintDefinition constraint) {
    String result = "";
    try {
      result =  ( (ConstraintParser) parser).parse(constraint);
      
      result = formula(result);
    }
    catch (Exception ex) {
      //ignore
    }
    return result;
  }

  /*protected IProposition activity(Proposition proposition) {
    return new Proposition(WorkflowModelElement + " " + EQUALS + " \"" + proposition.getActivity() +
        "\"");
  }*/
}

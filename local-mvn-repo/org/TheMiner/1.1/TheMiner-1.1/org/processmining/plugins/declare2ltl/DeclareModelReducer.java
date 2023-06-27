/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.plugins.declare2ltl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.Parameter;

@Plugin(name = "Declare Model Reducer", parameterLabels = { "Declare Model" }, returnLabels = { "Reduced Declare Model" }, returnTypes = { DeclareMinerOutput.class }, userAccessible = true)
public class DeclareModelReducer {
	Progress prog;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Declare Model Reducer")
	public DeclareMinerOutput analyse(UIPluginContext context, DeclareMap model) {
		prog = context.getProgress();
		prog.setMinimum(0);
		prog.setMaximum(112);
		prog.setIndeterminate(false);
		prog.setValue(50);

		int constraintsNumber = 90;

		AssignmentModel assModel = (AssignmentModel)model.getModel().clone();
		AssignmentModelView assModelView = model.getView();


		DeclareMap output = new DeclareMap(assModel, null, assModelView, null, null, null);

		DeclareMinerOutput out = new DeclareMinerOutput();
		
		out.setModel(output);

		int countConstraints = assModel.constraintDefinitionsCount();

		while(countConstraints > constraintsNumber){

			Random rand = new Random();
			int index = rand.nextInt(assModel.getConstraintDefinitions().size()+1);

			System.out.println(index);
			
			HashMap<String, Integer> activityNumbers = new HashMap<String, Integer>();


			for(ConstraintDefinition cd : assModel.getConstraintDefinitions()){
				for(Parameter parameter : cd.getParameters()){
					ActivityDefinition activity = cd.getBranches(parameter).iterator().next();
					if(activityNumbers.containsKey(activity.getName())){
						int temp = activityNumbers.get(activity.getName());
						temp++;
						activityNumbers.put(activity.getName(),temp);
					}else{
						activityNumbers.put(activity.getName(),1);
					}
				}
			}

			Iterator<ConstraintDefinition> iterator = assModel.getConstraintDefinitions().iterator();
			
			if(assModel.constraintDefinitionAt(index)!=null){

				boolean canRemove = true;

				for(Parameter parameter : assModel.constraintDefinitionAt(index).getParameters()){
					ActivityDefinition activity = assModel.constraintDefinitionAt(index).getBranches(parameter).iterator().next();
					if(activityNumbers.get(activity.getName())==1){
						canRemove = false;
					}
				}
				if(canRemove){
					for(Parameter parameter : assModel.constraintDefinitionAt(index).getParameters()){
						ActivityDefinition activity = assModel.constraintDefinitionAt(index).getBranches(parameter).iterator().next();
						int temp = activityNumbers.get(activity.getName());
						temp--;
						activityNumbers.put(activity.getName(),temp);
					}
					assModel.deleteConstraintDefinition(assModel.constraintDefinitionAt(index));
					countConstraints --;
				}
			}

		}

		prog.setValue(100);
		return out;
	}

}

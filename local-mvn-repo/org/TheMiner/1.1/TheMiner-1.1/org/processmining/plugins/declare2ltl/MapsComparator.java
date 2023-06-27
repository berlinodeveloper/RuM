/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.plugins.declare2ltl;

import java.util.Collection;
import java.util.Vector;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.Parameter;

@Plugin(name = "MapsComparator", parameterLabels = { "Traditional", "Apriori" }, returnLabels = {"Maps Comparison"}, returnTypes = {Vector.class}, userAccessible = true)
public class MapsComparator {
	Progress prog;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0,1}, variantLabel = "Maps Comparator")
	public Vector<String> analyse(UIPluginContext context, DeclareMap model1, DeclareMap model2) {
		prog = context.getProgress();
		prog.setMinimum(0);
		prog.setMaximum(112);
		prog.setIndeterminate(false);
		prog.setValue(50);
		Collection<ConstraintDefinition> cds1 = model1.getModel().getConstraintDefinitions();
		Collection<ConstraintDefinition> cds2 = model2.getModel().getConstraintDefinitions();
		
		System.out.println("No. Constraints in First Model: "+cds1.size());
		System.out.println("No. Constraints in Second Model: "+cds2.size());
		Vector<String> output = new Vector<String>();
		Vector<String> output1 = new Vector<String>();
		Vector<String> output2 = new Vector<String>();
		Vector<String> respondedExistenceMap1 = new Vector<String>();
		for(ConstraintDefinition cd : cds1){	
			String oldParam = "";
			int count = 0;
			String constr = cd.getName()+"(";
			Parameter p1 = null;
			for(Parameter p : cd.getParameters()){
				p1=p;
				if(count==0){
					oldParam = cd.getBranches(p).iterator().next().toString();
				}
				count ++;
				constr = constr+cd.getBranches(p).iterator().next()+", ";
			}

			constr = constr+"&;";
			constr= constr.replaceAll(", &;", "")+ ")";
			constr = constr.replaceAll("-complete", "");
			output1.add(constr);
			
			if(cd.getName().contains("responded existence")){
				respondedExistenceMap1.add(constr);
			}
			if(cd.getName().contains("co-existence")||cd.getName().equals("exclusive choice")||cd.getName().equals("choice")){
				output1.add( (cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
		}
		Vector<String> succession = new Vector<String>();
		Vector<String> alternate_Succession = new Vector<String>();
		Vector<String> chain_Succession = new Vector<String>();
		Vector<String> choice = new Vector<String>();
		Vector<String> exclusive_Choice = new Vector<String>();
		Vector<String> existence = new Vector<String>();
		Vector<String> existence2 = new Vector<String>();
		Vector<String> existence3 = new Vector<String>();
		Vector<String> init = new Vector<String>();
		Vector<String> absence = new Vector<String>();
		Vector<String> absence2 = new Vector<String>();
		Vector<String> absence3 = new Vector<String>();
		Vector<String> exactly1 = new Vector<String>();
		Vector<String> exactly2 = new Vector<String>();
		Vector<String> responded_Existence = new Vector<String>();
		Vector<String> response = new Vector<String>();
		Vector<String> alternate_Response = new Vector<String>();
		Vector<String> chain_Response = new Vector<String>();
		Vector<String> precedence = new Vector<String>();
		Vector<String> alternate_Precedence = new Vector<String>();
		Vector<String> chain_Precedence = new Vector<String>();
		Vector<String> coExistence = new Vector<String>();
		Vector<String> not_CoExistence = new Vector<String>();
		Vector<String> not_Succession = new Vector<String>();
		Vector<String> not_Chain_Succession = new Vector<String>();
		for(ConstraintDefinition cd : cds2){	
			String oldParam = "";
			int count = 0;
			String constr = cd.getName()+"(";
			Parameter p1 = null;
			for(Parameter p : cd.getParameters()){
				p1=p;
				if(count==0){
					oldParam = cd.getBranches(p).iterator().next().toString();
				}
				count ++;
				constr = constr+cd.getBranches(p).iterator().next()+", ";
			}
			constr = constr+"&;";
			constr= constr.replaceAll(", &;", "")+ ")";
			constr = constr.replaceAll("-complete", "");
			output2.add(constr);
			if(cd.getName().contains("co-existence")||cd.getName().equals("exclusive choice")||cd.getName().equals("choice")){
				output2.add( (cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("succession")){
				succession.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("alternate succession")){
				alternate_Succession.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("chain succession")){
				chain_Succession.add( (cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("choice")){
				choice.add( (cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("exclusive choice")){
				exclusive_Choice.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("existence")){
				existence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("existence2")){
				existence2.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("existence3")){
				existence3.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("init")){
				init.add( (cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("absence")){
				absence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("absence2")){
				absence2.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("absence3")){
				absence3.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("exactly1")){
				exactly1.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("exactly2")){
				exactly2.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("responded existence")){
				responded_Existence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("response")){
				response.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("alternate response")){
				alternate_Response.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("chain response")){
				chain_Response.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("precedence")){
				precedence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("alternate precedence")){
				alternate_Precedence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("chain precedence")){
				chain_Precedence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("co-existence")){
				coExistence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("not co-existence")){
				not_CoExistence.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("not succession")){
				not_Succession.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
			if(cd.getName().equals("not chain succession")){
				not_Chain_Succession.add(( cd.getName()+"("+cd.getBranches(p1).iterator().next()+", "+oldParam+ ")").replaceAll("-complete", ""));
			}
		}
		
		System.out.println("RE Map1 Size: "+respondedExistenceMap1.size());
		System.out.println("RE Map2 Size: "+responded_Existence.size());
		
		if(output1.containsAll(output2) && output2.containsAll(output1)){
			prog.setValue(100);
			if(chain_Succession.size()>chain_Precedence.size()){
				output.add("more chain succession than chain precedence!");
			}
			if(chain_Succession.size()>chain_Response.size()){
				output.add("more chain succession than chain response!");
			}
			if(chain_Succession.size()>alternate_Succession.size()){
				output.add("more chain succession than alternate succession!");
			}
			if(chain_Succession.size()>alternate_Response.size()){
				output.add("more chain succession than alternate response!");
			}
			if(chain_Succession.size()>alternate_Precedence.size()){
				output.add("more chain succession than alternate precedence!");
			}
			if(chain_Succession.size()>response.size()){
				output.add("more chain succession than response!");
			}
			if(chain_Succession.size()>precedence.size()){
				output.add("more chain succession than precedence!");
			}
			if(chain_Succession.size()>succession.size()){
				output.add("more chain succession than succession!");
			}
			if(chain_Succession.size()>(coExistence.size()*2)){
				output.add("more chain succession than co-existence!");
			}
			if(chain_Succession.size()>responded_Existence.size()){
				output.add("more chain succession than responded existence!");
			}
			
			
			
			
			
			if(alternate_Succession.size()>alternate_Response.size()){
				output.add("more alternate succession than alternate response!");
			}
			if(alternate_Succession.size()>alternate_Precedence.size()){
				output.add("more alternate succession than alternate precedence!");
			}
			if(alternate_Succession.size()>response.size()){
				output.add("more alternate succession than response!");
			}
			if(alternate_Succession.size()>precedence.size()){
				output.add("more alternate succession than precedence!");
			}
			if(alternate_Succession.size()>succession.size()){
				output.add("more alternate succession than succession!");
			}
			if(alternate_Succession.size()>(coExistence.size()*2)){
				output.add("more alternate succession than co-existence!");
			}
			if(alternate_Succession.size()>responded_Existence.size()){
				output.add("more alternate succession than responded existence!");
			}
			
			
			
			if(succession.size()>response.size()){
				output.add("more succession than response!");
			}
			if(succession.size()>precedence.size()){
				output.add("more succession than precedence!");
			}
			if(succession.size()>(coExistence.size()*2)){
				output.add("more succession than co-existence!");
			}
			if(succession.size()>responded_Existence.size()){
				output.add("more succession than responded existence!");
			}
			
			
			
			if(coExistence.size()*2>responded_Existence.size()){
				output.add("more co-existence than responded existence!");
			}
			
			
			if(chain_Response.size()>alternate_Response.size()){
				output.add("more chain response than alternate response!");
			}
			
			if(chain_Response.size()>response.size()){
				output.add("more chain response than response!");
			}
			if(chain_Response.size()>responded_Existence.size()){
				output.add("more chain response than responded existence!");
			}
			
			
			if(chain_Precedence.size()>alternate_Precedence.size()){
				output.add("more chain precedence than alternate precedence!");
			}
			
			if(chain_Precedence.size()>precedence.size()){
				output.add("more chain precedence than precedence!");
			}
			if(chain_Precedence.size()>responded_Existence.size()){
				output.add("more chain precedence than responded existence!");
			}
			
			
			if(alternate_Response.size()>response.size()){
				output.add("more alternate response than response!");
			}
			if(alternate_Response.size()>responded_Existence.size()){
				output.add("more alternate response than responded existence!");
			}
			
			
			if(alternate_Precedence.size()>precedence.size()){
				output.add("more alternate precedence than precedence!");
			}
			if(alternate_Precedence.size()>responded_Existence.size()){
				output.add("more alternate precedence than responded existence!");
			}
			
			if(response.size()>responded_Existence.size()){
				output.add("more response than responded existence!");
			}
			
			
			if(precedence.size()>responded_Existence.size()){
				output.add("more precedence than responded existence!");
			}
			
			
			if(exclusive_Choice.size()*2>choice.size()*2){
				output.add("more exclusive choice than choice!");
			}
			
			if(exclusive_Choice.size()*2>not_CoExistence.size()*2){
				output.add("more exclusive choice than not co-existence!");
			}
			
			if(not_CoExistence.size()*2>not_Succession.size()){
				output.add("more not co-existence than not succession!");
			}
			
			if(not_Succession.size()>not_Chain_Succession.size()){
				output.add("more not succession than not chain succession!");
			}
			
			if(existence2.size()>existence.size()){
				output.add("more existence2 than existence!");
			}
			
			if(existence3.size()>existence2.size()){
				output.add("more existence3 than existence2!");
			}
			
			if(existence3.size()>existence.size()){
				output.add("more existence3 than existence!");
			}
			
			if(absence.size()>absence2.size()){
				output.add("more absence than absence2!");
			}
			
			if(absence2.size()>absence3.size()){
				output.add("more absence2 than absence3!");
			}
			
			if(absence.size()>absence3.size()){
				output.add("more absence than absence3!");
			}
			
			if(exactly1.size()>existence.size()){
				output.add("more exactly1 than existence!");
			}
			
			if(exactly2.size()>existence2.size()){
				output.add("more exactly2 than existence2!");
			}	
			
			if(exactly1.size()>absence2.size()){
				output.add("more exactly1 than absence2!");
			}
			
			if(exactly1.size()>absence3.size()){
				output.add("more exactly1 than absence3!");
			}
			
			if(exactly2.size()>absence3.size()){
				output.add("more exactly2 than absence3!");
			}
			
			return output;
		}else{
			if(!output2.containsAll(output1)){
				Vector<String> extraincds1 = new Vector<String>();
				extraincds1.addAll(output1);
				extraincds1.removeAll(output2);
				for(String constr : extraincds1){
					output.add("the first input model contains "+constr+" not contained in the second input model");
				}
			}
			if(!output1.containsAll(output2)){
				Vector<String> extraincds2 = new Vector<String>();
				extraincds2.addAll(output2);
				extraincds2.removeAll(output1);
				for(String constr : extraincds2){
					output.add("the second input model contains "+constr+" not contained in the first input model");
				}
			}
			prog.setValue(100);
			return output;
		}
	}
}

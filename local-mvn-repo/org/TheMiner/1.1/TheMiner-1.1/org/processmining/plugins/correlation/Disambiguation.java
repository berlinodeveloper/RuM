package org.processmining.plugins.correlation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;

public class Disambiguation {

	//private ExtendedTrace ext;



	public Vector<Integer> getTargetsCorrespondingToActivationsAfterDisambiguation(ExtendedTrace ext ,ConstraintDefinition constraintDefinition, Integer activationPosition, String candidateCorrelation, HashMap<String,ExtendedEvent>  extEvents){
		Vector<Integer> correspAfterDisambiguation = new Vector<Integer>();
		List<String> parameters = new ArrayList<String>();

		for(Parameter parameter : constraintDefinition.getParameters()){
			if(constraintDefinition.getBranches(parameter).iterator().hasNext()){
				String activityName = constraintDefinition.getBranches(parameter).iterator().next().getName();
				if((activityName.contains("-assign")||activityName.contains("-ate_abort")||activityName.contains("-suspend")||activityName.contains("-complete")||activityName.contains("-autoskip")||activityName.contains("-manualskip")||activityName.contains("pi_abort")||activityName.contains("-reassign")||activityName.contains("-resume")||activityName.contains("-schedule")||activityName.contains("-start")||activityName.contains("-unknown")||activityName.contains("-withdraw"))){
					String[] splittedName = constraintDefinition.getBranches(parameter).iterator().next().getName().split("-");
					activityName = splittedName[0];
					for(int i = 1; i<splittedName.length-1; i++){
						activityName = activityName + "-" + splittedName[i];
					}	
				}
				parameters.add(activityName);

			}else{
				parameters.add("EMPTY_PARAM");
			}
		}
		if(ext.getCorrespcorrel().get(activationPosition)!=null){
			for(int j = 0; j<ext.getCorrespcorrel().get(activationPosition).size(); j++){
				if(candidateCorrelation.equals("conservative")){
					correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
				}else{
					XEvent event1 = null;
					XEvent event2 = null;
					Integer eveAct = activationPosition;
					Integer eveTarg = ext.getCorrespcorrel().get(activationPosition).get(j);
					//if(XConceptExtension.instance().extractName(ext.getTrace().get(eveAct)).equals(parameters.get(0)) &&
					//		XConceptExtension.instance().extractName(ext.getTrace().get(eveTarg)).equals(parameters.get(1))){
					event1 = ext.getTrace().get(eveAct);
					event2 = ext.getTrace().get(eveTarg);
					//	}else{
					//	event1 = ext.getTrace().get(eveTarg);
					//	event2 = ext.getTrace().get(eveAct);
					//	}
					String type = extEvents.get(XConceptExtension.instance().extractName(event1)).getAttributeTypes().get(candidateCorrelation.split(";")[0]);
					String attr1 = candidateCorrelation.split(";")[0];
					String attr2 = candidateCorrelation.split(";")[1];
					if (type.equals("Float")){
						try{
							Float num1 = new Float(event1.getAttributes().get(attr1).toString());
							Float num2 = new Float(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("=") && num1.floatValue()==num2.floatValue()){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals("<=") && num1.floatValue()<=num2.floatValue()){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals(">=") && num1.floatValue()>=num2.floatValue()){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("!=") && num1.floatValue()!=num2.floatValue()){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}catch(NumberFormatException e){


						}
					}else if (type.equals("Byte")){
						try{
							byte num1 = new Byte(event1.getAttributes().get(attr1).toString());
							byte num2 = new Byte(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}catch(NumberFormatException e){


						}
					}else if (type.equals("Double")){
						try{
							Double num1 = new Double(event1.getAttributes().get(attr1).toString());
							Double num2 = new Double(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}catch(NumberFormatException e){


						}
					}else if (type.equals("Integer")){
						try{
							Integer num1 = new Integer(event1.getAttributes().get(attr1).toString());
							Integer num2 = new Integer(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}catch(NumberFormatException e){


						}
					}else if (type.equals("Long")){
						try{
							Long num1 = new Long(event1.getAttributes().get(attr1).toString());
							Long num2 = new Long(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}catch(NumberFormatException e){


						}
					}else if (type.equals("Short")){
						try{
							Short num1 = new Short(event1.getAttributes().get(attr1).toString());
							Short num2 = new Short(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}catch(NumberFormatException e){


						}
					}else if (type.equals("Boolean")){
						boolean num1 = new Boolean(event1.getAttributes().get(attr1).toString());
						boolean num2 = new Boolean(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("b=") && num1==num2){
							correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						}
						if(candidateCorrelation.split(";")[2].equals("b!=") && num1!=num2){
							correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						}
					}else if (type.equals("String")){
						if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){

							long num1 = XTimeExtension.instance().extractTimestamp(event1).getTime();
							long num2 = XTimeExtension.instance().extractTimestamp(event2).getTime();
							long timeDiff = num2 - num1;
							if(timeDiff<0){
								timeDiff = 0 - timeDiff;
							}
							double avg = new Double(candidateCorrelation.split(";")[3]);
							double stddev = new Double(candidateCorrelation.split(";")[4]);
							if(candidateCorrelation.split(";")[2].equals("singlestddev")){
								if(timeDiff<= avg+stddev){
									correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
								}
							}
							if(candidateCorrelation.split(";")[2].equals("doublestddev")){
								if(timeDiff<= avg+2*stddev){
									correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
								}
							}
							//					if(candidateCorrelation.split(";")[2].equals("d=") && num1==num2){
							//						correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							//					}
							//					if( candidateCorrelation.split(";")[2].equals("d<=") && num1<=num2){
							//						correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							//					}
							//					if( candidateCorrelation.split(";")[2].equals("d>=") && num1>=num2){
							//						correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							//					}
							//					if(candidateCorrelation.split(";")[2].equals("d!=") && num1!=num2){
							//						correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							//					}

						}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
							String num1 = new String(event1.getAttributes().get(attr1).toString());
							String num2 = new String(event2.getAttributes().get(attr2).toString());
							if(candidateCorrelation.split(";")[2].equals("s=") && num1.equals(num2)){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
							if(candidateCorrelation.split(";")[2].equals("s!=") && !num1.equals(num2)){
								correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							}
						}
					}
				}
			}
		}
		return correspAfterDisambiguation;
	}


	public double getDegreeofDisambiguation(Vector<ExtendedTrace> tracesWithCorrespondingEvents, ConstraintDefinition constraintDefinition, String candidateCorrelation, HashMap<String,ExtendedEvent>  extEvents){
		double degreeOfDisambiguationSupp = 0;
		double actNum = 0;
		for(ExtendedTrace ext : tracesWithCorrespondingEvents){
			Vector<Integer> correspAfterDisambiguation;
			for(int i=0; i<ext.getNonambi().size(); i++){

				if(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size()==1){ 
					//degreeOfDisambiguationSupp ++;
				}else{
					actNum ++;
					Disambiguation disambiguator = new Disambiguation();
					correspAfterDisambiguation = disambiguator.getTargetsCorrespondingToActivationsAfterDisambiguation(ext, constraintDefinition, ext.getNonambi().get(i), candidateCorrelation, extEvents);
					if(correspAfterDisambiguation.size()==1){
						degreeOfDisambiguationSupp ++;
					}
				}
			}
		}
		return degreeOfDisambiguationSupp/actNum;
	}


	public double getDegreeofDisambiguationDisjunction(Vector<ExtendedTrace> tracesWithCorrespondingEvents, ConstraintDefinition constraintDefinition, Vector<String> candidateCorrelation, HashMap<String,ExtendedEvent>  extEvents){
		double degreeOfDisambiguationSupp = 0;
		double actNum = 0;
		for(ExtendedTrace ext : tracesWithCorrespondingEvents){
			Vector<Integer> correspAfterDisambiguation = null;
			for(int i=0; i<ext.getNonambi().size(); i++){

				if(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size()==1){ 
					//degreeOfDisambiguationSupp ++;
				}else{
					actNum ++;
					Disambiguation disambiguator = new Disambiguation();
					boolean oneDisambiguate = false;
					for(String correlation : candidateCorrelation){
						correspAfterDisambiguation = disambiguator.getTargetsCorrespondingToActivationsAfterDisambiguation(ext, constraintDefinition, ext.getNonambi().get(i), correlation,extEvents);
						if(correspAfterDisambiguation.size()==1){
							oneDisambiguate = true;
						}
					}
					if(oneDisambiguate){
						degreeOfDisambiguationSupp ++;
					}
				}
			}
		}
		return degreeOfDisambiguationSupp/actNum;
	}

	public double getDegreeofDisambiguationConjunction(Vector<ExtendedTrace> tracesWithCorrespondingEvents, ConstraintDefinition constraintDefinition, Vector<String> candidateCorrelation, HashMap<String,ExtendedEvent>  extEvents){
		double degreeOfDisambiguationSupp = 0;
		double actNum = 0;
		for(ExtendedTrace ext : tracesWithCorrespondingEvents){
			Vector<Integer> correspAfterDisambiguation = null;
			for(int i=0; i<ext.getNonambi().size(); i++){

				if(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size()==1){ 
					//degreeOfDisambiguationSupp ++;
				}else{
					actNum ++;
					Disambiguation disambiguator = new Disambiguation();
					boolean allDisambiguate = true;
					for(String correlation : candidateCorrelation){
						correspAfterDisambiguation = disambiguator.getTargetsCorrespondingToActivationsAfterDisambiguation(ext, constraintDefinition, ext.getNonambi().get(i), correlation,extEvents);
						if(correspAfterDisambiguation.size()!=1){
							allDisambiguate = false;
						}
					}
					if(allDisambiguate){
						degreeOfDisambiguationSupp ++;
					}
				}
			}
		}
		return degreeOfDisambiguationSupp/actNum;
	}

	public HashMap<Integer, Vector<Integer>> getCorrelatedEventsAfterDisambiguation(ExtendedTrace traceWithAmbiguousActivations, ConstraintDefinition constraintDefinition, String candidateCorrelation, HashMap<String,ExtendedEvent>  extEvents){
		HashMap<Integer,Vector<Integer>> correlatedEvents = new HashMap<Integer, Vector<Integer>>();
		Vector<Integer> correspAfterDisambiguation;
		for(int i=0; i<traceWithAmbiguousActivations.getNonambi().size(); i++){
			Disambiguation disambiguator = new Disambiguation();
			correspAfterDisambiguation = disambiguator.getTargetsCorrespondingToActivationsAfterDisambiguation(traceWithAmbiguousActivations,constraintDefinition, traceWithAmbiguousActivations.getNonambi().get(i), candidateCorrelation, extEvents);
			correlatedEvents.put(traceWithAmbiguousActivations.getNonambi().get(i), correspAfterDisambiguation);
		}
		return correlatedEvents;
	}




	public Vector<Integer> getTargetsCorrespondingToActivationsAfterDisambiguationForDisjunctiveCorrelation(ExtendedTrace ext ,ConstraintDefinition constraintDefinition, Integer activationPosition, HashMap<String,ExtendedEvent>  extEvents, Vector<String> disjunctiveCorrelation){
		Vector<Integer> correspAfterDisambiguation = new Vector<Integer>();
		List<String> parameters = new ArrayList<String>();

		for(Parameter parameter : constraintDefinition.getParameters()){
			if(constraintDefinition.getBranches(parameter).iterator().hasNext()){
				String activityName = constraintDefinition.getBranches(parameter).iterator().next().getName();
				if((activityName.contains("-assign")||activityName.contains("-ate_abort")||activityName.contains("-suspend")||activityName.contains("-complete")||activityName.contains("-autoskip")||activityName.contains("-manualskip")||activityName.contains("pi_abort")||activityName.contains("-reassign")||activityName.contains("-resume")||activityName.contains("-schedule")||activityName.contains("-start")||activityName.contains("-unknown")||activityName.contains("-withdraw"))){
					String[] splittedName = constraintDefinition.getBranches(parameter).iterator().next().getName().split("-");
					activityName = splittedName[0];
					for(int i = 1; i<splittedName.length-1; i++){
						activityName = activityName + "-" + splittedName[i];
					}	
				}
				parameters.add(activityName);

			}else{
				parameters.add("EMPTY_PARAM");
			}
		}



		for(int j = 0; j<ext.getCorrespcorrel().get(activationPosition).size(); j++){
			boolean foundAtLeastOne = false;
			for(String candidateCorrelation : disjunctiveCorrelation){
				XEvent event1 = null;
				XEvent event2 = null;
				Integer eveAct = activationPosition;
				Integer eveTarg = ext.getCorrespcorrel().get(activationPosition).get(j);
				//if(XConceptExtension.instance().extractName(ext.getTrace().get(eveAct)).equals(parameters.get(0)) &&
				//		XConceptExtension.instance().extractName(ext.getTrace().get(eveTarg)).equals(parameters.get(1))){
				event1 = ext.getTrace().get(eveAct);
				event2 = ext.getTrace().get(eveTarg);
				//	}else{
				//	event1 = ext.getTrace().get(eveTarg);
				//	event2 = ext.getTrace().get(eveAct);
				//	}
				String type = extEvents.get(XConceptExtension.instance().extractName(event1)).getAttributeTypes().get(candidateCorrelation.split(";")[0]);
				String attr1 = candidateCorrelation.split(";")[0];
				String attr2 = candidateCorrelation.split(";")[1];
				if (type.equals("Float")){
					try{
						Float num1 = new Float(event1.getAttributes().get(attr1).toString());
						Float num2 = new Float(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=") && num1.floatValue()==num2.floatValue()){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals("<=") && num1.floatValue()<=num2.floatValue()){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals(">=") && num1.floatValue()>=num2.floatValue()){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("!=") && num1.floatValue()!=num2.floatValue()){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Byte")){
					try{
						byte num1 = new Byte(event1.getAttributes().get(attr1).toString());
						byte num2 = new Byte(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}

						if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Double")){
					try{
						Double num1 = new Double(event1.getAttributes().get(attr1).toString());
						Double num2 = new Double(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Integer")){
					try{
						Integer num1 = new Integer(event1.getAttributes().get(attr1).toString());
						Integer num2 = new Integer(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Long")){
					try{
						Long num1 = new Long(event1.getAttributes().get(attr1).toString());
						Long num2 = new Long(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Short")){
					try{
						Short num1 = new Short(event1.getAttributes().get(attr1).toString());
						Short num2 = new Short(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Boolean")){
					boolean num1 = new Boolean(event1.getAttributes().get(attr1).toString());
					boolean num2 = new Boolean(event2.getAttributes().get(attr2).toString());
					if(candidateCorrelation.split(";")[2].equals("b=") && num1==num2){
						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						foundAtLeastOne = true;
						break;
					}
					if(candidateCorrelation.split(";")[2].equals("b!=") && num1!=num2){
						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						foundAtLeastOne = true;
						break;
					}
				}else if (type.equals("String")){
					if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){

						long num1 = XTimeExtension.instance().extractTimestamp(event1).getTime();
						long num2 = XTimeExtension.instance().extractTimestamp(event2).getTime();
						long timeDiff = num2 - num1;
						if(timeDiff<0){
							timeDiff = 0 - timeDiff;
						}
						double avg = new Double(candidateCorrelation.split(";")[3]);
						double stddev = new Double(candidateCorrelation.split(";")[4]);
						if(candidateCorrelation.split(";")[2].equals("singlestddev")){
							if(timeDiff<= avg+stddev){
								//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
								foundAtLeastOne = true;
								break;
							}
						}
						if(candidateCorrelation.split(";")[2].equals("doublestddev")){
							if(timeDiff<= avg+2*stddev){
								//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
								foundAtLeastOne = true;
								break;
							}
						}
						//					if(candidateCorrelation.split(";")[2].equals("d=") && num1==num2){
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}
						//					if( candidateCorrelation.split(";")[2].equals("d<=") && num1<=num2){
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}
						//					if( candidateCorrelation.split(";")[2].equals("d>=") && num1>=num2){
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}
						//					if(candidateCorrelation.split(";")[2].equals("d!=") && num1!=num2){
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}

					}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
						String num1 = new String(event1.getAttributes().get(attr1).toString());
						String num2 = new String(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("s=") && num1.equals(num2)){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
						if(candidateCorrelation.split(";")[2].equals("s!=") && !num1.equals(num2)){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							foundAtLeastOne = true;
							break;
						}
					}


				}
			}
			if(foundAtLeastOne){
				correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
			}
		}
		return correspAfterDisambiguation;
	}









	public Vector<Integer> getTargetsCorrespondingToActivationsAfterDisambiguationForConjunctiveCorrelation(ExtendedTrace ext ,ConstraintDefinition constraintDefinition, Integer activationPosition, HashMap<String,ExtendedEvent>  extEvents, Vector<String> conjunctiveCorrelation){
		Vector<Integer> correspAfterDisambiguation = new Vector<Integer>();
		List<String> parameters = new ArrayList<String>();

		for(Parameter parameter : constraintDefinition.getParameters()){
			if(constraintDefinition.getBranches(parameter).iterator().hasNext()){
				String activityName = constraintDefinition.getBranches(parameter).iterator().next().getName();
				if((activityName.contains("-assign")||activityName.contains("-ate_abort")||activityName.contains("-suspend")||activityName.contains("-complete")||activityName.contains("-autoskip")||activityName.contains("-manualskip")||activityName.contains("pi_abort")||activityName.contains("-reassign")||activityName.contains("-resume")||activityName.contains("-schedule")||activityName.contains("-start")||activityName.contains("-unknown")||activityName.contains("-withdraw"))){
					String[] splittedName = constraintDefinition.getBranches(parameter).iterator().next().getName().split("-");
					activityName = splittedName[0];
					for(int i = 1; i<splittedName.length-1; i++){
						activityName = activityName + "-" + splittedName[i];
					}	
				}
				parameters.add(activityName);

			}else{
				parameters.add("EMPTY_PARAM");
			}
		}



		for(int j = 0; j<ext.getCorrespcorrel().get(activationPosition).size(); j++){
			boolean foundAtLeastOne = true;
			for(String candidateCorrelation : conjunctiveCorrelation){
				XEvent event1 = null;
				XEvent event2 = null;
				Integer eveAct = activationPosition;
				Integer eveTarg = ext.getCorrespcorrel().get(activationPosition).get(j);
				//if(XConceptExtension.instance().extractName(ext.getTrace().get(eveAct)).equals(parameters.get(0)) &&
				//		XConceptExtension.instance().extractName(ext.getTrace().get(eveTarg)).equals(parameters.get(1))){
				event1 = ext.getTrace().get(eveAct);
				event2 = ext.getTrace().get(eveTarg);
				//	}else{
				//	event1 = ext.getTrace().get(eveTarg);
				//	event2 = ext.getTrace().get(eveAct);
				//	}
				String type = extEvents.get(XConceptExtension.instance().extractName(event1)).getAttributeTypes().get(candidateCorrelation.split(";")[0]);
				String attr1 = candidateCorrelation.split(";")[0];
				String attr2 = candidateCorrelation.split(";")[1];
				if (type.equals("Float")){
					try{
						Float num1 = new Float(event1.getAttributes().get(attr1).toString());
						Float num2 = new Float(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=")){
							//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
							if(!(num1.floatValue()==num2.floatValue())){
								foundAtLeastOne = false;
								break;
							}
						}
						if( candidateCorrelation.split(";")[2].equals("<=")){
							if(!(num1.floatValue()<=num2.floatValue())){
								foundAtLeastOne = false;
								break;
							}					
						}
						if( candidateCorrelation.split(";")[2].equals(">=")){
							if(!( num1.floatValue()>=num2.floatValue())){
								foundAtLeastOne=false;
								break;
							}
						}
						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						if(candidateCorrelation.split(";")[2].equals("!=")){
							if(!( num1.floatValue()!=num2.floatValue())){
								foundAtLeastOne=false;
								break;
							}
						}

					}catch(NumberFormatException e){


					}
				}else if (type.equals("Byte")){
					try{
						byte num1 = new Byte(event1.getAttributes().get(attr1).toString());
						byte num2 = new Byte(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=")){
							if(!( num1==num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals("<=")){
							if(!( num1<=num2)){
								foundAtLeastOne=false;
								break;
							}
						}
						if( candidateCorrelation.split(";")[2].equals(">=")){
							if(!( num1>=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if(candidateCorrelation.split(";")[2].equals("!=")){
							if(!( num1!=num2)){
								foundAtLeastOne=false;
								break;
							}
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Double")){
					try{
						Double num1 = new Double(event1.getAttributes().get(attr1).toString());
						Double num2 = new Double(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=")){
							if(!( num1==num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals("<=")){
							if(!( num1<=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals(">=")){
							if(!( num1>=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if(candidateCorrelation.split(";")[2].equals("!=")){
							if(!( num1!=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

					}catch(NumberFormatException e){


					}
				}else if (type.equals("Integer")){
					try{
						Integer num1 = new Integer(event1.getAttributes().get(attr1).toString());
						Integer num2 = new Integer(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=")){
							if(!( num1==num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals("<=")){
							if(!( num1<=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals(">=")){
							if(!( num1>=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if(candidateCorrelation.split(";")[2].equals("!=")){
							if(!( num1!=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

					}catch(NumberFormatException e){


					}
				}else if (type.equals("Long")){
					try{
						Long num1 = new Long(event1.getAttributes().get(attr1).toString());
						Long num2 = new Long(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=")){
							if(!( num1==num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals("<=")){
							if(!( num1<=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if( candidateCorrelation.split(";")[2].equals(">=")){
							if(!( num1>=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

						if(candidateCorrelation.split(";")[2].equals("!=")){
							if(!( num1!=num2)){
								foundAtLeastOne=false;
								break;
							}
						}

					}catch(NumberFormatException e){


					}
				}else if (type.equals("Short")){
					try{
						Short num1 = new Short(event1.getAttributes().get(attr1).toString());
						Short num2 = new Short(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("=")){
							if(!( num1==num2)){
								foundAtLeastOne=false;
								break;
							}
						}
						if( candidateCorrelation.split(";")[2].equals("<=")){
							if(!( num1<=num2)){
								foundAtLeastOne=false;
								break;
							}
						}
						if( candidateCorrelation.split(";")[2].equals(">=")){
							if(!( num1>=num2)){
								foundAtLeastOne=false;
								break;
							}
						}
						if(candidateCorrelation.split(";")[2].equals("!=")){
							if(!( num1!=num2)){
								foundAtLeastOne=false;
								break;
							}
						}
					}catch(NumberFormatException e){


					}
				}else if (type.equals("Boolean")){
					boolean num1 = new Boolean(event1.getAttributes().get(attr1).toString());
					boolean num2 = new Boolean(event2.getAttributes().get(attr2).toString());
					if(candidateCorrelation.split(";")[2].equals("b=")){
						if(!( num1==num2)){
							foundAtLeastOne=false;
							break;
						}
					}
					if(candidateCorrelation.split(";")[2].equals("b!=")){
						if(!( num1!=num2)){
							foundAtLeastOne=false;
							break;
						}
					}
				}else if (type.equals("String")){
					if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){

						long num1 = XTimeExtension.instance().extractTimestamp(event1).getTime();
						long num2 = XTimeExtension.instance().extractTimestamp(event2).getTime();
						long timeDiff = num2 - num1;
						if(timeDiff<0){
							timeDiff = 0 - timeDiff;
						}
						double avg = new Double(candidateCorrelation.split(";")[3]);
						double stddev = new Double(candidateCorrelation.split(";")[4]);
						if(candidateCorrelation.split(";")[2].equals("singlestddev")){
							if(timeDiff> avg+stddev){
								//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
								foundAtLeastOne = false;
								break;
							}
						}
						if(candidateCorrelation.split(";")[2].equals("doublestddev")){
							if(timeDiff> avg+2*stddev){
								foundAtLeastOne = false;
								break;
							}
						}
						//					if(candidateCorrelation.split(";")[2].equals("d=")){if(!( num1==num2)){foundAtLeastOne=false;
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}
						//					if( candidateCorrelation.split(";")[2].equals("d<=")){if(!( num1<=num2)){foundAtLeastOne=false;
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}
						//					if( candidateCorrelation.split(";")[2].equals("d>=")){if(!( num1>=num2)){foundAtLeastOne=false;
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}
						//					if(candidateCorrelation.split(";")[2].equals("d!=")){if(!( num1!=num2)){foundAtLeastOne=false;
						//						//correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
						//					}

					}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
						String num1 = new String(event1.getAttributes().get(attr1).toString());
						String num2 = new String(event2.getAttributes().get(attr2).toString());
						if(candidateCorrelation.split(";")[2].equals("s=")){
							if(!num1.equals(num2)){
								foundAtLeastOne = false;
								break;
							}
						}
						if(candidateCorrelation.split(";")[2].equals("s!=")){
							if(num1.equals(num2)){
								foundAtLeastOne = false;
								break;
							}
						}
					}


				}
			}
			if(foundAtLeastOne){
				correspAfterDisambiguation.add(ext.getCorrespcorrel().get(activationPosition).get(j));
			}
		}
		return correspAfterDisambiguation;
	}












}
/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.plugins.correlation;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.declare2ltl.Correlations;
import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.Parameter;

@Plugin(name = "Discover Event Correlations in a Declare Map", parameterLabels = { "Log", "Declare Model" }, returnLabels = { "Discovered Correlations" }, returnTypes = { Correlations.class }, userAccessible = true)
public class DeclareExtensionWithData {
	Progress prog;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl", uiLabel = "Discover Event Correlations in a Declare Map", pack = "DeclareMiner")
	@PluginVariant(requiredParameterLabels = { 0, 1}, variantLabel = "Discover Event Correlations in a Declare Map")
	public Correlations analyse(UIPluginContext context, XLog log, DeclareMinerOutput inputModel) {
		return extend(context, log,inputModel, inputModel.getModel(), inputModel.getSupportRule(), inputModel.getConfidence(), inputModel.getActivations());
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl", uiLabel = "Discover Event Correlations in a Declare Model", pack = "DeclareMiner")
	@PluginVariant(requiredParameterLabels = { 0, 1}, variantLabel = "Discover Event Correlations in a Declare Map")
	public Correlations analyse(UIPluginContext context, XLog log, DeclareMap inputModel) {
		return extend(context, log,inputModel);
	}
	


	public Correlations extend(UIPluginContext context, XLog log, DeclareMinerOutput complModel ,DeclareMap inputModel, HashMap<Integer, Float> hashMap, HashMap<Integer, Float> hashMap2, HashMap<Object, Integer> hashMap3){
		Vector<ExtendedTrace> tracesWithCorrespondingEvents = null;
		PrintWriter pw = null;
		HashMap<String,ExtendedEvent> extEvents = null;
		Correlations output = new Correlations();
		output.setComplModel(complModel);
		HashMap<ConstraintDefinition, HashMap<String, Double>> correlationSupportMap  = new HashMap<ConstraintDefinition, HashMap<String, Double>>();
		HashMap<ConstraintDefinition, HashMap<String, Double>> correlationDisambiguationMap  = new HashMap<ConstraintDefinition, HashMap<String, Double>>();
		////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		double correlationSupport = 0;
		double disambiguationSupport = 0;
		///////////////////////////////////////////////////////
		//////////////////////////////////////////////////////
		///////////////////////////////////////////////////
		Vector<String> setOfGoodCorrelations = new Vector<String>();
		try{
			pw = new PrintWriter(new File("correlation.txt"));
		//	pw.println("CORRELATION SUPPORT: "+correlationSupport);
		//	pw.println("DISAMBIGUATION SUPPORT: "+disambiguationSupport);
			Vector<String> ads = new Vector<String>();
			for(ActivityDefinition ad : inputModel.getModel().getActivityDefinitions()){
				String activityName = ad.getName();
				if((activityName.contains("-assign")||activityName.contains("-ate_abort")||activityName.contains("-suspend")||activityName.contains("-complete")||activityName.contains("-autoskip")||activityName.contains("-manualskip")||activityName.contains("pi_abort")||activityName.contains("-reassign")||activityName.contains("-resume")||activityName.contains("-schedule")||activityName.contains("-start")||activityName.contains("-unknown")||activityName.contains("-withdraw"))){
					String[] splittedName = ad.getName().split("-");
					activityName = splittedName[0];
					for(int i = 1; i<splittedName.length-1; i++){
						activityName = activityName + "-" + splittedName[i];
					}
				}
				ads.add(activityName);
			}

		//	DeclareMinerInput input = complModel.getInput();
			
						if(context!=null){
				context.getProgress().setMaximum(inputModel.getModel().constraintDefinitionsCount());
				context.getProgress().setValue(5);
			}
			String res = "";
			
			//MapsGenerator gen = new MapsGenerator("CONFORMANCE", 0, 0);
			extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads,log);

			for(ConstraintDefinition constraintDefinition : inputModel.getModel().getConstraintDefinitions()){
				HashMap<String, Double> discoveredCorrelations = new HashMap<String, Double>();
				//double numberOfActivationsWithoutData = 0;
				double numberOfViolationsWithoutData = 0;
				double numberOfAmbiguousFulfillmentWithoutData = 0;
				double numberOfNonAmbiguousFulfillmentWithoutData = 0;
				double numberOfActivationsWithData = 0;
				double numberOfViolationsWithData = 0;
				double numberOfAmbiguousFulfillmentWithData = 0;
				double numberOfNonAmbiguousFulfillmentWithData = 0;
				if(context!=null){
					context.getProgress().inc();
				}
				setOfGoodCorrelations = new Vector<String>();
				//////////////	
				//gen.initializeMaps(constraintDefinition, log, ads);

			//	HashMap<String, HashMap<String, Object>> mapFeat = gen.getInstancesFeatureFeatureValueMap();

			///	HashMap<String, String> mapLabel = gen.getInstancesClassLableMap();
				int nonconf = 0;

//				for(String inst: mapLabel.keySet()){
//					if(mapLabel.get(inst).contains("non")){
//						nonconf++;
//					}
//				}
//
		//		System.out.println(mapFeat);
		//		System.out.println(mapLabel);
				///////////////////////////////////
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

				DeclareTemplate template = DeclareMiner.getTemplate(constraintDefinition); 
				if(hashMap!=null){
					pw.println("CONSTRAINT SUPPORT: "+hashMap.get(constraintDefinition.getId()));
				}
				if(hashMap!=null){
					pw.println("CONSTRAINT CONFIDENCE: "+hashMap2.get(constraintDefinition.getId()));
				}
//				pw.println("PARAM_A:"+parameters.get(0));
//				pw.println("PARAM_B:"+parameters.get(1));
//				pw.flush();
				
				switch(template){
					case Succession:
						pw.println("PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Precedence, parameters.get(1), parameters.get(0), log );

						Vector<String> comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						CorrelationMiner miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						Disambiguation disambiguator = new Disambiguation();
						HashMap<String, Double> disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		

						}
						pw.println("RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	

						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Alternate_Succession:

						pw.println("ALTERNATE PRECEDENCE");

						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		
						}

						pw.println("ALTERNATE RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Chain_Succession:
						pw.println("CHAIN PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		

						}
						pw.println("CHAIN RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Precedence:
						pw.println("PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Alternate_Precedence:
						pw.println("ALTERNATE PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Chain_Precedence:
						pw.println("CHAIN PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		

						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Responded_Existence:
						pw.println("RESPONDED EXISTENCE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Responded_Existence, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	

						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Response:
						pw.println("RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Response, parameters.get(0), parameters.get(1), log );

							comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
//						comparablePairs= new Vector<String>();
//						comparablePairs.add("org:group;org:group");
//						comparablePairs.add("Producer code;Producer code");
//						comparablePairs.add("time:timestamp;time:timestamp");
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Alternate_Response:
						pw.println("ALTERNATE RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Chain_Response:
						pw.println("CHAIN RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}

							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case CoExistence:
						pw.println("RESPONDED EXISTENCE 1");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Responded_Existence, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	

						}
						pw.println("RESPONDED EXISTENCE 2");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Responded_Existence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Not_CoExistence:
						//	templateInfo = new NotCoexistenceInfo();
						//	tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, parameters.get(0), parameters.get(1));
					//	correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
					//	correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Not_Succession:
					case Not_Chain_Succession:
						//	templateInfo = new NegativeRelationInfo();
						//	tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, parameters.get(0), parameters.get(1));
					//	correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
					//	correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;	

				}
				pw.flush();	
				Disambiguation disambiguator = new Disambiguation();
				CorrelationMiner miner = new CorrelationMiner();
			//	pw.println("CONSIDERED CORRELATIONS FOR DISJUNCTION AND CONJUNCTION");
				for(String correlation : setOfGoodCorrelations){
		//			pw.println(correlation);
				}
		//		pw.println("DEGREE OF DISAMBIGUATION OF THE DISJUNCTION :"+disambiguator.getDegreeofDisambiguationDisjunction(tracesWithCorrespondingEvents, constraintDefinition, setOfGoodCorrelations, extEvents));
		//		pw.println("DEGREE OF DISAMBIGUATION OF THE CONJUNCTION :"+disambiguator.getDegreeofDisambiguationConjunction(tracesWithCorrespondingEvents, constraintDefinition, setOfGoodCorrelations, extEvents));



			}


		}catch(Exception e){
			e.printStackTrace();
		}

		pw.flush();
		pw.close();

		output.setCorrelationSupport(correlationSupportMap); 
		output.setCorrelationDisambiguation(correlationDisambiguationMap);
		
		return output;
	}

	private static boolean isConfidenceEvaluable(String constraintName){
		return !constraintName.startsWith("existence") && !constraintName.startsWith("absence")&& !constraintName.startsWith("exactly")
				&& !constraintName.contains("choice") && !constraintName.contains("init");
	}

	
	
	
	
	
	
	public Correlations extend(UIPluginContext context, XLog log,DeclareMap inputModel){
		Vector<ExtendedTrace> tracesWithCorrespondingEvents = null;
		PrintWriter pw = null;
		HashMap<String,ExtendedEvent> extEvents = null;
		Correlations output = new Correlations();
	//	output.setComplModel(complModel);
		HashMap<ConstraintDefinition, HashMap<String, Double>> correlationSupportMap  = new HashMap<ConstraintDefinition, HashMap<String, Double>>();
		HashMap<ConstraintDefinition, HashMap<String, Double>> correlationDisambiguationMap  = new HashMap<ConstraintDefinition, HashMap<String, Double>>();
		////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////
		double correlationSupport = 0;
		double disambiguationSupport = 0;
		///////////////////////////////////////////////////////
		//////////////////////////////////////////////////////
		///////////////////////////////////////////////////
		Vector<String> setOfGoodCorrelations = new Vector<String>();
		try{
			pw = new PrintWriter(new File("correlation.txt"));
		//	pw.println("CORRELATION SUPPORT: "+correlationSupport);
		//	pw.println("DISAMBIGUATION SUPPORT: "+disambiguationSupport);
			Vector<String> ads = new Vector<String>();
			for(ActivityDefinition ad : inputModel.getModel().getActivityDefinitions()){
				String activityName = ad.getName();
				if((activityName.contains("-assign")||activityName.contains("-ate_abort")||activityName.contains("-suspend")||activityName.contains("-complete")||activityName.contains("-autoskip")||activityName.contains("-manualskip")||activityName.contains("pi_abort")||activityName.contains("-reassign")||activityName.contains("-resume")||activityName.contains("-schedule")||activityName.contains("-start")||activityName.contains("-unknown")||activityName.contains("-withdraw"))){
					String[] splittedName = ad.getName().split("-");
					activityName = splittedName[0];
					for(int i = 1; i<splittedName.length-1; i++){
						activityName = activityName + "-" + splittedName[i];
					}
				}
				ads.add(activityName);
			}

			DeclareMinerInput input = new DeclareMinerInput();
			input.setAlpha(1);
			Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteria = new HashSet<AprioriKnowledgeBasedCriteria>(); 
			input.setReferenceEventType("complete");
			//Set<DeclareTemplate> selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
			input.setEventTypesMismatchLogModel(false);
			input.setMinSupport(0);
			Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();
			DeclareTemplate[] declareTemplates = DeclareTemplate.values();
			for(DeclareTemplate d : declareTemplates){
				String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
				templateNameStringDeclareTemplateMap.put(templateNameString, d);
			}

			if(context!=null){
				context.getProgress().setMaximum(inputModel.getModel().constraintDefinitionsCount());
				context.getProgress().setValue(5);
			}
			String res = "";
			input.setEventTypesMismatchLogModel(true);
			input.setAprioriKnowledgeBasedCriteriaSet(aprioriKnowledgeBasedCriteria);

			//MapsGenerator gen = new MapsGenerator("CONFORMANCE", 0, 0);
			extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads,log);

			for(ConstraintDefinition constraintDefinition : inputModel.getModel().getConstraintDefinitions()){
				HashMap<String, Double> discoveredCorrelations = new HashMap<String, Double>();
				//double numberOfActivationsWithoutData = 0;
				double numberOfViolationsWithoutData = 0;
				double numberOfAmbiguousFulfillmentWithoutData = 0;
				double numberOfNonAmbiguousFulfillmentWithoutData = 0;
				double numberOfActivationsWithData = 0;
				double numberOfViolationsWithData = 0;
				double numberOfAmbiguousFulfillmentWithData = 0;
				double numberOfNonAmbiguousFulfillmentWithData = 0;
				if(context!=null){
					context.getProgress().inc();
				}
				setOfGoodCorrelations = new Vector<String>();
				//////////////	
				//gen.initializeMaps(constraintDefinition, log, ads);

			//	HashMap<String, HashMap<String, Object>> mapFeat = gen.getInstancesFeatureFeatureValueMap();

			///	HashMap<String, String> mapLabel = gen.getInstancesClassLableMap();
				int nonconf = 0;

//				for(String inst: mapLabel.keySet()){
//					if(mapLabel.get(inst).contains("non")){
//						nonconf++;
//					}
//				}
//
		//		System.out.println(mapFeat);
		//		System.out.println(mapLabel);
				///////////////////////////////////
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

				DeclareTemplate template = DeclareMiner.getTemplate(constraintDefinition); 
//				if(hashMap!=null){
//					pw.println("CONSTRAINT SUPPORT: "+hashMap.get(constraintDefinition.getId()));
//				}
//				if(hashMap!=null){
//					pw.println("CONSTRAINT CONFIDENCE: "+hashMap2.get(constraintDefinition.getId()));
//				}
//				pw.println("PARAM_A:"+parameters.get(0));
//				pw.println("PARAM_B:"+parameters.get(1));
//				pw.flush();
				
				switch(template){
					case Succession:
						pw.println("PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Precedence, parameters.get(1), parameters.get(0), log );

						Vector<String> comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						CorrelationMiner miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						Disambiguation disambiguator = new Disambiguation();
						HashMap<String, Double> disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		

						}
						pw.println("RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	

						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Alternate_Succession:

						pw.println("ALTERNATE PRECEDENCE");

						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		
						}

						pw.println("ALTERNATE RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Chain_Succession:
						pw.println("CHAIN PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		

						}
						pw.println("CHAIN RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Precedence:
						pw.println("PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Alternate_Precedence:
						pw.println("ALTERNATE PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Chain_Precedence:
						pw.println("CHAIN PRECEDENCE");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Precedence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}		

						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Responded_Existence:
						pw.println("RESPONDED EXISTENCE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Responded_Existence, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}

								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	

						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Response:
						pw.println("RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Response, parameters.get(0), parameters.get(1), log );

							comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
//						comparablePairs= new Vector<String>();
//						comparablePairs.add("org:group;org:group");
//						comparablePairs.add("Producer code;Producer code");
//						comparablePairs.add("time:timestamp;time:timestamp");
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Alternate_Response:
						pw.println("ALTERNATE RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Alternate_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Chain_Response:
						pw.println("CHAIN RESPONSE");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Chain_Response, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}

							}	
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case CoExistence:
						pw.println("RESPONDED EXISTENCE 1");
						pw.println("activation: "+parameters.get(0));
						pw.println("target: "+parameters.get(1));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Responded_Existence, parameters.get(0), parameters.get(1), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(0), parameters.get(1), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(0), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}	

						}
						pw.println("RESPONDED EXISTENCE 2");
						pw.println("activation: "+parameters.get(1));
						pw.println("target: "+parameters.get(0));
						tracesWithCorrespondingEvents = Correlator.getExtendedTracesWithCorrespondingEvents(DeclareTemplate.Responded_Existence, parameters.get(1), parameters.get(0), log );

						comparablePairs = ExtendedEvent.getComparablePairs(ads, log, parameters.get(1), parameters.get(0), extEvents);
						//extEvents = ExtendedEvent.getEventsWithAttributeTypes(ads, log);
						miner = new CorrelationMiner();
						discoveredCorrelations = miner.mineCorrelations(pw,correlationSupport,parameters.get(1), comparablePairs, tracesWithCorrespondingEvents, constraintDefinition, extEvents);

						disambiguator = new Disambiguation();
						disambiguationMap = new HashMap<String, Double>();


						for(String candidateCorrelation : discoveredCorrelations.keySet()){				
							if(discoveredCorrelations.get(candidateCorrelation)>=correlationSupport){
								double degreeOfDisambiguation = disambiguator.getDegreeofDisambiguation(tracesWithCorrespondingEvents, constraintDefinition, candidateCorrelation, extEvents);
								disambiguationMap.put(candidateCorrelation, degreeOfDisambiguation);
								if(degreeOfDisambiguation>disambiguationSupport){
									setOfGoodCorrelations.add(candidateCorrelation);
								}
								if(candidateCorrelation.split(";").length>3){
									if(candidateCorrelation.split(";")[2].equals("singlestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp single standard deviation) :  "+degreeOfDisambiguation);
									}
									if(candidateCorrelation.split(";")[2].equals("doublestddev")){
										pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (timestamp double standard deviation) :  "+degreeOfDisambiguation);								
									}
								}else{
									pw.println("DEGREE OF DISAMBIGUATION OF CORRELATION (activation attribute <R> target attribute) "+ candidateCorrelation.split(";")[0]+" "+candidateCorrelation.split(";")[2]+" "+candidateCorrelation.split(";")[1]+" :  "+degreeOfDisambiguation);
								}
							}
						}
						correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
						correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Not_CoExistence:
						//	templateInfo = new NotCoexistenceInfo();
						//	tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, parameters.get(0), parameters.get(1));
					//	correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
					//	correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;
					case Not_Succession:
					case Not_Chain_Succession:
						//	templateInfo = new NegativeRelationInfo();
						//	tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, parameters.get(0), parameters.get(1));
					//	correlationSupportMap.put(constraintDefinition, discoveredCorrelations);
					//	correlationDisambiguationMap.put(constraintDefinition, disambiguationMap);
						break;	

				}
				pw.flush();	
				Disambiguation disambiguator = new Disambiguation();
				CorrelationMiner miner = new CorrelationMiner();
			//	pw.println("CONSIDERED CORRELATIONS FOR DISJUNCTION AND CONJUNCTION");
				for(String correlation : setOfGoodCorrelations){
		//			pw.println(correlation);
				}
		//		pw.println("DEGREE OF DISAMBIGUATION OF THE DISJUNCTION :"+disambiguator.getDegreeofDisambiguationDisjunction(tracesWithCorrespondingEvents, constraintDefinition, setOfGoodCorrelations, extEvents));
		//		pw.println("DEGREE OF DISAMBIGUATION OF THE CONJUNCTION :"+disambiguator.getDegreeofDisambiguationConjunction(tracesWithCorrespondingEvents, constraintDefinition, setOfGoodCorrelations, extEvents));



			}


		}catch(Exception e){
			e.printStackTrace();
		}

		pw.flush();
		pw.close();

		output.setCorrelationSupport(correlationSupportMap); 
		output.setCorrelationDisambiguation(correlationDisambiguationMap);
		
		return output;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

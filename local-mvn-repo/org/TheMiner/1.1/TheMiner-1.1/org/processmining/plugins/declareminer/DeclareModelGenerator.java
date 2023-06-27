package org.processmining.plugins.declareminer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.processmining.plugins.correlation.ExtendedEvent;
import org.processmining.plugins.declare2ltl.DeclareExtensionOutput;
import org.processmining.plugins.declareanalyzer.executions.ExecutionsTree;
import org.processmining.plugins.declareminer.apriori.FindItemSets;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.templates.Absence2Info;
import org.processmining.plugins.declareminer.templates.Absence3Info;
import org.processmining.plugins.declareminer.templates.AbsenceInfo;
import org.processmining.plugins.declareminer.templates.ChoiceInfo;
import org.processmining.plugins.declareminer.templates.CoexistenceInfo;
import org.processmining.plugins.declareminer.templates.Exactly1Info;
import org.processmining.plugins.declareminer.templates.Exactly2Info;
import org.processmining.plugins.declareminer.templates.ExclusiveChoiceInfo;
import org.processmining.plugins.declareminer.templates.Existence2Info;
import org.processmining.plugins.declareminer.templates.Existence3Info;
import org.processmining.plugins.declareminer.templates.ExistenceInfo;
import org.processmining.plugins.declareminer.templates.InitInfo;
import org.processmining.plugins.declareminer.templates.NegativeRelationInfo;
import org.processmining.plugins.declareminer.templates.NotCoexistenceInfo;
import org.processmining.plugins.declareminer.templates.PrecedenceInfo;
import org.processmining.plugins.declareminer.templates.RespondedExistenceInfo;
import org.processmining.plugins.declareminer.templates.ResponseInfo;
import org.processmining.plugins.declareminer.templates.SuccessionInfo;
import org.processmining.plugins.declareminer.templates.TemplateInfo;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinitonCell;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.Language;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.TemplateBroker;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

public class DeclareModelGenerator {

	private Vector<Integer> transitiveClosureSuccessionConstraints;
	private Vector<Integer> transitiveClosureCoexistenceConstraints;
	private Vector<Integer> transitiveClosureResponseConstraints;
	private Vector<Integer> transitiveClosurePrecedenceConstraints;
	private Vector<Integer> transitiveClosureRespondedExistenceConstraints;
	private Vector<Integer> transitiveClosureNotCoexistenceConstraints;
	private boolean transitiveReduction;

	public DeclareMinerOutput createModel(boolean hier, boolean trans,
			Map<DeclareTemplate, Vector<MetricsValues>> metricsVectorPerTemplate, XLog log, DeclareMinerInput input,
			Vector<String> templatesToRemove, Vector<String> eventsToRemove, Vector<String> eventTypesToRemove) {
		transitiveReduction = trans;
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> visibleConstraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, String> constraintFormulaMap = new HashMap<Integer, String>();
		HashMap<Integer, Float> constraintSupportAntecendentMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintSupportConsequentMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintSupportRuleMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintConfidenceMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintCpirMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintInterestFactorMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleSupportRuleMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleConfidenceMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleCpirMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleInterestFactorMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> activitySupportMap = new HashMap<Integer, Float>();
		HashMap<ActivityDefinition, Vector<ConstraintDefinition>> activityConstraintMap = new HashMap<ActivityDefinition, Vector<ConstraintDefinition>>();
		Vector<String> activityDefinitions = new Vector<String>();
		HashMap<String, ActivityDefinition> activitiesMap = new HashMap<String, ActivityDefinition>();
		Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = input
				.getDeclareTemplateConstraintTemplateMap();

		FindItemSets f = new FindItemSets(log, input);

		InputStream ir = getClass().getResourceAsStream("/resources/template.xml");
		File language = null;
		try {
			language = File.createTempFile("template", ".xml");
			BufferedReader br = new BufferedReader(new InputStreamReader(ir));
			String line = br.readLine();
			PrintStream out = new PrintStream(language);
			while (line != null) {
				out.println(line);
				line = br.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Vector<ConstraintDefinition> allDiscoveredConstraints = new Vector<ConstraintDefinition>();
		DeclareMinerOutput visualizationData = new DeclareMinerOutput();
		HashMap<Integer, MetricsValues> constraintMetricsMap = new HashMap<Integer, MetricsValues>();
		TemplateBroker template = XMLBrokerFactory.newTemplateBroker(language.getAbsolutePath());
		List<Language> languages = template.readLanguages();
		Language lang = languages.get(0);
		AssignmentModel model = new AssignmentModel(lang);
		model.setName("new model");
		DeclareMap outputMap = null;
		ActivityDefinition activitydefinition = null;
		HashMap<Integer, String> allActivities = new HashMap<Integer, String>();
		Vector<String> adIds = new Vector<String>();
		int constraintID = 0;
		int activityID = 1;

		float minSupport = input.getMinSupport() / 100.f - 1;
		float maxSupport = minSupport;
		if (!constraintSupportRuleMap.isEmpty()) {
			maxSupport = Collections.max(constraintSupportRuleMap.values());
		}
		maxSupport = maxSupport + 1;
		float avgSupport = ((minSupport + maxSupport) / 2.f);

		float maxConfidence = 0;
		for (Float max : constraintConfidenceMap.values()) {
			if ((max > maxConfidence) && (max < (Float.MAX_VALUE - 10000))) {
				maxConfidence = max;
			}
		}

		float minConfidence = maxConfidence;
		if (!constraintConfidenceMap.isEmpty()) {
			minConfidence = Collections.min(constraintConfidenceMap.values());
		}

		minConfidence = minConfidence - 1;
		maxConfidence = maxConfidence + 1;

		float avgConfidence = ((minConfidence + maxConfidence) / 2.f);

		float maxCPIR = 0;
		for (Float max : constraintCpirMap.values()) {
			if ((max > maxCPIR) && (max < (Float.MAX_VALUE - 10000))) {
				maxCPIR = max;
			}
		}

		float minCPIR = maxCPIR;
		if (!constraintCpirMap.isEmpty()) {
			minCPIR = Collections.min(constraintCpirMap.values());
		}

		minCPIR = minCPIR - 1;
		maxCPIR = maxCPIR + 1;
		float avgCPIR = ((minCPIR + maxCPIR) / 2.f);

		float maxInterestFactor = 0;
		for (Float max : constraintInterestFactorMap.values()) {
			if ((max > maxInterestFactor) && (max < (Float.MAX_VALUE - 10000))) {
				maxInterestFactor = max;
			} else {
				// System.out.println("Max Interest Factor: "+max);
			}
		}

		float minInterestFactor = maxInterestFactor;
		if (!constraintInterestFactorMap.isEmpty()) {
			minInterestFactor = Collections.min(constraintInterestFactorMap.values());
		}

		minInterestFactor = minInterestFactor - 1;
		maxInterestFactor = maxInterestFactor + 1;
		float avgInterestFactor = ((minInterestFactor + maxInterestFactor) / 2.f);

		for (Object key : metricsVectorPerTemplate.keySet()) {
			Vector<MetricsValues> currentMetricsVectorPerTemplate = metricsVectorPerTemplate.get(key);
			DeclareTemplate currentTemplate = (DeclareTemplate) key;
			int numberOfParameters = 0;
			if (isBinary(currentTemplate)) {
				numberOfParameters = 2;
			} else {
				numberOfParameters = 1;
			}
			String[][] parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate = new String[currentMetricsVectorPerTemplate
					.size()][numberOfParameters];
			for (int i = 0; i < currentMetricsVectorPerTemplate.size(); i++) {
				parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i] = new String[currentMetricsVectorPerTemplate
						.get(i).getParameters().size()];
				for (int j = 0; j < numberOfParameters; j++) {
					parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j] = currentMetricsVectorPerTemplate
							.get(i).getParameters().get(j);
				}
			}

			for (int i = 0; i < parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate.length; i++) {
				for (int j = 0; j < parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i].length; j++) {
					if (!activityDefinitions
							.contains(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j])) {
						if (!parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j]
								.contains("EMPTY_PARAM")) {
							activityDefinitions
									.add(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j]);
							activitydefinition = model.addActivityDefinition(activityID); // new
																							// ActivityDefinition(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j],
																							// activityID, model);
							activitydefinition
									.setName(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j]);
							activitiesMap.put(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j],
									activitydefinition);
							allActivities.put(activityID,
									parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j]);
							activitySupportMap.put(activityID, f.getSupport(activitydefinition.getName()) / 100.f);
							activityID++;

							// for (int i = 0; i <
							// parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate.length; i++)
							// {
							// for(int j = 0; j <
							// parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i].length;
							// j++){
							// if(!activityDefinitions.contains(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j])){
							// activityDefinitions.add(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j]);
							// activitydefinition = model.addActivityDefinition(activityID);
							// activitydefinition.setName(parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[i][j]);
							// activitySupportMap.put(activityID,
							// f.getSupport(activitydefinition.getName())/100.f);
							// activityID++;
							// }
							// }
						}
					}
				}
			}

			for (int k = 0; k < parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate.length; k++) {
				MetricsValues metricsValues = currentMetricsVectorPerTemplate.get(k);

				if ((metricsValues.getSupportRule() >= minSupport) &&
						(metricsValues.getConfidence() >= minConfidence) &&
						(metricsValues.getCPIR() >= minCPIR) &&
						(metricsValues.getI() >= minInterestFactor)) {

					constraintID++;
					ConstraintDefinition constraintdefinition = new ConstraintDefinition(constraintID, model,
							declareTemplateConstraintTemplateMap.get(key));
					Collection<Parameter> parameters = (declareTemplateConstraintTemplateMap.get(key)).getParameters();
					int h = 0;
					for (Parameter parameter : parameters) {
						ActivityDefinition activityDefinition = model.activityDefinitionWithName(
								parametersOfDiscoveredConstraintsInstantiationsOfCurrentTemplate[k][h]);
						constraintdefinition.addBranch(parameter, activityDefinition);
						Vector<ConstraintDefinition> activityDefinitionsForCurrentConstraint = new Vector<ConstraintDefinition>();
						if (activityConstraintMap.containsKey(activityDefinition)) {
							activityDefinitionsForCurrentConstraint = activityConstraintMap.get(activityDefinition);
						}
						activityDefinitionsForCurrentConstraint.add(constraintdefinition);
						activityConstraintMap.put(activityDefinition, activityDefinitionsForCurrentConstraint);
						h++;
					}
					constraintTemplateMap.put(constraintID, metricsValues.getTemplate());
					constraintParametersMap.put(constraintID, metricsValues.getParameters());
					constraintFormulaMap.put(constraintID, metricsValues.getFormula());
					constraintSupportRuleMap.put(constraintID, metricsValues.getSupportRule());

					/*
					 * if(constraintdefinition.getName().equals("response")&&metricsValues.
					 * getParameters().get(0).startsWith("Nursing")&&
					 * metricsValues.getParameters().get(1).startsWith("First")){
					 * System.out.println("Nursing - First IF: "+metricsValues.getI());
					 * }
					 * if(constraintdefinition.getName().equals("response")&&metricsValues.
					 * getParameters().get(1).startsWith("Nursing")&&
					 * metricsValues.getParameters().get(0).startsWith("First")){
					 * System.out.println("First - Nursing IF: "+metricsValues.getI());
					 * }
					 */

					if (isConfidenceEvaluable(constraintdefinition.getName())) {
						constraintSupportAntecendentMap.put(constraintID, metricsValues.getSuppAntec());
						constraintSupportConsequentMap.put(constraintID, metricsValues.getSupportConseq());
						constraintConfidenceMap.put(constraintID, metricsValues.getConfidence());
						constraintCpirMap.put(constraintID, metricsValues.getCPIR());
						constraintInterestFactorMap.put(constraintID, metricsValues.getI());
					} else {
						constraintSupportAntecendentMap.put(constraintID, Float.MAX_VALUE);
						constraintSupportConsequentMap.put(constraintID, Float.MAX_VALUE);
						constraintConfidenceMap.put(constraintID, Float.MAX_VALUE);
						constraintCpirMap.put(constraintID, Float.MAX_VALUE);
						constraintInterestFactorMap.put(constraintID, Float.MAX_VALUE);
					}
					// model.addConstraintDefiniton(constraintdefinition);
					allDiscoveredConstraints.add(constraintdefinition);
					constraintMetricsMap.put(constraintID, metricsValues);
					if (!trans && !hier) {
						model.addConstraintDefiniton(constraintdefinition);
						constraintvisibleSupportRuleMap.put(constraintdefinition.getId(),
								constraintSupportRuleMap.get(constraintdefinition.getId()));
						constraintvisibleConfidenceMap.put(constraintdefinition.getId(),
								constraintConfidenceMap.get(constraintdefinition.getId()));
						constraintvisibleCpirMap.put(constraintdefinition.getId(),
								constraintCpirMap.get(constraintdefinition.getId()));
						constraintvisibleInterestFactorMap.put(constraintdefinition.getId(),
								constraintInterestFactorMap.get(constraintdefinition.getId()));
					}

				}
			}
		}

		// System.out.println("SESSANTINIIIIIIIIIIIIIIIIIIIIII: "+sessantini);
		// System.out.println("OTTANTINIIIIIIIIIIIIIIIIIIIIII: "+ottantini);
		// System.out.println("CENTINIIIIIIIIIIIIIIIIIIIIII: "+centini);

		visualizationData.setAllActivities(allActivities);
		Vector<ConstraintDefinition> allVisibleConstraints = new Vector<ConstraintDefinition>();

		/*
		 * float minSupport = input.getMinSupport()/100.f-1;
		 * float maxSupport = minSupport;
		 * if(!constraintSupportRuleMap.isEmpty()){
		 * maxSupport = Collections.max(constraintSupportRuleMap.values());
		 * }
		 * maxSupport = maxSupport +1;
		 * float avgSupport = ((minSupport + maxSupport)/2.f);
		 * 
		 * float maxConfidence = 0;
		 * for(Float max : constraintConfidenceMap.values()){
		 * if((max > maxConfidence) && (max < (Float.MAX_VALUE - 10000))){
		 * maxConfidence = max;
		 * }
		 * }
		 * 
		 * float minConfidence = maxConfidence;
		 * if(!constraintConfidenceMap.isEmpty()){
		 * minConfidence = Collections.min(constraintConfidenceMap.values());
		 * }
		 * 
		 * minConfidence = minConfidence -1;
		 * maxConfidence = maxConfidence + 1;
		 * 
		 * float avgConfidence = ((minConfidence + maxConfidence)/2.f);
		 * 
		 * float maxCPIR = 0;
		 * for(Float max : constraintCpirMap.values()){
		 * if((max > maxCPIR) && (max < (Float.MAX_VALUE - 10000))){
		 * maxCPIR = max;
		 * }
		 * }
		 * 
		 * 
		 * float minCPIR = maxCPIR;
		 * if(!constraintCpirMap.isEmpty()){
		 * minCPIR = Collections.min(constraintCpirMap.values());
		 * }
		 * 
		 * minCPIR = minCPIR -1;
		 * maxCPIR = maxCPIR +1;
		 * float avgCPIR = ((minCPIR + maxCPIR)/2.f);
		 * 
		 * float maxInterestFactor = 0;
		 * for(Float max : constraintInterestFactorMap.values()){
		 * if((max > maxInterestFactor) && (max < (Float.MAX_VALUE - 10000))){
		 * maxInterestFactor = max;
		 * }else{
		 * // System.out.println("Max Interest Factor: "+max);
		 * }
		 * }
		 * 
		 * float minInterestFactor = maxInterestFactor;
		 * if(!constraintInterestFactorMap.isEmpty()){
		 * minInterestFactor = Collections.min(constraintInterestFactorMap.values());
		 * }
		 * 
		 * minInterestFactor = minInterestFactor-1;
		 * maxInterestFactor = maxInterestFactor +1;
		 * float avgInterestFactor = ((minInterestFactor + maxInterestFactor)/2.f);
		 */

		Vector<ConstraintDefinition> filteredConstraints = new Vector<ConstraintDefinition>();

		for (ConstraintDefinition cd : allDiscoveredConstraints) {
			if ((constraintSupportRuleMap.get(cd.getId()) >= minSupport) &&
					(constraintConfidenceMap.get(cd.getId()) >= minConfidence) &&
					(constraintCpirMap.get(cd.getId()) >= minCPIR) &&
					(constraintInterestFactorMap.get(cd.getId()) >= minInterestFactor)) {
				visibleConstraintParametersMap.put(cd.getId(), constraintParametersMap.get(cd.getId()));
				filteredConstraints.add(cd);
			}
		}

		HashMap<String, HashMap<String, ConstraintDefinition>> visible = getVisibleCd(
				((float) input.getMinSupport()) / 100, filteredConstraints, constraintSupportRuleMap, templatesToRemove,
				eventsToRemove, eventTypesToRemove);

		if (hier) {
			filteredConstraints = new Vector<ConstraintDefinition>();

			for (String parameters : visible.keySet()) {
				for (String name : visible.get(parameters).keySet()) {
					filteredConstraints.add(visible.get(parameters).get(name));
					visibleConstraintParametersMap.put(visible.get(parameters).get(name).getId(),
							constraintParametersMap.get(visible.get(parameters).get(name).getId()));
				}
			}
		}
		if (trans || hier) {
			transitiveClosureSuccessionConstraints = new Vector<Integer>();
			if (transitiveReduction)
				if (constraintTemplateMap.values().contains(DeclareTemplate.Succession)) {
					getTransitiveClosureSuccessionConstraints(allVisibleConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, constraintTemplateMap);
				}

			transitiveClosureCoexistenceConstraints = new Vector<Integer>();
			if (transitiveReduction)
				if (constraintTemplateMap.values().contains(DeclareTemplate.CoExistence)) {
					getTransitiveClosureCoexistenceConstraints(allVisibleConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, constraintTemplateMap);
				}

			transitiveClosureResponseConstraints = new Vector<Integer>();
			if (transitiveReduction)
				if (constraintTemplateMap.values().contains(DeclareTemplate.Response)) {
					getTransitiveClosureResponseConstraints(allVisibleConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, constraintTemplateMap);
				}

			transitiveClosurePrecedenceConstraints = new Vector<Integer>();
			if (transitiveReduction)
				if (constraintTemplateMap.values().contains(DeclareTemplate.Precedence)) {
					getTransitiveClosurePrecedenceConstraints(allVisibleConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, constraintTemplateMap);
				}

			transitiveClosureRespondedExistenceConstraints = new Vector<Integer>();
			if (transitiveReduction)
				if (constraintTemplateMap.values().contains(DeclareTemplate.Responded_Existence)) {
					getTransitiveClosureRespondedExistenceConstraints(allVisibleConstraints,
							model.getActivityDefinitions(), visibleConstraintParametersMap, constraintTemplateMap);
				}

			transitiveClosureNotCoexistenceConstraints = new Vector<Integer>();
			if (transitiveReduction)
				if (constraintTemplateMap.values().contains(DeclareTemplate.Not_CoExistence)) {
					getTransitiveClosureNotCoexistenceConstraints(allVisibleConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, constraintTemplateMap);
				}

			filteredConstraints = getInterestingConstraints(filteredConstraints);

			for (ConstraintDefinition constraintDefinition : filteredConstraints) {
				if ((!transitiveClosureResponseConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureRespondedExistenceConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosurePrecedenceConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureSuccessionConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureCoexistenceConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureNotCoexistenceConstraints.contains(constraintDefinition.getId()))) {
					// if((constraintSupportRuleMap.get(constraintDefinition.getId())>=avgSupport)
					// &&
					// (constraintConfidenceMap.get(constraintDefinition.getId())>=avgConfidence) &&
					// (constraintCpirMap.get(constraintDefinition.getId())>=avgCPIR) &&
					// (constraintInterestFactorMap.get(constraintDefinition.getId())>=avgInterestFactor)){
					String param = "";
					boolean first = true;
					// model.addConstraintDefiniton(constraintDefinition);
					for (Parameter p : constraintDefinition.getParameters()) {
						if (!first) {
							param = param + ";";
						}
						if (constraintDefinition.getBranches(p).iterator().next() != null) {
							param = param + constraintDefinition.getBranches(p).iterator().next().getName();
						} else {
							param = param + "EMPTY_PARAM";
						}
						first = false;
					}
					if (!hier || (visible.containsKey(param)
							&& visible.get(param).containsKey(constraintDefinition.getName()))) {
						for (Parameter p : constraintDefinition.getParameters()) {
							// adIds.add(constraintDefinition.getBranches(p).iterator().next().getName().replace("<html><body
							// text=404040>","<html>").replace("</body></html>", "</html>"));
							String activityName = "";
							if (constraintDefinition.getBranches(p).iterator().next() != null) {
								activityName = constraintDefinition.getBranches(p).iterator().next().getName();
							}

							if (!adIds.contains(activityName) && !param.contains("EMPTY_PARAM")) {
								adIds.add(activityName);
								// ActivityDefinition act = activitiesMap.get(activityName);
								// activitydefinition = model.addActivityDefinition(act.getId());
								// activitydefinition.setName(activityName);
								// activitySupportMap.put(activityID,
								// f.getSupport(activitydefinition.getName())/100.f);
								// activityID++;
							}
						}
						// }
					}
				}
			}
			for (String ad : activityDefinitions) {
				if (!adIds.contains(ad)) {
					model.deleteActivityDefinition(model.activityDefinitionWithName(ad));
				}
			}

			for (ConstraintDefinition constraintDefinition : filteredConstraints) {
				if ((!transitiveClosureResponseConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureRespondedExistenceConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosurePrecedenceConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureSuccessionConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureCoexistenceConstraints.contains(constraintDefinition.getId()))
						&& (!transitiveClosureNotCoexistenceConstraints.contains(constraintDefinition.getId()))) {
					// if((constraintSupportRuleMap.get(constraintDefinition.getId())>=avgSupport)
					// &&
					//// (constraintConfidenceMap.get(constraintDefinition.getId())>=avgConfidence)
					// &&
					// (constraintCpirMap.get(constraintDefinition.getId())>=avgCPIR) &&
					// (constraintInterestFactorMap.get(constraintDefinition.getId())>=avgInterestFactor)){
					String param = "";
					boolean first = true;
					// model.addConstraintDefiniton(constraintDefinition);
					for (Parameter p : constraintDefinition.getParameters()) {
						if (!first) {
							param = param + ";";
						}
						if (constraintDefinition.getBranches(p).iterator().next() != null) {
							param = param + constraintDefinition.getBranches(p).iterator().next().getName();
						} else {
							param = param + "EMPTY_PARAM";
						}
						first = false;
					}

					if (!hier || (visible.containsKey(param)
							&& visible.get(param).containsKey(constraintDefinition.getName())
							&& !param.contains("EMPTY_PARAM"))) {

						model.addConstraintDefiniton(constraintDefinition);
						constraintvisibleSupportRuleMap.put(constraintDefinition.getId(),
								constraintSupportRuleMap.get(constraintDefinition.getId()));
						constraintvisibleConfidenceMap.put(constraintDefinition.getId(),
								constraintConfidenceMap.get(constraintDefinition.getId()));
						constraintvisibleCpirMap.put(constraintDefinition.getId(),
								constraintCpirMap.get(constraintDefinition.getId()));
						constraintvisibleInterestFactorMap.put(constraintDefinition.getId(),
								constraintInterestFactorMap.get(constraintDefinition.getId()));
					}
				}
				// }
			}

		}
		// AssignmentModelView view = new AssignmentModelView(model);
		AssignmentModelView view = null;
		// Vector<String> adIds = new Vector<String>();

		/*
		 * for(ConstraintDefinition constraintdefinition :
		 * model.getConstraintDefinitions()){
		 * 
		 * // boolean found = false;
		 * // for(Object key : visible.keySet()){
		 * // if((((HashMap)visible.get(key)).containsValue(constraintdefinition))){
		 * // found = true;
		 * // }
		 * // }
		 * // if(!found){
		 * // view.setConstraintDefinitionInvisible(constraintdefinition);
		 * // constraintdefinition.setVisible(false);
		 * // }else{
		 * //
		 * // view.setConstraintDefinitionVisible(constraintdefinition);
		 * // constraintdefinition.setVisible(true);
		 * // for(Parameter p: constraintdefinition.getParameters()){
		 * // adIds.add(constraintdefinition.getBranches(p).iterator().next().getName().
		 * replace("<html><body text=404040>","<html>").replace("</body></html>",
		 * "</html>"));
		 * // }
		 * // }
		 * 
		 * 
		 * 
		 * Color constraintDefinitionColor;
		 * int green =
		 * 255-(int)(255*constraintSupportRuleMap.get(constraintdefinition.getId()));
		 * //System.out.println(constraintSupportRuleMap.get(constraintdefinition.
		 * getName()));
		 * constraintDefinitionColor = new Color(255,green,green);
		 * if(constraintdefinition.getName().contains("alternate")||constraintdefinition
		 * .getName().contains("chain")){
		 * view.setConstraintDefinitionColor(constraintdefinition,Color.black,
		 * constraintDefinitionColor,constraintDefinitionColor);
		 * }else{
		 * view.setConstraintDefinitionColor(constraintdefinition,
		 * constraintDefinitionColor,constraintDefinitionColor,constraintDefinitionColor
		 * );
		 * }
		 * }
		 * Color activityDefinitionColor;
		 * HashMap<Integer, HashMap<String, Float>>
		 * balancingStartCompletePerActivityDefinition = new HashMap<Integer,
		 * HashMap<String, Float>>();
		 * for(ActivityDefinition ad : model.getActivityDefinitions()){
		 * String activityName = ad.getName();
		 * if(input.getAprioriKnowledgeBasedCriteriaSet().contains(
		 * AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)){
		 * if(!hasEventTypeInName(activityName)){
		 * activityName = activityName+"-"+input.getReferenceEventType();
		 * }
		 * }
		 * 
		 * 
		 * int blue = 255-(int)(255*(f.getSupport(activityName)/100.f));
		 * activityDefinitionColor = new Color(255, 255,blue);
		 * ////////////////////////////REMOVE///////////////////////
		 * // activityDefinitionColor = Color.white;
		 * //////////////////////////REMOVE///////////////////////
		 * view.setActivityDefinitionBackground(ad, activityDefinitionColor);
		 * HashMap<String, Float> balanceStartComplete =
		 * getBalance(removeEventTypeFromActivityName(ad.getName()),log);
		 * balancingStartCompletePerActivityDefinition.put(ad.getId(),
		 * balanceStartComplete);
		 * }
		 */

		// for(ActivityDefinition ad: model.getActivityDefinitions()){
		// if(!adIds.contains(ad.getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>"))){
		// view.setActivityDefinitionInvisible(ad);
		// ad.setVisible(false);
		// }else{
		// view.setActivityDefinitionVisible(ad);
		// ad.setVisible(true);
		// }
		// }

		// model.constraintDefinitionsCount();

		outputMap = new DeclareMap(model, null, view, null, null, null);

		visualizationData.setAllDiscoveredConstraints(allDiscoveredConstraints);

		// Integer sessantini = 0;
		// Integer ottantini = 0;
		// Integer centini = 0;
		//
		// for(ConstraintDefinition cd: allDiscoveredConstraints){
		// if((constraintSupportRuleMap.get(cd.getId())>=0.6)){
		// sessantini++;
		// }
		// if((constraintSupportRuleMap.get(cd.getId())>=0.8)){
		// ottantini++;
		// }
		//
		// if((constraintSupportRuleMap.get(cd.getId())>=1)){
		// centini++;
		// }
		// }
		visualizationData.setVisibleconfidence(constraintvisibleConfidenceMap);
		visualizationData.setVisibleCPIR(constraintvisibleCpirMap);
		visualizationData.setVisiblesupportRule(constraintvisibleSupportRuleMap);
		visualizationData.setVisibleI(constraintvisibleInterestFactorMap);

		/*
		 * System.out.println("I am in First Model generation");
		 * for(Integer index: constraintTemplateMap.keySet()){
		 * if(constraintTemplateMap.get(index).equals(DeclareTemplate.Response)){
		 * System.out.println("interest factor"+
		 * constraintParametersMap.get(index)+": "+constraintInterestFactorMap.get(index
		 * ));
		 * }
		 * }
		 */

		visualizationData.setConfidence(constraintConfidenceMap);
		visualizationData.setCPIR(constraintCpirMap);
		visualizationData.setFormula(constraintFormulaMap);
		visualizationData.setI(constraintInterestFactorMap);
		visualizationData.setModel(outputMap);
		visualizationData.setParameters(constraintParametersMap);
		visualizationData.setSuppAntec(constraintSupportAntecendentMap);
		visualizationData.setSupportConseq(constraintSupportConsequentMap);
		visualizationData.setSupportRule(constraintSupportRuleMap);
		visualizationData.setTemplate(constraintTemplateMap);
		visualizationData.setMappingAdCd(activityConstraintMap);
		visualizationData.setActSupp(activitySupportMap);
		// visualizationData.setBlnc(balancingStartCompletePerActivityDefinition);
		visualizationData.setTransitiveClosureResponseConstraints(transitiveClosureResponseConstraints);
		visualizationData
				.setTransitiveClosureRespondedExistenceConstraints(transitiveClosureRespondedExistenceConstraints);
		visualizationData.setTransitiveClosurePrecedenceConstraints(transitiveClosurePrecedenceConstraints);
		visualizationData.setTransitiveClosureSuccessionConstraints(transitiveClosureSuccessionConstraints);
		visualizationData.setTransitiveClosureCoexistenceConstraints(transitiveClosureCoexistenceConstraints);
		visualizationData.setTransitiveClosureCoexistenceConstraints(transitiveClosureNotCoexistenceConstraints);
		visualizationData.setConstraintParametersMap(constraintParametersMap);
		visualizationData.setLog(log);

		Watch timeInformationWatch = new Watch();
		timeInformationWatch.start();
		TemplateInfo templateInfo = null;
		HashMap<Object, double[]> constraintIdTimeInstancesMap = new HashMap<Object, double[]>();
		AssignmentModel assignmentModel = visualizationData.getModel().getModel();
		HashMap<Object, Integer> activationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> fulfillmentsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> violationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> conflictsMap = new HashMap<Object, Integer>();
		HashMap<Object, Long> maxTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> minTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> avgTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> stdDevTimeDistancesMap = new HashMap<Object, Long>();

		XAttributeMap eventAttributeMap;
		boolean timed = input.getDeclarePerspectiveSet().contains(DeclarePerspective.Time) ? true : false;
		if (timed) {
			for (ConstraintDefinition constraintDefinition : assignmentModel.getConstraintDefinitions()) {
				constraintParametersMap.get(constraintDefinition.getId());
				int numberOfActivations = 0;
				int numberOfViolations = 0;
				int numberOfFulfillments = 0;
				int numberOfConflicts = 0;
				Vector<Long> timeDists = new Vector<Long>();
				for (XTrace trace : log) {

					List<Integer> traceIndexes = new LinkedList<Integer>();
					List<String> traceEvents = new LinkedList<String>();
					int i = 0;
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						traceEvents.add((eventAttributeMap.get(XConceptExtension.KEY_NAME) + "-"
								+ eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION)).toLowerCase());
						traceIndexes.add(i);
						i++;
					}
					ExecutionsTree executiontree = new ExecutionsTree(traceEvents, traceIndexes, constraintDefinition);
					Set<Integer> activations = executiontree.getActivations();
					Set<Integer> violations = executiontree.getViolations();
					Set<Integer> fulfillments = executiontree.getFulfillments();
					Set<Integer> conflicts = executiontree.getConflicts();
					switch (visualizationData.getTemplate().get(constraintDefinition.getId())) {
						case Succession:
						case Alternate_Succession:
						case Chain_Succession:
							templateInfo = new SuccessionInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Choice:
							templateInfo = new ChoiceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Exclusive_Choice:
							templateInfo = new ExclusiveChoiceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence:
							templateInfo = new ExistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence2:
							templateInfo = new Existence2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence3:
							templateInfo = new Existence3Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Init:
							templateInfo = new InitInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Absence:
							templateInfo = new AbsenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Absence2:
							templateInfo = new Absence2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Absence3:
							templateInfo = new Absence3Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Exactly1:
							templateInfo = new Exactly1Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Exactly2:
							templateInfo = new Exactly2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Responded_Existence:
							templateInfo = new RespondedExistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Response:
						case Alternate_Response:
						case Chain_Response:
							templateInfo = new ResponseInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Precedence:
						case Alternate_Precedence:
						case Chain_Precedence:
							templateInfo = new PrecedenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case CoExistence:
							templateInfo = new CoexistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Not_CoExistence:
							templateInfo = new NotCoexistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Not_Succession:
						case Not_Chain_Succession:
							templateInfo = new NegativeRelationInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
					}
					numberOfActivations = numberOfActivations + activations.size();
					numberOfViolations = numberOfViolations + violations.size();
					numberOfFulfillments = numberOfFulfillments + fulfillments.size();
					numberOfConflicts = numberOfConflicts + conflicts.size();

				}
				double[] timeInstances = new double[timeDists.size()];
				int timeInstanceIndex = 0;
				for (Long time : timeDists)
					timeInstances[timeInstanceIndex++] = time;

				if (timeDists.size() > 0) {
					maxTimeDistancesMap.put(constraintDefinition.getId(), Collections.max(timeDists));
					minTimeDistancesMap.put(constraintDefinition.getId(), Collections.min(timeDists));
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), timeInstances);
				} else {
					maxTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					minTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), new double[] { -1 });
				}
				double avg = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					avg = avg + timeDists.get(i);
				}
				avg = avg / timeDists.size();
				avgTimeDistancesMap.put(constraintDefinition.getId(), (long) avg);

				double stddev = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					stddev = stddev + ((timeDists.get(i) - avg) * (timeDists.get(i) - avg));
				}
				stddev = stddev / (timeDists.size() - 1);
				stddev = Math.sqrt(stddev);
				stdDevTimeDistancesMap.put(constraintDefinition.getId(), (long) stddev);

				activationsMap.put(constraintDefinition.getId(), numberOfActivations);
				violationsMap.put(constraintDefinition.getId(), numberOfViolations);
				fulfillmentsMap.put(constraintDefinition.getId(), numberOfFulfillments);
				conflictsMap.put(constraintDefinition.getId(), numberOfConflicts);
			}
			// if(pw!=null){
			// pw.println("END TIME INFORMATION EVALUATION - time:
			// "+timeInformationWatch.msecs()+" msecs");
			// }

			visualizationData.setConstraintIdTimeInstancesMap(constraintIdTimeInstancesMap);
			visualizationData.setActivations(activationsMap);
			visualizationData.setViolations(violationsMap);
			visualizationData.setFulfillments(fulfillmentsMap);
			visualizationData.setConflicts(conflictsMap);
			visualizationData.setMaxTD(maxTimeDistancesMap);
			visualizationData.setMinTD(minTimeDistancesMap);
			visualizationData.setAvgTD(avgTimeDistancesMap);
			visualizationData.setStdDevTD(stdDevTimeDistancesMap);
			visualizationData.setExtend(false);
			visualizationData.setVisibleConstraintParametersMap(visibleConstraintParametersMap);
		}
		visualizationData.setInput(input);

		visualizationData.setVisibleConstraintParametersMap(visibleConstraintParametersMap);
		return visualizationData;
	}

	// private boolean allShortCycles(PrimaryGraph p){
	// boolean out = true;
	// while(out){
	// Path cycle = Cycles.findCycle(p);
	// if(cycle.edgeCount()>2){
	// out = false;
	// }
	// }
	// return out;
	// }

	// public void getTransitiveClosureResponseConstraints(
	// Vector<ConstraintDefinition> allDiscoveredConstraints,
	// Iterable<ActivityDefinition> activityDefinitions, HashMap<Integer,
	// List<String>> constraintParametersMap, HashMap<Integer, DeclareTemplate>
	// constraintTemplateMap) {
	// List<List<String>> thereIsAPath = new ArrayList<List<String>>();
	// List<List<String>> alreadyRemoved = new ArrayList<List<String>>();
	// HashMap<String, Vector<String>>
	// activityDefinitionsSuccessorsForTransitiveClosure = new HashMap<String,
	// Vector<String>>();
	// for(ActivityDefinition ad : activityDefinitions){
	//
	// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((ad.getName()))){
	// // transitive closure for response
	// Vector<String> successorsOfad = getResponseSuccessors(ad.getName(),
	// constraintParametersMap, constraintTemplateMap);
	// activityDefinitionsSuccessorsForTransitiveClosure.put(ad.getName(),
	// successorsOfad);
	// for(String successor : successorsOfad){
	// Vector<String> successorsOfsuccessor = null;
	// List<String> param = new ArrayList<String>();
	// param.add(ad.getName());
	// param.add(successor);
	// if(!alreadyRemoved.contains(param)){
	// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((successor))){
	// successorsOfsuccessor = getResponseSuccessors(successor,
	// constraintParametersMap, constraintTemplateMap);
	// activityDefinitionsSuccessorsForTransitiveClosure.put(successor,
	// successorsOfsuccessor);
	// }else{
	// successorsOfsuccessor =
	// activityDefinitionsSuccessorsForTransitiveClosure.get(successor);
	// }
	// for(String successorOfsuccessor : successorsOfsuccessor){
	// param = new ArrayList<String>();
	// param.add(ad.getName());
	// param.add(successorOfsuccessor);
	// ArrayList<String> paramSucc = new ArrayList<String>();
	// paramSucc.add(successor);
	// paramSucc.add(successorOfsuccessor);
	// if((!alreadyRemoved.contains(param)||thereIsAPath.contains(param)) &&
	// (!alreadyRemoved.contains(paramSucc)|| thereIsAPath.contains(paramSucc))){
	// for(Integer constraintId : constraintParametersMap.keySet()){
	// if(constraintParametersMap.get(constraintId).equals(param)&&constraintTemplateMap.get(constraintId).equals(DeclareTemplate.Response)){
	// if(!transitiveClosureResponseConstraints.contains(constraintId)){
	// transitiveClosureResponseConstraints.add(constraintId);
	// }
	// thereIsAPath.add(param);
	// alreadyRemoved.add(param);
	// }
	// }
	// }
	// }
	// }
	// }
	// }else{
	// Vector<String> successorsOfad =
	// activityDefinitionsSuccessorsForTransitiveClosure.get(ad.getName());
	// activityDefinitionsSuccessorsForTransitiveClosure.put(ad.getName(),
	// successorsOfad);
	// for(String successor : successorsOfad){
	// Vector<String> successorsOfsuccessor = null;
	// List<String> param = new ArrayList<String>();
	// param.add(ad.getName());
	// param.add(successor);
	// if(!alreadyRemoved.contains(param)){
	// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((successor))){
	// successorsOfsuccessor = getResponseSuccessors(successor,
	// constraintParametersMap, constraintTemplateMap);
	// activityDefinitionsSuccessorsForTransitiveClosure.put(successor,
	// successorsOfsuccessor);
	// }else{
	// successorsOfsuccessor =
	// activityDefinitionsSuccessorsForTransitiveClosure.get(successor);
	// }
	// for(String successorOfsuccessor : successorsOfsuccessor){
	// param = new ArrayList<String>();
	// param.add(ad.getName());
	// param.add(successorOfsuccessor);
	// ArrayList<String> paramSucc = new ArrayList<String>();
	// paramSucc.add(successor);
	// paramSucc.add(successorOfsuccessor);
	// if(!alreadyRemoved.contains(param) && !alreadyRemoved.contains(paramSucc)){
	// for(Integer constraintId : constraintParametersMap.keySet()){
	// if(constraintParametersMap.get(constraintId).equals(param)&&(!alreadyRemoved.contains(param))&&constraintTemplateMap.get(constraintId).equals(DeclareTemplate.Response)){
	// transitiveClosureResponseConstraints.add(constraintId);
	// alreadyRemoved.add(param);
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// }
	// }
	// }

	public void getTransitiveClosureResponseConstraints(
			Vector<ConstraintDefinition> allDiscoveredConstraints, Iterable<ActivityDefinition> activityDefinitions,
			HashMap<Integer, List<String>> constraintParametersMap,
			HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String, HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		// List<List<String>> simplePaths = new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Response))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Response))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Response))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)
							&& !transitiveClosureSuccessionConstraints.contains(id))) {

				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				// HashSet<String> bluesA = null;
				// HashSet<String> redsB = null;
				// HashSet<String> bluesB = null;
				// HashSet<String> redsA = null;
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				// d.addEdge(constraintParametersMap.get(cd.getId()).get(0),
				// constraintParametersMap.get(cd.getId()).get(1));
				// }

				HashSet<String> descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
			}
		}
		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.Response)) {
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				if (!d.containsEdge(a, b) && constraintTemplateMap.get(id).equals(DeclareTemplate.Response)) {
					transitiveClosureResponseConstraints.add(id);
				}
			}
		}

	}

	public void getTransitiveClosureSuccessionConstraints(
			Vector<ConstraintDefinition> allDiscoveredConstraints, Iterable<ActivityDefinition> activityDefinitions,
			HashMap<Integer, List<String>> constraintParametersMap,
			HashMap<Integer, DeclareTemplate> constraintTemplateMap) {

		HashMap<String, HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		// List<List<String>> simplePaths = new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession))) {

				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				// HashSet<String> bluesA = null;
				// HashSet<String> redsB = null;
				// HashSet<String> bluesB = null;
				// HashSet<String> redsA = null;
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				// d.addEdge(constraintParametersMap.get(cd.getId()).get(0),
				// constraintParametersMap.get(cd.getId()).get(1));
				// }

				HashSet<String> descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
			}
		}
		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)) {
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				if (!d.containsEdge(a, b) && constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)) {
					transitiveClosureSuccessionConstraints.add(id);
				}
			}
		}
		// List<List<String>> alreadyRemoved = new ArrayList<List<String>>();
		// HashMap<String, Vector<String>>
		// activityDefinitionsSuccessorsForTransitiveClosure = new HashMap<String,
		// Vector<String>>();
		// for(ActivityDefinition ad : activityDefinitions){
		//
		// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((ad.getName()))){
		// // transitive closure for response
		// Vector<String> successorsOfad = getSuccessionSuccessors(ad.getName(),
		// constraintParametersMap, constraintTemplateMap);
		// activityDefinitionsSuccessorsForTransitiveClosure.put(ad.getName(),
		// successorsOfad);
		// for(String successor : successorsOfad){
		// Vector<String> successorsOfsuccessor = null;
		// List<String> param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successor);
		// if(!alreadyRemoved.contains(param)){
		// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((successor))){
		// successorsOfsuccessor = getSuccessionSuccessors(successor,
		// constraintParametersMap, constraintTemplateMap);
		// activityDefinitionsSuccessorsForTransitiveClosure.put(successor,
		// successorsOfsuccessor);
		// }else{
		// successorsOfsuccessor =
		// activityDefinitionsSuccessorsForTransitiveClosure.get(successor);
		// }
		// for(String successorOfsuccessor : successorsOfsuccessor){
		// param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successorOfsuccessor);
		// ArrayList<String> paramSucc = new ArrayList<String>();
		// paramSucc.add(successor);
		// paramSucc.add(successorOfsuccessor);
		// if(!alreadyRemoved.contains(param) && !alreadyRemoved.contains(paramSucc)){
		// for(Integer constraintId : constraintParametersMap.keySet()){
		// if(constraintParametersMap.get(constraintId).equals(param)&&constraintTemplateMap.get(constraintId).equals(DeclareTemplate.Succession)){
		// transitiveClosureSuccessionConstraints.add(constraintId);
		// alreadyRemoved.add(param);
		// }
		// }
		// }
		// }
		// }
		// }
		// }else{
		// Vector<String> successorsOfad =
		// activityDefinitionsSuccessorsForTransitiveClosure.get(ad.getName());
		// activityDefinitionsSuccessorsForTransitiveClosure.put(ad.getName(),
		// successorsOfad);
		// for(String successor : successorsOfad){
		// Vector<String> successorsOfsuccessor = null;
		// List<String> param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successor);
		// if(!alreadyRemoved.contains(param)){
		// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((successor))){
		// successorsOfsuccessor = getSuccessionSuccessors(successor,
		// constraintParametersMap, constraintTemplateMap);
		// activityDefinitionsSuccessorsForTransitiveClosure.put(successor,
		// successorsOfsuccessor);
		// }else{
		// successorsOfsuccessor =
		// activityDefinitionsSuccessorsForTransitiveClosure.get(successor);
		// }
		// for(String successorOfsuccessor : successorsOfsuccessor){
		// param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successorOfsuccessor);
		// ArrayList<String> paramSucc = new ArrayList<String>();
		// paramSucc.add(successor);
		// paramSucc.add(successorOfsuccessor);
		// if(!alreadyRemoved.contains(param) && !alreadyRemoved.contains(paramSucc)){
		// for(Integer constraintId : constraintParametersMap.keySet()){
		// if(constraintParametersMap.get(constraintId).equals(param)&&(!alreadyRemoved.contains(param))&&constraintTemplateMap.get(constraintId).equals(DeclareTemplate.Succession)){
		// transitiveClosureSuccessionConstraints.add(constraintId);
		// alreadyRemoved.add(param);
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// }
		// }

		// transitiveClosureSuccessionConstraints = new Vector<Integer>();
		// pruneNegatives(); pruneNegatives(); pruneNegatives();
	}

	public void getTransitiveClosureCoexistenceConstraints(
			Vector<ConstraintDefinition> allDiscoveredConstraints, Iterable<ActivityDefinition> activityDefinitions,
			HashMap<Integer, List<String>> constraintParametersMap,
			HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String, HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		// List<List<String>> simplePaths = new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		String a = null;
		String b = null;
		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)
							&& !transitiveClosureCoexistenceConstraints.contains(id))
					|| constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)
							&& !transitiveClosureSuccessionConstraints.contains(id)) {
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);

				HashSet<String> descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
				a = constraintParametersMap.get(id).get(1);
				b = constraintParametersMap.get(id).get(0);

				descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
			}
		}
		Vector<String> added = new Vector<String>();
		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)) {
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);
				if (!d.containsEdge(a, b) && constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)) {
					transitiveClosureCoexistenceConstraints.add(id);
				}
			}
		}
		// transitiveClosureCoexistenceConstraints = new Vector<Integer>();
	}

	public void getTransitiveClosurePrecedenceConstraints(
			Vector<ConstraintDefinition> allDiscoveredConstraints, Iterable<ActivityDefinition> activityDefinitions,
			HashMap<Integer, List<String>> constraintParametersMap,
			HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String, HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		// List<List<String>> simplePaths = new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Precedence))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Precedence))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)
							&& !transitiveClosureSuccessionConstraints.contains(id))) {

				String a = constraintParametersMap.get(id).get(1);
				String b = constraintParametersMap.get(id).get(0);
				// HashSet<String> bluesA = null;
				// HashSet<String> redsB = null;
				// HashSet<String> bluesB = null;
				// HashSet<String> redsA = null;
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				// d.addEdge(constraintParametersMap.get(cd.getId()).get(0),
				// constraintParametersMap.get(cd.getId()).get(1));
				// }

				HashSet<String> descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
			}
		}
		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)) {
				String a = constraintParametersMap.get(id).get(1);
				String b = constraintParametersMap.get(id).get(0);
				if (!d.containsEdge(a, b) && constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)) {
					transitiveClosurePrecedenceConstraints.add(id);
				}
			}
		}

	}

	public void getTransitiveClosureNotCoexistenceConstraints(
			Vector<ConstraintDefinition> allDiscoveredConstraints, Iterable<ActivityDefinition> activityDefinitions,
			HashMap<Integer, List<String>> constraintParametersMap,
			HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		// HashMap<String,ArrayList<String>> negativeConnections = new HashMap<String,
		// ArrayList<String>>();

		DefaultDirectedGraph<String, DefaultEdge> coexistenceDiagram = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		DefaultDirectedGraph<String, DefaultEdge> notCoexistenceDiagram = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);

		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};

		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence))) {
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				// System.out.println(d.toString());
				notCoexistenceDiagram.addVertex(a);
				notCoexistenceDiagram.addVertex(b);
				// if(DijkstraShortestPath.findPathBetween(notCoexistenceDiagram, a, b)==null){
				DefaultEdge de = ef.createEdge(a, b);
				notCoexistenceDiagram.addEdge(a, b, de);

				DefaultEdge ed = ef.createEdge(b, a);
				notCoexistenceDiagram.addEdge(b, a, ed);

				// d.addEdge(a, b);
				// }
			}

		}

		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)
							&& !transitiveClosureCoexistenceConstraints.contains(id))
					||
					constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)
							&& !transitiveClosureSuccessionConstraints.contains(id)) {
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				// System.out.println(d.toString());
				coexistenceDiagram.addVertex(a);
				coexistenceDiagram.addVertex(b);
				// if(DijkstraShortestPath.findPathBetween(coexistenceDiagram, a, b)==null){
				DefaultEdge de = ef.createEdge(a, b);
				coexistenceDiagram.addEdge(a, b, de);

				DefaultEdge ed = ef.createEdge(b, a);
				coexistenceDiagram.addEdge(b, a, ed);

				// d.addEdge(a, b);
				// }
			}

		}
		HashMap<String, ArrayList<String>> coexistencePaths = new HashMap<String, ArrayList<String>>();
		ArrayList<ArrayList<String>> alreadyRemoved = new ArrayList<ArrayList<String>>();
		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence)) {
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				coexistenceDiagram.addVertex(a);

				ArrayList<String> coexistencePathsFromA = coexistencePaths.get(a);
				if (!coexistencePaths.containsKey(a)) {
					coexistencePathsFromA = new ArrayList<String>();
					coexistencePathsFromA.add(a);
					for (String node : coexistenceDiagram.vertexSet()) {
						if (DijkstraShortestPath.findPathBetween(coexistenceDiagram, a, node) != null) {
							coexistencePathsFromA.add(node);
						}
					}
					coexistencePaths.put(a, coexistencePathsFromA);
				}
				coexistenceDiagram.addVertex(b);
				ArrayList<String> coexistencePathsFromB = coexistencePaths.get(b);
				if (!coexistencePaths.containsKey(b)) {
					coexistencePathsFromB = new ArrayList<String>();
					coexistencePathsFromB.add(b);
					for (String node : coexistenceDiagram.vertexSet()) {
						if (DijkstraShortestPath.findPathBetween(coexistenceDiagram, b, node) != null) {
							coexistencePathsFromB.add(node);
						}
					}
					coexistencePaths.put(b, coexistencePathsFromB);
				}
			}
		}

		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.Not_CoExistence)) {
				String a = constraintParametersMap.get(id).get(0);
				String b = constraintParametersMap.get(id).get(1);
				ArrayList<String> currentEdge = new ArrayList<String>();
				currentEdge.add(a);
				currentEdge.add(b);
				boolean removed = false;
				if (!alreadyRemoved.contains(currentEdge)) {
					if (coexistencePaths.get(a) != null) {
						for (String reachableNodeFromA : coexistencePaths.get(a)) {
							if (coexistencePaths.get(b) != null) {
								for (String reachableNodeFromB : coexistencePaths.get(b)) {
									ArrayList<String> reachedEdge = new ArrayList<String>();
									reachedEdge.add(a);
									reachedEdge.add(b);
									ArrayList<String> invreachedEdge = new ArrayList<String>();
									invreachedEdge.add(b);
									invreachedEdge.add(a);
									ArrayList<String> currentNegEdge = new ArrayList<String>();
									currentNegEdge.add(reachableNodeFromA);
									currentNegEdge.add(reachableNodeFromB);
									if (!reachableNodeFromA.equals(a) || !reachableNodeFromB.equals(b)) {
										if (notCoexistenceDiagram.containsEdge(reachableNodeFromA, reachableNodeFromB)
												&& !alreadyRemoved.contains(currentNegEdge)) {
											transitiveClosureNotCoexistenceConstraints.add(id);
											alreadyRemoved.add(reachedEdge);
											alreadyRemoved.add(invreachedEdge);
											removed = true;
											break;
										}
									}
								}
								if (removed) {
									break;
								}

							}
						}
					}
				}
			}
		}
	}

	public void getTransitiveClosureRespondedExistenceConstraints(
			Vector<ConstraintDefinition> allDiscoveredConstraints, Iterable<ActivityDefinition> activityDefinitions,
			HashMap<Integer, List<String>> constraintParametersMap,
			HashMap<Integer, DeclareTemplate> constraintTemplateMap) {
		HashMap<String, HashSet<String>> ancestorsMap = new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<String>> descendantsMap = new HashMap<String, HashSet<String>>();
		// List<List<String>> simplePaths = new ArrayList<List<String>>();
		DefaultDirectedGraph<String, DefaultEdge> d = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		EdgeFactory<String, DefaultEdge> ef = new EdgeFactory<String, DefaultEdge>() {

			public DefaultEdge createEdge(String arg0, String arg1) {
				// TODO Auto-generated method stub
				DefaultEdge edge = new DefaultEdge();
				edge.setSource(arg0);
				edge.setTarget(arg1);
				return edge;
			}
		};
		String a = null;
		String b = null;
		for (Integer id : constraintParametersMap.keySet()) {
			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Response)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Precedence)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Responded_Existence)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Response)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Precedence)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Response)
							&& !transitiveClosureResponseConstraints.contains(id))
					|| (constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)
							&& !transitiveClosurePrecedenceConstraints.contains(id))) {
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);

				if (constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)) {
					a = constraintParametersMap.get(id).get(1);
					b = constraintParametersMap.get(id).get(0);
				}

				HashSet<String> descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
			}

			if ((constraintTemplateMap.get(id).equals(DeclareTemplate.Alternate_Succession)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.Chain_Succession)) ||
					(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)
							&& !transitiveClosureCoexistenceConstraints.contains(id))
					|| constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)
							&& !transitiveClosureSuccessionConstraints.contains(id)) {
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);

				HashSet<String> descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				HashSet<String> ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}
				a = constraintParametersMap.get(id).get(1);
				b = constraintParametersMap.get(id).get(0);

				descendantsA = descendantsMap.get(a);
				if (descendantsA == null) {
					descendantsA = new HashSet<String>();
				}
				if (descendantsMap.get(b) != null) {
					descendantsA.addAll(descendantsMap.get(b));
					descendantsA.add(b);
					descendantsA.add(a);
				} else {
					descendantsA.add(b);
					descendantsA.add(a);
				}
				descendantsMap.put(a, descendantsA);

				if (ancestorsMap.get(a) != null) {
					for (String ancestor : ancestorsMap.get(a)) {
						HashSet<String> descendants = descendantsMap.get(ancestor);
						if (descendantsMap.get(b) != null) {
							descendants.addAll(descendantsMap.get(b));
							descendants.add(b);
						} else {
							descendants.add(b);
						}
						descendantsMap.put(ancestor, descendants);
					}
				}

				ancestorsB = ancestorsMap.get(b);
				if (ancestorsB == null) {
					ancestorsB = new HashSet<String>();
				}

				if (ancestorsMap.get(a) != null) {
					ancestorsB.addAll(ancestorsMap.get(a));
					ancestorsB.add(a);
					ancestorsB.add(b);
				} else {
					ancestorsB.add(a);
					ancestorsB.add(b);
				}
				ancestorsMap.put(b, ancestorsB);

				if (descendantsMap.get(b) != null) {
					for (String descendant : descendantsMap.get(b)) {
						HashSet<String> ancestors = ancestorsMap.get(descendant);
						if (ancestorsMap.get(a) != null) {
							ancestors.addAll(ancestorsMap.get(a));
							ancestors.add(a);
						} else {
							ancestors.add(a);
						}
						ancestorsMap.put(descendant, ancestors);
					}
				}

				// System.out.println(d.toString());
				d.addVertex(a);
				d.addVertex(b);
				if (DijkstraShortestPath.findPathBetween(d, a, b) == null) {
					DefaultEdge de = ef.createEdge(a, b);
					d.addEdge(a, b, de);
					HashSet<String> reds = new HashSet<String>();
					// boolean isolated = true;
					if (ancestorsMap.get(a) != null) {
						reds.addAll(ancestorsMap.get(a));
						// isolated = false;
					} else {
						reds.add(a);
						ancestorsMap.put(a, reds);
					}
					HashSet<String> blues = new HashSet<String>();
					if (descendantsMap.get(b) != null) {
						blues.addAll(descendantsMap.get(b));
					} else {
						// if(!isolated){
						// blues.add(b);
						// }
						blues.add(b);
						descendantsMap.put(b, blues);
					}
					for (String red : reds) {
						for (String blue : blues) {
							if (!red.equals(a) || !blue.equals(b)) {
								if (d.containsEdge(red, blue)) {
									List<DefaultEdge> redA = DijkstraShortestPath.findPathBetween(d, red, a);
									List<DefaultEdge> bBlue = DijkstraShortestPath.findPathBetween(d, b, blue);
									if (redA != null && bBlue != null) {
										redA.add(d.getEdge(a, b));
										redA.addAll(bBlue);
										boolean shortest = true;
										Vector<String> nodes = new Vector<String>();
										for (DefaultEdge edge : redA) {
											if (!nodes.contains(edge.getSource())
													&& !nodes.contains(edge.getTarget())) {
												nodes.add((String) edge.getSource());
											} else {
												shortest = false;
												break;
											}
										}

										if (shortest) {
											d.removeEdge(red, blue);
										}
									}
								}
							}
						}
					}
					// d.addEdge(a, b);
				}

				// HashSet<String> bluesA = null;
				// HashSet<String> redsB = null;
				// HashSet<String> bluesB = null;
				// HashSet<String> redsA = null;
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(0));
				// d.addVertex(constraintParametersMap.get(cd.getId()).get(1));
				// d.addEdge(constraintParametersMap.get(cd.getId()).get(0),
				// constraintParametersMap.get(cd.getId()).get(1));
				// }

			}
		}
		for (Integer id : constraintParametersMap.keySet()) {
			if (constraintTemplateMap.get(id).equals(DeclareTemplate.Responded_Existence)) {
				a = constraintParametersMap.get(id).get(0);
				b = constraintParametersMap.get(id).get(1);
				if (!d.containsEdge(a, b)
						&& constraintTemplateMap.get(id).equals(DeclareTemplate.Responded_Existence)) {
					transitiveClosureRespondedExistenceConstraints.add(id);
				}
			}
		}

		// List<List<String>> alreadyRemoved = new ArrayList<List<String>>();
		// HashMap<String, Vector<String>>
		// activityDefinitionsSuccessorsForTransitiveClosure = new HashMap<String,
		// Vector<String>>();
		// for(ActivityDefinition ad : activityDefinitions){
		//
		// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((ad.getName()))){
		// // transitive closure for response
		// Vector<String> successorsOfad = getRespondedExistenceSuccessors(ad.getName(),
		// constraintParametersMap, constraintTemplateMap);
		// activityDefinitionsSuccessorsForTransitiveClosure.put(ad.getName(),
		// successorsOfad);
		// for(String successor : successorsOfad){
		// Vector<String> successorsOfsuccessor = null;
		// List<String> param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successor);
		// if(!alreadyRemoved.contains(param)){
		// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((successor))){
		// successorsOfsuccessor = getRespondedExistenceSuccessors(successor,
		// constraintParametersMap, constraintTemplateMap);
		// activityDefinitionsSuccessorsForTransitiveClosure.put(successor,
		// successorsOfsuccessor);
		// }else{
		// successorsOfsuccessor =
		// activityDefinitionsSuccessorsForTransitiveClosure.get(successor);
		// }
		// for(String successorOfsuccessor : successorsOfsuccessor){
		// param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successorOfsuccessor);
		// ArrayList<String> paramSucc = new ArrayList<String>();
		// paramSucc.add(successor);
		// paramSucc.add(successorOfsuccessor);
		// if(!alreadyRemoved.contains(param) && !alreadyRemoved.contains(paramSucc)){
		// for(Integer constraintId : constraintParametersMap.keySet()){
		// if(constraintParametersMap.get(constraintId).equals(param)&&constraintTemplateMap.get(constraintId).equals(DeclareTemplate.Responded_Existence)){
		// transitiveClosureRespondedExistenceConstraints.add(constraintId);
		// alreadyRemoved.add(param);
		// }
		// }
		// }
		// }
		// }
		// }
		// }else{
		// Vector<String> successorsOfad =
		// activityDefinitionsSuccessorsForTransitiveClosure.get(ad.getName());
		// activityDefinitionsSuccessorsForTransitiveClosure.put(ad.getName(),
		// successorsOfad);
		// for(String successor : successorsOfad){
		// Vector<String> successorsOfsuccessor = null;
		// List<String> param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successor);
		// if(!alreadyRemoved.contains(param)){
		// if(!activityDefinitionsSuccessorsForTransitiveClosure.containsKey((successor))){
		// successorsOfsuccessor = getRespondedExistenceSuccessors(successor,
		// constraintParametersMap, constraintTemplateMap);
		// activityDefinitionsSuccessorsForTransitiveClosure.put(successor,
		// successorsOfsuccessor);
		// }else{
		// successorsOfsuccessor =
		// activityDefinitionsSuccessorsForTransitiveClosure.get(successor);
		// }
		// for(String successorOfsuccessor : successorsOfsuccessor){
		// param = new ArrayList<String>();
		// param.add(ad.getName());
		// param.add(successorOfsuccessor);
		// ArrayList<String> paramSucc = new ArrayList<String>();
		// paramSucc.add(successor);
		// paramSucc.add(successorOfsuccessor);
		// if(!alreadyRemoved.contains(param) && !alreadyRemoved.contains(paramSucc)){
		// for(Integer constraintId : constraintParametersMap.keySet()){
		// if(constraintParametersMap.get(constraintId).equals(param)&&(!alreadyRemoved.contains(param))&&constraintTemplateMap.get(constraintId).equals(DeclareTemplate.Responded_Existence)){
		// transitiveClosureRespondedExistenceConstraints.add(constraintId);
		// alreadyRemoved.add(param);
		// }
		// }
		// }
		// }
		// }
		// }
		//
		// }
		// }

		// transitiveClosureRespondedExistenceConstraints = new Vector<Integer>();
	}

	// private Vector<String> getCoexistenceSuccessors(String activityName,
	// HashMap<Integer, List<String>> constraintParametersMap, HashMap<Integer,
	// DeclareTemplate> constraintTemplateMap) {
	// Vector<String>output = new Vector<String>();
	// for(Integer id : constraintParametersMap.keySet()){
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) &&
	// !transitiveClosureSuccessionConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// }
	// return output;
	// }

	// private Vector<String> getRespondedExistenceSuccessors(String activityName,
	// HashMap<Integer, List<String>> constraintParametersMap, HashMap<Integer,
	// DeclareTemplate> constraintTemplateMap) {
	// Vector<String>output = new Vector<String>();
	// for(Integer id : constraintParametersMap.keySet()){
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Responded_Existence)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.CoExistence)&&
	// !transitiveClosureCoexistenceConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) &&
	// !transitiveClosureSuccessionConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Response) &&
	// !transitiveClosureResponseConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence) &&
	// !transitiveClosurePrecedenceConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// }
	// return output;
	// }

	// private Vector<String> getResponseSuccessors(String activityName,
	// HashMap<Integer, List<String>> constraintParametersMap, HashMap<Integer,
	// DeclareTemplate> constraintTemplateMap) {
	// Vector<String>output = new Vector<String>();
	// for(Integer id : constraintParametersMap.keySet()){
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Response)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) &&
	// !transitiveClosureSuccessionConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// }
	// }
	// return output;
	//
	// }

	// private Vector<String> getSuccessionSuccessors(String activityName,
	// HashMap<Integer, List<String>> constraintParametersMap, HashMap<Integer,
	// DeclareTemplate> constraintTemplateMap) {
	// Vector<String>output = new Vector<String>();
	// for(Integer id : constraintParametersMap.keySet()){
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession)){
	// if(constraintParametersMap.get(id).get(0).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(1));
	// }
	// }
	// }
	// return output;
	// }

	// private Vector<String> getPrecedenceSuccessors(String activityName,
	// HashMap<Integer, List<String>> constraintParametersMap, HashMap<Integer,
	// DeclareTemplate> constraintTemplateMap) {
	// Vector<String> output = new Vector<String>();
	// for(Integer id : constraintParametersMap.keySet()){
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Precedence)){
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// if(constraintTemplateMap.get(id).equals(DeclareTemplate.Succession) &&
	// !transitiveClosureSuccessionConstraints.contains(id)){
	// if(constraintParametersMap.get(id).get(1).equals(activityName)){
	// output.add(constraintParametersMap.get(id).get(0));
	// }
	// }
	// }
	// return output;
	// }

	public DeclareMinerOutput createModel(boolean hier, boolean trans, DeclareMinerOutput output, float minSupport,
			float minConfidence, float minCPIR, float minInterestFactor, XLog log, DeclareMinerInput input,
			Vector<String> templatesToRemove /* templates */, Vector<String> eventsToRemove,
			Vector<String> eventTypesToRemove, boolean extension) {
		transitiveReduction = trans;
		InputStream ir = getClass().getResourceAsStream("/resources/template.xml");
		File language = null;
		try {
			language = File.createTempFile("template", ".xml");
			BufferedReader br = new BufferedReader(new InputStreamReader(ir));
			String line = br.readLine();
			PrintStream out = new PrintStream(language);
			while (line != null) {
				out.println(line);
				line = br.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// HashMap<Integer, MetricsValues> constraintMetricsMap = new HashMap<Integer,
		// MetricsValues>();
		TemplateBroker template = XMLBrokerFactory.newTemplateBroker(language.getAbsolutePath());
		List<Language> languages = template.readLanguages();
		Language lang = languages.get(0);
		AssignmentModel model = new AssignmentModel(lang);
		model.setName("new model");

		HashMap<Integer, Float> constraintvisibleSupportRuleMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleConfidenceMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleCpirMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintvisibleInterestFactorMap = new HashMap<Integer, Float>();

		ActivityDefinition newActivityDefinition = null;
		for (Integer key : output.getAllActivities().keySet()) {
			newActivityDefinition = model.addActivityDefinition(key);
			newActivityDefinition.setName(output.getAllActivities().get(key));
		}

		Vector<ConstraintDefinition> allDiscoveredConstraints = output.getAllDiscoveredConstraints();

		Vector<ConstraintDefinition> filteredConstraints = new Vector<ConstraintDefinition>();

		HashMap<Integer, List<String>> constraintParametersMap = output.getConstraintParametersMap();

		HashMap<Integer, List<String>> visibleConstraintParametersMap = new HashMap<Integer, List<String>>();

		for (ConstraintDefinition cd : allDiscoveredConstraints) {
			if ((output.getSupportRule().get(cd.getId()) >= minSupport) &&
					(output.getConfidence().get(cd.getId()) >= minConfidence) &&
					(output.getCPIR().get(cd.getId()) >= minCPIR) &&
					(output.getI().get(cd.getId()) >= minInterestFactor)) {
				filteredConstraints.add(cd);
			}
		}

		HashMap<Integer, Float> constraintSupportRuleMap = output.getSupportRule();

		HashMap<String, HashMap<String, ConstraintDefinition>> visible = getVisibleCd(
				((float) input.getMinSupport()) / 100, filteredConstraints, constraintSupportRuleMap, templatesToRemove,
				eventsToRemove, eventTypesToRemove);
		if (hier) {

			filteredConstraints = new Vector<ConstraintDefinition>();

			for (String parameters : visible.keySet()) {
				for (String name : visible.get(parameters).keySet()) {
					filteredConstraints.add(visible.get(parameters).get(name));
					visibleConstraintParametersMap.put(visible.get(parameters).get(name).getId(),
							constraintParametersMap.get(visible.get(parameters).get(name).getId()));
				}
			}
		}
		transitiveClosureSuccessionConstraints = new Vector<Integer>();

		if (transitiveReduction)
			if (!extension) {
				if (output.getTemplate().values().contains(DeclareTemplate.Succession)) {
					getTransitiveClosureSuccessionConstraints(allDiscoveredConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, output.getTemplate());
				}
			}

		transitiveClosureCoexistenceConstraints = new Vector<Integer>();
		if (transitiveReduction)
			if (!extension) {
				if (output.getTemplate().values().contains(DeclareTemplate.CoExistence)) {
					getTransitiveClosureCoexistenceConstraints(allDiscoveredConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, output.getTemplate());
				}
			}

		transitiveClosureResponseConstraints = new Vector<Integer>();
		if (transitiveReduction)
			if (!extension) {
				if (output.getTemplate().values().contains(DeclareTemplate.Response)) {
					getTransitiveClosureResponseConstraints(allDiscoveredConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, output.getTemplate());
				}
			}

		transitiveClosurePrecedenceConstraints = new Vector<Integer>();
		if (transitiveReduction)
			if (!extension) {
				if (output.getTemplate().values().contains(DeclareTemplate.Precedence)) {
					getTransitiveClosurePrecedenceConstraints(allDiscoveredConstraints, model.getActivityDefinitions(),
							visibleConstraintParametersMap, output.getTemplate());
				}
			}

		transitiveClosureRespondedExistenceConstraints = new Vector<Integer>();
		if (transitiveReduction)
			if (!extension) {
				if (output.getTemplate().values().contains(DeclareTemplate.Responded_Existence)) {
					getTransitiveClosureRespondedExistenceConstraints(allDiscoveredConstraints,
							model.getActivityDefinitions(), visibleConstraintParametersMap, output.getTemplate());
				}
			}

		transitiveClosureNotCoexistenceConstraints = new Vector<Integer>();
		if (transitiveReduction)
			if (!extension) {
				if (output.getTemplate().values().contains(DeclareTemplate.Not_CoExistence)) {
					getTransitiveClosureNotCoexistenceConstraints(allDiscoveredConstraints,
							model.getActivityDefinitions(), visibleConstraintParametersMap, output.getTemplate());
				}
			}

		filteredConstraints = getInterestingConstraints(filteredConstraints);

		Vector<String> adIds = new Vector<String>();
		// int constraintID = 0;
		// int activityID = 1;
		for (ConstraintDefinition constraintDefinition : filteredConstraints) {
			if ((!transitiveClosureResponseConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureRespondedExistenceConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosurePrecedenceConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureSuccessionConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureCoexistenceConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureNotCoexistenceConstraints.contains(constraintDefinition.getId()))) {
				// if((output.getSupportRule().get(constraintDefinition.getId())>=minSupport) &&
				// (output.getConfidence().get(constraintDefinition.getId())>=minConfidence) &&
				// (output.getCPIR().get(constraintDefinition.getId())>=minCPIR) &&
				// (output.getI().get(constraintDefinition.getId())>=minInterestFactor)){
				String param = "";
				boolean first = true;
				// model.addConstraintDefiniton(constraintDefinition);
				for (Parameter p : constraintDefinition.getParameters()) {
					if (!first) {
						param = param + ";";
					}
					if (constraintDefinition.getBranches(p).iterator().next() != null) {
						param = param + constraintDefinition.getBranches(p).iterator().next().getName();
					} else {
						param = param + "EMPTY_PARAM";
					}
					first = false;
				}
				if (!hier || (visible.containsKey(param)
						&& visible.get(param).containsKey(constraintDefinition.getName()))) {
					for (Parameter p : constraintDefinition.getParameters()) {
						// adIds.add(constraintDefinition.getBranches(p).iterator().next().getName().replace("<html><body
						// text=404040>","<html>").replace("</body></html>", "</html>"));
						String activityName = "";
						if (constraintDefinition.getBranches(p).iterator().next() != null) {
							activityName = constraintDefinition.getBranches(p).iterator().next().getName();
						}

						if (!adIds.contains(activityName) && !param.contains("EMPTY_PARAM")) {
							adIds.add(activityName);
							// ActivityDefinition act = activitiesMap.get(activityName);
							// activitydefinition = model.addActivityDefinition(act.getId());
							// activitydefinition.setName(activityName);
							// activitySupportMap.put(activityID,
							// f.getSupport(activitydefinition.getName())/100.f);
							// activityID++;
						}
					}
					// }
				}
			}
		}

		for (Integer key : output.getAllActivities().keySet()) {
			if (!adIds.contains(output.getAllActivities().get(key))) {
				model.deleteActivityDefinition(model.activityDefinitionWithName(output.getAllActivities().get(key)));
			}
		}

		for (ConstraintDefinition constraintDefinition : filteredConstraints) {
			if ((!transitiveClosureResponseConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureRespondedExistenceConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosurePrecedenceConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureSuccessionConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureCoexistenceConstraints.contains(constraintDefinition.getId()))
					&& (!transitiveClosureNotCoexistenceConstraints.contains(constraintDefinition.getId()))) {
				// if((output.getSupportRule().get(constraintDefinition.getId())>=minSupport) &&
				// (output.getConfidence().get(constraintDefinition.getId())>=minConfidence) &&
				// (output.getCPIR().get(constraintDefinition.getId())>=minCPIR) &&
				// (output.getI().get(constraintDefinition.getId())>=minInterestFactor)){

				String param = "";
				boolean first = true;
				// model.addConstraintDefiniton(constraintDefinition);
				for (Parameter p : constraintDefinition.getParameters()) {
					if (!first) {
						param = param + ";";
					}
					if (constraintDefinition.getBranches(p).iterator().next() != null) {
						param = param + constraintDefinition.getBranches(p).iterator().next().getName();
					} else {
						param = param + "EMPTY_PARAM";
					}
					first = false;
				}

				if (!hier
						|| (visible.containsKey(param) && visible.get(param).containsKey(constraintDefinition.getName())
								&& !param.contains("EMPTY_PARAM"))) {

					model.addConstraintDefiniton(constraintDefinition);
					constraintvisibleSupportRuleMap.put(constraintDefinition.getId(),
							output.getSupportRule().get(constraintDefinition.getId()));
					constraintvisibleConfidenceMap.put(constraintDefinition.getId(),
							output.getConfidence().get(constraintDefinition.getId()));
					constraintvisibleCpirMap.put(constraintDefinition.getId(),
							output.getCPIR().get(constraintDefinition.getId()));
					constraintvisibleInterestFactorMap.put(constraintDefinition.getId(),
							output.getI().get(constraintDefinition.getId()));
				}
			}
			// }
		}

		AssignmentModelView view = new AssignmentModelView(model);
		// Vector<String> adIds = new Vector<String>();

		for (ConstraintDefinition constraintdefinition : model.getConstraintDefinitions()) {

			// boolean found = false;
			// for(Object key : visible.keySet()){
			// if((((HashMap)visible.get(key)).containsValue(constraintdefinition))){
			// found = true;
			// }
			// }
			// if(!found){
			// view.setConstraintDefinitionInvisible(constraintdefinition);
			// constraintdefinition.setVisible(false);
			// }else{
			//
			// view.setConstraintDefinitionVisible(constraintdefinition);
			// constraintdefinition.setVisible(true);
			// for(Parameter p: constraintdefinition.getParameters()){
			// adIds.add(constraintdefinition.getBranches(p).iterator().next().getName().replace("<html><body
			// text=404040>","<html>").replace("</body></html>", "</html>"));
			// }
			// }

			Color constraintDefinitionColor;
			int green = 255 - (int) (255 * constraintSupportRuleMap.get(constraintdefinition.getId()));
			constraintDefinitionColor = new Color(255, green, green);
			if (constraintdefinition.getName().contains("alternate")
					|| constraintdefinition.getName().contains("chain")) {
				view.setConstraintDefinitionColor(constraintdefinition, Color.black, constraintDefinitionColor,
						constraintDefinitionColor);
			} else {
				view.setConstraintDefinitionColor(constraintdefinition, constraintDefinitionColor,
						constraintDefinitionColor, constraintDefinitionColor);
			}
		}
		// Set<AprioriKnowledgeBasedCriteria> apriori = new
		// HashSet<AprioriKnowledgeBasedCriteria>();
		// apriori.add(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
		// input.setAprioriKnowledgeBasedCriteriaSet(apriori);
		FindItemSets f = new FindItemSets(log, input);
		Color activityDefinitionColor;
		HashMap<Integer, HashMap<String, Float>> balancingStartCompletePerActivityDefinition = new HashMap<Integer, HashMap<String, Float>>();
		for (ActivityDefinition ad : model.getActivityDefinitions()) {
			String activityName = ad.getName();
			if (input.getAprioriKnowledgeBasedCriteriaSet()
					.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
				if (!hasEventTypeInName(activityName)) {
					activityName = activityName + "-" + input.getReferenceEventType();
				}
			}

			int blue = 255 - (int) (255 * (f.getSupport(activityName) / 100.f));
			activityDefinitionColor = new Color(255, 255, blue);
			//////////////////////////// REMOVE///////////////////////
			// activityDefinitionColor = Color.white;
			////////////////////////// REMOVE///////////////////////
			view.setActivityDefinitionBackground(ad, activityDefinitionColor);
			HashMap<String, Float> balanceStartComplete = getBalance(removeEventTypeFromActivityName(ad.getName()),
					log);
			balancingStartCompletePerActivityDefinition.put(ad.getId(), balanceStartComplete);
		}

		// for(ActivityDefinition ad: model.getActivityDefinitions()){
		// if(!adIds.contains(ad.getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>"))){
		// view.setActivityDefinitionInvisible(ad);
		// ad.setVisible(false);
		// }else{
		// view.setActivityDefinitionVisible(ad);
		// ad.setVisible(true);
		// }
		// }
		DeclareMap outputMap = new DeclareMap(model, null, view, null, null, null);
		output.setModel(outputMap);
		output.setVisibleconfidence(constraintvisibleConfidenceMap);
		output.setVisibleCPIR(constraintvisibleCpirMap);
		output.setVisiblesupportRule(constraintvisibleSupportRuleMap);
		output.setVisibleI(constraintvisibleInterestFactorMap);
		output.setLog(log);

		System.out.println("I am in Second Model generation");
		for (Integer index : output.getTemplate().keySet()) {
			if (output.getTemplate().get(index).equals(DeclareTemplate.Response)) {
				System.out.println(
						"interest factor" + constraintParametersMap.get(index) + ": " + output.getI().get(index));
			}
		}

		Watch timeInformationWatch = new Watch();
		timeInformationWatch.start();
		TemplateInfo templateInfo = null;
		HashMap<Object, double[]> constraintIdTimeInstancesMap = new HashMap<Object, double[]>();
		AssignmentModel assignmentModel = output.getModel().getModel();
		HashMap<Object, Integer> activationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> fulfillmentsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> violationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> conflictsMap = new HashMap<Object, Integer>();
		HashMap<Object, Long> maxTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> minTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> avgTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> stdDevTimeDistancesMap = new HashMap<Object, Long>();

		XAttributeMap eventAttributeMap;
		boolean timed = output.getInput().getDeclarePerspectiveSet().contains(DeclarePerspective.Time) ? true : false;
		if (timed) {
			for (ConstraintDefinition constraintDefinition : assignmentModel.getConstraintDefinitions()) {
				// if(constraintDefinition.getBranches(constraintDefinition.getParameters().iterator().next()).iterator().next().getName().contains("Transmea")){
				// System.out.println("ciao");
				// }
				// if(constraintDefinition.getBranches(constraintDefinition.getParameterWithId(2)).iterator().next().getName().contains("Transmea")){
				// System.out.println("ciao");
				// }
				int numberOfActivations = 0;
				int numberOfViolations = 0;
				int numberOfFulfillments = 0;
				int numberOfConflicts = 0;
				Vector<Long> timeDists = new Vector<Long>();
				for (XTrace trace : log) {

					List<Integer> traceIndexes = new LinkedList<Integer>();
					List<String> traceEvents = new LinkedList<String>();
					int i = 0;
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						traceEvents.add((eventAttributeMap.get(XConceptExtension.KEY_NAME) + "-"
								+ eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION)).toLowerCase());
						traceIndexes.add(i);
						i++;
					}
					ExecutionsTree executiontree = new ExecutionsTree(traceEvents, traceIndexes, constraintDefinition);
					Set<Integer> activations = executiontree.getActivations();
					Set<Integer> violations = executiontree.getViolations();
					Set<Integer> fulfillments = executiontree.getFulfillments();
					Set<Integer> conflicts = executiontree.getConflicts();
					switch (output.getTemplate().get(constraintDefinition.getId())) {
						case Succession:
						case Alternate_Succession:
						case Chain_Succession:
							templateInfo = new SuccessionInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Choice:
							templateInfo = new ChoiceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Exclusive_Choice:
							templateInfo = new ExclusiveChoiceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence:
							templateInfo = new ExistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence2:
							templateInfo = new Existence2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence3:
							templateInfo = new Existence3Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Init:
							templateInfo = new InitInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Absence:
							templateInfo = new AbsenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Absence2:
							templateInfo = new Absence2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Absence3:
							templateInfo = new Absence3Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Exactly1:
							templateInfo = new Exactly1Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Exactly2:
							templateInfo = new Exactly2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Responded_Existence:
							templateInfo = new RespondedExistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Response:
						case Alternate_Response:
						case Chain_Response:
							templateInfo = new ResponseInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Precedence:
						case Alternate_Precedence:
						case Chain_Precedence:
							templateInfo = new PrecedenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case CoExistence:
							templateInfo = new CoexistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Not_CoExistence:
							templateInfo = new NotCoexistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Not_Succession:
						case Not_Chain_Succession:
							templateInfo = new NegativeRelationInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
					}
					numberOfActivations = numberOfActivations + activations.size();
					numberOfViolations = numberOfViolations + violations.size();
					numberOfFulfillments = numberOfFulfillments + fulfillments.size();
					numberOfConflicts = numberOfConflicts + conflicts.size();

				}
				double[] timeInstances = new double[timeDists.size()];
				int timeInstanceIndex = 0;
				for (Long time : timeDists)
					timeInstances[timeInstanceIndex++] = time;

				if (timeDists.size() > 0) {
					maxTimeDistancesMap.put(constraintDefinition.getId(), Collections.max(timeDists));
					minTimeDistancesMap.put(constraintDefinition.getId(), Collections.min(timeDists));
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), timeInstances);
				} else {
					maxTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					minTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), new double[] { -1 });
				}
				double avg = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					avg = avg + timeDists.get(i);
				}
				avg = avg / timeDists.size();
				avgTimeDistancesMap.put(constraintDefinition.getId(), (long) avg);

				double stddev = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					stddev = stddev + ((timeDists.get(i) - avg) * (timeDists.get(i) - avg));
				}
				stddev = stddev / (timeDists.size() - 1);
				stddev = Math.sqrt(stddev);
				stdDevTimeDistancesMap.put(constraintDefinition.getId(), (long) stddev);

				activationsMap.put(constraintDefinition.getId(), numberOfActivations);
				violationsMap.put(constraintDefinition.getId(), numberOfViolations);
				fulfillmentsMap.put(constraintDefinition.getId(), numberOfFulfillments);
				conflictsMap.put(constraintDefinition.getId(), numberOfConflicts);
			}
			// if(pw!=null){
			// pw.println("END TIME INFORMATION EVALUATION - time:
			// "+timeInformationWatch.msecs()+" msecs");
			// }

			output.setConstraintIdTimeInstancesMap(constraintIdTimeInstancesMap);
			output.setActivations(activationsMap);
			output.setViolations(violationsMap);
			output.setFulfillments(fulfillmentsMap);
			output.setConflicts(conflictsMap);
			output.setMaxTD(maxTimeDistancesMap);
			output.setMinTD(minTimeDistancesMap);
			output.setAvgTD(avgTimeDistancesMap);
			output.setStdDevTD(stdDevTimeDistancesMap);
			output.setExtend(extension);
		}
		output.setInput(input);
		return output;
	}

	private Vector<ConstraintDefinition> getInterestingConstraints(Vector<ConstraintDefinition> filteredConstraints) {
		// for(ConstraintDefinition cd : filteredConstraints){
		// if
		// }
		return filteredConstraints;
	}

	public static Map<Set<String>, Float> getFrequentItemSetSupportMap(int noparam, XLog log, float tolerance,
			boolean negative, PrintWriter pw, DeclareMinerInput input) {
		Map<Set<String>, Float> frequentItemSetSupportMap = null;
		Watch every = new Watch();
		every.start();
		FindItemSets finder = new FindItemSets(log, input);
		if (negative) {
			frequentItemSetSupportMap = finder.findItemSets(log, noparam, tolerance, true);

		} else {
			frequentItemSetSupportMap = finder.findItemSets(log, noparam, tolerance, false);
		}
		/*
		 * if(pw!=null){
		 * if(negative){
		 * pw.println("number of (positive/negative) frequent sets of size "
		 * +noparam+" with support "+tolerance+": "+frequentItemSetSupportMap.keySet().
		 * size());
		 * pw.println("time to generate the (positive/negative) frequent sets of size "
		 * +noparam+" with support "+tolerance+": "+every.msecs()+" msecs");
		 * }else{
		 * pw.println("number of (positive) frequent sets of size "
		 * +noparam+" with support "+tolerance+": "+frequentItemSetSupportMap.keySet().
		 * size());
		 * pw.println("time to generate the (positive) frequent sets of size "
		 * +noparam+" with support "+tolerance+": "+every.msecs()+" msecs");
		 * }
		 * }
		 */
		return frequentItemSetSupportMap;
	}

	public static DeclareMap layout(AssignmentModelView view, AssignmentModel model) {
		view.getGraph().doLayout();
		int i = 0;
		for (ActivityDefinitonCell cell : view.activityDefinitionCells()) {
			new Integer(i + 1);
			String label = cell.getActivityDefinition().getName();
			cell.setSize(new Point2D.Double(5. * (label.length() + 9), 30.0));
			cell.setPosition(new Point2D.Double(20. + (i * 180), 50 + (i * 80)));
			i++;
		}
		return new DeclareMap(model, null, view, null, null, null);
	}

	static HashMap<String, Float> getBalance(String activity, XLog log) {
		float unbalancingDegreeMissingStartSum = 0.f;
		float unbalancingDegreeMissingCompleteSum = 0.f;
		float tracesNumber = 0.f;
		float tracesBalancedNumber = 0.f;
		float tracesWithMissingStart = 0.f;
		float tracesWithMissingComplete = 0.f;
		for (XTrace trace : log) {
			float start = 0.f;
			float complete = 0.f;
			for (XEvent event : trace) {
				if (XConceptExtension.instance().extractName(event)
						.equals(activity.split("-")[activity.split("-").length - 1])) {
					String lifecycle = null;
					if (event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION) != null) {
						lifecycle = XLifecycleExtension.instance().extractTransition(event).toLowerCase();
					} else {
						lifecycle = "complete";
					}
					if (lifecycle.equals("start")) {
						start++;
					}
					if (lifecycle.equals("complete")) {
						complete++;
					}
				}
			}
			if (start != 0 || complete != 0) {
				if (start > complete) {
					tracesWithMissingComplete++;
					unbalancingDegreeMissingCompleteSum = unbalancingDegreeMissingCompleteSum + complete / start;
				} else if (complete > start) {
					tracesWithMissingStart++;
					unbalancingDegreeMissingStartSum = unbalancingDegreeMissingStartSum + start / complete;
				} else {
					tracesBalancedNumber++;
				}
			} else {
				tracesBalancedNumber++;
			}
			tracesNumber++;
		}
		float unbalancingDegreeMissingCompleteAverage = 1.f;
		if (tracesWithMissingComplete != 0) {
			unbalancingDegreeMissingCompleteAverage = unbalancingDegreeMissingCompleteSum / tracesWithMissingComplete;
		}
		float unbalancingDegreeMissingStartAverage = 1.f;
		if (tracesWithMissingStart != 0) {
			unbalancingDegreeMissingStartAverage = unbalancingDegreeMissingStartSum / tracesWithMissingStart;
		}
		HashMap<String, Float> result = new HashMap<String, Float>();
		result.put("tracesBalanced", tracesBalancedNumber / tracesNumber);
		result.put("tracesComplete", tracesWithMissingComplete / tracesNumber);
		result.put("tracesStart", tracesWithMissingStart / tracesNumber);
		result.put("unbalancingDegreeCompl", 1 - (unbalancingDegreeMissingCompleteAverage));
		result.put("unbalancingDegreeStart", 1 - (unbalancingDegreeMissingStartAverage));
		return result;
	}

	private static boolean isBinary(DeclareTemplate template) {
		return template.equals(DeclareTemplate.Alternate_Precedence)
				|| template.equals(DeclareTemplate.Alternate_Response) ||
				template.equals(DeclareTemplate.Alternate_Succession)
				|| template.equals(DeclareTemplate.Chain_Precedence) ||
				template.equals(DeclareTemplate.Chain_Response) || template.equals(DeclareTemplate.Chain_Succession) ||
				template.equals(DeclareTemplate.CoExistence) || template.equals(DeclareTemplate.Precedence) ||
				template.equals(DeclareTemplate.Responded_Existence) || template.equals(DeclareTemplate.Response) ||
				template.equals(DeclareTemplate.Succession) || template.equals(DeclareTemplate.Exclusive_Choice)
				|| template.equals(DeclareTemplate.Not_CoExistence)
				|| template.equals(DeclareTemplate.Not_Succession)
				|| template.equals(DeclareTemplate.Not_Chain_Succession)
				|| template.equals(DeclareTemplate.Choice);
	}

	private static boolean isConfidenceEvaluable(String constraintName) {
		return !constraintName.startsWith("existence") && !constraintName.startsWith("absence")
				&& !constraintName.startsWith("exactly")
				&& !constraintName.contains("choice") && !constraintName.contains("init");
	}

	private static String removeEventTypeFromActivityName(String activityName) {
		String[] splitActivityName = activityName.split("-");
		String eventClass = "";
		for (int s = 0; s < splitActivityName.length - 1; s++) {
			if (s < splitActivityName.length - 2) {
				eventClass = eventClass + splitActivityName[s] + "-";
			} else {
				eventClass = eventClass + splitActivityName[s];
			}
		}
		return eventClass;
	}

	public static HashMap<String, HashMap<String, ConstraintDefinition>> getVisibleCd(float minSupp,
			Vector<ConstraintDefinition> allDiscoveredConstraints, HashMap<Integer, Float> constraintSupportRuleMap,
			Vector<String> toSkip /* templates */, Vector<String> toHide, Vector<String> toETHide) {
		// HashMap visibleCds = new HashMap();
		// for(ConstraintDefinition cd: model.getConstraintDefinitions()){
		// minSupp = minSupp/100.f;
		// String paramA = null;
		// String paramB =null;
		// paramA =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>");
		// if(cd.getParameterWithId(2)!= null){
		// paramB =
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>");
		// }
		// String comp1 = "";
		// if(cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>").length>1){
		// comp1 =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>")[1].split("</center></html>")[0];
		// }else{
		// comp1 =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("-")[cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("-").length-1];
		// }
		// if(!toSkip.contains(cd.getName())&&!toHide.contains(paramA)&&!toETHide.contains(comp1)){
		// String comp2 = "";
		// if(cd.getParameterWithId(2)!= null){
		// if(cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>").length>1){
		// comp2 =
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>")[1].split("</center></html>")[0];
		// }else{
		// comp2 =
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("-")[cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("-").length-1];
		// }
		// }
		// if(paramB!=null &&!toHide.contains(paramB)&&!toETHide.contains(comp2)){
		// if(paramB!=null){
		// HashMap current = null;
		// if(visibleCds.containsKey(paramA+";"+paramB)){
		// current = (HashMap)visibleCds.get(paramA+";"+paramB);
		// }else{
		// current = new HashMap();
		// }
		// current.put(cd.getName(), cd);
		// visibleCds.put(paramA+";"+paramB,current);
		// }
		// }
		// if(paramB ==null){
		// HashMap current = null;
		// if(visibleCds.containsKey(paramA)){
		// current = (HashMap)visibleCds.get(paramA);
		// }else{
		// current = new HashMap();
		// }
		// current.put(cd.getName(), cd);
		// visibleCds.put(paramA, current);
		// }
		// }
		// }
		//
		//
		// for(ConstraintDefinition cd: model.getConstraintDefinitions()){
		//
		//
		// String key =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>");
		// if(cd.getParameterWithId(2)!= null){
		// key = key + ";" +
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>");
		// }
		// if(!(constraintSupportRuleMap.get(cd.getId())>=minSupp)){
		// if(visibleCds.containsKey(key)&&((HashMap)visibleCds.get(key)).containsKey(cd.getName())){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }else{
		//
		// if(visibleCds.containsKey(key)){
		// if(cd.getName().equals("chain response")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain succession")){
		// ConstraintDefinition cs =
		// (ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain succession");
		// if(constraintSupportRuleMap.get(cs.getId())>=minSupp){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		// }
		//
		// if(cd.getName().equals("chain precedence")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain succession")){
		// ConstraintDefinition cs =
		// (ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain succession");
		// if(constraintSupportRuleMap.get(cs.getId())>=minSupp){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		// }
		//
		//
		//
		// if(cd.getName().equals("alternate response")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		//
		// if(cd.getName().equals("alternate precedence")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// precedence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		// if(cd.getName().equals("alternate succession")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		// if(cd.getName().equals("response")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		// if(cd.getName().equals("succession")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		// if(cd.getName().equals("precedence")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// precedence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// precedence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("init")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("init")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		// if(cd.getName().equals("responded existence")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("co-existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("co-existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		//
		// String reverseKey = key.split(";")[1]+";"+key.split(";")[0];
		//
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// precedence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// precedence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("precedence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("co-existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("co-existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey.split(";")[0])&&((HashMap)visibleCds.get(reverseKey.split(";")[0])).containsKey("init")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey.split(";")[0])).get("init")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		//
		// }
		//
		//
		// if(cd.getName().equals("co-existence")){
		// if(((HashMap)visibleCds.get(key)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(key)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		//
		//
		// String reverseKey = key.split(";")[1]+";"+key.split(";")[0];
		//
		//
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("chain
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("chain
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("precedence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("precedence")).getId())>=minSupp)&&((HashMap)visibleCds.get(reverseKey)).containsKey("alternate
		// response")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("alternate
		// response")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		// if(cd.getName().equals("existence")){
		// if(((HashMap)visibleCds.get(key)).containsKey("init")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("init")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (((HashMap)visibleCds.get(key)).containsKey("existence2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("existence2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (((HashMap)visibleCds.get(key)).containsKey("existence3")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("existence3")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("exactly1")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exactly1")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("exactly2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exactly2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		// if(cd.getName().equals("existence2")){
		// if
		// (((HashMap)visibleCds.get(key)).containsKey("existence3")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("existence3")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("exactly2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exactly2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		// if(cd.getName().equals("absence2")){
		// if(((HashMap)visibleCds.get(key)).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("exactly1")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exactly1")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		// if(cd.getName().equals("absence3")){
		// if(((HashMap)visibleCds.get(key)).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("absence2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("absence2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("exactly1")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exactly1")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(((HashMap)visibleCds.get(key)).containsKey("exactly2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exactly2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		// if(cd.getName().equals("choice")){
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("init")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("init")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("existence2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("existence2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("existence3")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("existence3")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("exactly1")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("exactly1")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("exactly2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("exactly2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("init")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("init")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("existence2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("existence2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("existence3")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("existence3")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("exactly1")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("exactly1")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("exactly2")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("exactly2")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if(((HashMap)visibleCds.get(key)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		// if(cd.getName().equals("not chain succession")){
		// if(((HashMap)visibleCds.get(key)).containsKey("not
		// succession")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("not
		// succession")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("not
		// co-existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("not
		// co-existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// String reverseKey = key.split(";")[1]+";"+key.split(";")[0];
		//
		// if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("not
		// co-existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("not
		// co-existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		//
		//
		//
		// if(cd.getName().equals("not succession")){
		// if (((HashMap)visibleCds.get(key)).containsKey("not
		// co-existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("not
		// co-existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if (((HashMap)visibleCds.get(key)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		//
		// String reverseKey = key.split(";")[1]+";"+key.split(";")[0];
		//
		// if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("not
		// co-existence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("not
		// co-existence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		//
		// if(cd.getName().equals("not co-existence")){
		// if (((HashMap)visibleCds.get(key)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[0])&&((HashMap)visibleCds.get(key.split(";")[0])).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[0])).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }else
		// if(visibleCds.containsKey(key.split(";")[1])&&((HashMap)visibleCds.get(key.split(";")[1])).containsKey("absence")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(key.split(";")[1])).get("absence")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// String reverseKey = key.split(";")[1]+";"+key.split(";")[0];
		//
		// if
		// (visibleCds.containsKey(reverseKey)&&((HashMap)visibleCds.get(reverseKey)).containsKey("exclusive
		// choice")&&(constraintSupportRuleMap.get(((ConstraintDefinition)((HashMap)visibleCds.get(reverseKey)).get("exclusive
		// choice")).getId())>=minSupp)){
		// ((HashMap)visibleCds.get(key)).remove(cd.getName());
		// }
		// }
		//
		//
		//
		//
		//
		// }
		// }
		// }
		// return visibleCds;

		HashMap<String, HashMap<String, ConstraintDefinition>> visibleCds = new HashMap<String, HashMap<String, ConstraintDefinition>>();
		// if(extend){
		// for(ConstraintDefinition cd: model.getConstraintDefinitions()){
		// String paramA = null;
		// String paramB =null;
		// paramA =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName();
		// if(cd.getParameterWithId(2)!= null){
		// paramB =
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName();
		// }
		// String comp1 = "";
		// //
		// if(cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>").length>1){
		// // comp1 =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>")[1].split("</center></html>")[0];
		// // }else{
		// comp1 =
		// cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().split("-")[cd.getBranches(cd.getParameters().iterator().next()).iterator().next().getName().split("-").length-1];
		// // }
		// if(!toSkip.contains(cd.getName())&&!toHide.contains(paramA)&&!toETHide.contains(comp1)){
		// String comp2 = "";
		// if(cd.getParameterWithId(2)!= null){
		// //
		// if(cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>").length>1){
		// // comp2 =
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>",
		// "</html>").split("</center><center>")[1].split("</center></html>")[0];
		// // }else{
		// comp2 =
		// cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().split("-")[cd.getBranches(cd.getParameterWithId(2)).iterator().next().getName().split("-").length-1];
		// // }
		// }
		// if(paramB!=null &&!toHide.contains(paramB)&&!toETHide.contains(comp2)){
		// HashMap<String, ConstraintDefinition> current = null;
		// if(visibleCds.containsKey(paramA+";"+paramB)){
		// current = (HashMap<String,
		// ConstraintDefinition>)visibleCds.get(paramA+";"+paramB);
		// }else{
		// current = new HashMap<String, ConstraintDefinition>();
		// }
		// current.put(cd.getName(), cd);
		// visibleCds.put(paramA+";"+paramB,current);
		// }
		// if(paramB ==null){
		// HashMap current = null;
		// if(visibleCds.containsKey(paramA)){
		// current = (HashMap)visibleCds.get(paramA);
		// }else{
		// current = new HashMap();
		// }
		// current.put(cd.getName(), cd);
		// visibleCds.put(paramA, current);
		// }
		// }
		// }
		//
		// }else{
		for (ConstraintDefinition cd : allDiscoveredConstraints) {
			String paramA = "EMPTY_PARAM";
			String paramB = null;
			Iterator<Parameter> iter = cd.getParameters().iterator();
			Parameter p1 = iter.next();
			Parameter p2 = null;
			if (iter.hasNext()) {
				p2 = iter.next();
			}
			if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p1).iterator().next() != null) {
				paramA = cd.getBranches(p1).iterator().next().getName();
			}
			if (p2 != null) {
				if (cd.getBranches(p2).iterator().hasNext() && cd.getBranches(p2).iterator().next() != null) {
					paramB = cd.getBranches(p2).iterator().next().getName();
				} else {
					paramB = "EMPTY_PARAM";
				}
			}
			String comp1 = "EMPTY_PARAM";
			if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p1).iterator().next() != null) {
				// if(cd.getBranches(p1).iterator().next().getName()").split("</center><center>").length>1){
				// comp1 = cd.getBranches(p1).iterator().next().getName().replace("<html><body
				// text=404040>","<html>").replace("</body></html>",
				// "</html>").split("</center><center>")[1].split("</center></html>")[0];
				// }else{
				comp1 = cd.getBranches(p1).iterator().next().getName()
						.split("-")[cd.getBranches(p1).iterator().next().getName().split("-").length - 1];
				// }
			}
			if (!toSkip.contains(cd.getName()) && !toHide.contains(paramA) && !toETHide.contains(comp1)) {
				String comp2 = null;
				if (p2 != null) {
					if (cd.getBranches(p2).iterator().hasNext() && cd.getBranches(p2).iterator().next() != null) {
						// if(cd.getBranches(p2).iterator().next().getName().replace("<html><body
						// text=404040>","<html>").replace("</body></html>",
						// "</html>").split("</center><center>").length>1){
						// comp2 = cd.getBranches(p2).iterator().next().getName().replace("<html><body
						// text=404040>","<html>").replace("</body></html>",
						// "</html>").split("</center><center>")[1].split("</center></html>")[0];
						// }else{
						comp2 = cd.getBranches(p2).iterator().next().getName()
								.split("-")[cd.getBranches(p2).iterator().next().getName().split("-").length - 1];
						// }
					} else {
						comp2 = "EMPTY_PARAM";
					}
				}
				if (paramB != null && !toHide.contains(paramB) && !toETHide.contains(comp2)) {
					HashMap<String, ConstraintDefinition> current = null;
					if (visibleCds.containsKey(paramA + ";" + paramB)) {
						current = visibleCds.get(paramA + ";" + paramB);
					} else {
						current = new HashMap<String, ConstraintDefinition>();
					}
					current.put(cd.getName(), cd);
					visibleCds.put(paramA + ";" + paramB, current);
				}
				if (paramB == null) {
					HashMap<String, ConstraintDefinition> current = null;
					if (visibleCds.containsKey(paramA)) {
						current = visibleCds.get(paramA);
					} else {
						current = new HashMap<String, ConstraintDefinition>();
					}
					current.put(cd.getName(), cd);
					visibleCds.put(paramA, current);
				}
			}
		}

		for (ConstraintDefinition cd : allDiscoveredConstraints) {
			Iterator<Parameter> iter = cd.getParameters().iterator();
			Parameter p1 = iter.next();
			Parameter p2 = null;
			if (iter.hasNext()) {
				p2 = iter.next();
			}
			String key = "EMPTY_PARAM";
			if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p1).iterator().next() != null) {
				key = cd.getBranches(p1).iterator().next().getName();
			}
			if (p2 != null) {
				if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p2).iterator().next() != null) {
					key = key + ";" + cd.getBranches(p2).iterator().next().getName();
				} else {
					key = key + ";EMPTY_PARAM";
				}
			}
			if (!(constraintSupportRuleMap.get(cd.getId()) >= minSupp)) {
				if (visibleCds.containsKey(key) && (visibleCds.get(key)).containsKey(cd.getName())) {
					(visibleCds.get(key)).remove(cd.getName());
				}
			} else {

				if (visibleCds.containsKey(key)) {
					if (cd.getName().equals("chain response")) {
						if ((visibleCds.get(key)).containsKey("chain succession")) {
							ConstraintDefinition cs = (visibleCds.get(key)).get("chain succession");
							if (constraintSupportRuleMap.get(cs.getId()) >= minSupp) {
								(visibleCds.get(key)).remove(cd.getName());
							}
						}
					}

					if (cd.getName().equals("chain precedence")) {
						if ((visibleCds.get(key)).containsKey("chain succession")) {
							ConstraintDefinition cs = (visibleCds.get(key)).get("chain succession");
							if (constraintSupportRuleMap.get(cs.getId()) >= minSupp) {
								(visibleCds.get(key)).remove(cd.getName());
							}
						}
					}

					if (cd.getName().equals("alternate response")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain response") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("alternate precedence")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain precedence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain precedence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("alternate succession")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("response")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain response") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate response") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("succession")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("precedence")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain precedence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain precedence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate precedence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("init") && (constraintSupportRuleMap
										.get(((visibleCds.get(key.split(";")[0])).get("init")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("responded existence")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain response") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate response") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("response") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("co-existence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("co-existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}

						String reverseKey = key.split(";")[1] + ";" + key.split(";")[0];

						if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("chain succession")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("chain precedence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("chain precedence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("alternate precedence"))
												.getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("alternate succession"))
												.getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("succession") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("precedence") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("precedence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("co-existence") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("co-existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey.split(";")[0])
								&& (visibleCds.get(reverseKey.split(";")[0])).containsKey("init")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey.split(";")[0])).get("init")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}

					}

					if (cd.getName().equals("co-existence")) {
						if ((visibleCds.get(key)).containsKey("chain succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("chain precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("chain response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("alternate response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("chain precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("alternate response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("chain precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("chain precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("chain response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("chain response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("precedence")).getId()) >= minSupp)
								&& (visibleCds.get(key)).containsKey("alternate response") && (constraintSupportRuleMap
										.get(((visibleCds.get(key)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}

						String reverseKey = key.split(";")[1] + ";" + key.split(";")[0];

						if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("chain succession")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("chain succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("alternate succession")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("alternate succession"))
												.getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("succession") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("chain precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("chain precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("chain response")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("alternate precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("alternate response")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("response") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("chain precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("chain precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("alternate response")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("chain precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("chain precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("response") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("alternate precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("chain response")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("alternate precedence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("alternate precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("response") && (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("chain response")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("chain response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("precedence")
								&& (constraintSupportRuleMap
										.get(((visibleCds.get(reverseKey)).get("precedence")).getId()) >= minSupp)
								&& (visibleCds.get(reverseKey)).containsKey("alternate response")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("alternate response")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("existence")) {
						if ((visibleCds.get(key)).containsKey("init") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("init")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("existence2") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("existence2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("existence3") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("existence3")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exactly1") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exactly1")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exactly2") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exactly2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("existence2")) {
						if ((visibleCds.get(key)).containsKey("existence3") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("existence3")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exactly2") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exactly2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("absence2")) {
						if ((visibleCds.get(key)).containsKey("absence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exactly1") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exactly1")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("absence3")) {
						if ((visibleCds.get(key)).containsKey("absence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("absence2") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("absence2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exactly1") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exactly1")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exactly2") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exactly2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("choice")) {
						if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("init") && (constraintSupportRuleMap
										.get(((visibleCds.get(key.split(";")[0])).get("init")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("existence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("existence2")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("existence2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("existence3")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("existence3")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("exactly1")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("exactly1")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("exactly2")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("exactly2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("init") && (constraintSupportRuleMap
										.get(((visibleCds.get(key.split(";")[1])).get("init")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("existence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("existence2")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("existence2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("existence3")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("existence3")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("exactly1")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("exactly1")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("exactly2")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("exactly2")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exclusive choice") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("not chain succession")) {
						if ((visibleCds.get(key)).containsKey("not succession") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("not succession")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("not co-existence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("not co-existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exclusive choice") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("absence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("absence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
						String reverseKey = key.split(";")[1] + ";" + key.split(";")[0];

						if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("not co-existence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("not co-existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("exclusive choice")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("not succession")) {
						if ((visibleCds.get(key)).containsKey("not co-existence") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("not co-existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if ((visibleCds.get(key)).containsKey("exclusive choice") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("absence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("absence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}

						String reverseKey = key.split(";")[1] + ";" + key.split(";")[0];

						if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("not co-existence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("not co-existence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("exclusive choice")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}

					if (cd.getName().equals("not co-existence")) {
						if ((visibleCds.get(key)).containsKey("exclusive choice") && (constraintSupportRuleMap
								.get(((visibleCds.get(key)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[0])
								&& (visibleCds.get(key.split(";")[0])).containsKey("absence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[0])).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						} else if (visibleCds.containsKey(key.split(";")[1])
								&& (visibleCds.get(key.split(";")[1])).containsKey("absence")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(key.split(";")[1])).get("absence")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
						String reverseKey = key.split(";")[1] + ";" + key.split(";")[0];

						if (visibleCds.containsKey(reverseKey)
								&& (visibleCds.get(reverseKey)).containsKey("exclusive choice")
								&& (constraintSupportRuleMap.get(
										((visibleCds.get(reverseKey)).get("exclusive choice")).getId()) >= minSupp)) {
							(visibleCds.get(key)).remove(cd.getName());
						}
					}
				}
			}
		}
		return visibleCds;
	}

	public DeclareExtensionOutput createModel(Map<Integer, String> correlationsPerConstraint, boolean hier,
			boolean trans, Map<Integer, MetricsValues> metricsVectorConstraint, Collection<ConstraintDefinition> cds,
			float minSupport, float minConfidence, float minCPIR, float minInterestFactor, XLog log,
			DeclareMinerInput input, DeclareMap inputMap) {
		transitiveReduction = trans;
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, String> constraintFormulaMap = new HashMap<Integer, String>();
		HashMap<Integer, Float> constraintSupportAntecendentMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintSupportConsequentMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintSupportRuleMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintConfidenceMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintCpirMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintInterestFactorMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> activitySupportMap = new HashMap<Integer, Float>();
		HashMap<ActivityDefinition, Vector<ConstraintDefinition>> activityConstraintMap = new HashMap<ActivityDefinition, Vector<ConstraintDefinition>>();
		Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();
		DeclareTemplate[] declareTemplates = DeclareTemplate.values();
		for (DeclareTemplate d : declareTemplates) {
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}

		// Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap
		// = DeclareMiner.readConstraintTemplates(templateNameStringDeclareTemplateMap);

		FindItemSets f = new FindItemSets(log, input);

		InputStream ir = getClass().getResourceAsStream("/resources/template.xml");
		File language = null;
		try {
			language = File.createTempFile("template", ".xml");
			BufferedReader br = new BufferedReader(new InputStreamReader(ir));
			String line = br.readLine();
			PrintStream out = new PrintStream(language);
			while (line != null) {
				out.println(line);
				line = br.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Vector<ConstraintDefinition> allDiscoveredConstraints = new Vector<ConstraintDefinition>();
		HashMap<Integer, MetricsValues> constraintMetricsMap = new HashMap<Integer, MetricsValues>();
		TemplateBroker template = XMLBrokerFactory.newTemplateBroker(language.getAbsolutePath());
		List<Language> languages = template.readLanguages();
		Language lang = languages.get(0);
		AssignmentModel model = new AssignmentModel(lang);
		model.setName("new model");
		DeclareMap outputMap = null;
		ActivityDefinition activitydefinition = null;
		Vector<String> activityDefinitions = new Vector<String>();
		// int constraintID = 0;
		// int activityID = 1;
		for (ConstraintDefinition cd : cds) {

			// DeclareTemplate currentTemplate = DeclareMiner.getTemplate(cd);
			// int numberOfParameters = 0;
			// if(isBinary(currentTemplate)){
			// numberOfParameters = 2;
			// }else{
			// numberOfParameters = 1;
			// }

			// DeclareTemplate ctemplate = DeclareMiner.getTemplate(cd);
			// int numberOfParams = 0;
			// if(isBinary(ctemplate)){
			// numberOfParams = 2;
			// }else{
			// numberOfParams = 1;
			// }

			for (Parameter parameter : cd.getParameters()) {
				if (cd.getBranches(parameter).iterator().hasNext()) {
					if (!activityDefinitions.contains(cd.getBranches(parameter).iterator().next().getName())) {
						activityDefinitions.add(cd.getBranches(parameter).iterator().next().getName());
						activitydefinition = model
								.addActivityDefinition(cd.getBranches(parameter).iterator().next().getId());
						activitydefinition.setName(cd.getBranches(parameter).iterator().next().getName());
						activitySupportMap.put(cd.getBranches(parameter).iterator().next().getId(),
								f.getSupport(activitydefinition.getName()) / 100.f);
					}
				} else {
					if (!activityDefinitions.contains("EMPTY_PARAM")) {
						activityDefinitions.add("EMPTY_PARAM");
						activitydefinition = model.addActivityDefinition(-1);
						activitydefinition.setName("EMPTY_PARAM");
						activitySupportMap.put(-1, f.getSupport(activitydefinition.getName()) / 100.f);
					}
				}
			}

			MetricsValues metricsValues = metricsVectorConstraint.get(cd.getId());
			constraintTemplateMap.put(cd.getId(), metricsValues.getTemplate());
			constraintParametersMap.put(cd.getId(), metricsValues.getParameters());
			constraintFormulaMap.put(cd.getId(), metricsValues.getFormula());
			constraintSupportRuleMap.put(cd.getId(), metricsValues.getSupportRule());
			if (isConfidenceEvaluable(cd.getName())) {
				constraintSupportAntecendentMap.put(cd.getId(), metricsValues.getSuppAntec());
				constraintSupportConsequentMap.put(cd.getId(), metricsValues.getSupportConseq());
				constraintConfidenceMap.put(cd.getId(), metricsValues.getConfidence());
				constraintCpirMap.put(cd.getId(), metricsValues.getCPIR());
				constraintInterestFactorMap.put(cd.getId(), metricsValues.getI());
			} else {
				constraintSupportAntecendentMap.put(cd.getId(), Float.MAX_VALUE);
				constraintSupportConsequentMap.put(cd.getId(), Float.MAX_VALUE);
				constraintConfidenceMap.put(cd.getId(), Float.MAX_VALUE);
				constraintCpirMap.put(cd.getId(), Float.MAX_VALUE);
				constraintInterestFactorMap.put(cd.getId(), Float.MAX_VALUE);
			}
			// model.addConstraintDefiniton(cd);
			allDiscoveredConstraints.add(cd);
			constraintMetricsMap.put(cd.getId(), metricsValues);
		}

		for (ConstraintDefinition constraintDefinition : allDiscoveredConstraints) {
			model.addConstraintDefiniton(constraintDefinition);
		}

		AssignmentModelView view = inputMap.getView();
		Vector<String> adIds = new Vector<String>();
		for (ConstraintDefinition constraintdefinition : model.getConstraintDefinitions()) {
			// view.setConstraintDefinitionVisible(constraintdefinition);
			// constraintdefinition.setVisible(true);
			for (Parameter p : constraintdefinition.getParameters()) {
				if (constraintdefinition.getBranches(p).iterator().hasNext()) {
					adIds.add(constraintdefinition.getBranches(p).iterator().next().getName()
							.replace("<html><body text=404040>", "<html>").replace("</body></html>", "</html>"));
				} else {
					adIds.add("EMPTY_PARAM");
				}
			}

			Color constraintDefinitionColor;
			int green = 255 - (int) (255 * constraintSupportRuleMap.get(constraintdefinition.getId()));
			constraintDefinitionColor = new Color(255, green, green);
			if (constraintdefinition.getName().contains("alternate")
					|| constraintdefinition.getName().contains("chain")) {
				view.setConstraintDefinitionColor(constraintdefinition, Color.black, constraintDefinitionColor,
						constraintDefinitionColor);
			} else {
				view.setConstraintDefinitionColor(constraintdefinition, constraintDefinitionColor,
						constraintDefinitionColor, constraintDefinitionColor);
			}
		}
		Color activityDefinitionColor;
		HashMap<Integer, HashMap<String, Float>> balancingStartCompletePerActivityDefinition = new HashMap<Integer, HashMap<String, Float>>();
		for (ActivityDefinition ad : model.getActivityDefinitions()) {
			String activityName = ad.getName();
			if (input.getAprioriKnowledgeBasedCriteriaSet()
					.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
				if (!hasEventTypeInName(activityName)) {
					activityName = activityName + "-" + input.getReferenceEventType();
				}
			}

			int blue = 255 - (int) (255 * (f.getSupport(ad.getName()) / 100.f));
			activityDefinitionColor = new Color(255, 255, blue);
			//////////////////////////// REMOVE///////////////////////
			// activityDefinitionColor = Color.white;
			////////////////////////// REMOVE///////////////////////
			view.setActivityDefinitionBackground(ad, activityDefinitionColor);
			HashMap<String, Float> balanceStartComplete = getBalance(removeEventTypeFromActivityName(ad.getName()),
					log);
			balancingStartCompletePerActivityDefinition.put(ad.getId(), balanceStartComplete);
		}

		// for(ActivityDefinition ad: model.getActivityDefinitions()){
		// if(!adIds.contains(ad.getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>"))){
		// view.setActivityDefinitionInvisible(ad);
		// ad.setVisible(false);
		// }else{
		// view.setActivityDefinitionVisible(ad);
		// ad.setVisible(true);
		// }
		// }
		outputMap = new DeclareMap(model, null, view, null, null, null);
		DeclareExtensionOutput visualizationData = new DeclareExtensionOutput();
		visualizationData.setAllDiscoveredConstraints(allDiscoveredConstraints);
		visualizationData.setAllActivities(model.getActivityDefinitions());
		visualizationData.setConfidence(constraintConfidenceMap);
		visualizationData.setCPIR(constraintCpirMap);
		visualizationData.setFormula(constraintFormulaMap);
		visualizationData.setI(constraintInterestFactorMap);
		visualizationData.setModel(outputMap);
		visualizationData.setParameters(constraintParametersMap);
		visualizationData.setSuppAntec(constraintSupportAntecendentMap);
		visualizationData.setSupportConseq(constraintSupportConsequentMap);
		visualizationData.setSupportRule(constraintSupportRuleMap);
		visualizationData.setTemplate(constraintTemplateMap);
		visualizationData.setMappingAdCd(activityConstraintMap);
		visualizationData.setActSupp(activitySupportMap);
		visualizationData.setBlnc(balancingStartCompletePerActivityDefinition);
		visualizationData.setTransitiveClosureResponseConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosureRespondedExistenceConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosurePrecedenceConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosureSuccessionConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosureCoexistenceConstraints(new Vector<Integer>());
		visualizationData.setLog(log);

		Watch timeInformationWatch = new Watch();
		timeInformationWatch.start();
		TemplateInfo templateInfo = null;
		HashMap<Object, double[]> constraintIdTimeInstancesMap = new HashMap<Object, double[]>();
		AssignmentModel assignmentModel = visualizationData.getModel().getModel();
		HashMap<Object, Integer> activationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> fulfillmentsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> violationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> conflictsMap = new HashMap<Object, Integer>();
		HashMap<Object, Long> maxTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> minTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> avgTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> stdDevTimeDistancesMap = new HashMap<Object, Long>();

		XAttributeMap eventAttributeMap;
		boolean timed = input.getDeclarePerspectiveSet().contains(DeclarePerspective.Time) ? true : false;
		if (timed) {
			for (ConstraintDefinition constraintDefinition : assignmentModel.getConstraintDefinitions()) {

				int numberOfActivations = 0;
				int numberOfViolations = 0;
				int numberOfFulfillments = 0;
				int numberOfConflicts = 0;
				Vector<Long> timeDists = new Vector<Long>();
				for (XTrace trace : log) {

					List<Integer> traceIndexes = new LinkedList<Integer>();
					List<String> traceEvents = new LinkedList<String>();
					int i = 0;
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						traceEvents.add((eventAttributeMap.get(XConceptExtension.KEY_NAME) + "-"
								+ eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION)).toLowerCase());
						traceIndexes.add(i);
						// System.out.println("eventoooo:
						// "+eventAttributeMap.get(XConceptExtension.KEY_NAME));
						i++;
					}
					ExecutionsTree executiontree = new ExecutionsTree(traceEvents, traceIndexes, constraintDefinition);
					Set<Integer> activations = executiontree.getActivations();
					Set<Integer> violations = executiontree.getViolations();
					Set<Integer> fulfillments = executiontree.getFulfillments();
					Set<Integer> conflicts = executiontree.getConflicts();
					String correlation = correlationsPerConstraint.get(constraintDefinition.getId());
					switch (visualizationData.getTemplate().get(constraintDefinition.getId())) {
						case Succession:
						case Alternate_Succession:
						case Chain_Succession:
							templateInfo = new SuccessionInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Choice:
							templateInfo = new ChoiceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Exclusive_Choice:
							templateInfo = new ExclusiveChoiceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Existence:
							templateInfo = new ExistenceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Existence2:
							templateInfo = new Existence2Info();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Existence3:
							templateInfo = new Existence3Info();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Init:
							templateInfo = new InitInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Absence:
							templateInfo = new AbsenceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, conflicts, correlation));
							break;
						case Absence2:
							templateInfo = new Absence2Info();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, conflicts, correlation));
							break;
						case Absence3:
							templateInfo = new Absence3Info();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, conflicts, correlation));
							break;
						case Exactly1:
							templateInfo = new Exactly1Info();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Exactly2:
							templateInfo = new Exactly2Info();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Responded_Existence:
							templateInfo = new RespondedExistenceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Response:
						case Alternate_Response:
						case Chain_Response:
							templateInfo = new ResponseInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Precedence:
						case Alternate_Precedence:
						case Chain_Precedence:
							templateInfo = new PrecedenceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case CoExistence:
							templateInfo = new CoexistenceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, fulfillments, correlation));
							break;
						case Not_CoExistence:
							templateInfo = new NotCoexistenceInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, conflicts, correlation));
							break;
						case Not_Succession:
						case Not_Chain_Succession:
							templateInfo = new NegativeRelationInfo();
							timeDists.addAll(templateInfo.getTimeDistances(
									ExtendedEvent.getEventsWithAttributeTypes(activityDefinitions, log), input, trace,
									constraintDefinition, conflicts, correlation));
							break;
					}
					numberOfActivations = numberOfActivations + activations.size();
					numberOfViolations = numberOfViolations + violations.size();
					numberOfFulfillments = numberOfFulfillments + fulfillments.size();
					numberOfConflicts = numberOfConflicts + conflicts.size();

				}
				double[] timeInstances = new double[timeDists.size()];
				int timeInstanceIndex = 0;
				for (Long time : timeDists)
					timeInstances[timeInstanceIndex++] = time;

				if (timeDists.size() > 0) {
					maxTimeDistancesMap.put(constraintDefinition.getId(), Collections.max(timeDists));
					minTimeDistancesMap.put(constraintDefinition.getId(), Collections.min(timeDists));
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), timeInstances);
				} else {
					maxTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					minTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), new double[] { -1 });
				}
				double avg = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					avg = avg + timeDists.get(i);
				}
				avg = avg / timeDists.size();
				avgTimeDistancesMap.put(constraintDefinition.getId(), (long) avg);

				double stddev = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					stddev = stddev + ((timeDists.get(i) - avg) * (timeDists.get(i) - avg));
				}
				stddev = stddev / (timeDists.size() - 1);
				stddev = Math.sqrt(stddev);
				stdDevTimeDistancesMap.put(constraintDefinition.getId(), (long) stddev);

				activationsMap.put(constraintDefinition.getId(), numberOfActivations);
				violationsMap.put(constraintDefinition.getId(), numberOfViolations);
				fulfillmentsMap.put(constraintDefinition.getId(), numberOfFulfillments);
				conflictsMap.put(constraintDefinition.getId(), numberOfConflicts);
			}
			// if(pw!=null){
			// pw.println("END TIME INFORMATION EVALUATION - time:
			// "+timeInformationWatch.msecs()+" msecs");
			// }

			visualizationData.setConstraintIdTimeInstancesMap(constraintIdTimeInstancesMap);
			visualizationData.setActivations(activationsMap);
			visualizationData.setViolations(violationsMap);
			visualizationData.setFulfillments(fulfillmentsMap);
			visualizationData.setConflicts(conflictsMap);
			visualizationData.setMaxTD(maxTimeDistancesMap);
			visualizationData.setMinTD(minTimeDistancesMap);
			visualizationData.setAvgTD(avgTimeDistancesMap);
			visualizationData.setStdDevTD(stdDevTimeDistancesMap);
			visualizationData.setExtend(false);
		}
		visualizationData.setInput(input);

		return visualizationData;
	}

	public DeclareExtensionOutput createModel(boolean hier, boolean trans,
			Map<Integer, MetricsValues> metricsVectorConstraint, Collection<ConstraintDefinition> cds, float minSupport,
			float minConfidence, float minCPIR, float minInterestFactor, XLog log, DeclareMinerInput input,
			DeclareMap inputMap) {
		transitiveReduction = trans;
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		HashMap<Integer, List<String>> constraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, String> constraintFormulaMap = new HashMap<Integer, String>();
		HashMap<Integer, Float> constraintSupportAntecendentMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintSupportConsequentMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintSupportRuleMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintConfidenceMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintCpirMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> constraintInterestFactorMap = new HashMap<Integer, Float>();
		HashMap<Integer, Float> activitySupportMap = new HashMap<Integer, Float>();
		HashMap<ActivityDefinition, Vector<ConstraintDefinition>> activityConstraintMap = new HashMap<ActivityDefinition, Vector<ConstraintDefinition>>();
		Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();
		DeclareTemplate[] declareTemplates = DeclareTemplate.values();
		for (DeclareTemplate d : declareTemplates) {
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}

		// Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap
		// = DeclareMiner.readConstraintTemplates(templateNameStringDeclareTemplateMap);

		FindItemSets f = new FindItemSets(log, input);

		InputStream ir = getClass().getResourceAsStream("/resources/template.xml");
		File language = null;
		try {
			language = File.createTempFile("template", ".xml");
			BufferedReader br = new BufferedReader(new InputStreamReader(ir));
			String line = br.readLine();
			PrintStream out = new PrintStream(language);
			while (line != null) {
				out.println(line);
				line = br.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Vector<ConstraintDefinition> allDiscoveredConstraints = new Vector<ConstraintDefinition>();
		HashMap<Integer, MetricsValues> constraintMetricsMap = new HashMap<Integer, MetricsValues>();
		TemplateBroker template = XMLBrokerFactory.newTemplateBroker(language.getAbsolutePath());
		List<Language> languages = template.readLanguages();
		Language lang = languages.get(0);
		AssignmentModel model = new AssignmentModel(lang);
		model.setName("new model");
		DeclareMap outputMap = null;
		ActivityDefinition activitydefinition = null;
		Vector<String> activityDefinitions = new Vector<String>();
		// int constraintID = 0;
		// int activityID = 1;
		for (ConstraintDefinition cd : cds) {

			// DeclareTemplate currentTemplate = DeclareMiner.getTemplate(cd);
			// int numberOfParameters = 0;
			// if(isBinary(currentTemplate)){
			// numberOfParameters = 2;
			// }else{
			// numberOfParameters = 1;
			// }

			// DeclareTemplate ctemplate = DeclareMiner.getTemplate(cd);
			// int numberOfParams = 0;
			// if(isBinary(ctemplate)){
			// numberOfParams = 2;
			// }else{
			// numberOfParams = 1;
			// }

			for (Parameter parameter : cd.getParameters()) {
				if (cd.getBranches(parameter).iterator().hasNext()) {
					if (!activityDefinitions.contains(cd.getBranches(parameter).iterator().next().getName())) {
						activityDefinitions.add(cd.getBranches(parameter).iterator().next().getName());
						activitydefinition = model
								.addActivityDefinition(cd.getBranches(parameter).iterator().next().getId());
						activitydefinition.setName(cd.getBranches(parameter).iterator().next().getName());
						activitySupportMap.put(cd.getBranches(parameter).iterator().next().getId(),
								f.getSupport(activitydefinition.getName()) / 100.f);
					}
				} else {
					if (!activityDefinitions.contains("EMPTY_PARAM")) {
						activityDefinitions.add("EMPTY_PARAM");
						activitydefinition = model.addActivityDefinition(-1);
						activitydefinition.setName("EMPTY_PARAM");
						activitySupportMap.put(-1, f.getSupport(activitydefinition.getName()) / 100.f);
					}
				}
			}

			MetricsValues metricsValues = metricsVectorConstraint.get(cd.getId());
			constraintTemplateMap.put(cd.getId(), metricsValues.getTemplate());
			constraintParametersMap.put(cd.getId(), metricsValues.getParameters());
			constraintFormulaMap.put(cd.getId(), metricsValues.getFormula());
			constraintSupportRuleMap.put(cd.getId(), metricsValues.getSupportRule());
			if (isConfidenceEvaluable(cd.getName())) {
				constraintSupportAntecendentMap.put(cd.getId(), metricsValues.getSuppAntec());
				constraintSupportConsequentMap.put(cd.getId(), metricsValues.getSupportConseq());
				constraintConfidenceMap.put(cd.getId(), metricsValues.getConfidence());
				constraintCpirMap.put(cd.getId(), metricsValues.getCPIR());
				constraintInterestFactorMap.put(cd.getId(), metricsValues.getI());
			} else {
				constraintSupportAntecendentMap.put(cd.getId(), Float.MAX_VALUE);
				constraintSupportConsequentMap.put(cd.getId(), Float.MAX_VALUE);
				constraintConfidenceMap.put(cd.getId(), Float.MAX_VALUE);
				constraintCpirMap.put(cd.getId(), Float.MAX_VALUE);
				constraintInterestFactorMap.put(cd.getId(), Float.MAX_VALUE);
			}
			// model.addConstraintDefiniton(cd);
			allDiscoveredConstraints.add(cd);
			constraintMetricsMap.put(cd.getId(), metricsValues);
		}

		for (ConstraintDefinition constraintDefinition : allDiscoveredConstraints) {
			model.addConstraintDefiniton(constraintDefinition);
		}

		AssignmentModelView view = inputMap.getView();
		Vector<String> adIds = new Vector<String>();
		for (ConstraintDefinition constraintdefinition : model.getConstraintDefinitions()) {
			// view.setConstraintDefinitionVisible(constraintdefinition);
			// constraintdefinition.setVisible(true);
			for (Parameter p : constraintdefinition.getParameters()) {
				if (constraintdefinition.getBranches(p).iterator().hasNext()) {
					adIds.add(constraintdefinition.getBranches(p).iterator().next().getName()
							.replace("<html><body text=404040>", "<html>").replace("</body></html>", "</html>"));
				} else {
					adIds.add("EMPTY_PARAM");
				}
			}

			Color constraintDefinitionColor;
			int green = 255 - (int) (255 * constraintSupportRuleMap.get(constraintdefinition.getId()));
			constraintDefinitionColor = new Color(255, green, green);
			if (constraintdefinition.getName().contains("alternate")
					|| constraintdefinition.getName().contains("chain")) {
				view.setConstraintDefinitionColor(constraintdefinition, Color.black, constraintDefinitionColor,
						constraintDefinitionColor);
			} else {
				view.setConstraintDefinitionColor(constraintdefinition, constraintDefinitionColor,
						constraintDefinitionColor, constraintDefinitionColor);
			}
		}
		Color activityDefinitionColor;
		HashMap<Integer, HashMap<String, Float>> balancingStartCompletePerActivityDefinition = new HashMap<Integer, HashMap<String, Float>>();
		for (ActivityDefinition ad : model.getActivityDefinitions()) {
			String activityName = ad.getName();
			if (input.getAprioriKnowledgeBasedCriteriaSet()
					.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
				if (!hasEventTypeInName(activityName)) {
					activityName = activityName + "-" + input.getReferenceEventType();
				}
			}

			int blue = 255 - (int) (255 * (f.getSupport(ad.getName()) / 100.f));
			activityDefinitionColor = new Color(255, 255, blue);
			//////////////////////////// REMOVE///////////////////////
			// activityDefinitionColor = Color.white;
			////////////////////////// REMOVE///////////////////////
			view.setActivityDefinitionBackground(ad, activityDefinitionColor);
			HashMap<String, Float> balanceStartComplete = getBalance(removeEventTypeFromActivityName(ad.getName()),
					log);
			balancingStartCompletePerActivityDefinition.put(ad.getId(), balanceStartComplete);
		}

		// for(ActivityDefinition ad: model.getActivityDefinitions()){
		// if(!adIds.contains(ad.getName().replace("<html><body
		// text=404040>","<html>").replace("</body></html>", "</html>"))){
		// view.setActivityDefinitionInvisible(ad);
		// ad.setVisible(false);
		// }else{
		// view.setActivityDefinitionVisible(ad);
		// ad.setVisible(true);
		// }
		// }
		outputMap = new DeclareMap(model, null, view, null, null, null);
		DeclareExtensionOutput visualizationData = new DeclareExtensionOutput();
		visualizationData.setAllDiscoveredConstraints(allDiscoveredConstraints);
		visualizationData.setAllActivities(model.getActivityDefinitions());
		visualizationData.setConfidence(constraintConfidenceMap);
		visualizationData.setCPIR(constraintCpirMap);
		visualizationData.setFormula(constraintFormulaMap);
		visualizationData.setI(constraintInterestFactorMap);
		visualizationData.setModel(outputMap);
		visualizationData.setParameters(constraintParametersMap);
		visualizationData.setSuppAntec(constraintSupportAntecendentMap);
		visualizationData.setSupportConseq(constraintSupportConsequentMap);
		visualizationData.setSupportRule(constraintSupportRuleMap);
		visualizationData.setTemplate(constraintTemplateMap);
		visualizationData.setMappingAdCd(activityConstraintMap);
		visualizationData.setActSupp(activitySupportMap);
		visualizationData.setBlnc(balancingStartCompletePerActivityDefinition);
		visualizationData.setTransitiveClosureResponseConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosureRespondedExistenceConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosurePrecedenceConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosureSuccessionConstraints(new Vector<Integer>());
		visualizationData.setTransitiveClosureCoexistenceConstraints(new Vector<Integer>());
		visualizationData.setLog(log);

		Watch timeInformationWatch = new Watch();
		timeInformationWatch.start();
		TemplateInfo templateInfo = null;
		HashMap<Object, double[]> constraintIdTimeInstancesMap = new HashMap<Object, double[]>();
		AssignmentModel assignmentModel = visualizationData.getModel().getModel();
		HashMap<Object, Integer> activationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> fulfillmentsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> violationsMap = new HashMap<Object, Integer>();
		HashMap<Object, Integer> conflictsMap = new HashMap<Object, Integer>();
		HashMap<Object, Long> maxTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> minTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> avgTimeDistancesMap = new HashMap<Object, Long>();
		HashMap<Object, Long> stdDevTimeDistancesMap = new HashMap<Object, Long>();

		XAttributeMap eventAttributeMap;
		boolean timed = input.getDeclarePerspectiveSet().contains(DeclarePerspective.Time) ? true : false;
		if (timed) {
			for (ConstraintDefinition constraintDefinition : assignmentModel.getConstraintDefinitions()) {

				int numberOfActivations = 0;
				int numberOfViolations = 0;
				int numberOfFulfillments = 0;
				int numberOfConflicts = 0;
				Vector<Long> timeDists = new Vector<Long>();
				for (XTrace trace : log) {

					List<Integer> traceIndexes = new LinkedList<Integer>();
					List<String> traceEvents = new LinkedList<String>();
					int i = 0;
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						traceEvents.add((eventAttributeMap.get(XConceptExtension.KEY_NAME) + "-"
								+ eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION)).toLowerCase());
						traceIndexes.add(i);
						// System.out.println("eventoooo:
						// "+eventAttributeMap.get(XConceptExtension.KEY_NAME));
						i++;
					}
					ExecutionsTree executiontree = new ExecutionsTree(traceEvents, traceIndexes, constraintDefinition);
					Set<Integer> activations = executiontree.getActivations();
					Set<Integer> violations = executiontree.getViolations();
					Set<Integer> fulfillments = executiontree.getFulfillments();
					Set<Integer> conflicts = executiontree.getConflicts();
					// String correlation =
					// correlationsPerConstraint.get(constraintDefinition.getId());
					switch (visualizationData.getTemplate().get(constraintDefinition.getId())) {
						case Succession:
						case Alternate_Succession:
						case Chain_Succession:
							templateInfo = new SuccessionInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Choice:
							templateInfo = new ChoiceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Exclusive_Choice:
							templateInfo = new ExclusiveChoiceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence:
							templateInfo = new ExistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence2:
							templateInfo = new Existence2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Existence3:
							templateInfo = new Existence3Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Init:
							templateInfo = new InitInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Absence:
							templateInfo = new AbsenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Absence2:
							templateInfo = new Absence2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Absence3:
							templateInfo = new Absence3Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Exactly1:
							templateInfo = new Exactly1Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Exactly2:
							templateInfo = new Exactly2Info();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Responded_Existence:
							templateInfo = new RespondedExistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Response:
						case Alternate_Response:
						case Chain_Response:
							templateInfo = new ResponseInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Precedence:
						case Alternate_Precedence:
						case Chain_Precedence:
							templateInfo = new PrecedenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case CoExistence:
							templateInfo = new CoexistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, fulfillments));
							break;
						case Not_CoExistence:
							templateInfo = new NotCoexistenceInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
						case Not_Succession:
						case Not_Chain_Succession:
							templateInfo = new NegativeRelationInfo();
							timeDists.addAll(
									templateInfo.getTimeDistances(input, trace, constraintDefinition, conflicts));
							break;
					}
					numberOfActivations = numberOfActivations + activations.size();
					numberOfViolations = numberOfViolations + violations.size();
					numberOfFulfillments = numberOfFulfillments + fulfillments.size();
					numberOfConflicts = numberOfConflicts + conflicts.size();

				}
				double[] timeInstances = new double[timeDists.size()];
				int timeInstanceIndex = 0;
				for (Long time : timeDists)
					timeInstances[timeInstanceIndex++] = time;

				if (timeDists.size() > 0) {
					maxTimeDistancesMap.put(constraintDefinition.getId(), Collections.max(timeDists));
					minTimeDistancesMap.put(constraintDefinition.getId(), Collections.min(timeDists));
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), timeInstances);
				} else {
					maxTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					minTimeDistancesMap.put(constraintDefinition.getId(), (long) -1);
					constraintIdTimeInstancesMap.put(constraintDefinition.getId(), new double[] { -1 });
				}
				double avg = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					avg = avg + timeDists.get(i);
				}
				avg = avg / timeDists.size();
				avgTimeDistancesMap.put(constraintDefinition.getId(), (long) avg);

				double stddev = 0;
				for (int i = 0; i < timeDists.size(); i++) {
					stddev = stddev + ((timeDists.get(i) - avg) * (timeDists.get(i) - avg));
				}
				stddev = stddev / (timeDists.size() - 1);
				stddev = Math.sqrt(stddev);
				stdDevTimeDistancesMap.put(constraintDefinition.getId(), (long) stddev);

				activationsMap.put(constraintDefinition.getId(), numberOfActivations);
				violationsMap.put(constraintDefinition.getId(), numberOfViolations);
				fulfillmentsMap.put(constraintDefinition.getId(), numberOfFulfillments);
				conflictsMap.put(constraintDefinition.getId(), numberOfConflicts);
			}
			// if(pw!=null){
			// pw.println("END TIME INFORMATION EVALUATION - time:
			// "+timeInformationWatch.msecs()+" msecs");
			// }

			visualizationData.setConstraintIdTimeInstancesMap(constraintIdTimeInstancesMap);
			visualizationData.setActivations(activationsMap);
			visualizationData.setViolations(violationsMap);
			visualizationData.setFulfillments(fulfillmentsMap);
			visualizationData.setConflicts(conflictsMap);
			visualizationData.setMaxTD(maxTimeDistancesMap);
			visualizationData.setMinTD(minTimeDistancesMap);
			visualizationData.setAvgTD(avgTimeDistancesMap);
			visualizationData.setStdDevTD(stdDevTimeDistancesMap);
			visualizationData.setExtend(false);
		}
		visualizationData.setInput(input);

		return visualizationData;
	}

	public boolean hasEventTypeInName(String activityName) {
		return (activityName.endsWith("-assign") || activityName.endsWith("-ate_abort")
				|| activityName.endsWith("-suspend") || activityName.endsWith("-complete")
				|| activityName.endsWith("-autoskip") || activityName.endsWith("-manualskip")
				|| activityName.endsWith("pi_abort") || activityName.endsWith("-reassign")
				|| activityName.endsWith("-resume") || activityName.endsWith("-schedule")
				|| activityName.endsWith("-start") || activityName.endsWith("-unknown")
				|| activityName.endsWith("-withdraw"));
	}

	public String getEventType(String activityName) {
		if (!(activityName.endsWith("-assign") || activityName.endsWith("-ate_abort")
				|| activityName.endsWith("-suspend") || activityName.endsWith("-complete")
				|| activityName.endsWith("-autoskip") || activityName.endsWith("-manualskip")
				|| activityName.endsWith("pi_abort") || activityName.endsWith("-reassign")
				|| activityName.endsWith("-resume") || activityName.endsWith("-schedule")
				|| activityName.endsWith("-start") || activityName.endsWith("-unknown")
				|| activityName.endsWith("-withdraw"))) {
			return null;
		} else {
			return activityName.split("-")[activityName.split("-").length - 1];
		}
	}

	public Vector<Integer> getTransitiveClosureSuccessionConstraints() {
		return transitiveClosureSuccessionConstraints;
	}

	public void setTransitiveClosureSuccessionConstraints(Vector<Integer> transitiveClosureSuccessionConstraints) {
		this.transitiveClosureSuccessionConstraints = transitiveClosureSuccessionConstraints;
	}

	public Vector<Integer> getTransitiveClosureCoexistenceConstraints() {
		return transitiveClosureCoexistenceConstraints;
	}

	public void setTransitiveClosureCoexistenceConstraints(Vector<Integer> transitiveClosureCoexistenceConstraints) {
		this.transitiveClosureCoexistenceConstraints = transitiveClosureCoexistenceConstraints;
	}

	public Vector<Integer> getTransitiveClosureResponseConstraints() {
		return transitiveClosureResponseConstraints;
	}

	public void setTransitiveClosureResponseConstraints(Vector<Integer> transitiveClosureResponseConstraints) {
		this.transitiveClosureResponseConstraints = transitiveClosureResponseConstraints;
	}

	public Vector<Integer> getTransitiveClosurePrecedenceConstraints() {
		return transitiveClosurePrecedenceConstraints;
	}

	public void setTransitiveClosurePrecedenceConstraints(Vector<Integer> transitiveClosurePrecedenceConstraints) {
		this.transitiveClosurePrecedenceConstraints = transitiveClosurePrecedenceConstraints;
	}

	public Vector<Integer> getTransitiveClosureRespondedExistenceConstraints() {
		return transitiveClosureRespondedExistenceConstraints;
	}

	public void setTransitiveClosureRespondedExistenceConstraints(
			Vector<Integer> transitiveClosureRespondedExistenceConstraints) {
		this.transitiveClosureRespondedExistenceConstraints = transitiveClosureRespondedExistenceConstraints;
	}

	public Vector<Integer> getTransitiveClosureNotCoexistenceConstraints() {
		return transitiveClosureNotCoexistenceConstraints;
	}

	public void setTransitiveClosureNotCoexistenceConstraints(
			Vector<Integer> transitiveClosureNotCoexistenceConstraints) {
		this.transitiveClosureNotCoexistenceConstraints = transitiveClosureNotCoexistenceConstraints;
	}

}

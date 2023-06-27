/***********************************************************
 * This software is part of the ProM package * http://www.processmining.org/ * *
 * Copyright (c) 2003-2006 TU/e Eindhoven * and is licensed under the * Common
 * Public License, Version 1.0 * by Eindhoven University of Technology *
 * Department of Information Systems * http://is.tm.tue.nl * *
 **********************************************************/

package org.processmining.plugins.declareminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.declare2ltl.DeclareExtensionOutput;
import org.processmining.plugins.declareminer.apriori.FindItemSets;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareProMInput;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.FrequentItemSetType;
import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.templates.Absence2Info;
import org.processmining.plugins.declareminer.templates.AbsenceInfo;
import org.processmining.plugins.declareminer.templates.ChoiceInfo;
import org.processmining.plugins.declareminer.templates.CoexistenceInfo;
import org.processmining.plugins.declareminer.templates.Exactly1Info;
import org.processmining.plugins.declareminer.templates.ExclusiveChoiceInfo;
import org.processmining.plugins.declareminer.templates.ExistenceInfo;
import org.processmining.plugins.declareminer.templates.InitInfo;
import org.processmining.plugins.declareminer.templates.NegativeRelationInfo;
import org.processmining.plugins.declareminer.templates.NotCoexistenceInfo;
import org.processmining.plugins.declareminer.templates.PrecedenceInfo;
import org.processmining.plugins.declareminer.templates.ResponseInfo;
import org.processmining.plugins.declareminer.templates.SuccessionInfo;
import org.processmining.plugins.declareminer.templates.TemplateInfo;
import org.processmining.plugins.declareminer.ui.DeclareMinerConfigurationUI;
import org.processmining.plugins.declareminer.util.CombinationGenerator;
import org.processmining.plugins.declareminer.util.DeclareExportComplete;
import org.processmining.plugins.declareminer.util.DispositionsGenerator;
import org.processmining.plugins.declareminer.util.UnifiedLogger;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.IItem;
import org.processmining.plugins.declareminer.visualizing.Language;
import org.processmining.plugins.declareminer.visualizing.LanguageGroup;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.TemplateBroker;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

@Plugin(name = "Declare Miner", parameterLabels = { "Log", "Reference Model" }, returnLabels = {
		"Mined Model" }, returnTypes = { DeclareMinerOutput.class }, userAccessible = true)
public class DeclareMinerNoRed {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl", uiLabel = "Declare Maps Miner No Reductions", pack = "DeclareMiner")
	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Declare Maps Miner")
	public DeclareMinerOutput analyse(UIPluginContext context, XLog log) {
		DeclareMinerConfigurationUI declareMinerConfigurationUI = new DeclareMinerConfigurationUI(context,
				DeclareProMInput.Log_Only);
		DeclareMinerInput input = declareMinerConfigurationUI.getInput();
		input.setReferenceEventType("complete");
		if (input.isEmpty())
			return null;
		return mineDeclareConstraints(context, log, input);

	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl", uiLabel = "Repair a Declare Model", pack = "DeclareMiner")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Repair a Declare Model")
	public DeclareMinerOutput analyse(UIPluginContext context, XLog log, DeclareMap inputModel) {
		DeclareMinerConfigurationUI declareMinerConfigurationUI = new DeclareMinerConfigurationUI(context,
				DeclareProMInput.Log_and_Model);
		DeclareMinerInput input = declareMinerConfigurationUI.getInput();
		input.setReferenceEventType("complete");
		if (input.isEmpty())
			return null;
		return mineDeclareConstraints(context, log, input, inputModel);

	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl", uiLabel = "Repair a Declare Map", pack = "DeclareMiner")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Repair a Declare Map")
	public DeclareMinerOutput analyse(UIPluginContext context, XLog log, DeclareMinerOutput inputModel) {
		DeclareMinerConfigurationUI declareMinerConfigurationUI = new DeclareMinerConfigurationUI(context,
				DeclareProMInput.Log_and_Model);
		DeclareMinerInput input = declareMinerConfigurationUI.getInput();
		input.setReferenceEventType("complete");
		if (input.isEmpty())
			return null;
		return mineDeclareConstraints(context, log, input, inputModel);

	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl", uiLabel = "Repair a Declare Map", pack = "DeclareMiner")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Repair a Declare Map")
	public DeclareMinerOutput analyse(UIPluginContext context, XLog log, DeclareExtensionOutput inputModel) {
		DeclareMinerConfigurationUI declareMinerConfigurationUI = new DeclareMinerConfigurationUI(context,
				DeclareProMInput.Log_and_Model);
		DeclareMinerInput input = declareMinerConfigurationUI.getInput();
		input.setReferenceEventType("complete");
		if (input.isEmpty())
			return null;
		return mineDeclareConstraints(context, log, input, inputModel);

	}

	public static DeclareMinerOutput mineDeclareConstraints(UIPluginContext context, XLog log, DeclareMinerInput input,
			DeclareMap inputModel) {
		DeclareMinerOutput declareMinerOutput = null;
		printInputConfiguration(input);

		Set<Set<String>> conceptGroupingSet = new HashSet<Set<String>>();

		// String parentDir = "C:\\Users\\fmaggi\\Desktop";
		String parentDir = input.getAprioriKnowledgeConceptFileName();
		if (parentDir != null) {
			File[] conceptGroupFileNames = new File(parentDir).listFiles();

			// String group1FileName = "G1.txt";
			// String group2FileName = "G2.txt";
			// String group3FileName = "G3.txt";
			//
			FileIO io = new FileIO();
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group1FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group2FileName));
			// System.out.println("G3: "+io.readFileAsSet(parentDir, group3FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group3FileName));

			for (File conceptGroupFile : conceptGroupFileNames) {
				conceptGroupingSet.add(io.readFileAsSet(parentDir, conceptGroupFile.getName()));
			}

			System.out.println("COncept Groups: " + conceptGroupingSet);

		}
		if (context != null) {
			context.getProgress().setValue(5);
		}
		float support = input.getMinSupport();
		float alpha = input.getAlpha() / 100.0f;

		FindItemSets finder = new FindItemSets(log, input);

		boolean detectActivitiesAutomatically = false; // input.getMapTemplateConfiguration();
		boolean detectTemplatesAutomatically = false; // input.isDetectTemplatesAutomatically();
		boolean detectSupportAutomatically = false; // input.isDetectSupportAutomatically();
		boolean strengthen = false;
		if (input.getMapTemplateConfiguration()
				.equals(MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossAllActivitesInLog)) {
			detectTemplatesAutomatically = true;
		}
		if (input.getMapTemplateConfiguration()
				.equals(MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossActivitiesOnlyInTheMap)) {
			detectTemplatesAutomatically = true;
			detectActivitiesAutomatically = true;
		}

		if (input.getMapTemplateConfiguration().equals(MapTemplateConfiguration.StrengthenMap)) {
			detectActivitiesAutomatically = true;
			strengthen = true;
		}

		if (detectSupportAutomatically) {
			support = learnSupport(input, inputModel.getModel(), log, alpha, finder);
		}

		// change traditional = true to start the traditional algorithm
		boolean traditional = false;
		boolean printVerbose = false;

		// String output = "C:\\Users\\fmaggi\\Desktop\\Exp\\";
		// if(traditional){
		// output = output+"alpha"+alpha+"Traditional.txt";
		// }else{
		// output = output+"alpha"+alpha+"Apriori"+support+".txt";
		// }

		String output = input.getOutputDir() + System.getProperty("file.separator") + input.getOutputFileName();

		Watch overall = new Watch();
		if (printVerbose) {
			overall.start();
		}

		Watch aprioriWatch = new Watch();
		Watch aprioriLocalWatch = new Watch();

		try {
			PrintWriter pw = null;
			if (printVerbose) {
				pw = new PrintWriter(new FileWriter(new File(output)));
				if (traditional) {
					pw.println("trad");
				} else {
					pw.println("minimum support for apriori algorithm: " + (support) / 100.0);
				}
				pw.println("alpha value: " + alpha);
				pw.println("  ");
				pw.flush();
				aprioriWatch.start();
				aprioriLocalWatch.start();
			}
			DeclareModelGenerator dmg = new DeclareModelGenerator();

			List<String> activityNameList = new ArrayList<String>();
			System.out.println("Detect Activities Automatically: " + detectActivitiesAutomatically);
			if (!detectActivitiesAutomatically) {
				for (XTrace trace : log) {
					for (XEvent event : trace) {
						String label = (XConceptExtension.instance().extractName(event));
						if (input.getAprioriKnowledgeBasedCriteriaSet()
								.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
							if (event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION) != null) {
								label = (XConceptExtension.instance().extractName(event)) + "-"
										+ XLifecycleExtension.instance().extractTransition(event);
							} else {
								if (input.getReferenceEventType() != null)
									label = (XConceptExtension.instance().extractName(event)) + "-"
											+ input.getReferenceEventType();
								else
									label = (XConceptExtension.instance().extractName(event));

							}
						}
						if (!activityNameList.contains(label)) {
							activityNameList.add(label);
						}
					}
				}
			} else {
				for (ActivityDefinition activity : inputModel.getModel().getActivityDefinitions()) {
					String activityName = activity.getName();
					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (!dmg.hasEventTypeInName(activityName)) {
							if (input.getReferenceEventType() != null)
								activityName = activityName + "-" + input.getReferenceEventType();
						}
					}
					activityNameList.add(activityName);
				}
			}
			System.out.println("Activity Name list: " + activityNameList);
			String[] activityNamesArray = new String[activityNameList.size()];
			for (int i = 0; i < activityNameList.size(); i++) {
				activityNamesArray[i] = activityNameList.get(i);
			}

			Map<FrequentItemSetType, Map<Set<String>, Float>> frequentItemSetTypeFrequentItemSetSupportMap = new HashMap<FrequentItemSetType, Map<Set<String>, Float>>();
			// Map<DeclareTemplate, String[][]> declareTemplateCandidateDispositionsMap =
			// new HashMap<DeclareTemplate, String[][]>();
			Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap = new HashMap<DeclareTemplate, List<List<String>>>();

			Hashtable<String, Object> aprioriSupportValues = new Hashtable<String, Object>();

			String[][] candidateListArray = null;
			List<List<String>> candidateList;
			if (printVerbose) {
				pw.println("precomputation time: " + aprioriLocalWatch.msecs() + " msecs");
				pw.println("   ");
				pw.println("START APRIORI");
			}
			if (context != null) {
				context.getProgress().setMinimum(0);
				context.getProgress().setMaximum(112);
				context.getProgress().setIndeterminate(false);
				context.getProgress().setValue(1);
			}

			Set<DeclareTemplate> selectedTemplates = new HashSet<DeclareTemplate>();
			if (!detectTemplatesAutomatically) {
				selectedTemplates = input.getSelectedDeclareTemplateSet();
			} else {
				for (ConstraintDefinition constraint : inputModel.getModel().getConstraintDefinitions()) {
					if (!selectedTemplates.contains(getTemplate(constraint))) {
						selectedTemplates.add(getTemplate(constraint));
					}
				}
				input.setSelectedDeclareTemplateSet(selectedTemplates);
			}
			if (!traditional) {
				for (DeclareTemplate template : selectedTemplates) {
					if (alpha == 0 || template.equals(DeclareTemplate.Choice)
							|| template.equals(DeclareTemplate.Exclusive_Choice)) {

						if (template.equals(DeclareTemplate.Alternate_Precedence)
								|| template.equals(DeclareTemplate.Alternate_Response) ||
								template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.CoExistence)
								|| template.equals(DeclareTemplate.Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, null);
						}
						if (template.equals(DeclareTemplate.Exclusive_Choice)
								|| template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Succession)
								|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Choice)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, null);
						}
					} else {
						if (template.equals(DeclareTemplate.Precedence)
								|| template.equals(DeclareTemplate.Alternate_Precedence) ||
								template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Alternate_Response)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, null);
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, null);
						}
						if (template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.Succession)
								|| template.equals(DeclareTemplate.CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Chain_Succession)
								|| template.equals(DeclareTemplate.Not_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, null);
						}
					}

					if (isOneHalfPositiveFrequentItemType(template)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, null);
					}

					if (template.equals(DeclareTemplate.Absence)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, null);
					}

					if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
						Map<Set<String>, Float> frequentItemSetSupportMap = DeclareModelGenerator
								.getFrequentItemSetSupportMap(1, log, 0, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_All,
								frequentItemSetSupportMap);
					}

				}

				boolean previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Four_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
							support / 4.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Three_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 3.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.Four_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 3.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							String element2 = iterator.next();
							if (!element1.contains("NOT-") && !element2.contains("NOT-")) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Positive)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Positive);
							for (Set<String> key : previousMap.keySet()) {
								if (previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						} else {
							if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Half_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Half_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Three_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Three_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Four_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Four_Negative);
							}
							for (Set<String> key : previousMap.keySet()) {
								Iterator<String> iterator = key.iterator();
								String element1 = iterator.next();
								String element2 = iterator.next();
								if (!element1.contains("NOT-") && !element2.contains("NOT-")
										&& previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					}
					previous = true;
				}

				previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
							support / 2.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					}
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);

					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);
					}
					previous = true;
				}
			}
			if (!strengthen) {
				int numberOfParameters;
				for (DeclareTemplate template : selectedTemplates) {
					if (isBinaryTemplate(template)) {
						numberOfParameters = 2;
					} else {
						numberOfParameters = 1;
					}

					if (traditional) {
						if (printVerbose) {
							aprioriLocalWatch.start();
						}
						candidateListArray = DispositionsGenerator.generateDisp(activityNamesArray, numberOfParameters);
						int noCandidates = candidateListArray.length;
						candidateList = new ArrayList<List<String>>();
						for (int i = 0; i < noCandidates; i++) {
							List<String> candidate = new ArrayList<String>();
							for (int j = 0; j < candidateListArray[i].length; j++)
								candidate.add(candidateListArray[i][j]);
							candidateList.add(candidate);
						}
						// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
						declareTemplateCandidateDispositionsMap.put(template, candidateList);
						if (printVerbose) {
							pw.println("time needed to generate all the dispositions (traditional approach) of size "
									+ numberOfParameters + ": " + aprioriLocalWatch.msecs() + " msecs");
							pw.println("number of dispositions (traditional approach) of size " + numberOfParameters
									+ ": " + candidateList.size());
						}
					} else {
						if (alpha == 0 || template.equals(DeclareTemplate.Choice)
								|| template.equals(DeclareTemplate.Exclusive_Choice)) {
							if (isPositiveFrequentItemType(template)) {

								if (printVerbose) {
									aprioriLocalWatch.start();
								}

								Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Positive);
								int noCandidates = 0;
								candidateListArray = new String[frequentItemSetSupportMap.size()
										* Factorial.fatt(numberOfParameters)][numberOfParameters];
								candidateList = new ArrayList<List<String>>();
								for (Set<String> frequentItemSet : frequentItemSetSupportMap.keySet()) {
									int itemIndex = 0;
									String[] frequentItemSetArray = new String[frequentItemSet.size()];
									for (String freqItem : frequentItemSet)
										frequentItemSetArray[itemIndex++] = freqItem;
									String[][] dispositions = null;
									dispositions = DispositionsGenerator.generateDisp(frequentItemSetArray,
											numberOfParameters);

									for (int i = 0; i < dispositions.length; i++) {
										List<String> candidate = new ArrayList<String>();
										for (int j = 0; j < numberOfParameters; j++) {
											candidateListArray[noCandidates][j] = dispositions[i][j];
											candidate.add(dispositions[i][j]);
										}
										candidateList.add(candidate);
										noCandidates++;
									}
								}

								// print candidates
								// System.out.println("No. candidates: "+noCandidates+" @ CanddiateListSize:
								// "+candidatedList.length);
								// for(int i = 0; i < candidatedList.length; i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidatedList[i][j]+" ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								//

								if ((printVerbose) && alpha == 0) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support
													+ " [positive relation; simple choice]" + ": "
													+ aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support
											+ " [positive relation templates; simple choice]" + ": "
											+ candidateList.size());
								} else if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support
													+ " [simple choice]" + ": " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support + " [simple choice]"
											+ ": " + candidateList.size());
								}
							}

							if (template.equals(DeclareTemplate.Choice)) {
								/*
								 * The basic idea here is to explore all
								 * combinations of size 2 that satisfy the sum of
								 * support of both the individual activities is greater than
								 * minSupp
								 */
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								System.out.println("HERE: " + declareTemplateCandidateDispositionsMap.keySet());
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									int[] indices;
									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}

									// System.out.println("Dispositions");
									// for(int j = 0; j < noDispositions; j++){
									// for(int k = 0; k < 2; k++)
									// System.out.print(dispositions[j][k]+" ");
									// System.out.println();
									// }

									candidateList = new ArrayList<List<String>>();
									int noCandidates = 0;
									// boolean[] isCandidateDisposition = new boolean[noDispositions];
									float suppActivity1, suppActivity2;
									for (int i = 0; i < noDispositions; i++) {
										// isCandidateDisposition[i] = false;
										suppActivity1 = finder.getSupport(dispositions[i][0].replaceAll("NOT-", ""));
										suppActivity2 = finder.getSupport(dispositions[i][1].replaceAll("NOT-", ""));
										if (suppActivity1 + suppActivity2 >= support) {
											List<String> candidate = new ArrayList<String>();
											candidate.add(dispositions[i][0]);
											candidate.add(dispositions[i][1]);
											candidateList.add(candidate);

											// isCandidateDisposition[i] = true;
											noCandidates++;
											// System.out.println("C: " + dispositions[i][0] + " , " +
											// dispositions[i][1]);
										}
									}

									// candidateListArray = new String[noCandidates][numberOfParameters];
									// int candidateIndex = 0;
									// for(int i = 0; i < noDispositions; i++){
									// if(isCandidateDisposition[i]){
									// candidateListArray[candidateIndex][0] = dispositions[i][0];
									// candidateListArray[candidateIndex++][1] = dispositions[i][1];
									// }
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// print candidates

									if ((printVerbose) && alpha == 0) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [simple choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support / 2.f
												+ " [simple choice]" + ": " + candidateList.size());
									} else if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support / 2.f
												+ " [simple choice]" + ": " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Exclusive_Choice)
									|| template.equals(DeclareTemplate.Not_CoExistence)) {
								/*
								 * The basic idea is to explore all combinations of
								 * the type (A, NOT-B) and (NOT-A, B) that have a
								 * support greater than minSupport
								 */
								Set<Set<String>> candidateDispositionSet = new HashSet<Set<String>>();
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
											.get(FrequentItemSetType.Half_Negative);
									System.out.println("Freq Item Set: ");
									for (Set<String> freqItem : frequentItemSetSupportMap.keySet())
										System.out.println(freqItem + " @ " + frequentItemSetSupportMap.get(freqItem));

									System.out.println("Exploring Candidates: " + numberOfParameters);
									int noCandidates = 0;
									for (Set<String> freqItemSet : frequentItemSetSupportMap.keySet()) {
										if (freqItemSet.size() != numberOfParameters)
											continue;
										// check if both activities are positive or both negative; ignore such
										// combinations
										boolean isAllPositive = true;
										boolean isAllNegative = true;
										for (String item : freqItemSet) {
											if (item.contains("NOT-")) {
												isAllPositive = false;
											} else {
												isAllNegative = false;
											}
										}
										/*
										 * it could be that the support of (A,
										 * NOT-B) is less than minSupp and the
										 * support of (B, NOT-A) is less than
										 * minSupp, but the sum of their supports is
										 * greater than minSupport. check for those
										 */
										float complementaryFreqItemSupport;
										Set<String> complementaryFreqItemSet = new HashSet<String>();
										for (String freqItem : freqItemSet) {
											if (freqItem.contains("NOT-")) {
												complementaryFreqItemSet.add(freqItem.replace("NOT-", ""));
											} else {
												complementaryFreqItemSet.add("NOT-" + freqItem);
											}
										}
										Iterator<String> it = complementaryFreqItemSet.iterator();
										complementaryFreqItemSupport = finder.getSupport(it.next(), it.next());

										if (!isAllNegative && !isAllPositive
												&& (frequentItemSetSupportMap.get(freqItemSet)
														+ complementaryFreqItemSupport) >= support) {
											Set<String> candidateSet = new HashSet<String>();
											for (String item : freqItemSet)
												candidateSet.add(item.replace("NOT-", ""));
											if (candidateSet.size() != numberOfParameters) {
												System.out.println("Strange for this to be here: " + freqItemSet + " @ "
														+ frequentItemSetSupportMap.get(freqItemSet) + " @ " + support
														+ " @ " + candidateSet);
												continue;
											}
											candidateDispositionSet.add(candidateSet);
										}
									}

									noCandidates = candidateDispositionSet.size();
									System.out.println("No. candidates: " + candidateDispositionSet.size());
									for (Set<String> candidateDisposition : candidateDispositionSet)
										System.out.println(candidateDisposition);
									System.out.println("----------");

									candidateList = new ArrayList<List<String>>();
									candidateListArray = new String[noCandidates][numberOfParameters];
									for (Set<String> candidateDisposition : candidateDispositionSet) {
										List<String> candidate = new ArrayList<String>();
										Iterator<String> candidateDispositionIterator = candidateDisposition.iterator();
										while (candidateDispositionIterator.hasNext()) {
											candidate.add(candidateDispositionIterator.next());
											// candidateListArray[candidateIndex][paramIndex++] =
											// candidateDispositionIterator.next();
										}
										candidateList.add(candidate);
									}

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(List<String> candidate: candidateList){
									// System.out.println(candidate);
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence,
									// candidatedList);
									if ((printVerbose) && alpha == 0) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [not co-existence; exclusive choice]" + ": "
														+ aprioriLocalWatch.msecs() + " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [not co-existence; exclusive choice]" + ": "
														+ candidateList.size());
									} else if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Not_Chain_Succession)
									|| template.equals(DeclareTemplate.Not_Succession)) {
								/*
								 * We should consider dispositions of the form (A,
								 * B), (A, NOT-B) and (NOT-A, B) provided the sum of
								 * their support is > minSupport
								 */
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									if (printVerbose) {
										// pw.println("start generation (positive/negative) frequent sets of size
										// "+noparam+" with support "+supp/3.f+" [not succession]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppFirstPositive, suppFirstNegative;
									for (int i = 0; i < noDispositions; i++) {
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);// supp(A,B)
										suppFirstPositive = finder.getSupport(dispositions[i][0],
												"NOT-" + dispositions[i][1]);// supp (A,NOT-B);
										suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0],
												dispositions[i][1]);// supp (NOT-A,B);

										if (suppBothPositive + suppFirstPositive + suppFirstNegative >= support) {
											// we should add both (A,B) and (B,A) as candidates
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											candidateList.add(candidateDisposition);
											// candidateDispositionSet.add(candidateDisposition);
											System.out.println(candidateDisposition);

											candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											candidateList.add(candidateDisposition);
											// candidateDispositionSet.add(candidateDisposition);
											System.out.println(candidateDisposition);
										}
									}
									System.out.println("No. Candidates: " + candidateList.size());
									// System.out.println("No. Candidates: "+candidateDispositionSet.size());
									//
									// int candidateIndex = 0;
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									// }

									// System.out.println("Candidate List Size: "+candidateList.length);
									// for(int i = 0; i < candidateList.length; i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateList[i][j]+", ");
									// System.out.println();
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
									// candidatedList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
									// candidatedList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 3.f
														+ " [not succession]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 3.f
														+ " [not succession]" + ": " + candidateList.size());
									}
								}
							}

						} else {
							System.out.println("alpha " + alpha);
							if (template.equals(DeclareTemplate.Precedence)
									|| template.equals(DeclareTemplate.Alternate_Precedence) ||
									template.equals(DeclareTemplate.Chain_Precedence) ||
									template.equals(DeclareTemplate.Responded_Existence)
									|| template.equals(DeclareTemplate.Response) ||
									template.equals(DeclareTemplate.Chain_Response)
									|| template.equals(DeclareTemplate.Alternate_Response)) {
								if (((template.equals(DeclareTemplate.Responded_Existence)
										|| template.equals(DeclareTemplate.Response) ||
										template.equals(DeclareTemplate.Chain_Response)
										|| template.equals(DeclareTemplate.Alternate_Response))
										&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("respon"))
										|| ((template.equals(DeclareTemplate.Precedence)
												|| template.equals(DeclareTemplate.Alternate_Precedence) ||
												template.equals(DeclareTemplate.Chain_Precedence))
												&& !frequentItemSetTypeFrequentItemSetSupportMap
														.containsKey("precedence"))) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									if (printVerbose) {
										// pw.println("start generation for (positive) frequent sets of size "+noparam+"
										// and (positive/negative) frequent sets of size 1 with support "+supp/2.f+"
										// [responded existence; (simple, alternate, chain) response; (simple,
										// alternate, chain) precedence]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}

									// print the supp values
									for (String activity : activityNameList) {
										System.out.println(activity + " @ " + finder.getSupport(activity));
									}
									for (int i = 0; i < noDispositions; i++) {
										System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
												+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppFirstNegative, suppSecondNegative;
									for (int i = 0; i < noDispositions; i++) {
										// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A) >
										// minSupp
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
										suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0]);
										suppSecondNegative = finder.getSupport("NOT-" + dispositions[i][1]);
										boolean bothDirection = true;
										if (suppBothPositive + suppFirstNegative >= support) {
											// add (A,B)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
											// System.out.println(candidateDisposition);
											bothDirection = false;
										}

										if (suppBothPositive + suppSecondNegative >= support) {
											// add (B,A)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
											// System.out.println(candidateDisposition);
											if (!bothDirection)
												bothDirection = true;
											else
												bothDirection = false;
										}

										// if(!bothDirection){
										// System.out.println("this dispositon contains only one direction");
										// }

									}

									System.out.println("No. Candidates: " + candidateList.size());
									for (List<String> candidate : candidateList)
										System.out.println(candidate);
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// int candidateIndex = 0;
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									//// System.out.println(candidateDisposition);
									// }

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters
														+ " and (positive/negative) frequent sets of size 1 with support "
														+ support / 2.f
														+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
														+ aprioriLocalWatch.msecs() + " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters
												+ " and (positive/negative) frequent sets of size 1 with support "
												+ support / 2.f
												+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
												+ candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Alternate_Succession)
									|| template.equals(DeclareTemplate.Chain_Succession) ||
									template.equals(DeclareTemplate.Succession)
									|| template.equals(DeclareTemplate.CoExistence)) {
								if ((template.equals(DeclareTemplate.CoExistence)
										&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("coexistence"))
										|| ((template.equals(DeclareTemplate.Alternate_Succession)
												|| template.equals(DeclareTemplate.Chain_Succession) ||
												template.equals(DeclareTemplate.Succession)
														&& !frequentItemSetTypeFrequentItemSetSupportMap
																.containsKey("succession")))) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}
									if (printVerbose) {
										// pw.println("start generation (positive/negative) frequent sets of size
										// "+noparam+" with support "+supp/2.f+" [succession; co-existence]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// print the supp values
									for (String activity : activityNameList) {
										System.out.println(activity + " @ " + finder.getSupport(activity));
									}
									for (int i = 0; i < noDispositions; i++) {
										System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
												+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppBothNegative;
									for (int i = 0; i < noDispositions; i++) {
										// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A, NOT-B)
										// > minSupp
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
										suppBothNegative = finder.getSupport("NOT-" + dispositions[i][0],
												"NOT-" + dispositions[i][1]);

										if (suppBothPositive + suppBothNegative >= support) {
											// add (A,B) and (B,A) both (but for co-existence only one)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);

											System.out.println(candidateDisposition);

											if (!template.equals(DeclareTemplate.CoExistence)) {
												candidateDisposition = new ArrayList<String>();
												candidateDisposition.add(dispositions[i][1]);
												candidateDisposition.add(dispositions[i][0]);
												// candidateDispositionSet.add(candidateDisposition);
												candidateList.add(candidateDisposition);
											}
										}

										// if(!bothDirection){
										// System.out.println("this dispositon contains only one direction");
										// }

									}

									System.out.println("No. Candidates: " + candidateList.size());
									// System.out.println("No. Candidates: "+candidateDispositionSet.size());
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// int candidateIndex = 0;
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									// System.out.println(candidateDisposition);
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }

									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [succession; co-existence]: " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [succession; co-existence]: " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Not_CoExistence)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" with support "+supp/3.f+" [not co-existence]");
								}

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;
									// int noDispositions = 0;
									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}
								candidateList = new ArrayList<List<String>>();
								// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
								float suppBothPositive;
								for (int i = 0; i < noDispositions; i++) {
									// for every combination (A,B), if 1-supp(A,B) >= minSupport, then we need to
									// explore
									suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
									if (100 - suppBothPositive >= support) {
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][0]);
										candidateDisposition.add(dispositions[i][1]);
										// candidateDispositionSet.add(candidateDisposition);
										candidateList.add(candidateDisposition);
									}
								}
								System.out.println("No. Candidates: " + candidateList.size());
								// System.out.println("No. Candidates: "+candidateDispositionSet.size());
								// candidateListArray = new
								// String[candidateDispositionSet.size()][numberOfParameters];
								// int candidateIndex = 0;
								// for(List<String> candidateDisposition : candidateDispositionSet){
								// int paramIndex = 0;
								// for(String candidateItem : candidateDisposition)
								// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
								// candidateIndex++;
								//// System.out.println(candidateDisposition);
								// }

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(int i = 0; i < candidateList.size(); i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateListArray[i][j]+", ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for f(positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 3.f
													+ " [not co-existence]: " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 3.f
											+ " [not co-existence]: " + candidateList.size());
								}
							}

							if (template.equals(DeclareTemplate.Not_Succession)
									|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									/*
									 * need to add both (A,B) and (B,A) irrespective of support values
									 */

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}

									candidateList = new ArrayList<List<String>>();
									System.out.println("No. Candidates: " + dispositions.length * 2);
									candidateListArray = new String[dispositions.length * 2][numberOfParameters];
									for (int i = 0; i < noDispositions; i++) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(dispositions[i][0]);
										candidate.add(dispositions[i][1]);
										candidateList.add(candidate);

										candidate = new ArrayList<String>();
										candidate.add(dispositions[i][1]);
										candidate.add(dispositions[i][0]);
										candidateList.add(candidate);

										// candidateListArray[candidateIndex][0] = dispositions[i][0];
										// candidateListArray[candidateIndex][1] = dispositions[i][1];
										// candidateIndex++;
										// candidateListArray[candidateIndex][0] = dispositions[i][1];
										// candidateListArray[candidateIndex][1] = dispositions[i][0];
										// candidateIndex++;
									}

									System.out.println("Candidate List Size: " + candidateList.size());

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }
									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
									// candidatedList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
									// candidatedList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 4.f
														+ " [not succession]: " + aprioriLocalWatch.msecs() + " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 4.f
														+ " [not succession]: " + candidateList.size());
									}
								}
							}
						}

						if (template.equals(DeclareTemplate.Exactly1) || template.equals(DeclareTemplate.Exactly2) ||
								template.equals(DeclareTemplate.Existence)
								|| template.equals(DeclareTemplate.Existence2) ||
								template.equals(DeclareTemplate.Existence3) || template.equals(DeclareTemplate.Init)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive) frequent sets of size "+noparam+"
									// [unary templates]");
								}

								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									if (finder.getSupport(activity) >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(activity);
										candidateList.add(candidate);
									}
								}
								System.out.println("Candidate List Size: " + candidateList.size());
								for (List<String> candidate : candidateList)
									System.out.println(candidate);

								declareTemplateCandidateDispositionsMap.put(template, candidateList);

								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ noparam + " [unary templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size " + noparam
											+ " [unary templates]: " + candidateList.size());
								}

							}
						}

						if (template.equals(DeclareTemplate.Absence)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" [absence templates]");
								}

								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									if (finder.getSupport("NOT-" + activity) >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(activity);
										candidateList.add(candidate);
									}
								}

								System.out.println("Candidate List Size: " + candidateList.size());

								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ noparam + " [absence templates]: " + candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" [absence templates]");
								}
								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									List<String> candidate = new ArrayList<String>();
									candidate.add(activity);
									candidateList.add(candidate);
								}

								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ noparam + " [absence templates]: " + candidateList.size());
								}
							}
						}
					}
				}
				if (printVerbose) {
					pw.println("END APRIORI - time: " + aprioriWatch.msecs() + " msecs");
					pw.println("  ");
				}

				int value = 0;

				System.out.println("JC: " + declareTemplateCandidateDispositionsMap.keySet());
				for (DeclareTemplate template : selectedTemplates) {

					candidateList = declareTemplateCandidateDispositionsMap.get(template);
					if (candidateList != null) {
						value = value + candidateList.size();
					}
				}

				if (context != null)
					context.getProgress().setMaximum(value);

			} else {
				for (ConstraintDefinition cd : inputModel.getModel().getConstraintDefinitions()) {

					if (!selectedTemplates.contains(getTemplate(cd))) {
						selectedTemplates.add(getTemplate(cd));
					}

					Iterator<Parameter> iter = cd.getParameters().iterator();
					Parameter p1 = iter.next();
					Parameter p2 = null;
					if (iter.hasNext()) {
						p2 = iter.next();
					}
					String key = "EMPTY_PARAM";
					ArrayList<String> param = new ArrayList<String>();
					if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p1).iterator().next() != null) {
						key = cd.getBranches(p1).iterator().next().getName();

					}

					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (!dmg.hasEventTypeInName(key)) {
							key = key + "-" + input.getReferenceEventType();
						}
					}
					param.add(key);
					if (p2 != null) {
						if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p2).iterator().next() != null) {
							key = cd.getBranches(p2).iterator().next().getName();
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
								if (!dmg.hasEventTypeInName(key)) {
									key = key + "-" + input.getReferenceEventType();
								}
							}
							param.add(key);
						} else {
							key = "EMPTY_PARAM";
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
								if (!dmg.hasEventTypeInName(key)) {
									key = key + "-" + input.getReferenceEventType();
								}
							}
							param.add(key);
						}
					}

					ArrayList<String> invPar = new ArrayList<String>();
					if (param.size() > 1) {
						invPar.add(param.get(1));
						invPar.add(param.get(0));
					}
					List<List<String>> currparams = null;
					if (declareTemplateCandidateDispositionsMap.containsKey(getTemplate(cd))) {
						currparams = declareTemplateCandidateDispositionsMap.get(getTemplate(cd));
					} else {
						currparams = new ArrayList<List<String>>();
					}
					if (getTemplate(cd).equals(DeclareTemplate.Precedence)
							|| getTemplate(cd).equals(DeclareTemplate.Alternate_Precedence) ||
							getTemplate(cd).equals(DeclareTemplate.Chain_Precedence)) {
						currparams.add(invPar);
					} else {
						currparams.add(param);
					}
					declareTemplateCandidateDispositionsMap.put(getTemplate(cd), currparams);

					if (cd.getName().equals("chain response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

					}

					if (cd.getName().equals("chain precedence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
					}

					if (cd.getName().equals("alternate response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
					}

					if (cd.getName().equals("alternate precedence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
					}

					if (cd.getName().equals("alternate succession")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
					}

					if (cd.getName().equals("response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Response)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Response);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

					}

					if (cd.getName().equals("succession")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}

					}

					if (cd.getName().equals("precedence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Precedence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);
						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}

					}

					if (cd.getName().equals("responded existence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Response, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Response)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Response)) {
							selectedTemplates.add(DeclareTemplate.Response);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.CoExistence)) {
							selectedTemplates.add(DeclareTemplate.CoExistence);
						}

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Precedence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Precedence)) {
							selectedTemplates.add(DeclareTemplate.Precedence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.CoExistence, params);

					}

					if (cd.getName().equals("co-existence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

					}

					if (cd.getName().equals("existence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);

						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);

						if (!selectedTemplates.contains(DeclareTemplate.Existence2)) {
							selectedTemplates.add(DeclareTemplate.Existence2);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}

					}

					if (cd.getName().equals("existence2")) {

						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);

						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}
					}

					if (cd.getName().equals("absence2")) {
						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}

					}

					if (cd.getName().equals("absence3")) {
						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence2)) {
							selectedTemplates.add(DeclareTemplate.Absence2);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}
					}

					if (cd.getName().equals("choice")) {
						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);
						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence)) {
							selectedTemplates.add(DeclareTemplate.Existence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence2)) {
							selectedTemplates.add(DeclareTemplate.Existence2);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}

						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

					}

					if (cd.getName().equals("not chain succession")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_Succession)) {
							selectedTemplates.add(DeclareTemplate.Not_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_CoExistence)) {
							selectedTemplates.add(DeclareTemplate.Not_CoExistence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

					if (cd.getName().equals("not succession")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_CoExistence)) {
							selectedTemplates.add(DeclareTemplate.Not_CoExistence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

					if (cd.getName().equals("not co-existence")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

				}
			}

			Pruner pruner = new Pruner(context, log, input, pw);
			declareMinerOutput = pruner.fastPrune(false, false, log, input, declareTemplateCandidateDispositionsMap);
			int constraintsNo = 0;
			for (ConstraintDefinition cd : declareMinerOutput.getModel().getModel().getConstraintDefinitions()) {
				// if(cd.isVisible()){
				constraintsNo++;
				// }
			}

			int activitiesNo = 0;
			for (ActivityDefinition ad : declareMinerOutput.getModel().getModel().getActivityDefinitions()) {
				// if(ad.isVisible()){
				activitiesNo++;
				// }
			}
			System.out.println("number of discovered constraints: " + constraintsNo);
			System.out.println("number of activities: " + activitiesNo);
			if (printVerbose) {
				pw.println("total time: " + overall.msecs() + " msecs");
				pw.println("number of discovered constraints: "
						+ declareMinerOutput.getModel().getModel().constraintDefinitionsCount());
				pw.println(
						"number of activities: " + declareMinerOutput.getModel().getModel().activityDefinitionsCount());
				pw.flush();
				pw.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		declareMinerOutput.setHier(false);
		declareMinerOutput.setTrans(false);
		return declareMinerOutput;
	}

	public static DeclareMinerOutput mineDeclareConstraints(UIPluginContext context, XLog log, DeclareMinerInput input,
			DeclareMinerOutput inputObject) {
		DeclareMinerOutput declareMinerOutput = null;
		DeclareMap inputModel = inputObject.getModel();
		printInputConfiguration(input);

		Set<Set<String>> conceptGroupingSet = new HashSet<Set<String>>();
		// // String parentDir = "C:\\Users\\fmaggi\\Desktop";
		String parentDir = input.getAprioriKnowledgeConceptFileName();
		if (parentDir != null) {
			File[] conceptGroupFileNames = new File(parentDir).listFiles();

			// String group1FileName = "G1.txt";
			// String group2FileName = "G2.txt";
			// String group3FileName = "G3.txt";
			//
			FileIO io = new FileIO();
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group1FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group2FileName));
			// System.out.println("G3: "+io.readFileAsSet(parentDir, group3FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group3FileName));

			for (File conceptGroupFile : conceptGroupFileNames) {
				conceptGroupingSet.add(io.readFileAsSet(parentDir, conceptGroupFile.getName()));
			}
			System.out.println("COncept Groups: " + conceptGroupingSet);

		}

		if (context != null) {
			context.getProgress().setValue(5);
		}
		float support = input.getMinSupport();
		float alpha = input.getAlpha() / 100.0f;

		FindItemSets finder = new FindItemSets(log, input);

		boolean detectActivitiesAutomatically = false; // input.getMapTemplateConfiguration();
		boolean detectTemplatesAutomatically = false; // input.isDetectTemplatesAutomatically();
		boolean detectSupportAutomatically = false; // input.isDetectSupportAutomatically();
		boolean strengthen = false;
		if (input.getMapTemplateConfiguration()
				.equals(MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossAllActivitesInLog)) {
			detectTemplatesAutomatically = true;
		}
		if (input.getMapTemplateConfiguration()
				.equals(MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossActivitiesOnlyInTheMap)) {
			detectTemplatesAutomatically = true;
			detectActivitiesAutomatically = true;
		}

		if (input.getMapTemplateConfiguration().equals(MapTemplateConfiguration.StrengthenMap)) {
			strengthen = true;
			detectActivitiesAutomatically = true;
		}

		if (detectSupportAutomatically) {
			support = learnSupport(input, inputModel.getModel(), log, alpha, finder);
		}

		// change traditional = true to start the traditional algorithm
		boolean traditional = false;
		boolean printVerbose = false;

		// String output = "C:\\Users\\fmaggi\\Desktop\\Exp\\";
		// if(traditional){
		// output = output+"alpha"+alpha+"Traditional.txt";
		// }else{
		// output = output+"alpha"+alpha+"Apriori"+support+".txt";
		// }

		String output = input.getOutputDir() + System.getProperty("file.separator") + input.getOutputFileName();

		Watch overall = new Watch();
		if (printVerbose) {
			overall.start();
		}

		Watch aprioriWatch = new Watch();
		Watch aprioriLocalWatch = new Watch();

		try {
			PrintWriter pw = null;
			if (printVerbose) {
				pw = new PrintWriter(new FileWriter(new File(output)));
				if (traditional) {
					pw.println("trad");
				} else {
					pw.println("minimum support for apriori algorithm: " + (support) / 100.0);
				}
				pw.println("alpha value: " + alpha);
				pw.println("  ");
				pw.flush();
				aprioriWatch.start();
				aprioriLocalWatch.start();
			}

			DeclareModelGenerator dmg = new DeclareModelGenerator();
			List<String> activityNameList = new ArrayList<String>();
			if (!detectActivitiesAutomatically) {
				for (XTrace trace : log) {
					for (XEvent event : trace) {
						String label = (XConceptExtension.instance().extractName(event));
						if (input.getAprioriKnowledgeBasedCriteriaSet()
								.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
							if (event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION) != null) {
								label = (XConceptExtension.instance().extractName(event)) + "-"
										+ XLifecycleExtension.instance().extractTransition(event);
							} else {
								label = (XConceptExtension.instance().extractName(event)) + "-"
										+ input.getReferenceEventType();
							}
						}
						if (!activityNameList.contains(label)) {
							activityNameList.add(label);
						}
					}
				}
			} else {
				for (ActivityDefinition activity : inputModel.getModel().getActivityDefinitions()) {
					String activityName = activity.getName();
					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (!dmg.hasEventTypeInName(activityName)) {
							activityName = activityName + "-" + input.getReferenceEventType();
						}
					}
					activityNameList.add(activityName);
				}
			}

			String[] activityNamesArray = new String[activityNameList.size()];
			for (int i = 0; i < activityNameList.size(); i++) {
				activityNamesArray[i] = activityNameList.get(i);
			}

			Map<FrequentItemSetType, Map<Set<String>, Float>> frequentItemSetTypeFrequentItemSetSupportMap = new HashMap<FrequentItemSetType, Map<Set<String>, Float>>();
			// Map<DeclareTemplate, String[][]> declareTemplateCandidateDispositionsMap =
			// new HashMap<DeclareTemplate, String[][]>();
			Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap = new HashMap<DeclareTemplate, List<List<String>>>();

			Hashtable<String, Object> aprioriSupportValues = new Hashtable<String, Object>();

			String[][] candidateListArray = null;
			List<List<String>> candidateList;
			if (printVerbose) {
				pw.println("precomputation time: " + aprioriLocalWatch.msecs() + " msecs");
				pw.println("   ");
				pw.println("START APRIORI");
			}
			if (context != null) {
				context.getProgress().setMinimum(0);
				context.getProgress().setMaximum(112);
				context.getProgress().setIndeterminate(false);
				context.getProgress().setValue(1);
			}

			Set<DeclareTemplate> selectedTemplates = new HashSet<DeclareTemplate>();
			if (!detectTemplatesAutomatically) {
				selectedTemplates = input.getSelectedDeclareTemplateSet();
			} else {
				for (ConstraintDefinition constraint : inputModel.getModel().getConstraintDefinitions()) {
					if (!selectedTemplates.contains(getTemplate(constraint))) {
						selectedTemplates.add(getTemplate(constraint));
					}
				}
				input.setSelectedDeclareTemplateSet(selectedTemplates);
			}
			if (!traditional) {
				for (DeclareTemplate template : selectedTemplates) {
					if (alpha == 0 || template.equals(DeclareTemplate.Choice)
							|| template.equals(DeclareTemplate.Exclusive_Choice)) {

						if (template.equals(DeclareTemplate.Alternate_Precedence)
								|| template.equals(DeclareTemplate.Alternate_Response) ||
								template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.CoExistence)
								|| template.equals(DeclareTemplate.Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, null);
						}
						if (template.equals(DeclareTemplate.Exclusive_Choice)
								|| template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Succession)
								|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Choice)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, null);
						}
					} else {
						if (template.equals(DeclareTemplate.Precedence)
								|| template.equals(DeclareTemplate.Alternate_Precedence) ||
								template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Alternate_Response)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, null);
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, null);
						}
						if (template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.Succession)
								|| template.equals(DeclareTemplate.CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Chain_Succession)
								|| template.equals(DeclareTemplate.Not_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, null);
						}
					}

					if (isOneHalfPositiveFrequentItemType(template)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, null);
					}

					if (template.equals(DeclareTemplate.Absence)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, null);
					}

					if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
						Map<Set<String>, Float> frequentItemSetSupportMap = DeclareModelGenerator
								.getFrequentItemSetSupportMap(1, log, 0, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_All,
								frequentItemSetSupportMap);
					}

				}

				boolean previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Four_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
							support / 4.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Three_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 3.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.Four_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 3.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							String element2 = iterator.next();
							if (!element1.contains("NOT-") && !element2.contains("NOT-")) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Positive)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Positive);
							for (Set<String> key : previousMap.keySet()) {
								if (previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						} else {
							if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Half_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Half_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Three_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Three_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Four_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Four_Negative);
							}
							for (Set<String> key : previousMap.keySet()) {
								Iterator<String> iterator = key.iterator();
								String element1 = iterator.next();
								String element2 = iterator.next();
								if (!element1.contains("NOT-") && !element2.contains("NOT-")
										&& previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					}
					previous = true;
				}

				previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
							support / 2.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					}
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);

					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);
					}
					previous = true;
				}
			}
			if (!strengthen) {
				int numberOfParameters;
				for (DeclareTemplate template : selectedTemplates) {
					if (isBinaryTemplate(template)) {
						numberOfParameters = 2;
					} else {
						numberOfParameters = 1;
					}

					if (traditional) {
						if (printVerbose) {
							aprioriLocalWatch.start();
						}
						candidateListArray = DispositionsGenerator.generateDisp(activityNamesArray, numberOfParameters);
						int noCandidates = candidateListArray.length;
						candidateList = new ArrayList<List<String>>();
						for (int i = 0; i < noCandidates; i++) {
							List<String> candidate = new ArrayList<String>();
							for (int j = 0; j < candidateListArray[i].length; j++)
								candidate.add(candidateListArray[i][j]);
							candidateList.add(candidate);
						}
						// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
						declareTemplateCandidateDispositionsMap.put(template, candidateList);
						if (printVerbose) {
							pw.println("time needed to generate all the dispositions (traditional approach) of size "
									+ numberOfParameters + ": " + aprioriLocalWatch.msecs() + " msecs");
							pw.println("number of dispositions (traditional approach) of size " + numberOfParameters
									+ ": " + candidateList.size());
						}
					} else {
						if (alpha == 0 || template.equals(DeclareTemplate.Choice)
								|| template.equals(DeclareTemplate.Exclusive_Choice)) {
							if (isPositiveFrequentItemType(template)) {

								if (printVerbose) {
									aprioriLocalWatch.start();
								}

								Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Positive);
								int noCandidates = 0;
								candidateListArray = new String[frequentItemSetSupportMap.size()
										* Factorial.fatt(numberOfParameters)][numberOfParameters];
								candidateList = new ArrayList<List<String>>();
								for (Set<String> frequentItemSet : frequentItemSetSupportMap.keySet()) {
									int itemIndex = 0;
									String[] frequentItemSetArray = new String[frequentItemSet.size()];
									for (String freqItem : frequentItemSet)
										frequentItemSetArray[itemIndex++] = freqItem;
									String[][] dispositions = null;
									dispositions = DispositionsGenerator.generateDisp(frequentItemSetArray,
											numberOfParameters);

									for (int i = 0; i < dispositions.length; i++) {
										List<String> candidate = new ArrayList<String>();
										for (int j = 0; j < numberOfParameters; j++) {
											candidateListArray[noCandidates][j] = dispositions[i][j];
											candidate.add(dispositions[i][j]);
										}
										candidateList.add(candidate);
										noCandidates++;
									}
								}

								// print candidates
								// System.out.println("No. candidates: "+noCandidates+" @ CanddiateListSize:
								// "+candidatedList.length);
								// for(int i = 0; i < candidatedList.length; i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidatedList[i][j]+" ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								//

								if ((printVerbose) && alpha == 0) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support
													+ " [positive relation; simple choice]" + ": "
													+ aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support
											+ " [positive relation templates; simple choice]" + ": "
											+ candidateList.size());
								} else if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support
													+ " [simple choice]" + ": " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support + " [simple choice]"
											+ ": " + candidateList.size());
								}
							}

							if (template.equals(DeclareTemplate.Choice)) {
								/*
								 * The basic idea here is to explore all
								 * combinations of size 2 that satisfy the sum of
								 * support of both the individual activities is greater than
								 * minSupp
								 */
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								System.out.println("HERE: " + declareTemplateCandidateDispositionsMap.keySet());
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									int[] indices;
									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// System.out.println("Dispositions");
									// for(int j = 0; j < noDispositions; j++){
									// for(int k = 0; k < 2; k++)
									// System.out.print(dispositions[j][k]+" ");
									// System.out.println();
									// }

									candidateList = new ArrayList<List<String>>();
									int noCandidates = 0;
									// boolean[] isCandidateDisposition = new boolean[noDispositions];
									float suppActivity1, suppActivity2;
									for (int i = 0; i < noDispositions; i++) {
										// isCandidateDisposition[i] = false;
										suppActivity1 = finder.getSupport(dispositions[i][0].replaceAll("NOT-", ""));
										suppActivity2 = finder.getSupport(dispositions[i][1].replaceAll("NOT-", ""));
										if (suppActivity1 + suppActivity2 >= support) {
											List<String> candidate = new ArrayList<String>();
											candidate.add(dispositions[i][0]);
											candidate.add(dispositions[i][1]);
											candidateList.add(candidate);

											// isCandidateDisposition[i] = true;
											noCandidates++;
											// System.out.println("C: " + dispositions[i][0] + " , " +
											// dispositions[i][1]);
										}
									}

									// candidateListArray = new String[noCandidates][numberOfParameters];
									// int candidateIndex = 0;
									// for(int i = 0; i < noDispositions; i++){
									// if(isCandidateDisposition[i]){
									// candidateListArray[candidateIndex][0] = dispositions[i][0];
									// candidateListArray[candidateIndex++][1] = dispositions[i][1];
									// }
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// print candidates

									if ((printVerbose) && alpha == 0) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [simple choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support / 2.f
												+ " [simple choice]" + ": " + candidateList.size());
									} else if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support / 2.f
												+ " [simple choice]" + ": " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Exclusive_Choice)
									|| template.equals(DeclareTemplate.Not_CoExistence)) {
								/*
								 * The basic idea is to explore all combinations of
								 * the type (A, NOT-B) and (NOT-A, B) that have a
								 * support greater than minSupport
								 */
								Set<Set<String>> candidateDispositionSet = new HashSet<Set<String>>();
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
											.get(FrequentItemSetType.Half_Negative);
									System.out.println("Freq Item Set: ");
									for (Set<String> freqItem : frequentItemSetSupportMap.keySet())
										System.out.println(freqItem + " @ " + frequentItemSetSupportMap.get(freqItem));

									System.out.println("Exploring Candidates: " + numberOfParameters);
									int noCandidates = 0;
									for (Set<String> freqItemSet : frequentItemSetSupportMap.keySet()) {
										if (freqItemSet.size() != numberOfParameters)
											continue;
										// check if both activities are positive or both negative; ignore such
										// combinations
										boolean isAllPositive = true;
										boolean isAllNegative = true;
										for (String item : freqItemSet) {
											if (item.contains("NOT-")) {
												isAllPositive = false;
											} else {
												isAllNegative = false;
											}
										}
										/*
										 * it could be that the support of (A,
										 * NOT-B) is less than minSupp and the
										 * support of (B, NOT-A) is less than
										 * minSupp, but the sum of their supports is
										 * greater than minSupport. check for those
										 */
										float complementaryFreqItemSupport;
										Set<String> complementaryFreqItemSet = new HashSet<String>();
										for (String freqItem : freqItemSet) {
											if (freqItem.contains("NOT-")) {
												complementaryFreqItemSet.add(freqItem.replace("NOT-", ""));
											} else {
												complementaryFreqItemSet.add("NOT-" + freqItem);
											}
										}
										Iterator<String> it = complementaryFreqItemSet.iterator();
										complementaryFreqItemSupport = finder.getSupport(it.next(), it.next());

										if (!isAllNegative && !isAllPositive
												&& (frequentItemSetSupportMap.get(freqItemSet)
														+ complementaryFreqItemSupport) >= support) {
											Set<String> candidateSet = new HashSet<String>();
											for (String item : freqItemSet)
												candidateSet.add(item.replace("NOT-", ""));
											if (candidateSet.size() != numberOfParameters) {
												System.out.println("Strange for this to be here: " + freqItemSet + " @ "
														+ frequentItemSetSupportMap.get(freqItemSet) + " @ " + support
														+ " @ " + candidateSet);
												continue;
											}
											candidateDispositionSet.add(candidateSet);
										}
									}

									noCandidates = candidateDispositionSet.size();
									System.out.println("No. candidates: " + candidateDispositionSet.size());
									for (Set<String> candidateDisposition : candidateDispositionSet)
										System.out.println(candidateDisposition);
									System.out.println("----------");

									candidateList = new ArrayList<List<String>>();
									candidateListArray = new String[noCandidates][numberOfParameters];
									for (Set<String> candidateDisposition : candidateDispositionSet) {
										List<String> candidate = new ArrayList<String>();
										Iterator<String> candidateDispositionIterator = candidateDisposition.iterator();
										while (candidateDispositionIterator.hasNext()) {
											candidate.add(candidateDispositionIterator.next());
											// candidateListArray[candidateIndex][paramIndex++] =
											// candidateDispositionIterator.next();
										}
										candidateList.add(candidate);
									}

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(List<String> candidate: candidateList){
									// System.out.println(candidate);
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence,
									// candidatedList);
									if ((printVerbose) && alpha == 0) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [not co-existence; exclusive choice]" + ": "
														+ aprioriLocalWatch.msecs() + " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [not co-existence; exclusive choice]" + ": "
														+ candidateList.size());
									} else if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Not_Chain_Succession)
									|| template.equals(DeclareTemplate.Not_Succession)) {
								/*
								 * We should consider dispositions of the form (A,
								 * B), (A, NOT-B) and (NOT-A, B) provided the sum of
								 * their support is > minSupport
								 */
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									if (printVerbose) {
										// pw.println("start generation (positive/negative) frequent sets of size
										// "+noparam+" with support "+supp/3.f+" [not succession]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppFirstPositive, suppFirstNegative;
									for (int i = 0; i < noDispositions; i++) {
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);// supp(A,B)
										suppFirstPositive = finder.getSupport(dispositions[i][0],
												"NOT-" + dispositions[i][1]);// supp (A,NOT-B);
										suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0],
												dispositions[i][1]);// supp (NOT-A,B);

										if (suppBothPositive + suppFirstPositive + suppFirstNegative >= support) {
											// we should add both (A,B) and (B,A) as candidates
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											candidateList.add(candidateDisposition);
											// candidateDispositionSet.add(candidateDisposition);
											System.out.println(candidateDisposition);

											candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											candidateList.add(candidateDisposition);
											// candidateDispositionSet.add(candidateDisposition);
											System.out.println(candidateDisposition);
										}
									}
									System.out.println("No. Candidates: " + candidateList.size());
									// System.out.println("No. Candidates: "+candidateDispositionSet.size());
									//
									// int candidateIndex = 0;
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									// }

									// System.out.println("Candidate List Size: "+candidateList.length);
									// for(int i = 0; i < candidateList.length; i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateList[i][j]+", ");
									// System.out.println();
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
									// candidatedList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
									// candidatedList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 3.f
														+ " [not succession]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 3.f
														+ " [not succession]" + ": " + candidateList.size());
									}
								}
							}

						} else {
							System.out.println("alpha " + alpha);
							if (template.equals(DeclareTemplate.Precedence)
									|| template.equals(DeclareTemplate.Alternate_Precedence) ||
									template.equals(DeclareTemplate.Chain_Precedence) ||
									template.equals(DeclareTemplate.Responded_Existence)
									|| template.equals(DeclareTemplate.Response) ||
									template.equals(DeclareTemplate.Chain_Response)
									|| template.equals(DeclareTemplate.Alternate_Response)) {
								if (((template.equals(DeclareTemplate.Responded_Existence)
										|| template.equals(DeclareTemplate.Response) ||
										template.equals(DeclareTemplate.Chain_Response)
										|| template.equals(DeclareTemplate.Alternate_Response))
										&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("respon"))
										|| ((template.equals(DeclareTemplate.Precedence)
												|| template.equals(DeclareTemplate.Alternate_Precedence) ||
												template.equals(DeclareTemplate.Chain_Precedence))
												&& !frequentItemSetTypeFrequentItemSetSupportMap
														.containsKey("precedence"))) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									if (printVerbose) {
										// pw.println("start generation for (positive) frequent sets of size "+noparam+"
										// and (positive/negative) frequent sets of size 1 with support "+supp/2.f+"
										// [responded existence; (simple, alternate, chain) response; (simple,
										// alternate, chain) precedence]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// print the supp values
									for (String activity : activityNameList) {
										System.out.println(activity + " @ " + finder.getSupport(activity));
									}
									for (int i = 0; i < noDispositions; i++) {
										System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
												+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppFirstNegative, suppSecondNegative;
									for (int i = 0; i < noDispositions; i++) {
										// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A) >
										// minSupp
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
										suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0]);
										suppSecondNegative = finder.getSupport("NOT-" + dispositions[i][1]);
										boolean bothDirection = true;
										if (suppBothPositive + suppFirstNegative >= support) {
											// add (A,B)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
											// System.out.println(candidateDisposition);
											bothDirection = false;
										}

										if (suppBothPositive + suppSecondNegative >= support) {
											// add (B,A)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
											// System.out.println(candidateDisposition);
											if (!bothDirection)
												bothDirection = true;
											else
												bothDirection = false;
										}

										// if(!bothDirection){
										// System.out.println("this dispositon contains only one direction");
										// }

									}

									System.out.println("No. Candidates: " + candidateList.size());
									for (List<String> candidate : candidateList)
										System.out.println(candidate);
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// int candidateIndex = 0;
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									//// System.out.println(candidateDisposition);
									// }

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters
														+ " and (positive/negative) frequent sets of size 1 with support "
														+ support / 2.f
														+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
														+ aprioriLocalWatch.msecs() + " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters
												+ " and (positive/negative) frequent sets of size 1 with support "
												+ support / 2.f
												+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
												+ candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Alternate_Succession)
									|| template.equals(DeclareTemplate.Chain_Succession) ||
									template.equals(DeclareTemplate.Succession)
									|| template.equals(DeclareTemplate.CoExistence)) {
								if ((template.equals(DeclareTemplate.CoExistence)
										&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("coexistence"))
										|| ((template.equals(DeclareTemplate.Alternate_Succession)
												|| template.equals(DeclareTemplate.Chain_Succession) ||
												template.equals(DeclareTemplate.Succession)
														&& !frequentItemSetTypeFrequentItemSetSupportMap
																.containsKey("succession")))) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}
									if (printVerbose) {
										// pw.println("start generation (positive/negative) frequent sets of size
										// "+noparam+" with support "+supp/2.f+" [succession; co-existence]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// print the supp values
									for (String activity : activityNameList) {
										System.out.println(activity + " @ " + finder.getSupport(activity));
									}
									for (int i = 0; i < noDispositions; i++) {
										System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
												+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppBothNegative;
									for (int i = 0; i < noDispositions; i++) {
										// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A, NOT-B)
										// > minSupp
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
										suppBothNegative = finder.getSupport("NOT-" + dispositions[i][0],
												"NOT-" + dispositions[i][1]);

										if (suppBothPositive + suppBothNegative >= support) {
											// add (A,B) and (B,A) both (but for co-existence only one)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);

											System.out.println(candidateDisposition);

											if (!template.equals(DeclareTemplate.CoExistence)) {
												candidateDisposition = new ArrayList<String>();
												candidateDisposition.add(dispositions[i][1]);
												candidateDisposition.add(dispositions[i][0]);
												// candidateDispositionSet.add(candidateDisposition);
												candidateList.add(candidateDisposition);
											}
										}

										// if(!bothDirection){
										// System.out.println("this dispositon contains only one direction");
										// }

									}

									System.out.println("No. Candidates: " + candidateList.size());
									// System.out.println("No. Candidates: "+candidateDispositionSet.size());
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// int candidateIndex = 0;
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									// System.out.println(candidateDisposition);
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }

									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [succession; co-existence]: " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [succession; co-existence]: " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Not_CoExistence)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" with support "+supp/3.f+" [not co-existence]");
								}

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;
									// int noDispositions = 0;
									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}
								candidateList = new ArrayList<List<String>>();
								// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
								float suppBothPositive;
								for (int i = 0; i < noDispositions; i++) {
									// for every combination (A,B), if 1-supp(A,B) >= minSupport, then we need to
									// explore
									suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
									if (100 - suppBothPositive >= support) {
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][0]);
										candidateDisposition.add(dispositions[i][1]);
										// candidateDispositionSet.add(candidateDisposition);
										candidateList.add(candidateDisposition);
									}
								}
								System.out.println("No. Candidates: " + candidateList.size());
								// System.out.println("No. Candidates: "+candidateDispositionSet.size());
								// candidateListArray = new
								// String[candidateDispositionSet.size()][numberOfParameters];
								// int candidateIndex = 0;
								// for(List<String> candidateDisposition : candidateDispositionSet){
								// int paramIndex = 0;
								// for(String candidateItem : candidateDisposition)
								// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
								// candidateIndex++;
								//// System.out.println(candidateDisposition);
								// }

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(int i = 0; i < candidateList.size(); i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateListArray[i][j]+", ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for f(positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 3.f
													+ " [not co-existence]: " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 3.f
											+ " [not co-existence]: " + candidateList.size());
								}
							}

							if (template.equals(DeclareTemplate.Not_Succession)
									|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									/*
									 * need to add both (A,B) and (B,A) irrespective of support values
									 */

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									candidateList = new ArrayList<List<String>>();
									System.out.println("No. Candidates: " + dispositions.length * 2);
									candidateListArray = new String[dispositions.length * 2][numberOfParameters];
									for (int i = 0; i < noDispositions; i++) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(dispositions[i][0]);
										candidate.add(dispositions[i][1]);
										candidateList.add(candidate);

										candidate = new ArrayList<String>();
										candidate.add(dispositions[i][1]);
										candidate.add(dispositions[i][0]);
										candidateList.add(candidate);

										// candidateListArray[candidateIndex][0] = dispositions[i][0];
										// candidateListArray[candidateIndex][1] = dispositions[i][1];
										// candidateIndex++;
										// candidateListArray[candidateIndex][0] = dispositions[i][1];
										// candidateListArray[candidateIndex][1] = dispositions[i][0];
										// candidateIndex++;
									}

									System.out.println("Candidate List Size: " + candidateList.size());

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }
									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
									// candidatedList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
									// candidatedList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 4.f
														+ " [not succession]: " + aprioriLocalWatch.msecs() + " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 4.f
														+ " [not succession]: " + candidateList.size());
									}
								}
							}
						}

						if (template.equals(DeclareTemplate.Exactly1) || template.equals(DeclareTemplate.Exactly2) ||
								template.equals(DeclareTemplate.Existence)
								|| template.equals(DeclareTemplate.Existence2) ||
								template.equals(DeclareTemplate.Existence3) || template.equals(DeclareTemplate.Init)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive) frequent sets of size "+noparam+"
									// [unary templates]");
								}

								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									if (finder.getSupport(activity) >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(activity);
										candidateList.add(candidate);
									}
								}
								System.out.println("Candidate List Size: " + candidateList.size());
								for (List<String> candidate : candidateList)
									System.out.println(candidate);

								declareTemplateCandidateDispositionsMap.put(template, candidateList);

								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ noparam + " [unary templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size " + noparam
											+ " [unary templates]: " + candidateList.size());
								}

							}
						}

						if (template.equals(DeclareTemplate.Absence)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" [absence templates]");
								}

								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									if (finder.getSupport("NOT-" + activity) >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(activity);
										candidateList.add(candidate);
									}
								}

								System.out.println("Candidate List Size: " + candidateList.size());

								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ noparam + " [absence templates]: " + candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" [absence templates]");
								}
								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									List<String> candidate = new ArrayList<String>();
									candidate.add(activity);
									candidateList.add(candidate);
								}

								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ noparam + " [absence templates]: " + candidateList.size());
								}
							}
						}
					}
				}
				if (printVerbose) {
					pw.println("END APRIORI - time: " + aprioriWatch.msecs() + " msecs");
					pw.println("  ");
				}

				int value = 0;

				System.out.println("JC: " + declareTemplateCandidateDispositionsMap.keySet());
				for (DeclareTemplate template : selectedTemplates) {

					candidateList = declareTemplateCandidateDispositionsMap.get(template);
					if (candidateList != null) {
						value = value + candidateList.size();
					}
				}
				if (context != null)
					context.getProgress().setMaximum(value);
			} else {
				for (ConstraintDefinition cd : inputModel.getModel().getConstraintDefinitions()) {

					if (!selectedTemplates.contains(getTemplate(cd))) {
						selectedTemplates.add(getTemplate(cd));
					}

					Iterator<Parameter> iter = cd.getParameters().iterator();
					Parameter p1 = iter.next();
					Parameter p2 = null;
					if (iter.hasNext()) {
						p2 = iter.next();
					}
					String key = "EMPTY_PARAM";
					ArrayList<String> param = new ArrayList<String>();
					if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p1).iterator().next() != null) {
						key = cd.getBranches(p1).iterator().next().getName();

					}

					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (!dmg.hasEventTypeInName(key)) {
							key = key + "-" + input.getReferenceEventType();
						}
					}
					param.add(key);
					if (p2 != null) {
						if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p2).iterator().next() != null) {
							key = cd.getBranches(p2).iterator().next().getName();
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
								if (!dmg.hasEventTypeInName(key)) {
									key = key + "-" + input.getReferenceEventType();
								}
							}
							param.add(key);
						} else {
							key = "EMPTY_PARAM";
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
								if (!dmg.hasEventTypeInName(key)) {
									key = key + "-" + input.getReferenceEventType();
								}
							}
							param.add(key);
						}
					}

					ArrayList<String> invPar = new ArrayList<String>();
					invPar.add(param.get(1));
					invPar.add(param.get(0));

					List<List<String>> currparams = null;
					if (declareTemplateCandidateDispositionsMap.containsKey(getTemplate(cd))) {
						currparams = declareTemplateCandidateDispositionsMap.get(getTemplate(cd));
					} else {
						currparams = new ArrayList<List<String>>();
					}
					if (getTemplate(cd).equals(DeclareTemplate.Precedence)
							|| getTemplate(cd).equals(DeclareTemplate.Alternate_Precedence) ||
							getTemplate(cd).equals(DeclareTemplate.Chain_Precedence)) {
						currparams.add(invPar);
					} else {
						currparams.add(param);
					}
					declareTemplateCandidateDispositionsMap.put(getTemplate(cd), currparams);

					if (cd.getName().equals("chain response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

					}

					if (cd.getName().equals("chain precedence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
					}

					if (cd.getName().equals("alternate response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
					}

					if (cd.getName().equals("alternate precedence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
					}

					if (cd.getName().equals("alternate succession")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
					}

					if (cd.getName().equals("response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Response)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Response);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

					}

					if (cd.getName().equals("succession")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}

					}

					if (cd.getName().equals("precedence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Precedence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(invPar.get(0));
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);
						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}

					}

					if (cd.getName().equals("responded existence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Response, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Response)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Response)) {
							selectedTemplates.add(DeclareTemplate.Response);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.CoExistence)) {
							selectedTemplates.add(DeclareTemplate.CoExistence);
						}

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Precedence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Precedence)) {
							selectedTemplates.add(DeclareTemplate.Precedence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.CoExistence, params);

					}

					if (cd.getName().equals("co-existence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

					}

					if (cd.getName().equals("existence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);

						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);

						if (!selectedTemplates.contains(DeclareTemplate.Existence2)) {
							selectedTemplates.add(DeclareTemplate.Existence2);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}

					}

					if (cd.getName().equals("existence2")) {

						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);

						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}
					}

					if (cd.getName().equals("absence2")) {
						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}

					}

					if (cd.getName().equals("absence3")) {
						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence2)) {
							selectedTemplates.add(DeclareTemplate.Absence2);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}
					}

					if (cd.getName().equals("choice")) {
						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);
						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence)) {
							selectedTemplates.add(DeclareTemplate.Existence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence2)) {
							selectedTemplates.add(DeclareTemplate.Existence2);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}

						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

					}

					if (cd.getName().equals("not chain succession")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_Succession)) {
							selectedTemplates.add(DeclareTemplate.Not_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_CoExistence)) {
							selectedTemplates.add(DeclareTemplate.Not_CoExistence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

					if (cd.getName().equals("not succession")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_CoExistence)) {
							selectedTemplates.add(DeclareTemplate.Not_CoExistence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

					if (cd.getName().equals("not co-existence")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

				}
			}
			Pruner pruner = new Pruner(context, log, input, pw);
			declareMinerOutput = pruner.fastPrune(false, false, log, input, declareTemplateCandidateDispositionsMap);
			int constraintsNo = 0;
			for (ConstraintDefinition cd : declareMinerOutput.getModel().getModel().getConstraintDefinitions()) {
				// if(cd.isVisible()){
				constraintsNo++;
				// }
			}

			int activitiesNo = 0;
			for (ActivityDefinition ad : declareMinerOutput.getModel().getModel().getActivityDefinitions()) {
				// if(ad.isVisible()){
				activitiesNo++;
				// }
			}
			System.out.println("number of discovered constraints: " + constraintsNo);
			System.out.println("number of activities: " + activitiesNo);
			if (printVerbose) {
				pw.println("total time: " + overall.msecs() + " msecs");
				pw.println("number of discovered constraints: "
						+ declareMinerOutput.getModel().getModel().constraintDefinitionsCount());
				pw.println(
						"number of activities: " + declareMinerOutput.getModel().getModel().activityDefinitionsCount());
				pw.flush();
				pw.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		declareMinerOutput.setHier(false);
		declareMinerOutput.setTrans(false);
		return declareMinerOutput;
	}

	public static DeclareMinerOutput mineDeclareConstraints(UIPluginContext context, XLog log, DeclareMinerInput input,
			DeclareExtensionOutput inputObject) {
		DeclareMinerOutput declareMinerOutput = null;
		DeclareMap inputModel = inputObject.getModel();
		printInputConfiguration(input);

		Set<Set<String>> conceptGroupingSet = new HashSet<Set<String>>();
		// String parentDir = "C:\\Users\\fmaggi\\Desktop";
		String parentDir = input.getAprioriKnowledgeConceptFileName();
		if (parentDir != null) {
			File[] conceptGroupFileNames = new File(parentDir).listFiles();

			// String group1FileName = "G1.txt";
			// String group2FileName = "G2.txt";
			// String group3FileName = "G3.txt";
			//
			FileIO io = new FileIO();
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group1FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group2FileName));
			// System.out.println("G3: "+io.readFileAsSet(parentDir, group3FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group3FileName));

			for (File conceptGroupFile : conceptGroupFileNames) {
				conceptGroupingSet.add(io.readFileAsSet(parentDir, conceptGroupFile.getName()));
			}
			System.out.println("COncept Groups: " + conceptGroupingSet);

		}
		if (context != null) {
			context.getProgress().setValue(5);
		}
		float support = input.getMinSupport();
		float alpha = input.getAlpha() / 100.0f;

		FindItemSets finder = new FindItemSets(log, input);

		boolean detectActivitiesAutomatically = false; // input.getMapTemplateConfiguration();
		boolean detectTemplatesAutomatically = false; // input.isDetectTemplatesAutomatically();
		boolean detectSupportAutomatically = false; // input.isDetectSupportAutomatically();
		boolean strengthen = false; // input.isDetectSupportAutomatically();

		if (input.getMapTemplateConfiguration()
				.equals(MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossAllActivitesInLog)) {
			detectTemplatesAutomatically = true;
		}
		if (input.getMapTemplateConfiguration()
				.equals(MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossActivitiesOnlyInTheMap)) {
			detectTemplatesAutomatically = true;
			detectActivitiesAutomatically = true;
		}

		if (input.getMapTemplateConfiguration().equals(MapTemplateConfiguration.StrengthenMap)) {
			// detectTemplatesAutomatically = true;
			detectActivitiesAutomatically = true;
			strengthen = true;
		}

		if (detectSupportAutomatically) {
			support = learnSupport(input, inputModel.getModel(), log, alpha, finder);
		}

		// change traditional = true to start the traditional algorithm
		boolean traditional = false;
		boolean printVerbose = false;

		// String output = "C:\\Users\\fmaggi\\Desktop\\Exp\\";
		// if(traditional){
		// output = output+"alpha"+alpha+"Traditional.txt";
		// }else{
		// output = output+"alpha"+alpha+"Apriori"+support+".txt";
		// }

		String output = input.getOutputDir() + System.getProperty("file.separator") + input.getOutputFileName();

		Watch overall = new Watch();
		if (printVerbose) {
			overall.start();
		}

		Watch aprioriWatch = new Watch();
		Watch aprioriLocalWatch = new Watch();

		try {
			PrintWriter pw = null;
			if (printVerbose) {
				pw = new PrintWriter(new FileWriter(new File(output)));
				if (traditional) {
					pw.println("trad");
				} else {
					pw.println("minimum support for apriori algorithm: " + (support) / 100.0);
				}
				pw.println("alpha value: " + alpha);
				pw.println("  ");
				pw.flush();
				aprioriWatch.start();
				aprioriLocalWatch.start();
			}

			DeclareModelGenerator dmg = new DeclareModelGenerator();
			List<String> activityNameList = new ArrayList<String>();
			if (!detectActivitiesAutomatically) {
				for (XTrace trace : log) {
					for (XEvent event : trace) {
						String label = (XConceptExtension.instance().extractName(event));
						if (input.getAprioriKnowledgeBasedCriteriaSet()
								.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
							if (event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION) != null) {
								label = (XConceptExtension.instance().extractName(event)) + "-"
										+ XLifecycleExtension.instance().extractTransition(event);
							} else {
								label = (XConceptExtension.instance().extractName(event)) + "-"
										+ input.getReferenceEventType();
							}
						}
						if (!activityNameList.contains(label)) {
							activityNameList.add(label);
						}
					}
				}
			} else {
				for (ActivityDefinition activity : inputModel.getModel().getActivityDefinitions()) {
					String activityName = activity.getName();
					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (!dmg.hasEventTypeInName(activityName)) {
							activityName = activityName + "-" + input.getReferenceEventType();
						}
					}
					activityNameList.add(activityName);
				}
			}

			String[] activityNamesArray = new String[activityNameList.size()];
			for (int i = 0; i < activityNameList.size(); i++) {
				activityNamesArray[i] = activityNameList.get(i);
			}

			Map<FrequentItemSetType, Map<Set<String>, Float>> frequentItemSetTypeFrequentItemSetSupportMap = new HashMap<FrequentItemSetType, Map<Set<String>, Float>>();

			Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap = new HashMap<DeclareTemplate, List<List<String>>>();

			Hashtable<String, Object> aprioriSupportValues = new Hashtable<String, Object>();

			String[][] candidateListArray = null;
			List<List<String>> candidateList;
			if (printVerbose) {
				pw.println("precomputation time: " + aprioriLocalWatch.msecs() + " msecs");
				pw.println("   ");
				pw.println("START APRIORI");
			}
			if (context != null) {
				context.getProgress().setMinimum(0);
				context.getProgress().setMaximum(112);
				context.getProgress().setIndeterminate(false);
				context.getProgress().setValue(1);
			}

			// declareMinerOutput =
			// pruner.prune(false,frequentItemSetTypeFrequentItemSetSupportMap,
			// declareTemplateCandidateDispositionsMap,);

			Set<DeclareTemplate> selectedTemplates = new HashSet<DeclareTemplate>();
			if (!detectTemplatesAutomatically) {
				selectedTemplates = input.getSelectedDeclareTemplateSet();
			} else {
				for (ConstraintDefinition constraint : inputModel.getModel().getConstraintDefinitions()) {
					if (!selectedTemplates.contains(getTemplate(constraint))) {
						selectedTemplates.add(getTemplate(constraint));
					}
				}
				input.setSelectedDeclareTemplateSet(selectedTemplates);
			}
			if (!traditional) {
				for (DeclareTemplate template : selectedTemplates) {
					if (alpha == 0 || template.equals(DeclareTemplate.Choice)
							|| template.equals(DeclareTemplate.Exclusive_Choice)) {

						if (template.equals(DeclareTemplate.Alternate_Precedence)
								|| template.equals(DeclareTemplate.Alternate_Response) ||
								template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.CoExistence)
								|| template.equals(DeclareTemplate.Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, null);
						}
						if (template.equals(DeclareTemplate.Exclusive_Choice)
								|| template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Succession)
								|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Choice)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, null);
						}
					} else {
						if (template.equals(DeclareTemplate.Precedence)
								|| template.equals(DeclareTemplate.Alternate_Precedence) ||
								template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Alternate_Response)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, null);
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, null);
						}
						if (template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.Succession)
								|| template.equals(DeclareTemplate.CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Chain_Succession)
								|| template.equals(DeclareTemplate.Not_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, null);
						}
					}

					if (isOneHalfPositiveFrequentItemType(template)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, null);
					}

					if (template.equals(DeclareTemplate.Absence)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, null);
					}

					if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
						Map<Set<String>, Float> frequentItemSetSupportMap = DeclareModelGenerator
								.getFrequentItemSetSupportMap(1, log, 0, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_All,
								frequentItemSetSupportMap);
					}

				}

				boolean previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Four_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
							support / 4.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Three_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 3.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.Four_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 3.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							String element2 = iterator.next();
							if (!element1.contains("NOT-") && !element2.contains("NOT-")) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Positive)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Positive);
							for (Set<String> key : previousMap.keySet()) {
								if (previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						} else {
							if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Half_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Half_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Three_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Three_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Four_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Four_Negative);
							}
							for (Set<String> key : previousMap.keySet()) {
								Iterator<String> iterator = key.iterator();
								String element1 = iterator.next();
								String element2 = iterator.next();
								if (!element1.contains("NOT-") && !element2.contains("NOT-")
										&& previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					}
					previous = true;
				}

				previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
							support / 2.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					}
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);

					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);
					}
					previous = true;
				}
			}
			if (!strengthen) {
				int numberOfParameters;
				for (DeclareTemplate template : selectedTemplates) {
					if (isBinaryTemplate(template)) {
						numberOfParameters = 2;
					} else {
						numberOfParameters = 1;
					}

					if (traditional) {
						if (printVerbose) {
							aprioriLocalWatch.start();
						}
						candidateListArray = DispositionsGenerator.generateDisp(activityNamesArray, numberOfParameters);
						int noCandidates = candidateListArray.length;
						candidateList = new ArrayList<List<String>>();
						for (int i = 0; i < noCandidates; i++) {
							List<String> candidate = new ArrayList<String>();
							for (int j = 0; j < candidateListArray[i].length; j++)
								candidate.add(candidateListArray[i][j]);
							candidateList.add(candidate);
						}
						// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
						declareTemplateCandidateDispositionsMap.put(template, candidateList);
						if (printVerbose) {
							pw.println("time needed to generate all the dispositions (traditional approach) of size "
									+ numberOfParameters + ": " + aprioriLocalWatch.msecs() + " msecs");
							pw.println("number of dispositions (traditional approach) of size " + numberOfParameters
									+ ": " + candidateList.size());
						}
					} else {
						if (alpha == 0 || template.equals(DeclareTemplate.Choice)
								|| template.equals(DeclareTemplate.Exclusive_Choice)) {
							if (isPositiveFrequentItemType(template)) {

								if (printVerbose) {
									aprioriLocalWatch.start();
								}

								Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Positive);
								int noCandidates = 0;
								candidateListArray = new String[frequentItemSetSupportMap.size()
										* Factorial.fatt(numberOfParameters)][numberOfParameters];
								candidateList = new ArrayList<List<String>>();
								for (Set<String> frequentItemSet : frequentItemSetSupportMap.keySet()) {
									int itemIndex = 0;
									String[] frequentItemSetArray = new String[frequentItemSet.size()];
									for (String freqItem : frequentItemSet)
										frequentItemSetArray[itemIndex++] = freqItem;
									String[][] dispositions = null;
									dispositions = DispositionsGenerator.generateDisp(frequentItemSetArray,
											numberOfParameters);

									for (int i = 0; i < dispositions.length; i++) {
										List<String> candidate = new ArrayList<String>();
										for (int j = 0; j < numberOfParameters; j++) {
											candidateListArray[noCandidates][j] = dispositions[i][j];
											candidate.add(dispositions[i][j]);
										}
										candidateList.add(candidate);
										noCandidates++;
									}
								}

								// print candidates
								// System.out.println("No. candidates: "+noCandidates+" @ CanddiateListSize:
								// "+candidatedList.length);
								// for(int i = 0; i < candidatedList.length; i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidatedList[i][j]+" ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								//

								if ((printVerbose) && alpha == 0) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support
													+ " [positive relation; simple choice]" + ": "
													+ aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support
											+ " [positive relation templates; simple choice]" + ": "
											+ candidateList.size());
								} else if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support
													+ " [simple choice]" + ": " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support + " [simple choice]"
											+ ": " + candidateList.size());
								}
							}

							if (template.equals(DeclareTemplate.Choice)) {
								/*
								 * The basic idea here is to explore all
								 * combinations of size 2 that satisfy the sum of
								 * support of both the individual activities is greater than
								 * minSupp
								 */
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								System.out.println("HERE: " + declareTemplateCandidateDispositionsMap.keySet());
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									int[] indices;
									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// System.out.println("Dispositions");
									// for(int j = 0; j < noDispositions; j++){
									// for(int k = 0; k < 2; k++)
									// System.out.print(dispositions[j][k]+" ");
									// System.out.println();
									// }

									candidateList = new ArrayList<List<String>>();
									int noCandidates = 0;
									// boolean[] isCandidateDisposition = new boolean[noDispositions];
									float suppActivity1, suppActivity2;
									for (int i = 0; i < noDispositions; i++) {
										// isCandidateDisposition[i] = false;
										suppActivity1 = finder.getSupport(dispositions[i][0].replaceAll("NOT-", ""));
										suppActivity2 = finder.getSupport(dispositions[i][1].replaceAll("NOT-", ""));
										if (suppActivity1 + suppActivity2 >= support) {
											List<String> candidate = new ArrayList<String>();
											candidate.add(dispositions[i][0]);
											candidate.add(dispositions[i][1]);
											candidateList.add(candidate);

											// isCandidateDisposition[i] = true;
											noCandidates++;
											// System.out.println("C: " + dispositions[i][0] + " , " +
											// dispositions[i][1]);
										}
									}

									// candidateListArray = new String[noCandidates][numberOfParameters];
									// int candidateIndex = 0;
									// for(int i = 0; i < noDispositions; i++){
									// if(isCandidateDisposition[i]){
									// candidateListArray[candidateIndex][0] = dispositions[i][0];
									// candidateListArray[candidateIndex++][1] = dispositions[i][1];
									// }
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// print candidates

									if ((printVerbose) && alpha == 0) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [simple choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support / 2.f
												+ " [simple choice]" + ": " + candidateList.size());
									} else if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support / 2.f
												+ " [simple choice]" + ": " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Exclusive_Choice)
									|| template.equals(DeclareTemplate.Not_CoExistence)) {
								/*
								 * The basic idea is to explore all combinations of
								 * the type (A, NOT-B) and (NOT-A, B) that have a
								 * support greater than minSupport
								 */
								Set<Set<String>> candidateDispositionSet = new HashSet<Set<String>>();
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
											.get(FrequentItemSetType.Half_Negative);
									System.out.println("Freq Item Set: ");
									for (Set<String> freqItem : frequentItemSetSupportMap.keySet())
										System.out.println(freqItem + " @ " + frequentItemSetSupportMap.get(freqItem));

									System.out.println("Exploring Candidates: " + numberOfParameters);
									int noCandidates = 0;
									for (Set<String> freqItemSet : frequentItemSetSupportMap.keySet()) {
										if (freqItemSet.size() != numberOfParameters)
											continue;
										// check if both activities are positive or both negative; ignore such
										// combinations
										boolean isAllPositive = true;
										boolean isAllNegative = true;
										for (String item : freqItemSet) {
											if (item.contains("NOT-")) {
												isAllPositive = false;
											} else {
												isAllNegative = false;
											}
										}
										/*
										 * it could be that the support of (A,
										 * NOT-B) is less than minSupp and the
										 * support of (B, NOT-A) is less than
										 * minSupp, but the sum of their supports is
										 * greater than minSupport. check for those
										 */
										float complementaryFreqItemSupport;
										Set<String> complementaryFreqItemSet = new HashSet<String>();
										for (String freqItem : freqItemSet) {
											if (freqItem.contains("NOT-")) {
												complementaryFreqItemSet.add(freqItem.replace("NOT-", ""));
											} else {
												complementaryFreqItemSet.add("NOT-" + freqItem);
											}
										}
										Iterator<String> it = complementaryFreqItemSet.iterator();
										complementaryFreqItemSupport = finder.getSupport(it.next(), it.next());

										if (!isAllNegative && !isAllPositive
												&& (frequentItemSetSupportMap.get(freqItemSet)
														+ complementaryFreqItemSupport) >= support) {
											Set<String> candidateSet = new HashSet<String>();
											for (String item : freqItemSet)
												candidateSet.add(item.replace("NOT-", ""));
											if (candidateSet.size() != numberOfParameters) {
												System.out.println("Strange for this to be here: " + freqItemSet + " @ "
														+ frequentItemSetSupportMap.get(freqItemSet) + " @ " + support
														+ " @ " + candidateSet);
												continue;
											}
											candidateDispositionSet.add(candidateSet);
										}
									}

									noCandidates = candidateDispositionSet.size();
									System.out.println("No. candidates: " + candidateDispositionSet.size());
									for (Set<String> candidateDisposition : candidateDispositionSet)
										System.out.println(candidateDisposition);
									System.out.println("----------");

									candidateList = new ArrayList<List<String>>();
									candidateListArray = new String[noCandidates][numberOfParameters];
									for (Set<String> candidateDisposition : candidateDispositionSet) {
										List<String> candidate = new ArrayList<String>();
										Iterator<String> candidateDispositionIterator = candidateDisposition.iterator();
										while (candidateDispositionIterator.hasNext()) {
											candidate.add(candidateDispositionIterator.next());
											// candidateListArray[candidateIndex][paramIndex++] =
											// candidateDispositionIterator.next();
										}
										candidateList.add(candidate);
									}

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(List<String> candidate: candidateList){
									// System.out.println(candidate);
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence,
									// candidatedList);
									if ((printVerbose) && alpha == 0) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [not co-existence; exclusive choice]" + ": "
														+ aprioriLocalWatch.msecs() + " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [not co-existence; exclusive choice]" + ": "
														+ candidateList.size());
									} else if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [exclusive choice]" + ": " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Not_Chain_Succession)
									|| template.equals(DeclareTemplate.Not_Succession)) {
								/*
								 * We should consider dispositions of the form (A,
								 * B), (A, NOT-B) and (NOT-A, B) provided the sum of
								 * their support is > minSupport
								 */
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									if (printVerbose) {
										// pw.println("start generation (positive/negative) frequent sets of size
										// "+noparam+" with support "+supp/3.f+" [not succession]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppFirstPositive, suppFirstNegative;
									for (int i = 0; i < noDispositions; i++) {
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);// supp(A,B)
										suppFirstPositive = finder.getSupport(dispositions[i][0],
												"NOT-" + dispositions[i][1]);// supp (A,NOT-B);
										suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0],
												dispositions[i][1]);// supp (NOT-A,B);

										if (suppBothPositive + suppFirstPositive + suppFirstNegative >= support) {
											// we should add both (A,B) and (B,A) as candidates
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											candidateList.add(candidateDisposition);
											// candidateDispositionSet.add(candidateDisposition);
											System.out.println(candidateDisposition);

											candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											candidateList.add(candidateDisposition);
											// candidateDispositionSet.add(candidateDisposition);
											System.out.println(candidateDisposition);
										}
									}
									System.out.println("No. Candidates: " + candidateList.size());
									// System.out.println("No. Candidates: "+candidateDispositionSet.size());
									//
									// int candidateIndex = 0;
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									// }

									// System.out.println("Candidate List Size: "+candidateList.length);
									// for(int i = 0; i < candidateList.length; i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateList[i][j]+", ");
									// System.out.println();
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
									// candidatedList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
									// candidatedList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 3.f
														+ " [not succession]" + ": " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 3.f
														+ " [not succession]" + ": " + candidateList.size());
									}
								}
							}

						} else {
							System.out.println("alpha " + alpha);
							if (template.equals(DeclareTemplate.Precedence)
									|| template.equals(DeclareTemplate.Alternate_Precedence) ||
									template.equals(DeclareTemplate.Chain_Precedence) ||
									template.equals(DeclareTemplate.Responded_Existence)
									|| template.equals(DeclareTemplate.Response) ||
									template.equals(DeclareTemplate.Chain_Response)
									|| template.equals(DeclareTemplate.Alternate_Response)) {
								if (((template.equals(DeclareTemplate.Responded_Existence)
										|| template.equals(DeclareTemplate.Response) ||
										template.equals(DeclareTemplate.Chain_Response)
										|| template.equals(DeclareTemplate.Alternate_Response))
										&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("respon"))
										|| ((template.equals(DeclareTemplate.Precedence)
												|| template.equals(DeclareTemplate.Alternate_Precedence) ||
												template.equals(DeclareTemplate.Chain_Precedence))
												&& !frequentItemSetTypeFrequentItemSetSupportMap
														.containsKey("precedence"))) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									if (printVerbose) {
										// pw.println("start generation for (positive) frequent sets of size "+noparam+"
										// and (positive/negative) frequent sets of size 1 with support "+supp/2.f+"
										// [responded existence; (simple, alternate, chain) response; (simple,
										// alternate, chain) precedence]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// print the supp values
									for (String activity : activityNameList) {
										System.out.println(activity + " @ " + finder.getSupport(activity));
									}
									for (int i = 0; i < noDispositions; i++) {
										System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
												+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppFirstNegative, suppSecondNegative;
									for (int i = 0; i < noDispositions; i++) {
										// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A) >
										// minSupp
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
										suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0]);
										suppSecondNegative = finder.getSupport("NOT-" + dispositions[i][1]);
										boolean bothDirection = true;
										if (suppBothPositive + suppFirstNegative >= support) {
											// add (A,B)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
											// System.out.println(candidateDisposition);
											bothDirection = false;
										}

										if (suppBothPositive + suppSecondNegative >= support) {
											// add (B,A)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
											// System.out.println(candidateDisposition);
											if (!bothDirection)
												bothDirection = true;
											else
												bothDirection = false;
										}

										// if(!bothDirection){
										// System.out.println("this dispositon contains only one direction");
										// }

									}

									System.out.println("No. Candidates: " + candidateList.size());
									for (List<String> candidate : candidateList)
										System.out.println(candidate);
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// int candidateIndex = 0;
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									//// System.out.println(candidateDisposition);
									// }

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive) frequent sets of size "
														+ numberOfParameters
														+ " and (positive/negative) frequent sets of size 1 with support "
														+ support / 2.f
														+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
														+ aprioriLocalWatch.msecs() + " msecs");
										pw.println("number of dispositions for (positive) frequent sets of size "
												+ numberOfParameters
												+ " and (positive/negative) frequent sets of size 1 with support "
												+ support / 2.f
												+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
												+ candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Alternate_Succession)
									|| template.equals(DeclareTemplate.Chain_Succession) ||
									template.equals(DeclareTemplate.Succession)
									|| template.equals(DeclareTemplate.CoExistence)) {
								if ((template.equals(DeclareTemplate.CoExistence)
										&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("coexistence"))
										|| ((template.equals(DeclareTemplate.Alternate_Succession)
												|| template.equals(DeclareTemplate.Chain_Succession) ||
												template.equals(DeclareTemplate.Succession)
														&& !frequentItemSetTypeFrequentItemSetSupportMap
																.containsKey("succession")))) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}
									if (printVerbose) {
										// pw.println("start generation (positive/negative) frequent sets of size
										// "+noparam+" with support "+supp/2.f+" [succession; co-existence]");
									}

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									// print the supp values
									for (String activity : activityNameList) {
										System.out.println(activity + " @ " + finder.getSupport(activity));
									}
									for (int i = 0; i < noDispositions; i++) {
										System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
												+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
									}

									System.out.println("Exploring Candidates");

									candidateList = new ArrayList<List<String>>();
									// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
									float suppBothPositive, suppBothNegative;
									for (int i = 0; i < noDispositions; i++) {
										// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A, NOT-B)
										// > minSupp
										suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
										suppBothNegative = finder.getSupport("NOT-" + dispositions[i][0],
												"NOT-" + dispositions[i][1]);

										if (suppBothPositive + suppBothNegative >= support) {
											// add (A,B) and (B,A) both (but for co-existence only one)
											List<String> candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][0]);
											candidateDisposition.add(dispositions[i][1]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);

											System.out.println(candidateDisposition);

											if (!template.equals(DeclareTemplate.CoExistence)) {
												candidateDisposition = new ArrayList<String>();
												candidateDisposition.add(dispositions[i][1]);
												candidateDisposition.add(dispositions[i][0]);
												// candidateDispositionSet.add(candidateDisposition);
												candidateList.add(candidateDisposition);
											}
										}

										// if(!bothDirection){
										// System.out.println("this dispositon contains only one direction");
										// }

									}

									System.out.println("No. Candidates: " + candidateList.size());
									// System.out.println("No. Candidates: "+candidateDispositionSet.size());
									// candidateListArray = new
									// String[candidateDispositionSet.size()][numberOfParameters];
									// int candidateIndex = 0;
									// for(List<String> candidateDisposition : candidateDispositionSet){
									// int paramIndex = 0;
									// for(String candidateItem : candidateDisposition)
									// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
									// candidateIndex++;
									// System.out.println(candidateDisposition);
									// }

									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }

									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [succession; co-existence]: " + aprioriLocalWatch.msecs()
														+ " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 2.f
														+ " [succession; co-existence]: " + candidateList.size());
									}
								}
							}

							if (template.equals(DeclareTemplate.Not_CoExistence)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" with support "+supp/3.f+" [not co-existence]");
								}

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;
									// int noDispositions = 0;
									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}
								candidateList = new ArrayList<List<String>>();
								// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
								float suppBothPositive;
								for (int i = 0; i < noDispositions; i++) {
									// for every combination (A,B), if 1-supp(A,B) >= minSupport, then we need to
									// explore
									suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
									if (100 - suppBothPositive >= support) {
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][0]);
										candidateDisposition.add(dispositions[i][1]);
										// candidateDispositionSet.add(candidateDisposition);
										candidateList.add(candidateDisposition);
									}
								}
								System.out.println("No. Candidates: " + candidateList.size());
								// System.out.println("No. Candidates: "+candidateDispositionSet.size());
								// candidateListArray = new
								// String[candidateDispositionSet.size()][numberOfParameters];
								// int candidateIndex = 0;
								// for(List<String> candidateDisposition : candidateDispositionSet){
								// int paramIndex = 0;
								// for(String candidateItem : candidateDisposition)
								// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
								// candidateIndex++;
								//// System.out.println(candidateDisposition);
								// }

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(int i = 0; i < candidateList.size(); i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateListArray[i][j]+", ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for f(positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 3.f
													+ " [not co-existence]: " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 3.f
											+ " [not co-existence]: " + candidateList.size());
								}
							}

							if (template.equals(DeclareTemplate.Not_Succession)
									|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
								if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
									if (printVerbose) {
										aprioriLocalWatch.start();
									}

									/*
									 * need to add both (A,B) and (B,A) irrespective of support values
									 */

									/*
									 * The combination generator gives the set of indices for each combination
									 * get all nC2 combinations for 'n' activities
									 */
									String[][] dispositions = null;
									int noDispositions = 0;
									if (activityNameList.size() > 1) {
										CombinationGenerator combinationGenerator = new CombinationGenerator(
												activityNameList.size(), 2);
										dispositions = new String[combinationGenerator.getTotal().intValue()][2];
										int[] indices;
										// int noDispositions = 0;
										while (combinationGenerator.hasMore()) {
											indices = combinationGenerator.getNext();
											for (int k = 0; k < indices.length; k++) {
												dispositions[noDispositions][k] = activityNameList.get(indices[k]);
											}
											noDispositions++;
										}
									} else {
										dispositions = new String[0][2];
									}
									candidateList = new ArrayList<List<String>>();
									System.out.println("No. Candidates: " + dispositions.length * 2);
									candidateListArray = new String[dispositions.length * 2][numberOfParameters];
									for (int i = 0; i < noDispositions; i++) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(dispositions[i][0]);
										candidate.add(dispositions[i][1]);
										candidateList.add(candidate);

										candidate = new ArrayList<String>();
										candidate.add(dispositions[i][1]);
										candidate.add(dispositions[i][0]);
										candidateList.add(candidate);

										// candidateListArray[candidateIndex][0] = dispositions[i][0];
										// candidateListArray[candidateIndex][1] = dispositions[i][1];
										// candidateIndex++;
										// candidateListArray[candidateIndex][0] = dispositions[i][1];
										// candidateListArray[candidateIndex][1] = dispositions[i][0];
										// candidateIndex++;
									}

									System.out.println("Candidate List Size: " + candidateList.size());

									// System.out.println("Candidate List Size: "+candidateList.size());
									// for(int i = 0; i < candidateList.size(); i++){
									// for(int j = 0; j < numberOfParameters; j++)
									// System.out.print(candidateListArray[i][j]+", ");
									// System.out.println();
									// }
									// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
									declareTemplateCandidateDispositionsMap.put(template, candidateList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
									// candidatedList);
									// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
									// candidatedList);
									if (printVerbose) {
										pw.println(
												"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 4.f
														+ " [not succession]: " + aprioriLocalWatch.msecs() + " msecs");
										pw.println(
												"number of dispositions for (positive/negative) frequent sets of size "
														+ numberOfParameters + " with support " + support / 4.f
														+ " [not succession]: " + candidateList.size());
									}
								}
							}
						}

						if (template.equals(DeclareTemplate.Exactly1) || template.equals(DeclareTemplate.Exactly2) ||
								template.equals(DeclareTemplate.Existence)
								|| template.equals(DeclareTemplate.Existence2) ||
								template.equals(DeclareTemplate.Existence3) || template.equals(DeclareTemplate.Init)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive) frequent sets of size "+noparam+"
									// [unary templates]");
								}

								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									if (finder.getSupport(activity) >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(activity);
										candidateList.add(candidate);
									}
								}
								System.out.println("Candidate List Size: " + candidateList.size());
								for (List<String> candidate : candidateList)
									System.out.println(candidate);

								declareTemplateCandidateDispositionsMap.put(template, candidateList);

								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ noparam + " [unary templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size " + noparam
											+ " [unary templates]: " + candidateList.size());
								}

							}
						}

						if (template.equals(DeclareTemplate.Absence)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" [absence templates]");
								}

								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									if (finder.getSupport("NOT-" + activity) >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(activity);
										candidateList.add(candidate);
									}
								}

								System.out.println("Candidate List Size: " + candidateList.size());

								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ noparam + " [absence templates]: " + candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								int noparam = 1;
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" [absence templates]");
								}
								candidateList = new ArrayList<List<String>>();
								for (String activity : activityNameList) {
									List<String> candidate = new ArrayList<String>();
									candidate.add(activity);
									candidateList.add(candidate);
								}

								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ noparam + " [absence templates]: " + candidateList.size());
								}
							}
						}
					}
				}
				if (printVerbose) {
					pw.println("END APRIORI - time: " + aprioriWatch.msecs() + " msecs");
					pw.println("  ");
				}

				int value = 0;

				System.out.println("JC: " + declareTemplateCandidateDispositionsMap.keySet());
				for (DeclareTemplate template : selectedTemplates) {

					candidateList = declareTemplateCandidateDispositionsMap.get(template);
					if (candidateList != null) {
						value = value + candidateList.size();
					}
				}
				if (context != null)
					context.getProgress().setMaximum(value);

			} else {
				for (ConstraintDefinition cd : inputModel.getModel().getConstraintDefinitions()) {

					if (!selectedTemplates.contains(getTemplate(cd))) {
						selectedTemplates.add(getTemplate(cd));
					}

					Iterator<Parameter> iter = cd.getParameters().iterator();
					Parameter p1 = iter.next();
					Parameter p2 = null;
					if (iter.hasNext()) {
						p2 = iter.next();
					}
					String key = "EMPTY_PARAM";
					ArrayList<String> param = new ArrayList<String>();
					if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p1).iterator().next() != null) {
						key = cd.getBranches(p1).iterator().next().getName();

					}

					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (!dmg.hasEventTypeInName(key)) {
							key = key + "-" + input.getReferenceEventType();
						}
					}
					param.add(key);
					if (p2 != null) {
						if (cd.getBranches(p1).iterator().hasNext() && cd.getBranches(p2).iterator().next() != null) {
							key = cd.getBranches(p2).iterator().next().getName();
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
								if (!dmg.hasEventTypeInName(key)) {
									key = key + "-" + input.getReferenceEventType();
								}
							}
							param.add(key);
						} else {
							key = "EMPTY_PARAM";
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
								if (!dmg.hasEventTypeInName(key)) {
									key = key + "-" + input.getReferenceEventType();
								}
							}
							param.add(key);
						}
					}

					ArrayList<String> invPar = new ArrayList<String>();
					invPar.add(param.get(1));
					invPar.add(param.get(0));

					List<List<String>> currparams = null;
					if (declareTemplateCandidateDispositionsMap.containsKey(getTemplate(cd))) {
						currparams = declareTemplateCandidateDispositionsMap.get(getTemplate(cd));
					} else {
						currparams = new ArrayList<List<String>>();
					}
					if (getTemplate(cd).equals(DeclareTemplate.Precedence)
							|| getTemplate(cd).equals(DeclareTemplate.Alternate_Precedence) ||
							getTemplate(cd).equals(DeclareTemplate.Chain_Precedence)) {
						currparams.add(invPar);
					} else {
						currparams.add(param);
					}
					declareTemplateCandidateDispositionsMap.put(getTemplate(cd), currparams);

					if (cd.getName().equals("chain response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

					}

					if (cd.getName().equals("chain precedence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
					}

					if (cd.getName().equals("alternate response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
					}

					if (cd.getName().equals("alternate precedence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
					}

					if (cd.getName().equals("alternate succession")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
					}

					if (cd.getName().equals("response")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Response)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Response);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

					}

					if (cd.getName().equals("succession")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}

					}

					if (cd.getName().equals("precedence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Precedence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(invPar.get(0));
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);
						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}

					}

					if (cd.getName().equals("responded existence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Response)) {
							selectedTemplates.add(DeclareTemplate.Chain_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Response, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Response)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Response);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Response)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Response);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Response, params);

						if (!selectedTemplates.contains(DeclareTemplate.Response)) {
							selectedTemplates.add(DeclareTemplate.Response);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.CoExistence)) {
							selectedTemplates.add(DeclareTemplate.CoExistence);
						}

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Chain_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Precedence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Precedence)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Precedence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Precedence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Precedence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Precedence, params);

						if (!selectedTemplates.contains(DeclareTemplate.Precedence)) {
							selectedTemplates.add(DeclareTemplate.Precedence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.CoExistence, params);

					}

					if (cd.getName().equals("co-existence")) {
						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Chain_Succession)) {
							selectedTemplates.add(DeclareTemplate.Chain_Succession);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Alternate_Succession)) {
							selectedTemplates.add(DeclareTemplate.Alternate_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

						if (!selectedTemplates.contains(DeclareTemplate.Succession)) {
							selectedTemplates.add(DeclareTemplate.Succession);
						}

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Chain_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Chain_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Alternate_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Alternate_Succession, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Succession, params);

					}

					if (cd.getName().equals("existence")) {

						List<List<String>> params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);

						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);

						if (!selectedTemplates.contains(DeclareTemplate.Existence2)) {
							selectedTemplates.add(DeclareTemplate.Existence2);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}

					}

					if (cd.getName().equals("existence2")) {

						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);

						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}
					}

					if (cd.getName().equals("absence2")) {
						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}

					}

					if (cd.getName().equals("absence3")) {
						List<List<String>> params = null;

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence2)) {
							selectedTemplates.add(DeclareTemplate.Absence2);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}
					}

					if (cd.getName().equals("choice")) {
						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);
						if (!selectedTemplates.contains(DeclareTemplate.Init)) {
							selectedTemplates.add(DeclareTemplate.Init);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence)) {
							selectedTemplates.add(DeclareTemplate.Existence);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence2)) {
							selectedTemplates.add(DeclareTemplate.Existence2);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);
						if (!selectedTemplates.contains(DeclareTemplate.Existence3)) {
							selectedTemplates.add(DeclareTemplate.Existence3);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly1)) {
							selectedTemplates.add(DeclareTemplate.Exactly1);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exactly2)) {
							selectedTemplates.add(DeclareTemplate.Exactly2);
						}

						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Init)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Init, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence2, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Existence3)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Existence3);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Existence3, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly1)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly1);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly1, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exactly2)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exactly2);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exactly2, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

					}

					if (cd.getName().equals("not chain succession")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);
						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_Succession)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_Succession);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_Succession)) {
							selectedTemplates.add(DeclareTemplate.Not_Succession);
						}
						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_CoExistence)) {
							selectedTemplates.add(DeclareTemplate.Not_CoExistence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

					if (cd.getName().equals("not succession")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Not_CoExistence)) {
							selectedTemplates.add(DeclareTemplate.Not_CoExistence);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Not_CoExistence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Not_CoExistence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

					if (cd.getName().equals("not co-existence")) {

						List<List<String>> params = null;
						ArrayList<String> initPar = new ArrayList<String>();
						initPar.add(param.get(0));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);
						if (!selectedTemplates.contains(DeclareTemplate.Absence)) {
							selectedTemplates.add(DeclareTemplate.Absence);
						}
						params = null;
						initPar = new ArrayList<String>();
						initPar.add(param.get(1));

						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Absence)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Absence);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(initPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Absence, params);

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(param);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

						if (!selectedTemplates.contains(DeclareTemplate.Exclusive_Choice)) {
							selectedTemplates.add(DeclareTemplate.Exclusive_Choice);
						}

						params = null;
						if (declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Exclusive_Choice)) {
							params = declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Exclusive_Choice);
						} else {
							params = new ArrayList<List<String>>();
						}
						params.add(invPar);
						declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Exclusive_Choice, params);

					}

				}
			}
			Pruner pruner = new Pruner(context, log, input, pw);
			declareMinerOutput = pruner.fastPrune(false, false, log, input, declareTemplateCandidateDispositionsMap);
			int constraintsNo = 0;
			for (ConstraintDefinition cd : declareMinerOutput.getModel().getModel().getConstraintDefinitions()) {
				// if(cd.isVisible()){
				constraintsNo++;
				// }
			}

			int activitiesNo = 0;
			for (ActivityDefinition ad : declareMinerOutput.getModel().getModel().getActivityDefinitions()) {
				// if(ad.isVisible()){
				activitiesNo++;
				// }
			}
			System.out.println("number of discovered constraints: " + constraintsNo);
			System.out.println("number of activities: " + activitiesNo);
			if (printVerbose) {
				pw.println("total time: " + overall.msecs() + " msecs");
				pw.println("number of discovered constraints: "
						+ declareMinerOutput.getModel().getModel().constraintDefinitionsCount());
				pw.println(
						"number of activities: " + declareMinerOutput.getModel().getModel().activityDefinitionsCount());
				pw.flush();
				pw.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		declareMinerOutput.setHier(false);
		declareMinerOutput.setTrans(false);
		return declareMinerOutput;
	}

	private static void printInputConfiguration(DeclareMinerInput input) {
		System.out.println("min Support: " + input.getMinSupport());
		System.out.println("Alpha: " + input.getAlpha());
		System.out.println("Selected Declare Perspective: " + input.getDeclarePerspectiveSet());
		System.out.println("Declare-Constraint Map Size: " + input.getDeclareTemplateConstraintTemplateMap().size());
		System.out.println("No. Selected Templates: " + input.getSelectedDeclareTemplateSet().size());
		System.out.println("Selected Declare Templates: " + input.getSelectedDeclareTemplateSet());
		System.out.println("Map Template Configuration: " + input.getMapTemplateConfiguration());
	}

	private static boolean isPositiveFrequentItemType(DeclareTemplate template) {
		return template.equals(DeclareTemplate.Alternate_Precedence)
				|| template.equals(DeclareTemplate.Alternate_Response) ||
				template.equals(DeclareTemplate.Alternate_Succession)
				|| template.equals(DeclareTemplate.Chain_Precedence) ||
				template.equals(DeclareTemplate.Chain_Response) || template.equals(DeclareTemplate.Chain_Succession) ||
				template.equals(DeclareTemplate.CoExistence) || template.equals(DeclareTemplate.Precedence) ||
				template.equals(DeclareTemplate.Responded_Existence) || template.equals(DeclareTemplate.Response) ||
				template.equals(DeclareTemplate.Succession);
	}

	private static boolean isBinaryTemplate(DeclareTemplate template) {
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

	private static boolean isOneHalfPositiveFrequentItemType(DeclareTemplate template) {
		return template.equals(DeclareTemplate.Exactly1) || template.equals(DeclareTemplate.Exactly2) ||
				template.equals(DeclareTemplate.Existence) || template.equals(DeclareTemplate.Existence2) ||
				template.equals(DeclareTemplate.Existence3) || template.equals(DeclareTemplate.Init);
	}

	public static void main(String[] args) {
		Set<DeclarePerspective> declarePerspectiveSet = new HashSet<DeclarePerspective>();
		declarePerspectiveSet.add(DeclarePerspective.Control_Flow);

		Set<DeclareTemplate> selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
		DeclareTemplate[] declareTemplates = DeclareTemplate.values();
		for (DeclareTemplate d : declareTemplates)
			selectedDeclareTemplateSet.add(d);

		Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();

		for (DeclareTemplate d : declareTemplates) {
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}

		Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = readConstraintTemplates(
				templateNameStringDeclareTemplateMap);

		/*
		 * read the log
		 * 
		 */
		String outputDir = "C:\\Users\\fmaggi\\Desktop\\Exp\\Results";
		if (!new File(outputDir).exists()) {
			new File(outputDir).mkdirs();
		}

		for (int k = 250; k <= 250; k += 250) {
			XLog log = null;
			String inputLogFileName = "C:\\Users\\fmaggi\\Desktop\\Log1DiscFiltrato.mxml.gz";
			if (inputLogFileName.toLowerCase().contains("mxml.gz")) {
				XMxmlGZIPParser parser = new XMxmlGZIPParser();
				if (parser.canParse(new File(inputLogFileName))) {
					try {
						log = parser.parse(new File(inputLogFileName)).get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (inputLogFileName.toLowerCase().contains("mxml")) {
				XMxmlParser parser = new XMxmlParser();
				if (parser.canParse(new File(inputLogFileName))) {
					try {
						log = parser.parse(new File(inputLogFileName)).get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (inputLogFileName.toLowerCase().contains("xes.gz")) {
				XesXmlGZIPParser parser = new XesXmlGZIPParser();
				if (parser.canParse(new File(inputLogFileName))) {
					try {
						log = parser.parse(new File(inputLogFileName)).get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (inputLogFileName.toLowerCase().contains("xes")) {
				XesXmlParser parser = new XesXmlParser();
				if (parser.canParse(new File(inputLogFileName))) {
					try {
						log = parser.parse(new File(inputLogFileName)).get(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (log == null) {
				return;
			}

			int noRuns = 1;
			for (int i = 1; i <= noRuns; i++) {
				for (int alpha = 0; alpha <= 100; alpha += 50) {
					for (int support = 100; support >= 0; support -= 50) {
						DeclareMinerInput input = new DeclareMinerInput();
						String outputFileName = "Run_" + i + "_Alpha_" + alpha + "_Apriori_" + support + ".txt";
						String declareMapOutputFileName = "Complete_newCode_Traditional_A" + alpha + "_S" + support
								+ ".xml";
						File declareMapOutputFile = new File(declareMapOutputFileName);
						input.setAlpha(alpha);
						input.setDeclarePerspectiveSet(declarePerspectiveSet);
						input.setMinSupport(support);
						input.setSelectedDeclareTemplateSet(selectedDeclareTemplateSet);
						input.setDeclareTemplateConstraintTemplateMap(declareTemplateConstraintTemplateMap);
						input.setOutputDir(outputDir);
						input.setOutputFileName(outputFileName);
						DeclareMinerOutput output = mineDeclareConstraints(null, log, input);
						new DeclareExportComplete().export(null, output, declareMapOutputFile);
						System.gc();
					}
				}
			}
		}
		System.out.println("finished!");
	}

	public static Map<DeclareTemplate, ConstraintTemplate> readConstraintTemplates(
			Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap) {
		InputStream templateInputStream = DeclareMinerNoRed.class.getResourceAsStream("/resources/template.xml");
		File languageFile = null;
		try {
			languageFile = File.createTempFile("template", ".xml");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(templateInputStream));
			String line = bufferedReader.readLine();
			PrintStream out = new PrintStream(languageFile);
			while (line != null) {
				out.println(line);
				line = bufferedReader.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		TemplateBroker templateBroker = XMLBrokerFactory.newTemplateBroker(languageFile.getAbsolutePath());
		List<Language> languagesList = templateBroker.readLanguages();

		// the first language in the list is the condec language, which is what we need
		Language condecLanguage = languagesList.get(0);
		List<IItem> templateList = new ArrayList<IItem>();
		List<IItem> condecLanguageChildrenList = condecLanguage.getChildren();
		for (IItem condecLanguageChild : condecLanguageChildrenList) {
			if (condecLanguageChild instanceof LanguageGroup) {
				templateList.addAll(visit(condecLanguageChild));
			} else {
				templateList.add(condecLanguageChild);
			}
		}

		Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = new HashMap<DeclareTemplate, ConstraintTemplate>();

		for (IItem item : templateList) {
			if (item instanceof ConstraintTemplate) {
				ConstraintTemplate constraintTemplate = (ConstraintTemplate) item;
				// System.out.println(constraintTemplate.getName()+" @
				// "+constraintTemplate.getDescription()+" @ "+constraintTemplate.getText());
				if (templateNameStringDeclareTemplateMap
						.containsKey(constraintTemplate.getName().replaceAll("-", "").toLowerCase())) {
					declareTemplateConstraintTemplateMap.put(
							templateNameStringDeclareTemplateMap
									.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()),
							constraintTemplate);
					System.out.println(constraintTemplate.getName() + " @ " + templateNameStringDeclareTemplateMap
							.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()));
				}
			}
		}

		return declareTemplateConstraintTemplateMap;
	}

	private static List<IItem> visit(IItem item) {
		List<IItem> templateList = new ArrayList<IItem>();
		if (item instanceof LanguageGroup) {
			LanguageGroup languageGroup = (LanguageGroup) item;
			List<IItem> childrenList = languageGroup.getChildren();
			for (IItem child : childrenList) {
				if (child instanceof LanguageGroup) {
					templateList.addAll(visit(child));
				} else {
					templateList.add(child);
				}
			}
		}
		return templateList;
	}

	private static HashMap<DeclareTemplate, ConstraintTemplate> readConstraintTemplates() {
		InputStream templateInputStream = DeclareMinerNoRed.class.getResourceAsStream("/resources/template.xml");
		File languageFile = null;
		try {
			languageFile = File.createTempFile("template", ".xml");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(templateInputStream));
			String line = bufferedReader.readLine();
			PrintStream out = new PrintStream(languageFile);
			while (line != null) {
				out.println(line);
				line = bufferedReader.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		TemplateBroker templateBroker = XMLBrokerFactory.newTemplateBroker(languageFile.getAbsolutePath());
		List<Language> languagesList = templateBroker.readLanguages();

		// the first language in the list is the condec language, which is what we need
		Language condecLanguage = languagesList.get(0);
		List<IItem> templateList = new ArrayList<IItem>();
		List<IItem> condecLanguageChildrenList = condecLanguage.getChildren();
		for (IItem condecLanguageChild : condecLanguageChildrenList) {
			if (condecLanguageChild instanceof LanguageGroup) {
				templateList.addAll(visit(condecLanguageChild));
			} else {
				templateList.add(condecLanguageChild);
			}
		}

		HashMap<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = new HashMap<DeclareTemplate, ConstraintTemplate>();
		Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();

		for (IItem item : templateList) {
			if (item instanceof ConstraintTemplate) {
				ConstraintTemplate constraintTemplate = (ConstraintTemplate) item;
				// System.out.println(constraintTemplate.getName()+" @
				// "+constraintTemplate.getDescription()+" @ "+constraintTemplate.getText());
				if (templateNameStringDeclareTemplateMap
						.containsKey(constraintTemplate.getName().replaceAll("-", "").toLowerCase())) {
					declareTemplateConstraintTemplateMap.put(
							templateNameStringDeclareTemplateMap
									.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()),
							constraintTemplate);
					System.out.println(constraintTemplate.getName() + " @ " + templateNameStringDeclareTemplateMap
							.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()));
				}
			}
		}
		return declareTemplateConstraintTemplateMap;
	}

	public static DeclareMinerOutput mineDeclareConstraints(UIPluginContext context, XLog log,
			DeclareMinerInput input) {
		DeclareMinerOutput declareMinerOutput = null;

		printInputConfiguration(input);

		Set<Set<String>> conceptGroupingSet = new HashSet<Set<String>>();
		// String parentDir = "C:\\Users\\fmaggi\\Desktop";
		String parentDir = input.getAprioriKnowledgeConceptFileName();
		if (parentDir != null) {
			File[] conceptGroupFileNames = new File(parentDir).listFiles();

			// String group1FileName = "G1.txt";
			// String group2FileName = "G2.txt";
			// String group3FileName = "G3.txt";
			//
			FileIO io = new FileIO();
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group1FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group2FileName));
			// System.out.println("G3: "+io.readFileAsSet(parentDir, group3FileName));
			// conceptGroupingSet.add(io.readFileAsSet(parentDir, group3FileName));

			for (File conceptGroupFile : conceptGroupFileNames) {
				conceptGroupingSet.add(io.readFileAsSet(parentDir, conceptGroupFile.getName()));
			}
			System.out.println("COncept Groups: " + conceptGroupingSet);

		}
		if (context != null) {
			context.getProgress().setValue(5);
		}
		float support = input.getMinSupport();
		float alpha = input.getAlpha() / 100.0f;

		// change traditional = true to start the traditional algorithm
		boolean traditional = false;
		// boolean printVerbose = true;
		boolean printVerbose = input.isVerbose();

		// String output = "C:\\Users\\fmaggi\\Desktop\\Exp\\";
		// if(traditional){
		// output = output+"alpha"+alpha+"Traditional.txt";
		// }else{
		// output = output+"alpha"+alpha+"Apriori"+support+".txt";
		// }

		String output = input.getOutputDir() + System.getProperty("file.separator") + input.getOutputFileName();
		output = "./output_temp.txt";

		Watch overall = new Watch();
		if (printVerbose) {
			overall.start();
		}

		FindItemSets finder = new FindItemSets(log, input);

		Watch aprioriWatch = new Watch();
		Watch aprioriLocalWatch = new Watch();

		try {
			PrintWriter pw = null;
			if (printVerbose) {
				pw = new PrintWriter(new FileWriter(new File(output)));
				if (traditional) {
					pw.println("trad");
				} else {
					pw.println("minimum support for apriori algorithm: " + (support) / 100.0);
				}
				pw.println("alpha value: " + alpha);
				pw.println("  ");
				pw.flush();
				aprioriLocalWatch.start();
			}
			aprioriWatch.start();

			DeclareModelGenerator dmg = new DeclareModelGenerator();

			List<String> activityNameList = new ArrayList<String>();
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					String label = (XConceptExtension.instance().extractName(event));
					if (input.getAprioriKnowledgeBasedCriteriaSet()
							.contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)) {
						if (event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION) != null) {
							label = (XConceptExtension.instance().extractName(event)) + "-"
									+ XLifecycleExtension.instance().extractTransition(event);
						} else {
							label = (XConceptExtension.instance().extractName(event)) + "-"
									+ input.getReferenceEventType();
						}
					}
					if (!activityNameList.contains(label)) {
						activityNameList.add(label);
					}
				}
			}

			String[] activityNamesArray = new String[activityNameList.size()];
			for (int i = 0; i < activityNameList.size(); i++) {
				activityNamesArray[i] = activityNameList.get(i);
			}

			Map<FrequentItemSetType, Map<Set<String>, Float>> frequentItemSetTypeFrequentItemSetSupportMap = new HashMap<FrequentItemSetType, Map<Set<String>, Float>>();
			// Map<DeclareTemplate, String[][]> declareTemplateCandidateDispositionsMap =
			// new HashMap<DeclareTemplate, String[][]>();
			Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap = new HashMap<DeclareTemplate, List<List<String>>>();

			Hashtable<String, Object> aprioriSupportValues = new Hashtable<String, Object>();

			String[][] candidateListArray = null;
			List<List<String>> candidateList;
			if (printVerbose) {
				pw.println("precomputation time: " + aprioriLocalWatch.msecs() + " msecs");
				pw.println("   ");
				pw.println("START APRIORI");
			}
			if (context != null) {
				context.getProgress().setMinimum(0);
				context.getProgress().setMaximum(112);
				context.getProgress().setIndeterminate(false);
				context.getProgress().setValue(1);
			}
			Set<DeclareTemplate> selectedTemplates = input.getSelectedDeclareTemplateSet();
			if (!traditional) {
				for (DeclareTemplate template : selectedTemplates) {
					if (alpha == 0 || template.equals(DeclareTemplate.Choice)
							|| template.equals(DeclareTemplate.Exclusive_Choice)) {

						if (template.equals(DeclareTemplate.Alternate_Precedence)
								|| template.equals(DeclareTemplate.Alternate_Response) ||
								template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.CoExistence)
								|| template.equals(DeclareTemplate.Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, null);
						}
						if (template.equals(DeclareTemplate.Exclusive_Choice)
								|| template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Succession)
								|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Choice)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, null);
						}
					} else {
						if (template.equals(DeclareTemplate.Precedence)
								|| template.equals(DeclareTemplate.Alternate_Precedence) ||
								template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Alternate_Response)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, null);
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, null);
						}
						if (template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.Succession)
								|| template.equals(DeclareTemplate.CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_CoExistence)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, null);
						}
						if (template.equals(DeclareTemplate.Not_Chain_Succession)
								|| template.equals(DeclareTemplate.Not_Succession)) {
							frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, null);
						}
					}

					if (isOneHalfPositiveFrequentItemType(template)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, null);
					}

					if (template.equals(DeclareTemplate.Absence)) {
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, null);
					}

					if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
						Map<Set<String>, Float> frequentItemSetSupportMap = DeclareModelGenerator
								.getFrequentItemSetSupportMap(1, log, 0, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_All,
								frequentItemSetSupportMap);
					}

				}

				boolean previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Four_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
							support / 4.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Four_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Three_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 3.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.Four_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 3.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Three_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Three_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Three_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Four_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Four_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							String element2 = iterator.next();
							if (!element1.contains("NOT-") && !element2.contains("NOT-")) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Half_Positive, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(2, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = null;
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.Half_Positive)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Half_Positive);
							for (Set<String> key : previousMap.keySet()) {
								if (previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						} else {
							if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Half_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Half_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Three_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Three_Negative);
							} else if (frequentItemSetTypeFrequentItemSetSupportMap
									.containsKey(FrequentItemSetType.Four_Negative)) {
								previousMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Four_Negative);
							}
							for (Set<String> key : previousMap.keySet()) {
								Iterator<String> iterator = key.iterator();
								String element1 = iterator.next();
								String element2 = iterator.next();
								if (!element1.contains("NOT-") && !element2.contains("NOT-")
										&& previousMap.get(key) >= support) {
									map.put(key, previousMap.get(key));
								}
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.Positive, map);
					}
					previous = true;
				}

				previous = false;

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Negative)) {
					Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
							support / 2.f, true, pw, input);
					frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Negative, map);
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support / 2.f, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support / 2.f) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Positive, map);
					}
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Negative)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, true, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					} else {
						Map<Set<String>, Float> previousMap = frequentItemSetTypeFrequentItemSetSupportMap
								.get(FrequentItemSetType.One_Negative);
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							if (previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Negative, map);
					}
					previous = true;
				}

				if (frequentItemSetTypeFrequentItemSetSupportMap.containsKey(FrequentItemSetType.One_Half_Positive)) {
					if (!previous) {
						Map<Set<String>, Float> map = DeclareModelGenerator.getFrequentItemSetSupportMap(1, log,
								support, false, pw, input);
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);

					} else {
						Map<Set<String>, Float> previousMap = null;
						if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Half_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Half_Negative);
						} else if (frequentItemSetTypeFrequentItemSetSupportMap
								.containsKey(FrequentItemSetType.One_Negative)) {
							previousMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.One_Negative);
						}
						Map<Set<String>, Float> map = new HashMap<Set<String>, Float>();
						for (Set<String> key : previousMap.keySet()) {
							Iterator<String> iterator = key.iterator();
							String element1 = iterator.next();
							if (!element1.contains("NOT-") && previousMap.get(key) >= support) {
								map.put(key, previousMap.get(key));
							}
						}
						frequentItemSetTypeFrequentItemSetSupportMap.put(FrequentItemSetType.One_Half_Positive, map);
					}
					previous = true;
				}
			}

			int numberOfParameters;
			for (DeclareTemplate template : selectedTemplates) {
				if (isBinaryTemplate(template)) {
					numberOfParameters = 2;
				} else {
					numberOfParameters = 1;
				}

				if (traditional) {
					if (printVerbose) {
						aprioriLocalWatch.start();
					}
					candidateListArray = DispositionsGenerator.generateDisp(activityNamesArray, numberOfParameters);
					int noCandidates = candidateListArray.length;
					candidateList = new ArrayList<List<String>>();
					for (int i = 0; i < noCandidates; i++) {
						List<String> candidate = new ArrayList<String>();
						for (int j = 0; j < candidateListArray[i].length; j++)
							candidate.add(candidateListArray[i][j]);
						candidateList.add(candidate);
					}
					// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
					declareTemplateCandidateDispositionsMap.put(template, candidateList);
					if (printVerbose) {
						pw.println("time needed to generate all the dispositions (traditional approach) of size "
								+ numberOfParameters + ": " + aprioriLocalWatch.msecs() + " msecs");
						pw.println("number of dispositions (traditional approach) of size " + numberOfParameters + ": "
								+ candidateList.size());
					}
				} else {
					if (alpha == 0 || template.equals(DeclareTemplate.Choice)
							|| template.equals(DeclareTemplate.Exclusive_Choice)) {
						if (isPositiveFrequentItemType(template)) {

							if (printVerbose) {
								aprioriLocalWatch.start();
							}

							Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
									.get(FrequentItemSetType.Positive);
							int noCandidates = 0;
							candidateListArray = new String[frequentItemSetSupportMap.size()
									* Factorial.fatt(numberOfParameters)][numberOfParameters];
							candidateList = new ArrayList<List<String>>();
							for (Set<String> frequentItemSet : frequentItemSetSupportMap.keySet()) {
								int itemIndex = 0;
								String[] frequentItemSetArray = new String[frequentItemSet.size()];
								for (String freqItem : frequentItemSet)
									frequentItemSetArray[itemIndex++] = freqItem;

								String[][] dispositions = null;
								dispositions = DispositionsGenerator.generateDisp(frequentItemSetArray,
										numberOfParameters);

								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
									dispositions = pruneDispositions(dispositions);
									// noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
									dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
									// noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
									dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
									// noDispositions = dispositions.length;
								}

								for (int i = 0; i < dispositions.length; i++) {
									List<String> candidate = new ArrayList<String>();
									for (int j = 0; j < numberOfParameters; j++) {
										candidateListArray[noCandidates][j] = dispositions[i][j];
										candidate.add(dispositions[i][j]);
									}
									candidateList.add(candidate);
									noCandidates++;
								}
							}

							// print candidates
							// System.out.println("No. candidates: "+noCandidates+" @ CanddiateListSize:
							// "+candidatedList.length);
							// for(int i = 0; i < candidatedList.length; i++){
							// for(int j = 0; j < numberOfParameters; j++)
							// System.out.print(candidatedList[i][j]+" ");
							// System.out.println();
							// }

							// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
							declareTemplateCandidateDispositionsMap.put(template, candidateList);
							//

							if ((printVerbose) && alpha == 0) {
								pw.println(
										"time needed to generate all the dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support
												+ " [positive relation; simple choice]" + ": "
												+ aprioriLocalWatch.msecs() + " msecs");
								pw.println("number of dispositions for (positive) frequent sets of size "
										+ numberOfParameters + " with support " + support
										+ " [positive relation templates; simple choice]" + ": "
										+ candidateList.size());
							} else if (printVerbose) {
								pw.println(
										"time needed to generate all the dispositions for (positive) frequent sets of size "
												+ numberOfParameters + " with support " + support + " [simple choice]"
												+ ": " + aprioriLocalWatch.msecs() + " msecs");
								pw.println("number of dispositions for (positive) frequent sets of size "
										+ numberOfParameters + " with support " + support + " [simple choice]" + ": "
										+ candidateList.size());
							}
						}

						if (template.equals(DeclareTemplate.Choice)) {
							/*
							 * The basic idea here is to explore all
							 * combinations of size 2 that satisfy the sum of
							 * support of both the individual activities is greater than
							 * minSupp
							 */
							if (printVerbose) {
								aprioriLocalWatch.start();
							}
							System.out.println("HERE: " + declareTemplateCandidateDispositionsMap.keySet());
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								int[] indices;
								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									// int noDispositions = 0;
									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
									dispositions = pruneDispositions(dispositions);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
									dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
									dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}

								// System.out.println("Dispositions");
								// for(int j = 0; j < noDispositions; j++){
								// for(int k = 0; k < 2; k++)
								// System.out.print(dispositions[j][k]+" ");
								// System.out.println();
								// }

								candidateList = new ArrayList<List<String>>();
								int noCandidates = 0;
								// boolean[] isCandidateDisposition = new boolean[noDispositions];
								float suppActivity1, suppActivity2;
								for (int i = 0; i < noDispositions; i++) {
									// isCandidateDisposition[i] = false;
									suppActivity1 = finder.getSupport(dispositions[i][0].replaceAll("NOT-", ""));
									suppActivity2 = finder.getSupport(dispositions[i][1].replaceAll("NOT-", ""));
									if (suppActivity1 + suppActivity2 >= support) {
										List<String> candidate = new ArrayList<String>();
										candidate.add(dispositions[i][0]);
										candidate.add(dispositions[i][1]);
										candidateList.add(candidate);

										// isCandidateDisposition[i] = true;
										noCandidates++;
										// System.out.println("C: " + dispositions[i][0] + " , " + dispositions[i][1]);
									}
								}

								// candidateListArray = new String[noCandidates][numberOfParameters];
								// int candidateIndex = 0;
								// for(int i = 0; i < noDispositions; i++){
								// if(isCandidateDisposition[i]){
								// candidateListArray[candidateIndex][0] = dispositions[i][0];
								// candidateListArray[candidateIndex++][1] = dispositions[i][1];
								// }
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								// print candidates

								if ((printVerbose) && alpha == 0) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support / 2.f
													+ " [simple choice]" + ": " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support / 2.f + " [simple choice]"
											+ ": " + candidateList.size());
								} else if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters + " with support " + support / 2.f
													+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters + " with support " + support / 2.f + " [simple choice]"
											+ ": " + candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Exclusive_Choice)
								|| template.equals(DeclareTemplate.Not_CoExistence)) {
							/*
							 * The basic idea is to explore all combinations of
							 * the type (A, NOT-B) and (NOT-A, B) that have a
							 * support greater than minSupport
							 */
							Set<Set<String>> candidateDispositionSet = new HashSet<Set<String>>();
							if (printVerbose) {
								aprioriLocalWatch.start();
							}
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								Map<Set<String>, Float> frequentItemSetSupportMap = frequentItemSetTypeFrequentItemSetSupportMap
										.get(FrequentItemSetType.Half_Negative);
								System.out.println("Freq Item Set: ");
								for (Set<String> freqItem : frequentItemSetSupportMap.keySet())
									System.out.println(freqItem + " @ " + frequentItemSetSupportMap.get(freqItem));

								System.out.println("Exploring Candidates: " + numberOfParameters);
								int noCandidates = 0;
								for (Set<String> freqItemSet : frequentItemSetSupportMap.keySet()) {
									if (freqItemSet.size() != numberOfParameters)
										continue;
									// check if both activities are positive or both negative; ignore such
									// combinations
									boolean isAllPositive = true;
									boolean isAllNegative = true;
									for (String item : freqItemSet) {
										if (item.contains("NOT-")) {
											isAllPositive = false;
										} else {
											isAllNegative = false;
										}
									}
									/*
									 * it could be that the support of (A,
									 * NOT-B) is less than minSupp and the
									 * support of (B, NOT-A) is less than
									 * minSupp, but the sum of their supports is
									 * greater than minSupport. check for those
									 */
									float complementaryFreqItemSupport;
									Set<String> complementaryFreqItemSet = new HashSet<String>();
									for (String freqItem : freqItemSet) {
										if (freqItem.contains("NOT-")) {
											complementaryFreqItemSet.add(freqItem.replace("NOT-", ""));
										} else {
											complementaryFreqItemSet.add("NOT-" + freqItem);
										}
									}
									Iterator<String> it = complementaryFreqItemSet.iterator();
									complementaryFreqItemSupport = finder.getSupport(it.next(), it.next());

									if (!isAllNegative && !isAllPositive && (frequentItemSetSupportMap.get(freqItemSet)
											+ complementaryFreqItemSupport) >= support) {
										Set<String> candidateSet = new HashSet<String>();
										for (String item : freqItemSet)
											candidateSet.add(item.replace("NOT-", ""));
										if (candidateSet.size() != numberOfParameters) {
											System.out.println("Strange for this to be here: " + freqItemSet + " @ "
													+ frequentItemSetSupportMap.get(freqItemSet) + " @ " + support
													+ " @ " + candidateSet);
											continue;
										}
										candidateDispositionSet.add(candidateSet);
									}
								}

								noCandidates = candidateDispositionSet.size();
								System.out.println("No. candidates: " + candidateDispositionSet.size());
								for (Set<String> candidateDisposition : candidateDispositionSet)
									System.out.println(candidateDisposition);
								System.out.println("----------");

								candidateList = new ArrayList<List<String>>();
								candidateListArray = new String[noCandidates][numberOfParameters];
								for (Set<String> candidateDisposition : candidateDispositionSet) {
									List<String> candidate = new ArrayList<String>();
									Iterator<String> candidateDispositionIterator = candidateDisposition.iterator();
									while (candidateDispositionIterator.hasNext()) {
										candidate.add(candidateDispositionIterator.next());
										// candidateListArray[candidateIndex][paramIndex++] =
										// candidateDispositionIterator.next();
									}
									candidateList.add(candidate);
								}

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(List<String> candidate: candidateList){
								// System.out.println(candidate);
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_CoExistence,
								// candidatedList);
								if ((printVerbose) && alpha == 0) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 2.f
													+ " [not co-existence; exclusive choice]" + ": "
													+ aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 2.f
											+ " [not co-existence; exclusive choice]" + ": " + candidateList.size());
								} else if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 2.f
													+ " [exclusive choice]" + ": " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 2.f
											+ " [exclusive choice]" + ": " + candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Not_Chain_Succession)
								|| template.equals(DeclareTemplate.Not_Succession)) {
							/*
							 * We should consider dispositions of the form (A,
							 * B), (A, NOT-B) and (NOT-A, B) provided the sum of
							 * their support is > minSupport
							 */
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}

								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" with support "+supp/3.f+" [not succession]");
								}

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;
									// int noDispositions = 0;
									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
									dispositions = pruneDispositions(dispositions);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
									dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
									dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}

								System.out.println("Exploring Candidates");

								candidateList = new ArrayList<List<String>>();
								// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
								float suppBothPositive, suppFirstPositive, suppFirstNegative;
								for (int i = 0; i < noDispositions; i++) {
									suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);// supp(A,B)
									suppFirstPositive = finder.getSupport(dispositions[i][0],
											"NOT-" + dispositions[i][1]);// supp (A,NOT-B);
									suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0],
											dispositions[i][1]);// supp (NOT-A,B);

									if (suppBothPositive + suppFirstPositive + suppFirstNegative >= support) {
										// we should add both (A,B) and (B,A) as candidates
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][0]);
										candidateDisposition.add(dispositions[i][1]);
										candidateList.add(candidateDisposition);
										// candidateDispositionSet.add(candidateDisposition);
										System.out.println(candidateDisposition);

										candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][1]);
										candidateDisposition.add(dispositions[i][0]);
										candidateList.add(candidateDisposition);
										// candidateDispositionSet.add(candidateDisposition);
										System.out.println(candidateDisposition);
									}
								}
								System.out.println("No. Candidates: " + candidateList.size());
								// System.out.println("No. Candidates: "+candidateDispositionSet.size());
								//
								// int candidateIndex = 0;
								// candidateListArray = new
								// String[candidateDispositionSet.size()][numberOfParameters];
								// for(List<String> candidateDisposition : candidateDispositionSet){
								// int paramIndex = 0;
								// for(String candidateItem : candidateDisposition)
								// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
								// candidateIndex++;
								// }

								// System.out.println("Candidate List Size: "+candidateList.length);
								// for(int i = 0; i < candidateList.length; i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateList[i][j]+", ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
								// candidatedList);
								// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
								// candidatedList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 3.f
													+ " [not succession]" + ": " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 3.f
											+ " [not succession]" + ": " + candidateList.size());
								}
							}
						}

					} else {
						System.out.println("alpha " + alpha);
						if (template.equals(DeclareTemplate.Precedence)
								|| template.equals(DeclareTemplate.Alternate_Precedence) ||
								template.equals(DeclareTemplate.Chain_Precedence) ||
								template.equals(DeclareTemplate.Responded_Existence)
								|| template.equals(DeclareTemplate.Response) ||
								template.equals(DeclareTemplate.Chain_Response)
								|| template.equals(DeclareTemplate.Alternate_Response)) {
							if (((template.equals(DeclareTemplate.Responded_Existence)
									|| template.equals(DeclareTemplate.Response) ||
									template.equals(DeclareTemplate.Chain_Response)
									|| template.equals(DeclareTemplate.Alternate_Response))
									&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("respon"))
									|| ((template.equals(DeclareTemplate.Precedence)
											|| template.equals(DeclareTemplate.Alternate_Precedence) ||
											template.equals(DeclareTemplate.Chain_Precedence))
											&& !frequentItemSetTypeFrequentItemSetSupportMap
													.containsKey("precedence"))) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}

								if (printVerbose) {
									// pw.println("start generation for (positive) frequent sets of size "+noparam+"
									// and (positive/negative) frequent sets of size 1 with support "+supp/2.f+"
									// [responded existence; (simple, alternate, chain) response; (simple,
									// alternate, chain) precedence]");
								}

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;
									// int noDispositions = 0;
									Set<String> activityNameSet = new HashSet<String>();
									Set<String> activityNameWithoutEventTypeSet = new HashSet<String>();

									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
									dispositions = pruneDispositions(dispositions);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
									dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
									dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}

								// print the supp values
								for (String activity : activityNameList) {
									System.out.println(activity + " @ " + finder.getSupport(activity));
								}
								for (int i = 0; i < noDispositions; i++) {
									System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
											+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
								}

								System.out.println("Exploring Candidates");

								candidateList = new ArrayList<List<String>>();
								// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
								float suppBothPositive, suppFirstNegative, suppSecondNegative;
								for (int i = 0; i < noDispositions; i++) {
									// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A) >
									// minSupp
									suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
									suppFirstNegative = finder.getSupport("NOT-" + dispositions[i][0]);
									suppSecondNegative = finder.getSupport("NOT-" + dispositions[i][1]);
									boolean bothDirection = true;
									if (suppBothPositive + suppFirstNegative >= support) {
										// add (A,B)
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][0]);
										candidateDisposition.add(dispositions[i][1]);
										// candidateDispositionSet.add(candidateDisposition);
										candidateList.add(candidateDisposition);
										// System.out.println(candidateDisposition);
										bothDirection = false;
									}

									if (suppBothPositive + suppSecondNegative >= support) {
										// add (B,A)
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][1]);
										candidateDisposition.add(dispositions[i][0]);
										// candidateDispositionSet.add(candidateDisposition);
										candidateList.add(candidateDisposition);
										// System.out.println(candidateDisposition);
										if (!bothDirection)
											bothDirection = true;
										else
											bothDirection = false;
									}

									// if(!bothDirection){
									// System.out.println("this dispositon contains only one direction");
									// }

								}

								System.out.println("No. Candidates: " + candidateList.size());
								for (List<String> candidate : candidateList)
									System.out.println(candidate);
								// candidateListArray = new
								// String[candidateDispositionSet.size()][numberOfParameters];
								// int candidateIndex = 0;
								// for(List<String> candidateDisposition : candidateDispositionSet){
								// int paramIndex = 0;
								// for(String candidateItem : candidateDisposition)
								// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
								// candidateIndex++;
								//// System.out.println(candidateDisposition);
								// }

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(int i = 0; i < candidateList.size(); i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateListArray[i][j]+", ");
								// System.out.println();
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive) frequent sets of size "
													+ numberOfParameters
													+ " and (positive/negative) frequent sets of size 1 with support "
													+ support / 2.f
													+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
													+ aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive) frequent sets of size "
											+ numberOfParameters
											+ " and (positive/negative) frequent sets of size 1 with support "
											+ support / 2.f
											+ " [responded existence; (simple, alternate, chain) response; (simple, alternate, chain) precedence]: "
											+ candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Alternate_Succession)
								|| template.equals(DeclareTemplate.Chain_Succession) ||
								template.equals(DeclareTemplate.Succession)
								|| template.equals(DeclareTemplate.CoExistence)) {
							if ((template.equals(DeclareTemplate.CoExistence)
									&& !frequentItemSetTypeFrequentItemSetSupportMap.containsKey("coexistence"))
									|| ((template.equals(DeclareTemplate.Alternate_Succession)
											|| template.equals(DeclareTemplate.Chain_Succession) ||
											template.equals(DeclareTemplate.Succession)
													&& !frequentItemSetTypeFrequentItemSetSupportMap
															.containsKey("succession")))) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}
								if (printVerbose) {
									// pw.println("start generation (positive/negative) frequent sets of size
									// "+noparam+" with support "+supp/2.f+" [succession; co-existence]");
								}

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {
									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;

									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}

								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
									dispositions = pruneDispositions(dispositions);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
									dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
									dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}

								// print the supp values
								for (String activity : activityNameList) {
									System.out.println(activity + " @ " + finder.getSupport(activity));
								}
								for (int i = 0; i < noDispositions; i++) {
									System.out.println(dispositions[i][0] + "," + dispositions[i][1] + " @ "
											+ finder.getSupport(dispositions[i][0], dispositions[i][1]));
								}

								System.out.println("Exploring Candidates");

								candidateList = new ArrayList<List<String>>();
								// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
								float suppBothPositive, suppBothNegative;
								for (int i = 0; i < noDispositions; i++) {
									// for every combination (A,B), we need to check if supp(A,B)+supp(NOT-A, NOT-B)
									// > minSupp
									suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
									suppBothNegative = finder.getSupport("NOT-" + dispositions[i][0],
											"NOT-" + dispositions[i][1]);

									if (suppBothPositive + suppBothNegative >= support) {
										// add (A,B) and (B,A) both (but for co-existence only one)
										List<String> candidateDisposition = new ArrayList<String>();
										candidateDisposition.add(dispositions[i][0]);
										candidateDisposition.add(dispositions[i][1]);
										// candidateDispositionSet.add(candidateDisposition);
										candidateList.add(candidateDisposition);

										System.out.println(candidateDisposition);

										if (!template.equals(DeclareTemplate.CoExistence)) {
											candidateDisposition = new ArrayList<String>();
											candidateDisposition.add(dispositions[i][1]);
											candidateDisposition.add(dispositions[i][0]);
											// candidateDispositionSet.add(candidateDisposition);
											candidateList.add(candidateDisposition);
										}
									}

									// if(!bothDirection){
									// System.out.println("this dispositon contains only one direction");
									// }

								}

								System.out.println("No. Candidates: " + candidateList.size());
								// System.out.println("No. Candidates: "+candidateDispositionSet.size());
								// candidateListArray = new
								// String[candidateDispositionSet.size()][numberOfParameters];
								// int candidateIndex = 0;
								// for(List<String> candidateDisposition : candidateDispositionSet){
								// int paramIndex = 0;
								// for(String candidateItem : candidateDisposition)
								// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
								// candidateIndex++;
								// System.out.println(candidateDisposition);
								// }

								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(int i = 0; i < candidateList.size(); i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateListArray[i][j]+", ");
								// System.out.println();
								// }

								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 2.f
													+ " [succession; co-existence]: " + aprioriLocalWatch.msecs()
													+ " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 2.f
											+ " [succession; co-existence]: " + candidateList.size());
								}
							}
						}

						if (template.equals(DeclareTemplate.Not_CoExistence)) {
							if (printVerbose) {
								aprioriLocalWatch.start();
							}
							if (printVerbose) {
								// pw.println("start generation (positive/negative) frequent sets of size
								// "+noparam+" with support "+supp/3.f+" [not co-existence]");
							}

							/*
							 * The combination generator gives the set of indices for each combination
							 * get all nC2 combinations for 'n' activities
							 */
							String[][] dispositions = null;
							int noDispositions = 0;
							if (activityNameList.size() > 1) {
								CombinationGenerator combinationGenerator = new CombinationGenerator(
										activityNameList.size(), 2);
								dispositions = new String[combinationGenerator.getTotal().intValue()][2];
								int[] indices;
								// int noDispositions = 0;
								while (combinationGenerator.hasMore()) {
									indices = combinationGenerator.getNext();
									for (int k = 0; k < indices.length; k++) {
										dispositions[noDispositions][k] = activityNameList.get(indices[k]);
									}
									noDispositions++;
								}
							} else {
								dispositions = new String[0][2];
							}
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
								dispositions = pruneDispositions(dispositions);
								noDispositions = dispositions.length;
							}
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
								dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
								noDispositions = dispositions.length;
							}
							if (input.getAprioriKnowledgeBasedCriteriaSet()
									.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
								dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
								noDispositions = dispositions.length;
							}

							candidateList = new ArrayList<List<String>>();
							// Set<List<String>> candidateDispositionSet = new HashSet<List<String>>();
							float suppBothPositive;
							for (int i = 0; i < noDispositions; i++) {
								// for every combination (A,B), if 1-supp(A,B) >= minSupport, then we need to
								// explore
								suppBothPositive = finder.getSupport(dispositions[i][0], dispositions[i][1]);
								if (100 - suppBothPositive >= support) {
									List<String> candidateDisposition = new ArrayList<String>();
									candidateDisposition.add(dispositions[i][0]);
									candidateDisposition.add(dispositions[i][1]);
									// candidateDispositionSet.add(candidateDisposition);
									candidateList.add(candidateDisposition);
								}
							}
							System.out.println("No. Candidates: " + candidateList.size());
							// System.out.println("No. Candidates: "+candidateDispositionSet.size());
							// candidateListArray = new
							// String[candidateDispositionSet.size()][numberOfParameters];
							// int candidateIndex = 0;
							// for(List<String> candidateDisposition : candidateDispositionSet){
							// int paramIndex = 0;
							// for(String candidateItem : candidateDisposition)
							// candidateListArray[candidateIndex][paramIndex++] = candidateItem;
							// candidateIndex++;
							//// System.out.println(candidateDisposition);
							// }

							// System.out.println("Candidate List Size: "+candidateList.size());
							// for(int i = 0; i < candidateList.size(); i++){
							// for(int j = 0; j < numberOfParameters; j++)
							// System.out.print(candidateListArray[i][j]+", ");
							// System.out.println();
							// }

							// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
							declareTemplateCandidateDispositionsMap.put(template, candidateList);
							if (printVerbose) {
								pw.println(
										"time needed to generate all the dispositions for f(positive/negative) frequent sets of size "
												+ numberOfParameters + " with support " + support / 3.f
												+ " [not co-existence]: " + aprioriLocalWatch.msecs() + " msecs");
								pw.println("number of dispositions for (positive/negative) frequent sets of size "
										+ numberOfParameters + " with support " + support / 3.f
										+ " [not co-existence]: " + candidateList.size());
							}
						}

						if (template.equals(DeclareTemplate.Not_Succession)
								|| template.equals(DeclareTemplate.Not_Chain_Succession)) {
							if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
								if (printVerbose) {
									aprioriLocalWatch.start();
								}

								/*
								 * need to add both (A,B) and (B,A) irrespective of support values
								 */

								/*
								 * The combination generator gives the set of indices for each combination
								 * get all nC2 combinations for 'n' activities
								 */
								String[][] dispositions = null;
								int noDispositions = 0;
								if (activityNameList.size() > 1) {

									CombinationGenerator combinationGenerator = new CombinationGenerator(
											activityNameList.size(), 2);
									dispositions = new String[combinationGenerator.getTotal().intValue()][2];
									int[] indices;
									// int noDispositions = 0;
									while (combinationGenerator.hasMore()) {
										indices = combinationGenerator.getNext();
										for (int k = 0; k < indices.length; k++) {
											dispositions[noDispositions][k] = activityNameList.get(indices[k]);
										}
										noDispositions++;
									}
								} else {
									dispositions = new String[0][2];
								}

								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.Diversity)) {
									dispositions = pruneDispositions(dispositions);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.IntraGroup)) {
									dispositions = pruneForIntraGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}
								if (input.getAprioriKnowledgeBasedCriteriaSet()
										.contains(AprioriKnowledgeBasedCriteria.InterGroup)) {
									dispositions = pruneForInterGroupDispositions(dispositions, conceptGroupingSet);
									noDispositions = dispositions.length;
								}

								candidateList = new ArrayList<List<String>>();
								System.out.println("No. Candidates: " + dispositions.length * 2);
								candidateListArray = new String[dispositions.length * 2][numberOfParameters];
								for (int i = 0; i < noDispositions; i++) {
									List<String> candidate = new ArrayList<String>();
									candidate.add(dispositions[i][0]);
									candidate.add(dispositions[i][1]);
									candidateList.add(candidate);

									candidate = new ArrayList<String>();
									candidate.add(dispositions[i][1]);
									candidate.add(dispositions[i][0]);
									candidateList.add(candidate);

									// candidateListArray[candidateIndex][0] = dispositions[i][0];
									// candidateListArray[candidateIndex][1] = dispositions[i][1];
									// candidateIndex++;
									// candidateListArray[candidateIndex][0] = dispositions[i][1];
									// candidateListArray[candidateIndex][1] = dispositions[i][0];
									// candidateIndex++;
								}

								System.out.println("Candidate List Size: " + candidateList.size());

								// System.out.println("Candidate List Size: "+candidateList.size());
								// for(int i = 0; i < candidateList.size(); i++){
								// for(int j = 0; j < numberOfParameters; j++)
								// System.out.print(candidateListArray[i][j]+", ");
								// System.out.println();
								// }
								// declareTemplateCandidateDispositionsMap.put(template, candidateListArray);
								declareTemplateCandidateDispositionsMap.put(template, candidateList);
								// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Succession,
								// candidatedList);
								// declareTemplateCandidateDispositionsMap.put(DeclareTemplate.Not_Chain_Succession,
								// candidatedList);
								if (printVerbose) {
									pw.println(
											"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
													+ numberOfParameters + " with support " + support / 4.f
													+ " [not succession]: " + aprioriLocalWatch.msecs() + " msecs");
									pw.println("number of dispositions for (positive/negative) frequent sets of size "
											+ numberOfParameters + " with support " + support / 4.f
											+ " [not succession]: " + candidateList.size());
								}
							}
						}
					}

					if (template.equals(DeclareTemplate.Exactly1) || template.equals(DeclareTemplate.Exactly2) ||
							template.equals(DeclareTemplate.Existence) || template.equals(DeclareTemplate.Existence2) ||
							template.equals(DeclareTemplate.Existence3) || template.equals(DeclareTemplate.Init)) {
						if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
							if (printVerbose) {
								aprioriLocalWatch.start();
							}
							int noparam = 1;
							if (printVerbose) {
								// pw.println("start generation (positive) frequent sets of size "+noparam+"
								// [unary templates]");
							}

							candidateList = new ArrayList<List<String>>();
							for (String activity : activityNameList) {
								if (finder.getSupport(activity) >= support) {
									List<String> candidate = new ArrayList<String>();
									candidate.add(activity);
									candidateList.add(candidate);
								}
							}
							System.out.println("Candidate List Size: " + candidateList.size());
							for (List<String> candidate : candidateList)
								System.out.println(candidate);

							declareTemplateCandidateDispositionsMap.put(template, candidateList);

							if (printVerbose) {
								pw.println(
										"time needed to generate all the dispositions for (positive) frequent sets of size "
												+ noparam + " [unary templates]: " + aprioriLocalWatch.msecs()
												+ " msecs");
								pw.println("number of dispositions for (positive) frequent sets of size " + noparam
										+ " [unary templates]: " + candidateList.size());
							}

						}
					}

					if (template.equals(DeclareTemplate.Absence)) {
						if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
							if (printVerbose) {
								aprioriLocalWatch.start();
							}
							int noparam = 1;
							if (printVerbose) {
								// pw.println("start generation (positive/negative) frequent sets of size
								// "+noparam+" [absence templates]");
							}

							candidateList = new ArrayList<List<String>>();
							for (String activity : activityNameList) {
								if (finder.getSupport("NOT-" + activity) >= support) {
									List<String> candidate = new ArrayList<String>();
									candidate.add(activity);
									candidateList.add(candidate);
								}
							}

							System.out.println("Candidate List Size: " + candidateList.size());

							declareTemplateCandidateDispositionsMap.put(template, candidateList);
							if (printVerbose) {
								pw.println(
										"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
												+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
												+ " msecs");
								pw.println("number of dispositions for (positive/negative) frequent sets of size "
										+ noparam + " [absence templates]: " + candidateList.size());
							}
						}
					}

					if (template.equals(DeclareTemplate.Absence2) || template.equals(DeclareTemplate.Absence3)) {
						if (!declareTemplateCandidateDispositionsMap.containsKey(template)) {
							if (printVerbose) {
								aprioriLocalWatch.start();
							}
							int noparam = 1;
							if (printVerbose) {
								// pw.println("start generation (positive/negative) frequent sets of size
								// "+noparam+" [absence templates]");
							}
							candidateList = new ArrayList<List<String>>();
							for (String activity : activityNameList) {
								List<String> candidate = new ArrayList<String>();
								candidate.add(activity);
								candidateList.add(candidate);
							}

							declareTemplateCandidateDispositionsMap.put(template, candidateList);
							if (printVerbose) {
								pw.println(
										"time needed to generate all the dispositions for (positive/negative) frequent sets of size "
												+ noparam + " [absence templates]: " + aprioriLocalWatch.msecs()
												+ " msecs");
								pw.println("number of dispositions for (positive/negative) frequent sets of size "
										+ noparam + " [absence templates]: " + candidateList.size());
							}
						}
					}
				}
			}
			long aprioriTime = aprioriWatch.msecs();
			if (printVerbose) {
				pw.println("END APRIORI - time: " + aprioriTime + " msecs");
				pw.println("  ");
			}
			// UnifiedLogger.logAprioriTime(aprioriTime);

			int value = 0;

			System.out.println("JC: " + declareTemplateCandidateDispositionsMap.keySet());
			for (DeclareTemplate template : selectedTemplates) {

				candidateList = declareTemplateCandidateDispositionsMap.get(template);
				candidateList = declareTemplateCandidateDispositionsMap.get(template);
				if (candidateList != null) {
					value = value + candidateList.size();
				}
			}
			if (context != null)
				context.getProgress().setMaximum(value);

			Watch prunerWatch = new Watch();
			if (printVerbose) {
				prunerWatch.start();
			}
			Pruner pruner = new Pruner(context, log, input, pw);
			declareMinerOutput = pruner.fastPrune(false, false, log, input, declareTemplateCandidateDispositionsMap);
			int constraintsNo = 0;
			for (ConstraintDefinition cd : declareMinerOutput.getModel().getModel().getConstraintDefinitions()) {
				// if(cd.isVisible()){
				constraintsNo++;
				// }
			}

			int activitiesNo = 0;
			for (ActivityDefinition ad : declareMinerOutput.getModel().getModel().getActivityDefinitions()) {
				// if(ad.isVisible()){
				activitiesNo++;
				// }
			}
			System.out.println("number of discovered constraints: " + constraintsNo);
			System.out.println("number of activities: " + activitiesNo);
			if (printVerbose) {
				pw.println("Pruner time: " + prunerWatch.msecs() + " msecs");
				pw.println("total time: " + overall.msecs() + " msecs");
				pw.println("total time: " + overall.msecs() + " msecs");
				pw.println("number of discovered constraints: "
						+ declareMinerOutput.getModel().getModel().constraintDefinitionsCount());
				pw.println(
						"number of activities: " + declareMinerOutput.getModel().getModel().activityDefinitionsCount());
				pw.flush();
				pw.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		declareMinerOutput.setHier(false);
		declareMinerOutput.setTrans(false);
		return declareMinerOutput;

	}

	public static DeclareTemplate getTemplate(ConstraintDefinition constraint) {
		if (constraint.getName().toLowerCase().equals("absence")) {
			return DeclareTemplate.Absence;
		}
		if (constraint.getName().toLowerCase().equals("absence2")) {
			return DeclareTemplate.Absence2;
		}
		if (constraint.getName().toLowerCase().equals("absence3")) {
			return DeclareTemplate.Absence3;
		}
		if (constraint.getName().toLowerCase().equals("alternate precedence")) {
			return DeclareTemplate.Alternate_Precedence;
		}
		if (constraint.getName().toLowerCase().equals("alternate response")) {
			return DeclareTemplate.Alternate_Response;
		}
		if (constraint.getName().toLowerCase().equals("alternate succession")) {
			return DeclareTemplate.Alternate_Succession;
		}
		if (constraint.getName().toLowerCase().equals("chain precedence")) {
			return DeclareTemplate.Chain_Precedence;
		}
		if (constraint.getName().toLowerCase().equals("chain response")) {
			return DeclareTemplate.Chain_Response;
		}
		if (constraint.getName().toLowerCase().equals("chain succession")) {
			return DeclareTemplate.Chain_Succession;
		}
		if (constraint.getName().toLowerCase().equals("precedence")) {
			return DeclareTemplate.Precedence;
		}
		if (constraint.getName().toLowerCase().equals("response")) {
			return DeclareTemplate.Response;
		}
		if (constraint.getName().toLowerCase().equals("succession")) {
			return DeclareTemplate.Succession;
		}
		if (constraint.getName().toLowerCase().equals("responded existence")) {
			return DeclareTemplate.Responded_Existence;
		}
		if (constraint.getName().toLowerCase().equals("co-existence")) {
			return DeclareTemplate.CoExistence;
		}
		if (constraint.getName().toLowerCase().equals("exclusive choice")) {
			return DeclareTemplate.Exclusive_Choice;
		}
		if (constraint.getName().toLowerCase().equals("choice")) {
			return DeclareTemplate.Choice;
		}
		if (constraint.getName().toLowerCase().equals("existence")) {
			return DeclareTemplate.Existence;
		}
		if (constraint.getName().toLowerCase().equals("existence2")) {
			return DeclareTemplate.Existence2;
		}
		if (constraint.getName().toLowerCase().equals("existence3")) {
			return DeclareTemplate.Existence3;
		}
		if (constraint.getName().toLowerCase().equals("exactly1")) {
			return DeclareTemplate.Exactly1;
		}
		if (constraint.getName().toLowerCase().equals("exactly2")) {
			return DeclareTemplate.Exactly2;
		}
		if (constraint.getName().toLowerCase().equals("init")) {
			return DeclareTemplate.Init;
		}
		if (constraint.getName().toLowerCase().equals("not chain succession")) {
			return DeclareTemplate.Not_Chain_Succession;
		}
		if (constraint.getName().toLowerCase().equals("not succession")) {
			return DeclareTemplate.Not_Succession;
		}
		if (constraint.getName().toLowerCase().equals("not co-existence")) {
			return DeclareTemplate.Not_CoExistence;
		}
		return null;
	}

	private static float learnSupport(DeclareMinerInput input, AssignmentModel model, XLog log, float alpha,
			FindItemSets finder) {

		float support = 1;

		for (ConstraintDefinition constraint : model.getConstraintDefinitions()) {

			TemplateInfo templateInfo = null;
			DeclareTemplate template = getTemplate(constraint);
			switch (template) {
				case Succession:
				case Alternate_Succession:
				case Chain_Succession:
					templateInfo = new SuccessionInfo();
					break;
				case Choice:
					templateInfo = new ChoiceInfo();
					break;
				case Exclusive_Choice:
					templateInfo = new ExclusiveChoiceInfo();
					break;
				case Existence:
				case Existence2:
				case Existence3:
					templateInfo = new ExistenceInfo();
					break;
				case Init:
					templateInfo = new InitInfo();
					break;
				case Absence:
					templateInfo = new AbsenceInfo();
					break;
				case Absence2:
				case Absence3:
					templateInfo = new Absence2Info();
					break;
				case Exactly1:
				case Exactly2:
					templateInfo = new Exactly1Info();
					break;
				case Precedence:
				case Alternate_Precedence:
				case Chain_Precedence:
					templateInfo = new PrecedenceInfo();
					break;
				case Responded_Existence:
				case Response:
				case Alternate_Response:
				case Chain_Response:
					templateInfo = new ResponseInfo();
					break;
				case CoExistence:
					templateInfo = new CoexistenceInfo();
					break;
				case Not_CoExistence:
					templateInfo = new NotCoexistenceInfo();
					break;
				case Not_Succession:
				case Not_Chain_Succession:
					templateInfo = new NegativeRelationInfo();
					break;
			}
			List<String> parameters = new ArrayList<String>();
			for (Parameter parameter : constraint.getParameters()) {
				parameters.add(constraint.getBranches(parameter).iterator().next().getName());
			}
			MetricsValues metricsValues = templateInfo.computeMetrics(input, template, parameters, log, null, alpha,
					finder);
			if (metricsValues.getSupportRule() < support) {
				support = metricsValues.getSupportRule();
			}
		}
		return support;
	}

	private static String[][] pruneDispositions(String[][] dispositions) {
		int noDispositions = dispositions.length;
		boolean[] isValidDisposition = new boolean[noDispositions];

		for (int i = 0; i < noDispositions; i++) {
			isValidDisposition[i] = false;
		}

		int noValidDispositions = 0;
		StandardModel[] lifeCycleTransitions = XLifecycleExtension.StandardModel.values();

		Set<String> itemSetWithoutEventTypes = new HashSet<String>();
		for (int i = 0; i < noDispositions; i++) {
			itemSetWithoutEventTypes.clear();
			for (String item : dispositions[i]) {
				int index = -1;
				for (StandardModel transition : lifeCycleTransitions) {
					if (item.toLowerCase().contains("-" + transition.toString().toLowerCase())) {
						index = item.toLowerCase().indexOf("-" + transition.toString().toLowerCase());
						break;
					}
				}
				if (index != -1)
					itemSetWithoutEventTypes.add(item.substring(0, index));
				else
					itemSetWithoutEventTypes.add(item);
			}
			if (itemSetWithoutEventTypes.size() == dispositions[i].length) {
				isValidDisposition[i] = true;
				noValidDispositions++;
			}
		}

		String[][] prunedDispositions = new String[noValidDispositions][2];
		int index = 0;
		for (int i = 0; i < noDispositions; i++) {
			if (isValidDisposition[i]) {
				prunedDispositions[index++] = dispositions[i];
			}
		}

		System.out.println("Before Diversity Item Set Size: " + dispositions.length);
		System.out.println("After Diversity Item Set Size: " + prunedDispositions.length);
		return prunedDispositions;
	}

	private static String[][] pruneForIntraGroupDispositions(String[][] dispositions,
			Set<Set<String>> conceptGroupingSet) {
		System.out.println("In IntraGroup Prune");
		int noDispositions = dispositions.length;
		boolean[] isValidDisposition = new boolean[noDispositions];

		for (int i = 0; i < noDispositions; i++) {
			isValidDisposition[i] = false;
		}

		int noValidDispositions = 0;
		StandardModel[] lifeCycleTransitions = XLifecycleExtension.StandardModel.values();

		Set<String> itemSetWithoutEventTypes = new HashSet<String>();
		for (int i = 0; i < noDispositions; i++) {
			itemSetWithoutEventTypes.clear();
			for (String item : dispositions[i]) {
				int index = -1;
				for (StandardModel transition : lifeCycleTransitions) {
					if (item.toLowerCase().contains("-" + transition.toString().toLowerCase())) {
						index = item.toLowerCase().indexOf("-" + transition.toString().toLowerCase());
						break;
					}
				}
				if (index != -1)
					itemSetWithoutEventTypes.add(item.substring(0, index));
				else
					itemSetWithoutEventTypes.add(item);
			}
			System.out.println(itemSetWithoutEventTypes + " @ " + conceptGroupingSet);
			for (Set<String> conceptGroup : conceptGroupingSet) {
				if (conceptGroup.containsAll(itemSetWithoutEventTypes)) {
					isValidDisposition[i] = true;
					noValidDispositions++;
					break;
				}
			}
		}

		String[][] prunedDispositions = new String[noValidDispositions][2];
		int index = 0;
		for (int i = 0; i < noDispositions; i++) {
			if (isValidDisposition[i]) {
				prunedDispositions[index++] = dispositions[i];
			}
		}

		System.out.println("No. Original Dispositions: " + dispositions.length);
		System.out.println("No. Pruned Dispositions: " + prunedDispositions.length);
		return prunedDispositions;
	}

	private static String[][] pruneForInterGroupDispositions(String[][] dispositions,
			Set<Set<String>> conceptGroupingSet) {
		System.out.println("In InterGroup Prune");
		int noDispositions = dispositions.length;
		boolean[] isValidDisposition = new boolean[noDispositions];

		for (int i = 0; i < noDispositions; i++) {
			isValidDisposition[i] = true;
		}

		Set<String> allChosenActivitiesInConceptSet = new HashSet<String>();
		for (Set<String> conceptGroup : conceptGroupingSet)
			allChosenActivitiesInConceptSet.addAll(conceptGroup);

		int noValidDispositions = noDispositions;
		StandardModel[] lifeCycleTransitions = XLifecycleExtension.StandardModel.values();

		Set<String> itemSetWithoutEventTypes = new HashSet<String>();
		for (int i = 0; i < noDispositions; i++) {
			itemSetWithoutEventTypes.clear();
			for (String item : dispositions[i]) {
				int index = -1;
				for (StandardModel transition : lifeCycleTransitions) {
					if (item.toLowerCase().contains("-" + transition.toString().toLowerCase())) {
						index = item.toLowerCase().indexOf("-" + transition.toString().toLowerCase());
						break;
					}
				}
				if (index != -1)
					itemSetWithoutEventTypes.add(item.substring(0, index));
				else
					itemSetWithoutEventTypes.add(item);
			}

			System.out.println(itemSetWithoutEventTypes + " @ " + isValidDisposition[i] + " @ "
					+ allChosenActivitiesInConceptSet + " @ " + conceptGroupingSet);
			if (!allChosenActivitiesInConceptSet.containsAll(itemSetWithoutEventTypes)) {
				isValidDisposition[i] = false;
				noValidDispositions--;
				continue;
			}
			for (Set<String> conceptGroup : conceptGroupingSet) {
				if (conceptGroup.containsAll(itemSetWithoutEventTypes)) {
					isValidDisposition[i] = false;
					noValidDispositions--;
					break;
				}
			}
			System.out.println(isValidDisposition[i]);
		}

		String[][] prunedDispositions = new String[noValidDispositions][2];
		int index = 0;
		for (int i = 0; i < noDispositions; i++) {
			if (isValidDisposition[i]) {
				prunedDispositions[index++] = dispositions[i];
			}
		}

		System.out.println("No. Original Dispositions: " + dispositions.length);
		System.out.println("No. Pruned Dispositions: " + prunedDispositions.length);
		return prunedDispositions;
	}

}

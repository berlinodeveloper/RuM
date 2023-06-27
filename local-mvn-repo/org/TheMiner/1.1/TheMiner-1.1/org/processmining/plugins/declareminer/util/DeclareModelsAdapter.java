package org.processmining.plugins.declareminer.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinitonCell;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DGraph;
import org.processmining.plugins.declareminer.visualizing.IItem;
import org.processmining.plugins.declareminer.visualizing.Language;
import org.processmining.plugins.declareminer.visualizing.LanguageGroup;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.TemplateBroker;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;

/**
 * 
 * @author Andrea Burattin
 * @author Fabrizio M. Maggi
 */
public class DeclareModelsAdapter {

	private static int modelsCounter = 0;
	private static DeclareModelsAdapter instance = new DeclareModelsAdapter();

	private HashMap<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = new HashMap<DeclareTemplate, ConstraintTemplate>();
	private HashMap<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();
	private Language lang;

	private DeclareModelsAdapter() {
		readConstraintTemplates();
	}

	/**
	 * 
	 * @return
	 */
	public static DeclareModelsAdapter instance() {
		return instance;
	}

	/**
	 * 
	 * @param sourceModel
	 * @return
	 */
	public DeclareMap convert(DeclareModel sourceModel, boolean showConstraintLabel) {
		AssignmentModel model = new AssignmentModel(lang);
		AssignmentModelView view = new AssignmentModelView(model);

		model.setName("model_id_" + (modelsCounter++));

		/* add activities */
		HashMap<String, Pair<Integer, ActivityDefinition>> acts = new HashMap<String, Pair<Integer, ActivityDefinition>>();
		int actsCounter = 1;
		for (String actName : sourceModel.getActivities()) {
			actsCounter++;
			ActivityDefinition ad = model.addActivityDefinition(actsCounter);
			ad.setName("<html>" + actName.replace("\n", "<br/><small style=\"color: #aaaaaa; font-weight: normal\">")
					+ "</small></html>");
			acts.put(actName, new Pair<Integer, ActivityDefinition>(actsCounter, ad));
		}

		/* add constraints */
		HashMap<DeclareTemplate, HashMap<Pair<String, String>, HashMap<String, Double>>> constraints = sourceModel
				.getConstraints();

		int constraintId = 0;

		for (DeclareTemplate t : constraints.keySet()) {
			for (Pair<String, String> constraintActs : constraints.get(t).keySet()) {
				constraintId++;
				ConstraintDefinition constraintdefinition = new ConstraintDefinition(constraintId, model,
						declareTemplateConstraintTemplateMap.get(t));

				Collection<Parameter> parameters = (declareTemplateConstraintTemplateMap.get(t)).getParameters();
				Iterator<Parameter> iter = parameters.iterator();
				if (parameters.size() == 1) {
					constraintdefinition.addBranch(iter.next(), acts.get(constraintActs.getFirst()).getSecond());
				} else if (parameters.size() == 2) {
					constraintdefinition.addBranch(iter.next(), acts.get(constraintActs.getFirst()).getSecond());
					constraintdefinition.addBranch(iter.next(), acts.get(constraintActs.getSecond()).getSecond());
				}

				model.addConstraintDefiniton(constraintdefinition);
				if (!showConstraintLabel) {
					constraintdefinition.setDisplay("");
				}

				Double cost = sourceModel.getCost(t, constraintActs.getFirst(), constraintActs.getSecond());
				view.setConstraintDefinitionColor(constraintdefinition,
						GUICustomUtils.fromWeightToColor(new Color(78, 107, 252), cost), Color.black,
						UIColors.lightLightGray);
			}
		}

		Font f = view.getGraph().getFont();
		FontMetrics fm = view.getGraph().getFontMetrics(f);
		for (String actName : sourceModel.getActivities()) {
			ActivityDefinition ad = acts.get(actName).getSecond();
			ActivityDefinitonCell adc = view.getActivityDefinitionCell(ad);
			adc.setSize(new Point2D.Double(fm.stringWidth(actName.substring(0, actName.indexOf("\n"))) + 30.0, 35.0));
		}
		view.updateUI();

		return new DeclareMap(model, null, view, null, null, null);
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	public DGraph show(DeclareMap declare) {
		DGraph graph = declare.getView().getGraph();
		graph.setAntiAliased(true);

		JGraphFastOrganicLayout oc = new JGraphFastOrganicLayout();

		oc.setForceConstant(65);
		oc.setInitialTemp(50);

		/*
		 * oc.setDeterministic(true);
		 * oc.setOptimizeBorderLine(true);
		 * oc.setOptimizeEdgeCrossing(true);
		 * oc.setOptimizeEdgeDistance(true);
		 * oc.setOptimizeEdgeLength(true);
		 * oc.setOptimizeNodeDistribution(true);
		 * oc.setEdgeCrossingCostFactor(999999999);
		 * oc.setEdgeDistanceCostFactor(999999999);
		 * oc.setFineTuning(true);
		 * 
		 * oc.setEdgeLengthCostFactor(9999);
		 * if(model.getConstraintDefinitions().size() < 200){
		 * oc.setEdgeLengthCostFactor(99);
		 * }
		 * oc.setNodeDistributionCostFactor(999999999);
		 * oc.setBorderLineCostFactor(999);
		 * oc.setRadiusScaleFactor(0.9);
		 */

		/* layout */
		/*
		 * JGraphOrganicLayout oc = new JGraphOrganicLayout();
		 * oc.setDeterministic(true);
		 * oc.setOptimizeBorderLine(true);
		 * oc.setOptimizeEdgeCrossing(true);
		 * oc.setOptimizeEdgeDistance(true);
		 * oc.setOptimizeEdgeLength(true);
		 * oc.setOptimizeNodeDistribution(true);
		 * oc.setEdgeCrossingCostFactor(999999999);
		 * oc.setEdgeDistanceCostFactor(999999999);
		 * oc.setFineTuning(true);
		 * 
		 * // oc.setMinDistanceLimit(0.001);
		 * oc.setEdgeLengthCostFactor(9999);
		 * if(model.getConstraintDefinitions().size() < 200){
		 * oc.setEdgeLengthCostFactor(99);
		 * }
		 * oc.setNodeDistributionCostFactor(999999999);
		 * oc.setBorderLineCostFactor(999);
		 * oc.setRadiusScaleFactor(0.9);
		 */
		JGraphFacade jgf = new JGraphFacade(graph);
		oc.run(jgf);
		@SuppressWarnings("rawtypes")
		Map nestedMap = jgf.createNestedMap(true, true);
		graph.getGraphLayoutCache().edit(nestedMap);

		return graph;
	}

	private void readConstraintTemplates() {
		DeclareTemplate declareTemplate = DeclareTemplate.Absence;
		DeclareTemplate[] declareTemplateNames = declareTemplate.getDeclaringClass().getEnumConstants();
		for (DeclareTemplate d : declareTemplateNames) {
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}

		InputStream templateInputStream = getClass().getResourceAsStream("/resources/template.xml");
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

		// the first language in the list is the Condec language, which is what we need
		lang = languagesList.get(0);
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

		declareTemplateConstraintTemplateMap = new HashMap<DeclareTemplate, ConstraintTemplate>();

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
	}

	private List<IItem> visit(IItem item) {
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
}

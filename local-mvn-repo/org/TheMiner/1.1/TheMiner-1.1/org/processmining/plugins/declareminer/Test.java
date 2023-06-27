package org.processmining.plugins.declareminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.Language;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.TemplateBroker;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filepath = "C:\\Users\\fmaggi\\Desktop\\testCoex.xml";
		String outputPath = "C:\\Users\\fmaggi\\Desktop\\outputtestCoex.xml";
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(filepath);
		AssignmentModel model = broker.readAssignment();
		AssignmentModelView view = new AssignmentModelView(model);
		broker.readAssignmentGraphical(model, view);

		Collection<ConstraintDefinition> allDiscoveredConstraints = model.getConstraintDefinitions();
		HashMap<Integer, List<String>> visibleConstraintParametersMap = new HashMap<Integer, List<String>>();
		HashMap<Integer, DeclareTemplate> constraintTemplateMap = new HashMap<Integer, DeclareTemplate>();
		Vector<ConstraintDefinition> constrList = new Vector<ConstraintDefinition>();
		for (ConstraintDefinition cd : allDiscoveredConstraints) {
			ArrayList<String> param = new ArrayList<String>();
			for (Parameter p : cd.getParameters()) {
				param.add(cd.getBranches(p).iterator().next().getName());
			}
			visibleConstraintParametersMap.put(cd.getId(), param);
			DeclareTemplate currentTemplate = DeclareMiner.getTemplate(cd);
			constraintTemplateMap.put(cd.getId(), currentTemplate);
			constrList.add(cd);
		}

		InputStream ir = Test.class.getResourceAsStream("/resources/template.xml");
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
		TemplateBroker template = XMLBrokerFactory.newTemplateBroker(language.getAbsolutePath());
		List<Language> languages = template.readLanguages();
		Language lang = languages.get(0);
		AssignmentModel outmodel = new AssignmentModel(lang);
		outmodel.setName("new model");

		ActivityDefinition newActivityDefinition = null;
		for (ActivityDefinition activityDefinition : model.getActivityDefinitions()) {
			newActivityDefinition = outmodel.addActivityDefinition(activityDefinition.getId());
			newActivityDefinition.setName(activityDefinition.getName());
		}

		DeclareModelGenerator dmg = new DeclareModelGenerator();

		dmg.setTransitiveClosureSuccessionConstraints(new Vector<Integer>());
		if (visibleConstraintParametersMap.values().contains(DeclareTemplate.Succession)) {
			dmg.getTransitiveClosureSuccessionConstraints(constrList, model.getActivityDefinitions(),
					visibleConstraintParametersMap, constraintTemplateMap);
		}

		dmg.setTransitiveClosureCoexistenceConstraints(new Vector<Integer>());
		if (constraintTemplateMap.values().contains(DeclareTemplate.CoExistence)) {
			dmg.getTransitiveClosureCoexistenceConstraints(constrList, model.getActivityDefinitions(),
					visibleConstraintParametersMap, constraintTemplateMap);
		}

		dmg.setTransitiveClosureResponseConstraints(new Vector<Integer>());
		if (constraintTemplateMap.values().contains(DeclareTemplate.Response)) {
			dmg.getTransitiveClosureResponseConstraints(constrList, model.getActivityDefinitions(),
					visibleConstraintParametersMap, constraintTemplateMap);
		}

		dmg.setTransitiveClosurePrecedenceConstraints(new Vector<Integer>());
		if (constraintTemplateMap.values().contains(DeclareTemplate.Precedence)) {
			dmg.getTransitiveClosurePrecedenceConstraints(constrList, model.getActivityDefinitions(),
					visibleConstraintParametersMap, constraintTemplateMap);
		}

		dmg.setTransitiveClosureRespondedExistenceConstraints(new Vector<Integer>());
		if (constraintTemplateMap.values().contains(DeclareTemplate.Responded_Existence)) {
			dmg.getTransitiveClosureRespondedExistenceConstraints(constrList, model.getActivityDefinitions(),
					visibleConstraintParametersMap, constraintTemplateMap);
		}

		dmg.setTransitiveClosureNotCoexistenceConstraints(new Vector<Integer>());
		if (constraintTemplateMap.values().contains(DeclareTemplate.Not_CoExistence)) {
			dmg.getTransitiveClosureNotCoexistenceConstraints(constrList, model.getActivityDefinitions(),
					visibleConstraintParametersMap, constraintTemplateMap);
		}

		Vector<String> adIds = new Vector<String>();

		for (ConstraintDefinition constraintDefinition : allDiscoveredConstraints) {
			if ((!dmg.getTransitiveClosureResponseConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureRespondedExistenceConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosurePrecedenceConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureSuccessionConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureCoexistenceConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureNotCoexistenceConstraints().contains(constraintDefinition.getId()))) {
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
			}
		}
		///////////////////////
		// for(ActivityDefinition ads : outmodel.getActivityDefinitions()){
		// String ad = ads.getName();
		// if(!adIds.contains(ad)){
		// outmodel.deleteActivityDefinition(outmodel.activityDefinitionWithName(ad));
		// }
		// }
		///////////////////////////

		for (ConstraintDefinition constraintDefinition : allDiscoveredConstraints) {
			if ((!dmg.getTransitiveClosureResponseConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureRespondedExistenceConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosurePrecedenceConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureSuccessionConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureCoexistenceConstraints().contains(constraintDefinition.getId()))
					&& (!dmg.getTransitiveClosureNotCoexistenceConstraints().contains(constraintDefinition.getId()))) {
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

				outmodel.addConstraintDefiniton(constraintDefinition);

			}
		}

		AssignmentModelView outview = new AssignmentModelView(outmodel);
		// Vector<String> adIds = new Vector<String>();

		DeclareMap outputMap = new DeclareMap(outmodel, null, outview, null, null, null);

		// Vector ads = new Vector();
		// ActivityDefinition activitydefinition = null;
		//
		// try {
		// int k = 0;
		// // for (int i = 0; i < models.length; i++) {
		// for (ActivityDefinition ad : outmodel.getActivityDefinitions()) {
		// if (!ads.contains(ad.getName())) {
		// activitydefinition = model.addActivityDefinition(k + 1);
		// activitydefinition.setName(ad.getName());
		// //boolean c = ads.add(firstPart);
		// //System.out.println(c);
		// k++;
		// }
		// }
		//
		//
		// int l = 1;
		// //for (int i = 0; i < models.length; i++) {
		// for (ConstraintDefinition cd : outmodel.getConstraintDefinitions()) {
		// //if(cd.isVisible()){
		// ConstraintTemplate ct = new ConstraintTemplate(l, cd);
		// ConstraintDefinition toAdd = new ConstraintDefinition(l, model, ct);
		// Collection<Parameter> parameters = ct.getParameters();
		//
		// for (Parameter parameter : parameters) {
		// for (ActivityDefinition branch : cd.getBranches(parameter)) {
		//
		// String name = branch.getName();
		// if( branch.getName().contains("<html>")){
		// name = branch.getName().replace("<html><body
		// text=404040>","").replace("</body></html>",
		// "").replace("<html>","").replace("</html>", "").replace("</center><center>",
		// "-").replace("<center>", "").replace("</center>", "");
		// String[] splitted = branch.getName().replace("<html><body
		// text=404040>","").replace("</body></html>",
		// "").replace("<html>","").replace("</html>", "").replace("</center><center>",
		// "-").replace("<center>", "").replace("</center>", "").split("-");
		//
		// String firstPart = "";
		// for (int s=0;s<splitted.length-1; s++){
		// if(s<splitted.length-2){
		// firstPart = firstPart+splitted[s]+"-";
		// }else{
		// firstPart = firstPart+splitted[s];
		// }
		// }
		//
		// branch.setName(name);
		// }
		// toAdd.addBranch(parameter, branch);
		//
		// }
		// }
		// boolean c = model.addConstraintDefiniton(toAdd);
		// if (!c) {
		// c = model.addConstraintDefiniton(new ConstraintDefinition(l,
		// outputMap.getModel(), cd));
		// }
		// l++;
		// //System.out.println(c);
		// }
		// //}
		// //}
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		broker = XMLBrokerFactory.newAssignmentBroker(outputPath);
		// broker.readAssignmentGraphical(model, models.getModel().getView());
		broker.addAssignmentAndView(outmodel, outview);

	}

}

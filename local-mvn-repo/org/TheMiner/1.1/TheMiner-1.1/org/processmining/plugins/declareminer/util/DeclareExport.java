package org.processmining.plugins.declareminer.util;

import java.io.File;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

@Plugin(name = "Export Declare Maps", parameterLabels = { "Declare Map", "File" }, returnLabels = {}, returnTypes = {}, userAccessible = true)
@UIExportPlugin(description = "Declare files", extension = "xml")
public class DeclareExport {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export Declare Models")
	public void export(UIPluginContext context, DeclareMinerOutput models, File file) {
	//	AssignmentModel model = new AssignmentModel(models.getModel().getModel().getLanguage());
////		AssignmentModelView view = new AssignmentModelView(Model);
//		model.setName("new model");
//		Vector ads = new Vector();
//		ActivityDefinition activitydefinition = null;
//
//		try {
//			int k = 0;
//			//	for (int i = 0; i < models.length; i++) {
//			for (ActivityDefinition ad : models.getModel().getModel().getActivityDefinitions()) {
//				if (!ads.contains(ad.getName())) {
//					activitydefinition = model.addActivityDefinition(k + 1);
//					activitydefinition.setName(ad.getName());
//					
//					view.addActivityDefinition(activitydefinition);
//					//boolean c = ads.add(firstPart);
//					//System.out.println(c);
//					k++;
//				}
//			}
//
//
//			int l = 1;
//			//for (int i = 0; i < models.length; i++) {
//			for (ConstraintDefinition cd : models.getModel().getModel().getConstraintDefinitions()) {
//				//if(cd.isVisible()){
//					ConstraintTemplate ct = new ConstraintTemplate(l, cd);
//					ConstraintDefinition toAdd = new ConstraintDefinition(l, model, ct);
//					Collection<Parameter> parameters = ct.getParameters();
//
//					for (Parameter parameter : parameters) {
//						for (ActivityDefinition branch : cd.getBranches(parameter)) {
//
//							String name = branch.getName();
//							if( branch.getName().contains("<html>")){
//								name = branch.getName().replace("<html><body text=404040>","").replace("</body></html>", "").replace("<html>","").replace("</html>", "").replace("</center><center>", "-").replace("<center>", "").replace("</center>", "");	
//								String[] splitted = branch.getName().replace("<html><body text=404040>","").replace("</body></html>", "").replace("<html>","").replace("</html>", "").replace("</center><center>", "-").replace("<center>", "").replace("</center>", "").split("-");
//
//								String firstPart = "";
//								for (int s=0;s<splitted.length-1; s++){
//									if(s<splitted.length-2){
//										firstPart = firstPart+splitted[s]+"-";
//									}else{
//										firstPart = firstPart+splitted[s];
//									}
//								}
//
//								branch.setName(name);
//							}
//							toAdd.addBranch(parameter, branch);
//
//						}
//					}
//					boolean c = model.addConstraintDefiniton(toAdd);
//					if (!c) {
//						c = model.addConstraintDefiniton(new ConstraintDefinition(l, models.getModel().getModel(), cd));
//					
//					}
//					l++;
//					//System.out.println(c);
//				}
//			//}
//			//}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(file.getAbsolutePath());
	//	broker.readAssignmentGraphical(model, models.getModel().getView());
	//	AssignmentModelView view = models.getModel().getView();
//	    DGraph graph = view.getGraph();
//		final JGraphOrganicLayout oc = new JGraphOrganicLayout();
//
//		oc.setDeterministic(true);
//		oc.setOptimizeBorderLine(true);
//		oc.setOptimizeEdgeCrossing(true);
//		oc.setOptimizeEdgeDistance(true);
//		oc.setOptimizeEdgeLength(true);
//		oc.setOptimizeNodeDistribution(true);
//		oc.setEdgeCrossingCostFactor(999999999);
//		oc.setEdgeDistanceCostFactor(999999999);
//		oc.setFineTuning(true);
//
//		//	oc.setMinDistanceLimit(0.001);
//		oc.setEdgeLengthCostFactor(9999);
//		if(model.getConstraintDefinitions().size()<200){
//			oc.setEdgeLengthCostFactor(99);
//		}
//		oc.setNodeDistributionCostFactor(999999999);
//		oc.setBorderLineCostFactor(999);
//		oc.setRadiusScaleFactor(0.9);
//		final JGraphFacade jgf = new JGraphFacade(view.getGraph());
//		oc.run(jgf);
//		final Map nestedMap = jgf.createNestedMap(true, true); 
//		graph.getGraphLayoutCache().edit(nestedMap); 
//		
//		view.a
		
		broker.addAssignmentAndView(models.getModel().getModel(), models.getModel().getView());
	//	broker.readAssignmentGraphical(model, view);
	}

}
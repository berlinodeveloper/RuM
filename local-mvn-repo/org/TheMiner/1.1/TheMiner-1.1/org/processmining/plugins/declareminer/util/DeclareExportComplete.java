package org.processmining.plugins.declareminer.util;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

@Plugin(name = "Export Declare Maps", parameterLabels = { "Declare Map", "File" }, returnLabels = {}, returnTypes = {}, userAccessible = true)
@UIExportPlugin(description = "Declare files complete", extension = "xml")
public class DeclareExportComplete {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F.M. Maggi", email = "F.M.Maggi@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export Declare Models")
	public void export(UIPluginContext context, DeclareMinerOutput models, File file) {
		AssignmentModel model = new AssignmentModel(models.getModel().getModel().getLanguage());
		model.setName("new model");
		Vector ads = new Vector();
		ActivityDefinition activitydefinition = null;
		
		try {
			int k = 0;
			//	for (int i = 0; i < models.length; i++) {
			for (ActivityDefinition ad : models.getModel().getModel().getActivityDefinitions()) {
				if(ad.getName().contains("<html>")){
				String[] splitted =  ad.getName().replace("<html><body text=404040>","").replace("</body></html>", "").replace("<html>","").replace("</html>", "").replace("</center><center>", "-").replace("<center>", "").replace("</center>", "").split("-");
				String name = ad.getName().replace("<html><body text=404040>","").replace("</body></html>", "").replace("<html>","").replace("</html>", "").replace("</center><center>", "-").replace("<center>", "").replace("</center>", "");
				
				String firstPart = "";
				for (int s=0;s<splitted.length-1; s++){
					if(s<splitted.length-2){
						firstPart = firstPart+splitted[s]+"-";
					}else{
						firstPart = firstPart+splitted[s];
					}
				}
				if (!ads.contains(name)) {
					activitydefinition = model.addActivityDefinition(k + 1);
					activitydefinition.setName(name);
					//boolean c = ads.add(firstPart);
					//System.out.println(c);
					k++;
				}
				}else{
					if (!ads.contains(ad.getName())) {
						activitydefinition = model.addActivityDefinition(k + 1);
						activitydefinition.setName(ad.getName());
						//boolean c = ads.add(firstPart);
						//System.out.println(c);
						k++;
					}
				}
			}


			int l = 1;
			//for (int i = 0; i < models.length; i++) {
			for (ConstraintDefinition cd : models.getModel().getModel().getConstraintDefinitions()) {
			
					ConstraintTemplate ct = new ConstraintTemplate(l, cd);
					ConstraintDefinition toAdd = new ConstraintDefinition(l, model, ct);
					Collection<Parameter> parameters = ct.getParameters();
					
					for (Parameter parameter : parameters) {
						for (ActivityDefinition branch : cd.getBranches(parameter)) {
							
							String name = branch.getName();
							if( branch.getName().contains("<html>")){
								name = branch.getName().replace("<html><body text=404040>","").replace("</body></html>", "").replace("<html>","").replace("</html>", "").replace("</center><center>", "-").replace("<center>", "").replace("</center>", "");	
								String[] splitted = branch.getName().replace("<html><body text=404040>","").replace("</body></html>", "").replace("<html>","").replace("</html>", "").replace("</center><center>", "-").replace("<center>", "").replace("</center>", "").split("-");
								
								String firstPart = "";
								for (int s=0;s<splitted.length-1; s++){
									if(s<splitted.length-2){
										firstPart = firstPart+splitted[s]+"-";
									}else{
										firstPart = firstPart+splitted[s];
									}
								}
								
								branch.setName(name);
							}
							toAdd.addBranch(parameter, branch);
							
						}
					}
					boolean c = model.addConstraintDefiniton(toAdd);
					if (!c) {
						c = model.addConstraintDefiniton(new ConstraintDefinition(l, models.getModel().getModel(), cd));
					}
					l++;
					//System.out.println(c);
			
			}
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
		AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(file.getAbsolutePath());
		//broker.readAssignmentGraphical(model, models.getModel().getView());
		broker.addAssignmentAndView(model, models.getModel().getView());
	
	}
	
}
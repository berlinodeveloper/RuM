package org.processmining.plugins.declareminer.ui;

import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DataDeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareProMInput;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.listener.DeclareMinerSettingsListener;
import org.processmining.plugins.declareminer.swingx.ErrorDialog;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;


public class DeclareMinerConfigurationUI implements DeclareMinerSettingsListener {
	UIPluginContext context;
	DeclareProMInput declareProMInput;
	
	int noSteps;
	int currentStep;
	private myStep[] mySteps;
	
	private int mapTemplateConfigurationStep;
	private int templateConfigurationStep;
	private int aprioriConfigurationStep;
	private int declarePerspectiveConfigurationStep;
	
	DeclareMinerInput input;
	
	public DeclareMinerConfigurationUI(UIPluginContext context, DeclareProMInput declareProMInput){
				return;
	}

	private int go(int direction) {
		currentStep += direction;
		if (currentStep >= 0 && currentStep < noSteps) {
			if (mySteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}
	
	private void readSettings(){
		for(int currentStep = 0; currentStep < noSteps; currentStep++){
			mySteps[currentStep].readSettings();
		}
	}

	private boolean validateSettings(){
		if(declareProMInput == DeclareProMInput.Log_Only && input.getSelectedDeclareTemplateSet().size()==0)
			return false;
		if(input.getDeclarePerspectiveSet().size()==0)
			return false;
		return true;
	}
	
	public void setSelectedDeclareTemplateSet(Set<DeclareTemplate> selectedDeclareTemplateSet) {
		System.out.println("Setting delcare template set");
		input.setSelectedDeclareTemplateSet(selectedDeclareTemplateSet);		
	}

	public void setMinSupport(int minSupport) {
		System.out.println("Setting min support");
		input.setMinSupport(minSupport);
	}

	public void setAlpha(int alpha) {
		System.out.println("Setting alpha");
		input.setAlpha(alpha);
	}

	public void setDeclarePerspectiveSet(Set<DeclarePerspective> declarePerspectiveSet) {
		System.out.println("Setting delcare perspective");
		input.setDeclarePerspectiveSet(declarePerspectiveSet);
	}

	public DeclareMinerInput getInput() {
		return input;
	}

	public void setDeclareTemplateConstraintTemplateMap(Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap) {
		input.setDeclareTemplateConstraintTemplateMap(declareTemplateConstraintTemplateMap);
	}

	public void setMapTemplateConfiguration(MapTemplateConfiguration mapTemplateConfiguration) {
		input.setMapTemplateConfiguration(mapTemplateConfiguration);
	}

	public void setAprioriKnowledgeBasedCriteria(Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteriaSet) {
		input.setAprioriKnowledgeBasedCriteriaSet(aprioriKnowledgeBasedCriteriaSet);
	}

	public void setAprioriKnowledgeConceptFileName(String aprioriKnowledgeConceptFileName) {
		input.setAprioriKnowledgeConceptFileName(aprioriKnowledgeConceptFileName);
	}

	public void setDataDeclareTemplateConstraintTemplateMap(
			Map<DataDeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap) {
		// TODO Auto-generated method stub
		
	}

	public void setSelectedDataDeclareTemplateSet(Set<DataDeclareTemplate> selectedDeclareTemplateSet) {
		// TODO Auto-generated method stub
		
	}
}

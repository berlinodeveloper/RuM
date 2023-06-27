package org.processmining.plugins.declareminer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DataDeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;

public class DeclareMinerInput {
	Set<DeclareTemplate> selectedDeclareTemplateSet; //!
	// Set of templates I want to discover
	Set<DataDeclareTemplate> selectedDataDeclareTemplateSet; //-
	Set<DeclarePerspective> declarePerspectiveSet;//-
	MapTemplateConfiguration mapTemplateConfiguration;//?
	int minSupport; //!
	int alpha; //!
	Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap; //!
	Map<DataDeclareTemplate, ConstraintTemplate> declareDataTemplateConstraintTemplateMap; //-
	Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteriaSet;//-
	String aprioriKnowledgeConceptFileName;//-
	
	String outputDir; //-
	String outputFileName; //-
	boolean isEmpty;
	boolean detectActivitiesAutomatically;
	boolean detectTemplatesAutomatically;
	boolean alterExistingConstraints;
	boolean detectSupportAutomatically;
	boolean eventTypesMismatchLogModel;
	String referenceEventType;
	
	//NEW PARAMETERS
	boolean verbose;
	String loggingPreprocessingFile;
	String loggingAprioriFile;
	boolean memoryCheck;
	int threadNumber; 

	
	
	public DeclareMinerInput(){
		isEmpty = false;
	}
	

	
	public Set<DeclareTemplate> getSelectedDeclareTemplateSet() {
		return selectedDeclareTemplateSet;
	}



	public void setSelectedDataDeclareTemplateSet(Set<DataDeclareTemplate> selectedDataDeclareTemplateSet) {
		this.selectedDataDeclareTemplateSet = selectedDataDeclareTemplateSet;
	}



	public void setSelectedDeclareTemplateSet(Set<DeclareTemplate> selectedDeclareTemplateSet) {
		this.selectedDeclareTemplateSet = selectedDeclareTemplateSet;
	}



	public Map<DeclareTemplate, ConstraintTemplate> getDeclareTemplateConstraintTemplateMap() {
		return declareTemplateConstraintTemplateMap;
	}



	public void setDeclareTemplateConstraintTemplateMap(
			Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap) {
		this.declareTemplateConstraintTemplateMap = declareTemplateConstraintTemplateMap;
	}




	public Set<DataDeclareTemplate> getSelectedDataDeclareTemplateSet() {
		return selectedDataDeclareTemplateSet;
	}

	
	public void setMinSupport(int minSupport){
		this.minSupport = minSupport;
	}
	
	public void setAlpha(int alpha){
		this.alpha = alpha;
	}
	
	public void setDeclarePerspectiveSet(Set<DeclarePerspective> declarePerspectiveSet){
		this.declarePerspectiveSet = declarePerspectiveSet;
	}
	
	public void setDeclareDataTemplateConstraintTemplateMap(
			Map<DataDeclareTemplate, ConstraintTemplate> declareDataTemplateConstraintTemplateMap) {
		this.declareDataTemplateConstraintTemplateMap = declareDataTemplateConstraintTemplateMap;
		
	}

	public Set<DeclarePerspective> getDeclarePerspectiveSet() {
		return declarePerspectiveSet;
	}

	public int getMinSupport() {
		return minSupport;
	}

	public int getAlpha() {
		return alpha;
	}

	public Map<DataDeclareTemplate, ConstraintTemplate> getDeclareDataTemplateConstraintTemplateMap() {
		return declareDataTemplateConstraintTemplateMap;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}


	public boolean isEmpty() {
		return isEmpty;
	}


	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}


	public boolean isDetectActivitiesAutomatically() {
		return detectActivitiesAutomatically;
	}


	public void setDetectActivitiesAutomatically(boolean detectActivitiesAutomatically) {
		this.detectActivitiesAutomatically = detectActivitiesAutomatically;
	}


	public boolean isDetectTemplatesAutomatically() {
		return detectTemplatesAutomatically;
	}


	public void setDetectTemplatesAutomatically(boolean detectTemplatesAutomatically) {
		this.detectTemplatesAutomatically = detectTemplatesAutomatically;
	}


	public boolean isAlterExistingConstraints() {
		return alterExistingConstraints;
	}


	public void setAlterExistingConstraints(boolean alterExistingConstraints) {
		this.alterExistingConstraints = alterExistingConstraints;
	}


	public boolean isDetectSupportAutomatically() {
		return detectSupportAutomatically;
	}


	public void setDetectSupportAutomatically(boolean detectSupportAutomatically) {
		this.detectSupportAutomatically = detectSupportAutomatically;
	}
	
	public void setMapTemplateConfiguration(MapTemplateConfiguration mapTemplateConfiguration){
		this.mapTemplateConfiguration = mapTemplateConfiguration;
	}


	public Set<AprioriKnowledgeBasedCriteria> getAprioriKnowledgeBasedCriteriaSet() {
		return aprioriKnowledgeBasedCriteriaSet;
	}


	public void setAprioriKnowledgeBasedCriteriaSet(Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteriaSet) {
		this.aprioriKnowledgeBasedCriteriaSet = aprioriKnowledgeBasedCriteriaSet;
	}


	public String getAprioriKnowledgeConceptFileName() {
		return aprioriKnowledgeConceptFileName;
	}


	public void setAprioriKnowledgeConceptFileName(String aprioriKnowledgeConceptFileName) {
		this.aprioriKnowledgeConceptFileName = aprioriKnowledgeConceptFileName;
	}


	public MapTemplateConfiguration getMapTemplateConfiguration() {
		return mapTemplateConfiguration;
	}


	public boolean isEventTypesMismatchLogModel() {
		return eventTypesMismatchLogModel;
	}


	public void setEventTypesMismatchLogModel(boolean eventTypesMismatchLogModel) {
		this.eventTypesMismatchLogModel = eventTypesMismatchLogModel;
	}



	public String getReferenceEventType() {
		return referenceEventType;
	}



	public void setReferenceEventType(String referenceEventType) {
		this.referenceEventType = referenceEventType;
	}
	
	
	
	public void forceDataDeclareTemplateConstraintTemplateMap(Map<DeclareTemplate, ConstraintTemplate> declareConstraintTemplateMap) {
		declareDataTemplateConstraintTemplateMap = new HashMap<DataDeclareTemplate, ConstraintTemplate>();
		for(DeclareTemplate tem : declareConstraintTemplateMap.keySet()){
			if(tem.equals(DeclareTemplate.Alternate_Precedence))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Alternate_Precedence,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Alternate_Response))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Alternate_Response,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Chain_Precedence)) 
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Chain_Precedence,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Chain_Response))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Chain_Response,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Not_Chain_Succession))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Not_Ch_Response,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Not_CoExistence))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Not_Responded_Existence,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Not_Succession))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Not_Response,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Precedence))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Precedence,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Response)) 
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Response,declareConstraintTemplateMap.get(tem));
			if(tem.equals(DeclareTemplate.Responded_Existence))
				declareDataTemplateConstraintTemplateMap.put(DataDeclareTemplate.Responded_Existence,declareConstraintTemplateMap.get(tem));
		}
		
		
	}


	// NEW PARAMETERS
	public boolean isVerbose() {
		return verbose;
	}



	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public String getLoggingPreprocessingFile() {
		return loggingPreprocessingFile;
	}

	public void setLoggingPreprocessingFile(String loggingPreprocessingFile) {
		this.loggingPreprocessingFile = loggingPreprocessingFile;
	}

	public String getLoggingAprioriFile() {
		return loggingAprioriFile;
	}



	public void setLoggingAprioriFile(String loggingAprioriFile) {
		this.loggingAprioriFile = loggingAprioriFile;
	}

	public boolean isMemoryCheck() {
		return memoryCheck;
	}



	public void setMemoryCheck(boolean memoryCheck) {
		this.memoryCheck = memoryCheck;
	}



	public int getThreadNumber() {
		return threadNumber;
	}



	public void setThreadNumber(int threadNumber) {
		this.threadNumber = threadNumber;
	}
	
	
	
}

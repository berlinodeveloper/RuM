package org.processmining.plugins.declareminer.listener;

import java.util.Map;
import java.util.Set;

import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DataDeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;

public interface DeclareMinerSettingsListener {
	public void setSelectedDeclareTemplateSet(Set<DeclareTemplate> selectedDeclareTemplateSet);
	public void setSelectedDataDeclareTemplateSet(Set<DataDeclareTemplate> selectedDeclareTemplateSet);
	public void setMinSupport(int minSupport);
	public void setAlpha(int alpha);
	public void setMapTemplateConfiguration(MapTemplateConfiguration mapTemplateConfiguration);
	public void setAprioriKnowledgeBasedCriteria(Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteriaSet);
	public void setAprioriKnowledgeConceptFileName(String aprioriKnowledgeConceptFileName);
	public void setDeclarePerspectiveSet(Set<DeclarePerspective> declarePerspectiveSet);
	public void setDeclareTemplateConstraintTemplateMap(Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap);
	public void setDataDeclareTemplateConstraintTemplateMap(Map<DataDeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap);
}

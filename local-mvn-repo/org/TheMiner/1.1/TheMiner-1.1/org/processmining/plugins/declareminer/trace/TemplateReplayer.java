package org.processmining.plugins.declareminer.trace;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.util.DeclareModel;

public interface TemplateReplayer {

	/**
	 * Add a new event observation
	 * 
	 * @param caseId
	 */
//	public void addObservation(String caseId);
	
	/**
	 * 
	 * @param event
	 * @param caseId
	 * @param isTraceStart
	 * @param isTraceComplete
	 */
//	public boolean isToReplay(String event, String caseId, boolean isTraceStart, boolean isTraceComplete);
	
	/**
	 * Process the given event belonging to the given case id
	 * @param event
	 * @param caseId
	 */
	public void process(String event, boolean isANewTrace, boolean isLastEvent, boolean isEmpty);
	
	
	/**
	 * Update the given model with the new constraints
	 * 
	 * @param d
	 */
	public void updateModel(DeclareModel d, int completeTraces);

	/**
	 * 
	 * @return
	 */
//	public Integer getSize();
}

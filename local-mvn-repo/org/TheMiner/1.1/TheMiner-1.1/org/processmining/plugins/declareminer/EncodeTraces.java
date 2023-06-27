package org.processmining.plugins.declareminer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */
public class EncodeTraces {
	private List<String> charStreamList;
	
	public EncodeTraces(Map<String, String> activityCharMap, XLog log, DeclareMinerInput input) throws EncodingNotFoundException{
		charStreamList = new ArrayList<String>();

		StringBuilder charStreamBuilder = new StringBuilder();
		StringBuilder activityBuilder = new StringBuilder();
	
		XAttributeMap attributeMap;

		/*
		 * First sort the traces based on their ids; this is required only for CPN simulated logs as the process instance ids are not 1 2 3 ..but 1 10, 101 102 etc
		 */
		
		List<String> traceIdList = new ArrayList<String>();
		List<Integer> sortedTraceIdIndices = new ArrayList<Integer>();
		XAttributeMap traceAttributeMap;
		int index = 0;
		
//		for(XTrace trace : log){
//			traceAttributeMap = trace.getAttributes();
//			traceIdList.add(traceAttributeMap.get("concept:name").toString());
//		}
//
//		for(int i = 1; i <= 750; i++){
//			sortedTraceIdIndices.add(traceIdList.indexOf("Model1_Copier"+i));
//		}
//		
//		for(int i = 1; i <= 300; i++){
//			sortedTraceIdIndices.add(traceIdList.indexOf("Model2_Copier"+i));
//		}
//		
//		System.out.println("Trace Ids: ");
//		int noTraces = traceIdList.size();
//		for(int i = 0; i < noTraces; i++)
//			System.out.println(traceIdList.get(i)+" @ "+sortedTraceIdIndices.get(i));
		
//		for(int i = 0; i < noTraces; i++){
//			XTrace trace = log.get(sortedTraceIdIndices.get(i));
		
		boolean isConsiderEventTypes = input.getAprioriKnowledgeBasedCriteriaSet().contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
		for(XTrace trace : log){
			charStreamBuilder.setLength(0);
			for(XEvent event : trace){
				attributeMap = event.getAttributes();
				activityBuilder.setLength(0);
//				String lifecycle = null;
//				if(event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION)!=null){
//					lifecycle = XLifecycleExtension.instance().extractTransition(event);
//				}else{
//					lifecycle = "complete";
//				}
//				
//				activityBuilder.append(XConceptExtension.instance().extractName(event)+"-"+lifecycle);
//				activityBuilder.append(XConceptExtension.instance().extractName(event));
				
				if(isConsiderEventTypes){
					if(attributeMap.get(XLifecycleExtension.KEY_TRANSITION)!=null){
						activityBuilder.append(XConceptExtension.instance().extractName(event)+"-"+attributeMap.get(XLifecycleExtension.KEY_TRANSITION));
					}else{
						activityBuilder.append(XConceptExtension.instance().extractName(event));
					}
				}else{
					activityBuilder.append(XConceptExtension.instance().extractName(event));
				}
				
				if(activityCharMap.containsKey(activityBuilder.toString())){
					charStreamBuilder.append(activityCharMap.get(activityBuilder.toString()));
				}else{
					throw new EncodingNotFoundException(activityBuilder.toString());
				}
			}
			charStreamList.add(charStreamBuilder.toString());
		}
	}

	public List<String> getCharStreamList() {
		return charStreamList;
	}
}

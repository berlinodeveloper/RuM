package org.processmining.plugins.declareminer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;

public class VectorBasedConstraints {
	Map<String, String> activityCharMap;
	Map<String, Integer> charVectorIndexMap;
	Map<String, String> charActivityMap;
	List<String> encodedTraceList;
	int encodingLength;
	int[][] parikhVectors;
	DeclareMinerInput input;
	
	public VectorBasedConstraints(XLog log,DeclareMinerInput input){
		this.input = input;
		encodeLog(log);
		transformToVectorSpace();
	}
	
	private void transformToVectorSpace(){
		charVectorIndexMap = new HashMap<String, Integer>();
		int noActivities = 0;
		
		for(String encodedActivity : charActivityMap.keySet()){
			charVectorIndexMap.put(encodedActivity, noActivities++);
		}
		
		int noTraces = encodedTraceList.size();
		parikhVectors = new int[noTraces][noActivities];
		for(int i = 0; i < noTraces; i++)
			for(int j = 0; j < noActivities; j++)
				parikhVectors[i][j] = 0;
		
		Set<String> encodedActivitySet = charActivityMap.keySet();
		for(int i = 0; i < noTraces; i++){
			UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(encodingLength, encodedTraceList.get(i));
			for(String encodedActivity : encodedActivitySet){
				int vectorIndex = charVectorIndexMap.get(encodedActivity);
				parikhVectors[i][vectorIndex] = suffixTree.noMatches(encodedActivity);
			}
		}
	}
	
	private void encodeLog(XLog log){
		boolean isConsiderEventTypes = input.getAprioriKnowledgeBasedCriteriaSet().contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
		Set<String> activitySet = new HashSet<String>();
		String activity;
		for(XTrace trace : log){
			for(XEvent event : trace){
				XAttributeMap eventAttributeMap = event.getAttributes();
				if(isConsiderEventTypes){
					if(eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION)!=null){
						activity = XConceptExtension.instance().extractName(event)+"-"+eventAttributeMap.get(XLifecycleExtension.KEY_TRANSITION);
					}else{
						activity = XConceptExtension.instance().extractName(event);
					}
				}else{
					activity = XConceptExtension.instance().extractName(event);
				}
				activitySet.add(activity);
			}
		}

		try{
			EncodeActivitySet e = new EncodeActivitySet(activitySet);
			charActivityMap = e.getCharActivityMap();
			activityCharMap = e.getActivityCharMap();
			encodingLength = e.getEncodingLength();
			
			EncodeTraces et = new EncodeTraces(activityCharMap, log,input);
			encodedTraceList = et.getCharStreamList();
			System.out.println(encodedTraceList.size());
			
		}catch(ActivityOverFlowException e){
			e.printStackTrace();
		}catch(EncodingNotFoundException e){
			e.printStackTrace();
		}
	}
	
	public Set<Integer> getAtleastExistenceActivatedAndSatisfiedTraces(String activityName, int noOccurrences){
		if(!activityCharMap.containsKey(activityName))
			return null;
		int vectorIndex = charVectorIndexMap.get(activityCharMap.get(activityName));
		Set<Integer> activatedAndSatisfiedSet = new HashSet<Integer>();
		int noTraces = parikhVectors.length;
		
		for(int i = 0; i < noTraces; i++)
			if(parikhVectors[i][vectorIndex] >= noOccurrences)
				activatedAndSatisfiedSet.add(i);
		return activatedAndSatisfiedSet;
	}
	
	public Set<Integer> getAtmostExistenceActivatedAndSatisfiedTraces(String activityName, int noOccurrences){
		if(!activityCharMap.containsKey(activityName))
			return null;
		int vectorIndex = charVectorIndexMap.get(activityCharMap.get(activityName));
		Set<Integer> activatedAndSatisfiedSet = new HashSet<Integer>();
		int noTraces = parikhVectors.length;
		
		for(int i = 0; i < noTraces; i++)
			if(parikhVectors[i][vectorIndex] >= 0 && parikhVectors[i][vectorIndex] <= noOccurrences)
				activatedAndSatisfiedSet.add(i);
		return activatedAndSatisfiedSet;
	}
	
	public Set<Integer> getExactlyActivatedAndSatisfiedTraces(String activityName, int noOccurrences){
		if(!activityCharMap.containsKey(activityName))
			return null;
		int vectorIndex = charVectorIndexMap.get(activityCharMap.get(activityName));
		Set<Integer> activatedAndSatisfiedSet = new HashSet<Integer>();
		int noTraces = parikhVectors.length;
		
		for(int i = 0; i < noTraces; i++)
			if(parikhVectors[i][vectorIndex] == noOccurrences)
				activatedAndSatisfiedSet.add(i);
		return activatedAndSatisfiedSet;
	}
}

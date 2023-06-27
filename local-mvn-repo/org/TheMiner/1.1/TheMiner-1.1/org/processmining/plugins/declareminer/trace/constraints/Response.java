package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class Response implements TemplateReplayer {
	//non zerve	
		private HashSet<String> activityLabelsResponse = new HashSet<String>();
	HashMap<String, HashMap<String, Integer>> pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
	//	
	//	private Counting<HashMap<String, Integer>> activityLabelsCounterResponse = new Counting<HashMap<String, Integer>>();
	//	private Counting<HashMap<String, HashMap<String, Integer>>> pendingConstraintsPerTrace = new Counting<HashMap<String, HashMap<String, Integer>>>();
	//	private Counting<Boolean> finishedTraces = new Counting<Boolean>();

	HashMap<String, HashMap<String, Integer>> satisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;

	public Response (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Response)){
			String param1 = params.get(0);
			String param2 = params.get(1);
			HashMap<String, Integer> pend = pendingForThisTrace.get(param1);
			if(pend==null){
			 pend = new HashMap<String, Integer>();
			}
			pend.put(param2, 0);
			pendingForThisTrace.put(param1, pend);
			
			HashMap<String, Integer> sat = satisfiedTraces.get(param1);
			if(sat==null){
			 sat = new HashMap<String, Integer>();
			}
			sat.put(param2, 0);
			satisfiedTraces.put(param1, sat);
			
			

			HashMap<String, Integer> viol = violatedTraces.get(param1);
			if(viol==null){
			 viol = new HashMap<String, Integer>();
			}
			viol.put(param2, 0);
			violatedTraces.put(param1, viol);
			
		}
	}


	@Override
	public void process(String event, boolean isANewTrace, boolean isLastEvent, boolean isEmpty) {
		if(isANewTrace){
			activityLabelsResponse = new HashSet<String>();
			for(String param1 : pendingForThisTrace.keySet()){
				for(String param2 : pendingForThisTrace.get(param1).keySet()){
					HashMap<String, Integer> pend = pendingForThisTrace.get(param1);
					if(pend==null){
					 pend = new HashMap<String, Integer>();
					}
					pend.put(param2, 0);
					pendingForThisTrace.put(param1, pend);
				}
			}
		}
				activityLabelsResponse.add(event);
		//		HashMap<String, Integer> counter = new HashMap<String, Integer>();
		//		if (!activityLabelsCounterResponse.containsKey(caseId)) {
		//			activityLabelsCounterResponse.putItem(caseId, counter);
		//		} else {
		//			counter = activityLabelsCounterResponse.getItem(caseId);
		//		}

		//		HashMap<String, HashMap<String, Integer>> pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
		//		if (!pendingConstraintsPerTrace.containsKey(caseId)) {
		//			pendingConstraintsPerTrace.putItem(caseId, pendingForThisTrace);
		//		} else {
		//			pendingForThisTrace = pendingConstraintsPerTrace.getItem(caseId);
		//		}
		//		
		//	if (!counter.containsKey(event)) {
		//			if (activityLabelsResponse.size() > 1) {
		//				for (String existingEvent : activityLabelsResponse) {
		//					if (!existingEvent.equals(event)) {
		//						HashMap<String, Integer> secondElement = new HashMap<String, Integer>();
		//						if (pendingForThisTrace.containsKey(existingEvent)) {
		//							secondElement = pendingForThisTrace.get(existingEvent);
		//						}
		//						secondElement.put(event, 0);
		//						pendingForThisTrace.put(existingEvent, secondElement);
		//					}
		//				}
		//				for (String existingEvent : activityLabelsResponse) {
		//					if (!existingEvent.equals(event)) {
		//						HashMap<String, Integer> secondElement = new HashMap<String, Integer>();
		//						if (pendingForThisTrace.containsKey(event)) {
		//							secondElement = pendingForThisTrace.get(event);
		//						}
		//						secondElement.put(existingEvent, 1);
		//						pendingForThisTrace.put(event, secondElement);
		//					}
		//				}
		//				pendingConstraintsPerTrace.putItem(caseId, pendingForThisTrace);
		////				pendingConstraintsPerTrace.put(caseId, pendingForThisTrace);
		//			}
		//		} else {
		//			if (activityLabelsResponse.size() > 1) {
		for (String firstElement : pendingForThisTrace.keySet()) {
			if (pendingForThisTrace.get(firstElement).containsKey(event)) {
				HashMap<String, Integer> secondElement = pendingForThisTrace.get(firstElement);
				secondElement.put(event, 0);
				pendingForThisTrace.put(firstElement, secondElement);
				//	pendingConstraintsPerTrace.putItem(caseId, pendingForThisTrace);
				//					pendingConstraintsPerTrace.put(caseId, pendingForThisTrace);
			}
		}
		if(pendingForThisTrace.containsKey(event)){
			HashMap<String, Integer> secondElement = pendingForThisTrace.get(event);
			for (String second : secondElement.keySet()) {
				if (!second.equals(event)) {
					Integer pendingNo = secondElement.get(second);
					pendingNo++;
					secondElement.put(second, pendingNo);
				}
			}
			pendingForThisTrace.put(event, secondElement);
		}
		//pendingConstraintsPerTrace.putItem(caseId, pendingForThisTrace);
		//			pendingConstraintsPerTrace.put(caseId, pendingForThisTrace);
		//}
		// activityLabelsCounter.put(trace, counter);
		//}

		// update the counter for the current trace and the current event
		// **********************

		//int numberOfEvents = 1;
		//if (!counter.containsKey(event)) {
		//	counter.put(event, numberOfEvents);
		//} else {
		//	numberOfEvents = counter.get(event);
		//	numberOfEvents++;
		//	counter.put(event, numberOfEvents);
		//}
		//activityLabelsCounterResponse.putItem(caseId, counter);
		// ***********************

		if(isLastEvent){
			for(String param1 : pendingForThisTrace.keySet()) {
				for(String param2 : pendingForThisTrace.get(param1).keySet()) {
					if(!param1.equals(param2)){

						// let's generate responses


						//	for(String caseId : activityLabelsCounterResponse.keySet()) {
						//		if (finishedTraces.containsKey(caseId) && finishedTraces.getItem(caseId) == true) {
						//			HashMap<String, Integer> counter = activityLabelsCounterResponse.getItem(caseId);
						//			HashMap<String, HashMap<String, Integer>> pendingForThisTrace = pendingConstraintsPerTrace.getItem(caseId);
						//			if (pendingForThisTrace == null) {
						//				pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
						//			}
						if(activityLabelsResponse.contains(param1)){
						//				if(pendingForThisTrace.containsKey(param1)){
						//				if(pendingForThisTrace.get(param1).containsKey(param2)){
						if(pendingForThisTrace.get(param1).get(param2) == 0) {
							int satisfied = satisfiedTraces.get(param1).get(param2);
							satisfied ++;
							HashMap<String, Integer> sat = satisfiedTraces.get(param1);
							if(sat==null){
							 sat = new HashMap<String, Integer>();
							}
							sat.put(param2, satisfied);
							satisfiedTraces.put(param1, sat);
						} else if (pendingForThisTrace.get(param1).get(param2) > 0) {
							int violated = violatedTraces.get(param1).get(param2);
							violated ++;
							HashMap<String, Integer> viol = violatedTraces.get(param1);
							if(viol==null){
							 viol = new HashMap<String, Integer>();
							}
							viol.put(param2, violated);
							violatedTraces.put(param1, viol);
						}
						}
						//	}
						//	}
						//	}

					}
				}
			}
		}

	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {
		//	int completedTraces = log.size(); 
		//	for(String caseId : activityLabelsCounterResponse.keySet()) {
		//	if (finishedTraces.containsKey(caseId) && finishedTraces.getItem(caseId) == true) {
		//		completedTraces++;
		//	}
		//}

		for(String param1 : pendingForThisTrace.keySet()) {
			for(String param2 : pendingForThisTrace.get(param1).keySet()) {
				if(!param1.equals(param2)){


					int vacuouslySatisfied = completedTraces - satisfiedTraces.get(param1).get(param2) - violatedTraces.get(param1).get(param2);
				//	HashMap<String, Integer> map = new HashMap<String, Integer>();
				//	map.put(param2, vacuouslySatisfied);
				//	vacuouslySatisfiedTraces.put(param1, map);
					d.addResponse(param1, param2, completedTraces, satisfiedTraces.get(param1).get(param2), vacuouslySatisfied, violatedTraces.get(param1).get(param2));
					// d.addNotResponse(param1, param2, completedTraces, violatedTraces, vacuouslySatisfiedTraces, satisfiedTraces);
				}
			}
		}
	}

	//	@Override
	//	public Integer getSize() {
	//		return activityLabelsResponse.size() +
	//				activityLabelsCounterResponse.getSize() +
	//				pendingConstraintsPerTrace.getSize() +
	//				finishedTraces.getSize();
	//	}
}

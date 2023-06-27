package org.processmining.plugins.declareminer.trace.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class Precedence implements TemplateReplayer {

	private HashMap<String,Integer> pastEvents = null;
	//private Counting<HashMap<String, Integer>> activityLabelsCounterRespondedExistence = new Counting<HashMap<String, Integer>>();
	//	private Counting<HashMap<String, HashMap<String, Integer>>> pendingConstraintsPerTraceRe = new Counting<HashMap<String, HashMap<String, Integer>>>();
	//	private Counting<Boolean> finishedTraces = new Counting<Boolean>();

	HashMap<String, HashMap<String, Integer>> fulfilledForThisTrace = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> satisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;
	public Precedence (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Precedence)){
			String param1 = params.get(1);
			String param2 = params.get(0);
			HashMap<String, Integer> ful = fulfilledForThisTrace.get(param1);
			if(ful==null){
			 ful = new HashMap<String, Integer>();
			}
			ful.put(param2, 0);
			fulfilledForThisTrace.put(param1, ful);
			
			
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
		pastEvents = new HashMap<String,Integer>();
	}

	@Override
	public void process(String event, boolean isANewTrace, boolean isLastEvent, boolean isEmpty) {

		if(isANewTrace){
			for(String param1 : fulfilledForThisTrace.keySet()){
				for(String param2 : fulfilledForThisTrace.get(param1).keySet()){
					//String param1 = params.get(0);
					//String param2 = params.get(1);
					HashMap<String, Integer> ful = fulfilledForThisTrace.get(param1);
					if(ful==null){
					 ful = new HashMap<String, Integer>();
					}
					ful.put(param2, 0);
					fulfilledForThisTrace.put(param1, ful);
				}
			}
			pastEvents = new HashMap<String,Integer>();
		}



		//	if (activityLabelsPrecedence.size() > 1) {
		for (String targetEvent : fulfilledForThisTrace.keySet()) {
			if (fulfilledForThisTrace.get(targetEvent).containsKey(event)) {
				HashMap<String, Integer> secondElement = null;
				int fulfillments = 0;
				//	if (fulfilledForThisTrace.containsKey(activatingEvent)) {
				secondElement = fulfilledForThisTrace.get(targetEvent);
				//	}
				if (secondElement.containsKey(event)) {
					fulfillments = secondElement.get(event);
				}
				if (pastEvents.containsKey(targetEvent)) {
					secondElement.put(event, fulfillments + 1);
					fulfilledForThisTrace.put(targetEvent, secondElement);
				}
			}
		}
		// for(String existingEvent : activityLabels){
		// if(!existingEvent.equals(event)){
		// HashMap<String, Integer> secondElement = new HashMap<String,
		// Integer>();
		// if(fulfilledForThisTrace.containsKey(event)){
		// secondElement = fulfilledForThisTrace.get(event);
		// }
		// secondElement.put(existingEvent, 1);
		// fulfilledForThisTrace.put(event,secondElement);
		// }
		// }
		//	fulfilledConstraintsPerTrace.putItem(caseId, fulfilledForThisTrace);
		//			fulfilledConstraintsPerTrace.put(caseId, fulfilledForThisTrace);
		//	}

		// }else{
		//
		// for(String firstElement : fulfilledForThisTrace.keySet()){
		// if(!firstElement.equals(event)){
		// HashMap<String, Integer> secondElement =
		// fulfilledForThisTrace.get(firstElement);
		// secondElement.put(event, 0);
		// fulfilledForThisTrace.put(firstElement, secondElement);
		// pendingConstraintsPerTrace.put(trace, fulfilledForThisTrace);
		// }
		// }
		// HashMap<String, Integer> secondElement =
		// fulfilledForThisTrace.get(event);
		// for(String second : secondElement.keySet()){
		// if(!second.equals(event)){
		// Integer pendingNo = secondElement.get(second);
		// pendingNo ++;
		// secondElement.put(second, pendingNo);
		// }
		// }
		// fulfilledForThisTrace.put(event,secondElement);
		// pendingConstraintsPerTrace.put(trace, fulfilledForThisTrace);
		//
		// //activityLabelsCounter.put(trace, counter);
		// }

		// update the counter for the current trace and the current event
		// **********************

		int numberOfEvents = 1;
		if(!pastEvents.containsKey(event)){
			pastEvents.put(event, numberOfEvents);
		}else{
			numberOfEvents = pastEvents.get(event);
			numberOfEvents++;
			pastEvents.put(event, numberOfEvents); 
		}
		
		
		if(isLastEvent){
			for(String param1 : fulfilledForThisTrace.keySet()) {
				for(String param2 : fulfilledForThisTrace.get(param1).keySet()) {
					if(!param1.equals(param2)){

						// let's generate precedences
						

						//	for(String caseId : activityLabelsCounterPrecedence.keySet()) {
						//		if (finishedTraces.containsKey(caseId) && finishedTraces.getItem(caseId) == true) {
						//			HashMap<String, Integer> counter = activityLabelsCounterPrecedence.getItem(caseId);
						//			HashMap<String, HashMap<String, Integer>> fulfillForThisTrace = fulfilledConstraintsPerTrace.getItem(caseId);
						//		if (fulfillForThisTrace == null) {
						//				fulfillForThisTrace = new HashMap<String, HashMap<String, Integer>>();
						//			}
						if(pastEvents.containsKey(param2)) {
							//	if(fulfillForThisTrace.containsKey(param1)){
							//	if(fulfillForThisTrace.get(param1).containsKey(param2)) {
							if (fulfilledForThisTrace.get(param1).get(param2) == pastEvents.get(param2)) {
								int satisfied = satisfiedTraces.get(param1).get(param2);
								satisfied ++;
								HashMap<String, Integer> sat = satisfiedTraces.get(param1);
								if(sat==null){
								 sat = new HashMap<String, Integer>();
								}
								sat.put(param2, satisfied);
								satisfiedTraces.put(param1, sat);
							} else {
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
						//	}else{
						//		violatedTraces++;
						//	}
						//}else{
						//	violatedTraces ++;
						//	}
						//	}
						//		}
						//	}
					//ecedence(param1, param2, completedTraces, violatedTraces, vacuouslySatisfiedTraces, satisfiedTraces);
					}
				}
			}
		}
		
		
		
		
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {
		for(String param1 : fulfilledForThisTrace.keySet()) {
			for(String param2 : fulfilledForThisTrace.get(param1).keySet()) {
				if(!param1.equals(param2)){
					int vacuouslySatisfiedTraces = completedTraces - satisfiedTraces.get(param1).get(param2) - violatedTraces.get(param1).get(param2);
					d.addPrecedence(param1, param2, completedTraces, satisfiedTraces.get(param1).get(param2), vacuouslySatisfiedTraces, violatedTraces.get(param1).get(param2));
					// d.addNotPrecedence(param1, param2, completedTraces, violatedTraces, vacuouslySatisfiedTraces, satisfiedTraces);
				}
			}
		}
	}


}

package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class ChainResponse implements TemplateReplayer {
	private HashMap<String,Integer> seenEvents = null;
	private String lastActivity = null;
	//private Counting<Boolean> finishedTraces = new Counting<Boolean>();

	HashMap<String, HashMap<String, Integer>> fulfilledForThisTrace = new HashMap<String, HashMap<String, Integer>>();
	// let's generate precedences

	HashMap<String, HashMap<String, Integer>> satisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;

	public ChainResponse (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Chain_Response)){
			String param1 = params.get(0);
			String param2 = params.get(1);
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
		seenEvents = new HashMap<String,Integer>();
	}

	@Override
	public void process(String event, boolean isTraceStart, boolean isLastEvent, boolean isEmpty) {
		if(isTraceStart){
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
			seenEvents = new HashMap<String,Integer>();
			lastActivity = null;
			//pastEvents = new HashMap<String,Integer>();
		}
		//String previous = lastActivity.getItem(caseId);
		if(lastActivity!=null && !lastActivity.equals("") && !lastActivity.equals(event)){
			HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
			if(fulfilledForThisTrace.containsKey(lastActivity)){
				secondElement = fulfilledForThisTrace.get(lastActivity);
			}
			int nofull = 0;
			if(secondElement.containsKey(event)){
				nofull = secondElement.get(event);
				secondElement.put(event, nofull+1);
				fulfilledForThisTrace.put(lastActivity,secondElement);

			}
			//secondElement.put(event, nofull+1);
			//fulfilledForThisTrace.put(lastActivity,secondElement);
			//	fulfilledConstraintsPerTraceCh.putItem(caseId, fulfilledForThisTrace);
		}

		//update the counter for the current trace and the current event
		//**********************

		int numberOfEvents = 1;
		if(!seenEvents.containsKey(event)){
			seenEvents.put(event, numberOfEvents);
		}else{
			numberOfEvents = seenEvents.get(event);
			numberOfEvents++;
			seenEvents.put(event, numberOfEvents); 
		}
		lastActivity = event;
		if(isLastEvent){
			for(String param1 : fulfilledForThisTrace.keySet()) {
				for(String param2 : fulfilledForThisTrace.get(param1).keySet()) {
					if(!param1.equals(param2)){
						if(seenEvents.containsKey(param1)){
							if (fulfilledForThisTrace.get(param1).get(param2) == seenEvents.get(param1)) {
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
					}
				}
			}
		}
		//***********************
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {
		for(String param1 : fulfilledForThisTrace.keySet()) {
			for(String param2 : fulfilledForThisTrace.get(param1).keySet()) {
				if(!param1.equals(param2)){
					int vacuouslySatisfied = completedTraces - satisfiedTraces.get(param1).get(param2) - violatedTraces.get(param1).get(param2);
					//	HashMap<String, Integer> map = new HashMap<String, Integer>();
					//	map.put(param2, vacuouslySatisfied);
					//	vacuouslySatisfiedTraces.put(param1, map);
					d.addChainResponse(param1, param2, completedTraces, satisfiedTraces.get(param1).get(param2), vacuouslySatisfied, violatedTraces.get(param1).get(param2));
				}
			}
		}
	}
}

package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;





import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class Init implements TemplateReplayer {

	HashMap<String, Integer> fulfilledForThisTrace = new HashMap<String, Integer>();

	HashMap<String, Integer> satisfiedTraces = new HashMap<String, Integer>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, Integer> violatedTraces = new HashMap<String, Integer>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;

	public Init (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Init)){
			String param1 = params.get(0);
			fulfilledForThisTrace.put(param1, 0);
			satisfiedTraces.put(param1, 0);
			violatedTraces.put(param1, 0);
		}
	}

	@Override
	public void process(String event, boolean isTraceStart, boolean isLastEvent, boolean isEmpty) {
		if(!isEmpty){
			if(isTraceStart){

				for(String param1 : fulfilledForThisTrace.keySet()){
					//	for(String param2 : fulfilledForThisTrace.get(param1).keySet()){
					//String param1 = params.get(0);
					//String param2 = params.get(1);
					//		HashMap<String, Integer> pend = new HashMap<String, Integer>();
					//		pend.put(param2, 0);
					fulfilledForThisTrace.put(param1, 0);
				}
				//}
				//fulfilledForThisTrace = new HashMap<String, Integer>();
				if(fulfilledForThisTrace.keySet().contains(event)){
					fulfilledForThisTrace.put(event, 1);
				}
			}

			if(isLastEvent){
				for(String param1 : fulfilledForThisTrace.keySet()) {


					//for(String caseId : activityLabelsCounterResponse.keySet()) {
					//		if (finishedTraces.containsKey(caseId) && finishedTraces.getItem(caseId) == true) {
					//	if(fulfilledConstraintsPerTrace.containsKey(caseId)){
					//	HashMap<String, Integer> counter = fulfilledConstraintsPerTrace.getItem(caseId);
					if(fulfilledForThisTrace.get(param1)==1){
						satisfiedTraces.put(param1, satisfiedTraces.get(param1)+1);
					}else{
						violatedTraces.put(param1, violatedTraces.get(param1)+1);
					}
					//	}else{
					//	violatedTraces++;
					//	}
					//	}
					//	}

				}
			}
			// ***********************
		}else{
			for(String param1 : fulfilledForThisTrace.keySet()) {
				violatedTraces.put(param1, violatedTraces.get(param1)+1);
			}
		}

	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {

		for(String param1 : fulfilledForThisTrace.keySet()) {

			int vacuouslySatisfiedTraces = completedTraces - satisfiedTraces.get(param1) - violatedTraces.get(param1);
			d.addInit(param1, completedTraces, satisfiedTraces.get(param1), vacuouslySatisfiedTraces, violatedTraces.get(param1));

		}
	}



}

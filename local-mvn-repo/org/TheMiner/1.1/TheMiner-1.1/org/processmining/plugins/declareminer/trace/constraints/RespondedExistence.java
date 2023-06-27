package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class RespondedExistence implements TemplateReplayer {

	private HashSet<String> pastEvents = null;
	HashMap<String, HashMap<String, Integer>> satisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;
	HashMap<String, HashMap<String, Integer>> pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();

	public RespondedExistence (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Responded_Existence)){
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
		pastEvents = new HashSet<String>();
	}



	@Override
	public void process(String event, boolean isANewTrace, boolean isLastEvent, boolean isEmpty) {

		if(isANewTrace){
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
			pastEvents = new HashSet<String>();
		}




		//	if (activityLabelsRespondedExistence.size()>1) {
		for (String firstElement : pendingForThisTrace.keySet()) {
			if (pendingForThisTrace.get(firstElement).containsKey(event)) {
				HashMap<String, Integer> secondElement = pendingForThisTrace.get(firstElement);
				secondElement.put(event, 0);
				pendingForThisTrace.put(firstElement, secondElement);
				//		pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
				//					pendingConstraintsPerTraceRe.put(trace, pendingForThisTrace);
			}
		}
		if(pendingForThisTrace.containsKey(event)){
			HashMap<String, Integer> secondElement = pendingForThisTrace.get(event);
			for (String second : secondElement.keySet()) {
				if (!second.equals(event)) {
					if (!pastEvents.contains(second)) {
						Integer pendingNo = secondElement.get(second);
						pendingNo ++;
						secondElement.put(second, pendingNo);
					} else {
						secondElement.put(second, 0);
					}
				}
			}
			pendingForThisTrace.put(event,secondElement);
		}
		//	pendingConstraintsPerTraceRe.putItem(caseId, pendingForThisTrace);
		//			pendingConstraintsPerTraceRe.put(trace, pendingForThisTrace);


		//update the counter for the current trace and the current event
		//**********************


		pastEvents.add(event);
		//	activityLabelsCounterRespondedExistence.putItem(caseId, counter);
		//***********************

		if(isLastEvent){
			for(String param1 : pendingForThisTrace.keySet()) {
				for(String param2 : pendingForThisTrace.get(param1).keySet()) {
					if(!param1.equals(param2)){


						//	for(String caseId : activityLabelsCounterRespondedExistence.keySet()) {
						//		if (finishedTraces.containsKey(caseId) && finishedTraces.getItem(caseId) == true) {
						//		HashMap<String, Integer> counter = activityLabelsCounterRespondedExistence.getItem(caseId);
						//	HashMap<String, HashMap<String, Integer>> pendingForThisTrace = pendingConstraintsPerTraceRe.getItem(caseId);
						//	if (pendingForThisTrace == null) {
						//		pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
						//	}

						if (pastEvents.contains(param1)) {
						//if (pendingForThisTrace.containsKey(param1)) {
						//					if (pendingForThisTrace.get(param1).containsKey(param2)) {
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
						//				}
								}
						//	}
						//	}
						//}
						//	vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;

						//	d.addRespondedExistence(param1, param2, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
						// d.addNotCoExistence(param1, param2, completedTraces, violatedTraces, vacuouslySatisfiedTraces, satisfiedTraces);
					}
				}
			}
		}
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {

		for(String param1 : pendingForThisTrace.keySet()) {
			for(String param2 : pendingForThisTrace.get(param1).keySet()) {
				if(!param1.equals(param2)){



					int vacuouslySatisfied = completedTraces - satisfiedTraces.get(param1).get(param2) - violatedTraces.get(param1).get(param2);

					d.addRespondedExistence(param1, param2, completedTraces, satisfiedTraces.get(param1).get(param2), vacuouslySatisfied, violatedTraces.get(param1).get(param2));
					// d.addNotCoExistence(param1, param2, completedTraces, violatedTraces, vacuouslySatisfiedTraces, satisfiedTraces);
				}
			}
		}
	}


}

package org.processmining.plugins.declareminer.trace.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class AlternatePrecedence implements TemplateReplayer {

	private HashMap<String,Integer> pastEvents = null;
	HashMap<String, HashMap<String, Integer>> fulfilledConstraintsPerTraceAlt = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violationsPerTrace = new HashMap<String, HashMap<String, Integer>>();	
	HashMap<String, HashMap<String, Boolean>> isDuplicatedActivationPerTrace = new HashMap<String, HashMap<String, Boolean>>();
	//	HashMap<String, HashMap<String, Integer>> violatedForThisTrace = new HashMap<String, HashMap<String, Integer>>();	

	HashMap<String, HashMap<String, Integer>> satisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;

	public AlternatePrecedence (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){

		
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Precedence)){
			String param1 = params.get(1);
			String param2 = params.get(0);

			HashMap<String, Integer> ful = fulfilledConstraintsPerTraceAlt.get(param1);
			if(ful==null){
				ful = new HashMap<String, Integer>();
			}
			ful.put(param2, 0);
			fulfilledConstraintsPerTraceAlt.put(param1, ful);

			HashMap<String, Integer> sat1 = violationsPerTrace.get(param1);
			if(sat1==null){
				sat1 = new HashMap<String, Integer>();
			}
			sat1.put(param2, 0);
			violationsPerTrace.put(param1, sat1);

			HashMap<String, Boolean> dupl = isDuplicatedActivationPerTrace.get(param1);
			if(dupl==null){
				dupl = new HashMap<String, Boolean>();
			}
			dupl.put(param2, false);
			isDuplicatedActivationPerTrace.put(param1, dupl);

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
			for(String param1 : isDuplicatedActivationPerTrace.keySet()){
				for(String param2 : isDuplicatedActivationPerTrace.get(param1).keySet()){

					HashMap<String, Integer> ful = fulfilledConstraintsPerTraceAlt.get(param1);
					if(ful==null){
						ful = new HashMap<String, Integer>();
					}
					ful.put(param2, 0);
					fulfilledConstraintsPerTraceAlt.put(param1, ful);

					HashMap<String, Integer> sat = violationsPerTrace.get(param1);
					if(sat==null){
						sat = new HashMap<String, Integer>();
					}
					sat.put(param2, 0);
					violationsPerTrace.put(param1, sat);

					HashMap<String, Boolean> dupl = isDuplicatedActivationPerTrace.get(param1);
					if(dupl==null){
						dupl = new HashMap<String, Boolean>();
					}
					dupl.put(param2, false);
					isDuplicatedActivationPerTrace.put(param1, dupl);
				}
			}
			pastEvents = new HashMap<String,Integer>();
		}

		for(String first : fulfilledConstraintsPerTraceAlt.keySet()){
			if(fulfilledConstraintsPerTraceAlt.get(first).containsKey(event)){
				if(!first.equals(event)){
					if(isDuplicatedActivationPerTrace.get(first).get(event) || !pastEvents.containsKey(first)){
						HashMap<String, Integer> viol = violationsPerTrace.get(first);
						if(viol==null){
							viol = new HashMap<String, Integer>();
						}
						viol.put(event, 1);
						violationsPerTrace.put(first, viol);
					}
					isDuplicatedActivationPerTrace.get(first).put(event, true);
				}
			}
		}

		if(fulfilledConstraintsPerTraceAlt.containsKey(event)){
			for(String second : fulfilledConstraintsPerTraceAlt.get(event).keySet()){
				if(fulfilledConstraintsPerTraceAlt.containsKey(event)){
					isDuplicatedActivationPerTrace.get(event).put(second, false);
				}
			}
		}
		int numberOfEvents = 1;
		if(!pastEvents.containsKey(event)){
			pastEvents.put(event, numberOfEvents);
		}else{
			numberOfEvents = pastEvents.get(event);
			numberOfEvents++;
			pastEvents.put(event, numberOfEvents); 
		}

		if(isLastEvent){

			for(String param1 : fulfilledConstraintsPerTraceAlt.keySet()){
				for(String param2 : fulfilledConstraintsPerTraceAlt.get(param1).keySet()){
					if(!param1.equals(param2)){	
						if (violationsPerTrace.get(param1).get(param2)==0) {
							if(pastEvents.containsKey(param2)){
								int satisfied = satisfiedTraces.get(param1).get(param2);
								satisfied ++;
								HashMap<String, Integer> sat = satisfiedTraces.get(param1);
								if(sat==null){
									sat = new HashMap<String, Integer>();
								}
								sat.put(param2, satisfied);
								satisfiedTraces.put(param1, sat);
							}
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

			//vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;

			//d.addAlternatePrecedence(param1, param2, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		}
		//		int numberOfEvents = 1;
		//		if(!counter.containsKey(event)){
		//			counter.put(event, numberOfEvents);
		//		}else{
		//			numberOfEvents = counter.get(event);
		//			numberOfEvents++;
		//			counter.put(event, numberOfEvents); 
		//		}
		//		activityLabelsCounterAltPrecedence.putItem(caseId, counter);
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {


		for(String param1 : fulfilledConstraintsPerTraceAlt.keySet()){
			for(String param2 : fulfilledConstraintsPerTraceAlt.get(param1).keySet()){
				if(!param1.equals(param2)){	
					int vacuouslySatisfiedTraces = completedTraces - satisfiedTraces.get(param1).get(param2) - violatedTraces.get(param1).get(param2);
					d.addAlternatePrecedence(param1, param2, completedTraces, satisfiedTraces.get(param1).get(param2), vacuouslySatisfiedTraces, violatedTraces.get(param1).get(param2));

				}
			}
		}
	}
}

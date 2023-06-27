package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;








import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class Succession implements TemplateReplayer {
	DeclareTemplate template = DeclareTemplate.Succession;
	HashMap<String, HashMap<String, Integer>> pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
	private HashMap<String,Integer> pastEvents = null;
	HashMap<String, HashMap<String, Integer>> a_and_b_occur_always_a_event_b = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> a_and_b_occur_never_a_event_b = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> a_and_b_occur_sometimes_a_event_b = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> one_of_a_and_b_is_missing = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;
	HashMap<String, HashMap<String, Integer>> fulfilledForThisTrace = new HashMap<String, HashMap<String, Integer>>();


	public Succession (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap,DeclareTemplate template){
		this.template = template;
		//DeclareTemplate temp = DeclareTemplate.Succession;
		//if(!declareTemplateCandidateDispositionsMap.containsKey(DeclareTemplate.Succession)){
		//temp = DeclareTemplate.Not_Succession;
		//}
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(template)){
			String param1 = params.get(0);
			String param2 = params.get(1);
			HashMap<String, Integer> pend = pendingForThisTrace.get(param1);
			if(pend==null){
				pend = new HashMap<String, Integer>();
			}
			pend.put(param2, 0);
			pendingForThisTrace.put(param1, pend);

			HashMap<String, Integer> ful = fulfilledForThisTrace.get(param1);
			if(ful==null){
				ful = new HashMap<String, Integer>();
			}
			ful.put(param2, 0);
			fulfilledForThisTrace.put(param1, ful);

			HashMap<String, Integer> sat = a_and_b_occur_always_a_event_b.get(param1);
			if(sat==null){
				sat = new HashMap<String, Integer>();
			}
			sat.put(param2, 0);
			a_and_b_occur_always_a_event_b.put(param1, sat);

			HashMap<String, Integer> viol = a_and_b_occur_sometimes_a_event_b.get(param1);
			if(viol==null){
				viol = new HashMap<String, Integer>();
			}
			viol.put(param2, 0);
			a_and_b_occur_sometimes_a_event_b.put(param1, viol);

			HashMap<String, Integer> strviol = a_and_b_occur_never_a_event_b.get(param1);
			if(strviol==null){
				strviol = new HashMap<String, Integer>();
			}
			strviol.put(param2, 0);
			a_and_b_occur_never_a_event_b.put(param1, strviol);

			
			HashMap<String, Integer> strviol2 = one_of_a_and_b_is_missing.get(param1);
			if(strviol2==null){
				strviol2 = new HashMap<String, Integer>();
			}
			strviol2.put(param2, 0);
			one_of_a_and_b_is_missing.put(param1, strviol2);

			
		}
		pastEvents = new HashMap<String,Integer>();
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

		//if (activityLabelsSuccession.size() > 1) {
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
		//	pendingConstraintsPerTraceSuccession.putItem(caseId, pendingForThisTrace);
		//			pendingConstraintsPerTraceSuccession.put(caseId, pendingForThisTrace);

		// activityLabelsCounter.put(trace, counter);
		//}

		// update the counter for the current trace and the current event
		// **********************

		int numberOfEvents = 1;
		if (!pastEvents.containsKey(event)) {
			pastEvents.put(event, numberOfEvents);
		} else {
			numberOfEvents = pastEvents.get(event);
			numberOfEvents++;
			pastEvents.put(event, numberOfEvents);
		}
		//	activityLabelsCounterSuccession.putItem(caseId, counter);
		// ***********************

		if(isLastEvent){

			for(String param1 : pendingForThisTrace.keySet()) {
				for(String param2 : pendingForThisTrace.get(param1).keySet()) {
					if(!param1.equals(param2)){
						//boolean satisfiedBool = false;
						//boolean violatedBool = false;
						if(pastEvents.containsKey(param1) && pastEvents.containsKey(param2) && pendingForThisTrace.get(param1).get(param2) == 0  && fulfilledForThisTrace.get(param1).get(param2) == pastEvents.get(param2)) {
							//satisfiedBool = true;
							int satisfied = a_and_b_occur_always_a_event_b.get(param1).get(param2);
							satisfied ++;
							HashMap<String, Integer> sat = a_and_b_occur_always_a_event_b.get(param1);
							if(sat==null){
								sat = new HashMap<String, Integer>();
							}
							sat.put(param2, satisfied);
							a_and_b_occur_always_a_event_b.put(param1, sat);
						} else if (pastEvents.containsKey(param1) && pastEvents.containsKey(param2) && (pendingForThisTrace.get(param1).get(param2) > 0 || fulfilledForThisTrace.get(param1).get(param2) < pastEvents.get(param2)) && fulfilledForThisTrace.get(param1).get(param2)>0) {
							int violated = a_and_b_occur_sometimes_a_event_b.get(param1).get(param2);
							violated ++;
							HashMap<String, Integer> viol = a_and_b_occur_sometimes_a_event_b.get(param1);
							if(viol==null){
								viol = new HashMap<String, Integer>();
							}
							viol.put(param2, violated);
							a_and_b_occur_sometimes_a_event_b.put(param1, viol);
						} else if (pastEvents.containsKey(param1) && pastEvents.containsKey(param2) && (pendingForThisTrace.get(param1).get(param2) > 0 || fulfilledForThisTrace.get(param1).get(param2) < pastEvents.get(param2)) && fulfilledForThisTrace.get(param1).get(param2)==0) {
							int violated = a_and_b_occur_never_a_event_b.get(param1).get(param2);
							violated ++;
							HashMap<String, Integer> viol = a_and_b_occur_never_a_event_b.get(param1);
							if(viol==null){
								viol = new HashMap<String, Integer>();
							}
							viol.put(param2, violated);
							a_and_b_occur_never_a_event_b.put(param1, viol);
						}else{
							if((pastEvents.containsKey(param1) && !pastEvents.containsKey(param2))||(!pastEvents.containsKey(param1) && pastEvents.containsKey(param2))){
								//one_of_a_and_b_is_missing_TracesBool = true;
								int violated = one_of_a_and_b_is_missing.get(param1).get(param2);
								violated ++;
								HashMap<String, Integer> viol = one_of_a_and_b_is_missing.get(param1);
								if(viol==null){
									viol = new HashMap<String, Integer>();
								}
								viol.put(param2, violated);
								one_of_a_and_b_is_missing.put(param1, viol);
//							
							}
						}
						//	violatedBool = true;
//							if(pastEvents.containsKey(param1) && pastEvents.containsKey(param2)){
//								int strviolated = a_and_b_occur_never_a_event_b.get(param1).get(param2);
//								strviolated ++;
//
//								HashMap<String, Integer> strviol = a_and_b_occur_never_a_event_b.get(param1);
//								if(strviol==null){
//									strviol = new HashMap<String, Integer>();
//								}
//								strviol.put(param2, strviolated);
//								a_and_b_occur_never_a_event_b.put(param1, strviol);
//							}
						}
//						if(pastEvents.containsKey(param2)){
//							if (fulfilledForThisTrace.get(param1).get(param2) == pastEvents.get(param2)) {
//								if (satisfiedBool) {
//									int satisfied = satisfiedTraces.get(param1).get(param2);
//									satisfied ++;
//									HashMap<String, Integer> sat = satisfiedTraces.get(param1);
//									if(sat==null){
//										sat = new HashMap<String, Integer>();
//									}
//									sat.put(param2, satisfied);
//									satisfiedTraces.put(param1, sat);
//								}
//							} else {
//								if (!violatedBool) {
//									int violated = violatedTraces.get(param1).get(param2);
//									violated ++;
//									HashMap<String, Integer> viol = violatedTraces.get(param1);
//									if(viol==null){
//										viol = new HashMap<String, Integer>();
//									}
//									viol.put(param2, violated);
//									violatedTraces.put(param1, viol);
//									if(pastEvents.containsKey(param1) && pastEvents.containsKey(param2)){
//										int strviolated = stronglyViolatedTraces.get(param1).get(param2);
//										strviolated ++;
//
//										HashMap<String, Integer> strviol = stronglyViolatedTraces.get(param1);
//										if(strviol==null){
//											strviol = new HashMap<String, Integer>();
//										}
//										strviol.put(param2, strviolated);
//										stronglyViolatedTraces.put(param1, strviol);
//									}
//								}
//							}
//						}
					
				}
			}
		}
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {
		for(String param1 : pendingForThisTrace.keySet()) {
			for(String param2 : pendingForThisTrace.get(param1).keySet()) {
				if(!param1.equals(param2)){
					//int vacuouslySatisfiedTraces = completedTraces - a_and_b_occur_always_a_event_b.get(param1).get(param2) - a_and_b_occur_sometimes_a_event_b.get(param1).get(param2) -a_and_b_occur_never_a_event_b.get(param1).get(param2);
					if(template.equals(DeclareTemplate.Succession)){
						d.addSuccession(param1, param2, completedTraces, a_and_b_occur_always_a_event_b.get(param1).get(param2), completedTraces - a_and_b_occur_always_a_event_b.get(param1).get(param2) - a_and_b_occur_sometimes_a_event_b.get(param1).get(param2) -a_and_b_occur_never_a_event_b.get(param1).get(param2) -one_of_a_and_b_is_missing.get(param1).get(param2), a_and_b_occur_sometimes_a_event_b.get(param1).get(param2)+a_and_b_occur_never_a_event_b.get(param1).get(param2) +one_of_a_and_b_is_missing.get(param1).get(param2));
					}else{
					//	System.out.println(param1 +"  "+ param2 +"  "+ completedTraces +"  "+ violatedTraces.get(param1).get(param2) +"  "+ vacuouslySatisfiedTraces +"  "+ satisfiedTraces.get(param1).get(param2)+"  "+stronglyViolatedTraces.get(param1).get(param2));
						d.addNotSuccession(param1, param2, completedTraces,a_and_b_occur_never_a_event_b.get(param1).get(param2) , completedTraces-a_and_b_occur_never_a_event_b.get(param1).get(param2)-a_and_b_occur_always_a_event_b.get(param1).get(param2)- a_and_b_occur_sometimes_a_event_b.get(param1).get(param2), a_and_b_occur_always_a_event_b.get(param1).get(param2)+ a_and_b_occur_sometimes_a_event_b.get(param1).get(param2));
					}
				}
			}
		}
	}




	public HashMap<String, HashMap<String, Integer>> getA_and_b_occur_always_a_event_b() {
		return a_and_b_occur_always_a_event_b;
	}




	public void setA_and_b_occur_always_a_event_b(
			HashMap<String, HashMap<String, Integer>> a_and_b_occur_always_a_event_b) {
		this.a_and_b_occur_always_a_event_b = a_and_b_occur_always_a_event_b;
	}




	public HashMap<String, HashMap<String, Integer>> getA_and_b_occur_never_a_event_b() {
		return a_and_b_occur_never_a_event_b;
	}




	public void setA_and_b_occur_never_a_event_b(HashMap<String, HashMap<String, Integer>> a_and_b_occur_never_a_event_b) {
		this.a_and_b_occur_never_a_event_b = a_and_b_occur_never_a_event_b;
	}




	public HashMap<String, HashMap<String, Integer>> getA_and_b_occur_sometimes_a_event_b() {
		return a_and_b_occur_sometimes_a_event_b;
	}




	public void setA_and_b_occur_sometimes_a_event_b(
			HashMap<String, HashMap<String, Integer>> a_and_b_occur_sometimes_a_event_b) {
		this.a_and_b_occur_sometimes_a_event_b = a_and_b_occur_sometimes_a_event_b;
	}




	public HashMap<String, HashMap<String, Integer>> getOne_of_a_and_b_is_missing() {
		return one_of_a_and_b_is_missing;
	}




	public void setOne_of_a_and_b_is_missing(HashMap<String, HashMap<String, Integer>> one_of_a_and_b_is_missing) {
		this.one_of_a_and_b_is_missing = one_of_a_and_b_is_missing;
	}




	public HashMap<String, HashMap<String, Integer>> getSatisfiedTraces() {
		return a_and_b_occur_always_a_event_b;
	}


	public void setSatisfiedTraces(HashMap<String, HashMap<String, Integer>> satisfiedTraces) {
		this.a_and_b_occur_always_a_event_b = satisfiedTraces;
	}


	public HashMap<String, HashMap<String, Integer>> getViolatedTraces() {
		return a_and_b_occur_sometimes_a_event_b;
	}

	public void setViolatedTraces(HashMap<String, HashMap<String, Integer>> violatedTraces) {
		this.a_and_b_occur_sometimes_a_event_b = violatedTraces;
	}




	public HashMap<String, HashMap<String, Integer>> getStronglyViolatedTraces() {
		return a_and_b_occur_never_a_event_b;
	}




	public void setStronglyViolatedTraces(HashMap<String, HashMap<String, Integer>> stronglyViolatedTraces) {
		this.a_and_b_occur_never_a_event_b = stronglyViolatedTraces;
	}




	public DeclareTemplate getTemplate() {
		return template;
	}




	public void setTemplate(DeclareTemplate template) {
		this.template = template;
	}





}

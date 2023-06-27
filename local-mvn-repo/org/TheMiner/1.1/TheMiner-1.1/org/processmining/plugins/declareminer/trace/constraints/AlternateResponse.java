package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class AlternateResponse implements TemplateReplayer {

	HashMap<String, HashMap<String, Integer>> pendingForThisTrace = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedForThisTrace = new HashMap<String, HashMap<String, Integer>>();	
	//	private Counting<HashMap<String, Integer>> activityLabelsCounterResponse = new Counting<HashMap<String, Integer>>();
	//	private Counting<HashMap<String, HashMap<String, Integer>>> pendingConstraintsPerTrace = new Counting<HashMap<String, HashMap<String, Integer>>>();
	//	private Counting<Boolean> finishedTraces = new Counting<Boolean>();
	HashMap<String, HashMap<String, Integer>> vsatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;

	HashSet<String> seen = new HashSet<String>();

	public AlternateResponse (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Alternate_Response)){
			String param1 = params.get(0);
			String param2 = params.get(1);
			HashMap<String, Integer> pend = pendingForThisTrace.get(param1);
			if(pend==null){
				pend = new HashMap<String, Integer>();
			}
			pend.put(param2, 0);
			pendingForThisTrace.put(param1, pend);

			HashMap<String, Integer> sat = vsatisfiedTraces.get(param1);
			if(sat==null){
				sat = new HashMap<String, Integer>();
			}
			sat.put(param2, 0);
			vsatisfiedTraces.put(param1, sat);



			HashMap<String, Integer> viol = violatedTraces.get(param1);
			if(viol==null){
				viol = new HashMap<String, Integer>();
			}
			viol.put(param2, 0);
			violatedTraces.put(param1, viol);

			HashMap<String, Integer> ful = violatedForThisTrace.get(param1);
			if(ful==null){
				ful = new HashMap<String, Integer>();
			}
			ful.put(param2, 0);
			violatedForThisTrace.put(param1, ful);
		}
	}


	@Override
	public void process(String event, boolean isANewTrace, boolean isLastEvent, boolean isEmpty) {
		if(isANewTrace){

			seen = new HashSet<String>();
			for(String param1 : pendingForThisTrace.keySet()){
				for(String param2 : pendingForThisTrace.get(param1).keySet()){
					HashMap<String, Integer> pend = pendingForThisTrace.get(param1);
					if(pend==null){
						pend = new HashMap<String, Integer>();
					}
					pend.put(param2, 0);
					pendingForThisTrace.put(param1, pend);

					HashMap<String, Integer> ful = violatedForThisTrace.get(param1);
					if(ful==null){
						ful = new HashMap<String, Integer>();
					}
					ful.put(param2, 0);
					violatedForThisTrace.put(param1, ful);
				}
			}
		}


		seen.add(event);

		//		if(!counter.containsKey(event)){
		//			if(activityLabelsAltResponse.size()>1){	
		//				for(String existingEvent : activityLabelsAltResponse){
		//					if(!existingEvent.equals(event)){
		//						int pend = 0;
		//						if(activityLabelsCounterAltResponse.containsKey(trace)){
		//							if(activityLabelsCounterAltResponse.getItem(trace).containsKey(existingEvent)){
		//								pend = activityLabelsCounterAltResponse.getItem(trace).get(existingEvent);
		//							}
		//						}
		//						HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
		//						if(pendingForThisTrace.containsKey(existingEvent)){
		//							secondElement = pendingForThisTrace.get(existingEvent);
		//						}
		//						if(pend>1){
		//							HashMap<String, Integer> secondEl = new  HashMap<String, Integer>();
		//							if(violatedForThisTrace.containsKey(existingEvent)){
		//								secondEl = violatedForThisTrace.get(existingEvent);
		//							}
		//							secondEl.put(event, pend-1);
		//							violatedForThisTrace.put(existingEvent, secondEl);
		//							violatedConstraintsPerTrace.putItem(trace, violatedForThisTrace);
		//						}
		//						secondElement.put(event, 0);
		//						pendingForThisTrace.put(existingEvent, secondElement);
		//
		//						//	pendingConstraintsPerTraceAlt.put(trace, pendingForThisTrace);
		//						//					}
		//					}
		//				}
		//				for(String existingEvent : activityLabelsAltResponse){
		//					if(!existingEvent.equals(event)){
		//						HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
		//						if(pendingForThisTrace.containsKey(event)){
		//							secondElement = pendingForThisTrace.get(event);
		//						}
		//						secondElement.put(existingEvent, 1);
		//						pendingForThisTrace.put(event,secondElement);
		//					}
		//				}
		//
		//				pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);
		//
		//			}
		//	}else{
		//	if(activityLabelsAltResponse.size()>1){
		for(String firstElement : violatedForThisTrace.keySet()){
			if(violatedForThisTrace.get(firstElement).containsKey(event)){
				HashMap<String, Integer> secondEl = null;
				//	if(violatedForThisTrace.containsKey(firstElement)){
				secondEl = violatedForThisTrace.get(firstElement);
				//	}
				HashMap<String, Integer> secondElement = null;
				//	if(pendingForThisTrace.containsKey(firstElement)){
				secondElement = pendingForThisTrace.get(firstElement);
				//	}

				if(secondElement.containsKey(event) && secondElement.get(event)>1){
					Integer violNo = secondElement.get(event);
					Integer totviol = 0;
					if(secondEl.containsKey(event)){
						totviol = secondEl.get(event);
					}
					secondEl.put(event, totviol + violNo);
					violatedForThisTrace.put(firstElement, secondEl);
					//	violatedConstraintsPerTrace.putItem(trace, violatedForThisTrace);
				}
				secondElement.put(event, 0);
				pendingForThisTrace.put(firstElement, secondElement);

				//pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);

			}
		}
		if(pendingForThisTrace.containsKey(event)){
			HashMap<String, Integer> secondElement = pendingForThisTrace.get(event);
			for(String second : secondElement.keySet()){
				if(!second.equals(event)){
					Integer pendingNo = 1;
					if(secondElement.containsKey(second)){
						pendingNo = secondElement.get(second);	
						pendingNo ++;
					}
					secondElement.put(second, pendingNo);
				}
			}
			pendingForThisTrace.put(event,secondElement);
		}
		//pendingConstraintsPerTraceAlt.putItem(trace, pendingForThisTrace);

		//activityLabelsCounter.put(trace, counter);
		//	}
		//	}
		//update the counter for the current trace and the current event
		//**********************

		if(isLastEvent){

			for(String param1 : pendingForThisTrace.keySet()){
				for(String param2 : pendingForThisTrace.get(param1).keySet()){
					if(!param1.equals(param2)){
						if ((pendingForThisTrace.get(param1).get(param2) > 0)) {
							int violated = violatedTraces.get(param1).get(param2);
							violated ++;
							HashMap<String, Integer> viol = violatedTraces.get(param1);
							if(viol==null){
								viol = new HashMap<String, Integer>();
							}
							viol.put(param2, violated);
							violatedTraces.put(param1, viol);
						}else if(violatedForThisTrace.get(param1).get(param2) > 0){
							int violated = violatedTraces.get(param1).get(param2);
							violated ++;
							HashMap<String, Integer> viol = violatedTraces.get(param1);
							if(viol==null){
								viol = new HashMap<String, Integer>();
							}
							viol.put(param2, violated);
							violatedTraces.put(param1, viol);
						} else {
							if(!seen.contains(param1)){
								int satisfied = vsatisfiedTraces.get(param1).get(param2);
								satisfied ++;
								HashMap<String, Integer> sat = vsatisfiedTraces.get(param1);
								if(sat==null){
									sat = new HashMap<String, Integer>();
								}
								sat.put(param2, satisfied);
								vsatisfiedTraces.put(param1, sat);
							}
						}
					}
				}

				//	satisfiedTraces = completedTraces - (vacuouslySatisfiedTraces + violatedTraces);

				//	d.addAlternateResponse(param1, param2, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
				//}
			}
		}

		//		int numberOfEvents = 1;
		//		if(!counter.containsKey(event)){
		//			counter.put(event, numberOfEvents);
		//		}else{
		//			numberOfEvents = counter.get(event);
		//			numberOfEvents++;
		//			counter.put(event, numberOfEvents); 
		//		}
		//		activityLabelsCounterAltResponse.putItem(trace, counter);
		//***********************
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {

		for(String param1 : pendingForThisTrace.keySet()){
			for(String param2 : pendingForThisTrace.get(param1).keySet()){
				if(!param1.equals(param2)){

					int satisfiedTraces = completedTraces - (vsatisfiedTraces.get(param1).get(param2) + violatedTraces.get(param1).get(param2));

					d.addAlternateResponse(param1, param2, completedTraces, satisfiedTraces, vsatisfiedTraces.get(param1).get(param2), violatedTraces.get(param1).get(param2));
					//}
				}
			}
		}
	}


}

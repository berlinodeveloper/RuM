package org.processmining.plugins.declareminer.trace.constraints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class ChainSuccession implements TemplateReplayer {
	DeclareTemplate template = DeclareTemplate.Chain_Succession;
	private HashMap<String,Integer> seenEvents = null;
	private String lastActivity = null;
	//private Counting<Boolean> finishedTraces = new Counting<Boolean>();
	HashMap<String, HashMap<String, Integer>> fulfilledForThisTracePrecedencePart = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> fulfilledForThisTraceResponsePart = new HashMap<String, HashMap<String, Integer>>();
	// let's generate precedences
	HashMap<String, HashMap<String, Integer>> a_and_b_occur_but_never_in_sequence_Traces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> a_and_b_occur_always_in_sequence_Traces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> one_of_a_and_b_is_missing_Traces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> a_and_b_occur_only_sometimes_in_sequence_Traces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;
	public ChainSuccession (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap,DeclareTemplate template){
		this.template = template;
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(template)){
			String param1 = params.get(0);
			String param2 = params.get(1);

			HashMap<String, Integer> ful = fulfilledForThisTracePrecedencePart.get(param1);
			if(ful==null){
				ful = new HashMap<String, Integer>();
			}
			ful.put(param2, 0);
			fulfilledForThisTracePrecedencePart.put(param1, ful);

			HashMap<String, Integer> ful3 = fulfilledForThisTraceResponsePart.get(param1);
			if(ful3==null){
				ful3 = new HashMap<String, Integer>();
			}
			ful3.put(param2, 0);
			fulfilledForThisTraceResponsePart.put(param1, ful3);

			HashMap<String, Integer> sat = a_and_b_occur_always_in_sequence_Traces.get(param1);
			if(sat==null){
				sat = new HashMap<String, Integer>();
			}
			sat.put(param2, 0);
			a_and_b_occur_always_in_sequence_Traces.put(param1, sat);

			HashMap<String, Integer> viol = one_of_a_and_b_is_missing_Traces.get(param1);
			if(viol==null){
				viol = new HashMap<String, Integer>();
			}
			viol.put(param2, 0);
			one_of_a_and_b_is_missing_Traces.put(param1, viol);	

			HashMap<String, Integer> violb = a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1);
			if(violb==null){
				violb = new HashMap<String, Integer>();
			}
			violb.put(param2, 0);
			a_and_b_occur_only_sometimes_in_sequence_Traces.put(param1, violb);

			HashMap<String, Integer> strviol = a_and_b_occur_but_never_in_sequence_Traces.get(param1);
			if(strviol==null){
				strviol = new HashMap<String, Integer>();
			}
			strviol.put(param2, 0);
			a_and_b_occur_but_never_in_sequence_Traces.put(param1, strviol);
		}	


		seenEvents = new HashMap<String,Integer>();
	}


	@Override
	public void process(String event, boolean isTraceStart, boolean isLastEvent, boolean isEmpty) {
		if(isTraceStart){
			for(String param1 : fulfilledForThisTraceResponsePart.keySet()){
				for(String param2 : fulfilledForThisTraceResponsePart.get(param1).keySet()){
					HashMap<String, Integer> ful = fulfilledForThisTraceResponsePart.get(param1);
					if(ful==null){
						ful = new HashMap<String, Integer>();
					}
					ful.put(param2, 0);
					fulfilledForThisTraceResponsePart.put(param1, ful);
				}
			}

			for(String param1 : fulfilledForThisTracePrecedencePart.keySet()){
				for(String param2 : fulfilledForThisTracePrecedencePart.get(param1).keySet()){
					HashMap<String, Integer> ful = fulfilledForThisTracePrecedencePart.get(param1);
					if(ful==null){
						ful = new HashMap<String, Integer>();
					}
					ful.put(param2, 0);
					fulfilledForThisTracePrecedencePart.put(param1, ful);
				}
			}






			seenEvents = new HashMap<String,Integer>();
			lastActivity = null;
			//pastEvents = new HashMap<String,Integer>();
		}
		//String previous = lastActivity.getItem(caseId);
		if(lastActivity!=null && !lastActivity.equals("") && !lastActivity.equals(event)){
			HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
			if(fulfilledForThisTraceResponsePart.containsKey(lastActivity)){
				secondElement = fulfilledForThisTraceResponsePart.get(lastActivity);
			}
			int nofull = 0;
			if(secondElement.containsKey(event)){
				nofull = secondElement.get(event);
				secondElement.put(event, nofull+1);
				fulfilledForThisTraceResponsePart.put(lastActivity,secondElement);
			}

			//	fulfilledConstraintsPerTraceCh.putItem(caseId, fulfilledForThisTrace);
		}


		//		//update the counter for the current trace and the current event
		//		//**********************
		//
		//		int numberOfEvents = 1;
		//		if(!counter.containsKey(event)){
		//			counter.put(event, numberOfEvents);
		//		}else{
		//			numberOfEvents = counter.get(event);
		//			numberOfEvents++;
		//			counter.put(event, numberOfEvents); 
		//		}
		//		activityLabelsCounterChResponse.putItem(caseId, counter);
		//		lastActivityResponse.putItem(caseId, event);
		//		//***********************
		//		
		//		



		if(lastActivity!=null && !lastActivity.equals("") && !lastActivity.equals(event)){
			HashMap<String, Integer> secondElement = new  HashMap<String, Integer>();
			if(fulfilledForThisTracePrecedencePart.containsKey(lastActivity)){
				secondElement = fulfilledForThisTracePrecedencePart.get(lastActivity);
			}
			int nofull = 0;
			if(secondElement.containsKey(event)){
				nofull = secondElement.get(event);
				secondElement.put(event, nofull+1);
				fulfilledForThisTracePrecedencePart.put(lastActivity,secondElement);
			}
			//	fulfilledConstraintsPerTraceChPrecedence.putItem(caseId, fulfilledForThisTrace);
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

			for(String param1 : a_and_b_occur_always_in_sequence_Traces.keySet()) {
				for(String param2 : a_and_b_occur_always_in_sequence_Traces.get(param1).keySet()) {
					if(!param1.equals(param2)){
						
						
						boolean a_and_b_occur_but_never_in_sequence_TracesBool = false;
						boolean a_and_b_occur_always_in_sequence_TracesBool = false; 
						boolean one_of_a_and_b_is_missing_TracesBool = false;
						boolean a_and_b_occur_only_sometimes_in_sequence_TracesBool = false;
						
						//boolean pos_satisfiedBool = false;
						//boolean violatedBool = false;
						//boolean violatedBoth = false;
						if(seenEvents.containsKey(param1) && seenEvents.containsKey(param2)){
							if (fulfilledForThisTraceResponsePart.get(param1).get(param2) == seenEvents.get(param1) && fulfilledForThisTraceResponsePart.get(param1).get(param2) == seenEvents.get(param2)) {
								a_and_b_occur_always_in_sequence_TracesBool = true;
								//a_and_b_occur_but_never_in_sequence_TracesBool = true;
								int violated = a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2);
								violated ++;
								HashMap<String, Integer> viol = a_and_b_occur_always_in_sequence_Traces.get(param1);
								if(viol==null){
									viol = new HashMap<String, Integer>();
								}
								viol.put(param2, violated);
								a_and_b_occur_always_in_sequence_Traces.put(param1, viol);
							} else {
								if (fulfilledForThisTraceResponsePart.get(param1).get(param2) != 0){
									a_and_b_occur_only_sometimes_in_sequence_TracesBool = true;
									int violated = a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2);
									violated ++;
									HashMap<String, Integer> viol = a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1);
									if(viol==null){
										viol = new HashMap<String, Integer>();
									}
									viol.put(param2, violated);
									a_and_b_occur_only_sometimes_in_sequence_Traces.put(param1, viol);
								}else{
									a_and_b_occur_but_never_in_sequence_TracesBool = true;
									int violated = a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2);
									violated ++;
									HashMap<String, Integer> viol = a_and_b_occur_but_never_in_sequence_Traces.get(param1);
									if(viol==null){
										viol = new HashMap<String, Integer>();
									}
									viol.put(param2, violated);
									a_and_b_occur_but_never_in_sequence_Traces.put(param1, viol);
//									if(seenEvents.containsKey(param1) && seenEvents.containsKey(param2)){
//										int strviolated = a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2);
//										strviolated ++;
//
//										HashMap<String, Integer> strviol = a_and_b_occur_but_never_in_sequence_Traces.get(param1);
//										if(strviol==null){
//											strviol = new HashMap<String, Integer>();
//										}
//										strviol.put(param2, strviolated);
//										a_and_b_occur_but_never_in_sequence_Traces.put(param1, strviol);
//									}
								}
							}
						}else{
							if(seenEvents.containsKey(param1) || seenEvents.containsKey(param2)){
								one_of_a_and_b_is_missing_TracesBool = true;
								int violated = one_of_a_and_b_is_missing_Traces.get(param1).get(param2);
								violated ++;
								HashMap<String, Integer> viol = one_of_a_and_b_is_missing_Traces.get(param1);
								if(viol==null){
									viol = new HashMap<String, Integer>();
								}
								viol.put(param2, violated);
								one_of_a_and_b_is_missing_Traces.put(param1, viol);
//							
							}
						}
//						if(seenEvents.containsKey(param2)){
//							if (fulfilledForThisTracePrecedencePart.get(param1).get(param2) == seenEvents.get(param2)) {
//
//								if (pos_satisfiedBool) {
//									int satisfied = a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2);
//									satisfied ++;
//									HashMap<String, Integer> sat = a_and_b_occur_always_in_sequence_Traces.get(param1);
//									if(sat==null){
//										sat = new HashMap<String, Integer>();
//									}
//									sat.put(param2, satisfied);
//									a_and_b_occur_always_in_sequence_Traces.put(param1, sat);
//								}
//							}
//							else {
//								if (!violatedBool && !violatedBoth) {
//									if (fulfilledForThisTraceResponsePart.get(param1).get(param2) != 0){
//										violatedBoth = true;
//										int violated = a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2);
//										violated ++;
//										HashMap<String, Integer> viol = a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1);
//										if(viol==null){
//											viol = new HashMap<String, Integer>();
//										}
//										viol.put(param2, violated);
//										a_and_b_occur_only_sometimes_in_sequence_Traces.put(param1, viol);
//									}else{
//										int violated = one_of_a_and_b_is_missing_Traces.get(param1).get(param2);
//										violated ++;
//										HashMap<String, Integer> viol = one_of_a_and_b_is_missing_Traces.get(param1);
//										if(viol==null){
//											viol = new HashMap<String, Integer>();
//										}
//										viol.put(param2, violated);
//										one_of_a_and_b_is_missing_Traces.put(param1, viol);
//										if(seenEvents.containsKey(param1) && seenEvents.containsKey(param2)){
//											int strviolated = a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2);
//											strviolated ++;
//
//											HashMap<String, Integer> strviol = a_and_b_occur_but_never_in_sequence_Traces.get(param1);
//											if(strviol==null){
//												strviol = new HashMap<String, Integer>();
//											}
//											strviol.put(param2, strviolated);
//											a_and_b_occur_but_never_in_sequence_Traces.put(param1, strviol);
//										}
//									}
//
//								}
//
//							}
//						}
					}
				}
			}
		}
	}


	@Override
	public void updateModel(DeclareModel d, int completedTraces) {
		for(String param1 : a_and_b_occur_always_in_sequence_Traces.keySet()) {
			for(String param2 : a_and_b_occur_always_in_sequence_Traces.get(param1).keySet()) {
				if(!param1.equals(param2)){
					//int vacuouslySatisfied = completedTraces - a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2) - one_of_a_and_b_is_missing_Traces.get(param1).get(param2) - a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2);
					//	HashMap<String, Integer> map = new HashMap<String, Integer>();
					//	map.put(param2, vacuouslySatisfied);
					//	vacuouslySatisfiedTraces.put(param1, map);
					//int viol = a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)+ a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2);
					//System.out.println(""+param1 +" "+ param2 +" "+ completedTraces +" "+ violatedTraces.get(param1).get(param2) +" "+ vacuouslySatisfied+" "+ );
					if(template.equals(DeclareTemplate.Chain_Succession)){
						d.addChainSuccession(param1, param2, completedTraces, a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2), completedTraces-a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)-one_of_a_and_b_is_missing_Traces.get(param1).get(param2)- a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2)- a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2), one_of_a_and_b_is_missing_Traces.get(param1).get(param2)+ a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2)+ a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2));
				
//						int sat = a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2);
//						int vac = completedTraces -a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)- a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2)-a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2)-one_of_a_and_b_is_missing_Traces.get(param1).get(param2);
//						int viol = a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2)+a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2)+one_of_a_and_b_is_missing_Traces.get(param1).get(param2);
//						System.out.println(param1+" "+param2+" "+completedTraces+" "+sat+" "+vac+" "+viol);
					
					}else{
						
						//int sat = a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2);
						//int vac = completedTraces -a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)- a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2)-a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2);
						//int viol = a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)+ a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2);
						//System.out.println(param1+" "+param2+" "+completedTraces+" "+sat+" "+vac+" "+viol);
						d.addNotChainSuccession(param1, param2, completedTraces, a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2), completedTraces -a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)- a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2)-a_and_b_occur_but_never_in_sequence_Traces.get(param1).get(param2), a_and_b_occur_always_in_sequence_Traces.get(param1).get(param2)+ a_and_b_occur_only_sometimes_in_sequence_Traces.get(param1).get(param2));
					}
				}
			}
		}
	}


	public HashMap<String, HashMap<String, Integer>> getSatisfiedTraces() {
		return a_and_b_occur_always_in_sequence_Traces;
	}


	public void setSatisfiedTraces(HashMap<String, HashMap<String, Integer>> satisfiedTraces) {
		this.a_and_b_occur_always_in_sequence_Traces = satisfiedTraces;
	}


	public HashMap<String, HashMap<String, Integer>> getViolatedTraces() {
		return one_of_a_and_b_is_missing_Traces;
	}

	public void setViolatedTraces(HashMap<String, HashMap<String, Integer>> violatedTraces) {
		this.one_of_a_and_b_is_missing_Traces = violatedTraces;
	}
	

	public HashMap<String, HashMap<String, Integer>> getViolatedTracesBoth() {
		return a_and_b_occur_only_sometimes_in_sequence_Traces;
	}


	public void setViolatedTracesBoth(HashMap<String, HashMap<String, Integer>> violatedTracesBoth) {
		this.a_and_b_occur_only_sometimes_in_sequence_Traces = violatedTracesBoth;
	}


	public HashMap<String, HashMap<String, Integer>> getStronglyViolatedTraces() {
		return a_and_b_occur_but_never_in_sequence_Traces;
	}


	
	
	


	public HashMap<String, HashMap<String, Integer>> getA_and_b_occur_but_never_in_sequence_Traces() {
		return a_and_b_occur_but_never_in_sequence_Traces;
	}


	public void setA_and_b_occur_but_never_in_sequence_Traces(
			HashMap<String, HashMap<String, Integer>> a_and_b_occur_but_never_in_sequence_Traces) {
		this.a_and_b_occur_but_never_in_sequence_Traces = a_and_b_occur_but_never_in_sequence_Traces;
	}


	public HashMap<String, HashMap<String, Integer>> getA_and_b_occur_always_in_sequence_Traces() {
		return a_and_b_occur_always_in_sequence_Traces;
	}


	public void setA_and_b_occur_always_in_sequence_Traces(
			HashMap<String, HashMap<String, Integer>> a_and_b_occur_always_in_sequence_Traces) {
		this.a_and_b_occur_always_in_sequence_Traces = a_and_b_occur_always_in_sequence_Traces;
	}


	public HashMap<String, HashMap<String, Integer>> getOne_of_a_and_b_is_missing_Traces() {
		return one_of_a_and_b_is_missing_Traces;
	}


	public void setOne_of_a_and_b_is_missing_Traces(
			HashMap<String, HashMap<String, Integer>> one_of_a_and_b_is_missing_Traces) {
		this.one_of_a_and_b_is_missing_Traces = one_of_a_and_b_is_missing_Traces;
	}


	public HashMap<String, HashMap<String, Integer>> getA_and_b_occur_only_sometimes_in_sequence_Traces() {
		return a_and_b_occur_only_sometimes_in_sequence_Traces;
	}


	public void setA_and_b_occur_only_sometimes_in_sequence_Traces(
			HashMap<String, HashMap<String, Integer>> a_and_b_occur_only_sometimes_in_sequence_Traces) {
		this.a_and_b_occur_only_sometimes_in_sequence_Traces = a_and_b_occur_only_sometimes_in_sequence_Traces;
	}


	public void setStronglyViolatedTraces(HashMap<String, HashMap<String, Integer>> stronglyViolatedTraces) {
		this.a_and_b_occur_but_never_in_sequence_Traces = stronglyViolatedTraces;
	}




	public DeclareTemplate getTemplate() {
		return template;
	}




	public void setTemplate(DeclareTemplate template) {
		this.template = template;
	}





}

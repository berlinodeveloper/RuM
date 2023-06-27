package org.processmining.plugins.declareminer.trace.constraints;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;






import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.DeclareModel;

public class Choice implements TemplateReplayer {

	HashSet<String> seen = new HashSet<String>();
	Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap;
	HashMap<String, HashMap<String, Integer>> satisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	//HashMap<String, HashMap<String, Integer>> vacuouslySatisfiedTraces = new HashMap<String, HashMap<String, Integer>>();
	HashMap<String, HashMap<String, Integer>> violatedTraces = new HashMap<String, HashMap<String, Integer>>();
	//int satisfiedTraces = 0;
	//int vacuouslySatisfiedTraces = 0;
	//int violatedTraces = 0;
	public Choice (Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap){
		this.declareTemplateCandidateDispositionsMap = declareTemplateCandidateDispositionsMap;
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Choice)){
			String param1 = params.get(0);
			String param2 = params.get(1);

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
	public void process(String event, boolean isTraceStart, boolean isLastEvent, boolean isEmpty) {
		if(!isEmpty){
			if(isTraceStart){
				seen = new HashSet<String>();
			}
			seen.add(event);

			if(isLastEvent){
				for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Choice)) {
					//for(String param2 : activityLabelsChoice) {
					//	if(!param1.equals(param2)){

					String param1 = params.get(0);
					String param2 = params.get(1);
					//	for(String caseId : activityLabelsCounterChoice.keySet()) {
					//if (finishedTraces.containsKey(caseId) && finishedTraces.getItem(caseId) == true) {
					//	HashMap<String, Integer> counter = activityLabelsCounterChoice.getItem(caseId);
					if(seen.contains(param1)||seen.contains(param2)){
						int satisfied = satisfiedTraces.get(param1).get(param2);
						satisfied ++;
						HashMap<String, Integer> sat = satisfiedTraces.get(param1);
						if(sat==null){
							sat = new HashMap<String, Integer>();
						}
						sat.put(param2, satisfied);
						satisfiedTraces.put(param1, sat);
					}
					if((!seen.contains(param1))&&(!seen.contains(param2))){
						int violated = violatedTraces.get(param1).get(param2);
						violated ++;
						HashMap<String, Integer> viol = violatedTraces.get(param1);
						if(viol==null){
							viol = new HashMap<String, Integer>();
						}
						viol.put(param2, violated);
						violatedTraces.put(param1, viol);
					}	
					//}

					//vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;
					//	d.addChoices(param1,param2, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
				}
			}
		}else{
			for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Choice)) {
				//for(String param2 : activityLabelsChoice) {
				//	if(!param1.equals(param2)){

				String param1 = params.get(0);
				String param2 = params.get(1);
				int violated = violatedTraces.get(param1).get(param2);
				violated ++;
				HashMap<String, Integer> viol = violatedTraces.get(param1);
				if(viol==null){
					viol = new HashMap<String, Integer>();
				}
				viol.put(param2, violated);
				violatedTraces.put(param1, viol);
			}	
			//}

			//vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;
			//	d.addChoices(param1,param2, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		}
	}

	@Override
	public void updateModel(DeclareModel d, int completedTraces) {
		for(List<String> params : declareTemplateCandidateDispositionsMap.get(DeclareTemplate.Choice)) {
			//for(String param2 : activityLabelsChoice) {
			//	if(!param1.equals(param2)){

			String param1 = params.get(0);
			String param2 = params.get(1);
			int vacuouslySatisfiedTraces = completedTraces - satisfiedTraces.get(param1).get(param2) - violatedTraces.get(param1).get(param2);
			d.addChoices(param1, param2, completedTraces, satisfiedTraces.get(param1).get(param2), vacuouslySatisfiedTraces, violatedTraces.get(param1).get(param2));
		}
	}

}

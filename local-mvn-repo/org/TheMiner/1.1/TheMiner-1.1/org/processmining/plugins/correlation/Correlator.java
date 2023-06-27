package org.processmining.plugins.correlation;

import java.util.Vector;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.templates.NegativeRelationInfo;
import org.processmining.plugins.declareminer.templates.NotCoexistenceInfo;
import org.processmining.plugins.declareminer.templates.PrecedenceInfo;
import org.processmining.plugins.declareminer.templates.RespondedExistenceInfo;
import org.processmining.plugins.declareminer.templates.ResponseInfo;
import org.processmining.plugins.declareminer.templates.TemplateInfo;

public class Correlator {

	public static Vector<ExtendedTrace> getExtendedTracesWithCorrespondingEvents(DeclareTemplate template, String activation, String target, XLog log){
		Vector<ExtendedTrace> tracesWithCorrespondingEvents = new Vector<ExtendedTrace>();
		TemplateInfo templateInfo = null;
		switch(template){
//			case Succession:
//
//				templateInfo = new PrecedenceInfo();
//				Vector<ExtendedTrace> ambPrec = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Precedence, parameters.get(1), parameters.get(0));
//				templateInfo = new ResponseInfo();	
//				Vector<ExtendedTrace> ambResp = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Response, parameters.get(0), parameters.get(1));
//
//				//HashMap<Integer,Vector<Integer>> map = new HashMap<Integer, Vector<Integer>>();
//				//int traceNum = 0;
//				//for(ExtendedTrace ext : ambPrec){
//				//	map.put(traceNum, ext.getNonambi());
//				tracesWithCorrespondingEvents.addAll(ambPrec);
//				//	traceNum ++;
//				//}
//
//
//
//				int traceNum = 0;
//				for(ExtendedTrace ext : ambResp){
//					Vector<Integer> nonambig = tracesWithCorrespondingEvents.get(traceNum).getNonambi();
//					HashMap<Integer, Vector<Integer>> corresp = tracesWithCorrespondingEvents.get(traceNum).getCorrespcorrel();
//					HashMap<Integer, Vector<Integer>> currcorr = ext.getCorrespcorrel();
//					Vector<Integer> currnonamb = ext.getNonambi();
//					//int numnonamb = 0;
//					for(Integer nna : currnonamb){
//						//		if(!nonambig.contains(nna) && currcorr.get(nna).size()==1){
//						nonambig.add(nna);
//						corresp.put(nna, currcorr.get(nna));
//						//		}
//						//	numnonamb ++;
//					}
//					traceNum ++;
//				}
//
//				break;
//			case Alternate_Succession:
//
//				templateInfo = new PrecedenceInfo();
//				Vector<ExtendedTrace> ambPrec1 = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Alternate_Precedence, parameters.get(1), parameters.get(0));
//
//				templateInfo = new ResponseInfo();	
//				Vector<ExtendedTrace> ambResp1 = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Alternate_Response, parameters.get(0), parameters.get(1));
//
//				//HashMap<Integer,Vector<Integer>> map = new HashMap<Integer, Vector<Integer>>();
//				//int traceNum = 0;
//				//for(ExtendedTrace ext : ambPrec){
//				//	map.put(traceNum, ext.getNonambi());
//				tracesWithCorrespondingEvents.addAll(ambPrec1);
//				//	traceNum ++;
//				//}
//				traceNum = 0;
//				for(ExtendedTrace ext : ambResp1){
//					Vector<Integer> nonambig = tracesWithCorrespondingEvents.get(traceNum).getNonambi();
//					HashMap<Integer, Vector<Integer>> corresp = tracesWithCorrespondingEvents.get(traceNum).getCorrespcorrel();
//					HashMap<Integer, Vector<Integer>> currcorr = ext.getCorrespcorrel();
//					Vector<Integer> currnonamb = ext.getNonambi();
//					//int numnonamb = 0;
//					for(Integer nna : currnonamb){
//						//		if(!nonambig.contains(nna) && currcorr.get(nna).size()==1){
//						nonambig.add(nna);
//						corresp.put(nna, currcorr.get(nna));
//						//		}
//						//	numnonamb ++;
//					}
//					traceNum ++;
//				}
//
//
//				break;
//			case Chain_Succession:
//				templateInfo = new PrecedenceInfo();
//				Vector<ExtendedTrace> ambPrec2 = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Chain_Precedence, parameters.get(1), parameters.get(0));
//
//				templateInfo = new ResponseInfo();	
//				Vector<ExtendedTrace> ambResp2 = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Chain_Response, parameters.get(0), parameters.get(1));
//
//				//HashMap<Integer,Vector<Integer>> map = new HashMap<Integer, Vector<Integer>>();
//				//int traceNum = 0;
//				//for(ExtendedTrace ext : ambPrec){
//				//	map.put(traceNum, ext.getNonambi());
//				tracesWithCorrespondingEvents.addAll(ambPrec2);
//				//	traceNum ++;
//				//}
//				traceNum = 0;
//				for(ExtendedTrace ext : ambResp2){
//					Vector<Integer> nonambig = tracesWithCorrespondingEvents.get(traceNum).getNonambi();
//					HashMap<Integer, Vector<Integer>> corresp = tracesWithCorrespondingEvents.get(traceNum).getCorrespcorrel();
//					HashMap<Integer, Vector<Integer>> currcorr = ext.getCorrespcorrel();
//					Vector<Integer> currnonamb = ext.getNonambi();
//					//int numnonamb = 0;
//					for(Integer nna : currnonamb){
//						//		if(!nonambig.contains(nna) && currcorr.get(nna).size()==1){
//						nonambig.add(nna);
//						corresp.put(nna, currcorr.get(nna));
//						//		}
//						//	numnonamb ++;
//					}
//					traceNum ++;
//				}
//
//				break;
//			
			case Precedence:
			case Alternate_Precedence:
			case Chain_Precedence:
				templateInfo = new PrecedenceInfo();
				tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, activation, target);
				break;
			case Responded_Existence:
				templateInfo = new RespondedExistenceInfo();
				tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template,activation, target);
				break;
			case Response:
			case Alternate_Response:
			case Chain_Response:
				templateInfo = new ResponseInfo();	
				tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, activation, target);
				break;
//			case CoExistence:
//				templateInfo = new RespondedExistenceInfo();
//				Vector<ExtendedTrace> amb1 = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Responded_Existence, parameters.get(0), parameters.get(1));
//				Vector<ExtendedTrace> amb2 = templateInfo.getNonAmbiguousActivations(log, DeclareTemplate.Responded_Existence, parameters.get(1), parameters.get(0));
//
//				//HashMap<Integer,Vector<Integer>> map = new HashMap<Integer, Vector<Integer>>();
//				//int traceNum = 0;
//				//for(ExtendedTrace ext : ambPrec){
//				//	map.put(traceNum, ext.getNonambi());
//				tracesWithCorrespondingEvents.addAll(amb1);
//				//	traceNum ++;
//				//}
//				traceNum = 0;
//				for(ExtendedTrace ext : amb2){
//					Vector<Integer> nonambig = tracesWithCorrespondingEvents.get(traceNum).getNonambi();
//					HashMap<Integer, Vector<Integer>> corresp = tracesWithCorrespondingEvents.get(traceNum).getCorrespcorrel();
//
//					Vector<Integer> currnonamb = ext.getNonambi();
//					HashMap<Integer, Vector<Integer>> currcorr = ext.getCorrespcorrel();
//
//					//	int numnonamb = 0;
//					for(Integer nna : currnonamb){
//						//			if(!nonambig.contains(nna) && currcorr.get(nna).size()==1){
//						nonambig.add(nna);
//						corresp.put(nna, currcorr.get(nna));
//						//		}
//						//		numnonamb ++;
//					}
//					traceNum ++;
//				}
//

			//	break;
			case Not_CoExistence:
				templateInfo = new NotCoexistenceInfo();
				tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template, activation, target);
				break;
			case Not_Succession:
			case Not_Chain_Succession:
				templateInfo = new NegativeRelationInfo();
				tracesWithCorrespondingEvents = templateInfo.getNonAmbiguousActivations(log, template,activation, target);
				break;	
		}
		return tracesWithCorrespondingEvents;
	}

}

package org.processmining.plugins.declareminer;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.util.Utils;

public class CallableTemplate implements Callable {
	Long id = 0L;
	TemplateReplayer replayer = null;
	XLog log = null; 
	DeclareMinerInput input = null;
	DeclareTemplate template = null; 

	private Long min =new Long(-1) ,max=new Long(-1),tot=new Long(-1);
	private Integer memCount=0;	

	public CallableTemplate(Long id, TemplateReplayer replayer, XLog log, DeclareMinerInput input, DeclareTemplate template) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.replayer = replayer;
		this.log = log;
		this.input = input;
		this.template = template;
	}

	public TemplateThreadResult call() {

		boolean memoryCheck = input.isMemoryCheck();
		TemplateThreadResult result = null;

		Iterator<XTrace> trace_iter = log.iterator();
		while (trace_iter.hasNext()) {
			

				int tracePosition = 0;

				XTrace trace = trace_iter.next();
			
				if(trace.size()==0){
					replayer.process("emptyTrace",true, true, true);
				}else{
				
				String caseId = Utils.getCaseID(trace);

				Iterator<XEvent> event_iter = trace.iterator();
				// Iterate over all events
				while(event_iter.hasNext()) {
					boolean isTraceStart = false;
					boolean isTraceComplete = false;

					if(tracePosition == 0) {
						isTraceStart = true;
					}

					if(tracePosition == trace.size()-1) {
						isTraceComplete = true;
					}

					XEvent event = event_iter.next();

					String activity = XConceptExtension.instance().extractName(event);//+"-"+XLifecycleExtension.instance().extractTransition(event);

					if(input.getAprioriKnowledgeBasedCriteriaSet().contains(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes)){
						activity =  activity+"-"+XLifecycleExtension.instance().extractTransition(event);
					}
					//					boolean toReplay = replayer.isToReplay(activity, caseId, isTraceStart, isTraceComplete);

					//				if (toReplay) {
					//				replayer.addObservation(caseId);
					replayer.process(activity,isTraceStart, isTraceComplete, false);
					//			}

					tracePosition++;
				}
			}
			if(memoryCheck){
				long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				synchronized (tot) {
					tot+=memory;
				}
				synchronized (memCount) {
					memCount++;
				}
				synchronized(min){
					if (memory<min || min==-1)
						min=memory;
				}
				synchronized(max){
					if (memory>max)
						max=memory;
				}
				//System.out.println(min+" "+max+" "+tot+" "+memCount);
			}
		}
		if (memoryCheck)
			result = new TemplateThreadResult(replayer,min,max,tot,memCount);
		else
			result = new TemplateThreadResult(replayer);
		//System.out.println("THREAD "+this.id+" TIME "+threadWatcher.msecs());
		return result;
	}

}

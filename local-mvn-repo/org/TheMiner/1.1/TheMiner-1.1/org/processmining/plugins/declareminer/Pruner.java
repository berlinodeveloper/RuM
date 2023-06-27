package org.processmining.plugins.declareminer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.declareminer.apriori.FindItemSets;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.FrequentItemSetType;
import org.processmining.plugins.declareminer.templates.Absence2Info;
import org.processmining.plugins.declareminer.templates.AbsenceInfo;
import org.processmining.plugins.declareminer.templates.ChoiceInfo;
import org.processmining.plugins.declareminer.templates.CoexistenceInfo;
import org.processmining.plugins.declareminer.templates.Exactly1Info;
import org.processmining.plugins.declareminer.templates.ExclusiveChoiceInfo;
import org.processmining.plugins.declareminer.templates.ExistenceInfo;
import org.processmining.plugins.declareminer.templates.InitInfo;
import org.processmining.plugins.declareminer.templates.NegativeRelationInfo;
import org.processmining.plugins.declareminer.templates.NotCoexistenceInfo;
import org.processmining.plugins.declareminer.templates.PrecedenceInfo;
import org.processmining.plugins.declareminer.templates.ResponseInfo;
import org.processmining.plugins.declareminer.templates.SuccessionInfo;
import org.processmining.plugins.declareminer.templates.TemplateInfo;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.trace.constraints.Absence;
import org.processmining.plugins.declareminer.trace.constraints.Absence2;
import org.processmining.plugins.declareminer.trace.constraints.Absence3;
import org.processmining.plugins.declareminer.trace.constraints.AlternatePrecedence;
import org.processmining.plugins.declareminer.trace.constraints.AlternateResponse;
import org.processmining.plugins.declareminer.trace.constraints.AlternateSuccession;
import org.processmining.plugins.declareminer.trace.constraints.ChainPrecedence;
import org.processmining.plugins.declareminer.trace.constraints.ChainResponse;
import org.processmining.plugins.declareminer.trace.constraints.ChainSuccession;
import org.processmining.plugins.declareminer.trace.constraints.Choice;
import org.processmining.plugins.declareminer.trace.constraints.CoExistence;
import org.processmining.plugins.declareminer.trace.constraints.Exactly1;
import org.processmining.plugins.declareminer.trace.constraints.Exactly2;
import org.processmining.plugins.declareminer.trace.constraints.ExclusiveChoice;
import org.processmining.plugins.declareminer.trace.constraints.Existence;
import org.processmining.plugins.declareminer.trace.constraints.Existence2;
import org.processmining.plugins.declareminer.trace.constraints.Existence3;
import org.processmining.plugins.declareminer.trace.constraints.Init;
import org.processmining.plugins.declareminer.trace.constraints.Precedence;
import org.processmining.plugins.declareminer.trace.constraints.RespondedExistence;
import org.processmining.plugins.declareminer.trace.constraints.Response;
import org.processmining.plugins.declareminer.trace.constraints.Succession;
import org.processmining.plugins.declareminer.util.DeclareModel;
import org.processmining.plugins.declareminer.util.UnifiedLogger;
import org.processmining.plugins.declareminer.util.Utils;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DGraph;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphOrganicLayout;

public class Pruner {

	/**
	 * @param cpir2 
	 * @param log 
	 * @param posfrequentItemSetSupportMap 
	 * @param events 
	 * @param supp 
	 * @param args
	 */

	private PrintWriter pw;
	private UIPluginContext context;
	private XLog log;
	private DeclareMinerInput input;
	public static ExecutorService executor = null;
	

	public Pruner(UIPluginContext context, XLog log, DeclareMinerInput input, PrintWriter printWriter){
		this.context = context;
		this.log = log;
		this.pw = printWriter;
		this.input = input;
		this.executor = Executors.newFixedThreadPool(input.getThreadNumber());
	}

	private boolean optimized = false;
	private Map<DeclareTemplate, Vector<MetricsValues>> metricsValuesPerTemplate;
	private FindItemSets f;

	public DeclareMinerOutput prune(boolean hier, boolean trans,List<String> activityNameList,  Map<FrequentItemSetType, Map<Set<String>, Float>> frequentItemSetTypeFrequentItemSetSupportMap, Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap, Hashtable<?, ?> aprioriSupportValues, boolean verbose, DeclareMinerInput input){
		long prune_start = System.currentTimeMillis();
		if(pw!=null){
			pw.println("START PRUNING");
		}

		Watch pruneWatch = new Watch();
		pruneWatch.start();
		float support = input.getMinSupport()/100.f;
		float alpha = input.getAlpha()/100.f;
		f = new FindItemSets(log, input);

		Watch pruneLocalWatch = new Watch();
		pruneLocalWatch.start();
		HashMap<String, MetricsValues> metricsValues4precedence = null;
		HashMap<String, MetricsValues> metricsValues4response = null;

		metricsValuesPerTemplate = new HashMap<DeclareTemplate, Vector<MetricsValues>>();


		TemplateInfo templateInfo = null;
		Set<DeclareTemplate> selectedTemplateList = input.getSelectedDeclareTemplateSet();
		for(DeclareTemplate selectedDeclareTemplate : selectedTemplateList){
			startLogging(selectedDeclareTemplate, pruneLocalWatch);
			Vector<MetricsValues> metricsValues = new Vector<MetricsValues>();
			switch(selectedDeclareTemplate){
			case Succession:
			case Alternate_Succession:
			case Chain_Succession:
				templateInfo = new SuccessionInfo();
				break;
			case Choice:
				templateInfo = new ChoiceInfo();
				break;
			case Exclusive_Choice:
				templateInfo = new ExclusiveChoiceInfo();
				break;
			case Existence:
			case Existence2:
			case Existence3:
				templateInfo = new ExistenceInfo();
				break;
			case Init:
				templateInfo = new InitInfo();
				break;
			case Absence:
				templateInfo = new AbsenceInfo();
				break;
			case Absence2:
			case Absence3:
				templateInfo = new Absence2Info();
				break;
			case Exactly1:
			case Exactly2:
				templateInfo = new Exactly1Info();
				break;
			case Precedence:
			case Alternate_Precedence:
			case Chain_Precedence:
				templateInfo = new PrecedenceInfo();
				break;
			case Responded_Existence:
			case Response:
			case Alternate_Response:
			case Chain_Response:
				templateInfo = new ResponseInfo();
				break;
			case CoExistence:
				templateInfo = new CoexistenceInfo();
				break;
			case Not_CoExistence:
				templateInfo = new NotCoexistenceInfo();
				break;
			case Not_Succession:
			case Not_Chain_Succession:
				templateInfo = new NegativeRelationInfo();
				break;	
			}
			templateInfo.setMetricsValues4precedence(metricsValues4precedence);
			templateInfo.setMetricsValues4response(metricsValues4response);
			metricsValues = templateInfo.getMetrics(input,frequentItemSetTypeFrequentItemSetSupportMap,declareTemplateCandidateDispositionsMap, alpha, support, log, pw, selectedDeclareTemplate, context, verbose, f);
			metricsValues4precedence = templateInfo.getMetricsValues4precedence();
			metricsValues4response = templateInfo.getMetricsValues4response();
			metricsValuesPerTemplate.put(selectedDeclareTemplate, metricsValues);
			endLogging(selectedDeclareTemplate, pruneLocalWatch,metricsValues.size(), templateInfo.getNumberOfDiscoveredConstraints());

		}
		if(pw!=null){
			pw.println("END PRUNING - time: "+pruneWatch.msecs()+" msecs");
			pw.flush();
		}

		for(DeclareTemplate template : metricsValuesPerTemplate.keySet()){
			Vector<MetricsValues> toRemove = new Vector<MetricsValues>();
			for(MetricsValues values : metricsValuesPerTemplate.get(template)){
				if(values.getSupportRule()<support){
					toRemove.add(values);
				}
			}
			metricsValuesPerTemplate.get(template).removeAll(toRemove);
		}

		if(pw!=null){
			pw.println("START TIME MODEL GENERATION");
		}
		Watch modelGenerationWatch = new Watch();
		modelGenerationWatch.start();
		DeclareModelGenerator dmg = new DeclareModelGenerator();
		DeclareMinerOutput output = dmg.createModel(hier, trans,metricsValuesPerTemplate,log,input, new Vector<String>(), new Vector<String>(), new Vector<String>());
		//output = DeclareModelGenerator.createModel(output, 0, 0, 0, 0);
		DeclareModelGenerator.layout(output.getModel().getView(), output.getModel().getModel());
		output.setMinSupport(support);
		output.setExtend(false);
		output.setAlpha((int)alpha);
		if(pw!=null){
			pw.println("END TIME MODEL GENERATION - time: "+modelGenerationWatch.msecs()+" msecs");
			pw.println("START TIME INFORMATION EVALUATION");
		}
		long prune_end = System.currentTimeMillis();
		try {
			UnifiedLogger.log(String.valueOf(prune_end - prune_start));
		} catch (IOException e) {
			System.out.println("Couldn't write to unified log file!!! - " + UnifiedLogger.unified_log_path);
		}
		return output;
	}

	public DeclareMinerOutput fastPrune(boolean hier, boolean trans, final XLog log, final DeclareMinerInput input, Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap) {
		long prune_start = System.currentTimeMillis();
		long min = -1 ,max= -1,tot=-1;
		int memCount=0;
		
		int stampaNumero = 0;
		DeclareModel model = new DeclareModel();
		List<TemplateReplayer> replayers = new ArrayList<TemplateReplayer>();
		////	Set<DeclareTemplate> selectedTemplateList = input.getSelectedDeclareTemplateSet();
		// Filter constraints based on support and mininum given support after processing
		double minSupportPercent = ((double)input.minSupport) / 100.0;
		boolean alpha = (input.alpha == 100);
		boolean memoryCheck = input.isMemoryCheck();

		
		// Generate DeclareModel
		DeclareModelGenerator generator = new DeclareModelGenerator();
		// Add all the replayers
		List<DeclareTemplate> templates = new ArrayList<DeclareTemplate>();
		for (DeclareTemplate template: declareTemplateCandidateDispositionsMap.keySet()) {
			replayers.add((TemplateReplayer) replayerForTemplate(template,declareTemplateCandidateDispositionsMap));
			templates.add(template);
		};

		List<CallableTemplate> futureList = new ArrayList<CallableTemplate>();
		long i = 1;	
		int templN = 0;
		for (final TemplateReplayer fReplayer : replayers) {

			CallableTemplate callableTemplate = new CallableTemplate((long)i, fReplayer, log, input,templates.get(templN));
            futureList.add(callableTemplate);
			templN++;			            
            i++;
		}
		
        List<Future<TemplateThreadResult>> futures;
		try {
			futures = executor.invokeAll((Collection<? extends Callable<TemplateThreadResult>>) futureList);
	        for (Future<TemplateThreadResult> future : futures) {
	        	//System.out.println("logSize: "+log.size());
	        	future.get().getReplayer().updateModel(model, log.size());
	        	if(memoryCheck){
		        	min = future.get().getMin();
		        	max = future.get().getMax();
		        	tot = future.get().getTot();
		        	memCount = future.get().getCounter();
	        	}
				//System.out.println(future.get().getMin()+" "+future.get().getMin()+" "+future.get().getTot());
			}
			try {
				if(memoryCheck && UnifiedLogger.unified_memory_log_path!=null){
					double avg = (double)(tot/memCount);
					UnifiedLogger.logMemory(min, max, avg);
				}
			} catch (IOException e) {
				System.out.println("Couldn't write to unified log file!!! - " + UnifiedLogger.unified_memory_log_path);
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
		
		executor.shutdown();
		
		model.setReplayers(replayers);
		
		DeclareModel ruleSupportFiltered = DeclareModel.filterOnRuleSupport(model, minSupportPercent, alpha, log.size());
		//for(DeclareTemplate d : ruleSupportFiltered..)
		
		Map<DeclareTemplate, Vector<MetricsValues>> metricsVectorConstraint = createMetricsVectorConstraint(ruleSupportFiltered, log);
		DeclareMinerOutput output = generator.createModel(hier, trans, metricsVectorConstraint , log, input, new Vector<String>(), new Vector<String>(), new Vector<String>());
		//output.getModel().getModel().constraintDefinitionsCount();
		//ruleSupportFiltered = DeclareModel.filterOnRedundancy(ruleSupportFiltered,output,minSupportPercent, alpha, log.size());
		output.setDeclareModel(ruleSupportFiltered);
		long prune_end = System.currentTimeMillis();
		try {
			UnifiedLogger.log(String.valueOf(prune_end - prune_start));
		} catch (IOException e) {
			System.out.println("Couldn't write to unified log file!!! - " + UnifiedLogger.unified_log_path);
		}
		//System.out.println(ruleSupportFiltered.toString());
		return output;
	}

	public Map<DeclareTemplate, Vector<MetricsValues>> createMetricsVectorConstraint(DeclareModel model, XLog log) {
		Map<DeclareTemplate, Vector<MetricsValues>> mvc = new HashMap<DeclareTemplate, Vector<MetricsValues>>();
		Iterator<DeclareTemplate> template_iter = model.getConstraints().keySet().iterator();
		while(template_iter.hasNext()) {
			Vector<MetricsValues> vec_mv = new Vector<MetricsValues>();
			DeclareTemplate template = template_iter.next();
			Iterator<Pair<String, String>> pair_iter = model.getConstraints().get(template).keySet().iterator();
			while(pair_iter.hasNext()) {
				Pair<String, String> pair = pair_iter.next();
				MetricsValues mv = new MetricsValues();
				List<String> parameters = new ArrayList<String>();
				parameters.add(pair.getFirst());
				if (!pair.getFirst().equals(pair.getSecond())) {
					parameters.add(pair.getSecond());
				}
				mv.setParameters(parameters);
				mv.setTemplate(template);
				mv.setSupportRule(new Float(model.getConstraints().get(template).get(pair).get("support")));
				vec_mv.add(mv);
			}
			mvc.put(template, vec_mv);
		}
		return mvc;
	}

	public TemplateReplayer replayerForTemplate(DeclareTemplate template, Map<DeclareTemplate, List<List<String>>> declareTemplateCandidateDispositionsMap) {
		TemplateReplayer replayer = null;
		switch (template) {
		case Succession:
			replayer = new Succession(declareTemplateCandidateDispositionsMap,DeclareTemplate.Succession);
			break;
		case Not_Succession:
			replayer = new Succession(declareTemplateCandidateDispositionsMap,DeclareTemplate.Not_Succession);
			//((Succession)replayer).setTemplate(DeclareTemplate.Not_Succession);
			break;
		case Alternate_Succession:
			replayer = new AlternateSuccession(declareTemplateCandidateDispositionsMap);
			break;
		case Chain_Succession:
			replayer = new ChainSuccession(declareTemplateCandidateDispositionsMap,DeclareTemplate.Chain_Succession);
			break;
		case Not_Chain_Succession:
			replayer = new ChainSuccession(declareTemplateCandidateDispositionsMap,DeclareTemplate.Not_Chain_Succession);
			//((ChainSuccession)replayer).setTemplate(DeclareTemplate.Not_Chain_Succession);
			break;
		case Choice:
			replayer = new Choice(declareTemplateCandidateDispositionsMap);
			break;
		case Exclusive_Choice:
			replayer = new ExclusiveChoice(declareTemplateCandidateDispositionsMap);
			break;
		case Existence:
			replayer = new Existence(declareTemplateCandidateDispositionsMap);
			break;
		case Existence2:
			replayer = new Existence2(declareTemplateCandidateDispositionsMap);
			break;
		case Existence3:
			replayer = new Existence3(declareTemplateCandidateDispositionsMap);
			break;
		case Init:
			replayer = new Init(declareTemplateCandidateDispositionsMap);
			break;
		case Absence:
			replayer = new Absence(declareTemplateCandidateDispositionsMap);
			break;
		case Absence2:
			replayer = new Absence2(declareTemplateCandidateDispositionsMap);
			break;
		case Absence3:
			replayer = new Absence3(declareTemplateCandidateDispositionsMap);
			break;
		case Exactly1:
			replayer = new Exactly1(declareTemplateCandidateDispositionsMap);
			break;
		case Exactly2:
			replayer = new Exactly2(declareTemplateCandidateDispositionsMap);
			break;
		case Precedence:
			replayer = new Precedence(declareTemplateCandidateDispositionsMap);
			break;
		case Alternate_Precedence:
			replayer = new AlternatePrecedence(declareTemplateCandidateDispositionsMap);
			break;
		case Chain_Precedence:
			replayer = new ChainPrecedence(declareTemplateCandidateDispositionsMap);
			break;
		case Responded_Existence:
			replayer = new RespondedExistence(declareTemplateCandidateDispositionsMap);
			break;
		case Response:
			replayer = new Response(declareTemplateCandidateDispositionsMap);
			break;
		case Alternate_Response:
			replayer = new AlternateResponse(declareTemplateCandidateDispositionsMap);
			break;
		case Chain_Response:
			replayer = new ChainResponse(declareTemplateCandidateDispositionsMap);
			break;
		case CoExistence:
			replayer = new CoExistence(declareTemplateCandidateDispositionsMap,DeclareTemplate.CoExistence);
			break;
		case Not_CoExistence:
			replayer = new CoExistence(declareTemplateCandidateDispositionsMap,DeclareTemplate.Not_CoExistence);
		//	((CoExistence)replayer).setTemplate(DeclareTemplate.Not_CoExistence);
			break;
		default:
			break;	
		}

		return replayer;
	};


	private void endLogging(DeclareTemplate declareTemplate, Watch pruneLocalWatch, int numberOfCandidates, int numberOfDiscoveredConstraints) {
		if(pw!=null){
			pw.println(" ");
			pw.println("# time for pruning "+ declareTemplate.toString()+" = "+pruneLocalWatch.msecs()+" msecs");
			pw.println("# number of candidates "+declareTemplate.toString()+" constraints "+ numberOfCandidates);
			pw.println("# number of discovered "+declareTemplate.toString() +" constraints "+ numberOfDiscoveredConstraints);
			pw.println(" ");
		}

	}



	private void startLogging(DeclareTemplate declareTemplate, Watch pruneLocalWatch) {
		if(pw!=null){
			ConstraintTemplate constraintTemplate = input.getDeclareTemplateConstraintTemplateMap().get(declareTemplate);
			pw.println(" ");
			pw.println("******************************");
			pw.println("start pruning "+ constraintTemplate.getName());
			pw.println(constraintTemplate.getName()+ " rule: "+constraintTemplate.getText());
			pw.println(constraintTemplate.getName()+ " description: "+constraintTemplate.getDescription());
			pw.println("******************************");
			pw.println(" ");
			pruneLocalWatch.start();
		}
	}


	protected long getMaxTimeDistance(XLog log){
		return 0;
	}

	protected long getMinTimeDistance(XLog log){
		return 0;
	}

	protected long getAvgTimeDistance(XLog log){
		return 0;
	}

	public boolean isOptimized() {
		return optimized;
	}


	public void setOptimized(boolean optimized) {
		this.optimized = optimized;
	}


	public Map<DeclareTemplate, Vector<MetricsValues>> getMetrVectors() {
		return metricsValuesPerTemplate;
	}


	public void setMetrVectors(Map<DeclareTemplate, Vector<MetricsValues>> metrVectors) {
		this.metricsValuesPerTemplate = metrVectors;
	}

	public FindItemSets getF() {
		return f;
	}


	public void setF(FindItemSets f) {
		this.f = f;
	}


}



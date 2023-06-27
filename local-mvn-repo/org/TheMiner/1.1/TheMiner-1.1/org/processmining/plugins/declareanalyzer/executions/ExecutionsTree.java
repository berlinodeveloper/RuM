package org.processmining.plugins.declareanalyzer.executions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ltl2aut.automaton.Automaton;
import ltl2aut.automaton.Transition;
import ltl2aut.formula.DefaultParser;
import ltl2aut.formula.Formula;
import ltl2aut.formula.conjunction.ConjunctionFactory;
import ltl2aut.formula.conjunction.ConjunctionTreeLeaf;
import ltl2aut.formula.conjunction.ConjunctionTreeNode;
import ltl2aut.formula.conjunction.DefaultTreeFactory;
import ltl2aut.formula.conjunction.GroupedTreeConjunction;
import ltl2aut.formula.conjunction.TreeFactory;
import ltl2aut.ltl.SyntaxParserException;

import org.processmining.plugins.declareminer.ExecutableAutomaton;
import org.processmining.plugins.declareminer.PossibleNodes;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;


/**
 * This class is a tool to generate all the possible traces according to their
 * activations elements
 * 
 * @author Andrea Burattin
 * @author Fabrizio Maggi
 */
public class ExecutionsTree extends BinaryTree {

	private List<String> traceEvents;
	private List<Integer> traceIndexes;
	private ConstraintDefinition constraint;
	//	private Random rnd = new Random();
	private ExecutableAutomaton automaton;
	private Vector maximals = new Vector();
	private Set<Integer> activations;
	private Set<Integer> violations;
	private Set<Integer> fulfillments;
	private Set<Integer> conflicts;
	private Vector visitedViol = new Vector();
	private boolean completed;
	private List<Integer> filteredIndexes = new LinkedList<Integer>();
	private List<String> filteredEvents = new LinkedList<String>();

	/**
	 * 
	 * @param trace
	 * @param constraint
	 */
	public ExecutionsTree(List<String> traceEvents, List<Integer> traceIndexes, ConstraintDefinition constraint) {
		Vector<String> tr = new Vector<String>();
		for(String event: traceEvents){
			event = event.replace("null", "complete");
			tr.add(event);
		}
		this.traceEvents = tr;
		this.traceIndexes = traceIndexes;
		this.constraint = constraint;
		this.activations = new HashSet<Integer>();
		this.violations = new HashSet<Integer>();
		this.fulfillments = new HashSet<Integer>();
		this.conflicts = new HashSet<Integer>();
		this.completed = false;
		String formulaName = constraint.getName();
		String formula = constraint.getText();

		if (formulaName.equals("absence")){
			formula = "!( <> ( \"A\" ) )";
		}
		if (formulaName.equals("absence2")){
			formula = "! ( <> ( ( \"A\" /\\ X(<>(\"A\")) ) ) )";
		}
		if (formulaName.equals("absence3")){
			formula = "! ( <>( ( <> (\"A\") /\\ X( ( <> (\"A\") /\\  X ( <> ( \"A\" ) )) ) ) ) )";
		}
		if (formulaName.equals("alternate precedence")){
			formula = "( ( (! ( \"B\" ) U \"A\") \\/ ([](!(\"B\"))) ) /\\ [] ( ( \"B\" -> X( ( ( ! ( \"B\" ) U \"A\" )\\/([](!(\"B\"))) )) ) ) ) /\\ ! (\"B\" )";
		}
		if (formulaName.equals("alternate response")){
			formula = "( []( ( \"A\" -> X(( (! ( \"A\" )) U \"B\" ) )) ) )";
		}
		if (formulaName.equals("alternate succession")){
			formula = "(( []( ( \"A\" -> X(( ! ( \"A\" ) U \"B\" ) )) ) )) /\\ ( ( ( (! ( \"B\" ) U \"A\") \\/ ([](!(\"B\"))) ) /\\ [] ( ( \"B\" -> X( ( ( ! ( \"B\" ) U \"A\" )\\/([](!(\"B\"))) )) ) ) )  /\\ ! (\"B\" ))";
		}
		if (formulaName.equals("chain precedence")){
			formula = "[]( ( X( \"B\" ) -> \"A\") )/\\ ! (\"B\" )";
		}
		if (formulaName.equals("chain response")){
			formula = "[] ( ( \"A\" -> X( \"B\" ) ) )";
		}
		if (formulaName.equals("chain succession")){
			formula = "([]( ( \"A\" -> X( \"B\" ) ) )) /\\ ([]( ( X( \"B\" ) ->  \"A\") ) /\\ ! (\"B\" ))";
		}
		if (formulaName.contains("of")){
			formula = "(  <> ( \"A\" ) \\/ <>( \"B\" )  )";
		}
		if (formulaName.equals("co-existence")){
			formula = "( ( <>(\"A\") -> <>( \"B\" ) ) /\\ ( <>(\"B\") -> <>( \"A\" ) )  )";
		}
		if (formulaName.equals("exactly1")){
			formula = "(  <> (\"A\") /\\ ! ( <> ( ( \"A\" /\\ X(<>(\"A\")) ) ) ) )";
		}
		if (formulaName.equals("exactly2")){
			formula = "( <> (\"A\" /\\ (\"A\" -> (X(<>(\"A\"))))) /\\  ! ( <>( \"A\" /\\ (\"A\" -> X( <>( \"A\" /\\ (\"A\" -> X ( <> ( \"A\" ) ))) ) ) ) ) )";
		}
		if (formulaName.equals("exclusive choice")){
			formula = "(  ( <>( \"A\" ) \\/ <>( \"B\" )  )  /\\ !( (  <>( \"A\" ) /\\ <>( \"B\" ) ) ) )";
		}
		if (formulaName.equals("existence")){
			formula = "( <> ( \"A\" ) )";
		}
		if (formulaName.equals("existence2")){
			formula = "<> ( ( \"A\" /\\ X(<>(\"A\")) ) )";
		}
		if (formulaName.equals("existence3")){
			formula = "<>( \"A\" /\\ X(  <>( \"A\" /\\ X( <> \"A\" )) ))";
		}
		if (formulaName.equals("strong init")){
			formula = "( \"A\" )";
		}
		if (formulaName.equals("init")){
			formula = "( \"A\" )";
		}
		if (formulaName.equals("not chain succession")){
			formula = "[]( ( \"A\" -> !(X( \"B\" ) ) ))";
		}
		if (formulaName.equals("not co-existence")){
			formula = "(<>(\"A\")) -> (!(<>( \"B\" )))";
		}
		if (formulaName.equals("not succession")){
			formula = "[]( ( \"A\" -> !(<>( \"B\" ) ) ))";
		}
		if (formulaName.equals("precedence")){
			formula = "( ! (\"B\" ) U \"A\" ) \\/ ([](!(\"B\"))) /\\ ! (\"B\" )";
		}
		if (formulaName.equals("responded existence")){
			formula = "(( ( <>( \"A\" ) -> (<>( \"B\" ) )) ))";
		}
		if (formulaName.equals("response")){
			formula = "( []( ( \"A\" -> <>( \"B\" ) ) ))";
		}
		if (formulaName.equals("succession")){
			formula = "(( []( ( \"A\" -> <>( \"B\" ) ) ))) /\\ (( ! (\"B\" ) U \"A\" ) \\/ ([](!(\"B\"))) /\\ ! (\"B\" ))";
		}
		for (Parameter p : constraint.getParameters()) {
			int countB = 1;
			String actualParameter = "(\"";
			if(constraint.getBranches(p).size()==0){actualParameter = actualParameter + "EMPTY_PARAM" + "\")";}
			for (ActivityDefinition b : constraint.getBranches(p)) {
				String bname = b.getName();
				if(bname == null || bname.equals("") ){
					bname = "EMPTY_PARAM";
				}
				if (countB < constraint.branchesCount(p)) {
					if((!b.getName().contains("-assign")&&!b.getName().contains("-ate_abort")&&!b.getName().contains("-suspend")&&!b.getName().contains("-complete")&&!b.getName().contains("-autoskip")&&!b.getName().contains("-manualskip")&&!b.getName().contains("pi_abort")&&!b.getName().contains("-reassign")&&!b.getName().contains("-resume")&&!b.getName().contains("-schedule")&&!b.getName().contains("-start")&&!b.getName().contains("-unknown")&&!b.getName().contains("-withdraw"))&&(!b.getName().contains("<center>assign")&&!b.getName().contains("<center>ate_abort")&&!b.getName().contains("<center>suspend")&&!b.getName().contains("<center>complete")&&!b.getName().contains("<center>autoskip")&&!b.getName().contains("<center>manualskip")&&!b.getName().contains("<center>pi_abort")&&!b.getName().contains("<center>reassign")&&!b.getName().contains("<center>resume")&&!b.getName().contains("<center>schedule")&&!b.getName().contains("<center>start")&&!b.getName().contains("<center>unknown")&&!b.getName().contains("<center>withdraw"))){
						actualParameter = actualParameter + b.getName()+"-complete" + "\"||\"";
					}else{
						actualParameter = actualParameter + b.getName() + "\"||\"";
					}

				} else {
					if((!b.getName().contains("-assign")&&!b.getName().contains("-ate_abort")&&!b.getName().contains("-suspend")&&!b.getName().contains("-complete")&&!b.getName().contains("-autoskip")&&!b.getName().contains("-manualskip")&&!b.getName().contains("pi_abort")&&!b.getName().contains("-reassign")&&!b.getName().contains("-resume")&&!b.getName().contains("-schedule")&&!b.getName().contains("-start")&&!b.getName().contains("-unknown")&&!b.getName().contains("-withdraw"))&&(!b.getName().contains("<center>assign")&&!b.getName().contains("<center>ate_abort")&&!b.getName().contains("<center>suspend")&&!b.getName().contains("<center>complete")&&!b.getName().contains("<center>autoskip")&&!b.getName().contains("<center>manualskip")&&!b.getName().contains("<center>pi_abort")&&!b.getName().contains("<center>reassign")&&!b.getName().contains("<center>resume")&&!b.getName().contains("<center>schedule")&&!b.getName().contains("<center>start")&&!b.getName().contains("<center>unknown")&&!b.getName().contains("<center>withdraw"))){					
						actualParameter = actualParameter + b.getName()+"-complete" + "\")";
					}else{
						actualParameter = actualParameter + b.getName() + "\")";	
					}
				}
				countB++;
			}
			actualParameter = actualParameter.toLowerCase();
			formula = formula.replace("\"" + p.getName() + "\"", actualParameter);
		}

		String currentF = formula;
		currentF = currentF.replace("/\\ event==COMPLETE", "\"");
		currentF = currentF.replace("/\\ event==complete", "\"");
		currentF = currentF.replace("activity==", "\"");
		currentF = currentF.replace("_O", "X");
		currentF = currentF.replace("U_", "U");
		currentF = currentF.replace("<->", "=");

		List<Formula> formulaeParsed = new ArrayList<Formula>();
		try {
			formulaeParsed.add(new DefaultParser(currentF).parse());
		} catch (SyntaxParserException e) {
			e.printStackTrace();
		}
		TreeFactory<ConjunctionTreeNode, ConjunctionTreeLeaf> treeFactory = DefaultTreeFactory.getInstance();
		ConjunctionFactory<? extends GroupedTreeConjunction> conjunctionFactory = GroupedTreeConjunction
				.getFactory(treeFactory);
		GroupedTreeConjunction conjunction = conjunctionFactory.instance(formulaeParsed);
		Automaton pAut = conjunction.getAutomaton().op.reduce();
		automaton = new ExecutableAutomaton(pAut);

		process();
	}

	/**
	 * Method to generate all the possible combinations of traces
	 */
	public void process() {
		// tree building
		for (Integer event : traceIndexes) {
			if(isActivation(traceEvents.get(event),event)){
				activations.add(event);
			}
		}
		Vector toSkip = new Vector();
		Vector violated = new Vector();

		Vector alphabet = new Vector();

		//	boolean notSingViol = false;
		for(Parameter p : constraint.getParameters()){
			if(constraint.getBranches(p).size()==0){
				alphabet.add("empty_param");
			}else{

				ActivityDefinition b = constraint.getBranches(p).iterator().next();
				if((!b.getName().contains("-assign")&&!b.getName().contains("-ate_abort")&&!b.getName().contains("-suspend")&&!b.getName().contains("-complete")&&!b.getName().contains("-autoskip")&&!b.getName().contains("-manualskip")&&!b.getName().contains("pi_abort")&&!b.getName().contains("-reassign")&&!b.getName().contains("-resume")&&!b.getName().contains("-schedule")&&!b.getName().contains("-start")&&!b.getName().contains("-unknown")&&!b.getName().contains("-withdraw"))&&(!b.getName().contains("<center>assign")&&!b.getName().contains("<center>ate_abort")&&!b.getName().contains("<center>suspend")&&!b.getName().contains("<center>complete")&&!b.getName().contains("<center>autoskip")&&!b.getName().contains("<center>manualskip")&&!b.getName().contains("<center>pi_abort")&&!b.getName().contains("<center>reassign")&&!b.getName().contains("<center>resume")&&!b.getName().contains("<center>schedule")&&!b.getName().contains("<center>start")&&!b.getName().contains("<center>unknown")&&!b.getName().contains("<center>withdraw"))){
					alphabet.add(b.getName().toLowerCase()+"-complete");
				}else{
					alphabet.add(b.getName().toLowerCase());
				}
			}
		}

		filteredIndexes = new LinkedList<Integer>();
		filteredEvents = new LinkedList<String>();
		for(Integer e : traceIndexes){
			if(alphabet.contains(traceEvents.get(e))){
				filteredIndexes.add(e);
				filteredEvents.add(traceEvents.get(e));
			}
		}

		//	for(Integer event : activations){
		//		List<Integer> traceStr = new LinkedList<Integer>();
		//		for(Integer e : traceIndexes){
		//			if(!activations.contains(e) || e.equals(event)){
		//				traceStr.add(e);
		//			}
		//		}
		//		if(!isViolation(traceStr, true)){
		//			notSingViol = true;
		//			break;
		//		}
		//	}



		/*if(constraint.getName().contains("chain response")){
			for(Integer e : traceIndexes){
				if(activations.contains(e)){
					if(traceEvents.get(e+1).equals((String)alphabet.get(1))){
						fulfillments.add(e);
					}else{
						violations.add(e);
					}
				}
			}
		}else if(constraint.getName().contains("chain precedence")){
			for(Integer e : traceIndexes){
				if(activations.contains(e)){
					if(e!=0){
						if(traceEvents.get(e-1).equals((String)alphabet.get(0))){
							fulfillments.add(e);
						}else{
							violations.add(e);
						}
					}else{
						violated.add(e);
					}
				}
			}
		}else if(constraint.getName().contains("chain succession")){
			for(Integer e : traceIndexes){
				if(traceEvents.get(e).equals((String)alphabet.get(1))){
					if(e!=0){
						if(traceEvents.get(e-1).equals((String)alphabet.get(0))){
							fulfillments.add(e);
						}else{
							violations.add(e);
						}
					}else{
						violated.add(e);
					}
				}
				if(traceEvents.get(e).equals((String)alphabet.get(0))){
					if(traceEvents.get(e+1).equals((String)alphabet.get(1))){
						fulfillments.add(e);
					}else{
						violations.add(e);
					}
				}
			}*/
		if(constraint.getName().contains("chain")){
			filteredIndexes = traceIndexes;
			boolean viol = isViolation(traceIndexes, true);
			if(viol){
				visitedViol.add(traceEvents);
				visit(traceIndexes, toSkip);
			}else{
				maximals.add(traceIndexes);
			}


		}else{
			boolean viol = isViolation(filteredIndexes, true);
			if(viol){
				visitedViol.add(filteredEvents);
				visit(filteredIndexes, toSkip);
				Vector newMaximals = new Vector();
				for(Object max : maximals){
					List<Integer> maxv = (List<Integer>)max;
					List<Integer> currentTrace = new LinkedList<Integer>();
					for(Integer e : traceIndexes){
						if(!filteredIndexes.contains(e)){
							currentTrace.add(e);
						}else{
							if(maxv.contains(e)){
								currentTrace.add(e);
							}
						}
					}
					newMaximals.add(currentTrace);
				}
				maximals = newMaximals;
			}else{
				maximals.add(traceIndexes);
			}


		}			//	}else{
		//		List<Integer> traceStr = new LinkedList<Integer>();
		//		for(Integer e : traceIndexes){
		//			if(!activations.contains(e)){
		//				traceStr.add(e);
		//			}
		//		}
		//		maximals.add(traceStr);
		//	}


		//if(!violInstance){
		//	for (Integer event : traceIndexes) {
		//		if(isActivation(traceEvents.get(event),event)){
		//			fulfillments.add(event);
		//			activations.add(event);
		//		}

		//	}
		//	}else{

		//	for (Integer event : traceIndexes) {
		//		if(traceIndexes.indexOf(event)==traceIndexes.size()-1){
		//			completed = true;
		//		}
		//		if (isActivation(traceEvents.get(event), event)) {
		//			addActivation(event);
		//			activations.add(event);
		//		} else {
		//			addEvent(event);
		//		}
		//		checkViolations();
		//	}

		// stats calculation
		//		Set<ExtendibleTrace> maximals = getMaximalTraces();
		if (maximals.size() > 0) {
			for (Integer a : activations) {
				int observations = 0;

				for (Object et : maximals) {
					if (((List<Integer>)et).contains(a)) {
						observations++;
					}
				}

				if (observations == maximals.size()) {
					fulfillments.add(a);
				} else if (observations == 0) {
					violations.add(a);
				} else {
					conflicts.add(a);
				}
			}
		}

	}


	public void visit(List<Integer> trace, Vector<Integer> toSkip) {
		Vector viols = new Vector();
		Vector violsTS = new Vector();
		for(Integer event : activations){
			if(!toSkip.contains(event)){

				List<Integer> currentTrace = new LinkedList<Integer>();
				currentTrace.addAll(filteredIndexes);
				Vector<Integer> currentToskip = new Vector<Integer>();
				currentToskip.addAll(toSkip);
				currentToskip.add(event);
				for(Integer e : currentToskip){
					currentTrace.remove((Object)e);
				}
				boolean viol = isViolation(currentTrace, true);
				if(viol){
					//visit(currentTrace, currentToskip);
					viols.add(currentTrace);
					violsTS.add(currentToskip);
				}else{
					if(!maximals.contains(currentTrace)){
						boolean found = false;
						boolean rem = false;
						Vector ve = new Vector();
						for(Object max : maximals){
							List<Integer> maxv = (List<Integer>)max;
							if(maxv.containsAll(currentTrace)){
								found = true;
							}
							if(currentTrace.containsAll(maxv)){
								ve.add(maxv);
								rem = true;
							}
						}
						if(!found){
							maximals.add(currentTrace);
						}
						if(rem){
							maximals.removeAll(ve);
						}
					}
				}

			}
		}
		int i = 0;
		for(Object violTrObj : viols){
			List<Integer> violTrace = (List<Integer>)violTrObj;
			Vector<Integer> currentToskip = (Vector<Integer>) violsTS.get(i);
			List<String> traceStr = new LinkedList<String>();
			for(Integer e : violTrace){
				traceStr.add(traceEvents.get(e));
			}
			if(!visitedViol.contains(traceStr)){
				visitedViol.add(traceStr);
				visit(violTrace, currentToskip);
			}
			i++;
		}
	}

	/**
	 * Get all the traces generated from the combination of the activations. The
	 * results are grouped into a map which "goes" from the size of the trace
	 * to the list of traces
	 *  
	 * @return
	 */
	public Vector getMaximalTraces() {
		//	HashMap<Integer, HashSet<ExtendibleTrace>> result = new HashMap<Integer, HashSet<ExtendibleTrace>>();
		//	Set<Node> leaves = getLeaves();

		//	Integer max = Integer.MIN_VALUE;
		//	for (Node n : leaves) {

		//		Integer size = n.value.getTrace().size();

		//		HashSet<ExtendibleTrace> list;
		//		if (result.containsKey(size)) {
		//			list = result.get(size);
		//		} else {
		//			list = new HashSet<ExtendibleTrace>();
		//		}
		//		list.add(n.value);

		//		result.put(size, list);
		//		max = Math.max(max, size);
		//	}

		/*		Set<ExtendibleTrace> maximals = result.get(max);
		Set<Integer> keysSet = result.keySet();
		keysSet.remove(max);
		ArrayList<Integer> keys = new ArrayList<Integer>(keysSet);
		Collections.sort(keys, Collections.reverseOrder());
		for (Integer i : keys) {
			Set<ExtendibleTrace> smaller = result.get(i);
			Vector toAdd = new Vector();
			for (ExtendibleTrace et : smaller) {
				// let's iterate through all the `et', traces which can be
				// maximal: it is necessary to check if the other maximals
				// contains all the events of et
				for (ExtendibleTrace m : maximals) {
					if (!et.isContained(m)) {
						toAdd.add(et);
					}
				}

			}
			for(Object obj:toAdd){
				maximals.add((ExtendibleTrace) obj);
			}
		}

		if (maximals == null) {
			return new HashSet<ExtendibleTrace>();
		} else {
			return maximals;
		}*/
		return maximals;
	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getActivations() {
		return activations;
	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getFulfillments() {
		return fulfillments;
	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getConflicts() {
		return conflicts;
	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getViolations() {
		return violations;
	}

	private boolean isViolation(List<Integer> traceInt, boolean completeTrace) {
		automaton.ini();
		boolean violated = true;
		boolean empty = true;
		int i = 0;
		for (String event : traceEvents) {
			if(traceInt.contains(i)){
				empty = false;
				violated = true;
				PossibleNodes current = null;
				current = automaton.currentState();
				for (Transition out : current.output()) {
					if (out.parses(event)) {
						violated = false;
						break;
					}
				}
				if (!violated) {
					automaton.next(event);
				} else {
					break;
				}
			}
			i++;
		}
		if(!automaton.currentState().isAccepting() && completeTrace) {
			violated = true;
		}
		if(empty && automaton.currentState().isAccepting()){
			violated = false;
		}
		return violated;
	}

	private boolean isActivation(String event, Integer position) {
		int paramCount = 0;
		for (Parameter p : constraint.getParameters()) {
			int countB = 1;
			String param = null;
			String actualParameter = "(\"";
			if(constraint.getBranches(p).size()==0){actualParameter = actualParameter + "EMPTY_PARAM" + "\")"; }
			for (ActivityDefinition b : constraint.getBranches(p)) {
				String bname = b.getName();
				if(bname == null || bname.equals("") ){
					bname = "EMPTY_PARAM";
				}
				if (countB < constraint.branchesCount(p)) {
					actualParameter = actualParameter + b.getName() + "\"||\"";
				} else {
					actualParameter = actualParameter + b.getName() + "\")";
					param = b.getName();
				}
				countB++;
			}
			if(param != null){
				param = param.toLowerCase();
			}else{
				param =  "empty_param";
			}


			if (constraint.getName().startsWith("absence") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("absence2") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("absence3") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().contains("of") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("exactly1") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("exactly2") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("exclusive choice") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("existence") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("existence2") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("existence3") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("strong init") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("init") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("co-existence") && param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("responded existence") && param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 0)) {
				return true;
			}
			if (constraint.getName().startsWith("precedence") && param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 1)) {
				return true;
			}
			if (constraint.getName().startsWith("response") && param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 0)) {
				return true;
			}
			if (constraint.getName().startsWith("succession")&& param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("alternate precedence")&& param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 1)) {
				return true;
			}
			if (constraint.getName().startsWith("alternate response")&& param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 0)) {
				return true;
			}
			if (constraint.getName().startsWith("alternate succession")&& param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("chain precedence") && param.replace("-complete", "").equals(event.replace("-complete", ""))&& (paramCount == 1)) {
				return true;
			}
			if (constraint.getName().startsWith("chain response")&& param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 0)) {
				return true;
			}
			if (constraint.getName().startsWith("chain succession")&& param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("not co-existence")&& param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("not succession")&& param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("not chain succession")&& param.replace("-complete", "").equals(event.replace("-complete", ""))) {
				return true;
			}
			if (constraint.getName().startsWith("not response") && param.replace("-complete", "").equals(event.replace("-complete", ""))&& (paramCount == 0)) {
				return true;
			}
			if (constraint.getName().startsWith("not chain response") && param.replace("-complete", "").equals(event.replace("-complete", ""))&& (paramCount == 0)) {
				return true;
			}
			if (constraint.getName().startsWith("not precedence")&& param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 1)) {
				return true;
			}
			if (constraint.getName().startsWith("not chain precedence")&& param.replace("-complete", "").equals(event.replace("-complete", "")) && (paramCount == 1)) {
				return true;
			}
			paramCount++;
		}
		return false;
	}

	/*private void checkViolations() {
	Set<Node> leaves = getLeaves();
	for (Node l : leaves) {
		List<Integer> traceIndexes = l.value.getTrace();
		List<String> trace = new LinkedList<String>();

		for (Integer i : traceIndexes) {
			trace.add(traceEvents.get(i));

		}
		boolean vio = isViolation(trace, null ,completed);

		l.value.setViolation(vio);
	}
} */

	private void addActivation(Integer event) {
		Set<Node> leaves = getLeaves();
		for (Node l : leaves) {
			addLeftLeaf(l, new ExtendibleTrace(l.value));
			addRightLeaf(l, l.value.appendToNew(event, true));
		}
	}

	private void addEvent(Integer event) {
		Set<Node> leaves = getLeaves();
		for (Node l : leaves) {
			l.value = l.value.appendToNew(event, false);
		}
	}
}

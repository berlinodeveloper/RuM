package org.processmining.plugins.declare2ltl;

import java.util.ArrayList;
import java.util.List;
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
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.templates.LTLFormula;

public class BehavioralVacuityDetector {
	
	private static Vector<Vector<String>> activatingPaths = new Vector<Vector<String>>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Formula> formulaeParsed = new ArrayList<Formula>();
		String formula = LTLFormula.getFormulaByTemplate(DeclareTemplate.Response);
		boolean a = false;
		boolean b = false;
		boolean aUb = false;
		try {
			formulaeParsed.add(new DefaultParser(formula).parse());
			TreeFactory<ConjunctionTreeNode, ConjunctionTreeLeaf> treeFactory = DefaultTreeFactory.getInstance();
			ConjunctionFactory<? extends GroupedTreeConjunction> conjunctionFactory = GroupedTreeConjunction
					.getFactory(treeFactory);
			GroupedTreeConjunction conjunction = conjunctionFactory.instance(formulaeParsed);
			Automaton aut = conjunction.getAutomaton();
			Vector<String> trace = new Vector<String>();
			visit(aut, trace);
		} catch (SyntaxParserException e) {
			e.printStackTrace();
		}

	}

	private static void visit(Automaton aut, Vector<String> trace){
		ExecutableAutomaton execAut = new ExecutableAutomaton(aut);
		execAut.ini();
		for(String label : trace){
			execAut.next(label);
		}
		PossibleNodes current = execAut.currentState();
		if(!current.isAccepting()){
			activatingPaths.add(trace);
		}
		current.get(0).getId();
		for (Transition out : current.output()) {
			trace = new Vector<String>();
			trace.add(out.getPositiveLabel());
			visit(aut, trace);
			for(String negativeLabel : out.getNegativeLabels()){
				trace = new Vector<String>();
				trace.add(negativeLabel);
				visit(aut, trace);
			}
		}
	}

}

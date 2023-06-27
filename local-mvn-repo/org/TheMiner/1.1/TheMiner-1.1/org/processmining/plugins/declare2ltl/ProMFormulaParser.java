package org.processmining.plugins.declare2ltl;

import ltl2aut.ltl.LTLFormula;
import ltl2aut.ltl.Parser;

class ProMFormulaParser extends Parser<LTLFormula>{

	public ProMFormulaParser(String ltl) {
		super(ltl, new ProMFormulaFactory());
	}

}

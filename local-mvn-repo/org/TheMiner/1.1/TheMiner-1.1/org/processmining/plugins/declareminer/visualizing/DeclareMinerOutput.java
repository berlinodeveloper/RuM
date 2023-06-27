package org.processmining.plugins.declareminer.visualizing;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.util.DeclareModel;


public class DeclareMinerOutput {
	HashMap<Integer,List<String>> visibleConstraintParametersMap = new HashMap<Integer,List<String>>();
	private DeclareMap model;
	private DeclareModel declare_model;
	private HashMap<Integer, DeclareTemplate> template;
	private HashMap /*String[]*/ parameters;
	private HashMap /*String*/ formula;
	private HashMap<Integer,Float> /*float*/ suppAntec;
	private HashMap<Integer,Float> /*float*/ supportConseq;
	private HashMap<Integer,Float> /*float*/ supportRule;
    private HashMap<Integer,Float> /*float*/ confidence;
    private HashMap<Integer,Float> /*float*/ CPIR;
    private HashMap<Integer,Float> /*float*/ I;
    private HashMap<Integer,Float> /*float*/ visiblesupportRule;
    private HashMap<Integer,Float> /*float*/ visibleconfidence;
    private HashMap<Integer,Float> /*float*/ visibleCPIR;
    private HashMap<Integer,Float> /*float*/ visibleI;
    private HashMap<Object,Integer> activations;
    private HashMap<Object,Integer> fulfillments;
    private HashMap<Object,Integer> violations;
    private HashMap<Object,Integer> conflicts;
    private HashMap<Object,Long> maxTD;
    private HashMap<Object,Long> minTD;
    private HashMap<Object,Long> avgTD;
    private HashMap<Object,Long> stdDevTD;
    private HashMap<Integer,List<String>> constraintParametersMap = new HashMap<Integer,List<String>>();
    private HashMap<Object,double[]> constraintIdTimeInstancesMap;
    private HashMap actSupp;
    private HashMap blnc;
    private HashMap mappingAdCd = new HashMap();
    private float minSupport;
    private DeclareMinerInput input;
    private Vector<ConstraintDefinition> allDiscoveredConstraints;
    private  HashMap<Integer, String> allActivities;
    private Vector<Integer> transitiveClosureResponseConstraints;
    private Vector<Integer> transitiveClosureRespondedExistenceConstraints;
    private Vector<Integer> transitiveClosurePrecedenceConstraints;
    private Vector<Integer> transitiveClosureSuccessionConstraints;
    private Vector<Integer> transitiveClosureCoexistenceConstraints;
    private boolean extend;
    private XLog log;
    private int alpha;
    private boolean hier = true;
    private boolean trans = true;
    
    
    
    
	public boolean isHier() {
		return hier;
	}
	public void setHier(boolean hier) {
		this.hier = hier;
	}
	public boolean isTrans() {
		return trans;
	}
	public void setTrans(boolean trans) {
		this.trans = trans;
	}
	public DeclareMap getModel() {
		return model;
	}
	public void setModel(DeclareMap model) {
		this.model = model;
	}
	public void setDeclareModel(DeclareModel ruleSupportFiltered) {
		this.declare_model = ruleSupportFiltered;
	}
	public DeclareModel getDeclareModel() {
		return declare_model;
	}
	public HashMap<Integer, DeclareTemplate> getTemplate() {
		return template;
	}
	public int getAlpha() {
		return alpha;
	}
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	
	public void setTemplate(HashMap<Integer, DeclareTemplate> map) {
		this.template = map;
	}
	public HashMap getParameters() {
		return parameters;
	}
	public void setParameters(HashMap parameters) {
		this.parameters = parameters;
	}
	public HashMap getFormula() {
		return formula;
	}
	public void setFormula(HashMap formula) {
		this.formula = formula;
	}
	public HashMap<Integer, Float> getSuppAntec() {
		return suppAntec;
	}
	public void setSuppAntec(HashMap<Integer, Float> suppAntec) {
		this.suppAntec = suppAntec;
	}
	public HashMap<Integer, Float> getSupportConseq() {
		return supportConseq;
	}
	public void setSupportConseq(HashMap<Integer, Float> supportConseq) {
		this.supportConseq = supportConseq;
	}
	public HashMap<Integer, Float> getSupportRule() {
		return supportRule;
	}
	public void setSupportRule(HashMap<Integer, Float> supportRule) {
		this.supportRule = supportRule;
	}
	public HashMap<Integer, Float> getConfidence() {
		return confidence;
	}
	public void setConfidence(HashMap<Integer, Float> confidence) {
		this.confidence = confidence;
	}
	public HashMap<Integer, Float> getCPIR() {
		return CPIR;
	}
	public void setCPIR(HashMap<Integer, Float> cPIR) {
		CPIR = cPIR;
	}
	public HashMap<Integer, Float> getI() {
		return I;
	}
	public void setI(HashMap<Integer, Float> i) {
		I = i;
	}
	public float getMinSupport() {
		return minSupport;
	}
	public void setMinSupport(float minSupport) {
		this.minSupport = minSupport;
	}
	public HashMap getMappingAdCd() {
		return mappingAdCd;
	}
	public void setMappingAdCd(HashMap mappingAdCd) {
		this.mappingAdCd = mappingAdCd;
	}
	public HashMap<Object,Float> getActSupp() {
		return actSupp;
	}
	public void setActSupp(HashMap<Integer,Float> actSupp) {
		this.actSupp = actSupp;
	}
	public HashMap<Object, Integer> getActivations() {
		return activations;
	}
	public void setActivations(HashMap<Object, Integer> activations) {
		this.activations = activations;
	}
	public HashMap<Object, Integer> getFulfillments() {
		return fulfillments;
	}
	public void setFulfillments(HashMap<Object, Integer> fulfillments) {
		this.fulfillments = fulfillments;
	}
	public HashMap<Object, Integer> getViolations() {
		return violations;
	}
	public void setViolations(HashMap<Object, Integer> violations) {
		this.violations = violations;
	}
	public HashMap<Object, Integer> getConflicts() {
		return conflicts;
	}
	public void setConflicts(HashMap<Object, Integer> conflicts) {
		this.conflicts = conflicts;
	}
	public HashMap<Object, Long> getMaxTD() {
		return maxTD;
	}
	public void setMaxTD(HashMap<Object, Long> maxTD) {
		this.maxTD = maxTD;
	}
	public HashMap<Object, Long> getMinTD() {
		return minTD;
	}
	public void setMinTD(HashMap<Object, Long> minTD) {
		this.minTD = minTD;
	}
	public HashMap<Object, Long> getAvgTD() {
		return avgTD;
	}
	public void setAvgTD(HashMap<Object, Long> avgTD) {
		this.avgTD = avgTD;
	}
	public HashMap<Object, Long> getStdDevTD() {
		return stdDevTD;
	}
	public void setStdDevTD(HashMap<Object, Long> stdDevTD) {
		this.stdDevTD = stdDevTD;
	}
	public HashMap getBlnc() {
		return blnc;
	}
	public void setBlnc(HashMap blnc) {
		this.blnc = blnc;
	}
	public HashMap<Object, double[]> getConstraintIdTimeInstancesMap() {
		return constraintIdTimeInstancesMap;
	}
	public void setConstraintIdTimeInstancesMap(HashMap<Object, double[]> constraintIdTimeInstancesMap) {
		this.constraintIdTimeInstancesMap = constraintIdTimeInstancesMap;
	}
	public DeclareMinerInput getInput() {
		return input;
	}
	public void setInput(DeclareMinerInput input) {
		this.input = input;
	}
	public Vector<ConstraintDefinition> getAllDiscoveredConstraints() {
		return allDiscoveredConstraints;
	}
	public void setAllDiscoveredConstraints(Vector<ConstraintDefinition> allDiscoveredConstraints) {
		this.allDiscoveredConstraints = allDiscoveredConstraints;
	}
	public HashMap<Integer, String> getAllActivities() {
		return allActivities;
	}
	public void setAllActivities( HashMap<Integer, String> iterable) {
		this.allActivities = iterable;
	}
	public Vector<Integer> getTransitiveClosureResponseConstraints() {
		return transitiveClosureResponseConstraints;
	}
	public void setTransitiveClosureResponseConstraints(Vector<Integer> transitiveClosureResponseConstraints) {
		this.transitiveClosureResponseConstraints = transitiveClosureResponseConstraints;
	}
	public Vector<Integer> getTransitiveClosureRespondedExistenceConstraints() {
		return transitiveClosureRespondedExistenceConstraints;
	}
	public void setTransitiveClosureRespondedExistenceConstraints(
			Vector<Integer> transitiveClosureRespondedExistenceConstraints) {
		this.transitiveClosureRespondedExistenceConstraints = transitiveClosureRespondedExistenceConstraints;
	}
	public Vector<Integer> getTransitiveClosurePrecedenceConstraints() {
		return transitiveClosurePrecedenceConstraints;
	}
	public void setTransitiveClosurePrecedenceConstraints(Vector<Integer> transitiveClosurePrecedenceConstraints) {
		this.transitiveClosurePrecedenceConstraints = transitiveClosurePrecedenceConstraints;
	}
	public Vector<Integer> getTransitiveClosureSuccessionConstraints() {
		return transitiveClosureSuccessionConstraints;
	}
	public void setTransitiveClosureSuccessionConstraints(Vector<Integer> transitiveClosureSuccessionConstraints) {
		this.transitiveClosureSuccessionConstraints = transitiveClosureSuccessionConstraints;
	}
	public Vector<Integer> getTransitiveClosureCoexistenceConstraints() {
		return transitiveClosureCoexistenceConstraints;
	}
	public void setTransitiveClosureCoexistenceConstraints(Vector<Integer> transitiveClosureCoexistenceConstraints) {
		this.transitiveClosureCoexistenceConstraints = transitiveClosureCoexistenceConstraints;
	}
	public boolean isExtend() {
		return extend;
	}
	public void setExtend(boolean extend) {
		this.extend = extend;
	}
	public HashMap<Integer, List<String>> getConstraintParametersMap() {
		return constraintParametersMap;
	}
	public void setConstraintParametersMap(HashMap<Integer, List<String>> constraintParametersMap) {
		this.constraintParametersMap = constraintParametersMap;
	}
	public XLog getLog() {
		return log;
	}
	public void setLog(XLog log) {
		this.log = log;
	}
	public HashMap<Integer, Float> getVisiblesupportRule() {
		return visiblesupportRule;
	}
	public void setVisiblesupportRule(HashMap<Integer, Float> visiblesupportRule) {
		this.visiblesupportRule = visiblesupportRule;
	}
	public HashMap<Integer, Float> getVisibleconfidence() {
		return visibleconfidence;
	}
	public void setVisibleconfidence(HashMap<Integer, Float> visibleconfidence) {
		this.visibleconfidence = visibleconfidence;
	}
	public HashMap<Integer, Float> getVisibleCPIR() {
		return visibleCPIR;
	}
	public void setVisibleCPIR(HashMap<Integer, Float> visibleCPIR) {
		this.visibleCPIR = visibleCPIR;
	}
	public HashMap<Integer, Float> getVisibleI() {
		return visibleI;
	}
	public void setVisibleI(HashMap<Integer, Float> visibleI) {
		this.visibleI = visibleI;
	}
	public HashMap<Integer, List<String>> getVisibleConstraintParametersMap() {
		return visibleConstraintParametersMap;
	}
	public void setVisibleConstraintParametersMap(HashMap<Integer, List<String>> visibleConstraintParametersMap) {
		this.visibleConstraintParametersMap = visibleConstraintParametersMap;
	}
	
	
	

}

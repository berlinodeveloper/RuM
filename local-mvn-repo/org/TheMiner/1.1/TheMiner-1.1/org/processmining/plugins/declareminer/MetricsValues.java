package org.processmining.plugins.declareminer;

import java.util.List;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;


public class MetricsValues {
	private DeclareTemplate template;
	private List<String> parameters;
	private String formula;
	private float suppAntec;
	private float supportConseq;
	private float suppAntecSat;
	private float supportConseqSat;
	private float supportRule;
    private float confidence;
    private float CPIR;
    private float I;

    
	public float getSuppAntecSat() {
		return suppAntecSat;
	}
	public void setSuppAntecSat(float suppAntecSat) {
		this.suppAntecSat = suppAntecSat;
	}
	public float getSupportConseqSat() {
		return supportConseqSat;
	}
	public void setSupportConseqSat(float supportConseqSat) {
		this.supportConseqSat = supportConseqSat;
	}
	
	public DeclareTemplate getTemplate() {
		return template;
	}
	public void setTemplate(DeclareTemplate template) {
		this.template = template;
	}
	public List<String> getParameters() {
		return parameters;
	}
	public void setParameters(List<String> list) {
		this.parameters = list;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public float getSuppAntec() {
		return suppAntec;
	}
	public void setSuppAntec(float suppAntec) {
		this.suppAntec = suppAntec;
	}
	public float getSupportConseq() {
		return supportConseq;
	}
	public void setSupportConseq(float supportConseq) {
		this.supportConseq = supportConseq;
	}
	public float getSupportRule() {
		return supportRule;
	}
	public void setSupportRule(float supportRule) {
		this.supportRule = supportRule;
	}
	public float getConfidence() {
		return confidence;
	}
	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}
	public float getCPIR() {
		return CPIR;
	}
	public void setCPIR(float cPIR) {
		CPIR = cPIR;
	}
	public float getI() {
		return I;
	}
	public void setI(float i) {
		I = i;
	}
	
}

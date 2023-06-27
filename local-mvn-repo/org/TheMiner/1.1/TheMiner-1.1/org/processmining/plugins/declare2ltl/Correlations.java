package org.processmining.plugins.declare2ltl;

import java.util.HashMap;

import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;


public class Correlations {
	private HashMap<ConstraintDefinition, HashMap<String, Double>> correlationSupport  = new HashMap<ConstraintDefinition, HashMap<String, Double>>();
	private HashMap<ConstraintDefinition, HashMap<String, Double>> correlationDisambiguation  = new HashMap<ConstraintDefinition, HashMap<String, Double>>();
	private DeclareMinerOutput complModel;
	private HashMap<String,String> mapping = new HashMap<String, String>(); 
	
	
	public HashMap<String, String> getMapping() {
		return mapping;
	}
	public void setMapping(HashMap<String, String> mapping) {
		this.mapping = mapping;
	}
	public HashMap<ConstraintDefinition, HashMap<String, Double>> getCorrelationSupport() {
		return correlationSupport;
	}
	public void setCorrelationSupport(HashMap<ConstraintDefinition, HashMap<String, Double>> correlationSupport) {
		this.correlationSupport = correlationSupport;
	}
	public HashMap<ConstraintDefinition, HashMap<String, Double>> getCorrelationDisambiguation() {
		return correlationDisambiguation;
	}
	public void setCorrelationDisambiguation(HashMap<ConstraintDefinition, HashMap<String, Double>> correlationDisambiguation) {
		this.correlationDisambiguation = correlationDisambiguation;
	}
	public DeclareMinerOutput getComplModel() {
		return complModel;
	}
	public void setComplModel(DeclareMinerOutput complModel) {
		this.complModel = complModel;
	}
	
	
	
	
}

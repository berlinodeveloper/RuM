package org.processmining.plugins.dataaware;

import java.util.HashMap;


public class DataAwareDeclareMinerOutput {

	private DataDeclareMap model;
	private HashMap<Integer,Float> /*float*/ violRatioBeforeEvent;
    private HashMap<Integer,Float> /*float*/ fulfillRatioBeforeEvent;
    private HashMap<Integer,Float> /*float*/ violRatioAfterEvent;
    private HashMap<Integer,Float> /*float*/ fulfillRatioAfterEvent;
    private HashMap<Integer,Float> /*float*/ violRatioBeforeTrace;
    private HashMap<Integer,Float> /*float*/ fulfillRatioBeforeTrace;
    private HashMap<Integer,Float> /*float*/ violRatioAfterTrace;
    private HashMap<Integer,Float> /*float*/ fulfillRatioAfterTrace;
	public DataDeclareMap getModel() {
		return model;
	}
	public void setModel(DataDeclareMap model) {
		this.model = model;
	}
	public HashMap<Integer, Float> getViolRatioBeforeEvent() {
		return violRatioBeforeEvent;
	}
	public void setViolRatioBeforeEvent(HashMap<Integer, Float> violRatioBeforeEvent) {
		this.violRatioBeforeEvent = violRatioBeforeEvent;
	}
	public HashMap<Integer, Float> getFulfillRatioBeforeEvent() {
		return fulfillRatioBeforeEvent;
	}
	public void setFulfillRatioBeforeEvent(HashMap<Integer, Float> fulfillRatioBeforeEvent) {
		this.fulfillRatioBeforeEvent = fulfillRatioBeforeEvent;
	}
	public HashMap<Integer, Float> getViolRatioAfterEvent() {
		return violRatioAfterEvent;
	}
	public void setViolRatioAfterEvent(HashMap<Integer, Float> violRatioAfterEvent) {
		this.violRatioAfterEvent = violRatioAfterEvent;
	}
	public HashMap<Integer, Float> getFulfillRatioAfterEvent() {
		return fulfillRatioAfterEvent;
	}
	public void setFulfillRatioAfterEvent(HashMap<Integer, Float> fulfillRatioAfterEvent) {
		this.fulfillRatioAfterEvent = fulfillRatioAfterEvent;
	}
	public HashMap<Integer, Float> getViolRatioBeforeTrace() {
		return violRatioBeforeTrace;
	}
	public void setViolRatioBeforeTrace(HashMap<Integer, Float> violRatioBeforeTrace) {
		this.violRatioBeforeTrace = violRatioBeforeTrace;
	}
	public HashMap<Integer, Float> getFulfillRatioBeforeTrace() {
		return fulfillRatioBeforeTrace;
	}
	public void setFulfillRatioBeforeTrace(HashMap<Integer, Float> fulfillRatioBeforeTrace) {
		this.fulfillRatioBeforeTrace = fulfillRatioBeforeTrace;
	}
	public HashMap<Integer, Float> getViolRatioAfterTrace() {
		return violRatioAfterTrace;
	}
	public void setViolRatioAfterTrace(HashMap<Integer, Float> violRatioAfterTrace) {
		this.violRatioAfterTrace = violRatioAfterTrace;
	}
	public HashMap<Integer, Float> getFulfillRatioAfterTrace() {
		return fulfillRatioAfterTrace;
	}
	public void setFulfillRatioAfterTrace(HashMap<Integer, Float> fulfillRatioAfterTrace) {
		this.fulfillRatioAfterTrace = fulfillRatioAfterTrace;
	}
    

    

}

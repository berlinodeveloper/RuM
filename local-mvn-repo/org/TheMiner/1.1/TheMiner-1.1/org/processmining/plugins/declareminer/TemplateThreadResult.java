package org.processmining.plugins.declareminer;

import java.util.HashMap;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;

public class TemplateThreadResult {
	private TemplateReplayer replayer;
	private long tot;
	private long min;
	private long max;
	private int counter;
	
	public TemplateThreadResult(TemplateReplayer replayer){
		super();
		this.replayer = replayer;
		this.tot=-1;
		this.min=-1;
		this.max=-1;
		this.counter = -1;
	}
	
	public TemplateThreadResult(TemplateReplayer replayer, 
			long min, long max, long tot, int counter) {
		super();
		this.replayer = replayer;
		this.tot= tot;
		this.min= min;
		this.max=max;
		this.counter=counter;
	}

	public TemplateReplayer getReplayer() {
		return replayer;
	}

	public long getTot() {
		return tot;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	public int getCounter() {
		return counter;
	}
	
	

}

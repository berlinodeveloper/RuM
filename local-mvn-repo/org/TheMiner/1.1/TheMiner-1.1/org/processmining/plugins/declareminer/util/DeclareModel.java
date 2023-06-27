package org.processmining.plugins.declareminer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.trace.TemplateReplayer;
import org.processmining.plugins.declareminer.trace.constraints.ChainSuccession;
import org.processmining.plugins.declareminer.trace.constraints.CoExistence;
import org.processmining.plugins.declareminer.trace.constraints.Succession;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

public class DeclareModel {

	private boolean hasTraces = false;
	private Double maxFulfillment = Double.MIN_VALUE;
	private Double maxSatisfiedTraces = Double.MIN_VALUE;
	private List<TemplateReplayer> replayers;
	private HashSet<String> activities = new HashSet<String>();
	private HashMap<DeclareTemplate, HashMap<Pair<String, String>, HashMap<String, Double>>> constraints = new HashMap<>();

	public DeclareModel() {
		constraints.put(DeclareTemplate.Alternate_Precedence,	new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Alternate_Response,		new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Alternate_Succession,	new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Chain_Precedence,		new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Chain_Response,			new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Chain_Succession,		new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.CoExistence,			new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Not_CoExistence,		new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Not_Succession,			new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Not_Chain_Succession,	new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Precedence,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Responded_Existence,	new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Response,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Succession,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Existence,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Existence2,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Existence3,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Exactly1,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Exactly2,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Absence,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Absence2,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Absence3,				new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Choice,					new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Init,					new HashMap<Pair<String, String>, HashMap<String, Double>>());
		constraints.put(DeclareTemplate.Exclusive_Choice,		new HashMap<Pair<String, String>, HashMap<String, Double>>());
	}

	public void addResponse(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Response, activityA, activityB, activations, fulfillments);
	}

	public void addResponse(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Response, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addExistence(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Existence, activityA, activityA, activations, fulfillments);
	}

	public void addExistence(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Existence, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addChoices(String activityA, String activityB, Double activations, Double fulfillments) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.Choice, activityA, activityB, activations, fulfillments);
		} else {
			add(DeclareTemplate.Choice, activityB, activityA, activations, fulfillments);
		}
	}

	public void addChoices(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.Choice, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		} else {
			add(DeclareTemplate.Choice, activityB, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		}
	}

	public void addExclusiveChoices(String activityA, String activityB, Double activations, Double fulfillments) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.Exclusive_Choice, activityA, activityB, activations, fulfillments);
		} else {
			add(DeclareTemplate.Exclusive_Choice, activityB, activityA, activations, fulfillments);
		}
	}

	public void addExclusiveChoices(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.Exclusive_Choice, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		} else {
			add(DeclareTemplate.Exclusive_Choice, activityB, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		}
	}

	public void addAbsence(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Absence, activityA, activityA, activations, fulfillments);
	}

	public void addAbsence(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Absence, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addInit(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Init, activityA, activityA, activations, fulfillments);
	}

	public void addInit(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Init, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addAbsence2(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Absence2, activityA, activityA, activations, fulfillments);
	}

	public void addAbsence2(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Absence2, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addAbsence3(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Absence3, activityA, activityA, activations, fulfillments);
	}

	public void addAbsence3(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Absence3, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addExistence2(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Existence2, activityA, activityA, activations, fulfillments);
	}

	public void addExistence2(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Existence2, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addExistence3(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Existence3, activityA, activityA, activations, fulfillments);
	}

	public void addExistence3(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Existence3, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addExactly1(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Exactly1, activityA, activityA, activations, fulfillments);
	}

	public void addExactly1(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Exactly1, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addExactly2(String activityA, Double activations, Double fulfillments) {
		add(DeclareTemplate.Exactly2, activityA, activityA, activations, fulfillments);
	}

	public void addExactly2(String activityA, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Exactly2, activityA, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addNotResponse(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Not_Succession, activityA, activityB, activations, fulfillments);
	}

	public void addNotResponse(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Not_Succession, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addSuccession(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Succession, activityA, activityB, activations, fulfillments);
	}

	public void addSuccession(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Succession, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addNotSuccession(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Not_Succession, activityA, activityB, activations, fulfillments);
	}

	public void addNotSuccession(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Not_Succession, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addPrecedence(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Precedence, activityA, activityB, activations, fulfillments);
	}

	public void addPrecedence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Precedence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addNotPrecedence(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Not_Precedence, activityA, activityB, activations, fulfillments);
	}

	public void addNotPrecedence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Not_Precedence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addRespondedExistence(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Responded_Existence, activityA, activityB, activations, fulfillments);
	}

	public void addRespondedExistence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Responded_Existence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addNotCoExistence(String activityA, String activityB, Double activations, Double fulfillments) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.Not_CoExistence, activityA, activityB, activations, fulfillments);
		} else {
			add(DeclareTemplate.Not_CoExistence, activityB, activityA, activations, fulfillments);
		}
	}

	public void addNotCoExistence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.Not_CoExistence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		} else {
			add(DeclareTemplate.Not_CoExistence, activityB, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		}
	}

	public void addCoExistence(String activityA, String activityB, Double activations, Double fulfillments) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.CoExistence, activityA, activityB, activations, fulfillments);
		} else {
			add(DeclareTemplate.CoExistence, activityB, activityA, activations, fulfillments);
		}
	}

	public void addCoExistence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		if (activityA.compareTo(activityB) <= 0) {
			add(DeclareTemplate.CoExistence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		} else {
			add(DeclareTemplate.CoExistence, activityB, activityA, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
		}
	}

	public void addChainResponse(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Chain_Response, activityA, activityB, activations, fulfillments);
	}

	public void addChainResponse(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Chain_Response, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addChainSuccession(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Chain_Succession, activityA, activityB, activations, fulfillments);
	}

	public void addChainSuccession(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Chain_Succession, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addNotChainSuccession(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Not_Chain_Succession, activityA, activityB, activations, fulfillments);
	}

	public void addNotChainSuccession(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Not_Chain_Succession, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addChainPrecedence(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Chain_Precedence, activityA, activityB, activations, fulfillments);
	}

	public void addChainPrecedence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Chain_Precedence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addAlternateResponse(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Alternate_Response, activityA, activityB, activations, fulfillments);
	}

	public void addAlternateResponse(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Alternate_Response, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public void addAlternateSuccession(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Alternate_Succession, activityA, activityB, activations, fulfillments);
	}

	public void addAlternateSuccession(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Alternate_Succession, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}


	public void addAlternatePrecedence(String activityA, String activityB, Double activations, Double fulfillments) {
		add(DeclareTemplate.Alternate_Precedence, activityA, activityB, activations, fulfillments);
	}

	public void addAlternatePrecedence(String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		add(DeclareTemplate.Alternate_Precedence, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
	}

	public HashSet<String> getActivities() {
		return activities;
	}

	private Double getValue(DeclareTemplate template, String activityA, String activityB, String field) {
		if (constraints.containsKey(template)) {
			if (constraints.get(template).containsKey(new Pair<String, String>(activityA, activityB))) {
				if (constraints.get(template).get(new Pair<String, String>(activityA, activityB)).containsKey(field)) {
					return constraints.get(template).get(new Pair<String, String>(activityA, activityB)).get(field);
				}
			}
		}
		return 0.0;
	}

	private void add(DeclareTemplate template, String activityA, String activityB, Double activations, Double fulfillments) {
		if (constraints.containsKey(template)) {
			HashMap<Pair<String, String>, HashMap<String, Double>> constraint = constraints.get(template);

			Pair<String, String> k = new Pair<String, String>(activityA, activityB);
			HashMap<String, Double> v = new HashMap<String, Double>();
			v.put("activations", activations);
			v.put("fulfillments", fulfillments);
			constraint.put(k, v);

			constraints.put(template, constraint);

			activities.add(activityA);
			activities.add(activityB);

			maxFulfillment = Math.max(maxFulfillment, fulfillments);
			hasTraces = false;
		}
	}

	private void add(DeclareTemplate template, String activityA, String activityB, Integer completedTraces, Integer satisfiedTraces, Integer vacuouslySatisfiedTraces, Integer violatedTraces) {
		if (constraints.containsKey(template)) {
			HashMap<Pair<String, String>, HashMap<String, Double>> constraint = constraints.get(template);

			Pair<String, String> k = new Pair<String, String>(activityA, activityB);
			HashMap<String, Double> v = new HashMap<String, Double>();
			v.put("completedTraces", completedTraces.doubleValue());
			v.put("satisfiedTraces", satisfiedTraces.doubleValue());
			v.put("vacuouslySatisfiedTraces", vacuouslySatisfiedTraces.doubleValue());
			v.put("violatedTraces", violatedTraces.doubleValue());
			constraint.put(k, v);

			constraints.put(template, constraint);

			activities.add(activityA);
			activities.add(activityB);

			maxSatisfiedTraces = Math.max(maxSatisfiedTraces, satisfiedTraces.doubleValue());
			hasTraces = true;
		}
	}

	private Double getActivations(DeclareTemplate constraintName, String activityA, String activityB) {
		return getValue(constraintName, activityA, activityB, "activations");
	}

	private Double getFulfillment(DeclareTemplate constraintName, String activityA, String activityB) {
		return getValue(constraintName, activityA, activityB, "fulfillments");
	}

	private Integer getCompletedTraces(DeclareTemplate constraintName, String activityA, String activityB) {
		return getValue(constraintName, activityA, activityB, "completedTraces").intValue();
	}

	private Integer getSatisfiedTraces(DeclareTemplate constraintName, String activityA, String activityB) {
		return getValue(constraintName, activityA, activityB, "satisfiedTraces").intValue();
	}

	private Integer getVacuouslySatisfiedTraces(DeclareTemplate constraintName, String activityA, String activityB) {
		return getValue(constraintName, activityA, activityB, "vacuouslySatisfiedTraces").intValue();
	}

	private Integer getViolatedTraces(DeclareTemplate constraintName, String activityA, String activityB) {
		return getValue(constraintName, activityA, activityB, "violatedTraces").intValue();
	}

	public int size() {
		int i = 0;
		for(DeclareTemplate constraintName : constraints.keySet()) {
			i += constraints.get(constraintName).size();
		}
		return i;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(DeclareTemplate constraintName : constraints.keySet()) {
			HashMap<Pair<String, String>, HashMap<String, Double>> constraint = constraints.get(constraintName);
			if (constraint.size() > 0) {
				sb.append("=== " + constraintName.toString().toUpperCase() + " === ("+ constraint.size() + " constraints)\n");

				for(Pair<String, String> r : constraint.keySet()) {
					//				Pair<Double, Double> vals = constraint.get(r);
					//				double fulfillRatio = (vals.getSecond() / vals.getFirst());
					sb.append(" - " + r /*+ ", " + constraint.get(r)*/ + "\n");// + "\t fulfil ratio: " + fulfillRatio + "\t vio ratio: " + (1-fulfillRatio) + "\n");
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public String toHTMLString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body style=\"background: #c2c2c2\">");
		for(DeclareTemplate constraintName : constraints.keySet()) {
			HashMap<Pair<String, String>, HashMap<String, Double>> constraint = constraints.get(constraintName);
			if (constraint.size() > 0) {
				sb.append("<p><span style=\"color: #0428E0;\"><strong>" + constraintName.toString().replace("_", " ") + "</strong></span><br/>");
				sb.append(""+ constraint.size() + " constraints</p>");

				sb.append("<ul>");
				for(Pair<String, String> r : constraint.keySet()) {
					sb.append("<li>");
					sb.append(r + " <span style=\"color: #ffffff\">" + constraint.get(r) + "</span>");
					sb.append("</li>");
				}
				sb.append("</ul>");
			}
		}
		sb.append("<body></html>");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public void addSupport(DeclareTemplate template, Pair<String, String> pair, Double support) {
		constraints.get(template).get(pair).put("support", support);
	}

	public void writeConstraintsToFileWithSupport(File file) {
		FileWriter file_writer = null;
		try {
			file_writer = new FileWriter(file.getAbsoluteFile(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter buffered_writer = new BufferedWriter(file_writer);
		Iterator<DeclareTemplate> template_iter = constraints.keySet().iterator();
		while(template_iter.hasNext()) {
			DeclareTemplate template = template_iter.next();
			Iterator<Pair<String, String>> pair_iter = constraints.get(template).keySet().iterator();
			while(pair_iter.hasNext()) {
				Pair<String, String> pair = pair_iter.next();
				Double support = constraints.get(template).get(pair).get("support");
				String line = template.name() + "(" + pair.getFirst() + ", " + pair.getSecond() + "): " + String.valueOf(support);

				try {
					buffered_writer.write(line);
					buffered_writer.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			buffered_writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeConstraintsAsHumanReadable(File file, int totalTraces) {
		FileWriter file_writer = null;
		try {
			file_writer = new FileWriter(file.getAbsoluteFile(), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter buffered_writer = new BufferedWriter(file_writer);
		Iterator<DeclareTemplate> template_iter = constraints.keySet().iterator();
		while(template_iter.hasNext()) {
			DeclareTemplate template = template_iter.next();
			Iterator<Pair<String, String>> pair_iter = constraints.get(template).keySet().iterator();
			while(pair_iter.hasNext()) {
				Pair<String, String> pair = pair_iter.next();
				try {
					// Write general info
					String human_readable = humanReadableSentence(template.name(), pair);
					buffered_writer.write(human_readable);
					buffered_writer.newLine();
					buffered_writer.newLine();
					// Write witnesses
					Double satisfied = constraints.get(template).get(pair).get("satisfiedTraces");

					String witnesses_sentence = witnessesSentence(satisfied, totalTraces);
					buffered_writer.write(witnesses_sentence);
					buffered_writer.newLine();
					// Write counter examples
					Double violated = constraints.get(template).get(pair).get("violatedTraces");
					String counter_examples_sentence = counterExamplesSentence(violated, totalTraces);
					buffered_writer.write(counter_examples_sentence);
					buffered_writer.newLine();
					// Write vacuous cases
					Double vacuouslySatisfied = constraints.get(template).get(pair).get("vacuouslySatisfiedTraces");
					String vacous_cases_sentence = vacousCasesSentence(vacuouslySatisfied, totalTraces);
					buffered_writer.write(vacous_cases_sentence);
					buffered_writer.newLine();
					buffered_writer.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			buffered_writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String humanReadableSentence(String template_name, Pair<String, String> pair) {
		String sentence;
		String first = pair.getFirst();
		String second = pair.getSecond();
		switch(template_name) {
		case("Absence"):
			sentence = String.format("'%s' is never executed.", first);
		break;
		case("Absence2"):
			sentence = String.format("'%s' happens at most once.", first);
		break;
		case("Absence3"):
			sentence = String.format("'%s' happens at most twice.", first);
		break;
		case("AlternatePrecedence"):
			sentence = String.format("'%s' does not happen before '%s'. After '%s' happens, it does not happen before the next '%s' again.", second, first, second, first);
		break;
		case("AlternateResponse"):
			sentence = String.format(
					"After each '%s' at least one '%s' is executed. " +
							"Another '%s' is executed again only after the first '%s'.",
							first, second, first, second
					);
		break;
		case("AlternateSuccession"):
			sentence = String.format(
					"After each '%s' at least one '%s' is executed. Another '%s' is executed again only after the first '%s'." +
							"'%s' does not happen before '%s'. After it happens, it does not happen before the next '%s' again.",
							first, second, first, second, second, first, first
					);
		break;
		case("ChainPrecedence"):
			sentence = String.format("'%s' is executed only directly after '%s'.", second, first);
		break;
		case("ChainResponse"):
			sentence = String.format("After '%s' the next one is '%s'.", first, second);
		break;
		case("ChainSuccession"):
			sentence = String.format("'%s' and '%s' happen only next to each other.", first, second);
		break;
		case("Choice"):
			sentence = String.format("At least one of '%s' and '%s' is executed.", first, second);
		break;
		case("CoExistence"):
			sentence = String.format("'%s' and '%s' are both executed or none of them is executed.",first, second);
		break;
		case("Exactly1"):
			sentence = String.format("'%s' happens exactly once.", first);
		break;
		case("Exactly2"):
			sentence = String.format("'%s' happens exactly twice.", first);
		break;
		case("ExclusiveChoice"):
			sentence = String.format("One of '%s' and '%s' happens but not both.", first, second);
		break;
		case("Existence"):
			sentence = String.format("'%s' is executed at least once.", first);
		break;
		case("Existence2"):
			sentence = String.format("'%s' is executed at least twice.", first);
		break;
		case("Existence3"):
			sentence = String.format("'%s' is executed at least three times.", first);
		break;
		case("Init"):
			sentence = String.format("'%s' is the first activity to be executed.", first);
		break;
		case("Precedence"):
			sentence = String.format("'%s' is preceded by '%s'. '%s' happens only after '%s' had happened.", second, first, second, first);
		break;
		case("RespondedExistence"):
			sentence = String.format("If '%s' happens (at least once) then '%s' has (at least once) happened before or happens after '%s'.", first, second, first);
		break;
		case("Response"):
			sentence = String.format("Whenever activity '%s' is executed, activity '%s' is eventually executed afterwards.", first, second);
		break;
		case("Succession"):
			sentence = String.format(
					"Whenever activity '%s' is executed, activity '%s' is eventually executed afterwards." +
							"'%s' is preceded by '%s'. '%s' happens only after '%s' has happened.",
							first, second, second, first, second, first
					);
		break;
		default: sentence = "Unknown template: " + template_name;
		break;
		}
		return sentence;
	}

	public String tracesSentence(String type, Double typeCount, int totalCount) {
		return String.format("%s (%.2f%% of cases, %d cases in total)", type, (double)typeCount/(double)totalCount * 100, typeCount.intValue());
	}

	public String witnessesSentence(Double satisfiedCount, int totalCount) {
		return tracesSentence("witnesses", satisfiedCount, totalCount);
	}

	public String counterExamplesSentence(Double violatedCount, int totalCount) {
		return tracesSentence("counter examples", violatedCount, totalCount);
	}

	public String vacousCasesSentence(Double vacousCount, int totalCount) {
		return tracesSentence("vacuous cases", vacousCount, totalCount);
	}

	public static int TP(DeclareModel candidate, DeclareModel goldStandard) {
		int i = 0;
		for (DeclareTemplate template : candidate.constraints.keySet()) {
			for (Pair<String, String> constraint : candidate.constraints.get(template).keySet()) {
				if(template.equals(DeclareTemplate.CoExistence) ||
						template.equals(DeclareTemplate.Not_CoExistence) ||
						template.equals(DeclareTemplate.Choice) ||
						template.equals(DeclareTemplate.Exclusive_Choice)) {
					Pair<String, String> inverse = new Pair<String, String>(constraint.getSecond(), constraint.getFirst());
					if (goldStandard.constraints.get(template).containsKey(constraint)||goldStandard.constraints.get(template).containsKey(inverse)) {
						i++;
					}
				} else if (goldStandard.constraints.get(template).containsKey(constraint)) {
					i++;
				}
			}
		}
		return i;
	}

	public static int FP(DeclareModel candidate, DeclareModel goldStandard) {
		int i = 0;
		for (DeclareTemplate template : candidate.constraints.keySet()) {
			for (Pair<String, String> constraint : candidate.constraints.get(template).keySet()) {

				if(template.equals(DeclareTemplate.CoExistence) ||
						template.equals(DeclareTemplate.Not_CoExistence) ||
						template.equals(DeclareTemplate.Choice) ||
						template.equals(DeclareTemplate.Exclusive_Choice)){
					Pair<String, String> inverse = new Pair<String, String>(constraint.getSecond(), constraint.getFirst());
					if(!goldStandard.constraints.get(template).containsKey(constraint) && !goldStandard.constraints.get(template).containsKey(inverse)){
						i++;
					}
				} else if (!goldStandard.constraints.get(template).containsKey(constraint)) {
					i++;
				}
			}
		}
		return i;
	}

	public static int FN(DeclareModel candidate, DeclareModel goldStandard) {
		int i = 0;
		for (DeclareTemplate template : goldStandard.constraints.keySet()) {
			for (Pair<String, String> constraint : goldStandard.constraints.get(template).keySet()) {
				if(template.equals(DeclareTemplate.CoExistence) ||
						template.equals(DeclareTemplate.Not_CoExistence) ||
						template.equals(DeclareTemplate.Choice) ||
						template.equals(DeclareTemplate.Exclusive_Choice)){
					Pair<String, String> inverse = new Pair<String, String>(constraint.getSecond(), constraint.getFirst());
					if(!candidate.constraints.get(template).containsKey(constraint) && !candidate.constraints.get(template).containsKey(inverse)){
						i++;
					}
				} else if (!candidate.constraints.get(template).containsKey(constraint)) {
					i++;
				}

			}
		}
		return i;
	}

	public Double getCost(DeclareTemplate template, String ActA, String ActB) {
		if (hasTraces) {
			return getSatisfiedTraces(template, ActA, ActB).doubleValue() / maxSatisfiedTraces;
		} else {
			return getFulfillment(template, ActA, ActB) / maxFulfillment;
		}
	}

	public static DeclareModel getTopConstraints(final DeclareModel model, int k) {
		if (model == null || k <= 0) {
			return null;
		}
		// define our priority queue
		PriorityQueue<Triple<DeclareTemplate, String, String>> p = new PriorityQueue<Triple<DeclareTemplate, String, String>>(
				10,
				new Comparator<Triple<DeclareTemplate, String, String>>() {
					@Override
					public int compare(Triple<DeclareTemplate, String, String> A, Triple<DeclareTemplate, String, String> B) {
						Double costA = model.getCost(A.getFirst(), A.getSecond(), A.getThird());
						Double costB = model.getCost(B.getFirst(), B.getSecond(), B.getThird());
						return costB.compareTo(costA);
					}
				});

		// populate our priority queue
		for (DeclareTemplate constraint : model.constraints.keySet()) {
			for (Pair<String, String> instance : model.constraints.get(constraint).keySet()) {
				String activityA = instance.getFirst();
				String activityB = instance.getSecond();
				p.add(new Triple<DeclareTemplate, String, String>(constraint, activityA, activityB));
			}
		}

		// extract constraints from the priority queue
		DeclareModel filtered = new DeclareModel();
		filtered.hasTraces = model.hasTraces;
		Double lowestCost = 0.0;
		while(k-- > 0) {
			Triple<DeclareTemplate, String, String> element = p.poll();
			if (element != null) {
				DeclareTemplate constraintName = element.getFirst();
				String activityA = element.getSecond();
				String activityB = element.getThird();
				if (model.hasTraces) {
					filtered.add(
							constraintName, activityA, activityB,
							model.getCompletedTraces(constraintName, activityA, activityB),
							model.getSatisfiedTraces(constraintName, activityA, activityB),
							model.getVacuouslySatisfiedTraces(constraintName, activityA, activityB),
							model.getViolatedTraces(constraintName, activityA, activityB));
				} else {
					filtered.add(constraintName, activityA, activityB,
							model.getActivations(constraintName, activityA, activityB),
							model.getFulfillment(constraintName, activityA, activityB));
				}
				lowestCost = model.getCost(constraintName, activityA, activityB);
			}
		}
		// extract all other constraints with the same cost
		boolean checkAnother = false;
		do {
			Triple<DeclareTemplate, String, String> element = p.poll();
			if (element != null) {
				DeclareTemplate constraintName = element.getFirst();
				String activityA = element.getSecond();
				String activityB = element.getThird();
				Double currentCost = model.getCost(constraintName, activityA, activityB);
				if (currentCost == lowestCost) {
					if (model.hasTraces) {
						filtered.add(
								constraintName, activityA, activityB,
								model.getCompletedTraces(constraintName, activityA, activityB),
								model.getSatisfiedTraces(constraintName, activityA, activityB),
								model.getVacuouslySatisfiedTraces(constraintName, activityA, activityB),
								model.getViolatedTraces(constraintName, activityA, activityB));
					} else {
						filtered.add(constraintName, activityA, activityB,
								model.getActivations(constraintName, activityA, activityB),
								model.getFulfillment(constraintName, activityA, activityB));
					}
					checkAnother = true;
				} else {
					checkAnother = false;
				}
			}
		} while(checkAnother);
		return filtered;
	}

	public static DeclareModel filterOnFulfillmentRatio(DeclareModel model, double minFulfillmentRatio) {
		if (model == null || minFulfillmentRatio > 1 || minFulfillmentRatio < 0) {
			return null;
		}
		DeclareModel filtered = new DeclareModel();
		for (DeclareTemplate constraint : model.constraints.keySet()) {
			for (Pair<String, String> instance : model.constraints.get(constraint).keySet()) {
				String activityA = instance.getFirst();
				String activityB = instance.getSecond();
				Double activations = model.getActivations(constraint, activityA, activityB);
				Double fulfillments = model.getFulfillment(constraint, activityA, activityB);
				if (fulfillments / activations >= minFulfillmentRatio) {
					filtered.add(constraint, activityA, activityB, activations, fulfillments);
				}
			}
		}
		return filtered;
	}

	public static DeclareModel filterOnRuleSupport(DeclareModel model, double minSupport, boolean alpha, int totalTraces) {
		if (model == null || minSupport > 1 || minSupport < 0) {
			return null;
		}
		DeclareModel filtered = new DeclareModel();
		for (DeclareTemplate constraint : model.constraints.keySet()) {
			for (Pair<String, String> instance : model.constraints.get(constraint).keySet()) {
				String activityA = instance.getFirst();
				String activityB = instance.getSecond();
				int completedTraces = model.getCompletedTraces(constraint, activityA, activityB);
				int satisfiedTraces = model.getSatisfiedTraces(constraint, activityA, activityB);
				int vacuouslySatisfiedTraces = model.getVacuouslySatisfiedTraces(constraint, activityA, activityB);
				int violatedTraces = model.getViolatedTraces(constraint, activityA, activityB);
				TemplateReplayer replayer = null;
				if(constraint.equals(DeclareTemplate.Not_Chain_Succession)){
					for(TemplateReplayer rep : model.replayers){
						if(rep instanceof ChainSuccession && ((ChainSuccession) rep).getTemplate().equals(DeclareTemplate.Not_Chain_Succession)){
							replayer =  rep;
						}
					}
					//System.out.println("activities: "+activityA+" "+ activityB);
					//System.out.println("replaglier: "+((ChainSuccession) replayer).getStronglyViolatedTraces().get(activityA));

					satisfiedTraces = 0;
					violatedTraces =  0;
					if(((ChainSuccession) replayer).getA_and_b_occur_but_never_in_sequence_Traces().containsKey(activityA) && ((ChainSuccession) replayer).getA_and_b_occur_but_never_in_sequence_Traces().get(activityA).containsKey(activityB)){
						satisfiedTraces = ((ChainSuccession) replayer).getA_and_b_occur_but_never_in_sequence_Traces().get(activityA).get(activityB);
					}
					//int strsatisfiedTraces = (((Succession) replayer).getStronglyViolatedTraces().get(activityA).get(activityB));
					//satisfiedTraces = (((Succession) replayer).getViolatedTraces().get(activityA).get(activityB));

					if(((ChainSuccession) replayer).getA_and_b_occur_always_in_sequence_Traces().containsKey(activityA) && ((ChainSuccession) replayer).getA_and_b_occur_always_in_sequence_Traces().get(activityA).containsKey(activityB)){
						if(((ChainSuccession) replayer).getA_and_b_occur_only_sometimes_in_sequence_Traces().containsKey(activityA) && ((ChainSuccession) replayer).getA_and_b_occur_only_sometimes_in_sequence_Traces().get(activityA).containsKey(activityB)){
							violatedTraces = (((ChainSuccession) replayer).getA_and_b_occur_always_in_sequence_Traces().get(activityA).get(activityB)) + ((ChainSuccession) replayer).getA_and_b_occur_only_sometimes_in_sequence_Traces().get(activityA).get(activityB);
						}
					}
					vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;				
				}
				if(constraint.equals(DeclareTemplate.Not_Succession)){
					for(TemplateReplayer rep : model.replayers){
						if(rep instanceof Succession && ((Succession) rep).getTemplate().equals(DeclareTemplate.Not_Succession)){
							replayer =  rep;
						}
					}
					satisfiedTraces = 0;
					violatedTraces =  0;
					if(((Succession) replayer).getA_and_b_occur_never_a_event_b().containsKey(activityA) && ((Succession) replayer).getA_and_b_occur_never_a_event_b().get(activityA).containsKey(activityB)){
						satisfiedTraces = ((Succession) replayer).getA_and_b_occur_never_a_event_b().get(activityA).get(activityB);
					}
					//int strsatisfiedTraces = (((Succession) replayer).getStronglyViolatedTraces().get(activityA).get(activityB));
					//satisfiedTraces = (((Succession) replayer).getViolatedTraces().get(activityA).get(activityB));

					if(((Succession) replayer).getA_and_b_occur_always_a_event_b().containsKey(activityA) && ((Succession) replayer).getA_and_b_occur_always_a_event_b().get(activityA).containsKey(activityB)){
						if(((Succession) replayer).getA_and_b_occur_sometimes_a_event_b().containsKey(activityA) && ((Succession) replayer).getA_and_b_occur_sometimes_a_event_b().get(activityA).containsKey(activityB)){
							violatedTraces = (((Succession) replayer).getA_and_b_occur_always_a_event_b().get(activityA).get(activityB)) + ((Succession) replayer).getA_and_b_occur_sometimes_a_event_b().get(activityA).get(activityB);
						}
					}
					vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;				
				}


				Double support = null;
				if (alpha) {
					support = ((double)satisfiedTraces + (double)vacuouslySatisfiedTraces)/ (double)totalTraces;
				} else {
					support = ((double)satisfiedTraces) / (double)totalTraces;
				}

				if (support >= minSupport) {
					filtered.add(constraint, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
					filtered.addSupport(constraint, instance, support);
				}
			}
		}
		return filtered;
	}



	public static DeclareModel filterOnRedundancy(DeclareModel model, DeclareMinerOutput output, double minSupport, boolean alpha, int totalTraces) {
		if (model == null || minSupport > 1 || minSupport < 0) {
			return null;
		}
		DeclareModel filtered = new DeclareModel();
		for (DeclareTemplate constraint : model.constraints.keySet()) {
			for (Pair<String, String> instance : model.constraints.get(constraint).keySet()) {
				String activityA = instance.getFirst();
				String activityB = instance.getSecond();
				int completedTraces = model.getCompletedTraces(constraint, activityA, activityB);
				int satisfiedTraces = model.getSatisfiedTraces(constraint, activityA, activityB);
				int vacuouslySatisfiedTraces = model.getVacuouslySatisfiedTraces(constraint, activityA, activityB);
				int violatedTraces = model.getViolatedTraces(constraint, activityA, activityB);
				TemplateReplayer replayer = null;
				if(constraint.equals(DeclareTemplate.Not_Chain_Succession)){
					for(TemplateReplayer rep : model.replayers){
						if(rep instanceof ChainSuccession && ((ChainSuccession) rep).getTemplate().equals(DeclareTemplate.Not_Chain_Succession)){
							replayer =  rep;
						}
					}

					//System.out.println("activities: "+activityA+" "+ activityB);
					//System.out.println("replaglier: "+((ChainSuccession) replayer).getStronglyViolatedTraces().get(activityA));

					satisfiedTraces = 0;
					violatedTraces =  0;
					if(((ChainSuccession) replayer).getA_and_b_occur_but_never_in_sequence_Traces().containsKey(activityA) && ((ChainSuccession) replayer).getA_and_b_occur_but_never_in_sequence_Traces().get(activityA).containsKey(activityB)){
						satisfiedTraces = ((ChainSuccession) replayer).getA_and_b_occur_but_never_in_sequence_Traces().get(activityA).get(activityB);
					}
					//int strsatisfiedTraces = (((Succession) replayer).getStronglyViolatedTraces().get(activityA).get(activityB));
					//satisfiedTraces = (((Succession) replayer).getViolatedTraces().get(activityA).get(activityB));

					if(((ChainSuccession) replayer).getA_and_b_occur_always_in_sequence_Traces().containsKey(activityA) && ((ChainSuccession) replayer).getA_and_b_occur_always_in_sequence_Traces().get(activityA).containsKey(activityB)){
						if(((ChainSuccession) replayer).getA_and_b_occur_only_sometimes_in_sequence_Traces().containsKey(activityA) && ((ChainSuccession) replayer).getA_and_b_occur_only_sometimes_in_sequence_Traces().get(activityA).containsKey(activityB)){
							violatedTraces = (((ChainSuccession) replayer).getA_and_b_occur_always_in_sequence_Traces().get(activityA).get(activityB)) + ((ChainSuccession) replayer).getA_and_b_occur_only_sometimes_in_sequence_Traces().get(activityA).get(activityB);
						}
					}
					vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;				
				}

				if(constraint.equals(DeclareTemplate.Not_Succession)){
					for(TemplateReplayer rep : model.replayers){
						if(rep instanceof Succession && ((Succession) rep).getTemplate().equals(DeclareTemplate.Not_Succession)){
							replayer =  rep;
						}
					}
					satisfiedTraces = 0;
					violatedTraces =  0;
					if(((Succession) replayer).getA_and_b_occur_never_a_event_b().containsKey(activityA) && ((Succession) replayer).getA_and_b_occur_never_a_event_b().get(activityA).containsKey(activityB)){
						satisfiedTraces = ((Succession) replayer).getA_and_b_occur_never_a_event_b().get(activityA).get(activityB);
					}
					//int strsatisfiedTraces = (((Succession) replayer).getStronglyViolatedTraces().get(activityA).get(activityB));
					//satisfiedTraces = (((Succession) replayer).getViolatedTraces().get(activityA).get(activityB));

					if(((Succession) replayer).getA_and_b_occur_always_a_event_b().containsKey(activityA) && ((Succession) replayer).getA_and_b_occur_always_a_event_b().get(activityA).containsKey(activityB)){
						if(((Succession) replayer).getA_and_b_occur_sometimes_a_event_b().containsKey(activityA) && ((Succession) replayer).getA_and_b_occur_sometimes_a_event_b().get(activityA).containsKey(activityB)){
							violatedTraces = (((Succession) replayer).getA_and_b_occur_always_a_event_b().get(activityA).get(activityB)) + ((Succession) replayer).getA_and_b_occur_sometimes_a_event_b().get(activityA).get(activityB);
						}
					}
					vacuouslySatisfiedTraces = completedTraces - satisfiedTraces - violatedTraces;				
				}


				Double support = null;
				if (alpha) {
					support = ((double)satisfiedTraces + (double)vacuouslySatisfiedTraces)/ (double)totalTraces;
				} else {
					support = ((double)satisfiedTraces) / (double)totalTraces;
				}
				ArrayList<String> params = new ArrayList<String>();
				params.add(activityA);
				params.add(activityB);
				HashMap<Integer, List<String>> visibleconstr = output.getVisibleConstraintParametersMap();
				boolean visible = false;
				for(List<String> p : visibleconstr.values()){
					if(p.size()==2){
						if(p.get(0).equals(activityA) && p.get(1).equals(activityB)){
							visible = true;
						}
					}
					if(p.size()==1){
						if(p.get(0).equals(activityA)){
							visible = true;
						}
					}
				}
				if (visible) {
					filtered.add(constraint, activityA, activityB, completedTraces, satisfiedTraces, vacuouslySatisfiedTraces, violatedTraces);
					filtered.addSupport(constraint, instance, support);
				}
			}
		}
		return filtered;
	}



	public static DeclareModel filterOnSpecificConstraint(DeclareModel model, DeclareTemplate templateName, String activityA, String activityB) {
		if (model == null) {
			return null;
		}
		DeclareModel filtered = new DeclareModel();
		if (model.constraints.containsKey(templateName)) {
			if (model.constraints.get(templateName).containsKey(new Pair<String, String>(activityA, activityB))) {
				HashMap<Pair<String, String>, HashMap<String, Double>> v = new HashMap<Pair<String, String>, HashMap<String, Double>>();
				v.put(new Pair<String, String>(activityA, activityB), model.constraints.get(templateName).get(new Pair<String, String>(activityA, activityB)));
				filtered.constraints.put(templateName, v);
			}
		}
		return filtered;
	}

	public static DeclareModel filterOnTraceSupport(DeclareModel model, double minTraceSupport) {
		if (model == null || minTraceSupport > 1 || minTraceSupport < 0) {
			return null;
		}
		DeclareModel filtered = new DeclareModel();
		for (DeclareTemplate constraint : model.constraints.keySet()) {
			for (Pair<String, String> instance : model.constraints.get(constraint).keySet()) {
				String activityA = instance.getFirst();
				String activityB = instance.getSecond();

				double completedTraces = model.getCompletedTraces(constraint, activityA, activityB);
				double satisfiedTraces = model.getSatisfiedTraces(constraint, activityA, activityB);
				double vacuouslySatisfiedTraces = model.getVacuouslySatisfiedTraces(constraint, activityA, activityB);
				double violatedTraces = model.getViolatedTraces(constraint, activityA, activityB);

				System.out.println("considering : " + constraint + "\t comp = " + completedTraces + "\t sat = " + satisfiedTraces + "\t vac = " + vacuouslySatisfiedTraces + "\t vio = " + violatedTraces);
				if (completedTraces > 0 && ((satisfiedTraces + vacuouslySatisfiedTraces) / completedTraces) >= minTraceSupport) {
					System.out.println("adding : " + constraint + "\t comp = " + completedTraces + "\t sat = " + satisfiedTraces + "\t vac = " + vacuouslySatisfiedTraces + "\t vio = " + violatedTraces);
					filtered.add(constraint, activityA, activityB, (int) completedTraces, (int) satisfiedTraces, (int) vacuouslySatisfiedTraces, (int) violatedTraces);
				}
			}
		}
		return filtered;
	}

	public static double precision(DeclareModel goldStandard, DeclareModel candidate) {
		double tp = TP(goldStandard, candidate);
		double fp = FP(goldStandard, candidate);
		return tp / (tp + fp);
	}

	public static double recall(DeclareModel goldStandard, DeclareModel candidate) {
		double tp = TP(goldStandard, candidate);
		double fn = FN(goldStandard, candidate);
		return tp / (tp + fn);
	}

	public static double f1(DeclareModel candidate, DeclareModel goldStandard) {
		double precision = precision(goldStandard, candidate);
		double recall = recall(goldStandard, candidate);
		return (2 * precision * recall) / (precision + recall);
	}

	public HashMap<DeclareTemplate, HashMap<Pair<String, String>, HashMap<String, Double>>> getConstraints() {
		return constraints;
	}

	public void setConstraints(HashMap<DeclareTemplate, HashMap<Pair<String, String>, HashMap<String, Double>>> constraints) {
		this.constraints = constraints;
	}

	public boolean hasTraces() {
		return hasTraces;
	}

	public void dumpModel(String filename) {
		File f = new File(filename);
		try {
			PrintWriter pw = new PrintWriter(f);
			for (DeclareTemplate template : constraints.keySet()) {
				for (Pair<String, String> pair : constraints.get(template).keySet()) {
					pw.println(template + " (" + pair.getFirst() + ", " + pair.getSecond() + ")");
				}
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<TemplateReplayer> getReplayers() {
		return replayers;
	}

	public void setReplayers(List<TemplateReplayer> replayers) {
		this.replayers = replayers;
	}


}

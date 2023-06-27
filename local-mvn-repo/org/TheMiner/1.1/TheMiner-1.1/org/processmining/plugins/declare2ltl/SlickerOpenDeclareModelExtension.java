/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which are not
 * licensed under the terms of the GPL, given that they satisfy one or more of
 * the following conditions: 1) Explicit license is granted to the ProM and
 * ProMimport programs for usage, linking, and derivative work. 2) Carte blance
 * license is granted to all programs developed at Eindhoven Technical
 * University, The Netherlands, or under the umbrella of STW Technology
 * Foundation, The Netherlands. For further exemptions not covered by the above
 * conditions, please contact the author of this code.
 */
package org.processmining.plugins.declare2ltl;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;

/**
 * @author Fabrizio M. Maggi
 * 
 */
public class SlickerOpenDeclareModelExtension extends JPanel {

	public JComponent showLogVis(Correlations model) {

		HashMap<String, String> mapping = new HashMap<String, String>();
		Vector colNams = new Vector();
		colNams.add("Constraint");
		colNams.add("Activation (A)");
		colNams.add("Target (T)");
		colNams.add("Correlation");
		colNams.add("Correlation Support");
		colNams.add("Degree of Disambiguation");
		Vector data = new Vector();
		for(ConstraintDefinition cd : model.getCorrelationSupport().keySet()){
			for(String corr : model.getCorrelationSupport().get(cd).keySet()){
				Vector row = new Vector();
				row.add(cd.getName());
				if(cd.getName().contains("precedence")){
					row.add(cd.getBranches(cd.getParameterWithId(2)));
					if(cd.getBranches(cd.getParameterWithId(1))!=null){
						row.add(cd.getBranches(cd.getParameterWithId(1)));
					}else{
						row.add("-");
					}
				}else{
					row.add(cd.getBranches(cd.getParameterWithId(1)));
					if(cd.getBranches(cd.getParameterWithId(2))!=null){
						row.add(cd.getBranches(cd.getParameterWithId(2)));
					}else{
						row.add("-");
					}	
				}
				String operator = corr.split(";")[2];
				if(operator.contains("single")){
					Double avg = new Double(corr.split(";")[3]);	
					Double stddev = new Double(corr.split(";")[4]);
					Double delta = avg+stddev;
					Double hours = ((delta/1000)/60)/60;
					row.add("|A."+corr.split(";")[0]+" - T."+corr.split(";")[1]+"|<="+hours+" h");
					mapping.put("|A."+corr.split(";")[0]+" - T."+corr.split(";")[1]+"|<="+hours+" h", corr);
				}else if(operator.contains("double")){
					Double avg = new Double(corr.split(";")[3]);	
					Double stddev = new Double(corr.split(";")[4]);
					Double delta = avg+2*stddev;
					Double hours = ((delta/1000)/60)/60;
					row.add("|A."+corr.split(";")[0]+" - T."+corr.split(";")[1]+"|<="+hours+" h");
					mapping.put("|A."+corr.split(";")[0]+" - T."+corr.split(";")[1]+"|<="+hours+" h", corr);
				}else{
					if(operator.contains(">=")){
						operator = ">=";
					}else if(operator.contains("<=")){
						operator = "<=";
					}else if(operator.contains("!=")){
						operator = "!=";
					}else if(operator.contains("=")){
						operator = "=";
					}else if(operator.contains(">")){
						operator = ">";
					}else if(operator.contains("<")){
						operator = "<";
					}
					row.add("A."+corr.split(";")[0]+" "+operator+" T."+corr.split(";")[1]);
					mapping.put("A."+corr.split(";")[0]+" "+operator+" T."+corr.split(";")[1], corr);
				}
				Double cs = new Double(model.getCorrelationSupport().get(cd).get(corr));
				if(cs.isNaN() || cs.isInfinite()){
					row.add("-");
				}else{
					row.add(cs);
				}
				Double dd = new Double(model.getCorrelationDisambiguation().get(cd).get(corr));
				if(dd.isNaN() || dd.isInfinite()){
					row.add("-");
				}else{
					row.add(dd);
				}

				data.add(row);
			}
		}
		
		model.setMapping(mapping);
		
		TableModel dtm = new DefaultTableModel(data,colNams);
		ProMTable pt = new ProMTable(dtm);	
		
		pt.setSize(new Dimension(1500,800));
		pt.setMinimumSize(new Dimension(1500,800));
		pt.setPreferredSize(new Dimension(1500,800));
		pt.setMaximumSize(new Dimension(1500,800));
		this.add(pt);
		JScrollPane scroll = new JScrollPane(this);
		return scroll;
	}
	/**
	 * 
	 */
}
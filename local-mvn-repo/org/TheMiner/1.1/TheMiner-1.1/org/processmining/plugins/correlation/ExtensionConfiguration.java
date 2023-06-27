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
package org.processmining.plugins.correlation;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.plugins.declare2ltl.Correlations;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;

/**
 * @author Fabrizio M. Maggi
 * 
 */
public class ExtensionConfiguration extends JPanel {

	private ProMTable pt; 
	private Vector<Integer> constrsIDs;

	public Map<Integer, String> getCorrelationsPerConstraint(Correlations allCorrel){
		Map<Integer, String> output = new HashMap<Integer, String>();
		JTable table = pt.getTable();
		//int constrCount = -1;
		
		for(Integer selectedIndex : table.getSelectedRows()){
			int constrCount = 0;
			String oldConstr = table.getValueAt(0, 0).toString();
		    String oldParamA = table.getValueAt(0, 1).toString();
		    String oldParamT = table.getValueAt(0, 2).toString();
			for(int i = 0; i<table.getRowCount(); i++){
				if(!table.getValueAt(i, 0).equals(oldConstr) ||!table.getValueAt(i, 1).toString().equals(oldParamA) || !table.getValueAt(i, 2).toString().equals(oldParamT)){
					constrCount ++;
				}
				if(i == selectedIndex){
					break;
				}
				oldConstr = table.getValueAt(i, 0).toString();
			    oldParamA = table.getValueAt(i, 1).toString();
			    oldParamT = table.getValueAt(i, 2).toString();
			}
			if( table.getValueAt(selectedIndex, 3).toString().equals("INSERT A CORRELATION")){
				output.put(constrsIDs.get(constrCount), "conservative");
			}else{
				if(allCorrel.getMapping().get(table.getValueAt(selectedIndex, 3))!=null){
				output.put(constrsIDs.get(constrCount), allCorrel.getMapping().get(table.getValueAt(selectedIndex, 3).toString()));
				}else{
					String[] parts = table.getValueAt(selectedIndex, 3).toString().split(" ");
					if(ExtendedEvent.isNumeric(parts[0], Double.class) && ExtendedEvent.isNumeric(parts[2], Double.class)){
					output.put(constrsIDs.get(constrCount), parts[0].replaceAll("A.","")+";"+parts[2].replaceAll("T.","")+";"+parts[1]);
					}else if(ExtendedEvent.isBoolean(parts[0]) && ExtendedEvent.isBoolean(parts[2])){
						output.put(constrsIDs.get(constrCount), parts[0].replaceAll("A.","")+";"+parts[2].replaceAll("T.","")+";b"+parts[1]);
					}else{
						output.put(constrsIDs.get(constrCount), parts[0].replaceAll("A.","")+";"+parts[2].replaceAll("T.","")+";s"+parts[1]);
					}
				}
			}
		}
		for(Integer id : constrsIDs){
			if(!output.containsKey(id)){
				output.put(id, "conservative");
			}
		}
		return output;
	}

	public JComponent showLogVis(Correlations model) {


		Vector colNams = new Vector();
		colNams.add("Constraint");
		colNams.add("Activation (A)");
		colNams.add("Target (T)");
		colNams.add("Correlation");
		colNams.add("Correlation Support");
		colNams.add("Degree of Disambiguation");
		Vector data = new Vector();
		constrsIDs = new Vector<Integer>();
		for(ConstraintDefinition cd : model.getCorrelationSupport().keySet()){
			constrsIDs.add(cd.getId());
			//JTextField constraintTXT = new JTextField();

			Vector rowTXT = new Vector();
			rowTXT.add(cd.getName());
			if(cd.getName().contains("precedence")){
				//JTextField activationTXT = new JTextField(cd.getBranches(cd.getParameterWithId(2)).toString());
				//activationTXT.setEditable(false);
				rowTXT.add(cd.getBranches(cd.getParameterWithId(2)));
				if(cd.getBranches(cd.getParameterWithId(1))!=null){
					//JTextField targetTXT = new JTextField(cd.getBranches(cd.getParameterWithId(1)).toString());
					//targetTXT.setEditable(false);
					rowTXT.add(cd.getBranches(cd.getParameterWithId(1)));
				}else{
					//JTextField _TXT = new JTextField("-");
					//_TXT.setEditable(false);
					rowTXT.add("-");
				}
			}else{
				//JTextField activationTXT = new JTextField(cd.getBranches(cd.getParameterWithId(2)).toString());
				//activationTXT.setEditable(false);
				rowTXT.add(cd.getBranches(cd.getParameterWithId(1)));
				if(cd.getBranches(cd.getParameterWithId(1))!=null){
					//JTextField targetTXT = new JTextField(cd.getBranches(cd.getParameterWithId(1)).toString());
					//targetTXT.setEditable(false);
					rowTXT.add(cd.getBranches(cd.getParameterWithId(2)));
				}else{
					//JTextField _TXT = new JTextField("-");
					//_TXT.setEditable(false);
					rowTXT.add("-");
				}
			}
			//JTextField correlationToAdd = new JTextField();
			//correlationToAdd.setEditable(true);
			rowTXT.add("INSERT A CORRELATION");


			data.add(rowTXT);




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
				}else if(operator.contains("double")){
					Double avg = new Double(corr.split(";")[3]);	
					Double stddev = new Double(corr.split(";")[4]);
					Double delta = avg+2*stddev;
					Double hours = ((delta/1000)/60)/60;
					row.add("|A."+corr.split(";")[0]+" - T."+corr.split(";")[1]+"|<="+hours+" h");
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
		TableModel dtm = new DefaultTableModel(data,colNams);
		pt = new ProMTable(dtm);	



		pt.setSize(new Dimension(800,400));
		pt.setMinimumSize(new Dimension(800,400));
		pt.setPreferredSize(new Dimension(800,400));
		pt.setMaximumSize(new Dimension(800,400));
		this.add(pt);
		JScrollPane scroll = new JScrollPane(this);
		return scroll;
	}
	/**
	 * 
	 */
}
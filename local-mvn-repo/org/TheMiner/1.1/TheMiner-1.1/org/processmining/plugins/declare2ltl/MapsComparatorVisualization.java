package org.processmining.plugins.declare2ltl;

import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Maps Comparator Visualization", returnLabels = { "Maps Comparator" }, returnTypes = { JPanel.class }, parameterLabels = { "MapsConparatorFrame" }, userAccessible = false)
@Visualizer
public class MapsComparatorVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JPanel visualize(final PluginContext context, final Vector<String> results) {
		final JPanel frame = new JPanel();
		if(results.size()==0){
			frame.add(new JLabel("everything ok!!"));
			System.out.println("everything ok!!");
		}else{
			frame.add(new JLabel("something wrong!"));
			System.out.println("something wrong!");
			for(String element : results){
				System.out.println(element);
				frame.add(new JLabel(element));
			}
		}
		return frame;
	}
}

package org.processmining.plugins.declare2ltl;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Declare Miner Visualization", returnLabels = { "Visualization of Declare Models" }, returnTypes = { JComponent.class }, parameterLabels = { "DeclareMinerFrame" }, userAccessible = false)
@Visualizer
public class DeclareExtensionVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(final PluginContext context, final Correlations model) {
		//DeclareModelInformationPanel frame = new DeclareModelInformationPanel(model, context.getProgress());
		final JComponent frame = new SlickerOpenDeclareModelExtension().showLogVis(model);
		return frame;
		// The below code is unneccesary and unhealthy
		//		context.addConnection(new DeclareViewerConnection(frame));
		//		final ConnectionManager cm = context.getConnectionManager();
		//		try {
		//			if (cm.getConnections(DeclareViewerConnection.class, context) != null)
		//				return frame;
		//			return null;
		//		} catch (final ConnectionCannotBeObtained e) {
		//			// No connections available
		//			context.log("Connection does not exist", MessageLevel.DEBUG);
		//			return null;
		//		}
	}
}

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerNoHierarc;
import org.processmining.plugins.declareminer.DeclareMinerNoRed;
import org.processmining.plugins.declareminer.DeclareMinerNoTrans;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.util.Configuration;
import org.processmining.plugins.declareminer.util.DeclareModel;
import org.processmining.plugins.declareminer.visualizing.ActivityDefinition;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.DGraph;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphOrganicLayout;

public class BetterMiner {	
	public static void main (String args[])  {
		//String configuration_file_path = args[0];
		String configuration_file_path = "./config.properties.hybrid.fasterdeclare";
		Configuration conf = new Configuration(configuration_file_path);

		conf.setUnifiedLoggerPrunerType("replayers"); // will be obsolete after testing is done

		String miner_type = conf.miner_type;
		String output_file_type = conf.output_file_type;
		String output_path = conf.output_path;
		DeclareMinerOutput output = new DeclareMinerOutput();
		// Choose the Miner type based on configuration
		// TODO
		long init = System.currentTimeMillis();
		if (conf.log != null && conf.input != null){
			if (miner_type.equals("DeclareMiner"))
				output = DeclareMiner.mineDeclareConstraints(null, conf.log, conf.input);
			else if (miner_type.equals("DeclareMinerNoHierarc"))
				output = DeclareMinerNoHierarc.mineDeclareConstraints(null, conf.log, conf.input);
			else if (miner_type.equals("DeclareMinerNoRed"))
				output = DeclareMinerNoRed.mineDeclareConstraints(null, conf.log, conf.input);
			else if (miner_type.equals("DeclareMinerNoTrans"))
				output = DeclareMinerNoTrans.mineDeclareConstraints(null, conf.log, conf.input);
			else
				throw new IllegalArgumentException(String.format("Invalid miner type '%s'", miner_type));

		} else {
			throw new IllegalArgumentException("No valid argument combination found");
		}

		long fin = System.currentTimeMillis();
		long ciommesso = fin-init;

		DeclareMap model = output.getModel();
		DeclareModel declare_model = output.getDeclareModel();

		File file = new File(output_path);
		String abs_path = file.getAbsolutePath();

		if (output_file_type.equals("XML")) {
			// Write declare designer compatible XML
			AssignmentModelView view = new AssignmentModelView(model.getModel());
			DeclareMap map = new DeclareMap(model.getModel(), null, view, null, null, null);
			System.out.println(model.getModel().getConstraintDefinitions().iterator().next().getText());
			addLayout(map);
			AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(abs_path);
			broker.addAssignmentAndView(map.getModel(), map.getView());
		} else if (output_file_type.equals("TEXT")) {
			// Write constraints with supports to txt file
			//############################################
			//#       VECCHIO CODICE
			//#     declare_model.writeConstraintsToFileWithSupport(file);
            //#
			//############################################## 
			
			//########################################
			//  LA MIA PEZZA (SENZA IL VALORE DEL SUPPORTO)
			FileWriter file_writer = null;
			try {
				file_writer = new FileWriter(file.getAbsoluteFile(), false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			BufferedWriter buffered_writer = new BufferedWriter(file_writer);
			Iterator<ConstraintDefinition> constraints_iter = model.getModel().getConstraintDefinitions().iterator();
			while(constraints_iter.hasNext()) {
				ConstraintDefinition constraint = constraints_iter.next();
				//Iterator<Pair<String, String>> pair_iter = constraints.get(template).keySet().iterator();
				//while(pair_iter.hasNext()) {
				//	Pair<String, String> pair = pair_iter.next();
				//Double support = constraint.getId();
				ArrayList<String> params = new ArrayList<String>();
				for(Parameter p: constraint.getParameters()){
					for(ActivityDefinition a : constraint.getBranches(p)){
						params.add(a.getName());
					}
				}
				String line = null;
				if(params.size() == 1){
					line = constraint.getName() + "(" + params.get(0) + "): support";
				}else{
					line = constraint.getName() + "(" + params.get(0) + ", " + params.get(1) + "): support";
				}
				try {
					buffered_writer.write(line);
					buffered_writer.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				buffered_writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//#    FINE PEZZA
			//#######################################################

		} else if (output_file_type.equals("REPORT")) {
			// Write constraints as a human readable file
			declare_model.writeConstraintsAsHumanReadable(file, conf.log.size());
		}	else if (output_file_type.equals("NONE")) {

			//	declare_model.writeConstraintsAsHumanReadable(file, conf.log.size());
		} else {
			throw new IllegalArgumentException(String.format("Invalid output file type '%s'"));
		}

		System.out.println(abs_path);
		System.out.println("DONE!");
		System.out.println("Sono Velociminer e Ciommesso: "+ciommesso+" con "+conf.threadNumber+" threads");

	}

	public static void addLayout(DeclareMap map) {
		Iterable<ActivityDefinition> ads = map.getModel().getActivityDefinitions();
	
		for(ActivityDefinition ad : ads){
			System.out.println(map.getView().getActivityDefinitionCell(ad).getHeight());
			System.out.println(map.getView().getActivityDefinitionCell(ad).getWidth());
			System.out.println("------");
			Double r = new Rectangle2D.Double(10.,10.,(ad.getName().length())*7.,50.);
			//map.getView().getActivityDefinitionCell(ad).getAttributes().put("editable", true);
			//map.getView().getActivityDefinitionCell(ad).getAttributes().put("bounds", r);
			map.getView().setBounds(r, ad);
			
			//map.getView().getActivityDefinitionCell(ad).resize();
			System.out.println(map.getView().getActivityDefinitionCell(ad).getHeight());
			System.out.println(map.getView().getActivityDefinitionCell(ad).getWidth());
			System.out.println("------");
		}
		
		DGraph graph = map.getView().getGraph();
		
		

		final JGraphOrganicLayout oc = new JGraphOrganicLayout();

		oc.setDeterministic(true);
		oc.setOptimizeBorderLine(true);
		oc.setOptimizeEdgeCrossing(true);
		oc.setOptimizeEdgeDistance(true);
		oc.setOptimizeEdgeLength(true);
		oc.setOptimizeNodeDistribution(true);
		oc.setEdgeCrossingCostFactor(999999999);
		oc.setEdgeDistanceCostFactor(999999999);
		oc.setFineTuning(true);

		//	oc.setMinDistanceLimit(0.001);
		oc.setEdgeLengthCostFactor(9999);
		if(map.getModel().getConstraintDefinitions().size()<200){
			oc.setEdgeLengthCostFactor(99);
		}
		oc.setNodeDistributionCostFactor(999999999);
		oc.setBorderLineCostFactor(999);
		oc.setRadiusScaleFactor(0.9);
		final JGraphFacade jgf = new JGraphFacade(graph);
		oc.run(jgf);
		final Map nestedMap = jgf.createNestedMap(true, true); 
		graph.getGraphLayoutCache().edit(nestedMap); 
		
		if (graph != null) {
			graph.refresh();
			graph.repaint();
		}
		
		map.getView().updateUI();
	}

}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.DeclareMinerNoHierarc;
import org.processmining.plugins.declareminer.DeclareMinerNoRed;
import org.processmining.plugins.declareminer.DeclareMinerNoTrans;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.util.Configuration;
import org.processmining.plugins.declareminer.util.DeclareModel;
import org.processmining.plugins.declareminer.util.MultiConfiguration;
import org.processmining.plugins.declareminer.util.UnifiedLogger;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.DGraph;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.organic.JGraphOrganicLayout;


public class MultiBetterMiner {

	public static void main(String[] args) {
			//String configuration_file_path = args[0];
			String configuration_file_path = "config.properties";
			MultiConfiguration conf = new MultiConfiguration(configuration_file_path);

			conf.setUnifiedLoggerPrunerType("replayers"); // will be obsolete after testing is done
			conf.setUnifiedLoggerTemplates(conf.templates);
			
			String miner_type = conf.miner_type;
			String output_file_type = conf.output_file_type;
			String output_path = conf.output_path;
			
			ArrayList<DeclareMinerOutput> outputs = new ArrayList<DeclareMinerOutput>();
			FileWriter fW = initializeCSVFile();
			
			for (int i = 0; i < conf.logs.size(); i++) {
				for (int j = 0; j < conf.input.size(); j++) {
					long tot = 0; 
					for (int z = 0; z < conf.iterNumber; z++) {
						conf.setUnifiedLoggerInputLogName(conf.log_file_paths[i].substring(conf.log_file_paths[i].lastIndexOf("/")+1));
						conf.setUnifiedLoggerAlpha(conf.input.get(j).getAlpha());
						conf.setUnifiedLoggerMinSupport(conf.input.get(j).getMinSupport());
						DeclareMinerOutput output = new DeclareMinerOutput();
						// Choose the Miner type based on configuration
						// TODO
						long init = System.currentTimeMillis();
						if (conf.logs.get(i) != null && conf.input.get(j) != null){
							if (miner_type.equals("DeclareMiner"))
								output = DeclareMiner.mineDeclareConstraints(null, conf.logs.get(i), conf.input.get(j));
							else if (miner_type.equals("DeclareMinerNoHierarc"))
								output = DeclareMinerNoHierarc.mineDeclareConstraints(null, conf.logs.get(i), conf.input.get(j));
							else if (miner_type.equals("DeclareMinerNoRed"))
								output = DeclareMinerNoRed.mineDeclareConstraints(null, conf.logs.get(i), conf.input.get(j));
							else if (miner_type.equals("DeclareMinerNoTrans"))
								output = DeclareMinerNoTrans.mineDeclareConstraints(null, conf.logs.get(i), conf.input.get(j));
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
							addLayout(model);
							AssignmentViewBroker broker = XMLBrokerFactory.newAssignmentBroker(abs_path);
							broker.addAssignmentAndView(model.getModel(), model.getView());
						} else if (output_file_type.equals("TEXT")) {
							// Write constraints with supports to txt file
							declare_model.writeConstraintsToFileWithSupport(file);
						} else if (output_file_type.equals("REPORT")) {
							// Write constraints as a human readable file
							declare_model.writeConstraintsAsHumanReadable(file, conf.logs.get(i).size());
						}	else if (output_file_type.equals("NONE")) {
								
							//	declare_model.writeConstraintsAsHumanReadable(file, conf.log.size());
						} else {
							throw new IllegalArgumentException(String.format("Invalid output file type '%s'"));
						}
						
						System.out.println(abs_path);
						System.out.println("DONE!");
						System.out.println("Sono Velociminer e Ciommesso: "+ciommesso+" con "+conf.threadNumber+" threads");

						writeCSV(fW, conf.version, conf.input.get(j), conf.log_file_paths[i], ciommesso);

						//tot+=ciommesso;
						outputs.add(output);
					}	
					//double avg = (double)(new Double(tot)/conf.iterNumber);


				}
			}
			try {
				fW.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

				

	}
	
	public static FileWriter initializeCSVFile(){
		FileWriter fW = null;
		try {
			fW = new FileWriter(new File("./output/performance.csv"));
			fW.write("'version'; 'constraints'; 'alpha'; 'support'; 'mode'; 'file'; 'time' \n");
			fW.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fW;
	}
	
	public static void writeCSV(FileWriter fW, String version, DeclareMinerInput input, String fileName, double time){
		try {
			fW.write(version+"; "+ input.getSelectedDeclareTemplateSet()+"; "+input.getAlpha()+"; "+input.getMinSupport()+"; replayers; "+ fileName.substring(fileName.lastIndexOf("/")+1)+"; "+time+"\n");
			fW.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void addLayout(DeclareMap map) {
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
		}
}


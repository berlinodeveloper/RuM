package org.processmining.plugins.declareminer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declare2ltl.DeclareExtensionOutput;
import org.processmining.plugins.declareminer.DeclareMiner;
import org.processmining.plugins.declareminer.DeclareMinerInput;
import org.processmining.plugins.declareminer.Watch;
import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;
import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;

public class MultiConfiguration {
	public Properties properties;
	
	public String log_file_path_entry;
	public ArrayList<DeclareTemplate> templates = new ArrayList<DeclareTemplate>();
	public ArrayList<DeclarePerspective> perspectives = new ArrayList<DeclarePerspective>();
	public MapTemplateConfiguration map_template_configuration;
	public int[] min_support;
	public int[] alpha;
	public ArrayList<AprioriKnowledgeBasedCriteria> criterias = new ArrayList<AprioriKnowledgeBasedCriteria>();
	public String output_path;
	public String output_file_type;
	
	public ArrayList<XLog> logs;
	public String[] log_file_paths;
	
	public ArrayList<DeclareMinerInput> input;
	public DeclareMap inputModel;
	public DeclareMinerOutput inputObject;
	public DeclareMinerOutput model;
	public DeclareExtensionOutput inputObjectExtension;
	public Map<Integer, String> correlationsPerConstraint;
	
	//NEW ATTRIBUTES
	public boolean verbose;
	public String loggingPreprocessingFile;
	public String loggingAprioriFile;
	public String version;
	public boolean memoryCheck;
	public int threadNumber=1;

	
	public int iterNumber=1; 
	
	public String miner_type;
	
	public MultiConfiguration(String configuration_file_path) {
		inputFromConfigurationFile(configuration_file_path);
		// will be obsolete after testing is done
		setUnifiedLoggerLogPath();
		setUnifiedMemoryLoggerLogPath();
		//setUnifiedLoggerInputLogName();
		//setUnifiedLoggerTemplates(templates);
		//setUnifiedLoggerAlpha();
		//setUnifiedLoggerMinSupport();
		setIterationNumber();
	}
	
	public void inputFromConfigurationFile(String configuration_file_path) {
		input = new ArrayList<DeclareMinerInput>();
		Properties prop = configurationFile(configuration_file_path);
		
		Watch logImportWatch = new Watch();
		logImportWatch.start();
		log_file_path_entry = prop.getProperty("log_file_path");
		log_file_paths = log_file_path_entry.split(",");
		logs = new ArrayList<XLog>();
		version = prop.getProperty("version");
		
		UnifiedLogger.version = version;
		UnifiedLogger.unified_upload_log_path = prop.getProperty("unified_upload_log_path");
		UnifiedLogger.unified_apriori_log_path = prop.getProperty("unified_apriori_log_path");
		
		Watch localLogImportWatch = new Watch();

				
		for (int i = 0; i < log_file_paths.length; i++) {
			localLogImportWatch.start();
			// Parse log
			try {
				logs.add(XLogReader.openLog(log_file_paths[i]));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long localUploadTime = localLogImportWatch.msecs();
			
			try {
				UnifiedLogger.logUploadTime(log_file_paths[i].substring(log_file_paths[i].lastIndexOf("/")+1),localUploadTime);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		long uploadTime = logImportWatch.msecs();
		
		loggingAprioriFile = prop.getProperty("logging_preprocessing_file");
		PrintWriter pW = null;
		try {
			pW = new PrintWriter(new File(loggingAprioriFile));
			pW.write("LOG UPLOAD TIME: "+uploadTime+" ms");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pW.close();
		
		
		
		output_path = prop.getProperty("output_path");
		
		output_file_type = prop.getProperty("output_file_type");

		// read templates
		String[] raw_templates = prop.getProperty("templates").split(",");
		for (String template : raw_templates) {
			templates.add(DeclareTemplate.valueOf(template));
		}

		
		// read perspectives
		String[] raw_perspectives = prop.getProperty("perspectives").split(",");
		for (String perspective : raw_perspectives) {
			perspectives.add(DeclarePerspective.valueOf(perspective));
		}
		
		// read map_template_configuration
		String raw_map_template_configuration = prop.getProperty("map_template_configuration");
		map_template_configuration = MapTemplateConfiguration.valueOf(raw_map_template_configuration);
		
		// read min support
		String[] minSupports = prop.getProperty("min_support").split(",");
		min_support = new int[minSupports.length];
		for (int i = 0; i < minSupports.length; i++) {
			min_support[i] = Integer.valueOf(minSupports[i]);

		}
		
		// read alpha
		String[] alphas = prop.getProperty("alpha").split(",");
		alpha = new int[alphas.length];
		for (int i = 0; i < alphas.length; i++) {
			alpha[i] = Integer.valueOf(alphas[i]);
		}
		
		// read apriori knowledge based criteria
		String[] raw_criterias = prop.getProperty("criterias").split(",");
		for (String criteria : raw_criterias) {
			criterias.add(AprioriKnowledgeBasedCriteria.valueOf(criteria));
		}
		
		// read apriori concept file name
		String apriori_file_name = prop.getProperty("concept_file_name");
		
		// read miner_type
		miner_type = prop.getProperty("miner_type");
		
		//NEW VARIABLE
		verbose = Boolean.valueOf(prop.getProperty("verbose"));
		loggingAprioriFile = prop.getProperty("logging_apriori_file");
		memoryCheck = Boolean.valueOf(prop.getProperty("memory_check"));
		threadNumber = Integer.valueOf(prop.getProperty("thread_number"));

		
		for (int i = 0; i < minSupports.length; i++) {
			for (int j = 0; j < alphas.length; j++) {
				DeclareMinerInput dInput = new DeclareMinerInput();
				// -- Set selected declare template set
				Set<DeclareTemplate> template_set = new HashSet<DeclareTemplate>(templates);
				dInput.setSelectedDeclareTemplateSet(template_set);
				// -- Set declare perspective set
				Set<DeclarePerspective> persp_set = new HashSet<DeclarePerspective>(perspectives);
				dInput.setDeclarePerspectiveSet(persp_set);
				// -- Set declare template constraint template map
				Set<DeclareTemplate> selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
				DeclareTemplate[] declareTemplates = DeclareTemplate.values();
				for(DeclareTemplate d : declareTemplates)
					selectedDeclareTemplateSet.add(d);
			
				Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();
			
				for(DeclareTemplate d : declareTemplates){
					String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
					templateNameStringDeclareTemplateMap.put(templateNameString, d);
				}
				
				Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap = DeclareMiner.readConstraintTemplates(templateNameStringDeclareTemplateMap);
				
				dInput.setDeclareTemplateConstraintTemplateMap(declareTemplateConstraintTemplateMap);
				// -- Set map templare configuration
					dInput.setMapTemplateConfiguration(map_template_configuration);
				// -- Set min support
				dInput.setMinSupport(min_support[i]);
				// -- Set alpha
				dInput.setAlpha(alpha[j]);
				// -- Set aprior knowledge based criteria
				Set<AprioriKnowledgeBasedCriteria> apriori_set = new HashSet<AprioriKnowledgeBasedCriteria>(criterias);
				dInput.setAprioriKnowledgeBasedCriteriaSet(apriori_set);
				// -- Set apriori knowledge concept file name
				if (apriori_file_name != null && !apriori_file_name.equals("")) {
					dInput.setAprioriKnowledgeConceptFileName(apriori_file_name);
				}
				dInput.setVerbose(verbose);
				dInput.setLoggingPreprocessingFile(loggingPreprocessingFile);
				dInput.setLoggingAprioriFile(loggingAprioriFile);
				dInput.setMemoryCheck(memoryCheck);
				dInput.setThreadNumber(threadNumber);
					
				input.add(dInput);
			}
			
		} 
				

	}
	
	public Properties configurationFile(String configuration_file_path) {
		// Lazy loading
		if (properties == null) {
			properties = new Properties();
			InputStream in = null;
		 
			try {
				in = new FileInputStream(configuration_file_path);
		 
				// load a properties file
				properties.load(in);
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		return properties;
	}
	
	public void setUnifiedLoggerLogPath() {
		UnifiedLogger.unified_log_path = properties.getProperty("unified_log_path");
	}
	
	public void setUnifiedMemoryLoggerLogPath() {
		UnifiedLogger.unified_memory_log_path = properties.getProperty("unified_memory_log_path");
	}
	
	
	public void setUnifiedLoggerInputLogName(String logName) {
		UnifiedLogger.input_log_name = logName;
	}
	
	public void setUnifiedLoggerPrunerType(String pruner_type) {
		UnifiedLogger.pruner_type = pruner_type;
	}
	
	public void setUnifiedLoggerTemplates(ArrayList<DeclareTemplate> templates) {
		String template_name = "";
		if (templates.size() == 22) {
			template_name = "all";
		} else {
			template_name = properties.getProperty("templates");
		}
		UnifiedLogger.templates = template_name;
	}
	
	public void setUnifiedLoggerAlpha(int alpha) {
		UnifiedLogger.alpha = alpha;
	}
	
	public void setUnifiedLoggerMinSupport(int min_support) {
		UnifiedLogger.min_support = min_support;
	}
	
	public void setUnifiedLoggerVersion(String version){
		UnifiedLogger.version = version;
	}
	
	public void setIterationNumber(){
		iterNumber = new Integer(properties.getProperty("iteration_number")).intValue();
	}
}

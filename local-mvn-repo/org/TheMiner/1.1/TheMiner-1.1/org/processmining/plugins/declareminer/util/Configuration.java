package org.processmining.plugins.declareminer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class Configuration {
	public Properties properties;
	
	public String log_file_path;
	public ArrayList<DeclareTemplate> templates = new ArrayList<DeclareTemplate>();
	public ArrayList<DeclarePerspective> perspectives = new ArrayList<DeclarePerspective>();
	public MapTemplateConfiguration map_template_configuration;
	public int min_support;
	public int alpha;
	public ArrayList<AprioriKnowledgeBasedCriteria> criterias = new ArrayList<AprioriKnowledgeBasedCriteria>();
	public String output_path;
	public String output_file_type;
	
	public XLog log;
	public DeclareMinerInput input;
	public DeclareMap inputModel;
	public DeclareMinerOutput inputObject;
	public DeclareMinerOutput model;
	public DeclareExtensionOutput inputObjectExtension;
	public Map<Integer, String> correlationsPerConstraint;
	
	//NEW ATTRIBUTES
	public boolean verbose;
	public String loggingPreprocessingFile;
	public String loggingAprioriFile;
	boolean memoryCheck;
	public String version;
	public int threadNumber=1;

	
	public String miner_type;
	
	public Configuration() {}
	
	public Configuration(String configuration_file_path) {
		inputFromConfigurationFile(configuration_file_path);
		// will be obsolete after testing is done
		setUnifiedLoggerLogPath();
		setUnifiedMemoryLoggerLogPath();
		setUnifiedLoggerInputLogName();
		setUnifiedLoggerTemplates(templates);
		setUnifiedLoggerAlpha();
		setUnifiedLoggerMinSupport();
	}
	
	public void inputFromConfigurationFile(String configuration_file_path) {
		input = new DeclareMinerInput();
		Properties prop = configurationFile(configuration_file_path);
		version = prop.getProperty("version");

		UnifiedLogger.version = version;
		UnifiedLogger.unified_upload_log_path = prop.getProperty("unified_upload_log_path");
		UnifiedLogger.unified_apriori_log_path = prop.getProperty("unified_apriori_log_path");
		
		log_file_path = prop.getProperty("log_file_path");
		
		Watch localLogImportWatch = new Watch();
		localLogImportWatch.start();
		// Parse log
		try {
			log = XLogReader.openLog(log_file_path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long localUploadTime = localLogImportWatch.msecs();
		
		//try {
			//UnifiedLogger.logUploadTime(log_file_path.substring(log_file_path.lastIndexOf("/")+1),localUploadTime);
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		//}
		verbose = Boolean.valueOf(prop.getProperty("verbose"));
		loggingAprioriFile = prop.getProperty("logging_preprocessing_file");
		memoryCheck = Boolean.valueOf(prop.getProperty("memory_check"));
		threadNumber = Integer.valueOf(prop.getProperty("thread_number"));


		
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
		min_support = Integer.valueOf(prop.getProperty("min_support"));
		
		// read alpha
		alpha = Integer.valueOf(prop.getProperty("alpha"));
		
		// read apriori knowledge based criteria
		String[] raw_criterias = prop.getProperty("criterias").split(",");
		for (String criteria : raw_criterias) {
			criterias.add(AprioriKnowledgeBasedCriteria.valueOf(criteria));
		}
		
		// read apriori concept file name
		String apriori_file_name = prop.getProperty("concept_file_name");
		
		// read miner_type
		miner_type = prop.getProperty("miner_type");
		
		// -- Set selected declare template set
		Set<DeclareTemplate> template_set = new HashSet<DeclareTemplate>(templates);
		input.setSelectedDeclareTemplateSet(template_set);
		// -- Set declare perspective set
		Set<DeclarePerspective> persp_set = new HashSet<DeclarePerspective>(perspectives);
		input.setDeclarePerspectiveSet(persp_set);
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
		
		input.setDeclareTemplateConstraintTemplateMap(declareTemplateConstraintTemplateMap);
		// -- Set map templare configuration
		input.setMapTemplateConfiguration(map_template_configuration);
		// -- Set min support
		input.setMinSupport(min_support);
		// -- Set alpha
		input.setAlpha(alpha);
		// -- Set aprior knowledge based criteria
		Set<AprioriKnowledgeBasedCriteria> apriori_set = new HashSet<AprioriKnowledgeBasedCriteria>(criterias);
		input.setAprioriKnowledgeBasedCriteriaSet(apriori_set);
		// -- Set apriori knowledge concept file name
		if (apriori_file_name != null && !apriori_file_name.equals("")) {
			input.setAprioriKnowledgeConceptFileName(apriori_file_name);
		}
		input.setVerbose(verbose);
		input.setLoggingPreprocessingFile(loggingPreprocessingFile);
		input.setLoggingAprioriFile(loggingAprioriFile);	
		input.setMemoryCheck(memoryCheck);
		input.setThreadNumber(threadNumber);
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
	
	public void setUnifiedLoggerInputLogName() {
		UnifiedLogger.input_log_name = new File(log_file_path).getName();
	}
	
	public void setUnifiedMemoryLoggerLogPath() {
		UnifiedLogger.unified_memory_log_path = properties.getProperty("unified_memory_log_path");
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
	
	public void setUnifiedLoggerAlpha() {
		UnifiedLogger.alpha = alpha;
	}
	
	public void setUnifiedLoggerMinSupport() {
		UnifiedLogger.min_support = min_support;
	}
}

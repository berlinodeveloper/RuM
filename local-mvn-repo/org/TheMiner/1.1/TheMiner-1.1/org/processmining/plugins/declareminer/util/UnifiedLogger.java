package org.processmining.plugins.declareminer.util;

import java.io.FileWriter;
import java.io.IOException;

public class UnifiedLogger {
	public static String unified_log_path;
	public static String unified_memory_log_path;
	public static String unified_upload_log_path;
	public static String unified_apriori_log_path;
	public static String input_log_name;
	public static String pruner_type;
	public static String templates;
	public static int alpha;
	public static int min_support;
	public static String version;
	
	public static void log(String time) throws IOException {
		FileWriter fw = new FileWriter(unified_log_path, true);
		String log_line = templates + ";" + alpha + ";" + min_support + ";" + pruner_type + ";" + input_log_name + ";" + String.valueOf(time) + "\n";
		fw.write(log_line);
		fw.close();
	}
	
	public static void logMemory(String traceId, String template, String memory) throws IOException {
		//FileWriter fw = new FileWriter(unified_memory_log_path+"_"+templates + "_" + alpha + "_" + min_support + "_" + pruner_type + "_" + input_log_name.substring(0, input_log_name.indexOf("."))+".log", true);
		//String log_line = String.valueOf(memory) + "\n";
		FileWriter fw = new FileWriter(unified_memory_log_path, true);
		String log_line = version+";"+templates + ";" + alpha + ";" + min_support + ";" + pruner_type + ";" + input_log_name + ";" + String.valueOf(traceId)+ ";" + String.valueOf(template)+ ";" +String.valueOf(memory) + "\n";
		fw.write(log_line);
		fw.close();
	}
	
	public static void logMemory(long min, long max, double avg) throws IOException {
		//FileWriter fw = new FileWriter(unified_memory_log_path+"_"+templates + "_" + alpha + "_" + min_support + "_" + pruner_type + "_" + input_log_name.substring(0, input_log_name.indexOf("."))+".log", true);
		//String log_line = String.valueOf(memory) + "\n";
		FileWriter fw = new FileWriter(unified_memory_log_path, true);
		String log_line = version+";"+templates + ";" + alpha + ";" + min_support + ";" + pruner_type + ";" + input_log_name + ";" + String.valueOf(min)+ ";" + String.valueOf(max)+ ";" +String.valueOf(avg) + "\n";
		fw.write(log_line);
		fw.close();
	}
	
	
	public static void logAprioriTime(long time) throws IOException{
		FileWriter fw = new FileWriter(unified_apriori_log_path, true);
		String log_line = version+";"+templates + ";" + alpha + ";" + min_support + ";" + pruner_type + ";" + input_log_name + ";" + String.valueOf(time) + "\n";
		fw.write(log_line);
		fw.close();
	}
	
	public static void logUploadTime(String input_log_name, long time) throws IOException{
		FileWriter fw = new FileWriter(unified_upload_log_path, true);
		String log_line =  version+";"+ input_log_name + ";" + String.valueOf(time) + "\n";
		fw.write(log_line);
		fw.close();
	}
}

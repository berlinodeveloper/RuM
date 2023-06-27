package org.processmining.plugins.declareminer.util;

/**
 * This class contains the basic configurations of the plugins
 * 
 * @author Andrea Burattin
 */
public class BasicPluginConfiguration {

	// plugin information
	public static final String AUTHOR = "A. Burattin, F. Maggi, M. Cimitile, A. Sperduti";
	public static final String EMAIL = "burattin" + (char) 0x40 + "math.unipd.it";
	public static final String AFFILIATION = "Università degli Studi di Padova";
	public static final String PACK = "Online Declare Miner";
	
	// default network configuration
	public static int DEFAULT_NETWORK_PORT = 1234;
	// default model update
	public static final int DEFAULT_MODEL_UPDATE_FREQUENCY = 150;
	
	// approache names
	public static final String NAME_SW = "Sliding Window";
	public static final String NAME_LC = "Lossy Counting";
	public static final String NAME_LCB = "Lossy Counting with Budget";
}

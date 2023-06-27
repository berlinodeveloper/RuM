package org.processmining.plugins.correlation;

import java.util.HashMap;
import java.util.Vector;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XTrace;

public class ExtendedTrace {
	
	private XTrace trace;
	private Vector<Integer> nonambi;
	private HashMap<Integer,Vector<Integer>> correspcorrel;
		
	public XTrace getTrace() {
		return trace;
	}
	public void setTrace(XTrace trace) {
		this.trace = trace;
	}
	
	public Vector<Integer> getNonambi() {
		return nonambi;
	}
	public void setNonambi(Vector<Integer> nonambi) {
		this.nonambi = nonambi;
	}
	public HashMap<Integer, Vector<Integer>> getCorrespcorrel() {
		return correspcorrel;
	}
	public void setCorrespcorrel(HashMap<Integer, Vector<Integer>> correspcorrel) {
		this.correspcorrel = correspcorrel;
	}
	
	
	public static Object getAttributeValue(XAttributeMap map, String key){	
		AttributeType type = getAttributeType(map, key);
		Object output = null;
		if (type.equals(AttributeType.Real)){
			output = new Float(map.get(key).toString());
		}else if (type.equals(AttributeType.String)){
				output = new String(map.get(key).toString());
		}
		return output;
	}



	public static AttributeType getAttributeType(XAttributeMap map, String key){
		XAttribute attribute = map.get(key);
		  if(attribute instanceof XAttributeBoolean)
		   return AttributeType.Boolean;
		  else if(attribute instanceof XAttributeDiscrete)
		   return AttributeType.String;
		  else if(attribute instanceof XAttributeContinuous)
		   return AttributeType.Real;
		  else if(attribute instanceof XAttributeTimestamp)
		   return AttributeType.String;
		  else 
		   return AttributeType.String;
		 }
	

	public static boolean isNumeric(String str, Class<? extends Number> clazz)
	{
		try
		{
			if (clazz.equals(Float.class))
			{
				Byte.parseByte(str);
			}
			else if (clazz.equals(Double.class))
			{
				Double.parseDouble(str);
			}
			else if (clazz.equals(Byte.class))
			{
				Float.parseFloat(str);
			}
			else if (clazz.equals(Integer.class))
			{
				Integer.parseInt(str);
			}
			else if (clazz.equals(Long.class))
			{
				Long.parseLong(str);
			}
			else if (clazz.equals(Short.class))
			{
				Short.parseShort(str);
			}
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}

		return true;
	}

	public static boolean isBoolean(String str)
	{
		if(str.equals("true")|| str.equals("false")){
			return true;
		}else{
			return false;
		}
	}
	
	
}

package org.processmining.plugins.correlation;

import java.util.HashMap;
import java.util.Vector;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.declareminer.DeclareModelGenerator;

public class ExtendedEvent {

	private String event;
	//private HashMap<String, Vector<String>> attributeValues = new HashMap<String,Vector<String>>();
	private HashMap<String, String> attributeTypes = new HashMap<String, String>();

	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public HashMap<String, String> getAttributeTypes() {
		return attributeTypes;
	}
	public void setAttributeTypes(HashMap<String, String> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}

	public static Vector<String> getComparablePairs(Vector<String> activityDefinitions, XLog log, String activation, String target, HashMap<String, ExtendedEvent> extEvents){
		Vector<String> comparablePairs = new Vector<String>();

		//HashMap<String, ExtendedEvent> extEvents = getEventsWithAttributeTypes(activityDefinitions,log);
//		for(String attribute1 : extEvents.get(activation).getAttributeTypes().keySet()){
//			for(String attribute2 : extEvents.get(target).getAttributeTypes().keySet()){
//				if(extEvents.get(activation).getAttributeTypes().get(attribute1).equals(extEvents.get(target).getAttributeTypes().get(attribute2))){
//					if(!comparablePairs.contains(attribute1+";"+attribute2) && !comparablePairs.contains(attribute2+";"+attribute1)){
//						if((!attribute1.contains(":")&&!attribute2.contains(":")) || (attribute1.equals(XTimeExtension.KEY_TIMESTAMP) && attribute2.equals(XTimeExtension.KEY_TIMESTAMP))){
//							comparablePairs.add(attribute1+";"+attribute2);
//						}
//					}
//				}
//			}
//		}
		for(String attribute1 : extEvents.get(activation).getAttributeTypes().keySet()){
			for(String attribute2 : extEvents.get(target).getAttributeTypes().keySet()){
				if(extEvents.get(activation).getAttributeTypes().get(attribute1).equals(extEvents.get(target).getAttributeTypes().get(attribute2))){
					if(!comparablePairs.contains(attribute1+";"+attribute2) && !comparablePairs.contains(attribute2+";"+attribute1)){
						if(attribute1.equals(attribute2)){
							comparablePairs.add(attribute1+";"+attribute2);
						}
					}
				}
			}
		}

		return comparablePairs;
	}


	public static Object getAttributeValue(String attribute, XEvent event, HashMap<String,ExtendedEvent>  extEvents){	
		Object output = null;
		String type = extEvents.get(XConceptExtension.instance().extractName(event)).getAttributeTypes().get(attribute);
		if (type.equals("Float")){
			output = new Float(event.getAttributes().get(attribute).toString());
		}else if (type.equals("Byte")){
			output = new Byte(event.getAttributes().get(attribute).toString());
		}else if (type.equals("Double")){
			output = new Double(event.getAttributes().get(attribute).toString());
		}else if (type.equals("Integer")){
			output = new Integer(event.getAttributes().get(attribute).toString());
		}else if (type.equals("Long")){
			output = new Long(event.getAttributes().get(attribute).toString());
		}else if (type.equals("Short")){
			output = new Short(event.getAttributes().get(attribute).toString());
		}else if (type.equals("Boolean")){
			output = new Boolean(event.getAttributes().get(attribute).toString());
		}else if (type.equals("String")){
			if(attribute.equals(XTimeExtension.KEY_TIMESTAMP)){
				output = XTimeExtension.instance().extractTimestamp(event).getTime();
			}else if(!attribute.equals(XTimeExtension.KEY_TIMESTAMP)){
				output = new String(event.getAttributes().get(attribute).toString());
			}
		}
		return output;
	}

	public static HashMap<String,ExtendedEvent> getEventsWithAttributeTypes(Vector<String> activityDefinitions, XLog log){
		HashMap<String,ExtendedEvent> extEvents = new HashMap<String,ExtendedEvent>();
		Vector<String> alreadyAdded = new Vector<String>();
		for(String ad : activityDefinitions){
			Vector<String> oldAttributes = null;
			ExtendedEvent extEv = new ExtendedEvent();
			extEv.setEvent(ad);
			DeclareModelGenerator gen = new DeclareModelGenerator();
			boolean found = false;
			for(XTrace trace: log){
				if(!found){
			
				for(XEvent event : trace){
					if(!found){
					Vector<String> attributes = new Vector<String>();
					
					String eventName = null;
					if(gen.hasEventTypeInName(ad)){
						eventName = XConceptExtension.instance().extractName(event)+"-"+XLifecycleExtension.instance().extractTransition(event); 
					}else{
                        eventName = XConceptExtension.instance().extractName(event);
					}
					if(ad.equals(eventName)){
					if(!alreadyAdded.contains(eventName)){
						found = true;
						alreadyAdded.add(ad);
					
						
						// extEv = null;
						//if(!names.containsKey(eventName)){

						XAttributeMap attMap = event.getAttributes();
						for(String attr : attMap.keySet()){
							attributes.add(attr);
							String value = attMap.get(attr).toString();								
							//extEv.getAttributeTypes().put(attr, values);
							
							
							  XAttribute attribute = attMap.get(attr);
							if(isBoolean(value) || attribute  instanceof XAttributeBoolean){
								extEv.getAttributeTypes().put(attr, "Boolean");
							}
								  else if(isNumeric(value,Double.class) || attribute instanceof XAttributeDiscrete){
										extEv.getAttributeTypes().put(attr, "Float");
								  }
								  else if(isNumeric(value,Double.class) || attribute instanceof XAttributeContinuous){
										extEv.getAttributeTypes().put(attr, "Float");
								  }
								  else if(attribute instanceof XAttributeTimestamp){
								    	extEv.getAttributeTypes().put(attr, "String");
								  }
								  else {
										extEv.getAttributeTypes().put(attr, "String");
								 }
							
							
							
							//	}	

						}
						if(oldAttributes == null){
							oldAttributes = attributes;
						}else{
							oldAttributes.retainAll(attributes);
						}
					}
				}
				}
				}
				}
			}
			Vector<String> toRemove = new Vector<String>(); 
			for(String key : extEv.getAttributeTypes().keySet()){
				if(!oldAttributes.contains(key)){
					toRemove.add(key);
				}
			}
			for(String key: toRemove){
				extEv.getAttributeTypes().remove(key);
			}
			extEvents.put(ad,extEv);
		}
		return extEvents;
	}

	public static boolean isNumeric(String str, Class<? extends Number> clazz)
	{
		try
		{
			if (clazz.equals(Float.class))
			{
				Float.parseFloat(str);
			}
			else if (clazz.equals(Double.class))
			{
				Double.parseDouble(str);
			}
			else if (clazz.equals(Byte.class))
			{
				Byte.parseByte(str);
				
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

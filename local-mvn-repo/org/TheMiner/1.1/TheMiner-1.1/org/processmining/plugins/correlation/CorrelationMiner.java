package org.processmining.plugins.correlation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;

public class CorrelationMiner {



	public HashMap<String, Double> mineCorrelations(PrintWriter pw ,double correlationThreshold, String activation, Vector<String> comparablePairs, Vector<ExtendedTrace> tracesWithCorrespondingEvents, ConstraintDefinition constraintDefinition, HashMap<String,ExtendedEvent>  extEvents){
		HashMap<String, Double> discoveredCorrelations  = new HashMap<String, Double>();
		List<String> parameters = new ArrayList<String>();

		for(Parameter parameter : constraintDefinition.getParameters()){
			if(constraintDefinition.getBranches(parameter).iterator().hasNext()){
				String activityName = constraintDefinition.getBranches(parameter).iterator().next().getName();
				if((activityName.contains("-assign")||activityName.contains("-ate_abort")||activityName.contains("-suspend")||activityName.contains("-complete")||activityName.contains("-autoskip")||activityName.contains("-manualskip")||activityName.contains("pi_abort")||activityName.contains("-reassign")||activityName.contains("-resume")||activityName.contains("-schedule")||activityName.contains("-start")||activityName.contains("-unknown")||activityName.contains("-withdraw"))){
					String[] splittedName = constraintDefinition.getBranches(parameter).iterator().next().getName().split("-");
					activityName = splittedName[0];
					for(int i = 1; i<splittedName.length-1; i++){
						activityName = activityName + "-" + splittedName[i];
					}	
				}
				parameters.add(activityName);

			}else{
				parameters.add("EMPTY_PARAM");
			}
		}

		int numberNonambi = 0;
		int numberAmbi = 0;

		for(ExtendedTrace ext : tracesWithCorrespondingEvents){
			for(int i=0; i<ext.getNonambi().size(); i++){
				if(XConceptExtension.instance().extractName(ext.getTrace().get(ext.getNonambi().get(i))).equals(activation)){
					if(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size()==1){
						numberNonambi ++;
					}else{
						if(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size()>1){
							numberAmbi ++;
						}
					}
				}
			}
		}
		pw.println("#non-ambiguous instances: "+numberNonambi);
		pw.println("#ambiguous instances: "+numberAmbi);
		for(String pair : comparablePairs){
			String type = "";
			String attr1 = "";
			String attr2 = "";
			attr1 = pair.split(";")[0];
			attr2 = pair.split(";")[1];
			double numberofactivations = 0;
			double equal = 0;
			double lessequal = 0;
			double greaterequal = 0;
			double notequal = 0;
			double equalStr = 0;
			double notequalStr = 0;
			double equalBool = 0;
			double notEqualBool = 0;
			double equalDate = 0;
			double lessequalDate = 0;
			double greaterequalDate = 0;
			double notequalDate = 0; 
			Vector<Long> timeDists = new Vector<Long>();
			for(ExtendedTrace ext : tracesWithCorrespondingEvents){
				for(int i=0; i<ext.getNonambi().size(); i++){
					if(XConceptExtension.instance().extractName(ext.getTrace().get(ext.getNonambi().get(i))).equals(activation)){
						if(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size()==1){

							XEvent event1 = null;
							XEvent event2 = null;
							Integer eveAct = ext.getNonambi().get(i);
							Integer eveTarg = ext.getCorrespcorrel().get(ext.getNonambi().get(i)).get(0);
							//		if(XConceptExtension.instance().extractName(ext.getTrace().get(eveAct)).equals(parameters.get(0)) &&
							//				XConceptExtension.instance().extractName(ext.getTrace().get(eveTarg)).equals(parameters.get(1))){
							event1 = ext.getTrace().get(eveAct);
							event2 = ext.getTrace().get(eveTarg);
							//		}else{
							//			event1 = ext.getTrace().get(eveTarg);
							//			event2 = ext.getTrace().get(eveAct);
							//		}

							String eventName1 = XConceptExtension.instance().extractName(event1);


							numberofactivations++;

							type = extEvents.get(eventName1).getAttributeTypes().get(attr1);

							if (type.equals("Float")){
								try{
									Float num1 = new Float(event1.getAttributes().get(attr1).toString());
									Float num2 = new Float(event2.getAttributes().get(attr2).toString());
									if(num1.floatValue()==num2.floatValue()){
										equal++;
									}
									if(num1.floatValue()<=num2.floatValue()){
										lessequal++;
									}
									if(num1.floatValue()>=num2.floatValue()){
										greaterequal++;
									}
									if(num1.floatValue()!=num2.floatValue()){
										notequal++;
									}
								}catch(NumberFormatException e){

								}
							}else if (type.equals("Byte")){
								try{

									byte num1 = new Byte(event1.getAttributes().get(attr1).toString());
									byte num2 = new Byte(event2.getAttributes().get(attr2).toString());
									if(num1==num2){
										equal++;
									}
									if(num1<=num2){
										lessequal++;
									}
									if(num1>=num2){
										greaterequal++;
									}
									if(num1!=num2){
										notequal++;
									}
								}catch(NumberFormatException e){


								}
							}else if (type.equals("Double")){
								try{
									Double num1 = new Double(event1.getAttributes().get(attr1).toString());
									Double num2 = new Double(event2.getAttributes().get(attr2).toString());
									if(num1==num2){
										equal++;
									}
									if(num1<=num2){
										lessequal++;
									}
									if(num1>=num2){
										greaterequal++;
									}
									if(num1!=num2){
										notequal++;
									}
								}catch(NumberFormatException e){

								}
							}else if (type.equals("Integer")){
								try{
									Integer num1 = new Integer(event1.getAttributes().get(attr1).toString());
									Integer num2 = new Integer(event2.getAttributes().get(attr2).toString());
									if(num1==num2){
										equal++;
									}
									if(num1<=num2){
										lessequal++;
									}
									if(num1>=num2){
										greaterequal++;
									}
									if(num1!=num2){
										notequal++;
									}
								}catch(NumberFormatException e){

								}
							}else if (type.equals("Long")){
								try{
									Long num1 = new Long(event1.getAttributes().get(attr1).toString());
									Long num2 = new Long(event2.getAttributes().get(attr2).toString());
									if(num1==num2){
										equal++;
									}
									if(num1<=num2){
										lessequal++;
									}
									if(num1>=num2){
										greaterequal++;
									}
									if(num1!=num2){
										notequal++;
									}
								}catch(NumberFormatException e){

								}
							}else if (type.equals("Short")){
								try{
									Short num1 = new Short(event1.getAttributes().get(attr1).toString());
									Short num2 = new Short(event2.getAttributes().get(attr2).toString());
									if(num1==num2){
										equal++;
									}
									if(num1<=num2){
										lessequal++;
									}
									if(num1>=num2){
										greaterequal++;
									}
									if(num1!=num2){
										notequal++;
									}
								}catch(NumberFormatException e){

								}
							}else if (type.equals("Boolean")){
								boolean num1 = new Boolean(event1.getAttributes().get(attr1).toString());
								boolean num2 = new Boolean(event2.getAttributes().get(attr2).toString());
								if(num1==num2){
									equalBool++;
								}
								if(num1!=num2){
									notEqualBool++;
								}
							}else if (type.equals("String")){
								if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){

									long num1 = XTimeExtension.instance().extractTimestamp(event1).getTime();
									long num2 = XTimeExtension.instance().extractTimestamp(event2).getTime();
									long timeDiff = num2 - num1;
									if(timeDiff<0){
										timeDiff = 0 - timeDiff;
									}
									timeDists.add(timeDiff);
									//	if(num1==num2){
									//		equalDate++;
									//	}
									//	if(num1<=num2){
									//		lessequalDate++;
									//	}
									//	if(num1>=num2){
									//		greaterequalDate++;
									//	}
									//	if(num1!=num2){
									//		notequalDate++;
									//	}

								}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
									String num1 = new String(event1.getAttributes().get(attr1).toString());
									String num2 = new String(event2.getAttributes().get(attr2).toString());
									if(num1.equals(num2)){
										equalStr++;
									}
									if(!num1.equals(num2)){
										notequalStr++;
									}
								}
							}
						}
						//						}else if((ext.getNonambi().contains(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).get(0)) && 
						//								ext.getCorrespcorrel().get(ext.getCorrespcorrel().get(ext.getNonambi().get(i)).get(0)).size()==1)){
						//
						//
						//							for(int j=0;j<ext.getCorrespcorrel().get(ext.getNonambi().get(i)).size(); j++){
						//
						//								XEvent event1 = null;
						//								XEvent event2 = null;
						//								Integer eveAct = ext.getNonambi().get(i);
						//								Integer eveTarg = ext.getCorrespcorrel().get(ext.getNonambi().get(i)).get(j);
						//								//		if(XConceptExtension.instance().extractName(ext.getTrace().get(eveAct)).equals(parameters.get(0)) &&
						//								//				XConceptExtension.instance().extractName(ext.getTrace().get(eveTarg)).equals(parameters.get(1))){
						//								event1 = ext.getTrace().get(eveAct);
						//								event2 = ext.getTrace().get(eveTarg);
						//								//		}else{
						//								//			event1 = ext.getTrace().get(eveTarg);
						//								//			event2 = ext.getTrace().get(eveAct);
						//								//		}
						//
						//								String eventName1 = XConceptExtension.instance().extractName(event1);
						//
						//
						//								numberofactivations++;
						//								type = extEvents.get(eventName1).getAttributeTypes().get(attr1);
						//
						//								if (type.equals("Float")){
						//									Float num1 = new Float(event1.getAttributes().get(attr1).toString());
						//									Float num2 = new Float(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equal++;
						//									}
						//									if(num1<=num2){
						//										lessequal++;
						//									}
						//									if(num1>=num2){
						//										greaterequal++;
						//									}
						//									if(num1!=num2){
						//										notequal++;
						//									}
						//								}else if (type.equals("Byte")){
						//									byte num1 = new Byte(event1.getAttributes().get(attr1).toString());
						//									byte num2 = new Byte(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equal++;
						//									}
						//									if(num1<=num2){
						//										lessequal++;
						//									}
						//									if(num1>=num2){
						//										greaterequal++;
						//									}
						//									if(num1!=num2){
						//										notequal++;
						//									}
						//								}else if (type.equals("Double")){
						//									Double num1 = new Double(event1.getAttributes().get(attr1).toString());
						//									Double num2 = new Double(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equal++;
						//									}
						//									if(num1<=num2){
						//										lessequal++;
						//									}
						//									if(num1>=num2){
						//										greaterequal++;
						//									}
						//									if(num1!=num2){
						//										notequal++;
						//									}
						//								}else if (type.equals("Integer")){
						//									Integer num1 = new Integer(event1.getAttributes().get(attr1).toString());
						//									Integer num2 = new Integer(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equal++;
						//									}
						//									if(num1<=num2){
						//										lessequal++;
						//									}
						//									if(num1>=num2){
						//										greaterequal++;
						//									}
						//									if(num1!=num2){
						//										notequal++;
						//									}
						//								}else if (type.equals("Long")){
						//									Long num1 = new Long(event1.getAttributes().get(attr1).toString());
						//									Long num2 = new Long(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equal++;
						//									}
						//									if(num1<=num2){
						//										lessequal++;
						//									}
						//									if(num1>=num2){
						//										greaterequal++;
						//									}
						//									if(num1!=num2){
						//										notequal++;
						//									}
						//								}else if (type.equals("Short")){
						//									Short num1 = new Short(event1.getAttributes().get(attr1).toString());
						//									Short num2 = new Short(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equal++;
						//									}
						//									if(num1<=num2){
						//										lessequal++;
						//									}
						//									if(num1>=num2){
						//										greaterequal++;
						//									}
						//									if(num1!=num2){
						//										notequal++;
						//									}
						//								}else if (type.equals("Boolean")){
						//									boolean num1 = new Boolean(event1.getAttributes().get(attr1).toString());
						//									boolean num2 = new Boolean(event2.getAttributes().get(attr2).toString());
						//									if(num1==num2){
						//										equalBool++;
						//									}
						//									if(num1!=num2){
						//										notEqualBool++;
						//									}
						//								}else if (type.equals("String")){
						//									if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
						//
						//										long num1 = XTimeExtension.instance().extractTimestamp(event1).getTime();
						//										long num2 = XTimeExtension.instance().extractTimestamp(event2).getTime();
						//										if(num1==num2){
						//											equalDate++;
						//										}
						//										if(num1<=num2){
						//											lessequalDate++;
						//										}
						//										if(num1>=num2){
						//											greaterequalDate++;
						//										}
						//										if(num1!=num2){
						//											notequalDate++;
						//										}
						//
						//									}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
						//										String num1 = new String(event1.getAttributes().get(attr1).toString());
						//										String num2 = new String(event2.getAttributes().get(attr2).toString());
						//										if(num1.equals(num2)){
						//											equalStr++;
						//										}
						//										if(!num1.equals(num2)){
						//											notequalStr++;
						//										}
						//									}
						//								}
						//							}
						//						}
					}
				}


			}






			if (type.equals("Float")||type.equals("Byte")||type.equals("Double")||type.equals("Integer")||type.equals("Long")||type.equals("Short")){
				pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";=").split(";")[0]+(pair+";=").split(";")[2]+(pair+";=").split(";")[1]+":  "+equal/numberofactivations);
				pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";<=").split(";")[0]+(pair+";<=").split(";")[2]+(pair+";<=").split(";")[1]+" :  "+lessequal/numberofactivations);
				pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";>=").split(";")[0]+(pair+";>=").split(";")[2]+(pair+";>=").split(";")[1]+" :  "+greaterequal/numberofactivations);
				pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";!=").split(";")[0]+(pair+";!=").split(";")[2]+(pair+";!=").split(";")[1]+" :  "+ notequal/numberofactivations);
				if(equal/numberofactivations >= correlationThreshold){
					discoveredCorrelations.put(pair+";=", equal/numberofactivations);
				}
				if(lessequal/numberofactivations >= correlationThreshold){
					discoveredCorrelations.put(pair+";<=", lessequal/numberofactivations);
				}
				if(greaterequal/numberofactivations >= correlationThreshold){
					discoveredCorrelations.put(pair+";>=", greaterequal/numberofactivations);
				}
				if(notequal/numberofactivations >= correlationThreshold){
					discoveredCorrelations.put(pair+";!=", notequal/numberofactivations);
				}
				pw.flush();
			}else if (type.equals("Boolean")){
				pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";=").split(";")[0]+(pair+";=").split(";")[2]+(pair+";=").split(";")[1]+" :  "+equalBool/numberofactivations);
				pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";!=").split(";")[0]+(pair+";!=").split(";")[2]+(pair+";!=").split(";")[1]+" :  "+ notEqualBool/numberofactivations);
				if(equalBool/numberofactivations >= correlationThreshold){
					discoveredCorrelations.put(pair+";b=", equalBool/numberofactivations);
				}
				if(notEqualBool/numberofactivations >= correlationThreshold){
					discoveredCorrelations.put(pair+";b!=", notEqualBool/numberofactivations);
				}
				pw.flush();
			}else if (type.equals("String")){
				if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){



					double avg = 0;
					for(int i=0; i<timeDists.size(); i++){
						avg = avg + timeDists.get(i);
					}
					avg = avg/timeDists.size();
					//discoveredCorrelations.put("avg", avg);


					double stddev = 0;
					for(int i=0; i<timeDists.size(); i++){
						stddev = stddev + (  (timeDists.get(i)-avg) * (timeDists.get(i)-avg));
					}
					stddev = stddev/(timeDists.size()-1);
					stddev = Math.sqrt(stddev);

					//	avg= 345600000;
					//	stddev=0;
					double total =0;
					double supp = 0;
					for(int i=0; i<timeDists.size(); i++){
						if(timeDists.get(i)<= avg+stddev){
							supp++;
						}
						total++;
					}


					pw.println("MEAN FOR TIMESTAMP CORRELATION: "+avg);
					pw.println("STD DEVIATION FOR TIMESTAMP CORRELATION: "+stddev);
					discoveredCorrelations.put(pair+";singlestddev;"+avg+";"+stddev, supp/total);				
					pw.println("SUPPORT OF THE CANDIDATE CORRELATION (timestamp single standard deviation) :  "+supp/total);
					total =0;
					supp = 0;
					for(int i=0; i<timeDists.size(); i++){
						if(timeDists.get(i)<= avg+2*stddev){
							supp++;
						}
						total++;
					}


					discoveredCorrelations.put(pair+";doublestddev;"+avg+";"+stddev, supp/total);				


					pw.println("SUPPORT OF THE CANDIDATE CORRELATION (timestamp double standard deviation) :  "+supp/total);


					//	if(equalDate/numberofactivations >= correlationThreshold){
					//		discoveredCorrelations.put(pair+";d=", equalDate/numberofactivations);
					//	}
					//	if(lessequalDate/numberofactivations >= correlationThreshold){
					//		discoveredCorrelations.put(pair+";d<=", lessequalDate/numberofactivations);
					//	}
					//	if(greaterequalDate/numberofactivations >= correlationThreshold){
					//		discoveredCorrelations.put(pair+";d>=", greaterequalDate/numberofactivations);
					//	}
					//	if(notequalDate/numberofactivations >= correlationThreshold){
					//		discoveredCorrelations.put(pair+";d!=", notequalDate/numberofactivations);
					//	}

					pw.flush();
				}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
					pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";=").split(";")[0]+(pair+";=").split(";")[2]+(pair+";=").split(";")[1]+" :  "+equalStr/numberofactivations);
					pw.println("SUPPORT OF THE CANDIDATE CORRELATION (activation attribute <R> target attribute) "+(pair+";!=").split(";")[0]+(pair+";!=").split(";")[2]+(pair+";!=").split(";")[1]+" :  "+ notequalStr/numberofactivations);

					if(equalStr/numberofactivations >= correlationThreshold){
						discoveredCorrelations.put(pair+";s=", equalStr/numberofactivations);
					}
					if(notequalStr/numberofactivations >= correlationThreshold){
						discoveredCorrelations.put(pair+";s!=", notequalStr/numberofactivations);
					}
					pw.flush();
				}


			}
			pw.flush();


		}

		return discoveredCorrelations;
	}



	public Vector<String> getAllCorrelations(Vector<String> comparablePairs, String event, HashMap<String,ExtendedEvent> extEvents){
		Vector<String> allCorrelations  = new Vector<String>();
		for(String pair : comparablePairs){
			String attr1 = "";
			String attr2 = "";
			attr1 = pair.split(";")[0];
			attr2 = pair.split(";")[1];
			String type = extEvents.get(event).getAttributeTypes().get(attr1);

			if (type.equals("Float")||type.equals("Byte")||type.equals("Double")||type.equals("Integer")||type.equals("Long")||type.equals("Short")){
				allCorrelations.add(pair+";=");
				allCorrelations.add(pair+";<=");
				allCorrelations.add(pair+";>=");
				allCorrelations.add(pair+";!=");
			}else if (type.equals("Boolean")){
				allCorrelations.add(pair+";b=");
				allCorrelations.add(pair+";b!=");
			}else if (type.equals("String")){
				if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
					allCorrelations.add(pair+";singlestddev;0;0");
					allCorrelations.add(pair+";doublestddev;0;0");
				}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
					allCorrelations.add(pair+";s=");
					allCorrelations.add(pair+";s!=");
				}
			}
		}
		return allCorrelations;
	}



	public boolean isValid(boolean eventType, String candidateCorrelation, XEvent activation , XEvent target, HashMap<String,ExtendedEvent>  extEvents){	
		if(!candidateCorrelation.equals("conservative")){
		String type = null;
		if(!eventType){
			type = extEvents.get(XConceptExtension.instance().extractName(activation)).getAttributeTypes().get(candidateCorrelation.split(";")[0]);
		}else{
			type = extEvents.get(XConceptExtension.instance().extractName(activation)+"-"+XLifecycleExtension.instance().extractTransition(activation)).getAttributeTypes().get(candidateCorrelation.split(";")[0]);
		}
		String attr1 = candidateCorrelation.split(";")[0];
		String attr2 = candidateCorrelation.split(";")[1];
		if (type.equals("Float")){
			try{
				Float num1 = new Float(activation.getAttributes().get(attr1).toString());
				Float num2 = new Float(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("=") && num1.floatValue()==num2.floatValue()){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals("<=") && num1.floatValue()<=num2.floatValue()){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals(">=") && num1.floatValue()>=num2.floatValue()){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("!=") && num1.floatValue()!=num2.floatValue()){
					return true;
				}
				return false;
			}catch(NumberFormatException e){
				return false;
			}
		}else if (type.equals("Byte")){
			try{
				byte num1 = new Byte(activation.getAttributes().get(attr1).toString());
				byte num2 = new Byte(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
					return true;
				}
				return false;
			}catch(NumberFormatException e){
				return false;
			}
		}else if (type.equals("Double")){
			try{

				Double num1 = new Double(activation.getAttributes().get(attr1).toString());
				Double num2 = new Double(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
					return true;
				}
				return false;
			}catch(NumberFormatException e){
				return false;
			}
		}else if (type.equals("Integer")){
			try{
				Integer num1 = new Integer(activation.getAttributes().get(attr1).toString());
				Integer num2 = new Integer(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
					return true;
				}
				return false;
			}catch(NumberFormatException e){
				return false;
			}
		}else if (type.equals("Long")){
			try{
				Long num1 = new Long(activation.getAttributes().get(attr1).toString());
				Long num2 = new Long(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
					return true;
				}
				return false;
			}catch(NumberFormatException e){
				return false;
			}
		}else if (type.equals("Short")){
			try{
				Short num1 = new Short(activation.getAttributes().get(attr1).toString());
				Short num2 = new Short(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("=") && num1==num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals("<=") && num1<=num2){
					return true;
				}
				if( candidateCorrelation.split(";")[2].equals(">=") && num1>=num2){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("!=") && num1!=num2){
					return true;
				}
				return false;
			}catch(NumberFormatException e){
				return false;
			}
		}else if (type.equals("Boolean")){

			boolean num1 = new Boolean(activation.getAttributes().get(attr1).toString());
			boolean num2 = new Boolean(target.getAttributes().get(attr2).toString());
			if(candidateCorrelation.split(";")[2].equals("b=") && num1==num2){
				return true;
			}
			if(candidateCorrelation.split(";")[2].equals("b!=") && num1!=num2){
				return true;
			}
			return false;
		}else if (type.equals("String")){
			if(attr1.equals(XTimeExtension.KEY_TIMESTAMP) && attr2.equals(XTimeExtension.KEY_TIMESTAMP)){

				long num1 = XTimeExtension.instance().extractTimestamp(activation).getTime();
				long num2 = XTimeExtension.instance().extractTimestamp(target).getTime();


				long timeDiff = num2 - num1;
				if(timeDiff<0){
					timeDiff = 0 - timeDiff;
				}
				Double avg = new Double(candidateCorrelation.split(";")[3]);
				Double stddev = new Double(candidateCorrelation.split(";")[4]);
				if(candidateCorrelation.split(";")[2].equals("singlestddev")){
					if(timeDiff<= avg+stddev){
						return true;
					}
				}
				if(candidateCorrelation.split(";")[2].equals("doublestddev")){
					if(timeDiff<= avg+2*stddev){
						return true;
					}
				}
				//				if(candidateCorrelation.split(";")[2].equals("d=") && num1==num2){
				//					return true;
				//				}
				//				if( candidateCorrelation.split(";")[2].equals("d<=") && num1<=num2){
				//					return true;
				//				}
				//				if( candidateCorrelation.split(";")[2].equals("d>=") && num1>=num2){
				//					return true;
				//				}
				//				if(candidateCorrelation.split(";")[2].equals("d!=") && num1!=num2){
				//					return true;
				//				}
				return false;
			}else if(!attr1.equals(XTimeExtension.KEY_TIMESTAMP) && !attr2.equals(XTimeExtension.KEY_TIMESTAMP)){
				String num1 = new String(activation.getAttributes().get(attr1).toString());
				String num2 = new String(target.getAttributes().get(attr2).toString());
				if(candidateCorrelation.split(";")[2].equals("s=") && num1.equals(num2)){
					return true;
				}
				if(candidateCorrelation.split(";")[2].equals("s!=") && !num1.equals(num2)){
					return true;
				}
				return false;
			}
		}
		return false;
		}else{
			return true;
		}
	}
	
}

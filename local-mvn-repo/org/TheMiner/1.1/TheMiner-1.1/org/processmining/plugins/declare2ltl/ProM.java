package org.processmining.plugins.declare2ltl;

import org.deckfour.xes.extension.std.XLifecycleExtension;

public class ProM {

  /**
   *
   * @param event AbstractEvent
   * @return EventType
   */
  public static XLifecycleExtension.StandardModel getType(Event event) {
	  XLifecycleExtension.StandardModel type = XLifecycleExtension.StandardModel.COMPLETE;
    if (event.getType().equals(Event.Type.STARTED)) {
      type = XLifecycleExtension.StandardModel.START;
    }
    if (event.getType().equals(Event.Type.CANCELLED)) {
      type = XLifecycleExtension.StandardModel.ATE_ABORT;
    }    
    return type;
  }

  /**
   *
   * @param event AbstractEvent
   * @return EventType
   */
  public static String convert(String typeName) {
	 XLifecycleExtension.StandardModel type = XLifecycleExtension.StandardModel.COMPLETE;
    if (typeName.equals(Event.Type.STARTED.name())) {
      type = XLifecycleExtension.StandardModel.START;
    }
    if (typeName.equals(Event.Type.CANCELLED.name())) {
      type = XLifecycleExtension.StandardModel.ATE_ABORT;
    }
    return type.toString();
  }
}

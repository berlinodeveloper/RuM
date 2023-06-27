package org.processmining.plugins.dataaware;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;

/**
 * @author michael
 * 
 */
public class DataDeclareMap {

	private final transient AssignmentModel model;
	private final transient AssignmentModelView view;
	private final transient AssignmentViewBroker broker;

	

	/**
	 * @param model
	 * @param view
	 */
	public DataDeclareMap(final AssignmentModel model,  final AssignmentModelView view, final AssignmentViewBroker broker) {
		this.model = model;
		this.view = view;
		this.broker = broker;
	}

	/**
	 * @return
	 */
	public AssignmentModel getModel() {
		return model;
	}

	public AssignmentViewBroker getBroker() {
		return broker;
	}

	/**
	 * @return
	 */
	public AssignmentModelView getView() {
		return view;
	}


	

}

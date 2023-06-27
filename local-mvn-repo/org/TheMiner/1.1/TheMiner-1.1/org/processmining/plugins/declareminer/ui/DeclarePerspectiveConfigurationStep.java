package org.processmining.plugins.declareminer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.processmining.plugins.declareminer.enumtypes.DeclarePerspective;
import org.processmining.plugins.declareminer.listener.DeclareMinerSettingsListener;
import org.processmining.plugins.declareminer.swingx.ScrollableGridLayout;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class DeclarePerspectiveConfigurationStep extends myStep {
	Set<DeclarePerspective> declarePerspectiveSet;
	DeclareMinerSettingsListener listener;
	
	public DeclarePerspectiveConfigurationStep(){
		initComponents();
	}
	
	private void initComponents(){
		int noRows = 3;
		ScrollableGridLayout declarePerspectiveConfigurationStepLayout = new ScrollableGridLayout(this, 1, noRows, 0, 0);
		for(int i = 0; i < noRows; i++)
			declarePerspectiveConfigurationStepLayout.setRowFixed(i, true);
		this.setLayout(declarePerspectiveConfigurationStepLayout);
		
		JLabel headerLabel = SlickerFactory.instance().createLabel("<html><h1>Choose the Perspective(s) of Declare Map</h1>");
		
		declarePerspectiveSet = new HashSet<DeclarePerspective>();
		final JCheckBox controlFlowPerspectiveCheckBox = SlickerFactory.instance().createCheckBox(DeclarePerspective.Control_Flow+" ", true);
		final JCheckBox timePerspectiveCheckBox = SlickerFactory.instance().createCheckBox(DeclarePerspective.Time+" ", false);
		declarePerspectiveSet.add(DeclarePerspective.Control_Flow);
		
		controlFlowPerspectiveCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(controlFlowPerspectiveCheckBox.isSelected()){
					declarePerspectiveSet.add(DeclarePerspective.Control_Flow);
				}else{
					declarePerspectiveSet.remove(DeclarePerspective.Control_Flow);
				}
			}
		});
		
		timePerspectiveCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(timePerspectiveCheckBox.isSelected()){
					declarePerspectiveSet.add(DeclarePerspective.Time);
				}else{
					declarePerspectiveSet.remove(DeclarePerspective.Time);
				}
			}
		});
		
		int yPos = 0;
		
		declarePerspectiveConfigurationStepLayout.setPosition(headerLabel, 0, yPos++);
		add(headerLabel);
		
		declarePerspectiveConfigurationStepLayout.setPosition(controlFlowPerspectiveCheckBox, 0, yPos++);
		add(controlFlowPerspectiveCheckBox);
		
		declarePerspectiveConfigurationStepLayout.setPosition(timePerspectiveCheckBox, 0, yPos++);
		add(timePerspectiveCheckBox);
	}
	
	public boolean precondition() {
		return true;
	}

	public void readSettings() {
		listener.setDeclarePerspectiveSet(declarePerspectiveSet);
	}


	public void setListener(DeclareMinerSettingsListener listener) {
		this.listener = listener;
	}
}

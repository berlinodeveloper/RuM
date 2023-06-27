package org.processmining.plugins.declareminer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.processmining.plugins.declareminer.enumtypes.MapTemplateConfiguration;
import org.processmining.plugins.declareminer.listener.DeclareMinerSettingsListener;
import org.processmining.plugins.declareminer.swingx.ScrollableGridLayout;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class MapTemplateActivityConfigurationStep extends myStep {
	DeclareMinerSettingsListener listener;
	MapTemplateConfiguration mapTemplateConfiguration;
	
	public MapTemplateActivityConfigurationStep(){
		initComponents();
	}
	
	private void initComponents(){
		int noRows = 3;
		ScrollableGridLayout mapTemplateActivityConfigurationStepLayout = new ScrollableGridLayout(this, 1, noRows, 0, 0);
		
		for(int i = 0; i < noRows; i++)
			mapTemplateActivityConfigurationStepLayout.setRowFixed(i, true);
		this.setLayout(mapTemplateActivityConfigurationStepLayout);
		
		final JRadioButton discoverProvidedTemplatesOnlyRadioButton = SlickerFactory.instance().createRadioButton("Discover Only Those Templates Provided in the Map (but Across All Activities in the Log)");
		final JRadioButton discoverProvidedTemplatesAndActivitiesRadioButton = SlickerFactory.instance().createRadioButton("Discover Only Those Templates across only Those Activities Provided in the Map");
		final JRadioButton strengthenConstraintsRadioButton = SlickerFactory.instance().createRadioButton("Strengthen Map");
		
		ButtonGroup mapTemplateActivityConfigurationButtonGroup = new ButtonGroup();
		mapTemplateActivityConfigurationButtonGroup.add(discoverProvidedTemplatesOnlyRadioButton);
		mapTemplateActivityConfigurationButtonGroup.add(discoverProvidedTemplatesAndActivitiesRadioButton);
		mapTemplateActivityConfigurationButtonGroup.add(strengthenConstraintsRadioButton);
		
		
		discoverProvidedTemplatesAndActivitiesRadioButton.setSelected(true);
		mapTemplateConfiguration = MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossActivitiesOnlyInTheMap;
		
		int yPos = 0;
		mapTemplateActivityConfigurationStepLayout.setPosition(discoverProvidedTemplatesOnlyRadioButton, 0, yPos++);
		add(discoverProvidedTemplatesOnlyRadioButton);
		mapTemplateActivityConfigurationStepLayout.setPosition(discoverProvidedTemplatesAndActivitiesRadioButton, 0, yPos++);
		add(discoverProvidedTemplatesAndActivitiesRadioButton);
		mapTemplateActivityConfigurationStepLayout.setPosition(strengthenConstraintsRadioButton, 0, yPos++);
		add(strengthenConstraintsRadioButton);
		
		discoverProvidedTemplatesOnlyRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(discoverProvidedTemplatesOnlyRadioButton.isSelected())
					mapTemplateConfiguration = MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossAllActivitesInLog;
			}
		});
		
		discoverProvidedTemplatesAndActivitiesRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(discoverProvidedTemplatesAndActivitiesRadioButton.isSelected())
					mapTemplateConfiguration = MapTemplateConfiguration.DiscoverProvidedTemplatesAcrossActivitiesOnlyInTheMap;
			}
		});
		
		strengthenConstraintsRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(strengthenConstraintsRadioButton.isSelected())
					mapTemplateConfiguration = MapTemplateConfiguration.StrengthenMap;
			}
		});
		
		
	}
	
	public boolean precondition() {
		return true;
	}

	public void readSettings() {
		listener.setMapTemplateConfiguration(mapTemplateConfiguration);
	}

	public void setListener(DeclareMinerSettingsListener listener) {
		this.listener = listener;
	}

}

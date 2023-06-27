package org.processmining.plugins.declareminer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.listener.DeclareMinerSettingsListener;
import org.processmining.plugins.declareminer.swingx.ScrollableGridLayout;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class TemplateConfigurationStep extends myStep {
	JPanel templateSelectionPanel;
	Set<DeclareTemplate> selectedDeclareTemplateSet;
	DeclareMinerSettingsListener listener;
	
	TemplateListPanel templateListPanel;
	
	public TemplateConfigurationStep(){
		initComponents();
	}
	
	private void initComponents(){
		int noRows = 4;
		ScrollableGridLayout templateConfigurationStepLayout = new ScrollableGridLayout(this, 1, noRows, 0, 0);
		for(int i = 0; i < noRows; i++){
			templateConfigurationStepLayout.setRowFixed(i, true);
		}
		this.setLayout(templateConfigurationStepLayout);
		
		JLabel headerLabel = SlickerFactory.instance().createLabel("<html><h1>Choose the Templates to Mine</h1>");
		
		prepareTemplateSelectionPanel();
		
		int yPos = 0;
		templateConfigurationStepLayout.setPosition(headerLabel, 0, yPos++);
		add(headerLabel);
		
		templateConfigurationStepLayout.setPosition(templateSelectionPanel, 0, yPos++);
		add(templateSelectionPanel);
	}
	
	private void prepareTemplateSelectionPanel(){
		templateSelectionPanel = SlickerFactory.instance().createRoundedPanel();
		ScrollableGridLayout templateSelectionPanelLayout = new ScrollableGridLayout(templateSelectionPanel, 3,1,0,0);
		templateSelectionPanelLayout.setColumnFixed(0, true);
		templateSelectionPanelLayout.setColumnFixed(1, true);
		templateSelectionPanelLayout.setColumnFixed(2, true);
		templateSelectionPanel.setLayout(templateSelectionPanelLayout);
		
		final JList selectedTemplatesJList = new JList();
		JScrollPane selectedTemplatesJListScrollPane = new JScrollPane(selectedTemplatesJList);
		selectedTemplatesJListScrollPane.setPreferredSize(new Dimension(200,250));
		selectedTemplatesJListScrollPane.setOpaque(false);
		selectedTemplatesJListScrollPane.getViewport().setOpaque(false);
		selectedTemplatesJListScrollPane.setBorder(BorderFactory.createEmptyBorder());
		SlickerDecorator.instance().decorate(selectedTemplatesJListScrollPane.getVerticalScrollBar(), new Color(0, 0, 0, 0),
				new Color(140, 140, 140), new Color(80, 80, 80));
		selectedTemplatesJListScrollPane.getVerticalScrollBar().setOpaque(false);

		SlickerDecorator.instance().decorate(selectedTemplatesJListScrollPane.getHorizontalScrollBar(), new Color(0, 0, 0, 0),
				new Color(140, 140, 140), new Color(80, 80, 80));
		selectedTemplatesJListScrollPane.getHorizontalScrollBar().setOpaque(false);
		selectedTemplatesJListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		selectedTemplatesJListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel selectedTemplatesPanel = SlickerFactory.instance().createRoundedPanel();
		selectedTemplatesPanel.setBorder(BorderFactory.createTitledBorder("Selected Templates"));
		selectedTemplatesPanel.add(selectedTemplatesJListScrollPane);
		


		JPanel addRemovePanel = SlickerFactory.instance().createRoundedPanel();
		ScrollableGridLayout addRemovePanelLayout = new ScrollableGridLayout(addRemovePanel,1,5,0,0);
		addRemovePanel.setLayout(addRemovePanelLayout);
//		addRemovePanelLayout.setRowFixed(1, true);
//		addRemovePanelLayout.setRowFixed(2, true);
		
		Component verticalStrut = Box.createVerticalStrut(200);
		JButton selectAllButton = SlickerFactory.instance().createButton("Select All");
		JButton deSelectAllButton = SlickerFactory.instance().createButton("Deselect All");
		JButton addButton = SlickerFactory.instance().createButton("Add");
		JButton removeButton =SlickerFactory.instance().createButton("Remove");
		
		addRemovePanelLayout.setPosition(verticalStrut, 0, 0);
		addRemovePanel.add(verticalStrut);
		addRemovePanelLayout.setPosition(selectAllButton, 0, 1);
		addRemovePanel.add(selectAllButton);
		addRemovePanelLayout.setPosition(deSelectAllButton, 0, 2);
		addRemovePanel.add(deSelectAllButton);
		addRemovePanelLayout.setPosition(addButton, 0, 3);
		addRemovePanel.add(addButton);
		addRemovePanelLayout.setPosition(removeButton, 0, 4);
		addRemovePanel.add(removeButton);
		
		templateListPanel = new TemplateListPanel();
		selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
		
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object[] allTemplates = templateListPanel.getAllRows().toArray();
				selectedDeclareTemplateSet.clear();
				selectedDeclareTemplateSet.addAll(templateListPanel.getAllDeclareTemplates());
				selectedTemplatesJList.setListData(allTemplates);
			}
		});
		
		deSelectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectedDeclareTemplateSet.clear();	
				selectedTemplatesJList.removeAll();
				selectedTemplatesJList.setListData(selectedDeclareTemplateSet.toArray());
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedDeclareTemplateSet.addAll(templateListPanel.getSelectedDeclareTemplates());
				System.out.println(selectedDeclareTemplateSet);
				selectedTemplatesJList.setListData(templateListPanel.getTemplates(selectedDeclareTemplateSet).toArray());
			}
		});
		
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Set<String> toRemoveTemplateSet = new HashSet<String>();
				for(Object toRemoveTemplate : selectedTemplatesJList.getSelectedValues())
					toRemoveTemplateSet.add((String)toRemoveTemplate);
				
				selectedDeclareTemplateSet.removeAll(templateListPanel.getDeclareTemplates(toRemoveTemplateSet));
				selectedTemplatesJList.setListData(templateListPanel.getTemplates(selectedDeclareTemplateSet).toArray());
			}
		});
		
		templateSelectionPanelLayout.setPosition(templateListPanel, 0, 0);
		templateSelectionPanel.add(templateListPanel);
		
		templateSelectionPanelLayout.setPosition(addRemovePanel, 1, 0);
		templateSelectionPanel.add(addRemovePanel);
		
		templateSelectionPanelLayout.setPosition(selectedTemplatesPanel, 2, 0);
		templateSelectionPanel.add(selectedTemplatesPanel);
//
//		part12NCSelectionStepPanelLayout.setPosition(templateSelectionPanel, 0, 0);
//		part12NCSelectionStepPanel.add(templateSelectionPanel);
		

	}
	
	
		
	public boolean precondition() {
		return true;
	}

	public void readSettings() {
		listener.setSelectedDeclareTemplateSet(selectedDeclareTemplateSet);
		listener.setDeclareTemplateConstraintTemplateMap(templateListPanel.getDeclareTemplateConstraintTemplateMap());
	}

	public void setListener(DeclareMinerSettingsListener listener) {
		this.listener = listener;
	}
}

package org.processmining.plugins.declareminer.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.declareminer.enumtypes.AprioriKnowledgeBasedCriteria;
import org.processmining.plugins.declareminer.listener.DeclareMinerSettingsListener;
import org.processmining.plugins.declareminer.swingx.ScrollableGridLayout;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class AprioriConfigurationStep extends myStep {
	int minSupportValue;
	int alphaValue;
	JPanel supportAlphaConfigurationPanel;
	JPanel conceptBasedAprioriPanel;
	
	DeclareMinerSettingsListener listener;
	JTextField alphaValueTextField;
	JTextField supportValueTextField;
	Set<AprioriKnowledgeBasedCriteria> aprioriKnowledgeBasedCriteriaSet;
	JCheckBox conceptBasedCheckBox;
	
	public AprioriConfigurationStep(){
		initComponents();
	}
	
	
	private void initComponents(){
		int noRows = 8;
		ScrollableGridLayout aprioriConfigurationStepLayout = new ScrollableGridLayout(this, 1, noRows, 0, 0);
		for(int i = 0; i < noRows; i++)
			aprioriConfigurationStepLayout.setRowFixed(i, true);
		this.setLayout(aprioriConfigurationStepLayout);
		
		buildConceptBasedAprioriPanel();
		buildSupportAlphaConfigurationPanel();
		
		JLabel headerLabel = SlickerFactory.instance().createLabel("<html><h1>Apriori and Activation/Satisfaction Configuration</h1>");
		
		aprioriKnowledgeBasedCriteriaSet = new HashSet<AprioriKnowledgeBasedCriteria>();
		aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
		
		final JRadioButton allActivitiesConsideringEventTypesRadioButton = SlickerFactory.instance().createRadioButton("All Activities (considering Event Types)");
		final JRadioButton allActivitiesIngoringEventTypesRadioButton = SlickerFactory.instance().createRadioButton("All Activities (ignoring Event Types)");
		
		ButtonGroup allActivitiesButtonGroup = new ButtonGroup();
		allActivitiesButtonGroup.add(allActivitiesConsideringEventTypesRadioButton);
		allActivitiesButtonGroup.add(allActivitiesIngoringEventTypesRadioButton);
		allActivitiesConsideringEventTypesRadioButton.setSelected(true);
		
		final JCheckBox diversityCheckBox = SlickerFactory.instance().createCheckBox("Diversity (Ignore associations between event types of same activity", false);
		conceptBasedCheckBox = SlickerFactory.instance().createCheckBox("Concept Based associations", false);
		
		allActivitiesConsideringEventTypesRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(allActivitiesConsideringEventTypesRadioButton.isSelected()){
					aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
					diversityCheckBox.setVisible(true);
				}else{
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
					diversityCheckBox.setVisible(false);
				}
			}
		});
		
		allActivitiesIngoringEventTypesRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(allActivitiesIngoringEventTypesRadioButton.isSelected()){
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.AllActivitiesWithEventTypes);
					aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.AllActivitiesIgnoringEventTypes);
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.Diversity);
					diversityCheckBox.setVisible(false);
				}else{
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.AllActivitiesIgnoringEventTypes);
					diversityCheckBox.setVisible(true);
				}
			}
		});
		
		diversityCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(diversityCheckBox.isSelected()){
					aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.Diversity);
				}else{
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.Diversity);
				}
			}
		});
		
		conceptBasedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(conceptBasedCheckBox.isSelected()){
					conceptBasedAprioriPanel.setVisible(true);
				}
				else{
					conceptBasedAprioriPanel.setVisible(false);
				}
			}
		});
		
		int yPos = 0;

		aprioriConfigurationStepLayout.setPosition(headerLabel, 0, yPos++);
		add(headerLabel);
		
		aprioriConfigurationStepLayout.setPosition(allActivitiesConsideringEventTypesRadioButton, 0, yPos++);
		add(allActivitiesConsideringEventTypesRadioButton);
		
		aprioriConfigurationStepLayout.setPosition(allActivitiesIngoringEventTypesRadioButton, 0, yPos++);
		add(allActivitiesIngoringEventTypesRadioButton);
		
		aprioriConfigurationStepLayout.setPosition(diversityCheckBox, 0, yPos++);
		add(diversityCheckBox);
		
		aprioriConfigurationStepLayout.setPosition(conceptBasedCheckBox, 0, yPos++);
		add(conceptBasedCheckBox);
		
		aprioriConfigurationStepLayout.setPosition(conceptBasedAprioriPanel, 0, yPos++);
		add(conceptBasedAprioriPanel);
		
		aprioriConfigurationStepLayout.setPosition(supportAlphaConfigurationPanel, 0, yPos++);
		add(supportAlphaConfigurationPanel);
	}
	
	private void buildConceptBasedAprioriPanel(){
		conceptBasedAprioriPanel = SlickerFactory.instance().createRoundedPanel();
		conceptBasedAprioriPanel.setBorder(BorderFactory.createTitledBorder("Concept Based Item Set Configuration"));
		
		int noRows = 3;
		ScrollableGridLayout conceptBasedAprioriPanelLayout = new ScrollableGridLayout(conceptBasedAprioriPanel, 1, noRows, 0, 0);
		for(int i = 0; i < noRows; i++)
			conceptBasedAprioriPanelLayout.setRowFixed(i, true);
		conceptBasedAprioriPanel.setLayout(conceptBasedAprioriPanelLayout);
		
		JPanel loadConceptPanel = SlickerFactory.instance().createRoundedPanel();
		ScrollableGridLayout loadConceptPanelLayout = new ScrollableGridLayout(loadConceptPanel, 3, 1, 0, 0);
		loadConceptPanel.setLayout(loadConceptPanelLayout);
		
		JLabel loadConceptLabel = SlickerFactory.instance().createLabel("Load Concept Ontology");
		JButton loadConceptFileButton = SlickerFactory.instance().createButton("Load File");
		final JLabel conceptFileNameLabel = SlickerFactory.instance().createLabel("..");
		
		loadConceptFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String tmpDir = System.getProperty("java.io.tmpdir");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(tmpDir));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(fileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION){
					conceptFileNameLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
					listener.setAprioriKnowledgeConceptFileName(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		loadConceptPanelLayout.setPosition(loadConceptLabel, 0, 0);
		loadConceptPanel.add(loadConceptLabel);
		
		loadConceptPanelLayout.setPosition(loadConceptFileButton, 1, 0);
		loadConceptPanel.add(loadConceptFileButton);
		
		loadConceptPanelLayout.setPosition(conceptFileNameLabel, 2, 0);
		loadConceptPanel.add(conceptFileNameLabel);
		
		aprioriKnowledgeBasedCriteriaSet = new HashSet<AprioriKnowledgeBasedCriteria>();
		final JCheckBox intraGroupCheckBox = SlickerFactory.instance().createCheckBox("Intra-Group Concept Associations", false);
		final JCheckBox interGroupCheckBox = SlickerFactory.instance().createCheckBox("Inter Group Concept Associations", false);
		aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.InterGroup);
		
		intraGroupCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(intraGroupCheckBox.isSelected())
					aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.IntraGroup);
				else
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.IntraGroup);
			}
		});
		
		interGroupCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(interGroupCheckBox.isSelected())
					aprioriKnowledgeBasedCriteriaSet.add(AprioriKnowledgeBasedCriteria.InterGroup);
				else
					aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.InterGroup);
			}
		});
		
		int yPos = 0;
		conceptBasedAprioriPanelLayout.setPosition(loadConceptPanel, 0, yPos++);
		conceptBasedAprioriPanel.add(loadConceptPanel);
		
		conceptBasedAprioriPanelLayout.setPosition(intraGroupCheckBox, 0, yPos++);
		conceptBasedAprioriPanel.add(intraGroupCheckBox);
		
		conceptBasedAprioriPanelLayout.setPosition(interGroupCheckBox, 0, yPos++);
		conceptBasedAprioriPanel.add(interGroupCheckBox);
		
		conceptBasedAprioriPanel.setVisible(false);
	}
	
	private void buildSupportAlphaConfigurationPanel(){
		supportAlphaConfigurationPanel = SlickerFactory.instance().createRoundedPanel();
		supportAlphaConfigurationPanel.setBorder(BorderFactory.createTitledBorder("Configure support/alpha"));
		
		int noRows = 5;
		ScrollableGridLayout supportAlphaConfigurationPanelLayout = new ScrollableGridLayout(supportAlphaConfigurationPanel, 1, noRows, 0, 0);
		for(int i = 0; i < noRows; i++)
			supportAlphaConfigurationPanelLayout.setRowFixed(i, true);
		supportAlphaConfigurationPanel.setLayout(supportAlphaConfigurationPanelLayout);
		
		JPanel supportPanel = SlickerFactory.instance().createRoundedPanel();
		ScrollableGridLayout supportPanelLayout = new ScrollableGridLayout(supportPanel, 3, 1, 0, 0);
		for(int i = 0; i < 2; i++)
			supportPanelLayout.setColumnFixed(i, true);
		supportPanel.setLayout(supportPanelLayout);
		
		JLabel supportLabel = SlickerFactory.instance().createLabel("Min. Support ");
		final JSlider supportSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		supportSlider.setMinimum(0);
		supportSlider.setMaximum(100);
		supportSlider.setValue(100);
		supportValueTextField = new JTextField("100");
		minSupportValue = supportSlider.getValue();
		supportSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(!supportSlider.getValueIsAdjusting()){
					minSupportValue = supportSlider.getValue();
					supportValueTextField.setText(minSupportValue+"");
					supportValueTextField.revalidate();
					supportValueTextField.repaint();
				}
			}
		});
		
		supportPanelLayout.setPosition(supportLabel, 0, 0);
		supportPanel.add(supportLabel);
		supportPanelLayout.setPosition(supportSlider, 1, 0);
		supportPanel.add(supportSlider);
		supportPanelLayout.setPosition(supportValueTextField, 2, 0);
		supportPanel.add(supportValueTextField);
		
		JPanel alphaPanel = SlickerFactory.instance().createRoundedPanel();
		ScrollableGridLayout alphaPanelLayout = new ScrollableGridLayout(alphaPanel, 3, 1, 0, 0);
		for(int i = 0; i < 2; i++)
			alphaPanelLayout.setColumnFixed(i, true);
		alphaPanel.setLayout(alphaPanelLayout);
		
		JLabel alphaLabel = SlickerFactory.instance().createLabel("Alpha ");
		final JSlider alphaSlider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
		alphaSlider.setMinimum(0);
		alphaSlider.setMaximum(100);
		alphaSlider.setValue(0);
		alphaValueTextField = new JTextField("0");
		alphaValue = alphaSlider.getValue();
		alphaSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(!alphaSlider.getValueIsAdjusting()){
					alphaValue = alphaSlider.getValue();
					alphaValueTextField.setText(alphaValue+"");
					alphaValueTextField.revalidate();
					alphaValueTextField.repaint();
				}
			}
		});
		
		
		alphaPanelLayout.setPosition(alphaLabel, 0, 0);
		alphaPanel.add(alphaLabel);
		alphaPanelLayout.setPosition(alphaSlider, 1, 0);
		alphaPanel.add(alphaSlider);
		alphaPanelLayout.setPosition(alphaValueTextField, 2, 0);
		alphaPanel.add(alphaValueTextField);

		int yPos = 0;
		
		JLabel supportInformationLabel = SlickerFactory.instance().createLabel("<HTML> <H4> Choose support 100 if your log contains no noise </H4></html>");
		supportAlphaConfigurationPanelLayout.setPosition(supportInformationLabel, 0, yPos++);
		supportAlphaConfigurationPanel.add(supportInformationLabel);
		
		supportAlphaConfigurationPanelLayout.setPosition(supportPanel, 0, yPos++);
		supportAlphaConfigurationPanel.add(supportPanel);
		
		JLabel alphaInformationLabel = SlickerFactory.instance().createLabel("<HTML> <H4> Choose alpha as 0 if you want to discover only those contraints that are always activated in the log (non-trivially true)</H4></html>");
		supportAlphaConfigurationPanelLayout.setPosition(alphaInformationLabel, 0, yPos++);
		supportAlphaConfigurationPanel.add(alphaInformationLabel);
		
		supportAlphaConfigurationPanelLayout.setPosition(alphaPanel, 0, yPos++);
		supportAlphaConfigurationPanel.add(alphaPanel);
	}
	
	public boolean precondition() {
		return true;
	}

	public void readSettings() {
		minSupportValue = new Integer(supportValueTextField.getText().trim());
		alphaValue = new Integer(alphaValueTextField.getText().trim());
		
		if(!conceptBasedCheckBox.isSelected()){
			aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.IntraGroup);
			aprioriKnowledgeBasedCriteriaSet.remove(AprioriKnowledgeBasedCriteria.InterGroup);
		}
		
		listener.setAprioriKnowledgeBasedCriteria(aprioriKnowledgeBasedCriteriaSet);
		listener.setMinSupport(minSupportValue);
		listener.setAlpha(alphaValue);
	}

	public void setListener(DeclareMinerSettingsListener listener) {
		this.listener = listener;
	}

}

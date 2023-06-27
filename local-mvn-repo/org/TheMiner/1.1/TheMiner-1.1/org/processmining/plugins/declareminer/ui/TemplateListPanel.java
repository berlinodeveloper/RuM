package org.processmining.plugins.declareminer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.swingx.ScrollableGridLayout;
import org.processmining.plugins.declareminer.visualizing.ConstraintTemplate;
import org.processmining.plugins.declareminer.visualizing.IItem;
import org.processmining.plugins.declareminer.visualizing.Language;
import org.processmining.plugins.declareminer.visualizing.LanguageGroup;
import org.processmining.plugins.declareminer.visualizing.TemplateBroker;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@SuppressWarnings("serial")
public class TemplateListPanel extends JPanel {
	class GenTableModel extends DefaultTableModel {
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	protected GenTableModel templatesTableModel;
	JTable templatesTable;
	Map<String, DeclareTemplate> templateNameStringDeclareTemplateMap;
	Map<DeclareTemplate, ConstraintTemplate> declareTemplateConstraintTemplateMap;

	public TemplateListPanel() {
		super();
		populateDeclareTemplates();

		readConstraintTemplates();
		preparePanel();
	}

	private void populateDeclareTemplates() {
		DeclareTemplate declareTemplate = DeclareTemplate.Absence;
		DeclareTemplate[] declareTemplateNames = declareTemplate.getDeclaringClass().getEnumConstants();

		templateNameStringDeclareTemplateMap = new HashMap<String, DeclareTemplate>();

		for (DeclareTemplate d : declareTemplateNames) {
			String templateNameString = d.toString().replaceAll("_", " ").toLowerCase();
			templateNameStringDeclareTemplateMap.put(templateNameString, d);
		}
	}

	private void preparePanel() {
		Vector<Object> colNamesVector = new Vector<Object>();
		colNamesVector.add("Template");

		Vector<Vector<Object>> templateNamesVector = new Vector<Vector<Object>>();
		for (String templateNameString : templateNameStringDeclareTemplateMap.keySet()) {
			Vector<Object> a = new Vector<Object>();
			a.add(templateNameString);
			templateNamesVector.add(a);
		}

		int noRows = 4;
		ScrollableGridLayout templateListPanelLayout = new ScrollableGridLayout(this, 1, noRows, 0, 0);
		templateListPanelLayout.setRowFixed(0, true);
		templateListPanelLayout.setRowFixed(2, true);
		// templateListPanelLayout.setRowFixed(3, true);
		this.setLayout(templateListPanelLayout);

		JPanel searchPanel = prepareSearchPanel(templateNamesVector, colNamesVector);

		templatesTableModel = new GenTableModel();
		templatesTableModel.setDataVector(templateNamesVector, colNamesVector);

		templatesTable = new JTable(templatesTableModel);
		templatesTable.setAutoCreateRowSorter(true);

		JScrollPane templatesTableScrollPane = new JScrollPane(templatesTable);
		templatesTableScrollPane.setPreferredSize(new Dimension(200, 250));
		templatesTableScrollPane.setOpaque(false);
		templatesTableScrollPane.getViewport().setOpaque(false);
		templatesTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
		SlickerDecorator.instance().decorate(templatesTableScrollPane.getVerticalScrollBar(), new Color(0, 0, 0, 0),
				new Color(140, 140, 140), new Color(80, 80, 80));
		templatesTableScrollPane.getVerticalScrollBar().setOpaque(false);

		SlickerDecorator.instance().decorate(templatesTableScrollPane.getHorizontalScrollBar(), new Color(0, 0, 0, 0),
				new Color(140, 140, 140), new Color(80, 80, 80));
		templatesTableScrollPane.getHorizontalScrollBar().setOpaque(false);

		// templatesTableScrollPane.setMinimumSize(new Dimension((int)3.5*d.width, (int)
		// 2*d.height));
		// templatesTableScrollPane.setPreferredSize(new Dimension(200, 250));
		templatesTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		templatesTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		templatesTableScrollPane.setVisible(true);

		final JEditorPane selectedTemplateDescriptionPane = new JEditorPane();
		selectedTemplateDescriptionPane.setEditable(false);
		selectedTemplateDescriptionPane.setContentType("text/html");
		selectedTemplateDescriptionPane.setText(makeHTMLPage("<H1>No description available.</H1>"));

		JScrollPane selectedTemplateDescriptionScrollPane = new JScrollPane(selectedTemplateDescriptionPane);
		selectedTemplateDescriptionScrollPane.setPreferredSize(new Dimension(200, 200));
		selectedTemplateDescriptionScrollPane.setOpaque(false);
		selectedTemplateDescriptionScrollPane.getViewport().setOpaque(false);
		selectedTemplateDescriptionScrollPane.setBorder(BorderFactory.createEmptyBorder());
		selectedTemplateDescriptionScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		selectedTemplateDescriptionScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(selectedTemplateDescriptionScrollPane.getVerticalScrollBar(),
				new Color(0, 0, 0, 0),
				new Color(140, 140, 140), new Color(80, 80, 80));
		selectedTemplateDescriptionScrollPane.getVerticalScrollBar().setOpaque(false);

		SlickerDecorator.instance().decorate(selectedTemplateDescriptionScrollPane.getHorizontalScrollBar(),
				new Color(0, 0, 0, 0),
				new Color(140, 140, 140), new Color(80, 80, 80));
		selectedTemplateDescriptionScrollPane.getHorizontalScrollBar().setOpaque(false);

		templatesTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int[] selectedRows = templatesTable.getSelectedRows();
				if (selectedRows.length > 1) {
					selectedTemplateDescriptionPane
							.setText(makeHTMLPage("<H1>Multiple Templates Chosen. No description available.</H1>"));
				} else {
					String selectedTemplateString = (String) templatesTable.getValueAt(selectedRows[0], 0);
					DeclareTemplate declareTemplate = templateNameStringDeclareTemplateMap.get(selectedTemplateString);
					ConstraintTemplate constraintTemplate = declareTemplateConstraintTemplateMap.get(declareTemplate);
					System.out.println("Selected template: " + selectedTemplateString);
					String description = "<h2>" + constraintTemplate.getName() + "</h2><p>"
							+ constraintTemplate.getDescription() + "</p><p>"
							+ constraintTemplate.getText().replace("\n", "").replace("<", "&lt;").replace(">", "&gt;")
							+ "</p>";
					selectedTemplateDescriptionPane.setText(makeHTMLPage(description));
				}
			}
		});

		templateListPanelLayout.setPosition(searchPanel, 0, 0);
		add(searchPanel);

		templateListPanelLayout.setPosition(templatesTableScrollPane, 0, 1);
		add(templatesTableScrollPane);

		JLabel descriptionLabel = SlickerFactory.instance().createLabel("<HTML><H3>Description</H3></HTML>");
		templateListPanelLayout.setPosition(descriptionLabel, 0, 2);
		add(descriptionLabel);

		templateListPanelLayout.setPosition(selectedTemplateDescriptionScrollPane, 0, 3);
		add(selectedTemplateDescriptionScrollPane);
	}

	private JPanel prepareSearchPanel(final Vector<Vector<Object>> templateNamesVector,
			final Vector<Object> colNamesVector) {
		final JTextField regexText = new JTextField(20);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Vector<Vector<Object>> templateNameList = new Vector<Vector<Object>>();

				String regex = regexText.getText().trim();
				if (!regex.contains("\\*"))
					regex = "*" + regex + "*";
				regex = regex.replaceAll("\\*", "[A-Za-z0-9,\\\\- ]*");

				Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				System.out.println(regex);
				for (Vector<Object> vector : templateNamesVector) {
					if (p.matcher(vector.firstElement().toString().replaceAll("\\[", "").replaceAll("\\]", ""))
							.matches()) {
						templateNameList.add(vector);
					}
				}

				templatesTableModel.setDataVector(templateNameList, colNamesVector);
				templatesTable.revalidate();
			}
		};

		regexText.addActionListener(actionListener);

		JButton searchButton = SlickerFactory.instance().createButton("Search");
		searchButton.addActionListener(actionListener);

		JPanel searchPanel = SlickerFactory.instance().createRoundedPanel();
		ScrollableGridLayout searchPanelLayout = new ScrollableGridLayout(searchPanel, 2, 1, 0, 0);
		searchPanelLayout.setColumnFixed(0, true);
		searchPanelLayout.setColumnFixed(1, true);
		searchPanel.setLayout(searchPanelLayout);

		searchPanelLayout.setPosition(regexText, 0, 0);
		searchPanel.add(regexText);
		searchPanelLayout.setPosition(searchButton, 1, 0);
		searchPanel.add(searchButton);

		return searchPanel;
	}

	private void readConstraintTemplates() {
		InputStream templateInputStream = getClass().getResourceAsStream("/resources/template.xml");
		File languageFile = null;
		try {
			languageFile = File.createTempFile("template", ".xml");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(templateInputStream));
			String line = bufferedReader.readLine();
			PrintStream out = new PrintStream(languageFile);
			while (line != null) {
				out.println(line);
				line = bufferedReader.readLine();
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		TemplateBroker templateBroker = XMLBrokerFactory.newTemplateBroker(languageFile.getAbsolutePath());
		List<Language> languagesList = templateBroker.readLanguages();

		// the first language in the list is the condec language, which is what we need
		Language condecLanguage = languagesList.get(0);
		List<IItem> templateList = new ArrayList<IItem>();
		List<IItem> condecLanguageChildrenList = condecLanguage.getChildren();
		for (IItem condecLanguageChild : condecLanguageChildrenList) {
			if (condecLanguageChild instanceof LanguageGroup) {
				templateList.addAll(visit(condecLanguageChild));
			} else {
				templateList.add(condecLanguageChild);
			}
		}

		declareTemplateConstraintTemplateMap = new HashMap<DeclareTemplate, ConstraintTemplate>();

		for (IItem item : templateList) {
			if (item instanceof ConstraintTemplate) {
				ConstraintTemplate constraintTemplate = (ConstraintTemplate) item;
				// System.out.println(constraintTemplate.getName()+" @
				// "+constraintTemplate.getDescription()+" @ "+constraintTemplate.getText());
				if (templateNameStringDeclareTemplateMap
						.containsKey(constraintTemplate.getName().replaceAll("-", "").toLowerCase())) {
					declareTemplateConstraintTemplateMap.put(
							templateNameStringDeclareTemplateMap
									.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()),
							constraintTemplate);
					System.out.println(constraintTemplate.getName() + " @ " + templateNameStringDeclareTemplateMap
							.get(constraintTemplate.getName().replaceAll("-", "").toLowerCase()));
				}
			}
		}
	}

	private List<IItem> visit(IItem item) {
		List<IItem> templateList = new ArrayList<IItem>();
		if (item instanceof LanguageGroup) {
			LanguageGroup languageGroup = (LanguageGroup) item;
			List<IItem> childrenList = languageGroup.getChildren();
			for (IItem child : childrenList) {
				if (child instanceof LanguageGroup) {
					templateList.addAll(visit(child));
				} else {
					templateList.add(child);
				}
			}
		}
		return templateList;
	}

	private String makeHTMLPage(String text) {
		return "<html>\n" + "\t" + "<head></head>\n" + "\t" + "<body>\n" + "\t" + "\t" + text + "\t" + "</body>\n"
				+ "</html>";
	}

	public Set<String> getAllRows() {
		return templateNameStringDeclareTemplateMap.keySet();
	}

	public Set<DeclareTemplate> getAllDeclareTemplates() {
		Set<DeclareTemplate> selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
		Set<String> selectedTemplateNameSet = getAllRows();
		for (String selectedTemplateName : selectedTemplateNameSet)
			selectedDeclareTemplateSet.add(templateNameStringDeclareTemplateMap.get(selectedTemplateName));
		return selectedDeclareTemplateSet;
	}

	public Set<String> getSelectedRows() {
		Set<String> selectedTemplateSet = new HashSet<String>();
		int[] selectedRows = templatesTable.getSelectedRows();
		for (int i = 0; i < selectedRows.length; i++) {
			selectedTemplateSet.add((String) templatesTable.getValueAt(selectedRows[i], 0));
		}
		return selectedTemplateSet;
	}

	public Set<DeclareTemplate> getSelectedDeclareTemplates() {
		Set<DeclareTemplate> selectedDeclareTemplateSet = new HashSet<DeclareTemplate>();
		Set<String> selectedTemplateNameSet = getSelectedRows();
		for (String selectedTemplateName : selectedTemplateNameSet)
			selectedDeclareTemplateSet.add(templateNameStringDeclareTemplateMap.get(selectedTemplateName));
		return selectedDeclareTemplateSet;
	}

	public Set<DeclareTemplate> getDeclareTemplates(Set<String> templateNameSet) {
		Set<DeclareTemplate> declareTemplateSet = new HashSet<DeclareTemplate>();
		for (String templateName : templateNameSet)
			declareTemplateSet.add(templateNameStringDeclareTemplateMap.get(templateName));
		return declareTemplateSet;
	}

	public Set<String> getTemplates(Set<DeclareTemplate> declareTemplateSet) {
		Set<String> templateNameSet = new HashSet<String>();
		for (DeclareTemplate declareTemplate : declareTemplateSet) {
			templateNameSet.add(declareTemplate.toString().replaceAll("_", " ").toLowerCase());
		}
		return templateNameSet;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new TemplateListPanel());
		frame.setVisible(true);
	}

	public Map<DeclareTemplate, ConstraintTemplate> getDeclareTemplateConstraintTemplateMap() {
		return declareTemplateConstraintTemplateMap;
	}

}

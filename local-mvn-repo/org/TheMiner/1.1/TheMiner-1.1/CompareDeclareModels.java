import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;
import org.processmining.plugins.declareminer.visualizing.AssignmentModelView;
import org.processmining.plugins.declareminer.visualizing.AssignmentViewBroker;
import org.processmining.plugins.declareminer.visualizing.ConstraintDefinition;
import org.processmining.plugins.declareminer.visualizing.Parameter;
import org.processmining.plugins.declareminer.visualizing.XMLBrokerFactory;


public class CompareDeclareModels {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String xml1= "withFinancial.xml";
		String xml2 = "withoutFinancial.xml";
		String xml3 = "oldFinancial.xml";
		boolean check = true;
		if(check){
			check = checkModels(xml1, xml2, xml3);
		}
		if(check){
			check = checkModels(xml2, xml1, xml3);
		}
		if(check){
			check = checkModels(xml3, xml1, xml2);
		}
		if(check){
			System.out.println("the input models are identical");
		}else{
			System.out.println("the input models are different");
		}
	}


	private static boolean checkModels(String xml1, String xml2, String xml3){
		boolean check = true;
		//		File file = new File("output.txt");
		//		FileWriter file_writer = null;
		//		try {
		//			file_writer = new FileWriter(file.getAbsoluteFile(), false);
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//		BufferedWriter buffered_writer = new BufferedWriter(file_writer);
		AssignmentViewBroker broker1 = XMLBrokerFactory.newAssignmentBroker(xml1);
		AssignmentModel model1 = broker1.readAssignment();
		AssignmentModelView view1 = new AssignmentModelView(model1);
		broker1.readAssignmentGraphical(model1, view1);

		//	BufferedWriter buffered_writer = new BufferedWriter(file_writer);
		AssignmentViewBroker broker2 = XMLBrokerFactory.newAssignmentBroker(xml2);
		AssignmentModel model2 = broker2.readAssignment();
		AssignmentModelView view2 = new AssignmentModelView(model2);
		broker2.readAssignmentGraphical(model2, view2);

		//	BufferedWriter buffered_writer = new BufferedWriter(file_writer);
		AssignmentViewBroker broker3 = XMLBrokerFactory.newAssignmentBroker(xml3);
		AssignmentModel model3 = broker3.readAssignment();
		AssignmentModelView view3 = new AssignmentModelView(model3);
		broker3.readAssignmentGraphical(model3, view3);

		System.out.println("Modello 1");
		String stringModel1 = "";
		Collection<ConstraintDefinition> constraints1 = model1.getConstraintDefinitions();
		for(ConstraintDefinition cd : constraints1){
			String template = cd.getName();
			String current = template+"[";
			stringModel1 = stringModel1 + "@"+template;
			if(template.equals("choice")||template.equals("exclusive choice")||template.equals("co-existence")){
				String branch = null;
				String firstbranch = null;
				for(Parameter p : cd.getParameters()){
					firstbranch = branch;
					branch = cd.getFirstBranch(p).getName();
					stringModel1 = stringModel1 + branch;
					current = current + branch+"; ";
				}
				System.out.println(current+"]");
				stringModel1 = stringModel1 + "@";
				stringModel1 = stringModel1 + "@" +template +branch + firstbranch + "@";
			}else{
				for(Parameter p : cd.getParameters()){
					String branch = cd.getFirstBranch(p).getName();
					stringModel1 = stringModel1 + branch;
					current = current + branch+"; ";
				}
				System.out.println(current+"]");
				stringModel1 = stringModel1 + "@";
			}
		}
		System.out.println("Modello 2");
		String stringModel2 = "";
		Collection<ConstraintDefinition> constraints2 = model2.getConstraintDefinitions();
		for(ConstraintDefinition cd : constraints2){
			String template = cd.getName();
			String current = template+"[";
			stringModel2 = stringModel2 + "@"+template;
			if(template.equals("choice")||template.equals("exclusive choice")||template.equals("co-existence")){
				String branch = null;
				String firstbranch = null;
				for(Parameter p : cd.getParameters()){
					firstbranch = branch;
					branch = cd.getFirstBranch(p).getName();
					stringModel2 = stringModel2 + branch;
					current = current + branch+"; ";
				}
				System.out.println(current+"]");
				stringModel2 = stringModel2 + "@";
				stringModel2 = stringModel2 + "@" +template +branch + firstbranch + "@";
			}else{
				for(Parameter p : cd.getParameters()){
					String branch = cd.getFirstBranch(p).getName();
					stringModel2 = stringModel2 + branch;
					current = current + branch+"; ";
				}
				System.out.println(current+"]");
				stringModel2 = stringModel2 + "@";
			}
		}

		System.out.println("Modello 3");
		String stringModel3 = "";
		Collection<ConstraintDefinition> constraints3 = model3.getConstraintDefinitions();
		for(ConstraintDefinition cd : constraints3){
			String template = cd.getName();
			String current = template+"[";
			stringModel3 = stringModel3 + "@"+template;
			if(template.equals("choice")||template.equals("exclusive choice")||template.equals("co-existence")){
				String branch = null;
				String firstbranch = null;
				for(Parameter p : cd.getParameters()){
					firstbranch = branch;
					branch = cd.getFirstBranch(p).getName();
					stringModel3 = stringModel3 + branch;
					current = current + branch+"; ";
				}
				System.out.println(current+"]");
				stringModel3 = stringModel3 + "@";
				stringModel3 = stringModel3 + "@" +template +branch + firstbranch + "@";
			}else{
				for(Parameter p : cd.getParameters()){
					String branch = cd.getFirstBranch(p).getName();
					stringModel3 = stringModel3 + branch;
					current = current + branch+"; ";
				}
				System.out.println(current+"]");
				stringModel3 = stringModel3 + "@";
			}
		}

		String[] toBeChecked = stringModel1.split("@");
		for(String stringCd : toBeChecked){
			if(!stringCd.isEmpty())
				stringModel2 = stringModel2.replaceFirst("@"+stringCd+"@", "");
		}
		//stringModel2 = stringModel2.replaceAll("@", "");
		if(!stringModel2.equals("")){
			check = false;
		}

		for(String stringCd : toBeChecked){
			if(!stringCd.isEmpty())
				stringModel3 = stringModel3.replaceFirst("@"+stringCd+"@", "");
		}
		//stringModel3 = stringModel3.replaceAll("@", "");
		if(!stringModel3.equals("")){
			check = false;
		}

		return check;
	}
}

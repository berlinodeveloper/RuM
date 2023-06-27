package org.processmining.plugins.declareminer;

import java.util.Comparator;

import org.processmining.plugins.declareminer.enumtypes.DeclareTemplate;

public class TemplateComparator implements Comparator<DeclareTemplate>{

	@Override
	public int compare(DeclareTemplate template1, DeclareTemplate template2){
		int comparison=0;
		switch (template1) {
		case Exactly1:
			switch (template2) {
				case Existence: case Absence2:
					comparison = 1;
					break;
				default:
					break;
			}
			break;
		case Exactly2:
			switch (template2) {
				case Existence: case Existence2: case Absence3:
					comparison = 1;
					break;
				default:
					break;
			}
			break;
		case Existence:
			switch (template2) {
			case Exactly1: case Existence2: case Exactly2: case Init: case Existence3:
					comparison = -1;
					break;
			case Responded_Existence:
				comparison = 1;
				default:
					break;
			}
			break;					
		case Existence2:
			switch (template2) {
				case Existence:
					comparison = 1;
					break;
				case Existence3: case Exactly2:
					comparison = -1;
					break;							
				default:
					break;
			}
			break;
		case Existence3:
			switch (template2) {
				case Existence2: case Existence:
					comparison = 1;
					break;
				default:
					break;
			}
		break;
		case Init:
			switch (template2) {
			case Existence: case Precedence: case Responded_Existence:
				comparison = 1;
				break;
			default:
				break;
			}
			break;
		case Absence:
			switch (template2) {
				case Absence2: case Absence3: 
				/*case Responded_Existence: case Precedence: case Alternate_Precedence: 
				case Chain_Precedence: case Chain_Succession: case Succession: case Alternate_Succession: case Response: case Alternate_Response:
				case Chain_Response: case CoExistence:*/
					comparison = 1;
					break;
				default:
					break;
			}
		break;
		case Absence2:
			switch (template2) {
				case Absence3: 
					comparison = 1;
				break;
				case Absence: case Exactly1: 
					comparison = -1;
			default:
				break;
			}
		break;
		case Absence3:
			switch (template2) {
				case Absence: case Absence2: case Exactly2:
					comparison = -1;
				break;
			default:
				break;
			}
		break;				
		case Exclusive_Choice:
			switch (template2) {
			case Choice: case Not_CoExistence:
				comparison = 1;
				break;
			default:
				break;
			}
		break;
		case Choice:
			switch (template2) {
			case Exclusive_Choice:
				comparison = -1;
				break;
			default:
				break;
			}		
		break;
		case CoExistence:
			switch (template2) {
			case Responded_Existence: 
				comparison = 1;
				break;
			case Succession: case Alternate_Succession: case Chain_Succession: //case Absence: 
				comparison = -1;
				break;						
			default:
				break;
			}
		break;
		case Succession:
			switch (template2) {
			case CoExistence: case Response: case Precedence: case Responded_Existence: 
				comparison = 1;
				break;
			case Alternate_Succession: case Chain_Succession: //case Absence: 
				comparison = -1;
				break;							
			default:
				break;
			}
		break;
		case Alternate_Succession:
			switch (template2) {
			case CoExistence: case Alternate_Response: case Alternate_Precedence: case Response: case Succession: case Precedence: case Responded_Existence:
				comparison = 1;
				break;
			case Chain_Succession: //case Absence: 
				comparison = -1;
				break;						
			default:
				break;
			}
		break;
		case Chain_Succession:
			switch (template2) {
			case CoExistence: case Chain_Response: case Chain_Precedence: case Alternate_Response: case Alternate_Precedence:
			case Alternate_Succession: case Response: case Succession: case Precedence: case Responded_Existence:
				comparison = 1;
				break;
			/*case Absence:
				comparison = -1;
			break;*/
			default:
				break;
			}
		break;
		case Chain_Response:
			switch (template2) {
			case Alternate_Response: case Response: case Responded_Existence:  
				comparison = 1;
				break;
			case Chain_Succession: //case Absence: 
				comparison = -1;
				break;
			default:
				break;
			}
		break;
		case Alternate_Response:
			switch (template2) {
			case Response: case Responded_Existence:
				comparison = 1;
				break;
			case Chain_Response: case Alternate_Succession: case Chain_Succession: //case Absence:  
				comparison = -1;
				break;
			default:
				break;
			}
		break;				
		case Chain_Precedence:
			switch (template2) {
			case Alternate_Precedence: case Precedence: case Responded_Existence:
				comparison = 1;
				break;
			case Chain_Succession: //case Absence: 
				comparison = -1;
				break;
			default:
				break;
			}
		break;
		case Alternate_Precedence:
			switch (template2) {
			case Precedence: case Responded_Existence:
				comparison = 1;
				break;
			case Chain_Precedence: case Chain_Succession: case Alternate_Succession: //case Absence: 
				comparison = -1;
				break;
			default:
				break;
			}
		break;
		case Response:
			switch (template2) {
			case Responded_Existence: 
				comparison = 1;
				break;
			case Chain_Response: case Alternate_Response: case Chain_Succession: case Alternate_Succession: case Succession: //case Absence: 
				comparison = -1;
				break;						
			default:
				break;
			}
		break;
		case Precedence:
			switch (template2) {
			case Responded_Existence: 
				comparison = 1;
				break;
			case Chain_Precedence: case Alternate_Precedence: case Init: 
			case Alternate_Succession: case Chain_Succession: case Succession: //case Absence: 
				comparison = -1;
				break;						
				
			default:
				break;
			}
		break;
		case Responded_Existence:
			switch(template2){
				case Existence:
					comparison = -1;
				case Response: case Alternate_Response: case Chain_Response: case CoExistence: 
				case Succession: case Alternate_Succession: case Chain_Succession:
				 case Precedence: case Alternate_Precedence: case Chain_Precedence: case Init: //case Absence: 
					comparison = -1;
				break;
				default:
					break;
			}
		break;
		case Not_CoExistence:
			switch (template2) {
			case Not_Succession: case Not_Chain_Succession: 
				comparison = 1;
				break;
			case Exclusive_Choice:
				comparison = -1;
				break;
			default:
				break;
			}
		case Not_Succession: 
			switch(template2){
			case Not_Chain_Succession:
				comparison = 1;
				break;
			case Not_CoExistence:
				comparison = -1;
				break;
			default:
				break;
			}
		case Not_Chain_Succession:{
			switch(template2){
			case Not_Succession: case Not_CoExistence:
				comparison = -1;
				break;
			default:
				break;
			}
		}
			
		break;	
		default:
			break;
		}
		//System.out.println(template1+" "+template2+" "+comparison);
		return comparison;
	}

}

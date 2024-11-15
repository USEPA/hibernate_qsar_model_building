package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import java.util.ArrayList;
import java.util.List;

/**
* @author TMARTI02
* 
* Class to attempt to assign integer scores (GHS, EPA, VT, NT) to catmos LD50 values
* 
* TODO this is not completed- all the code needs to be checked carefully to assign categorical values from the various experimental values
* 
* 
*/
public class CatmosUtilities {

	String getEPACategory(double value) {
		if(value<=50) return "Category 1: oral rat LD50 ≤ 50 mg/kg";
		else if(value<=500) return "Category 2: 50 < oral rat LD50 ≤ 500 mg/kg";
		else if(value<=5000) return "Category 3: 500 < oral rat LD50 ≤ 5000 mg/kg";
		else if(value>5000) return "Category 4: oral rat LD50 > 5000 mg/kg";
		return null;
	}
	
	
	String getExperimentalConclusionEPA(String strvalue) {
		if (strvalue.equals("NA")) return null;
		
		if(strvalue.contains("-")) {
			try {
				double val1=Double.parseDouble(strvalue.substring(0,strvalue.indexOf("-")));
				double val2=Double.parseDouble(strvalue.substring(strvalue.indexOf("-")+1,strvalue.length()));

				String cat1=getEPACategory(val1);
				String cat2=getEPACategory(val2);
				
				if(cat1.equals(cat2)) {
					return cat1;
				} else {
					cat1=cat1.substring(cat1.indexOf(" ")+1,cat1.indexOf(":"));
					cat2=cat2.substring(cat2.indexOf(" ")+1,cat2.indexOf(":"));
					return "Category "+cat1+" or "+cat2;
				}
				
			} catch (Exception ex) {
				return null;
			}
			
		
		} else if(strvalue.contains("<")) {
			if(strvalue.equals("<2000")) return "Category 1, 2, or 3";			
			else if(strvalue.equals("<=50") || strvalue.equals("<=5")) return "Category 1: oral rat LD50 ≤ 50 mg/kg";
			else return null;
			
		} else if(strvalue.contains(">")) {
			if(strvalue.equals(">5001") || strvalue.equals(">10000")) {
				return "Category 4: oral rat LD50 > 2000 mg/kg";
			} else if (strvalue.equals(">2001")) {
				return "Category 3 or 4";
			} else if (strvalue.equals(">51")) {
				return "Category 2, 3, or 4";
			} else {
				return null;	
			}
		} else {
			double value=Double.parseDouble(strvalue);
			return getEPACategory(value);
		}
	}

	String getExperimentalConclusionNT(String strvalue) {
		
		if (strvalue.equals("NA")) return null;
		
		if(strvalue.contains("-")) {
			try {
				double val1=Double.parseDouble(strvalue.substring(0,strvalue.indexOf("-")));
				double val2=Double.parseDouble(strvalue.substring(strvalue.indexOf("-")+1,strvalue.length()));

				String cat1=getConclusionNT(val1);
				String cat2=getConclusionNT(val2);
				
				if(cat1.equals(cat2)) {
					return cat1;
				} else {
					cat1=cat1.substring(cat1.indexOf(" ")+1,cat1.indexOf(":"));
					cat2=cat2.substring(cat2.indexOf(" ")+1,cat2.indexOf(":"));
					return "Category "+cat1+" or "+cat2;
				}
				
			} catch (Exception ex) {
				return null;
			}
			
		
		} else if(strvalue.contains("<")) {

			double val=Double.parseDouble(strvalue.substring(1,strvalue.length()));
			
			if(val<=2000) {
				return "Not nontoxic: oral rat LD50 ≤ 2000 mg/kg";
			} else {
				return null;//we cant make conclusion
			}
			
		} else if(strvalue.contains(">")) {
			
			double val=Double.parseDouble(strvalue.substring(1,strvalue.length()));

			if(val>=2000) {
				return "Nontoxic: oral rat LD50 > 2000 mg/kg";
			} else {
				return null;//we cant make conclusion
			}
		} else {
			double value=Double.parseDouble(strvalue);
			return getEPACategory(value);
		}
	}
	private static Integer getGHSCategoryNumber(double value) {
		if (value<=5) return 1;
		else if(value<=50) return 2;
		else if(value<=300) return 3;
		else if(value<=2000) return 4;
		else if(value>2000) return 5;
		else return null;
	}
	
	private static String getGHSCategory(int category) {
		if(category==1) return "Category 1: oral rat LD50 ≤ 5 mg/kg";
		else if (category==2) return "Category 2: 5 < oral rat LD50 ≤ 50 mg/kg" ;
		else if (category==3) return "Category 3: 50 < oral rat LD50 ≤ 300 mg/kg";
		else if (category==4) return "Category 4: 300 < oral rat LD50 ≤ 2000 mg/kg";
		else if (category==5) return "Category 5: oral rat LD50 > 2000 mg/kg";
		else return null;
	}
	
	private static String getGHSCategoriesLessThan(double value) {
		List<Integer>cats=new ArrayList<>();
		if(value>=5) cats.add(1);
		if(value>5) cats.add(2);
		if(value>50) cats.add(3);
		if(value>300) cats.add(4);
		if(value>2000) cats.add(5);
		
		if(cats.size()==1) {
			return getGHSCategory(cats.get(0));
		} else if(cats.size()==2) {
			return "Category "+cats.get(0)+" or "+cats.get(1);
		} else {
			String result="Category ";
			for (int i=0;i<cats.size();i++) {
				result+=cats.get(i);
				if(i<cats.size()-2) result+=", ";
				else if(i<cats.size()-1) result+=", or ";
			}
			return result;
		} 
	}
	
	private static String getGHSCategoriesGreaterThan(double value) {
		
		List<Integer>cats=new ArrayList<>();

		//TODO this might not be correct:
		if (value<5) cats.add(1);		
		if(value<50) cats.add(2);
		if(value<300) cats.add(3);
		if(value<2000) cats.add(4);
		
		if(cats.size()==1) {
			return getGHSCategory(cats.get(0));
		} else if(cats.size()==2){
			return "Category "+cats.get(0)+ " or "+cats.get(1);
		} else if(cats.size()==3){
			return "Category "+cats.get(0)+ ", "+cats.get(1)+", or "+cats.get(2);
		} else {
			return "Category 1, 2, 3, or 4";
		}
	}
	
	static String getExperimentalConclusionGHS(String strvalue) {
		if (strvalue.equals("NA")) return null;
		
		if(strvalue.contains("-")) {
			try {
				double val1=Double.parseDouble(strvalue.substring(0,strvalue.indexOf("-")));
				double val2=Double.parseDouble(strvalue.substring(strvalue.indexOf("-")+1,strvalue.length()));

				int cat1=getGHSCategoryNumber(val1);
				int cat2=getGHSCategoryNumber(val2);
				
				if(cat1==cat2) {
					return getGHSCategory(cat1);
				} else {
					return "Category "+cat1+" or "+cat2;
				}
				
			} catch (Exception ex) {
				return null;
			}
		
		} else if(strvalue.contains("<")) {
			double val=Double.parseDouble(strvalue.substring(1,strvalue.length()));
			return getGHSCategoriesLessThan(val);

		} else if(strvalue.contains(">")) {
			
			double val=Double.parseDouble(strvalue.substring(1,strvalue.length()));

			
			if(strvalue.equals(">5001") || strvalue.equals(">10000") ||  strvalue.equals(">2001")) {
				return "Category 5: oral rat LD50 > 2000 mg/kg";
			} else if (strvalue.equals(">51")) {
				return "Category 3, 4, or 5";
			} else {
				return null;	
			}
		} else {
			double value=Double.parseDouble(strvalue);
			return getGHSCategory(getGHSCategoryNumber(value));
		}
		
	}
	private String getConclusionNT(double value) {
		if(value<=2000) return "Not nontoxic: oral rat LD50 ≤ 2000 mg/kg";
		else return "Nontoxic: oral rat LD50 > 2000 mg/kg";
	}
	
	private String getConclusionVT(double value) {
		if(value<=50) return "Very toxic: oral rat LD50 ≤ 50 mg/kg";
		else return "Not very toxic: oral rat LD50 > 50 mg/kg";
	}


	private String getConclusionEPA(Double value) {
		if(value==1) return "Category 1: oral rat LD50 ≤ 50 mg/kg";
		else if(value==2) return "Category 2: 50 < oral rat LD50 ≤ 500 mg/kg";
		else if(value==3) return "Category 3: 500 < oral rat LD50 ≤ 5000 mg/kg";
		else if(value==4) return "Category 4: oral rat LD50 > 5000 mg/kg";
		else return "NA";
	}
}

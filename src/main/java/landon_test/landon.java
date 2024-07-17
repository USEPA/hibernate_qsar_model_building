package landon_test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class landon {

	String name;
	String date; 
	
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues().create();

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		landon l=new landon();
		l.name="bob";
		l.date="7/17/24";
		
		System.out.println(gson.toJson(l));
		
	}

}

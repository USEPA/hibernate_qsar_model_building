package gov.epa.run_from_java.scripts;

import java.util.List;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;

public class ExpPropScript {
	
	private ExpPropPropertyService expPropPropertyService;
	
	public ExpPropScript() {
		this.expPropPropertyService = new ExpPropPropertyServiceImpl();
	}
	
	public static void main(String[] args) {
		ExpPropScript script = new ExpPropScript();
		List<ExpPropProperty> properties = script.expPropPropertyService.findByPropertyCategoryName("Physchem");
		
		for (ExpPropProperty property:properties) {
			System.out.println(property.getName());
		}
	}

}

package gov.epa.run_from_java.scripts;

import java.util.List;

import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyService;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropPropertyServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitServiceImpl;
import gov.epa.databases.dev_qsar.exp_prop.service.ExpPropUnitService;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

public class ExpPropScript {
	
	private ExpPropPropertyService expPropPropertyService;
	private ExpPropUnitService expPropUnitService;
	
	public ExpPropScript() {
		this.expPropPropertyService = new ExpPropPropertyServiceImpl();
	}
	
	public static void main(String[] args) {
		ExpPropScript script = new ExpPropScript();
//		List<ExpPropProperty> properties = script.expPropPropertyService.findByPropertyCategoryName("Physchem");

		/*
		ExpPropUnit u = new ExpPropUnit();
		u.setName("L/kg");
		u.setCreatedBy("cramslan");
		script.expPropUnitService.create(u);
		*/
		
		
		
		ExpPropProperty p = new ExpPropProperty();
		p.setName("LogBCF_Fish_WholeBody");
		p.setCreatedBy("cramslan");
		
		script.expPropPropertyService.create(p);
		
		/*
		for (ExpPropProperty property:properties) {
			System.out.println(property.getName());
		}
		*/
	}

}

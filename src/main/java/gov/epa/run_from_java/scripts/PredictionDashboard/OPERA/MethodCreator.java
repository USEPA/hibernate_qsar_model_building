package gov.epa.run_from_java.scripts.PredictionDashboard.OPERA;

import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.service.MethodServiceImpl;

public class MethodCreator {
	static MethodServiceImpl servMethod=new MethodServiceImpl();

	public static Method createMethod(String user,boolean store) {
		String name="kNN_OPERA_v2.9";
		String description="OPERA kNN method, version 2.9";
		String description_url="https://github.com/kmansouri/OPERA";
		String hyperparameters=null;
		Boolean isBinary=false;//TODO should we pass a property name and correct this for binary endpoints? or it doesnt matter if we store that for opera here
		String createdBy=user;
		Method method=new Method(name, description,description_url,hyperparameters, isBinary, createdBy);
		
		if (store) {
			if (servMethod.findByName(method.getName())==null) {
				method =servMethod.create(method);	
			} else {
				method=servMethod.findByName(method.getName());
//				System.out.println("Method exists in db");
			}
		}
		
		return method;
	}
}

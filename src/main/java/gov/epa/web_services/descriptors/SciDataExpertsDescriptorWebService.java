package gov.epa.web_services.descriptors;

import gov.epa.web_services.WebService;

public class SciDataExpertsDescriptorWebService extends WebService {
	
	private String descriptorName;

	public SciDataExpertsDescriptorWebService(String server, int port, String descriptorName) {
		super(server, port);
		this.descriptorName = descriptorName;
	}

}

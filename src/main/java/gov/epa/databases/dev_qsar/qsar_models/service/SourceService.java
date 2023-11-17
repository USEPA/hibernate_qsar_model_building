package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.Source;

public interface SourceService {
	
	public Source findByName(String sourceName);
	
	public Source findByName(String sourceName, Session session);

	

}

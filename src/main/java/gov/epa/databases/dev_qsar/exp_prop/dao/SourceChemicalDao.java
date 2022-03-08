package gov.epa.databases.dev_qsar.exp_prop.dao;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;

public interface SourceChemicalDao {
	
	public SourceChemical findMatch(SourceChemical sc, Session session);
	
}
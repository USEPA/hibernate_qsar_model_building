package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;

public interface SourceChemicalDao {
	
	public SourceChemical findMatch(SourceChemical sc, Session session);

	List<SourceChemical> findAll(Session session);
	
}

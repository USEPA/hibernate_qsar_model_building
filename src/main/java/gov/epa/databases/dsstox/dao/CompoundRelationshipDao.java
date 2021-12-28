package gov.epa.databases.dsstox.dao;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.CompoundRelationship;

public interface CompoundRelationshipDao {
	
	public CompoundRelationship findQsarReadyByParent(DsstoxCompound parent, Session session);

}

package gov.epa.databases.dsstox.service;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.CompoundRelationship;

public interface CompoundRelationshipService {
	
	CompoundRelationship findQsarReadyByParent(DsstoxCompound parent);

	CompoundRelationship findQsarReadyByParent(DsstoxCompound parent, Session session);

}

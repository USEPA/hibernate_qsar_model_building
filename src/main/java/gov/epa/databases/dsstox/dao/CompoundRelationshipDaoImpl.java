package gov.epa.databases.dsstox.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.CompoundRelationship;

public class CompoundRelationshipDaoImpl implements CompoundRelationshipDao {
	private static final String QUERY_QSAR_READY_BY_PARENT = "from CompoundRelationship cr "
			+ " where cr.predecessorCompound = :parent and cr.compoundRelationshipType.id = 1";
	
	@Override
	public CompoundRelationship findQsarReadyByParent(DsstoxCompound parent, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(QUERY_QSAR_READY_BY_PARENT);
		query.setParameter("parent", parent);
		return (CompoundRelationship) query.uniqueResult();
	}
}

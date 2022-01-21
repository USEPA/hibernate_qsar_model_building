package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;

public class ModelSetDaoImpl implements ModelSetDao {
	
	private static final String HQL_BY_ID = "from ModelSet ms where ms.id = :modelSetId";
	
	@Override
	public ModelSet findById(Long modelSetId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
		query.setParameter("modelSetId", modelSetId);
		return (ModelSet) query.uniqueResult();
	}

}

package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;

public class UnitDaoImpl implements UnitDao {
	
	private static final String HQL_BY_NAME = 
			"from Unit u where u.name = :unitName";
	
	@Override
	public Unit findByName(String unitName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("unitName", unitName);
		return (Unit) query.uniqueResult();
	}

}

package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public class PropertyDaoImpl implements PropertyDao {
	
	private static final String HQL_BY_NAME = 
			"from Property p where p.name = :propertyName";
	
	@Override
	public Property findByName(String propertyName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("propertyName", propertyName);
		return (Property) query.uniqueResult();
	}

}

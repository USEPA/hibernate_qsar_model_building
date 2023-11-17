package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public class PropertyDaoImpl implements PropertyDao {
	
	private static final String HQL_BY_NAME = 
			"from Property p where p.name = :propertyName";
	
	private static final String HQL_ALL = "from Property";
	
	@Override
	public Property findByName(String propertyName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("propertyName", propertyName);
		return (Property) query.uniqueResult();
	}


	@Override
	public List<Property> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<Property>) query.list();
	}

}

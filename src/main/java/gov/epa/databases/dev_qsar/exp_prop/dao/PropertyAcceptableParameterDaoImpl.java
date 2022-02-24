package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyAcceptableParameter;

public class PropertyAcceptableParameterDaoImpl implements PropertyAcceptableParameterDao {

	private static final String HQL_ALL = "from PropertyAcceptableParameter";
	
	@Override
	public List<PropertyAcceptableParameter> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<PropertyAcceptableParameter>) query.list();
	}

}

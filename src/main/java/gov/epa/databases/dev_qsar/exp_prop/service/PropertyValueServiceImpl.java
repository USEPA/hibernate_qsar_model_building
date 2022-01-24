package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropUnitDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropUnitDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropUnit;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class PropertyValueServiceImpl implements PropertyValueService {
	
	public ExpPropUnit findByName(String unitName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(unitName, session);
	}
	
	public ExpPropUnit findByName(String unitName, Session session) {
		Transaction t = session.beginTransaction();
		ExpPropUnitDao expPropUnitDao = new ExpPropUnitDaoImpl();
		ExpPropUnit expPropUnit = expPropUnitDao.findByName(unitName, session);
		t.rollback();
		return expPropUnit;
	}

	@Override
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep,
			boolean omitValueQualifiers) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByPropertyNameWithOptions(propertyName, useKeep, omitValueQualifiers, session);
	}

	@Override
	public List<PropertyValue> findByPropertyNameWithOptions(String propertyName, boolean useKeep,
			boolean omitValueQualifiers, Session session) {
		Transaction t = session.beginTransaction();
		PropertyValueDao propertyValueDao = new PropertyValueDaoImpl();
		List<PropertyValue> propertyValues = propertyValueDao.findByPropertyNameWithOptions(propertyName, useKeep, omitValueQualifiers, session);
		t.rollback();
		return propertyValues;
	}

}

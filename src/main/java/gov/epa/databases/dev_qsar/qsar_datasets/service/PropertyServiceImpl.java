package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ExpPropPropertyDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.ExpPropProperty;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.PropertyDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.PropertyDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;

public class PropertyServiceImpl implements PropertyService {
	
	private Validator validator;
	
	public PropertyServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Property findByName(String propertyName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByName(propertyName, session);
	}
	
	public Property findByName(String propertyName, Session session) {
		Transaction t = session.beginTransaction();
		PropertyDao propertyDao = new PropertyDaoImpl();
		Property property = propertyDao.findByName(propertyName, session);
		t.rollback();
		return property;
	}
	
	@Override
	public Property create(Property property) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(property, session);
	}

	@Override
	public Property create(Property property, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Property>> violations = validator.validate(property);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(property);
			session.flush();
			session.refresh(property);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return property;
	}
	
	@Override
	public List<Property> findAll() {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	@Override
	public List<Property> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PropertyDao propertyDao = new PropertyDaoImpl();
		List<Property> propertys = propertyDao.findAll(session);
		t.rollback();
		return propertys;
	}


}

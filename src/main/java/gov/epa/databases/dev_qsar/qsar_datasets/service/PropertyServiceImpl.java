package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
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
	public Set<ConstraintViolation<Property>> create(Property property) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(property, session);
	}

	@Override
	public Set<ConstraintViolation<Property>> create(Property property, Session session) {
		Set<ConstraintViolation<Property>> violations = validator.validate(property);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(property);
		session.flush();
		session.refresh(property);
		t.commit();
		return null;
	}

}

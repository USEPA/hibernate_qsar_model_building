package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyCategoryDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyCategoryDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyCategory;

/**
 * @author TMARTI02
 *
 */
public class PropertyCategoryServiceImpl implements PropertyCategoryService {

	private Validator validator;
	
	public PropertyCategoryServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	@Override
	public List<PropertyCategory> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	@Override
	public List<PropertyCategory> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PropertyCategoryDao propertyCategoryDao = new PropertyCategoryDaoImpl();
		List<PropertyCategory> propertyCategories = propertyCategoryDao.findAll(session);
		t.rollback();
		return propertyCategories;
	}

	@Override
	public PropertyCategory create(PropertyCategory propertyCategory) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyCategory, session);
	}

	@Override
	public PropertyCategory create(PropertyCategory propertyCategory, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyCategory>> violations = validator.validate(propertyCategory);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(propertyCategory);
			session.flush();
			session.refresh(propertyCategory);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return propertyCategory;
	}
	
	
}

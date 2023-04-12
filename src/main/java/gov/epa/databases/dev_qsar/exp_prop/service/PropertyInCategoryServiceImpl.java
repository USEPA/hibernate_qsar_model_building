package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyInCategoryDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyInCategoryDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyInCategory;


/**
 * @author TMARTI02
 *
 */
public class PropertyInCategoryServiceImpl implements PropertyInCategoryService {

	private Validator validator;
	
	public PropertyInCategoryServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	
	
	@Override
	public List<PropertyInCategory> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}

	@Override
	public List<PropertyInCategory> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PropertyInCategoryDao propertyInCategoryDao = new PropertyInCategoryDaoImpl();
		List<PropertyInCategory> propertyInCategories = propertyInCategoryDao.findAll(session);
		t.rollback();
		return propertyInCategories;
	}

	@Override
	public PropertyInCategory create(PropertyInCategory propertyInCategory) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyInCategory, session);
	}

	@Override
	public PropertyInCategory create(PropertyInCategory propertyInCategory, Session session)
			throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyInCategory>> violations = validator.validate(propertyInCategory);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(propertyInCategory);
			session.flush();
			session.refresh(propertyInCategory);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return propertyInCategory;
	
	}
	

}

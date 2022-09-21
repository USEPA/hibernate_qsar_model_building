package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PropertyValueDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;

public class PropertyValueServiceImpl implements PropertyValueService {
	
	private Validator validator;
	
	public PropertyValueServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	private static final Pattern EXP_PROP_ID_PATTERN = Pattern.compile("EXP0*([0-9]+)");
	
	@Override
	public PropertyValue findByExpPropId(String expPropId) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByExpPropId(expPropId, session);
	}

	@Override
	public PropertyValue findByExpPropId(String expPropId, Session session) {
		Matcher matcher = EXP_PROP_ID_PATTERN.matcher(expPropId);
		Long id = null;
		if (matcher.find()) {
			id = Long.parseLong(matcher.group(1));
		} else {
			return null;
		}
		
		Transaction t = session.beginTransaction();
		PropertyValueDao propertyValueDao = new PropertyValueDaoImpl();
		PropertyValue propertyValue = propertyValueDao.findById(id, session);
		t.rollback();
		return propertyValue;
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

	@Override
	public PropertyValue create(PropertyValue propertyValue) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(propertyValue, session);
	}

	@Override
	public PropertyValue create(PropertyValue propertyValue, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PropertyValue>> violations = validator.validate(propertyValue);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(propertyValue);
			session.flush();
			session.refresh(propertyValue);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			System.out.println(e);
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return propertyValue;
	}

}

package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;

public class CompoundServiceImpl implements CompoundService {
	
	Validator validator;
	
	public CompoundServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Compound findByDtxcidSmilesAndStandardizer(String dtxcid, String smiles, String standardizer) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByDtxcidSmilesAndStandardizer(dtxcid, smiles, standardizer, session);
	}
	
	public Compound findByDtxcidSmilesAndStandardizer(String dtxcid, String smiles, String standardizer, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		Compound compound = compoundDao.findByDtxcidSmilesAndStandardizer(dtxcid, smiles, standardizer, session);
		t.rollback();
		return compound;
	}
	
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByCanonQsarSmiles(canonQsarSmiles, session);
	}
	
	public List<Compound> findByCanonQsarSmiles(String canonQsarSmiles, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		List<Compound> compounds = compoundDao.findByCanonQsarSmiles(canonQsarSmiles, session);
		t.rollback();
		return compounds;
	}
	
	@Override
	public Compound create(Compound compound) throws ConstraintViolationException {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(compound, session);
	}
	
	
	@Override
	public void delete(Compound compound) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		delete(compound, session);
	}

	@Override
	public void delete(Compound compound, Session session) {
		if (compound.getId()==null) {
			return;
		}
		
		Transaction t = session.beginTransaction();
		session.remove(compound);
		session.flush();
		t.commit();
	}

	@Override
	public Compound create(Compound compound, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Compound>> violations = validator.validate(compound);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(compound);
			session.flush();
			session.refresh(compound);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return compound;
	}

	@Override
	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		List<Compound> compounds = compoundDao.findAllWithStandardizerSmilesNotNull(standardizer, session);
		t.rollback();
		return compounds;
	}

	@Override
	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findAllWithStandardizerSmilesNotNull(standardizer, session);
		
	}
}

package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDao;
import gov.epa.databases.dev_qsar.qsar_descriptors.dao.CompoundDaoImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;

public class CompoundServiceImpl implements CompoundService {
	
	private Validator validator;
	
	public CompoundServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findByDtxcidAndStandardizer(dtxcid, standardizer, session);
	}
	
	public Compound findByDtxcidAndStandardizer(String dtxcid, String standardizer, Session session) {
		Transaction t = session.beginTransaction();
		CompoundDao compoundDao = new CompoundDaoImpl();
		Compound compound = compoundDao.findByDtxcidAndStandardizer(dtxcid, standardizer, session);
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
	public Set<ConstraintViolation<Compound>> create(Compound compound) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return create(compound, session);
	}

	@Override
	public Set<ConstraintViolation<Compound>> create(Compound compound, Session session) {
		Set<ConstraintViolation<Compound>> violations = validator.validate(compound);
		if (!violations.isEmpty()) {
			return violations;
		}
		
		Transaction t = session.beginTransaction();
		session.save(compound);
		session.flush();
		session.refresh(compound);
		t.commit();
		return null;
	}

}

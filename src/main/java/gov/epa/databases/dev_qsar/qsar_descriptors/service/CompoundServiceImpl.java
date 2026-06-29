package gov.epa.databases.dev_qsar.qsar_descriptors.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
import gov.epa.run_from_java.scripts.SqlUtilities;

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
	
	
	public List<Compound> findAllWithStandardizerSmilesNotNullSql(String standardizer) {
		final String sql = """
				select canon_qsar_smiles, dtxcid, smiles
				from qsar_descriptors.compounds
				where standardizer = ? and smiles is not null
				""";

		// Optional connection-level hints for read-only query

		System.out.println("enter findAllWithStandardizerSmilesNotNullSql()");
		
		Connection conn = SqlUtilities.getConnectionPostgres();

		try {

			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, standardizer);

			// Fetch size tuning (driver-specific):
			// - PostgreSQL: a positive fetch size enables cursor-based streaming
			ps.setFetchSize(500);
			
			// Optional timeout in seconds
			ps.setQueryTimeout(60);

			List<Compound> results = new ArrayList<>();
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String canonSmiles = rs.getString(1);
				String dtxcid = rs.getString(2);
				String smiles = rs.getString(3);

				Compound compound = new Compound();

				compound.setCanonQsarSmiles(canonSmiles);
				compound.setDtxcid(dtxcid);
				compound.setSmiles(smiles);
				compound.setStandardizer(standardizer);
				results.add(compound);
			}

			return results;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	@Override
	public List<Compound> findAllWithStandardizerSmilesNotNull(String standardizer) {
		Session session = QsarDescriptorsSession.getSessionFactory().getCurrentSession();
		return findAllWithStandardizerSmilesNotNull(standardizer, session);
		
	}
}

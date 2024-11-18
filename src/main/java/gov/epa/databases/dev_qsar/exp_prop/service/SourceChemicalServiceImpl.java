package gov.epa.databases.dev_qsar.exp_prop.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.ParameterDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.dao.SourceChemicalDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.SourceChemicalDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.Parameter;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class SourceChemicalServiceImpl implements SourceChemicalService {
	
	private Validator validator;
	
	public SourceChemicalServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	@Override
	public SourceChemical findMatch(SourceChemical sc) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findMatch(sc, session);
	}

	@Override
	public SourceChemical findMatch(SourceChemical sc, Session session) {
		Transaction t = session.beginTransaction();
		SourceChemicalDao sourceChemicalDao = new SourceChemicalDaoImpl();
		SourceChemical sourceChemical = sourceChemicalDao.findMatch(sc, session);
		t.rollback();
		return sourceChemical;
	}

	@Override
	public SourceChemical create(SourceChemical sourceChemical) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(sourceChemical, session);
	}

	@Override
	public SourceChemical create(SourceChemical sourceChemical, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<SourceChemical>> violations = validator.validate(sourceChemical);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(sourceChemical);
			session.flush();
			session.refresh(sourceChemical);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return sourceChemical;
	}
	
	@Override
	public List<SourceChemical> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	@Override
	public List<SourceChemical> findAll(Session session) {
		Transaction t = session.beginTransaction();
		SourceChemicalDao sourceChemicalDao = new SourceChemicalDaoImpl();
		List<SourceChemical> parameters = sourceChemicalDao.findAll(session);
		t.rollback();
		return parameters;
	}
	
	@Override
	public List<SourceChemical> findAllFromSource(PublicSource ps) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAllFromSource(ps, session);
	}
	
	@Override
	public List<SourceChemical> findAllFromSource(PublicSource ps,Session session) {
		Transaction t = session.beginTransaction();
		SourceChemicalDao sourceChemicalDao = new SourceChemicalDaoImpl();
		List<SourceChemical> parameters = sourceChemicalDao.findAll(ps, session);
		t.rollback();
		return parameters;
	}

	
	/**
	 * SQL way of getting source chemicals for a public source. The literature source will only have an id filled in
	 * 
	 * @param ps
	 * @return
	 */
	public List<SourceChemical> findAllFromSourceSql(PublicSource ps) {

		List<SourceChemical>sourceChemicals=new ArrayList<>();

		String sql="SELECT id,source_dtxcid,source_dtxsid,source_dtxrid,source_smiles, source_casrn,"
				+ "source_chemical_name,fk_literature_source_id,"
				+ "created_at,created_by,updated_at,updated_by from exp_prop.source_chemicals\n"
				+ "where fk_public_source_id="+ps.getId()+"\n";  

//		System.out.println(sql);

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				SourceChemical sc=new SourceChemical();				

				sc.setId(rs.getLong(1));
				sc.setSourceDtxcid(rs.getString(2));
				sc.setSourceDtxsid(rs.getString(3));
				sc.setSourceDtxrid(rs.getString(4));
				sc.setSourceSmiles(rs.getString(5));
				sc.setSourceCasrn(rs.getString(6));
				sc.setSourceChemicalName(rs.getString(7));
				
				if(rs.getLong(8)!=0) {
					LiteratureSource ls=new LiteratureSource();
					ls.setId(rs.getLong(8));
					sc.setLiteratureSource(ls);
				}

				sc.setPublicSource(ps);

				sc.setCreatedAt(rs.getTimestamp(9));
				sc.setCreatedBy(rs.getString(10));
				
				sc.setUpdatedAt(rs.getTimestamp(11));
				sc.setUpdatedBy(rs.getString(12));
				
				sourceChemicals.add(sc);

			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return sourceChemicals;
	}

}

package gov.epa.databases.dev_qsar.exp_prop.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.LiteratureSourceDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.LiteratureSourceDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class LiteratureSourceServiceImpl implements LiteratureSourceService {
	
	private Validator validator;
	
	public LiteratureSourceServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public LiteratureSource findByName(String sourceName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public LiteratureSource findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		LiteratureSourceDao literatureSourceDao = new LiteratureSourceDaoImpl();
		LiteratureSource literatureSource = literatureSourceDao.findByName(sourceName, session);
		t.rollback();
		return literatureSource;
	}
	
	public List<LiteratureSource> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<LiteratureSource> findAll(Session session) {
		Transaction t = session.beginTransaction();
		LiteratureSourceDao literatureSourceDao = new LiteratureSourceDaoImpl();
		List<LiteratureSource> literatureSources = literatureSourceDao.findAll(session);
		t.rollback();
		return literatureSources;
	}

	@Override
	public LiteratureSource create(LiteratureSource ls) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(ls, session);
	}

	@Override
	public LiteratureSource create(LiteratureSource ls, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<LiteratureSource>> violations = validator.validate(ls);
		if (!violations.isEmpty()) {
			
			for(ConstraintViolation<LiteratureSource>violation:violations) {
				System.out.println(violation.getMessage());
			}
			
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.persist(ls);
			session.flush();
			session.refresh(ls);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return ls;
	}

	@Override
	public void createBatchSQL(List<LiteratureSource> litSources, Connection conn) {

		long t1=System.currentTimeMillis();
		
		String [] fieldNames= {"name","title","author","citation","journal","year","volume","issue",
		"pages","url","doi","notes","created_by","created_at"};
		
		String sql = SqlUtilities.createSqlInsertWithTimeStamp(fieldNames,"literature_sources","exp_prop");
		
//		PreparedStatement prep = conn.prepareStatement(sql);
//		https://stackoverflow.com/questions/4224228/preparedstatement-with-statement-return-generated-keys
		
		try {
			PreparedStatement prep = conn.prepareStatement(sql, new String[]{"id"});//for some reason much faster than using Statement.RETURN_GENERATED_KEYS!

			for (LiteratureSource sc:litSources) {

				Integer i=0;
				i=SqlUtilities.setString(prep, sc.getName(), i);
				i=SqlUtilities.setString(prep, sc.getTitle(), i);
				i=SqlUtilities.setString(prep, sc.getAuthor(), i);
				i=SqlUtilities.setString(prep, sc.getCitation(), i);
				i=SqlUtilities.setString(prep, sc.getJournal(), i);
				i=SqlUtilities.setString(prep, sc.getYear(), i);
				i=SqlUtilities.setString(prep, sc.getVolume(), i);
				i=SqlUtilities.setString(prep, sc.getIssue(), i);
				i=SqlUtilities.setString(prep, sc.getPages(), i);
				i=SqlUtilities.setString(prep, sc.getUrl(), i);
				i=SqlUtilities.setString(prep, sc.getDoi(), i);
				i=SqlUtilities.setString(prep, sc.getNotes(), i);
				i=SqlUtilities.setString(prep, sc.getCreatedBy(), i);
				prep.addBatch();

				//System.out.println(prep);

			}

			prep.executeBatch();

			long t1a=System.currentTimeMillis();

			ResultSet keys=prep.getGeneratedKeys();

			Iterator<LiteratureSource> iterator=litSources.iterator();
			while (keys!=null && keys.next()) {
				LiteratureSource sc=iterator.next();
				Long key = keys.getLong(1);
				sc.setId(key);
			}

			long t2=System.currentTimeMillis();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}

}

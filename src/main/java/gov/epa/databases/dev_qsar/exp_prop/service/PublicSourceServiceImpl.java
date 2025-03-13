package gov.epa.databases.dev_qsar.exp_prop.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.dao.PublicSourceDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.PublicSourceDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class PublicSourceServiceImpl implements PublicSourceService {
	
	private Validator validator;
	
	public PublicSourceServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public PublicSource findByName(String sourceName) {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findByName(sourceName, session);
	}
	
	public PublicSource findByName(String sourceName, Session session) {
		Transaction t = session.beginTransaction();
		PublicSourceDao publicSourceDao = new PublicSourceDaoImpl();
		PublicSource publicSource = publicSourceDao.findByName(sourceName, session);
		t.rollback();
		return publicSource;
	}
	
	public List<PublicSource> findAll() {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return findAll(session);
	}
	
	public List<PublicSource> findAll(Session session) {
		Transaction t = session.beginTransaction();
		PublicSourceDao publicSourceDao = new PublicSourceDaoImpl();
		List<PublicSource> publicSources = publicSourceDao.findAll(session);
		t.rollback();
		return publicSources;
	}

	@Override
	public PublicSource create(PublicSource ps) throws ConstraintViolationException {
		Session session = ExpPropSession.getSessionFactory().getCurrentSession();
		return create(ps, session);
	}

	
	public void createBatchSQL(List<PublicSource>publicSources,Connection conn) {
		

		long t1=System.currentTimeMillis();
		

		String [] fieldNames={"name","description", "type","url","access_date","created_by","created_at"};

		String sql = SqlUtilities.createSqlInsertWithTimeStamp(fieldNames,"public_sources","exp_prop");
		
//		PreparedStatement prep = conn.prepareStatement(sql);
//		https://stackoverflow.com/questions/4224228/preparedstatement-with-statement-return-generated-keys
		
		try {
			PreparedStatement prep = conn.prepareStatement(sql, new String[]{"id"});//for some reason much faster than using Statement.RETURN_GENERATED_KEYS!



			List<Long>predictionDashboardIds=new ArrayList<>();

			for (PublicSource sc:publicSources) {

				Integer i=0;
				i=SqlUtilities.setString(prep, sc.getName(), i);
				i=SqlUtilities.setString(prep, sc.getDescription(), i);
				i=SqlUtilities.setString(prep, sc.getType(), i);
				i=SqlUtilities.setString(prep, sc.getUrl(), i);
				i=SqlUtilities.setString(prep, sc.getAccessDate()+"", i);
				i=SqlUtilities.setString(prep, sc.getCreatedBy(), i);

				prep.addBatch();

				//System.out.println(prep);

			}

			prep.executeBatch();

			long t1a=System.currentTimeMillis();

			ResultSet keys=prep.getGeneratedKeys();

			Iterator<PublicSource> iterator=publicSources.iterator();
			while (keys!=null && keys.next()) {
				PublicSource sc=iterator.next();
				Long key = keys.getLong(1);
				sc.setId(key);
			}

			long t2=System.currentTimeMillis();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public PublicSource create(PublicSource ps, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PublicSource>> violations = validator.validate(ps);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(ps);
			session.flush();
			session.refresh(ps);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		} catch (Exception e) {
			t.rollback();
			throw e;
		}
		
		return ps;
	}

}

package gov.epa.databases.dev_qsar.exp_prop.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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
import gov.epa.databases.dev_qsar.exp_prop.dao.SourceChemicalDao;
import gov.epa.databases.dev_qsar.exp_prop.dao.SourceChemicalDaoImpl;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
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
			session.persist(sourceChemical);
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
	 * This version gets the generated keys in one batch
	 * 
	 * @param predictionDashboards
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	@Override
	public void createBatchSql(List<SourceChemical> sourceChemicals, Connection conn) throws SQLException {
		
		long t1=System.currentTimeMillis();
		
		String [] fieldNames= {"source_casrn","source_smiles","source_chemical_name",
				"source_dtxsid","source_dtxcid", "source_dtxrid",
				"fk_public_source_id","fk_literature_source_id", "created_by","created_at" };

		
		String sql = SqlUtilities.createSqlInsertWithTimeStamp(fieldNames,"source_chemicals","exp_prop");
		

//		https://stackoverflow.com/questions/4224228/preparedstatement-with-statement-return-generated-keys
		PreparedStatement prep = conn.prepareStatement(sql, new String[]{"id"});//for some reason much faster than using Statement.RETURN_GENERATED_KEYS!
//		PreparedStatement prep = conn.prepareStatement(sql);
		
		for (int counter = 0; counter < sourceChemicals.size(); counter++) {

			SourceChemical sc=sourceChemicals.get(counter);

			Integer i=0;
			i=SqlUtilities.setString(prep, sc.getSourceCasrn(), i);
			i=SqlUtilities.setString(prep, sc.getSourceSmiles(), i);
			i=SqlUtilities.setString(prep, sc.getSourceChemicalName(), i);
			i=SqlUtilities.setString(prep, sc.getSourceDtxsid(), i);
			i=SqlUtilities.setString(prep, sc.getSourceDtxcid(), i);
			i=SqlUtilities.setString(prep, sc.getSourceDtxrid(), i);
			
			if(sc.getPublicSource()!=null) {
				prep.setLong(++i,sc.getPublicSource().getId());
			} else {
				prep.setNull(++i, Types.BIGINT);
			}
			
			if(sc.getLiteratureSource()!=null) {
//				System.out.println(sc.getLiteratureSource().getCitation());
				prep.setLong(++i,sc.getLiteratureSource().getId());
			} else {
				prep.setNull(++i, Types.BIGINT);
			}

			prep.setString(++i, sc.getCreatedBy());
			prep.addBatch();

			//System.out.println(prep);

		}

		prep.executeBatch();
		
		long t1a=System.currentTimeMillis();
		
		ResultSet keys=prep.getGeneratedKeys();

		Iterator<SourceChemical> iterator=sourceChemicals.iterator();
		while (keys!=null && keys.next()) {
			SourceChemical sc=iterator.next();
			Long key = keys.getLong(1);
			sc.setId(key);
		}
		
		long t2=System.currentTimeMillis();
		
	}

	
	
	/**
	 * SQL way of getting source chemicals for a public source. The literature source will only have an id filled in
	 * 
	 * @param ps
	 * @return
	 */
	
	@Override
	public List<SourceChemical> findAllFromSourceSql(PublicSource ps) {

		List<SourceChemical>sourceChemicals=new ArrayList<>();

		String sql="SELECT distinct sc.id,source_dtxcid,source_dtxsid,source_dtxrid,source_smiles, source_casrn,"
				+ "source_chemical_name,sc.fk_literature_source_id,"
				+ "sc.created_at,sc.created_by,sc.updated_at,sc.updated_by from exp_prop.source_chemicals sc\n"
//				+"join exp_prop.property_values pv  on sc.id = pv.fk_source_chemical_id\n"		
				+ "where sc.fk_public_source_id="+ps.getId()+"\n";  

		
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
	
	
	public List<SourceChemical> findAllFromDateCreated(String dateMin,String dateMax) {

		List<SourceChemical>sourceChemicals=new ArrayList<>();

		String sql="SELECT distinct sc.id,source_dtxcid,source_dtxsid,source_dtxrid,source_smiles, source_casrn,"
				+ "source_chemical_name,sc.fk_literature_source_id,sc.fk_public_source_id,"
				+ "sc.created_at,sc.created_by,sc.updated_at,sc.updated_by from exp_prop.source_chemicals sc\n"
//				+"join exp_prop.property_values pv  on sc.id = pv.fk_source_chemical_id\n"		
				+ "where created_at > '"+dateMin+"' and created_at < '"+dateMax+"';";  

		
		System.out.println(sql);

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				SourceChemical sc=new SourceChemical();				

				
				int col=1;
				
				sc.setId(rs.getLong(col++));
				sc.setSourceDtxcid(rs.getString(col++));
				sc.setSourceDtxsid(rs.getString(col++));
				sc.setSourceDtxrid(rs.getString(col++));
				sc.setSourceSmiles(rs.getString(col++));
				sc.setSourceCasrn(rs.getString(col++));
				sc.setSourceChemicalName(rs.getString(col++));
				
				if(rs.getLong(col)!=0) {
					LiteratureSource ls=new LiteratureSource();
					ls.setId(rs.getLong(col));
					sc.setLiteratureSource(ls);
				}
				col++;
				
				if(rs.getLong(col)!=0) {
					PublicSource ps=new PublicSource();
					ps.setId(rs.getLong(col));
					sc.setPublicSource(ps);
				}
				col++;
				

				sc.setCreatedAt(rs.getTimestamp(col++));
				sc.setCreatedBy(rs.getString(col++));
				
				sc.setUpdatedAt(rs.getTimestamp(col++));
				sc.setUpdatedBy(rs.getString(col++));
				
				sourceChemicals.add(sc);

			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return sourceChemicals;
	}
	
	@Override
	//Much faster
	public List<SourceChemical> findAllSql() {

		List<SourceChemical>sourceChemicals=new ArrayList<>();

		String sql="SELECT id,source_dtxcid,source_dtxsid,source_dtxrid,source_smiles, source_casrn,"
				+ "source_chemical_name,fk_literature_source_id,fk_public_source_id,"
				+ "created_at,created_by,updated_at,updated_by from exp_prop.source_chemicals;";

//		System.out.println(sql);

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				SourceChemical sc=new SourceChemical();				

				int i=0;
				
				sc.setId(rs.getLong(++i));
				sc.setSourceDtxcid(rs.getString(++i));
				sc.setSourceDtxsid(rs.getString(++i));
				sc.setSourceDtxrid(rs.getString(++i));
				sc.setSourceSmiles(rs.getString(++i));
				sc.setSourceCasrn(rs.getString(++i));
				sc.setSourceChemicalName(rs.getString(++i));
				
				if(rs.getLong(++i)!=0) {
					LiteratureSource ls=new LiteratureSource();
					ls.setId(rs.getLong(i));
					sc.setLiteratureSource(ls);
				}
				
				if(rs.getLong(++i)!=0) {
					PublicSource ps=new PublicSource();
					ps.setId(rs.getLong(i));
					sc.setPublicSource(ps);
				}

				sc.setCreatedAt(rs.getTimestamp(++i));
				sc.setCreatedBy(rs.getString(++i));
				
				sc.setUpdatedAt(rs.getTimestamp(++i));
				sc.setUpdatedBy(rs.getString(++i));
				
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

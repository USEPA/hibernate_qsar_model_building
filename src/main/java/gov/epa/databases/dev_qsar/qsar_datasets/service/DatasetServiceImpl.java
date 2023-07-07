package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DatasetServiceImpl implements DatasetService {

	private Validator validator;
	
	public DatasetServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public Dataset findByName(String datasetName) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findByName(datasetName, session);
	}
	
	public Dataset findByName(String datasetName, Session session) {
		Transaction t = session.beginTransaction();
		DatasetDao datasetDao = new DatasetDaoImpl();
		Dataset dataset = datasetDao.findByName(datasetName, session);
		t.rollback();
		return dataset;
	}
	
	@Override
	public Dataset create(Dataset dataset) throws ConstraintViolationException {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return create(dataset, session);
	}

	@Override
	public Dataset create(Dataset dataset, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<Dataset>> violations = validator.validate(dataset);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(dataset);
			session.flush();
			session.refresh(dataset);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return dataset;
	}
	
	
	
	public void delete(long id) {
				
		Dataset dataset=findById(id);
		
		if (dataset==null) {
			System.out.println("dataset id="+id+" is not in database");
			return;
		}
		
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		System.out.println(dataset.getName());
		
		try {
			Transaction t = session.beginTransaction();
			System.out.print("Deleting dataset...");
			session.delete(dataset);
			System.out.print("done\n");
			
//			System.out.print("Flushing...");
//			session.flush();
//			System.out.print("done\n");
			
			System.out.print("Committing...");
			t.commit();
			System.out.print("done\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Runs much faster deletes using a series of SQL command
	 * 
	 * @param id
	 */
	public void deleteSQL(long id) {

		System.out.println("Deleting dataset id="+id);
		
		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String sqlDID="delete from qsar_datasets.datasets_in_dashboard did\n"+ 
		"where did.fk_datasets_id="+id+";";
		SqlUtilities.runSQLUpdate(conn, sqlDID);

		
		String sqlDPC="delete from qsar_datasets.data_point_contributors dpc\n"+ 
		"using qsar_datasets.data_points dp\n"+
		"where dp.fk_dataset_id="+id+" and dpc.fk_data_point_id =dp.id;";
		SqlUtilities.runSQLUpdate(conn, sqlDPC);
		
		String sqlDP="delete from qsar_datasets.data_points dp\n"+
		"where dp.fk_dataset_id="+id+";";
		
		SqlUtilities.runSQLUpdate(conn, sqlDP);
		
		String sqlD="delete from qsar_datasets.datasets d\n"+
		"where d.id="+id+";";
		SqlUtilities.runSQLUpdate(conn, sqlD);
		

	}
	

	@Override
	public Dataset findById(Long datasetId) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findById(datasetId, session);
	}

	@Override
	public Dataset findById(Long datasetId, Session session) {
		Transaction t = session.beginTransaction();
		DatasetDao datasetDao = new DatasetDaoImpl();
		Dataset dataset = datasetDao.findById(datasetId, session);
		t.rollback();
		return dataset;
	}
	


}

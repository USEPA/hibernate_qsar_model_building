package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;

public class PredictionDashboardDaoImpl implements PredictionDashboardDao {

	//Following works when Splitting is an object:
//	private static final String HQL_BY_IDs = "select p from Prediction p "
//				+ "join p.model m "
//				+ "join p.splitting s "
//				+ "where m.id = :modelId and s.id = :splittingId";
		
	//Following works when Splitting is just a foreign key:
	private static final String HQL_BY_IDs = "select p from PredictionDashboard p "
			+ "join p.model m \n"
			+ "join p.dsstoxRecord dr "
			+ "where m.id = :modelId and dr.id = :dsstoxRecordId";
	
	
	private static final String HQL_BY_SOURCE_NAME_AND_DTXSID="select p from PredictionDashboard p\r\n"
			+ "join FETCH p.qsarPredictedADEstimates\r\n" // avoids lazily loading error
			+ "join p.model m \r\n"
			+ "join m.source s\r\n"
			+ "join p.dsstoxRecord dr\r\n"
			+ "where s.name = :sourceName and dr.dtxsid = :dtxsid";
			
	@Override
	public PredictionDashboard findByIds(Long modelId, Long dsstoxRecordId, Session session) {
		
//		System.out.println(HQL_BY_IDs);
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<PredictionDashboard> query = session.createQuery(HQL_BY_IDs,PredictionDashboard.class);
			
		query.setParameter("modelId", modelId);
		query.setParameter("dsstoxRecordId", dsstoxRecordId);
		return query.uniqueResult();
	}

	@Override
	public List<PredictionDashboard> findBySourceNameAndDTXSID(String sourceName, String dtxsid, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		
//		System.out.println(HQL_BY_SOURCE_NAME_AND_DTXSID);
		
		Query<PredictionDashboard> query = session.createQuery(HQL_BY_SOURCE_NAME_AND_DTXSID,PredictionDashboard.class);
			
		query.setParameter("sourceName", sourceName);
		query.setParameter("dtxsid", dtxsid);
		return query.list();
	}

}

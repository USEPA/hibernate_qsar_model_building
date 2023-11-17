package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedProperty;

public class QsarPredictedPropertyDaoImpl implements QsarPredictedPropertyDao {

	
	private static final String HQL_BY_MODEL_ID = "select p from QsarPredictedProperty p "
			+ "join p.model m "
			+ "where m.id = :modelId";
	
	private static final String HQL = "select p from QsarPredictedProperty p "
			+ "join p.model m "
			+ "where m.id = :modelId and p.dtxcid = :dtxcid and p.canonQsarSmiles = :canonQsarSmiles";

	@Override
	public List<QsarPredictedProperty> findByModelId(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return (List<QsarPredictedProperty>) query.list();
	}

	@Override
	public QsarPredictedProperty find(Long modelId, String dtxcid, String canonQsarSmiles, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL);
		query.setParameter("modelId", modelId);
		query.setParameter("canonQsarSmiles", canonQsarSmiles);
		query.setParameter("dtxcid", dtxcid);
		
		if (query.list()!=null && query.list().size()>0) {
			return (QsarPredictedProperty) query.list().get(0);	
		} else {
			return null;
		}
	}
}

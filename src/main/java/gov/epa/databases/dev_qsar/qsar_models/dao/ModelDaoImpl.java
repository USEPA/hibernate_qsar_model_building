package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

public class ModelDaoImpl implements ModelDao {
	
	private static final String HQL_BY_ID = "from Model m where m.id = :modelId";
	private static final String HQL_BY_DATASET_NAME = "from Model m where m.datasetName = :datasetName";
	
	@Override
	public Model findById(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_ID);
		query.setParameter("modelId", modelId);
		return (Model) query.uniqueResult();
	}

	@Override
	public List<Model> findByDatasetName(String datasetName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DATASET_NAME);
		query.setParameter("datasetName", datasetName);
		return (List<Model>) query.list();
	}

}

package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

public class ModelStatisticDaoImpl implements ModelStatisticDao {
	
	private static final String HQL_BY_MODEL_ID = "from ModelStatistic ms where ms.model.id = :modelId";

	@Override
	public List<ModelStatistic> findByModelId(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return (List<ModelStatistic>) query.list();
	}

}

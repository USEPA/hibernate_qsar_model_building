package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

public class ModelStatisticDaoImpl implements ModelStatisticDao {
	
//	private static final String HQL_BY_MODEL_ID = "from ModelStatistic ms where ms.model.id = :modelId";

	private static final String HQL_BY_IDs = "select ms from ModelStatistic ms "
			+ "join ms.model m "
			+ "join ms.statistic s "
			+ "where m.id = :modelId and s.id = :statisticId";
		
	@Override
	public List<ModelStatistic> findByModelId(Long modelId, Long statisticId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_IDs);
		query.setParameter("modelId", modelId);
		query.setParameter("statisticId",statisticId);
		return (List<ModelStatistic>) query.list();
	}

}

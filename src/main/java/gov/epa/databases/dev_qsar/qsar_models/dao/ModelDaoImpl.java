package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

public class ModelDaoImpl implements ModelDao {
	
	private static final String HQL_GET_ALL = "select m from Model m ";
	private static final String HQL_BY_ID = "from Model m where m.id = :modelId";
	private static final String HQL_BY_IDS = "from Model m where m.id in (:modelIds)";
	private static final String HQL_BY_IDS_IN_RANGE_INCLUSIVE = "from Model m where m.id >= :minModelId and m.id <= :maxModelId";
	private static final String HQL_BY_DATASET_NAME = "from Model m where m.datasetName = :datasetName";
	private static final String HQL_BY_MODEL_SET_ID = "select m from Model m "
			+ "join m.modelInModelSets mms "
			+ "join mms.modelSet ms "
			+ "where ms.id = :modelSetId";
	
	@Override
	public Model findById(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<Model> query = session.createQuery(HQL_BY_ID,Model.class);
		query.setParameter("modelId", modelId);
		return query.uniqueResult();
	}
	
	@Override
	public List<Model> getAll(Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<Model> query = session.createQuery(HQL_GET_ALL,Model.class);
		return query.list();
	}
	
	@Override
	public List<Model> findByIdIn(Collection<Long> modelIds, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<Model> query = session.createQuery(HQL_BY_IDS,Model.class);
		query.setParameter("modelIds", modelIds);
		return query.list();
	}
	
	@Override
	public List<Model> findByIdInRangeInclusive(Long minModelId, Long maxModelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<Model> query = session.createQuery(HQL_BY_IDS_IN_RANGE_INCLUSIVE,Model.class);
		query.setParameter("minModelId", minModelId);
		query.setParameter("maxModelId", maxModelId);
		return query.list();
	}

	@Override
	public List<Model> findByDatasetName(String datasetName, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<Model> query = session.createQuery(HQL_BY_DATASET_NAME,Model.class);
		query.setParameter("datasetName", datasetName);
		return query.list();
	}
	
	@Override
	public List<Model> findByModelSetId(Long modelSetId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query<Model> query = session.createQuery(HQL_BY_MODEL_SET_ID,Model.class);
		query.setParameter("modelSetId", modelSetId);
		return query.list();
	}

}

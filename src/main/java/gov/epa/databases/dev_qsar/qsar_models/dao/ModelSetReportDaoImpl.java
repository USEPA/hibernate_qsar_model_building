package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;

public class ModelSetReportDaoImpl implements ModelSetReportDao {
	
	private static final String HQL_BY_MODEL_SET_ID_AND_MODEL_DATA = "select msr from ModelSetReport msr "
			+ "join msr.modelSet ms "
			+ "where ms.id = :modelSetId "
			+ "and msr.datasetName = :datasetName "
			+ "and msr.descriptorSetName = :descriptorSetName "
			+ "and msr.splittingName = :splittingName";
	private static final String HQL_BY_MODEL_SET_ID = "select msr from ModelSetReport msr "
			+ "join msr.modelSet ms "
			+ "where ms.id = :modelSetId";

	@Override
	public ModelSetReport findByModelSetIdAndModelData(Long modelSetId, String datasetName, String descriptorSetName, String splittingName, 
			Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_SET_ID_AND_MODEL_DATA);
		query.setParameter("modelSetId", modelSetId);
		query.setParameter("datasetName", datasetName);
		query.setParameter("descriptorSetName", descriptorSetName);
		query.setParameter("splittingName", splittingName);
		return (ModelSetReport) query.uniqueResult();
	}
	
	@Override
	public List<ModelSetReport> findByModelSetId(Long modelSetId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_SET_ID);
		query.setParameter("modelSetId", modelSetId);
		return (List<ModelSetReport>) query.list();
	}

}

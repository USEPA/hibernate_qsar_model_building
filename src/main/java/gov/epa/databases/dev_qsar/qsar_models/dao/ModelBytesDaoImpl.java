package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

public class ModelBytesDaoImpl implements ModelBytesDao {
	
	private static final String HQL_BY_MODEL_ID = "select mb from ModelBytes mb "
			+ "join mb.model m "
			+ "where m.id = :modelId order by mb.id";

	@Override
	public List<ModelBytes> findByModelId(Long modelId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		return (List<ModelBytes>) query.getResultList();
	}

}

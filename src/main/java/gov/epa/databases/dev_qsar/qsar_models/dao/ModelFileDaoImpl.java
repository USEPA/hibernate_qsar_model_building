package gov.epa.databases.dev_qsar.qsar_models.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelFile;

public class ModelFileDaoImpl implements ModelFileDao {
	
	private static final String HQL_BY_MODEL_ID = "select mf from ModelFile mf "
			+ "join mf.model m\n"
			+ "join mf.fileType f\n"
			+ "where m.id = :modelId and f.id = :fileTypeId";

	@Override
	public ModelFile findByModelId(Long modelId, Long fileTypeId, Session session) {
		if (session==null) { session = QsarModelsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_MODEL_ID);
		query.setParameter("modelId", modelId);
		query.setParameter("fileTypeId", fileTypeId);
		return (ModelFile) query.uniqueResult();
	}

}

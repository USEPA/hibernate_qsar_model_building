package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import gov.epa.databases.dev_qsar.qsar_models.entity.QsarPredictedProperty;

public interface QsarPredictedPropertyDao {
	public List<QsarPredictedProperty> findByModelId(Long modelId, Session session);
	
	
	public QsarPredictedProperty find(Long modelId,String dtxcid,String qsarSmiles, Session session);
	
}

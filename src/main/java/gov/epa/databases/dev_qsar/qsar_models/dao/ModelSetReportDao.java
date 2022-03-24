package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;

public interface ModelSetReportDao {
	
	public ModelSetReport findByModelSetIdAndModelData(Long modelSetId, String datasetName, String splittingName,
			Session session);

	public List<ModelSetReport> findByModelSetId(Long modelSetId, Session session);

}

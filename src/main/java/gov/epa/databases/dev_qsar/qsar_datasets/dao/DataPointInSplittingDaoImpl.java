package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;

public class DataPointInSplittingDaoImpl implements DataPointInSplittingDao {

	private static final String HQL_BY_DATASET_NAME_AND_SPLITTING_NAME = "select distinct dpis from DataPointInSplitting dpis "
			+ "join fetch dpis.splitting s " + "join fetch dpis.dataPoint dp " + "join fetch dp.dataset d "
			+ "left join fetch dp.dataPointContributors dpc "
			+ "where d.name = :datasetName and s.name = :splittingName";

	@Override
	public List<DataPointInSplitting> findByDatasetNameAndSplittingName(String datasetName, String splittingName,
			Session session) {
		if (session == null) {
			session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		}
		Query<DataPointInSplitting> query = session.createQuery(HQL_BY_DATASET_NAME_AND_SPLITTING_NAME,
				DataPointInSplitting.class);
		query.setParameter("datasetName", datasetName);
		query.setParameter("splittingName", splittingName);
		return query.list();
	}

}

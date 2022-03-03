package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;

public class SplittingDaoImpl implements SplittingDao {
	
	private static final String HQL_BY_NAME = "from Splitting s where s.name = :splittingName";
	private static final String HQL_BY_DATASET_NAME = "select distinct s from Splitting s "
			+ "join fetch s.dataPointsInSplitting dpis "
			+ "join fetch dpis.dataPoint dp "
			+ "join fetch dp.dataset d "
			+ "where d.name = :datasetName";
	private static final String HQL_BY_DATASET_NAME_AND_SPLITTING_NAME = "select distinct s from Splitting s "
			+ "join fetch s.dataPointsInSplitting dpis "
			+ "join fetch dpis.dataPoint dp "
			+ "join fetch dp.dataset d "
			+ "where d.name = :datasetName "
			+ "and s.name = :splittingName";
	
	@Override
	public Splitting findByName(String splittingName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("splittingName", splittingName);
		return (Splitting) query.uniqueResult();
	}
	
	@Override
	public List<Splitting> findByDatasetName(String datasetName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DATASET_NAME);
		query.setParameter("datasetName", datasetName);
		return (List<Splitting>) query.list();
	}
	
	@Override
	public Splitting findByDatasetNameAndSplittingName(String datasetName, String splittingName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DATASET_NAME_AND_SPLITTING_NAME);
		query.setParameter("datasetName", datasetName);
		query.setParameter("splittingName", splittingName);
		return (Splitting) query.uniqueResult();
	}

}

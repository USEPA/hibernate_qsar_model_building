package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.dao.StatisticDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.StatisticDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class StatisticServiceImpl implements StatisticService {
	
	public Statistic findByName(String statisticName) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByName(statisticName, session);
	}
	
	public Statistic findByName(String statisticName, Session session) {
		Transaction t = session.beginTransaction();
		StatisticDao statisticDao = new StatisticDaoImpl();
		Statistic statistic = statisticDao.findByName(statisticName, session);
		t.rollback();
		return statistic;
	}
	
	@Override
	public void createBatchSQL (List<Statistic> statistics) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"name","description","is_binary","created_by","created_at"};
		int batchSize=100;
		
		String sql="INSERT INTO qsar_models.statistics (";
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
//		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < statistics.size(); counter++) {
				Statistic statistic=statistics.get(counter);
				
				prep.setString(1, statistic.getName());
				prep.setString(2,statistic.getDescription());
				prep.setBoolean(3, statistic.getIsBinary());
				prep.setString(4, statistic.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+statistics.size()+" statistics using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Statistic> getAll() {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return getAll(session);
	}

	@Override
	public List<Statistic> getAll(Session session) {
		Transaction t = session.beginTransaction();
		StatisticDao dao = new StatisticDaoImpl();
		List<Statistic> statistics = dao.getAll(session);
		t.rollback();
		return statistics;
	}

}

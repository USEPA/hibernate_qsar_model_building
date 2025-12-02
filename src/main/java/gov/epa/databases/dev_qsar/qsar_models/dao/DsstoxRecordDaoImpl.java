package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dsstox.entity.GenericSubstance;

/**
* @author TMARTI02
*/
public class DsstoxRecordDaoImpl {
	
	private static final String HQL_ALL = "from DsstoxRecord";//TODO needs to specify which snapshot
	
	private static final String HQL_BY_DTXSID ="FROM DsstoxRecord dr "
			+ "JOIN dr.dsstoxSnapshot ds"
			+ " WHERE dr.dtxsid = :dtxsid AND ds.id = :fk_snapshot_id";
      
	
	public List<DsstoxRecord> findAll(Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query<DsstoxRecord> query = session.createQuery(HQL_ALL,DsstoxRecord.class);
		return (List<DsstoxRecord>) query.list();
	}

	public DsstoxRecord findByDTXSID(String dtxsid, long fk_snapshot_id, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		
//		System.out.println(HQL_BY_DTXSID);
		Query<DsstoxRecord> query = session.createQuery(HQL_BY_DTXSID,DsstoxRecord.class);
		query.setParameter("dtxsid", dtxsid);
		query.setParameter("fk_snapshot_id", fk_snapshot_id);
		return query.uniqueResult();
	}

}

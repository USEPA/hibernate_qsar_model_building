package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;

/**
* @author TMARTI02
*/
public class DsstoxRecordDaoImpl {
	
	private static final String HQL_ALL = "from DsstoxRecord";//TODO needs to specify which snapshot
	
	public List<DsstoxRecord> findAll(Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<DsstoxRecord>) query.list();
	}

}

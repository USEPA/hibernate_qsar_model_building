package gov.epa.databases.dev_qsar.qsar_models.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxOtherCASRN;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;

/**
* @author TMARTI02
*/
public class DsstoxOtherCASRN_DaoImpl {
	
	private static final String HQL_ALL = "from DsstoxOtherCASRN";//TODO needs to specify which snapshot
	
	public List<DsstoxOtherCASRN> findAll(Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<DsstoxOtherCASRN>) query.list();
	}

}

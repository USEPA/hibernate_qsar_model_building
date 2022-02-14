package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;

public class LiteratureSourceDaoImpl implements LiteratureSourceDao {
	
	private static final String HQL_BY_NAME = "from LiteratureSource ls where ls.name = :sourceName";
	private static final String HQL_ALL = "from LiteratureSource";

	@Override
	public LiteratureSource findByName(String sourceName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("sourceName", sourceName);
		return (LiteratureSource) query.uniqueResult();
	}
	
	@Override
	public List<LiteratureSource> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<LiteratureSource>) query.list();
	}

}

package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;

public class PublicSourceDaoImpl implements PublicSourceDao {
	
	private static final String HQL_BY_NAME = "from PublicSource ps where ps.name = :sourceName";
	private static final String HQL_ALL = "from PublicSource";

	@Override
	public PublicSource findByName(String sourceName, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("sourceName", sourceName);
		return (PublicSource) query.uniqueResult();
	}
	
	@Override
	public List<PublicSource> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<PublicSource>) query.list();
	}

}

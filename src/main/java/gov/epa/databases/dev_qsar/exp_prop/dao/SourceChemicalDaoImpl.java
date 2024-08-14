package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;

public class SourceChemicalDaoImpl implements SourceChemicalDao {
	
	private static final String HQL_ALL = "from SourceChemical";
	
	private static final String HQL_BY_PUBLIC_SOURCE_NAME= "select sc from SourceChemical sc "
			+ "join sc.publicSource ps "
			+ "where ps.name = :publicSourceName";	
	@Override
	public SourceChemical findMatch(SourceChemical sc, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		
		String sourceCasrn = sc.getSourceCasrn();
		String sourceSmiles = sc.getSourceSmiles();
		String sourceChemicalName = sc.getSourceChemicalName();
		String sourceDtxsid = sc.getSourceDtxsid();
		String sourceDtxcid = sc.getSourceDtxcid();
		String sourceDtxrid = sc.getSourceDtxrid();
		
		PublicSource ps = sc.getPublicSource();
		LiteratureSource ls = sc.getLiteratureSource();
		
		Criteria crit = session.createCriteria(SourceChemical.class);
		crit.add(sourceCasrn == null ? Restrictions.isNull("sourceCasrn") : Restrictions.eq("sourceCasrn", sourceCasrn));
		crit.add(sourceSmiles == null ? Restrictions.isNull("sourceSmiles") : Restrictions.eq("sourceSmiles", sourceSmiles));
		crit.add(sourceChemicalName == null ? Restrictions.isNull("sourceChemicalName") : Restrictions.eq("sourceChemicalName", sourceChemicalName));
		crit.add(sourceDtxsid == null ? Restrictions.isNull("sourceDtxsid") : Restrictions.eq("sourceDtxsid", sourceDtxsid));
		crit.add(sourceDtxcid == null ? Restrictions.isNull("sourceDtxcid") : Restrictions.eq("sourceDtxcid", sourceDtxcid));
		crit.add(sourceDtxrid == null ? Restrictions.isNull("sourceDtxrid") : Restrictions.eq("sourceDtxrid", sourceDtxrid));
		crit.add(ps == null ? Restrictions.isNull("publicSource") : Restrictions.eq("publicSource.id", ps.getId()));
		crit.add(ls == null ? Restrictions.isNull("literatureSource") : Restrictions.eq("literatureSource.id", ls.getId()));
		
		return (SourceChemical) crit.uniqueResult();
	}

	@Override
	public List<SourceChemical> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_ALL);
		return (List<SourceChemical>) query.list();
	}

	@Override
	public List<SourceChemical> findAll(PublicSource ps, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		
		Query query = session.createQuery(HQL_BY_PUBLIC_SOURCE_NAME);
		query.setParameter("publicSourceName", ps.getName());
		return (List<SourceChemical>) query.list();
	}

}

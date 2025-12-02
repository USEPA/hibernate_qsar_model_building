package gov.epa.databases.dev_qsar.exp_prop.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

//import org.hibernate.Criteria;
//import org.hibernate.criterion.Restrictions;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


import gov.epa.databases.dev_qsar.exp_prop.ExpPropSession;
import gov.epa.databases.dev_qsar.exp_prop.entity.LiteratureSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;


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
		
//		Criteria crit = session.createCriteria(SourceChemical.class);
//		crit.add(sourceCasrn == null ? Restrictions.isNull("sourceCasrn") : Restrictions.eq("sourceCasrn", sourceCasrn));
//		crit.add(sourceSmiles == null ? Restrictions.isNull("sourceSmiles") : Restrictions.eq("sourceSmiles", sourceSmiles));
//		crit.add(sourceChemicalName == null ? Restrictions.isNull("sourceChemicalName") : Restrictions.eq("sourceChemicalName", sourceChemicalName));
//		crit.add(sourceDtxsid == null ? Restrictions.isNull("sourceDtxsid") : Restrictions.eq("sourceDtxsid", sourceDtxsid));
//		crit.add(sourceDtxcid == null ? Restrictions.isNull("sourceDtxcid") : Restrictions.eq("sourceDtxcid", sourceDtxcid));
//		crit.add(sourceDtxrid == null ? Restrictions.isNull("sourceDtxrid") : Restrictions.eq("sourceDtxrid", sourceDtxrid));
//		crit.add(ps == null ? Restrictions.isNull("publicSource") : Restrictions.eq("publicSource.id", ps.getId()));
//		crit.add(ls == null ? Restrictions.isNull("literatureSource") : Restrictions.eq("literatureSource.id", ls.getId()));
//		
//		return (SourceChemical) crit.uniqueResult();
		
		 // Obtain CriteriaBuilder instance
        CriteriaBuilder cb = session.getCriteriaBuilder();

        // Create CriteriaQuery instance
        CriteriaQuery<SourceChemical> cq = cb.createQuery(SourceChemical.class);

        // Define the root of the query
        Root<SourceChemical> root = cq.from(SourceChemical.class);

        // List to hold predicates
        List<Predicate> predicates = new ArrayList<>();

        // Add predicates based on conditions
		if (sourceCasrn == null)
			predicates.add(cb.isNull(root.get("sourceCasrn")));
		else
			predicates.add(cb.equal(root.get("sourceCasrn"), sourceCasrn));
		
		if (sourceSmiles == null)
			predicates.add(cb.isNull(root.get("sourceSmiles")));
		else
			predicates.add(cb.equal(root.get("sourceSmiles"), sourceSmiles));
		
		if (sourceChemicalName == null)
			predicates.add(cb.isNull(root.get("sourceChemicalName")));
		else
			predicates.add(cb.equal(root.get("sourceChemicalName"), sourceChemicalName));
		
		if (sourceDtxsid == null)
			predicates.add(cb.isNull(root.get("sourceDtxsid")));
		else
			predicates.add(cb.equal(root.get("sourceDtxsid"), sourceDtxsid));
		
		if (sourceDtxcid == null)
			predicates.add(cb.isNull(root.get("sourceDtxcid")));
		else
			predicates.add(cb.equal(root.get("sourceDtxcid"), sourceDtxcid));
		
		if (sourceDtxrid == null)
			predicates.add(cb.isNull(root.get("sourceDtxrid")));
		else
			predicates.add(cb.equal(root.get("sourceDtxrid"), sourceDtxrid));
		
		if (ps == null)
			predicates.add(cb.isNull(root.get("publicSource")));
		else
			predicates.add(cb.equal(root.get("publicSource").get("id"), ps.getId()));
		
		if (ls == null)
			predicates.add(cb.isNull(root.get("literatureSource")));
		else
			predicates.add(cb.equal(root.get("literatureSource").get("id"), ls.getId()));

        // Apply predicates to the query
        cq.where(predicates.toArray(new Predicate[0]));

        // Create and execute the query
        Query<SourceChemical> query = session.createQuery(cq);
        return query.getSingleResult();
		
		
	}

	@Override
	public List<SourceChemical> findAll(Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		
    	Query<SourceChemical> query = session.createQuery(HQL_ALL, SourceChemical.class);
        return query.getResultList();

	}

	@Override
	public List<SourceChemical> findAll(PublicSource ps, Session session) {
		if (session==null) { session = ExpPropSession.getSessionFactory().getCurrentSession(); }
		
		Query <SourceChemical>query = session.createQuery(HQL_BY_PUBLIC_SOURCE_NAME,SourceChemical.class);
		query.setParameter("publicSourceName", ps.getName());
		return query.getResultList();
	}

}

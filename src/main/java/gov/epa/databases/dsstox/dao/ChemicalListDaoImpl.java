package gov.epa.databases.dsstox.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dsstox.DsstoxSession;
import gov.epa.databases.dsstox.entity.ChemicalList;

public class ChemicalListDaoImpl implements ChemicalListDao {
	private static final String HQL_BY_NAME = "from ChemicalList cl where cl.name = :chemicalListName";

	@Override
	public ChemicalList findByName(String chemicalListName, Session session) {
		if (session==null) { session = DsstoxSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_NAME);
		query.setParameter("chemicalListName", chemicalListName);
		return (ChemicalList) query.uniqueResult();
	}
	
}

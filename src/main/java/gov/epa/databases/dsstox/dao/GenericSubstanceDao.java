package gov.epa.databases.dsstox.dao;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.GenericSubstance;

public interface GenericSubstanceDao {
	
	public GenericSubstance findById(Long id, Session session);
	
	public GenericSubstance findByDtxsid(String dtxsid, Session session);

	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session);
	
	public GenericSubstance findByCasrn(String casrn, Session session);

	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session);
	
}

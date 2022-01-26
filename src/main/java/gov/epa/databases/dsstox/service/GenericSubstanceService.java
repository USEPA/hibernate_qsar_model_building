package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.GenericSubstance;

public interface GenericSubstanceService {
	
	public GenericSubstance findByDtxsid(String dtxsid);

	public GenericSubstance findByDtxsid(String dtxsid, Session session);

	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids);

	public List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session);
	
	public GenericSubstance findByCasrn(String casrn);

	public GenericSubstance findByCasrn(String casrn, Session session);

	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns);

	public List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session);

}

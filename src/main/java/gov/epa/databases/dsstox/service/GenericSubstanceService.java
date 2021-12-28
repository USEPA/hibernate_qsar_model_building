package gov.epa.databases.dsstox.service;

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import gov.epa.databases.dsstox.entity.GenericSubstance;

public interface GenericSubstanceService {
	
	GenericSubstance findByDtxsid(String dtxsid);

	GenericSubstance findByDtxsid(String dtxsid, Session session);

	List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids);

	List<GenericSubstance> findByDtxsidIn(Collection<String> dtxsids, Session session);
	
	GenericSubstance findByCasrn(String casrn);

	GenericSubstance findByCasrn(String casrn, Session session);

	List<GenericSubstance> findByCasrnIn(Collection<String> casrns);

	List<GenericSubstance> findByCasrnIn(Collection<String> casrns, Session session);

}

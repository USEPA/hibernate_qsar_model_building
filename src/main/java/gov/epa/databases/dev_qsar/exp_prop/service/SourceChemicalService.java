package gov.epa.databases.dev_qsar.exp_prop.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PublicSource;
import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;

public interface SourceChemicalService {
	
	public SourceChemical findMatch(SourceChemical sc);
	
	public SourceChemical findMatch(SourceChemical sc, Session session);
	
	public SourceChemical create(SourceChemical sourceChemical) throws ConstraintViolationException;
	
	public SourceChemical create(SourceChemical sourceChemical, Session session) throws ConstraintViolationException;

	public List<SourceChemical> findAll();

	public List<SourceChemical> findAll(Session session);

	public List<SourceChemical> findAllFromSource(PublicSource ps);

	public List<SourceChemical> findAllFromSource(PublicSource ps, Session session);

}

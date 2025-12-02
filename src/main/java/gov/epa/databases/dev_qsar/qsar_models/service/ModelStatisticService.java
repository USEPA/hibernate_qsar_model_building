package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.exp_prop.entity.PropertyValue;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

public interface ModelStatisticService {
	
	public ModelStatistic findByModelId(Long modelId,Long statisticId);
	
	public ModelStatistic findByModelId(Long modelId, Long statisticId, Session session);

	public List<ModelStatistic> findByModelId(Long modelId);
	
	public List<ModelStatistic> findByModelId(Long modelId, Session session);

	
	public ModelStatistic create(ModelStatistic modelStatistic) throws ConstraintViolationException;
	
	public ModelStatistic create(ModelStatistic modelStatistic, Session session) throws ConstraintViolationException;

	public ModelStatistic update(ModelStatistic modelStatistic) throws ConstraintViolationException;

	public ModelStatistic update(ModelStatistic modelStatistic, Session session) throws ConstraintViolationException;

	public List<ModelStatistic> getAll();

	public List<ModelStatistic> getAll(Session session);

}

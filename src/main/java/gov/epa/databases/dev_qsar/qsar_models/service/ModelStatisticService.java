package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.List;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

public interface ModelStatisticService {
	
	public List<ModelStatistic> findByModelId(Long modelId,Long statisticId);
	
	public List<ModelStatistic> findByModelId(Long modelId, Long statisticId, Session session);
	
	public ModelStatistic create(ModelStatistic modelStatistic) throws ConstraintViolationException;
	
	public ModelStatistic create(ModelStatistic modelStatistic, Session session) throws ConstraintViolationException;

}

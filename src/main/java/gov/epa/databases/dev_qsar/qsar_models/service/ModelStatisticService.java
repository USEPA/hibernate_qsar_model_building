package gov.epa.databases.dev_qsar.qsar_models.service;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;

public interface ModelStatisticService {
	
	public Set<ConstraintViolation<ModelStatistic>> create(ModelStatistic modelStatistic);
	
	public Set<ConstraintViolation<ModelStatistic>> create(ModelStatistic modelStatistic, Session session);

}

package gov.epa.databases.dev_qsar.qsar_models.service;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;

public interface ModelBytesService {
	
	public ModelBytes findByModelId(Long modelId,boolean decompress);
	
	public ModelBytes findByModelId(Long modelId, boolean decompress,Session session);
	
	public ModelBytes create(ModelBytes modelBytes) throws ConstraintViolationException;
	
	public ModelBytes create(ModelBytes modelBytes, Session session) throws ConstraintViolationException;
	
//	public void delete(ModelBytes modelBytes);
//	
//	public void delete(ModelBytes modelBytes, Session session);

	public void deleteByModelId(Long id);

	public void deleteByModelId(Long id, Session session);

	public ModelBytes createSQL(ModelBytes modelBytes,boolean compress);

	
	public byte [] getBytesSql(Long modelId,boolean decompress);
}

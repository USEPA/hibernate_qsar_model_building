package gov.epa.databases.dev_qsar.qsar_models.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelBytesDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.ModelBytesDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelBytes;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public class ModelBytesServiceImpl implements ModelBytesService {

	public static int chunkSize = 26214400;
	
	private Validator validator;
	
	public ModelBytesServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}
	
	public ModelBytes findByModelId(Long modelId) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return findByModelId(modelId, session);
	}
	
	public ModelBytes findByModelId(Long modelId, Session session) {
		Transaction t = session.beginTransaction();
		ModelBytesDao modelBytesDao = new ModelBytesDaoImpl();
		List<ModelBytes> modelBytesList = modelBytesDao.findByModelId(modelId, session);
		ModelBytes modelBytes = new ModelBytes();
		modelBytes.setCreatedAt(modelBytesList.get(0).getCreatedAt());
		modelBytes.setCreatedBy(modelBytesList.get(0).getCreatedBy());
		modelBytes.setUpdatedAt(modelBytesList.get(0).getUpdatedAt());
		modelBytes.setUpdatedBy(modelBytesList.get(0).getUpdatedBy());
		modelBytes.setModel(modelBytesList.get(0).getModel());
		modelBytes.setId(modelBytesList.get(0).getId());
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		for (int i = 0; i < modelBytesList.size(); i++) {
			try {
				outputStream.write( modelBytesList.get(i).getBytes() );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		modelBytes.setBytes(outputStream.toByteArray());
		t.rollback();
		return modelBytes;
	}

	@Override
	public ModelBytes create(ModelBytes modelBytes) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(modelBytes, session);
	}

	@Override
	public ModelBytes createSQL (ModelBytes modelBytes) {

		Connection conn=DatabaseLookup.getConnectionPostgres();
		
		List<byte[]> partitions = divideArray(modelBytes.getBytes(), chunkSize);
		
		
		String [] fieldNames= {"bytes","fk_model_id","created_by","created_at"};
		int batchSize=1;
		
		String sql="INSERT INTO qsar_models.model_bytes (";
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
//		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < partitions.size(); counter++) {
				byte[] bytes=partitions.get(counter);
				prep.setBytes(1, bytes);
				prep.setLong(2, modelBytes.getModel().getId());
				prep.setString(3, modelBytes.getCreatedBy());
				prep.addBatch();
				
//				if (counter % batchSize == 0 && counter!=0) {
//					 System.out.println(counter);
//					prep.executeBatch();
//					conn.commit();//see if this frees up memory
//				}

				prep.executeBatch();
				conn.commit();//just commit each one to free up memory

			}

			int[] count = prep.executeBatch();// do what's left
			
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+modelBytes.getBytes().length+" bytes using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
//			conn.commit();
//			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return modelBytes;
	}		
	
	
	
	
	@Override
	public ModelBytes create(ModelBytes modelBytes, Session session) throws ConstraintViolationException {
		byte[] bytes = modelBytes.getBytes();


		List<byte[]> partitions = divideArray(bytes, chunkSize);


		Transaction t = session.beginTransaction();
		for (int i = 0; i < partitions.size(); i++) {
			ModelBytes modelBytesPartitioned = new ModelBytes(modelBytes.getModel(), partitions.get(i), modelBytes.getCreatedBy());

			Set<ConstraintViolation<ModelBytes>> violations = validator.validate(modelBytesPartitioned);
			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}

			try {
				session.save(modelBytesPartitioned);
				session.flush();
				session.clear();
				//				session.refresh(modelBytesPartitioned);
			} catch (org.hibernate.exception.ConstraintViolationException e) {
				t.rollback();
				throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
			}
		}
		t.commit();
		return modelBytes;
	}

//	@Override
//	public void delete(ModelBytes modelBytes) {
//		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
//		delete(modelBytes, session);
//	}
//
//	@Override
//	public void delete(ModelBytes modelBytes, Session session) {
//		if (modelBytes.getId()==null) {
//			return;
//		}
//		
//		Transaction t = session.beginTransaction();
//		session.delete(modelBytes);
//		session.flush();
//		t.commit();
//	}
	
	public static List<byte[]> divideArray(byte[] source, int chunksize) {
		
		System.out.println("Size of model bytes="+source.length);
		
		List <byte[]> ret=new ArrayList<>();
		
		if (source.length<chunksize) {
			ret.add(new byte[source.length]);
		} else {
			
			int numPieces=(int) Math.ceil(source.length / (double) chunksize);
			
			for (int i=0;i<numPieces-1;i++) {
				ret.add(new byte[chunksize]);	
			}
			
			int remainingBytesLength=source.length-(numPieces-1)*chunksize;
			
			ret.add(new byte[remainingBytesLength]);
				
		}
		
		int totalCount=0;
		
		for (byte[] bytes:ret) {
			totalCount+=bytes.length;
		}

		if(totalCount!=source.length) {
			System.out.println("byte length mismatch:"+totalCount+"\t"+source.length);
			return null;
		}
		
		
        int start = 0;
        int parts = 0;

        for (int i = 0; i < ret.size(); i++) {
            if (start + chunksize > source.length) {
                System.arraycopy(source, start, ret.get(i), 0, source.length - start);
            } else {
                System.arraycopy(source, start, ret.get(i), 0, chunksize);
            }
            start += chunksize;
            parts++;
        }

        System.out.println("# Parts = " + parts + "");

        return ret;
    }

	@Override
	public void deleteByModelId(Long id) {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		deleteByModelId(id,session);
	}
	
	@Override
	public void deleteByModelId(Long modelId,Session session) {
//		System.out.println("enter delete");
		
		Transaction t = session.beginTransaction();
		
		ModelBytesDao modelBytesDao = new ModelBytesDaoImpl();
		List<ModelBytes> modelBytesList = modelBytesDao.findByModelId(modelId, session);
		
		
		for(ModelBytes modelBytes:modelBytesList) {
			
//			System.out.println(modelBytes.getId());
			
//			if(true) continue;
			
			try {
				session.delete(modelBytes);
				session.flush();
				session.clear();
				//				session.refresh(modelBytesPartitioned);
			} catch (org.hibernate.exception.ConstraintViolationException e) {
				t.rollback();
				throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
			}
		}
		t.commit();
	}

}

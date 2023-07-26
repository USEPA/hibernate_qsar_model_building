package gov.epa.databases.dev_qsar.qsar_models.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_descriptors.QsarDescriptorsSession;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class PredictionDashboardServiceImpl implements PredictionDashboardService {
	Validator validator;

	public PredictionDashboardServiceImpl() {
		this.validator = DevQsarValidator.getValidator();
	}

	
	
	
	@Override
	public PredictionDashboard create(PredictionDashboard predictionDashboard) throws ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return create(predictionDashboard, session);
	}


	@Override
	public PredictionDashboard create(PredictionDashboard predictionDashboard, Session session) throws ConstraintViolationException {
		Set<ConstraintViolation<PredictionDashboard>> violations = validator.validate(predictionDashboard);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
		
		Transaction t = session.beginTransaction();
		
		try {
			session.save(predictionDashboard);
			session.flush();
			session.refresh(predictionDashboard);
			t.commit();
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			t.rollback();
			throw new ConstraintViolationException(e.getMessage() + ": " + e.getSQLException().getMessage(), null);
		}
		
		return predictionDashboard;
	}


	@Override
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboard)
			throws org.hibernate.exception.ConstraintViolationException {
		Session session = QsarModelsSession.getSessionFactory().getCurrentSession();
		return createBatch(predictionDashboard, session);
	}


	@Override
	public List<PredictionDashboard> createBatch(List<PredictionDashboard> predictionDashboards, Session session)
			throws org.hibernate.exception.ConstraintViolationException {
		Transaction tx = session.beginTransaction();
		try {
		for (int i = 0; i < predictionDashboards.size(); i++) {
			PredictionDashboard predictionDashboard = predictionDashboards.get(i);
			session.save(predictionDashboard);
		    if ( i % 20 == 0 ) { //20, same as the JDBC batch size
		        //flush a batch of inserts and release memory:
		        session.flush();
		        session.clear();
		    }
		}
		} catch (org.hibernate.exception.ConstraintViolationException e) {
			tx.rollback();
		}
		
		tx.commit();
		session.close();
		return predictionDashboards;
	}
	
	
	
	
	@Override
	public void createSQL (List<PredictionDashboard> predictionDashboards) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"canon_qsar_smiles", "fk_dsstox_records_id",	"fk_model_id", 
				"prediction_value", "prediction_string", "prediction_error",
				  "created_by", "created_at"};

		int batchSize=1000;
		
		String sql="INSERT INTO qsar_models.predictions_dashboard (";
		
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
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < predictionDashboards.size(); counter++) {
				PredictionDashboard p=predictionDashboards.get(counter);
				prep.setString(1, p.getCanonQsarSmiles());
				prep.setLong(2, p.getFk_dsstox_records_id());
				prep.setLong(3, p.getModel().getId());
				
				if (p.getPredictionValue()==null) {
					prep.setNull(4,Types.DOUBLE);
				} else {
					prep.setDouble(4, p.getPredictionValue());	
				}
				
				prep.setString(5, p.getPredictionString());
				prep.setString(6, p.getPredictionError());
				prep.setString(7, p.getCreatedBy());
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					// System.out.println(counter);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+predictionDashboards.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}			

	
	/**
	 * Use cross schema query so can get precise property name from dataset
	 * 
	 * @param dtxcid
	 * @param propertyName
	 * @param modelSource
	 * @return
	 */
	public String getPredictionDashboardAsJson(String dtxsid, String propertyName, String modelSource) {
		
		String sql="select p.\"name\" as property, m.\"source\",m.\"name\" as model_name,"
				+ "pd.canon_qsar_smiles, pd.smiles, pd.dtxsid, pd.dtxcid,"
				+ "pd.prediction_value as prediction_value,"
				+ "u.abbreviation as prediction_units, "
				+ "pd.prediction_string as prediction_string, "
				+ "pd.prediction_error as prediction_error"
				+ " from qsar_models.predictions_dashboard pd\n"+
		"join qsar_models.models m on m.id=pd.fk_model_id\n"+
		"join qsar_datasets.datasets d on d.\"name\" =m.dataset_name\n"+
		"join qsar_datasets.properties p on p.id=d.fk_property_id\n"+
		"join qsar_datasets.units u on u.id=d.fk_unit_id_contributor\n"+
		"where m.\"source\" ='"+modelSource+"' and dtxsid='"+dtxsid+"' and p.\"name\"='"+propertyName+"'";
		
//		System.out.println(sql);
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
		
		try {
			
			JsonObject jo=new JsonObject();
			
			ResultSetMetaData metadata=rs.getMetaData();	
			
//			System.out.println(metadata.getColumnCount());			
			
			while (rs.next()) {
				
				for (int i=1;i<=metadata.getColumnCount();i++) {
					jo.addProperty(metadata.getColumnLabel(i),rs.getString(i));
					
//					System.out.println(i+"\t"+metadata.getColumnLabel(i)+"\t"+rs.getString(i));
				}
				return Utilities.gson.toJson(jo);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "error getting report";
		
	}
	
	public static void main(String[] args) {
		PredictionDashboardServiceImpl p=new PredictionDashboardServiceImpl();
		String dtxsid="DTXSID80177704";
		String propertyName="Water solubility";
		String modelSource="TEST5.1.3";
		String json=p.getPredictionDashboardAsJson(dtxsid, "Water solubility", modelSource);
		System.out.println(json);
	}
	

}

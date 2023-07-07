package gov.epa.databases.dev_qsar.qsar_datasets.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DataPointDaoImpl implements DataPointDao {
	
	private static final String HQL_BY_DATASET_NAME = 
			"select distinct dp from DataPoint dp "
			+ "join dp.dataset d "
			+ "left join fetch dp.dataPointContributors dpc "
			+ "where d.name = :datasetName";
	
	private static final String HQL_BY_DATASET_ID = 
			"select distinct dp from DataPoint dp "
			+ "join dp.dataset d "
			+ "left join fetch dp.dataPointContributors dpc "
			+ "where d.id = :datasetId";
	
	//TODO- this method runs really slow in postgres_testing database
	// Had to add 	@NotFound(action=NotFoundAction.IGNORE)//https://edwin.baculsoft.com/2013/02/a-weird-hibernate-exception-org-hibernate-objectnotfoundexception-no-row-with-the-given-identifier-exists/ to dataset variable in DataPoint class to get it running at all 

	public List<DataPoint> findByDatasetName(String datasetName, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		
		Query query = session.createQuery(HQL_BY_DATASET_NAME);
		query.setParameter("datasetName", datasetName);
		
		return (List<DataPoint>) query.list();
	}
	
	
	/**
	 * This method was needed because the findByDatasetName method failed for the postgres_testing database after a bunch of models/datasets were deleted
	 * 
	 * @param datasetName
	 * @return
	 */
	public List<DataPoint> findByDatasetNameSql(String datasetName) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String sql="select id from qsar_datasets.datasets d where d.\"name\" ='"+datasetName+"'";

		String id=SqlUtilities.runSQL(conn, sql);
		
		sql="select * from qsar_datasets.data_points dp \r\n"
				+ "where dp.fk_dataset_id="+id;
				
		ResultSet rs=SqlUtilities.runSQL2(conn, sql);
		List<DataPoint>dps=new ArrayList<>();
		
		try {
			
			int counter=0;
			while (rs.next()) {
				counter++;
				
				DataPoint r=new DataPoint();
				dps.add(r);
				
				for (int i=1;i<=rs.getMetaData().getColumnCount();i++) {
					String val=rs.getString(i);
					String columnName=rs.getMetaData().getColumnName(i);
					
					if (columnName.equals("id")) {
						r.setId(Long.parseLong(val));
					} else if (columnName.equals("created_by")) {
						r.setCreatedBy(val);
					} else if (columnName.equals("updated_by")) {
						r.setUpdatedBy(val);
					} else if (columnName.equals("outlier")) {
						r.setOutlier(Boolean.parseBoolean(val));
					} else if (columnName.equals("canon_qsar_smiles")) {
						r.setCanonQsarSmiles(val);
					} else if (columnName.equals("qsar_dtxcid")) {
						r.setQsar_dtxcid(val);
					} else if (columnName.equals("qsar_exp_prop_property_values_id")) {
						r.setQsar_exp_prop_property_values_id(val);
					} else if (columnName.equals("qsar_property_value")) {
						r.setQsarPropertyValue(Double.parseDouble(val));
					} else if (columnName.equals("created_at")) {
						r.setCreatedAt(rs.getTimestamp(i));
					} else if (columnName.equals("updated_at")) {
						r.setUpdatedAt(rs.getTimestamp(i));
					} else if (columnName.equals("fk_dataset_id")) {
						//TODO create dataset object in datapoint
					} else {
						System.out.println(columnName);
					}
					
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println(Utilities.gson.toJson(dps));
		
		
		return dps;
		
	}
	
	
	
	public List<DataPoint> findByDatasetId(Long datasetId, Session session) {
		if (session==null) { session = QsarDatasetsSession.getSessionFactory().getCurrentSession(); }
		Query query = session.createQuery(HQL_BY_DATASET_ID);
		query.setParameter("datasetId", datasetId);
		return (List<DataPoint>) query.list();
	}

}

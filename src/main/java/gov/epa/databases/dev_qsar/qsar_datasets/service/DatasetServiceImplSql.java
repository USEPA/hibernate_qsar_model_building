package gov.epa.databases.dev_qsar.qsar_datasets.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.Session;
import org.hibernate.Transaction;

import gov.epa.databases.dev_qsar.DevQsarValidator;
import gov.epa.databases.dev_qsar.qsar_datasets.QsarDatasetsSession;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDao;
import gov.epa.databases.dev_qsar.qsar_datasets.dao.DatasetDaoImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Property;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Unit;
import gov.epa.databases.dev_qsar.qsar_models.QsarModelsSession;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodADDao;
import gov.epa.databases.dev_qsar.qsar_models.dao.MethodADDaoImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.MethodAD;
import gov.epa.run_from_java.scripts.SqlUtilities;

public class DatasetServiceImplSql implements DatasetService {

	public Dataset findByName(String datasetName) {
		System.out.println("TODO implement findByName(String datasetName)");
		return null;
	}
	
	public Dataset findByName(String datasetName, Session session) {
		System.out.println("findByName(String datasetName, Session session) is not valid for sql based impl");
		return null;
	}
	
	
	@Override
	public Dataset findById(Long datasetId) {
		Session session = QsarDatasetsSession.getSessionFactory().getCurrentSession();
		return findById(datasetId, session);
	}

	@Override
	public Dataset findById(Long datasetId, Session session) {
		System.out.println("findById(Long datasetId, Session session) is not valid for sql based impl");
		return null;
	}	
	
	
	@Override
	public List<Dataset> findAll() {
		
		String sql = "select id, name, description, "
				+ "dsstox_mapping_strategy,dsstox_chemical_list_name,"
				+ "fk_property_id, fk_unit_id, fk_unit_id_contributor"
				+ "\n"
				+ " from qsar_datasets.datasets d\n";
		
//		sql+="join qsar_datasets.properties p on p.id=d.fk_property_id";//if want to get property details
		
//		System.out.println(sql);
		try {

			ResultSet rs = new SqlUtilities().runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			List<Dataset> datasets = new ArrayList<>();
			while (rs.next()) {
				Dataset dataset = getDatasetFromRow(rs);
				datasets.add(dataset);				
			}
			return datasets;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		
	}
	
	private Dataset getDatasetFromRow(ResultSet rs) {

		Dataset dataset=new Dataset();
		
		int col=1;
		
		try {
			dataset.setId(rs.getLong(col++));
			dataset.setName(rs.getString(col++));
			dataset.setDescription(rs.getString(col++));
			dataset.setDsstoxMappingStrategy(rs.getString(col++));
			dataset.setDsstoxChemicalListName(rs.getString(col++));
			
			Property property=new Property();
			property.setId(rs.getLong(col++));			
			dataset.setProperty(property);
			
			Unit unit=new Unit();
			unit.setId(rs.getLong(col++));
			dataset.setUnit(unit);
			
			Unit unitContributor=new Unit();
			unitContributor.setId(rs.getLong(col++));
			dataset.setUnitContributor(unitContributor);
			
			return dataset;
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return dataset;
		
	}

	@Override
	public List<Dataset> findAll(Session session) {
		System.out.println("findAll(Session session) is not valid for sql based impl");
		return null;
	}
	
	
	
	@Override
	public Dataset create(Dataset dataset) throws ConstraintViolationException {
		System.out.println("TODO implement create(Dataset dataset)");
		return null;
	}

	@Override
	public Dataset create(Dataset dataset, Session session) throws ConstraintViolationException {
		System.out.println("create(Dataset dataset, Session session) is not valid for sql based impl");
		return null;
	}
	
	
	/**
	 * Since everything cascade on deleting the dataset fk just need one sql command
	 * @param id
	 */
	@Override
	public void delete(long id) {
		System.out.println("\nDeleting dataset id="+id);
		Connection conn=SqlUtilities.getConnectionPostgres();
		String sqlD="delete from qsar_datasets.datasets d\n"+
		"where d.id="+id+";";
		SqlUtilities.runSQLUpdate(conn, sqlD);
	}
	
	public void deleteByName(String name) {
		System.out.println("\nDeleting dataset name="+name);
		Connection conn=SqlUtilities.getConnectionPostgres();
		String sqlD="delete from qsar_datasets.datasets d\n"+"where d.name='"+name+"';";
		SqlUtilities.runSQLUpdate(conn, sqlD);
	}

}

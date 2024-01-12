package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.OPERA.OPERA_lookups;

/**
* @author TMARTI02
*/
@Entity
@Table(name="dsstox_other_casrns", uniqueConstraints={@UniqueConstraint(columnNames = {"casrn","fk_dsstox_record_id"})})

public class DsstoxOtherCASRN {

//	@Column(name="dtxsid")
//	private String dtxsid;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	
	@Column(name="casrn")
	private String casrn;
	
	@Column(name="cas_type")
	private String casType;
	
	@Column(name="source")
	private String source;
	
//	@NotNull(message="dsstoxRecord required")
//	@JoinColumn(name="fk_dsstox_record_id")
//	@ManyToOne
//	private DsstoxRecord dsstoxRecord;
	
	
	@NotNull(message="dsstoxRecord id required")
	@Column(name="fk_dsstox_record_id")
	private long fk_dsstox_record_id;


	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotBlank(message="Creator required")
	@Column(name="created_by")
	private String createdBy;
	
	static void getRecordsFromSnapshot() {
		String user="tmarti02";
		TreeMap<String, DsstoxOtherCASRN>otherCasrnsMap=new TreeMap<>();
		List<DsstoxOtherCASRN>otherCasrns=new ArrayList<>();
		
		String sql="select gs.dsstox_substance_id as dtxsid, oc.casrn, oc.cas_type,oc.source from other_casrns oc\r\n"
				+ "join generic_substances gs on oc.fk_generic_substance_id = gs.id";
		
		try {
			
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
			
			OPERA_lookups.getDsstoxRecordsFromJsonExport();
			
			while (rs.next()) {
				DsstoxOtherCASRN oc=new DsstoxOtherCASRN();
				
				String dtxsid=rs.getString(1);
				oc.casrn=rs.getString(2);
				oc.casType=rs.getString(3);
				oc.source=rs.getString(4);
				oc.updatedBy=user;
				oc.createdBy=user;
				oc.fk_dsstox_record_id=OPERA_lookups.mapDsstoxRecordsBySID.get(dtxsid).getId();
				
				if(otherCasrnsMap.get(oc.casrn)!=null) {
					DsstoxOtherCASRN ocOld=otherCasrnsMap.get(oc.casrn);
					System.out.println("Already have "+oc.casrn);
				} else {
					otherCasrnsMap.put(oc.casrn, oc);
					otherCasrns.add(oc);
//					System.out.println(oc.casrn+"\t"+oc.dtxsid);
				}
				
//				System.out.println(oc.dtxsid+"\t"+oc.casrn+"\t"+oc.dsstoxRecord.getId());
			}
			
			createSql(otherCasrns);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
	
	static void createSql(List<DsstoxOtherCASRN> valuesArray) {

		Connection conn=SqlUtilities.getConnectionPostgres();
		
		String [] fieldNames= {"fk_dsstox_record_id","casrn", "cas_type","source",
				 "created_by", "updated_by", "created_at","updated_at"};

		
		int batchSize=100;
		
		String sql="INSERT INTO qsar_models.dsstox_other_casrns (";
		
		for (int i=0;i<fieldNames.length;i++) {
			sql+=fieldNames[i];
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-2;i++) {
			sql+="?";
			if (i<fieldNames.length-2)sql+=",";			 		
		}
		sql+="current_timestamp,current_timestamp)";	
		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < valuesArray.size(); counter++) {
				DsstoxOtherCASRN oc=valuesArray.get(counter);
				prep.setLong(1, oc.fk_dsstox_record_id);
				prep.setString(2, oc.casrn);
				prep.setString(3, oc.casType);
				prep.setString(4, oc.source);
				prep.setString(5, oc.createdBy);
				prep.setString(6, oc.createdBy);
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
//					System.out.println(counter);
					prep.executeBatch();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+valuesArray.size()+" predictions using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}		
	
	public static void main(String[] args) {
		getRecordsFromSnapshot();
	}


	public String getCasrn() {
		return casrn;
	}


	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}


	public String getCasType() {
		return casType;
	}


	public void setCasType(String casType) {
		this.casType = casType;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	public Date getUpdatedAt() {
		return updatedAt;
	}


	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}


	public String getUpdatedBy() {
		return updatedBy;
	}


	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}


	public Date getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	public String getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public long getFk_dsstox_record_id() {
		return fk_dsstox_record_id;
	}


	public void setFk_dsstox_record_id(long fk_dsstox_record_id) {
		this.fk_dsstox_record_id = fk_dsstox_record_id;
	}
}

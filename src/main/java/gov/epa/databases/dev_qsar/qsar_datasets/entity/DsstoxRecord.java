package gov.epa.databases.dev_qsar.qsar_datasets.entity;

import java.util.ArrayList;

/**
* @author TMARTI02
*/


import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.databases.dsstox.entity.DsstoxCompound;

@Entity
@Table(name="dsstox_records", uniqueConstraints={@UniqueConstraint(columnNames = {"dtxcid", "fk_dsstox_snapshot_id"})})
public class DsstoxRecord {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="fk_compounds_id")
	private Long fk_compounds_id;
	
	@NotBlank(message="smiles required")
	@Column(name="smiles")
	private String smiles;
		
	@Column(name="dtxcid")
	private String dtxcid;

	@Column(name="dtxsid")
	private String dtxsid;
	
	
	@ManyToOne
	@NotNull(message="DsstoxSnapshot required")
	@JoinColumn(name="fk_dsstox_snapshot_id")
	private DsstoxSnapshot dsstoxSnapshot;
	
	
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
	
	public DsstoxRecord() {}
	
	
	public static List<DsstoxRecord> getRecords(List<DsstoxCompound> compounds, DsstoxSnapshot snapshot,String lanId) {

		List<DsstoxRecord>records=new ArrayList<>();
		
		for (DsstoxCompound c:compounds) {
			String dtxsid=null;
			if(c.getGenericSubstanceCompound()!=null) {
				dtxsid=c.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
			}
			DsstoxRecord record=new DsstoxRecord(snapshot, c.getId(),  dtxsid, c.getDsstoxCompoundId(),c.getSmiles(),lanId);
			records.add(record);
		}
		return records;
	}
	
	public DsstoxRecord(DsstoxSnapshot dsstoxSnapshot,Long fk_compounds_id, String DTXSID,String DTXCID, String smiles, String createdBy) {
		this.fk_compounds_id=fk_compounds_id;
		this.dsstoxSnapshot=dsstoxSnapshot;
		this.dtxcid=DTXCID;
		this.dtxsid=DTXSID;
		this.smiles=smiles;
		this.createdBy=createdBy;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getSmiles() {
		return smiles;
	}


	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}


	public String getDtxcid() {
		return dtxcid;
	}


	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}


	public String getDtxsid() {
		return dtxsid;
	}


	public void setDtxsid(String dtxsid) {
		this.dtxsid = dtxsid;
	}


	public DsstoxSnapshot getDsstoxSnapshot() {
		return dsstoxSnapshot;
	}


	public void setDsstoxSnapshot(DsstoxSnapshot dsstoxSnapshot) {
		this.dsstoxSnapshot = dsstoxSnapshot;
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


	public Long getFk_compounds_id() {
		return fk_compounds_id;
	}


	public void setFk_compounds_id(Long fk_compounds_id) {
		this.fk_compounds_id = fk_compounds_id;
	}


}

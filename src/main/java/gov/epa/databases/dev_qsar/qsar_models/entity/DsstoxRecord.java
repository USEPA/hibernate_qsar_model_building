package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.ArrayList;

/**
* @author TMARTI02
*/


import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import gov.epa.databases.dsstox.entity.DsstoxCompound;

@Entity
@Table(name="dsstox_records", uniqueConstraints={@UniqueConstraint(columnNames = {"dtxcid","dtxsid","fk_dsstox_snapshot_id"})})
public class DsstoxRecord {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="cid")
	private Long cid;
	
	@NotBlank(message="smiles required")
	@Column(name="smiles")
	private String smiles;
		
	@Column(name="dtxcid")
	private String dtxcid;

	@Column(name="dtxsid")
	private String dtxsid;

	@Column(name="casrn")
	private String casrn;
	
	@Column(name="preferred_name")
	private String preferredName;
	
	//These should all be true for the records with both sid and cid
	@Column(name="mol_image_png_available")
	private Boolean molImagePNGAvailable;
	
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

	
	@Column(name="mol_weight")
	private Double molWeight;

	//TODO cascade not working	
//	@OneToMany(mappedBy="dsstoxRecord", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@Transient
	private List<DsstoxOtherCASRN> otherCasrns=new ArrayList<>();

	public DsstoxRecord() {}
	
	
	public static List<DsstoxRecord> getRecords(List<DsstoxCompound> compounds, DsstoxSnapshot snapshot,String lanId) {

		List<DsstoxRecord>records=new ArrayList<>();
		
		for (DsstoxCompound c:compounds) {
			String dtxsid=null;
			String casrn=null;
			String preferredName=null;
			
			if(c.getGenericSubstanceCompound()!=null) {
				dtxsid=c.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
				casrn=c.getGenericSubstanceCompound().getGenericSubstance().getCasrn();
				preferredName=c.getGenericSubstanceCompound().getGenericSubstance().getPreferredName();
			}
			
			DsstoxRecord record=new DsstoxRecord(snapshot, c.getMolWeight(), c.getId(),  dtxsid, c.getDsstoxCompoundId(),c.getSmiles(),c.isMolImagePNGAvailable(),casrn,preferredName,lanId);
			
			if (!c.isMolImagePNGAvailable())
				System.out.println(dtxsid+"\t"+c.isMolImagePNGAvailable());
			
			
			records.add(record);
		}
		return records;
	}
	
	public DsstoxRecord(DsstoxSnapshot dsstoxSnapshot,Double mol_weight, Long fk_compounds_id, String DTXSID,String DTXCID, String smiles,boolean isMolImageAvailable, String casrn, String preferredName, String createdBy) {
		this.molWeight=mol_weight;
		this.cid=fk_compounds_id;
		this.dsstoxSnapshot=dsstoxSnapshot;
		this.dtxcid=DTXCID;
		this.dtxsid=DTXSID;
		this.smiles=smiles;
		this.createdBy=createdBy;
		this.casrn=casrn;
		this.preferredName=preferredName;
		this.setMolImagePNGAvailable(isMolImageAvailable);
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




	public Double getMolWeight() {
		return molWeight;
	}


	public void setMolWeight(Double molWeight) {
		this.molWeight = molWeight;
	}


	public String getPreferredName() {
		return preferredName;
	}


	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}


	public String getCasrn() {
		return casrn;
	}


	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}


	public Boolean isMolImagePNGAvailable() {
		return molImagePNGAvailable;
	}


	public void setMolImagePNGAvailable(boolean molImagePNGAvailable) {
		this.molImagePNGAvailable = molImagePNGAvailable;
	}


	public Long getCid() {
		return cid;
	}


	public void setCid(Long cid) {
		this.cid = cid;
	}


	public Boolean getMolImagePNGAvailable() {
		return molImagePNGAvailable;
	}


	public void setMolImagePNGAvailable(Boolean molImagePNGAvailable) {
		this.molImagePNGAvailable = molImagePNGAvailable;
	}



	public List<DsstoxOtherCASRN> getOtherCasrns() {
		return otherCasrns;
	}


	public void setOtherCasrns(List<DsstoxOtherCASRN> otherCasrns) {
		this.otherCasrns = otherCasrns;
	}




}

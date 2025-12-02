package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.ArrayList;

/**
* @author TMARTI02
*/


import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.google.gson.annotations.SerializedName;

import gov.epa.databases.dsstox.entity.DsstoxCompound;

@Entity
@Table(name="dsstox_records", uniqueConstraints={@UniqueConstraint(columnNames = {"dtxcid","dtxsid","fk_dsstox_snapshot_id"})})
public class DsstoxRecord {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="cid")
	private Long cid;
	
	@NotNull(message="smiles required")
	@Column(name="smiles",length=2000)
	private String smiles;
		
	@Column(name="dtxcid")
	private String dtxcid;

	@Column(name="dtxsid")
	private String dtxsid;

	@Column(name="casrn")
	private String casrn;
	
	@SerializedName(value="preferred_name") //when export from database software it will use the database column name not the Java variable name
	@Column(name="preferred_name",length=1024)
	private String preferredName;
	
	@SerializedName(value="jchem_inchi_key")
	@Column(name="jchem_inchi_key")
	private String jchemInchikey;

	
	@SerializedName(value="indigo_inchi_key")
	@Column(name="indigo_inchi_key")
	private String indigoInchikey;

	
	//These should all be true for the records with both sid and cid
	@SerializedName(value="mol_image_png_available")
	@Column(name="mol_image_png_available")
	private Boolean molImagePNGAvailable;
	
	@ManyToOne
	@NotNull(message="DsstoxSnapshot required")
	@JoinColumn(name="fk_dsstox_snapshot_id")
//	@SerializedName(value="fk_dsstox_snapshot_id") //cant regenerate this object from json file this way
	private DsstoxSnapshot dsstoxSnapshot;
	

	@Column(name="generic_substance_updated_at")
	@SerializedName(value="generic_substance_updated_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date genericSubstanceUpdatedAt;

	
	@Column(name="updated_at")
	@SerializedName(value="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	@SerializedName(value="updated_by")
	private String updatedBy;
	
	@Column(name="created_at")
	@SerializedName(value="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="Creator required")
	@Column(name="created_by")
	@SerializedName(value="created_by")
	private String createdBy;

	@SerializedName(value="mol_weight")
	@Column(name="mol_weight")
	private Double molWeight;

	//TODO cascade not working	
//	@OneToMany(mappedBy="dsstoxRecord", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@Transient
	private List<DsstoxOtherCASRN> otherCasrns=new ArrayList<>();

	public DsstoxRecord() {}
	
	
	public DsstoxRecord(gov.epa.databases.dsstox.DsstoxRecord dr) {
		
		this.dtxcid=dr.dsstoxCompoundId;
		this.molWeight=dr.molWeight;
		this.smiles=dr.smiles;
		this.jchemInchikey=dr.jchemInchikey;
		this.indigoInchikey=dr.indigoInchikey;

		this.dtxsid=dr.dsstoxSubstanceId;
		this.casrn=dr.casrn;
		this.preferredName=dr.preferredName;
		
	}
	
	public static List<DsstoxRecord> getRecords(List<DsstoxCompound> compounds, DsstoxSnapshot snapshot,String lanId) {

		List<DsstoxRecord>records=new ArrayList<>();
		
		for (DsstoxCompound c:compounds) {
			String dtxsid=null;
			String casrn=null;
			String preferredName=null;
			Date genericSubstanceUpdatedAt=null;
			
			if(c.getGenericSubstanceCompound()!=null) {
				dtxsid=c.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId();
				casrn=c.getGenericSubstanceCompound().getGenericSubstance().getCasrn();
				preferredName=c.getGenericSubstanceCompound().getGenericSubstance().getPreferredName();
				genericSubstanceUpdatedAt=c.getGenericSubstanceCompound().getGenericSubstance().getUpdatedAt();
//				System.out.println(dtxsid+"\t"+casrn+"\t"+preferredName);
			}
			
			DsstoxRecord record=new DsstoxRecord(snapshot, c.getId(),c.getDsstoxCompoundId(), c.getSmiles(),
					c.getMolWeight(),c.getIndigoInchikey(),c.getJchemInchikey(),
					dtxsid, casrn,preferredName,lanId,genericSubstanceUpdatedAt,c.isMolImagePNGAvailable());
			
//			if (!c.isMolImagePNGAvailable())
//				System.out.println(dtxsid+"\t"+c.isMolImagePNGAvailable());
			
			
			records.add(record);
		}
		return records;
	}
	
	public DsstoxRecord(DsstoxSnapshot dsstoxSnapshot,Long fk_compounds_id,
			String DTXCID, String smiles,Double mol_weight, String indigoInchiKey,String jchemInchikey, 
			String DTXSID,String casrn, String preferredName, String createdBy,Date genericSubstanceUpdatedAt,boolean isMolImageAvailable) {

		this.dsstoxSnapshot=dsstoxSnapshot;

		this.cid=fk_compounds_id;
		this.dtxcid=DTXCID;
		this.smiles=smiles;
		this.molWeight=mol_weight;
		this.indigoInchikey=indigoInchiKey;
		this.jchemInchikey=jchemInchikey;
		
		this.dtxsid=DTXSID;
		this.createdBy=createdBy;
		this.casrn=casrn;
		this.preferredName=preferredName;
		this.molImagePNGAvailable=isMolImageAvailable;
		this.genericSubstanceUpdatedAt=genericSubstanceUpdatedAt;
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


	public String getJchemInchikey() {
		return jchemInchikey;
	}


	public void setJchemInchikey(String jchemInchikey) {
		this.jchemInchikey = jchemInchikey;
	}


	public String getIndigoInchikey() {
		return indigoInchikey;
	}


	public void setIndigoInchikey(String indigoInchikey) {
		this.indigoInchikey = indigoInchikey;
	}


	public Date getGenericSubstanceUpdatedAt() {
		return genericSubstanceUpdatedAt;
	}


	public void setGenericSubstanceUpdatedAt(Date genericSubstanceUpdatedAt) {
		this.genericSubstanceUpdatedAt = genericSubstanceUpdatedAt;
	}




}

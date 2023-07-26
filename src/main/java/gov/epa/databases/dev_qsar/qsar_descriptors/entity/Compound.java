package gov.epa.databases.dev_qsar.qsar_descriptors.entity;

import java.util.Date;

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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="compounds", uniqueConstraints={@UniqueConstraint(columnNames = {"dtxcid", "smiles", "standardizer"})})

//TODO need to make sure that the constraint in DBeaver for compounds table is as follows:
//ALTER TABLE qsar_descriptors.compounds ADD CONSTRAINT unique_cmpd_dtxcid_smiles_and_stdizer UNIQUE (dtxcid, smiles, standardizer)

public class Compound {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message="DTXCID required to create compound")
	@Column(name="dtxcid")
	private String dtxcid;

	@NotBlank(message="Smiles required to create compound")
	@Column(name="smiles", length=1000) //TMM
	private String smiles;

	
	public String getSmiles() {
		return smiles;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	@Column(name="canon_qsar_smiles", length=1000) //TMM
	private String canonQsarSmiles;
	
	@NotBlank(message="Standardizer required to create compound")
	@Column(name="standardizer")
	private String standardizer;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@NotBlank(message="Compound creator required")
	@Column(name="created_by")
	private String createdBy;
	
	public Compound() {}
	
	public Compound(String dtxcid, String smiles, String canonQsarSmiles, String standardizer, String createdBy) {
		this.setDtxcid(dtxcid);
		this.setSmiles(smiles);
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setStandardizer(standardizer);
		this.setCreatedBy(createdBy);
	}

	public Long getId() {
		return id;
	}

	public String getKey() {
		return getDtxcid()+"\t"+getSmiles()+"\t"+getStandardizer();
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getDtxcid() {
		return dtxcid;
	}

	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}

	public String getCanonQsarSmiles() {
		return canonQsarSmiles;
	}

	public void setCanonQsarSmiles(String canonQsarSmiles) {
		this.canonQsarSmiles = canonQsarSmiles;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
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

	public String getStandardizer() {
		return standardizer;
	}

	public void setStandardizer(String standardizer) {
		this.standardizer = standardizer;
	}

}

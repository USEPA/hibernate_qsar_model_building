package gov.epa.databases.dev_qsar.exp_prop.entity;

import java.util.Date;

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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

import org.hibernate.Criteria;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.criterion.Restrictions;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(name="source_chemicals", uniqueConstraints={@UniqueConstraint(columnNames = {
		"source_casrn",
		"source_smiles",
		"source_chemical_name",
		"source_dtxsid",
		"source_dtxcid",
		"source_dtxrid",
		"fk_public_source_id",
		"fk_literature_source_id"})})
public class SourceChemical {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="source_casrn")
	private String sourceCasrn;
	
	@Column(name="source_smiles", length=255)
	@Length(max=255)
	private String sourceSmiles;
	
	@Column(name="source_chemical_name", length=255)
	@Length(max=255)
	private String sourceChemicalName;
	
	@ManyToOne
	@JoinColumn(name="fk_public_source_id")
	private PublicSource publicSource;
	
	@ManyToOne
	@JoinColumn(name="fk_literature_source_id")
	private LiteratureSource literatureSource;
	
	@Column(name="source_dtxsid")
	private String sourceDtxsid;
	
	@Column(name="source_dtxcid")
	private String sourceDtxcid;
	
	@Column(name="source_dtxrid")
	private String sourceDtxrid;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotBlank(message="SourceCollection creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@AssertTrue(message = "Public or literature source is required")
	private boolean isPublicSourceOrLiteratureSourceExists() {
	    return publicSource != null || literatureSource != null;
	}
	
	public String generateSrcChemId() {
		return String.format("SCH%012d", id);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSourceCasrn() {
		return sourceCasrn;
	}

	public void setSourceCasrn(String sourceCasrn) {
		this.sourceCasrn = sourceCasrn;
	}

	public String getSourceSmiles() {
		return sourceSmiles;
	}

	public void setSourceSmiles(String sourceSmiles) {
		this.sourceSmiles = sourceSmiles;
	}

	public String getSourceChemicalName() {
		return sourceChemicalName;
	}

	public void setSourceChemicalName(String sourceChemicalName) {
		this.sourceChemicalName = sourceChemicalName;
	}

	public PublicSource getPublicSource() {
		return publicSource;
	}

	public void setPublicSource(PublicSource publicSource) {
		this.publicSource = publicSource;
	}

	public LiteratureSource getLiteratureSource() {
		return literatureSource;
	}

	public void setLiteratureSource(LiteratureSource literatureSource) {
		this.literatureSource = literatureSource;
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

	public String getSourceDtxsid() {
		return sourceDtxsid;
	}

	public void setSourceDtxsid(String sourceDtxsid) {
		this.sourceDtxsid = sourceDtxsid;
	}

	public String getSourceDtxcid() {
		return sourceDtxcid;
	}

	public void setSourceDtxcid(String sourceDtxcid) {
		this.sourceDtxcid = sourceDtxcid;
	}

	public String getSourceDtxrid() {
		return sourceDtxrid;
	}

	public void setSourceDtxrid(String sourceDtxrid) {
		this.sourceDtxrid = sourceDtxrid;
	}

	public String getKey() {
		
		Long publicSourceID=null;
		if (publicSource!=null) publicSourceID=publicSource.getId();
		
		Long literatureSourceID=null;
		if (literatureSource!=null) literatureSourceID=literatureSource.getId();
		

		String key=sourceCasrn+"\t"+sourceSmiles+"\t"+sourceChemicalName+"\t"+sourceDtxsid+"\t"+sourceDtxcid+"\t"+
				sourceDtxrid+"\t"+publicSourceID+"\t"+literatureSourceID;

		return key;
	}
}

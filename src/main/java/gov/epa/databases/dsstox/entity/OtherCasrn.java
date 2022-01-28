package gov.epa.databases.dsstox.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name="other_casrns")
public class OtherCasrn {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="casrn")
	private String casrn;
	
	@ManyToOne
	@JoinColumn(name="fk_generic_substance_id")
	private GenericSubstance genericSubstance;
	
	@Column(name="source")
	private String source;
	
	@Column(name="cas_type")
	private String casType;
	
	@Column(name="qc_notes")
	private String qcNotes;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;
	
	public OtherCasrn() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCasrn() {
		return casrn;
	}

	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}

	public GenericSubstance getGenericSubstance() {
		return genericSubstance;
	}

	public void setGenericSubstance(GenericSubstance genericSubstance) {
		this.genericSubstance = genericSubstance;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getCasType() {
		return casType;
	}

	public void setCasType(String casType) {
		this.casType = casType;
	}

	public String getQcNotes() {
		return qcNotes;
	}

	public void setQcNotes(String qcNotes) {
		this.qcNotes = qcNotes;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}

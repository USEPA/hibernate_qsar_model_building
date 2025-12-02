package gov.epa.databases.dsstox.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;


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
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name="updated_at")
	@UpdateTimestamp
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

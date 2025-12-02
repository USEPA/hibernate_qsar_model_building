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
@Table(name="synonyms")
public class Synonym {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="identifier")
	private String identifier;
	
	@ManyToOne
	@JoinColumn(name="fk_generic_substance_id")
	private GenericSubstance genericSubstance;
	
	@Column(name="source")
	private String source;

	@Column(name="synonym_type")
	private String synonymType;

	@Column(name="synonym_quality")
	private String synonymQuality;

	@Column(name="qc_notes")
	private String qcNotes;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;

	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	public Synonym() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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

	public String getType() {
		return synonymType;
	}

	public void setType(String type) {
		this.synonymType = type;
	}

	public String getQuality() {
		return synonymQuality;
	}

	public void setQuality(String quality) {
		this.synonymQuality = quality;
	}

	public String getQcNotes() {
		return qcNotes;
	}

	public void setQcNotes(String qcNotes) {
		this.qcNotes = qcNotes;
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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

}

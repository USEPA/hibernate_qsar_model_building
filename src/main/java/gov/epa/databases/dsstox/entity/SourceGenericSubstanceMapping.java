package gov.epa.databases.dsstox.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name="source_generic_substance_mappings")
public class SourceGenericSubstanceMapping {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="fk_generic_substance_id")
	private GenericSubstance genericSubstance;
	
    @OneToOne
	@JoinColumn(name="fk_source_substance_id")
	private SourceSubstance sourceSubstance;
	
	@Column(name="connection_reason")
	private String connectionReason;
	
	@Column(name="linkage_score")
	private Double linkageScore;
	
	@Column(name="curator_validated")
	private Boolean curatorValidated;
	
	@Column(name="qc_notes")
	private String qcNotes;

	@Column(name="created_at")
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedAt;

	@Column(name="created_by")
	private String createdBy;

	@Column(name="updated_by")
	private String updatedBy;
	
	public SourceGenericSubstanceMapping() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GenericSubstance getGenericSubstance() {
		return genericSubstance;
	}

	public void setGenericSubstance(GenericSubstance genericSubstance) {
		this.genericSubstance = genericSubstance;
	}

	public SourceSubstance getSourceSubstance() {
		return sourceSubstance;
	}

	public void setSourceSubstance(SourceSubstance sourceSubstance) {
		this.sourceSubstance = sourceSubstance;
	}

	public String getConnectionReason() {
		return connectionReason;
	}

	public void setConnectionReason(String connectionReason) {
		this.connectionReason = connectionReason;
	}

	public Double getLinkageScore() {
		return linkageScore;
	}

	public void setLinkageScore(Double linkageScore) {
		this.linkageScore = linkageScore;
	}

	public Boolean getCuratorValidated() {
		return curatorValidated;
	}

	public void setCuratorValidated(Boolean curatorValidated) {
		this.curatorValidated = curatorValidated;
	}

	public String getQcNotes() {
		return qcNotes;
	}

	public void setQcNotes(String qcNotes) {
		this.qcNotes = qcNotes;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
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

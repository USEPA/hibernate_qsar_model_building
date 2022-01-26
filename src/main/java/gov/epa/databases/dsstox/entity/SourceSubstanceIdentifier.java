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
@Table(name="source_substance_identifiers")
public class SourceSubstanceIdentifier {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;

	@Column(name="identifier")
	private String identifier;

    @Column(name="identifier_type")
    private String identifierType;

    @Column(name="label")
    private String label;

	@ManyToOne
	@JoinColumn(name="fk_source_substance_id")
	private SourceSubstance sourceSubstance;

//    @OneToOne
//    @JoinColumn(name="fk_source_substance_identifier_parent")
//    private SourceSubstanceIdentifier parentSSI;
//
//    @OneToOne(mappedBy="parentSSI")
//    private SourceSubstanceIdentifier ChildSSI;

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

	public SourceSubstanceIdentifier() {}

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

	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public SourceSubstance getSourceSubstance() {
		return sourceSubstance;
	}

	public void setSourceSubstance(SourceSubstance sourceSubstance) {
		this.sourceSubstance = sourceSubstance;
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

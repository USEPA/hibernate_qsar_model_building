package gov.epa.databases.dsstox.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name="generic_substance_compounds")
public class GenericSubstanceCompound {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@Column(name="relationship")
	private String relationship;
	
	@Column(name="source")
	private String source;

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
	
	// Table joins
	@OneToOne
	@JoinColumn(name="fk_generic_substance_id")
	private GenericSubstance genericSubstance;
	
    @OneToOne
	@JoinColumn(name="fk_compound_id")
    private DsstoxCompound compound;
	
	public GenericSubstanceCompound() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
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

	public GenericSubstance getGenericSubstance() {
		return genericSubstance;
	}

	public void setGenericSubstance(GenericSubstance genericSubstance) {
		this.genericSubstance = genericSubstance;
	}

	public DsstoxCompound getCompound() {
		return compound;
	}

	public void setCompound(DsstoxCompound compound) {
		this.compound = compound;
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
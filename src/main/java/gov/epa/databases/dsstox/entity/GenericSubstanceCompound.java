package gov.epa.databases.dsstox.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

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
	@Generated(value=GenerationTime.INSERT)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Column(name="updated_at")
	@Generated(value=GenerationTime.ALWAYS)
	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

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

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
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
}
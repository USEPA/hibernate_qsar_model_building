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
@Table(name="compound_relationships")
public class CompoundRelationship {
	@Id
	@GeneratedValue
	@Column(name="id")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="fk_compound_id_predecessor")
	private DsstoxCompound predecessorCompound;
	
	@ManyToOne
	@JoinColumn(name="fk_compound_id_successor")
	private DsstoxCompound successorCompound;
	
	@ManyToOne
	@JoinColumn(name="fk_compound_relationship_type_id")
	private CompoundRelationshipType compoundRelationshipType;
	
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

	public CompoundRelationship() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DsstoxCompound getPredecessorCompound() {
		return predecessorCompound;
	}

	public void setPredecessorCompound(DsstoxCompound predecessorCompound) {
		this.predecessorCompound = predecessorCompound;
	}

	public DsstoxCompound getSuccessorCompound() {
		return successorCompound;
	}

	public void setSuccessorCompound(DsstoxCompound successorCompound) {
		this.successorCompound = successorCompound;
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

	public CompoundRelationshipType getCompoundRelationshipType() {
		return compoundRelationshipType;
	}

	public void setCompoundRelationshipType(CompoundRelationshipType compoundRelationshipType) {
		this.compoundRelationshipType = compoundRelationshipType;
	}
}

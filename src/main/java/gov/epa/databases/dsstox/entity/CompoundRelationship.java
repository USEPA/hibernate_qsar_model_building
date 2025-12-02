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
//import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.UpdateTimestamp;

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

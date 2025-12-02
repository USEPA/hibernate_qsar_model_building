package gov.epa.databases.dev_qsar.qsar_datasets.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="splittings", schema = "qsar_datasets", indexes={@Index(name="split_name_idx", columnList="name", unique=true)})
//@SecondaryTable(name = "predictions",  schema = "qsar_models", catalog = "", pkJoinColumns = {@PrimaryKeyJoinColumn(name = "fk_splitting_id", referencedColumnName = "id")})

public class Splitting {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Name required")
	@Column(name="name", unique=true)
	private String name;
	
	@NotNull(message="Description required")
	@Column(name="description")
	private String description;
	
	@NotNull(message="Number of splits required")
	@Column(name="num_splits")
	private Integer numSplits;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;
	
	@Column(name="updated_by")
	private String updatedBy;
	
	@Column(name="created_at")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@NotNull(message="Creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@OneToMany(mappedBy="splitting", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<DataPointInSplitting> dataPointsInSplitting;
	
	public Splitting() {}
	
	public Splitting(String name, String description, Integer numSplits, String createdBy) {
		this.setName(name);
		this.setDescription(description);
		this.setNumSplits(numSplits);
		this.setCreatedBy(createdBy);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getNumSplits() {
		return numSplits;
	}

	public void setNumSplits(Integer numSplits) {
		this.numSplits = numSplits;
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
}

package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="methods", indexes={@Index(name="method_name_idx", columnList="name", unique=true)})
public class Method {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Name required")
	@Column(name="name", unique=true)
	private String name;
	
	@NotNull(message="Description required")
	@Column(name="description", length=2047)
	private String description;

	public String getDescription_url() {
		return description_url;
	}

	public void setDescription_url(String description_url) {
		this.description_url = description_url;
	}

	@Column(name="description_url", length=2047)
	private String description_url;
	
	@Column(name="hyperparameter_grid", length=2047)
	private String hyperparameter_grid;
	
	@NotNull(message="Method type required")
	@Column(name="is_binary")
	private Boolean isBinary;

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
	
	public Method() {}
	
	public Method(String name, String description,String description_url, String hyperparameter_grid, Boolean isBinary, String createdBy) {
		this.setName(name);
		this.setDescription(description);
		this.setDescription_url(description_url);
		this.setHyperparameter_grid(hyperparameter_grid);
		this.setIsBinary(isBinary);
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

	public String getHyperparameter_grid() {
		return hyperparameter_grid;
	}

	public void setHyperparameter_grid(String hyperparameter_grid) {
		this.hyperparameter_grid = hyperparameter_grid;
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

	public Boolean getIsBinary() {
		return isBinary;
	}

	public void setIsBinary(Boolean isBinary) {
		this.isBinary = isBinary;
	}
}

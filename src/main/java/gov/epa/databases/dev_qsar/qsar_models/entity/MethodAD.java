package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="ad_methods", indexes={@Index(name="ad_method_name_idx", columnList="name", unique=true)})
public class MethodAD {

	public String getName() {
		return name;
	}

	public MethodAD() {}
			
	public MethodAD(String name,String description, String methodScope,
			String createdBy) {
		
		this.name = name;
		this.description = description;
		this.methodScope = methodScope;
		this.createdBy = createdBy;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message="Name required")
	@Column(name="name", unique=true)
	private String name;
	
	@NotBlank(message="Description required")
	@Column(name="description", length=100)
	private String description;
		
	@Column(name="method_scope", length=20)
	private String methodScope;
	
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
	
	@NotBlank(message="Creator required")
	@Column(name="created_by")
	private String createdBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMethodScope() {
		return methodScope;
	}

	public void setMethodScope(String methodScope) {
		this.methodScope = methodScope;
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

	public void setName(String name) {
		this.name = name;
	}
}

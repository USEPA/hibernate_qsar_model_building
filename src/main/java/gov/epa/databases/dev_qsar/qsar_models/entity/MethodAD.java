package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="ad_methods", indexes={@Index(name="ad_method_name_idx", columnList="name", unique=true)})
public class MethodAD {


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

	@NotNull(message="Name required")
	@Column(name="name", unique=true)
	private String name;

		
	@Column(name="name_display", unique=true)
	private String name_display;

	
	@NotNull(message="Description required")
	@Column(name="description", length=256)
	private String description;
	
	@Column(name="description_long")
	private String description_long;

		
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
	
	@NotNull(message="Creator required")
	@Column(name="created_by")
	private String createdBy;

	
	public String getName() {
		return name;
	}

	
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

	public String getName_display() {
		return name_display;
	}

	public void setName_display(String name_display) {
		this.name_display = name_display;
	}

	public String getDescription_long() {
		return description_long;
	}

	public void setDescription_long(String description_long) {
		this.description_long = description_long;
	}
}

package gov.epa.databases.dev_qsar.qsar_descriptors.entity;

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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name="descriptor_sets", indexes={@Index(name="descset_name_idx", columnList="name", unique=true)})
public class DescriptorSet {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message="descriptor set name required")
	@Column(name="name", unique=true)
	private String name;
	
	// this one I'm not sure if it's alright to keep blank.
	@NotBlank(message="description field required")
	@Column(name="description")
	private String description;
	
	@NotBlank(message="headers_tsv field required")
	@Column(length = 32767, name="headers_tsv")
	private String headersTsv;

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
	
	@NotBlank(message="Descriptor set creator required")
	@Column(name="created_by")
	private String createdBy;



	public DescriptorSet() {}
	
	public DescriptorSet(String name, String description, String createdBy, String headersTsv) {
		this.setName(name);
		this.setDescription(description);
		this.setCreatedBy(createdBy);
		this.setHeadersTsv(headersTsv);
	}

	public void setHeadersTsv(String headersTsv) {
		this.headersTsv = headersTsv;
	}
	
	public String getHeadersTsv() {
		return headersTsv;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	private void setCreatedBy(String createdBy) {
		this.createdBy=createdBy;
	}

	public String getCreatedBy() {
		return createdBy;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	
}

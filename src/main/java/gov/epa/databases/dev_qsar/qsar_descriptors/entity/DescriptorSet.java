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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


@Entity
@Table(name="descriptor_sets", indexes={@Index(name="descset_name_idx", columnList="name", unique=true)})
public class DescriptorSet {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="descriptor set name required")
	@Column(name="name", unique=true)
	private String name;
	
	// this one I'm not sure if it's alright to keep blank.
	@NotNull(message="description field required")
	@Column(name="description")
	private String description;
	
	@NotNull(message="headers_tsv field required")
	@Column(length = 32767, name="headers_tsv")
	private String headersTsv;
	
	@Column(name="descriptor_service")
	private String descriptorService;
	
	@Column(length = 1000, name="descriptor_service_options")
	private String descriptorServiceOptions;

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
	
	@NotNull(message="Descriptor set creator required")
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

	public String getDescriptorService() {
		return descriptorService;
	}

	public void setDescriptorService(String descriptorService) {
		this.descriptorService = descriptorService;
	}

	public String getDescriptorServiceOptions() {
		return descriptorServiceOptions;
	}

	public void setDescriptorServiceOptions(String descriptorServiceOptions) {
		this.descriptorServiceOptions = descriptorServiceOptions;
	}
	

	
}

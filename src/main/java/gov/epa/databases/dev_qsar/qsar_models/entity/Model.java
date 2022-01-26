package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="models")
public class Model {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Method required")
	@ManyToOne
	@JoinColumn(name="fk_method_id")
	private Method method;
	
	@NotNull(message="Descriptor set name required")
	@Column(name="descriptor_set_name")
	private String descriptorSetName;
	
	@NotNull(message="Dataset name required")
	@Column(name="dataset_name")
	private String datasetName;
	
	@NotNull(message="Splitting name required")
	@Column(name="splitting_name")
	private String splittingName;
	
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
	
	@OneToOne(mappedBy="model", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private ModelBytes modelBytes;
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL)
	private List<ModelInModelSet> modelInModelSets;
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL)
	private List<ModelStatistic> modelStatistics;
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<Prediction> predictions;
	
	public Model() {}
	
	public Model(Method method, String descriptorSetName, String datasetName, String splittingName, String createdBy) {
		this.setMethod(method);
		this.setDescriptorSetName(descriptorSetName);
		this.setDatasetName(datasetName);
		this.setSplittingName(splittingName);
		this.setCreatedBy(createdBy);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public String getDescriptorSetName() {
		return descriptorSetName;
	}

	public void setDescriptorSetName(String descriptorSetName) {
		this.descriptorSetName = descriptorSetName;
	}

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public String getSplittingName() {
		return splittingName;
	}

	public void setSplittingName(String splittingName) {
		this.splittingName = splittingName;
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

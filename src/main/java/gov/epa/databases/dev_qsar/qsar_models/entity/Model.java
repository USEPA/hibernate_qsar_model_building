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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

//TODO add name, version columns 

@Entity
@Table(name="models")
public class Model {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	
	@Column(name="name")
	private String name;

	
	@NotNull(message="Method required")
	@ManyToOne
	@JoinColumn(name="fk_method_id")
	private Method method;
	
	@ManyToOne
	@JoinColumn(name="fk_descriptor_embedding_id")
	private DescriptorEmbedding descriptorEmbedding;
	
	@NotNull(message="Descriptor set name required")
	@Column(name="descriptor_set_name")
	private String descriptorSetName;

//	@Column(name="source")
//	private String source;
	
	@ManyToOne
	@JoinColumn(name="fk_source_id")
	private Source source;

	
	@NotNull(message="Dataset name required")
	@Column(name="dataset_name")
	private String datasetName;
	
	@NotNull(message="Splitting name required")
	@Column(name="splitting_name")
	private String splittingName;
	
	@Column(name="hyperparameters")
	private String hyperparameters;

	@Column(name="details")
	private byte[] details;

	
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
	
//	@Column(name="url_scatter_plot")
//	private String urlScatterPlot;
//	
//	@Column(name="url_histogram")
//	private String urlHistogram;
	
	
//	@OneToOne(mappedBy="model", cascade=CascadeType.ALL, fetch=FetchType.LAZY, optional=false)
//	private ModelBytes modelBytes;
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL)
	private List<ModelInModelSet> modelInModelSets;
	
	@OneToMany(mappedBy="model", fetch=FetchType.EAGER,cascade=CascadeType.ALL)
	private List<ModelStatistic> modelStatistics;
	
	@OneToMany(mappedBy="model", cascade=CascadeType.ALL)
	private List<Prediction> predictions;
	
	@OneToMany(mappedBy="consensusModel", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private List<ModelInConsensusModel> modelsInConsensusModel;
	
	public Model() {}
	
	public Model(String name, Method method, String descriptorSetName, String datasetName, String splittingName, Source source, String createdBy) {
		this.setName(name);
		this.setMethod(method);
		this.setDescriptorSetName(descriptorSetName);
		this.setDatasetName(datasetName);
		this.setSplittingName(splittingName);
		this.setSource(source);
		this.setCreatedBy(createdBy);
	}
	
	public Model(String name, Method method, DescriptorEmbedding descriptorEmbedding, 
			String descriptorSetName, String datasetName, String splittingName, Source source,String createdBy) {
		this.setName(name);
		this.setMethod(method);
		this.setDescriptorEmbedding(descriptorEmbedding);
		this.setDescriptorSetName(descriptorSetName);
		this.setDatasetName(datasetName);
		this.setSplittingName(splittingName);
		this.setSource(source);
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

	public DescriptorEmbedding getDescriptorEmbedding() {
		return descriptorEmbedding;
	}

	public void setDescriptorEmbedding(DescriptorEmbedding descriptorEmbedding) {
		this.descriptorEmbedding = descriptorEmbedding;
	}

	public List<ModelInConsensusModel> getModelsInConsensusModel() {
		return modelsInConsensusModel;
	}
	
	public String getHyperparameters() {
		return hyperparameters;
	}

	public void setHyperparameters(String hyperparameters) {
		this.hyperparameters = hyperparameters;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getDetails() {
		return details;
	}

	public void setDetails(byte[] details) {
		this.details = details;
	}

	public List<ModelStatistic> getModelStatistics() {
		return modelStatistics;
	}

	public void setModelStatistics(List<ModelStatistic> modelStatistics) {
		this.modelStatistics = modelStatistics;
	}

	public List<ModelInModelSet> getModelInModelSets() {
		return modelInModelSets;
	}

	public void setModelInModelSets(List<ModelInModelSet> modelInModelSets) {
		this.modelInModelSets = modelInModelSets;
	}

	public List<Prediction> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<Prediction> predictions) {
		this.predictions = predictions;
	}

	public void setModelsInConsensusModel(List<ModelInConsensusModel> modelsInConsensusModel) {
		this.modelsInConsensusModel = modelsInConsensusModel;
	}

//	public String getUrlScatterPlot() {
//		return urlScatterPlot;
//	}
//
//	public void setUrlScatterPlot(String urlScatterPlot) {
//		this.urlScatterPlot = urlScatterPlot;
//	}
//
//	public String getUrlHistogram() {
//		return urlHistogram;
//	}
//
//	public void setUrlHistogram(String urlHistogram) {
//		this.urlHistogram = urlHistogram;
//	}

}

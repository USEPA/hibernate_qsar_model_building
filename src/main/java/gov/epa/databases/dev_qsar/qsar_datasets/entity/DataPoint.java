package gov.epa.databases.dev_qsar.qsar_datasets.entity;

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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="data_points", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles", "fk_dataset_id"})})
public class DataPoint {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message="SMILES required required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;
	
	@NotNull(message="QSAR property value required")
	@Column(name="qsar_property_value")
	private Double qsarPropertyValue;

	@Column(name="dtxcid")
	private String dtxcid;
	

	@NotNull(message="Outlier boolean required")
	@Column(name="outlier")
	private Boolean outlier;
	
	@ManyToOne
	@NotNull(message="Dataset required")
	@JoinColumn(name="fk_dataset_id")
	private Dataset dataset;
	
	@OneToMany(mappedBy="dataPoint", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private List<DataPointContributor> dataPointContributors;
	
	@OneToMany(mappedBy="dataPoint", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<DataPointInSplitting> dataPointInSplitting;
	
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
	
	public DataPoint() {}
	
	public DataPoint(String canonQsarSmiles, Double qsarPropertyValue, Dataset dataset, Boolean outlier, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setQsarPropertyValue(qsarPropertyValue);
		this.setDataset(dataset);
		this.setOutlier(outlier);
		this.setCreatedBy(createdBy);
	}
	
	public DataPoint(String canonQsarSmiles, String DTXCID, Double qsarPropertyValue, Dataset dataset, Boolean outlier, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setDtxcid(DTXCID);
		this.setQsarPropertyValue(qsarPropertyValue);
		this.setDataset(dataset);
		this.setOutlier(outlier);
		this.setCreatedBy(createdBy);
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCanonQsarSmiles() {
		return canonQsarSmiles;
	}

	public void setCanonQsarSmiles(String canonQsarSmiles) {
		this.canonQsarSmiles = canonQsarSmiles;
	}

	public Double getQsarPropertyValue() {
		return qsarPropertyValue;
	}

	public void setQsarPropertyValue(Double qsarPropertyValue) {
		this.qsarPropertyValue = qsarPropertyValue;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
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

	public Boolean getOutlier() {
		return outlier;
	}

	public void setOutlier(Boolean outlier) {
		this.outlier = outlier;
	}

	public List<DataPointContributor> getDataPointContributors() {
		return dataPointContributors;
	}

	public void setDataPointContributors(List<DataPointContributor> dataPointContributors) {
		this.dataPointContributors = dataPointContributors;
	}
	
	public String getDtxcid() {
		return dtxcid;
	}

	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}

}

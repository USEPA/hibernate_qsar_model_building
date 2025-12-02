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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.CheckStructure;


@Entity
@Table(name="data_points", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles", "fk_dataset_id"})})
public class DataPoint {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="SMILES required required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;
	
	@NotNull(message="QSAR property value required")
	@Column(name="qsar_property_value")
	private Double qsarPropertyValue;

	@Column(name="qsar_dtxcid")
	private String qsar_dtxcid;
	
	@Column(name="qsar_exp_prop_property_values_id")
	private String qsar_exp_prop_property_values_id;

	@NotNull(message="Outlier boolean required")
	@Column(name="outlier")
	private Boolean outlier;
	
	@ManyToOne
//	@NotFound(action=NotFoundAction.IGNORE)//https://edwin.baculsoft.com/2013/02/a-weird-hibernate-exception-org-hibernate-objectnotfoundexception-no-row-with-the-given-identifier-exists/
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
	
	@NotNull(message="Creator required")
	@Column(name="created_by")
	private String createdBy;
	
	@Transient
	public CheckStructure checkStructure;//for validation exercise (not for db)
	
	@Transient
	public String inchiKey1_qsarSmiles;
	
	public DataPoint() {}
	
	public DataPoint(String canonQsarSmiles, Double qsarPropertyValue, Dataset dataset, Boolean outlier, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		this.setQsarPropertyValue(qsarPropertyValue);
		this.setDataset(dataset);
		this.setOutlier(outlier);
		this.setCreatedBy(createdBy);
	}
	
	public DataPoint(String canonQsarSmiles, String DTXCID, String exp_prop_id, Double qsarPropertyValue, Dataset dataset, Boolean outlier, String createdBy) {
		this.setCanonQsarSmiles(canonQsarSmiles);
		
		//To account for edge case when loading CERAPP/COMPARA experimental values for dashboard:
		if(DTXCID.length()>500) {
			this.setQsar_dtxcid(DTXCID.substring(0,495)+"...");
		} else {
			this.setQsar_dtxcid(DTXCID);	
		}
		
		if (exp_prop_id.length()>255) {
			this.setQsar_exp_prop_property_values_id(exp_prop_id.substring(0,250)+"...");
		} else {
			this.setQsar_exp_prop_property_values_id(exp_prop_id);	
		}
		
		
		
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
	
	public String getQsar_dtxcid() {
		return qsar_dtxcid;
	}

	public void setQsar_dtxcid(String dtxcid) {
		this.qsar_dtxcid = dtxcid;
	}
	
	public List<DataPointInSplitting> getDataPointInSplitting() {
		return dataPointInSplitting;
	}

	public String getQsar_exp_prop_property_values_id() {
		return qsar_exp_prop_property_values_id;
	}

	public void setQsar_exp_prop_property_values_id(String qsar_exp_prop_property_values_id) {
		this.qsar_exp_prop_property_values_id = qsar_exp_prop_property_values_id;
	}


}

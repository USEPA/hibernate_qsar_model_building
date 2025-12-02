package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.lang.reflect.Field;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
//import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;


@Entity()
@Table(name = "predictions_dashboard", uniqueConstraints={@UniqueConstraint(columnNames = {"canon_qsar_smiles","fk_dsstox_records_id", "fk_model_id"})})
public class PredictionDashboard {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message="Canonical QSAR-ready SMILES required")
	@Column(name="canon_qsar_smiles")
	private String canonQsarSmiles;


//	@NotNull(message="fk_dsstox_records_id required")
//	@Column(name="fk_dsstox_records_id")
//	private Long fk_dsstox_records_id;//alternatively can just store DTXCID and/or smiles in this table
//
//	@Transient
//	private DsstoxRecord dsstoxRecord;//temp storage for convenience (dont have in table because in different schema)- maybe move to this schema 

	@ManyToOne
	@NotNull(message="fk_dsstox_records_id required")
	@JoinColumn(name="fk_dsstox_records_id")
	private DsstoxRecord dsstoxRecord;//temp storage for convenience (dont have in table because in different schema)- maybe move to this schema 

//	@Transient  
	private String dtxcid;//needed for storing cases where there is no matching dsstoxRecord
	
	@Transient
	public String endpoint;//property name from original software like TEST, not for database
	
	@ManyToOne
	@NotNull(message="Model required")
	@JoinColumn(name="fk_model_id")
	private Model model;
	
	@Column(name="experimental_value")
	private Double experimentalValue;
	
	@Column(name="experimental_string")
	private String experimentalString;
	
	@Column(name="prediction_value")
	private Double predictionValue;
	
	@Column(name="prediction_string")
	private String predictionString;
	
	@Column(name="prediction_error")
	private String predictionError;
	
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
	
	public String getKey() {
//		return canonQsarSmiles+"\t"+getDsstoxRecord().getId()+"\t"+model.getId();
		return canonQsarSmiles+"\t"+getDtxcid()+"\t"+model.getId();//TODO dont need smiles?
	}

	@OneToMany(mappedBy="predictionDashboard", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<QsarPredictedNeighbor> qsarPredictedNeighbors;
	
	@OneToMany(mappedBy="predictionDashboard", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private List<QsarPredictedADEstimate> qsarPredictedADEstimates;
	
	@OneToOne(mappedBy="predictionDashboard", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	private PredictionReport predictionReport;
	

	public static String getHeader() {
		return "id\tdtxcid\tmodel_name\texperimentalString\texperimentalValue\tpredictionString\tpredictionValue\tpredictionError";	
	}
				

	public String toTsv() {

		
		return getId()+"\t"+getDtxcid()+ "\t" + model.getName() + "\t"
				+ experimentalString + "\t" + experimentalValue + "\t" + predictionString + "\t"
				+ predictionValue + "\t" + predictionError;

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


	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Double getPredictionValue() {
		return predictionValue;
	}

	public void setPredictionValue(Double predictionValue) {
		this.predictionValue = predictionValue;
	}

	public String getPredictionString() {
		return predictionString;
	}

	public void setPredictionString(String predictionString) {
		this.predictionString = predictionString;
	}
	
	
	public String toJson() {
		JsonObject jo = toJsonObject();
		return Utilities.gson.toJson(jo);
	}
	
	/**
	 * Makes json without carriage returns
	 * 
	 * @return
	 */
	public String toJsonOneLine() {
		JsonObject jo = toJsonObject();
		Gson gson=new Gson();
		return gson.toJson(jo);
	}

	private JsonObject toJsonObject() {
		JsonObject jo=new JsonObject();

		jo.addProperty("dtxcid", this.getDtxcid());
		
//		jo.addProperty("dtxsid", this.getDsstoxRecord().getDtxsid());
//		jo.addProperty("casrn", this.getDsstoxRecord().getCasrn());
//		jo.addProperty("preferredName", this.getDsstoxRecord().getPreferredName());
//		jo.addProperty("smiles", this.getDsstoxRecord().getSmiles());

		jo.addProperty("canonQsarSmiles", canonQsarSmiles);
		
		jo.addProperty("experimentalString", experimentalString);
		jo.addProperty("experimentalValue", experimentalValue);
		
//		if(experimentalString!=null) {
//			System.out.println(this.getDsstoxRecord().getCasrn()+"\t"+model.getName()+"\t"+experimentalString);
//		}
//		
//		if(experimentalValue!=null) {
//			System.out.println(this.getDsstoxRecord().getCasrn()+"\t"+model.getName()+"\t"+experimentalValue);
//		}

		jo.addProperty("modelName", model.getName_ccd());
		
		jo.addProperty("predictionError", predictionError);
		jo.addProperty("predictionString", predictionString);
		jo.addProperty("predictionValue", predictionValue);

		if(predictionReport!=null) {
			String jsonReport=new String(predictionReport.getFileJson());
			JsonObject joReport=Utilities.gson.fromJson(jsonReport, JsonObject.class);
//			jo.addProperty("predictionReport", jsonReport);
			jo.addProperty("predictionReport", Utilities.gson.toJson(joReport));
		}
		
		JsonArray jaAD=new JsonArray();
		
		for (QsarPredictedADEstimate ad:qsarPredictedADEstimates) {
			JsonObject joAD=new JsonObject();
			joAD.addProperty("name", ad.getMethodAD().getName());
			joAD.addProperty("value", ad.getApplicabilityValue());
			joAD.addProperty("conclusion", ad.getConclusion());
			joAD.addProperty("reasoning", ad.getReasoning());
			jaAD.add(joAD);
		}
		
		jo.add("applicabilityDomains",jaAD);
		
//		jo.addProperty("createdAt", createdAt.toString());
//		jo.addProperty("createdBy", createdBy);

		return jo;
	}
	

	
	


	public String getPredictionError() {
		return predictionError;
	}

	public void setPredictionError(String predictionError) {
		this.predictionError = predictionError;
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



	public Double getExperimentalValue() {
		return experimentalValue;
	}

	public void setExperimentalValue(Double experimentalValue) {
		this.experimentalValue = experimentalValue;
	}

	public String getExperimentalString() {
		return experimentalString;
	}

	public void setExperimentalString(String experimentalString) {
		this.experimentalString = experimentalString;
	}


	public List<QsarPredictedNeighbor> getQsarPredictedNeighbors() {
		return qsarPredictedNeighbors;
	}


	public void setQsarPredictedNeighbors(List<QsarPredictedNeighbor> qsarPredictedNeighbors) {
		this.qsarPredictedNeighbors = qsarPredictedNeighbors;
	}


	public List<QsarPredictedADEstimate> getQsarPredictedADEstimates() {
		return qsarPredictedADEstimates;
	}


	public void setQsarPredictedADEstimates(List<QsarPredictedADEstimate> qsarPredictedADEstimates) {
		this.qsarPredictedADEstimates = qsarPredictedADEstimates;
	}


	public DsstoxRecord getDsstoxRecord() {
		return dsstoxRecord;
	}


	public void setDsstoxRecord(DsstoxRecord dsstoxRecord) {
		this.dsstoxRecord = dsstoxRecord;
	}


	public String getDtxcid() {
		return dtxcid;
	}


	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}


	public PredictionReport getPredictionReport() {
		return predictionReport;
	}


	public void setPredictionReport(PredictionReport predictionReport) {
		this.predictionReport = predictionReport;
	}


	public String getEndpoint() {
		return endpoint;
	}


	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}



}

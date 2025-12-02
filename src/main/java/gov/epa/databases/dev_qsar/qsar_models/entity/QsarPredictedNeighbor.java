package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import gov.epa.run_from_java.scripts.PredictionDashboard.PredictionDashboardTableMaps;
import gov.epa.run_from_java.scripts.PredictionDashboard.OPERA.HTMLReportCreatorOpera;

@Entity
//@Table(name="qsar_predicted_neighbors", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_predictions_dashboard_id", "neighbor_number","fk_dsstox_records_id"})})
@Table(name="qsar_predicted_neighbors", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_predictions_dashboard_id", "neighbor_number","casrn","dtxsid","split_num"})})

public class QsarPredictedNeighbor {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull(message="Neighbor number required")
	@Column(name="neighbor_number")
	private Integer neighborNumber;


//	@Column(name="preferred_name")
//	private String preferredName;

//	@Column(name="cid")
//	private Long cid;

//	@Column(name="mol_image_png_available")
//	private Boolean molImagePNGAvailable;
	
//	@NotNull(message="QsarPredictedProperty required")
//	@JoinColumn(name="fk_qsar_predicted_properties_id")
//	@ManyToOne
//	public QsarPredictedProperty QsarPredictedProperty;//the prediction that all n neighbors are associated with, the prediction for the neighbor itself is retrieved from the cid
	
	
	@NotNull(message="predictionDashboard required")
	@JoinColumn(name="fk_predictions_dashboard_id")
	@ManyToOne
	public PredictionDashboard predictionDashboard;//the prediction that all n neighbors are associated with, the prediction for the neighbor itself is retrieved from the cid

	
	@OneToOne
	@JoinColumn(name="fk_dsstox_records_id")
	private DsstoxRecord dsstoxRecord;//temp storage for convenience (dont have in table because in different schema)- maybe move to this schema 

	@Column(name="dtxsid")
	private String dtxsid;//in OPERA neighbors can be a mixture of completely different substances separated by | 
	
	@Column(name="casrn")
	private  String casrn;//in OPERA neighbors can be a mixture of completely different substances separated by |

	@Column(name="inchi_key_qsar_ready")//these are probably qsar ready inchi keys because sometimes salts are stored in same neighbor as parent
	private String inchiKey;////used to checking match to OPERA values from sqlite database

	@Column(name="match_by")
	private String matchBy;////used to checking match to OPERA values from sqlite database
	
//	@NotNull(message="data_point required")
//	@JoinColumn(name="fk_data_point_id")
//	@ManyToOne
	//TODO do we want to store in a different table or just store directly in neighbor?
//	private DataPoint measuredPropertyNeighbor;//Can look up experimental value for the neighbor from the cid, dont need to stor

	
//	@NotNull(message="qsar_predicted_property required")
//	@JoinColumn(name="fk_qsar_predicted_property_id")
//	@ManyToOne
	//TODO do we want to store in a different table or just store directly in neighbor?
//	private QsarPredictedProperty qsarPredictedPropertyNeighbor;
	
	
//	@Column(name="dtxcid")
//	private String dtxcid;

	
	@Column(name="experimental_value")
	private Double experimentalValue;//in opera the neighbor can be a mixture so need to store the experimental value for the mixture
	
	@Column(name="predicted_value")
	private Double predictedValue;////used to checking match to OPERA values from sqlite database

	@Column(name="experimental_string")
	private String experimentalString;//in opera the neighbor can be a mixture so need to store the experimental value for the mixture
	
	@Column(name="predicted_string")
	private String predictedString;////used to checking match to OPERA values from sqlite database

	
	@Column(name="split_num")
	private Integer splitNum; //0 = training set, 1=test set


	@Column(name="similarity_coefficient")
	private Double similarityCoefficient; //0 = training set, 1=test set
	
	
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
	
	public QsarPredictedNeighbor() {}
	
	public QsarPredictedNeighbor(int neighborNumber,
			Double experimentalValue,String experimentalString,
			Double predictedValue,String predictedString,
			String dtxsid, String casrn, 
			String user,PredictionDashboard pd,
			Integer splitNum) {
		
		this.neighborNumber=neighborNumber;

		this.experimentalValue=experimentalValue;
		this.experimentalString=experimentalString;

		this.predictedValue=predictedValue;
		this.predictedString=predictedString;

		this.dtxsid=dtxsid;
		this.casrn=casrn;
		
		this.createdBy=user;
		this.updatedBy=user;
		this.predictionDashboard=pd;
		
		this.splitNum=splitNum;
		
	}
	
	
//	public QsarPredictedNeighbor(QsarPredictedNeighbor n) {//clone but cant use if splitting a neighbor by dtxsid or casrn
//		
//		this.neighborNumber=n.neighborNumber;
//
//		this.experimentalValue=n.experimentalValue;
//		this.predictedValue=n.predictedValue;
//
//		this.experimentalString=n.experimentalString;
//		this.predictedString=n.predictedString;
//
//		this.dtxsid=n.dtxsid;
//		this.casrn=n.casrn;
//		
//		this.createdBy=n.createdBy;
//		this.updatedBy=n.updatedBy;
//		
//		this.predictionDashboard=n.predictionDashboard;
//		this.splitNum=n.splitNum;
//		
//	}

	public Integer getNeighborNumber() {
		return neighborNumber;
	}

	public void setNeighborNumber(Integer neighborNumber) {
		this.neighborNumber = neighborNumber;
	}

	public PredictionDashboard getPredictionDashboard() {
		return predictionDashboard;
	}

	public void setPredictionDashboard(PredictionDashboard predictionDashboard) {
		this.predictionDashboard = predictionDashboard;
	}

	public String getDtxsid() {
		return dtxsid;
	}

	public void setDtxsid(String dtxsid) {
		this.dtxsid = dtxsid;
	}

	public String getCasrn() {
		return casrn;
	}

	public void setCasrn(String casrn) {
		this.casrn = casrn;
	}


	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getInchiKey() {
		return inchiKey;
	}

	public void setInchiKey(String inchiKey) {
		this.inchiKey = inchiKey;
	}

//	public String getPreferredName() {
//		return preferredName;
//	}
//
//	public void setPreferredName(String preferredName) {
//		this.preferredName = preferredName;
//	}

//	public Long getCid() {
//		return cid;
//	}
//
//	public void setCid(Long cid) {
//		this.cid = cid;
//	}

//	public String getDtxcid() {
//		return dtxcid;
//	}
//
//	public void setDtxcid(String dtxcid) {
//		this.dtxcid = dtxcid;
//	}

//	public Boolean isMolImagePNGAvailable() {
//		return molImagePNGAvailable;
//	}
//
//	public void setMolImagePNGAvailable(Boolean molImagePNGAvailable) {
//		this.molImagePNGAvailable = molImagePNGAvailable;
//	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
//	public QsarPredictedNeighbor(int neighborNumber,String dtxsid, QsarPredictedProperty qsarPredictedProperty,DataPoint measuredPropertyNeighbor, QsarPredictedProperty qsarPredictedPropertyNeighbor,String user) {
//		this.neighborNumber=neighborNumber;		
//		this.dtxsid=dtxsid;		
//		this.measuredPropertyNeighbor=measuredPropertyNeighbor;//Can look up experimental value for the neighbor from the cid, dont need to stor
//		this.qsarPredictedPropertyNeighbor=qsarPredictedPropertyNeighbor;
//		this.qsarPredictedProperty=qsarPredictedProperty;		
//		this.createdBy=user;
//		this.updatedBy=user;
//		
//	}

	/**
	 * Removes neighbors with no sid or casrn
	 * TODO are there ones with just inchikey?
	 * 
	 * @param neighbors
	 */
	public static void removeEmptyNeighbors(List<QsarPredictedNeighbor> neighbors) {
		for (int i=0;i<neighbors.size();i++) {
			QsarPredictedNeighbor n=neighbors.get(i);
			
			if(n.getCasrn()==null && n.getDtxsid()==null) {
				neighbors.remove(i--);
			}
		}
	}
	

	/**
	 * If a neighbor has multiple SIDs/casrns make them separate neighbors with same neighbor number
	 * 
	 * @param neighbors
	 * @param propertyName - used for print statements
	 * @param lookups
	 */
	public static List<QsarPredictedNeighbor> splitNeighbors(List<QsarPredictedNeighbor> neighbors, String propertyName,PredictionDashboardTableMaps lookups) {
		
		List<QsarPredictedNeighbor> neighborsNew=new ArrayList<QsarPredictedNeighbor>();
		
		for (int i=0;i<neighbors.size();i++) {
			QsarPredictedNeighbor n=neighbors.get(i);
			
			if(n.getDtxsid()!=null && n.getDtxsid().contains("|")) {
				handleMultipleSIDs(n,neighborsNew, lookups);
			} else if (n.getDtxsid()==null && n.getCasrn().contains("|")) {
				handleMultipleCAS(n,neighborsNew);
			} else {
				if(n.getDtxsid()!=null && (n.getDtxsid().equals("?") || n.getDtxsid().equals("NoID"))) {
					n.setDtxsid(null);
				}
				neighborsNew.add(n);
			}
			
		}
		return neighborsNew;
	}

	private static void handleMultipleCAS(QsarPredictedNeighbor n,List<QsarPredictedNeighbor> neighborsNew) {
		String [] casrnsArray=n.getCasrn().split("\\|");
		
		List<String>casrns=Arrays.asList(casrnsArray);
		
		for (int j=0; j<casrns.size(); j++) {
			String casrn=casrns.get(j).trim();
			
			QsarPredictedNeighbor nnew = new QsarPredictedNeighbor(n.getNeighborNumber(), 
					n.getExperimentalValue(),n.getPredictedString(), 
					n.getPredictedValue(),n.getPredictedString(),
					null, casrn, 
					n.getCreatedBy(), n.getPredictionDashboard(),
					n.getSplitNum());
			
//			nnew.setDsstoxRecord(n.getDsstoxRecord());
			

			neighborsNew.add(nnew);
		}
	}

	private static void handleMultipleSIDs(QsarPredictedNeighbor n, List<QsarPredictedNeighbor> neighborsNew, PredictionDashboardTableMaps lookups) {
		String [] sidsArray=n.getDtxsid().split("\\|");
		String [] casrnsArray=n.getCasrn().split("\\|");
		
		List<String>sids=new ArrayList<>();
		List<String>casrns=new ArrayList<>();
		
		for (String sid:sidsArray) {
			sid=sid.trim();
			if(sid.equals("?") || sid.equals("NoID") || sid.isEmpty()) continue;
			sids.add(sid);
		}
		
		for (String casrn:casrnsArray) {
			casrn=casrn.trim();
			if(casrn.equals("?") || casrn.equals("NoID") || casrn.isEmpty()) continue;
			casrns.add(casrn);
		}

		if (sids.size()!=casrns.size()) {//mismatch in length of arrays, look up CAS from SID in DB
			//						System.out.println(propertyName+"\t"+n.getPredictionDashboard().getDsstoxRecord().getDtxcid()+"\t"+n.getNeighborNumber()+"\tmismatch on sid and cas");

			for (int j=0;j<sids.size();j++) {
				String dtxsid=sids.get(j);
				String casrnDB=null;
				if (lookups.mapDsstoxRecordsBySID.get(dtxsid)!=null) {
					casrnDB=lookups.mapDsstoxRecordsBySID.get(dtxsid).getCasrn();
//							System.out.println("Found casrn from db:"+casrnDB);
				}
				
//				System.out.println("unequal array sizes:"+j+"\t"+dtxsid+"\t"+casrnDB);
				
				QsarPredictedNeighbor nnew = new QsarPredictedNeighbor(n.getNeighborNumber(), 
						n.getExperimentalValue(),n.getPredictedString(), 
						n.getPredictedValue(),n.getPredictedString(),
						dtxsid, casrnDB, 
						n.getCreatedBy(), n.getPredictionDashboard(),
						n.getSplitNum());
				
//				nnew.setDsstoxRecord(n.getDsstoxRecord());
//				System.out.println(n.getDtxsid()+"\t"+dtxsid);
				
				neighborsNew.add(nnew);
			}

		} else {
			for (int j=0;j<sids.size();j++) {//go in reverse order to preserve order
				String dtxsid=sids.get(j);
				String casrn=casrns.get(j);
//						System.out.println("equal array sizes:"+j+"\t"+dtxsid+"\t"+casrn);

				QsarPredictedNeighbor nnew = new QsarPredictedNeighbor(n.getNeighborNumber(), 
						n.getExperimentalValue(),n.getPredictedString(), 
						n.getPredictedValue(),n.getPredictedString(),
						dtxsid, casrn, 
						n.getCreatedBy(), n.getPredictionDashboard(),
						n.getSplitNum());

//				nnew.setDsstoxRecord(n.getDsstoxRecord());
				
//				System.out.println(j+"\t"+n.getPred());
				neighborsNew.add(nnew);
			}

		}
	}

	

	/**
	 * Attempts to add DSSTOX metadata to neighbors
	 * TODO might need to update the info later due to changes in DSSTOX
	 * 
	 * @param tableMaps
	 * @param propertyName - used for print statements
	 * @param neighbors
	 */
	public static void addNeighborMetadata(PredictionDashboardTableMaps tableMaps, String propertyName, List<QsarPredictedNeighbor> neighbors) {

//		String matchBy="";

		for (QsarPredictedNeighbor n:neighbors) {

			DsstoxRecord dr=null;
			
			n.setMatchBy("None");
			
//			if(n.getCasrn()!=null && n.getCasrn().equals("97964-54-0")) {
//				System.out.println("97964-54-0\t"+n.getDtxsid()+"\t"+propertyName);
//			}
			
			if(n.getDtxsid()==null || tableMaps.mapDsstoxRecordsBySID.get(n.getDtxsid())==null) {

				if(n.getCasrn()!=null) {

					if(tableMaps.mapDsstoxRecordsByCAS.get(n.getCasrn())!=null) {
						dr=tableMaps.mapDsstoxRecordsByCAS.get(n.getCasrn());

						if(dr.getCasrn().equals(dr.getCasrn())) {
							n.setMatchBy("CASRN");
						}

						if(dr.getOtherCasrns().size()>0) {
							for(DsstoxOtherCASRN oc:dr.getOtherCasrns()) {
								if(oc.getCasrn().equals(dr.getCasrn())) {
									n.setMatchBy("Other CASRN");
								}
							}
						}
					}
					
				} 

			} else {

				if(tableMaps.mapDsstoxRecordsBySID.get(n.getDtxsid())!=null) {
					dr=tableMaps.mapDsstoxRecordsBySID.get(n.getDtxsid());
					n.setMatchBy("DTXSID");
				} 
			}

			
			if (dr!=null) {
				if(dr.getDtxsid()!=null) n.setDtxsid(dr.getDtxsid());
				n.setDsstoxRecord(dr);
				
//				System.out.println("\t"+dr.getDtxsid()+"\t"+dr.getPreferredName());
				
			} 
			
//			if(n.getDtxsid()!=null && n.getDtxsid().equals("DTXSID0050479")) {
//				System.out.println("Found DTXSID0050479\t"+tableMaps.mapDsstoxRecordsBySID.get(n.getDtxsid()));
//			}
			

//			if(!n.getMatchBy().equals("DTXSID")) {
//				System.out.println(n.getMatchBy());
//			} 
			

		}//end loop over neighbors
	}
	
	String getKey() {
		return predictionDashboard.getDtxcid()+"\t"+neighborNumber+"\t"+ casrn +"\t"+ dtxsid;
	}
	
	
	public static void removeDuplicates(String propertyName, List<QsarPredictedNeighbor> neighbors) {
		
		HashSet<String>keys=new HashSet<String>();
		
		for(int i=0;i<neighbors.size();i++) {
			QsarPredictedNeighbor qpn=neighbors.get(i);
			if(keys.contains(qpn.getKey())) {
//				System.out.println("Duplicate neighbor:"+propertyName+"\t"+qpn.getKey());
				neighbors.remove(i--);
			} else {
				keys.add(qpn.getKey());
			}
		}
	}
	

	public DsstoxRecord getDsstoxRecord() {
		return dsstoxRecord;
	}

	public void setDsstoxRecord(DsstoxRecord dsstoxRecord) {
		this.dsstoxRecord = dsstoxRecord;
	}

	public String getMatchBy() {
		return matchBy;
	}

	public void setMatchBy(String matchBy) {
		this.matchBy = matchBy;
	}

	public Double getExperimentalValue() {
		return experimentalValue;
	}

	public void setExperimentalValue(Double experimentalValue) {
		this.experimentalValue = experimentalValue;
	}

	public Double getPredictedValue() {
		return predictedValue;
	}

	public void setPredictedValue(Double predictedValue) {
		this.predictedValue = predictedValue;
	}

	public String getExperimentalString() {
		return experimentalString;
	}

	public void setExperimentalString(String experimentalString) {
		this.experimentalString = experimentalString;
	}

	public String getPredictedString() {
		return predictedString;
	}

	public void setPredictedString(String predictedString) {
		this.predictedString = predictedString;
	}

	public Integer getSplitNum() {
		return splitNum;
	}

	public void setSplitNum(Integer splitNum) {
		this.splitNum = splitNum;
	}

	public Double getSimilarityCoefficient() {
		return similarityCoefficient;
	}

	public void setSimilarityCoefficient(Double similarityCoefficient) {
		this.similarityCoefficient = similarityCoefficient;
	}

//	public Boolean getMolImagePNGAvailable() {
//		return molImagePNGAvailable;
//	}
	
}

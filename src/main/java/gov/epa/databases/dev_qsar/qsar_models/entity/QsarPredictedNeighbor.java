package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;



import gov.epa.run_from_java.scripts.OPERA.OPERA_lookups;

@Entity
@Table(name="qsar_predicted_neighbors", uniqueConstraints={@UniqueConstraint(columnNames = {"fk_predictions_dashboard_id", "neighbor_number","casrn","dtxsid"})})
public class QsarPredictedNeighbor {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	
	@NotNull(message="Neighbor number required")
	@Column(name="neighbor_number")
	private Integer neighborNumber;


	@Column(name="preferred_name")
	private String preferredName;

	@Column(name="cid")
	private Long cid;

	@Column(name="mol_image_png_available")
	private Boolean molImagePNGAvailable;

	
//	@NotNull(message="QsarPredictedProperty required")
//	@JoinColumn(name="fk_qsar_predicted_properties_id")
//	@ManyToOne
//	public QsarPredictedProperty QsarPredictedProperty;//the prediction that all n neighbors are associated with, the prediction for the neighbor itself is retrieved from the cid
	
	
	@NotNull(message="predictionDashboard required")
	@JoinColumn(name="fk_predictions_dashboard_id")
	@ManyToOne
	public PredictionDashboard predictionDashboard;//the prediction that all n neighbors are associated with, the prediction for the neighbor itself is retrieved from the cid

	

	
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
	
	
	@Column(name="dtxcid")
	private String dtxcid;

	@Column(name="dtxsid")
	private String dtxsid;//in OPERA neighbors can be a mixture of completely different substances separated by | 
	
	@Column(name="casrn")
	private  String casrn;//in OPERA neighbors can be a mixture of completely different substances separated by |
	
	@Column(name="exp")
	private String exp;//in opera the neighbor can be a mixture so need to store the experimental value for the mixture
	
	@Column(name="pred")
	private String pred;////used to checking match to OPERA values from sqlite database
		

	@Column(name="inchiKey")
	private String inchiKey;////used to checking match to OPERA values from sqlite database

	
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
	
	public QsarPredictedNeighbor() {}
	
	public QsarPredictedNeighbor(int neighborNumber,String casrn,String dtxsid,String exp,String pred, String user,PredictionDashboard pd) {
		this.neighborNumber=neighborNumber;
		this.casrn=casrn;
		this.dtxsid=dtxsid;
		this.exp=exp;
		this.pred=pred;
		this.createdBy=user;
		this.updatedBy=user;
		this.predictionDashboard=pd;
	}

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

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getPred() {
		return pred;
	}

	public void setPred(String pred) {
		this.pred = pred;
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

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public String getDtxcid() {
		return dtxcid;
	}

	public void setDtxcid(String dtxcid) {
		this.dtxcid = dtxcid;
	}

	public Boolean isMolImagePNGAvailable() {
		return molImagePNGAvailable;
	}

	public void setMolImagePNGAvailable(Boolean molImagePNGAvailable) {
		this.molImagePNGAvailable = molImagePNGAvailable;
	}

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

	public static void removeEmptyNeighbors(List<QsarPredictedNeighbor> neighbors) {
		for (int i=0;i<neighbors.size();i++) {
			QsarPredictedNeighbor n=neighbors.get(i);
			if(n.getCasrn()==null && n.getDtxsid()==null) {
				neighbors.remove(i--);
			}
		}
	}
	

	public static void cloneNeighbors(List<QsarPredictedNeighbor> neighbors, String propertyAbbrevOPERA,OPERA_lookups lookups) {
		
		for (int i=0;i<neighbors.size();i++) {
		
			QsarPredictedNeighbor n=neighbors.get(i);
			
			if(n.getDtxsid()!=null && n.getDtxsid().contains("|")) {

				String [] sids=n.getDtxsid().split("\\|");
				String [] casrns=n.getCasrn().split("\\|");

				if (sids.length!=casrns.length) {//mismatch in length of arrays, look up CAS from SID in DB
					//						System.out.println(propertyAbbrevOPERA+"\t"+n.getPredictionDashboard().getDsstoxRecord().getDtxcid()+"\t"+n.getNeighborNumber()+"\tmismatch on sid and cas");

					neighbors.remove(i);
					for (int j=sids.length-1;j>=0;j--) {//go in reverse order to preserve order
						String dtxsid=sids[j].trim();
						String casrnDB=null;
						if (lookups.mapDsstoxRecordsBySID.get(dtxsid)!=null) {
							casrnDB=lookups.mapDsstoxRecordsBySID.get(dtxsid).getCasrn();
//							System.out.println("Found casrn from db:"+casrnDB);
						}
						QsarPredictedNeighbor nnew=new QsarPredictedNeighbor(n.getNeighborNumber(),casrnDB,dtxsid,n.getExp(),n.getPred(), n.getCreatedBy(),n.getPredictionDashboard());
						neighbors.add(i,nnew);
					}

				} else {
					neighbors.remove(i);
					for (int j=sids.length-1;j>=0;j--) {//go in reverse order to preserve order
						String dtxsid=sids[j].trim();
						String casrn=casrns[j].trim();

						QsarPredictedNeighbor nnew=new QsarPredictedNeighbor(n.getNeighborNumber(),casrn,dtxsid,n.getExp(),n.getPred(), n.getCreatedBy(),n.getPredictionDashboard());
						neighbors.add(i,nnew);
					}

				}


			} else if (n.getDtxsid()==null && n.getCasrn().contains("|")) {
				String [] casrns=n.getCasrn().split("\\|");
				neighbors.remove(i);
				for (int j=casrns.length-1;j>=0;j--) {//go in reverse order to preserve order
					String casrn=casrns[j].trim();
					QsarPredictedNeighbor nnew=new QsarPredictedNeighbor(n.getNeighborNumber(),casrn,null,n.getExp(),n.getPred(), n.getCreatedBy(),n.getPredictionDashboard());
					neighbors.add(i,nnew);
				}
			} else {
				//Do nothing
			}
			
			
		}
		
	}

	

	public static void addNeighborMetadata(OPERA_lookups lookups, String propertyAbbrevOPERA, List<QsarPredictedNeighbor> neighbors) {
		
		
		for (QsarPredictedNeighbor n:neighbors) {

			DsstoxRecord dr=null;
			
			if(n.getDtxsid()==null) {
				if(lookups.mapDsstoxRecordsByCAS.get(n.getCasrn())!=null) {
					dr=lookups.mapDsstoxRecordsByCAS.get(n.getCasrn());
//					System.out.println("Retrieved by CAS\t"+n.getCasrn());
					
//					System.out.println(propertyAbbrevOPERA+"\t"+n.getCasrn()+"\t"+n.getDtxsid()+"\tCAS mapping");
				} else {
					if(lookups.mapDsstoxRecordsByOtherCAS.get(n.getCasrn())!=null) {
						dr=lookups.mapDsstoxRecordsByOtherCAS.get(n.getCasrn());
//						System.out.println("Retrieved by other CAS\t"+n.getCasrn());

//						System.out.println(propertyAbbrevOPERA+"\t"+n.getCasrn()+"\t"+n.getDtxsid()+"\tOtherCAS mapping");
					} else {
						
						if(lookups.mapDsstoxRecordsByCAS_NoCompound.get(n.getCasrn())!=null) {
							dr=lookups.mapDsstoxRecordsByCAS_NoCompound.get(n.getCasrn());
//							System.out.println("Retrieved by CAS No compound\t"+n.getCasrn());
						} else {
							if (!n.getCasrn().contains("CHEMBL") && !n.getCasrn().contains("SRC")) {
								System.out.println("cant get sid from "+n.getCasrn());
							}
						}
					}
				}
				
			} else {
				
				if(lookups.mapDsstoxRecordsBySID.get(n.getDtxsid())!=null) {
					dr=lookups.mapDsstoxRecordsBySID.get(n.getDtxsid());
//					System.out.println("Retrieved by SID\t"+n.getDtxsid());
				} else if (lookups.mapDsstoxRecordsBySID_NoCompound.get(n.getDtxsid())!=null) {
					dr=lookups.mapDsstoxRecordsBySID_NoCompound.get(n.getDtxsid());
//					System.out.println("Retrieved by SID no compound\t"+n.getDtxsid());
				} else {
					System.out.println("Couldnt retrieve by sid\t"+n.getDtxsid());
				}
				
			}
			
			if (dr!=null) {
				if(dr.getDtxsid()!=null) n.setDtxsid(dr.getDtxsid());
				n.setPreferredName(dr.getPreferredName());
				if(dr.getCid()!=null) n.setCid(dr.getCid());
				if(dr.getDtxcid()!=null) n.setDtxcid(dr.getDtxcid());
				n.setMolImagePNGAvailable(dr.isMolImagePNGAvailable());
			} else {
				n.setMolImagePNGAvailable(false);
			}
			
			
		}//end loop over neighbors
	}
	
}

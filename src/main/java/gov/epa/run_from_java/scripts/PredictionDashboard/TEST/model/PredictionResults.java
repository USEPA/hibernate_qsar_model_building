package gov.epa.run_from_java.scripts.PredictionDashboard.TEST.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PredictionResults {

	private String version;
    private String CAS;//test chemical CAS (chemical being predicted)
    private String name;//test chemical name (chemical being predicted)
    private String DTXSID;//test chemical CAS (chemical being predicted)
	private String DTXCID;//test chemical CAS (chemical being predicted)
    private String Smiles;//test chemical CAS (chemical being predicted)
    private String imageURL;//can be used for test chemical image for consensus table and similar chemicals tables
    private String error;
    private Double molWeight;
    private String inchiKey;

    private String method;//method used to calculate endpoint
    private String endpoint; //endpoint being calculated
    private boolean isBinaryEndpoint;//whether endpoint is binary (i.e. mutagenicity)
    private boolean isLogMolarEndpoint;// whether endpoint is log molar (i.e. fathead minnow LC50)


	// Main results table:
	private PredictionResultsPrimaryTable predictionResultsPrimaryTable = new PredictionResultsPrimaryTable();

	//Consensus predictions table:
	private IndividualPredictionsForConsensus individualPredictionsForConsensus;
    
	//Similar chemicals tables for test and training sets:
	private Vector<SimilarChemicals> similarChemicals = new Vector<>();

    private ClusterTable clusterTable;
    private ClusterTable invalidClusterTable;
    
	//Consensus predictions table:
	private MOATable moaTable = new MOATable();

	private int imgSize;//size of image to display
    private String webImagePathByCID;//path to get images on Chemistry Dashboard
    private String webImagePathBySID;//path to get images on Chemistry Dashboard
	private String webPathDashboardPage;//path to get property prediction page on Chemistry Dashboard 
    private double SCmin;//minimum similar coefficient for chemicals to display in similar chemicals table
    
	private String reportBase;
    private boolean createDetailedReport;
	
    private HashMap<String, Double> hmStats;

	
	public String getReportBase() {
		return reportBase;
	}

	public void setReportBase(String reportBase) {
		this.reportBase = reportBase;
	}

	public ClusterTable getClusterTable() {
		return clusterTable;
	}

	public void setClusterTable(ClusterTable clusterTable) {
		this.clusterTable = clusterTable;
	}

	public ClusterTable getInvalidClusterTable() {
		return invalidClusterTable;
	}

	public void setInvalidClusterTable(ClusterTable invalidClusterTable) {
		this.invalidClusterTable = invalidClusterTable;
	}

	public boolean isCreateDetailedReport() {
		return createDetailedReport;
	}

	public void setCreateDetailedReport(boolean createDetailedReport) {
		this.createDetailedReport = createDetailedReport;
	}


	public MOATable getMoaTable() {
		return moaTable;
	}

	public void setMoaTable(MOATable moaTable) {
		this.moaTable = moaTable;
	}

    public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public boolean isLogMolarEndpoint() {
		return isLogMolarEndpoint;
	}

	public void setLogMolarEndpoint(boolean isLogMolarEndpoint) {
		this.isLogMolarEndpoint = isLogMolarEndpoint;
	}


    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getCAS() {
        return CAS;
    }

    @JsonProperty("CAS")
    public void setCAS(String CAS) {
        this.CAS = CAS;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Vector<SimilarChemicals> getSimilarChemicals() {
        return similarChemicals;
    }

    public void setSimilarChemicals(Vector<SimilarChemicals> similarChemicals) {
        this.similarChemicals = similarChemicals;
    }

    public IndividualPredictionsForConsensus getIndividualPredictionsForConsensus() {
        return individualPredictionsForConsensus;
    }

    public void setIndividualPredictionsForConsensus(IndividualPredictionsForConsensus individualPredictionsForConsensus) {
        this.individualPredictionsForConsensus = individualPredictionsForConsensus;
    }

    public double getSCmin() {
        return SCmin;
    }

    @JsonProperty("SCmin")
    public void setSCmin(double SCmin) {
        this.SCmin = SCmin;
    }

    public PredictionResultsPrimaryTable getPredictionResultsPrimaryTable() {
        return predictionResultsPrimaryTable;
    }
    
//    public String getModelUnits () {
//    	
//    	String units=null;
//
//    	if(TESTConstants.isBinary(endpoint)) {
//    		units="binary";
//    	} else if(TESTConstants.isLogMolar(endpoint)) {
//			units=predictionResultsPrimaryTable.getMolarLogUnits();
//    	} else {
//			units=predictionResultsPrimaryTable.getMassUnits();
//		}
//    	
//    	return units;
//    }
    
    
    
    public Double getExpValueInModelUnits() {
    	
    	if(TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
    		return getPredictionResultsPrimaryTable().getExpToxValue();
    	} else {
    		return getPredictionResultsPrimaryTable().getExpToxValMass();
    	}
    }
    
    public Double getPredValueInModelUnits() {
    	if(TESTConstants.isLogMolar(endpoint) || TESTConstants.isBinary(endpoint)) {
    		return getPredictionResultsPrimaryTable().getPredToxValue();
    	} else {
    		return getPredictionResultsPrimaryTable().getPredToxValMass();
    	}
    }

    public void setPredictionResultsPrimaryTable(PredictionResultsPrimaryTable predictionResultsPrimaryTable) {
        this.predictionResultsPrimaryTable = predictionResultsPrimaryTable;
    }

    public boolean isBinaryEndpoint() {
        return isBinaryEndpoint;
    }

    @JsonProperty("isBinaryEndpoint")
    public void setBinaryEndpoint(boolean isBinaryEndpoint) {
        this.isBinaryEndpoint = isBinaryEndpoint;
    }

    public int getImgSize() {
        return imgSize;
    }

    public void setImgSize(int imgSize) {
        this.imgSize = imgSize;
    }

    public String getWebImagePathByCID() {
        return webImagePathByCID;
    }

    public void setWebImagePathByCID(String webPath) {
        this.webImagePathByCID = webPath;
    }

    public String getWebPathDashboardPage() {
        return webPathDashboardPage;
    }

    public void setWebPathDashboardPage(String webPathDashboardPage) {
        this.webPathDashboardPage = webPathDashboardPage;
    }
    
    public String getSmiles() {
		return Smiles;
	}

	public void setSmiles(String smiles) {
		Smiles = smiles;
	}

	public String getDTXSID() {
		return DTXSID;
	}

	public void setDTXSID(String dTXSID) {
		DTXSID = dTXSID;
	}

	public String getDTXCID() {
		return DTXCID;
	}

	public void setDTXCID(String dTXCID) {
		DTXCID = dTXCID;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

    public String getWebImagePathBySID() {
		return webImagePathBySID;
	}

	public void setWebImagePathBySID(String webImagePathBySID) {
		this.webImagePathBySID = webImagePathBySID;
	}

    public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}


	public HashMap<String, Double> getHmStats() {
		return hmStats;
	}

	public void setHmStats(HashMap<String, Double> hmStats) {
		this.hmStats = hmStats;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getMolWeight() {
		return molWeight;
	}

	public void setMolWeight(Double molWeight) {
		this.molWeight = molWeight;
	}

	public String getInchiKey() {
		return inchiKey;
	}

	public void setInchiKey(String inchiKey) {
		this.inchiKey = inchiKey;
	}

	
	
	
	public static List<PredictionResults> getResultsFromJson(String json) {
		Type type = new TypeToken<List<PredictionResults>>(){}.getType();
		Gson gson = new Gson();
		return  gson.fromJson(json, type);
	}
	
	
	public static List<PredictionResults> getPredictionsFromTESTAPI(String identifier,String server, int port) {

		String url=server+":"+port+"/predictDB";
		
		HttpResponse<String> responseGet = Unirest.get(url)
				.header("Content-Type", "application/json")
				.queryString("identifier",identifier)
				.queryString("format","json")
				.asString();
				
		String json=responseGet.getBody().toString();
		
		if(json.contains("not found")) {
			return null;
		}
		
//		System.out.println(json);
		
		List<PredictionResults>listResults=getResultsFromJson(json);
		return listResults;
		
	}

}

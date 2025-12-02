package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run;

/**
* @author TMARTI02
*/

import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionReport;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.EpisuiteReport;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.HTMLReportCreatorEPISUITE;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.PropertyResult2;

//import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
import java.util.ArrayList;
import java.util.Hashtable;


public class EpisuiteResults {

	public String error;
	
	public Parameters parameters;
	public ChemicalProperties chemicalProperties;
	
	public PropertyResult ecosar;
	
	public PropertyResult logKow;
	
	public PropertyResult meltingPoint;
	public PropertyResult boilingPoint;
	public PropertyResult waterSolubilityFromLogKow;
	public PropertyResult waterSolubilityFromWaterNt;
	public PropertyResult logKoa;

	public PropertyResult2 vaporPressure;//has multiple models
	public PropertyResult2 henrysLawConstant;//has multiple models

	public BiodegradationRate biodegradationRate;
	public PropertyResult hydrocarbonBiodegradationRate;
	public PropertyResult aerosolAdsorptionFraction;
	public AtmosphericHalfLife atmosphericHalfLife;
	
	public PropertyResult logKoc;

	public Hydrolysis hydrolysis;
	
	public Bioconcentration bioconcentration;
	
	public WaterVolatilization waterVolatilization;
	public SewageTreatmentModel sewageTreatmentModel;
	public FugacityModel fugacityModel;
	public DermalPermeability dermalPermeability;
	public ArrayList<Object> logKowAnalogs;
	public ArrayList<Object> analogs;
	
	public String smiles;
	public String canonQsarSmiles;
	public String dtxsid;
	public String dtxcid;
	
	
	private transient static Gson gson=new Gson();
	
	public static EpisuiteResults getResults(String json) throws JsonSyntaxException {
		return Utilities.gson.fromJson(json, EpisuiteResults.class);
	}
	
	
	public String fixNotes(String notes) {
		if(notes==null) return notes;
		
		String [] lines=notes.split("\n");

		notes="";

		for (int i=0;i<lines.length;i++) {
			String line=lines[i];
			notes+=line.trim();
			if(i<lines.length-1) {
				notes+=" ";
			}
		}		

		return notes;
	}
	
	
	public void setExpPred(PredictionDashboard pd,Dataset dataset,Boolean storeReports) {

		gov.epa.databases.dev_qsar.qsar_models.entity.Model modelPD=pd.getModel();
		
		PropertyResult2 pr2=null;
		
		
		if(this.error!=null) {
			//					String errorSmiles=results.error.substring(results.error.indexOf(":")+2,results.error.length());//there's a mistake in their output because it always outputs [N-]1[N-]C(=S)SC1=S.[K+].[K+] in the error 

			if(error.contains("Log Kow could not be determined")) {
				error="Log Kow could not be determined";
			}
			pd.setPredictionError(error);
			pr2=new PropertyResult2();
			pr2.estimatedValue=new EstimatedValue2();
			pr2.estimatedValue.error=pd.getPredictionError();
		
		} else if (modelPD.getName().equals("EPISUITE_AOH")) {
			
			pr2=new PropertyResult2();
			pr2.parameters=this.atmosphericHalfLife.parameters;
			pr2.estimatedValue=new EstimatedValue2(atmosphericHalfLife.estimatedHydroxylRadicalReactionRateConstant);			
			pr2.experimentalValues=this.atmosphericHalfLife.experimentalHydroxylRadicalReactionRateConstantValues;
			
//			addSelectedValue(pr);
			pd.setPredictionValue(pr2.estimatedValue.value);
			
			setAD(modelPD, pr2, atmosphericHalfLife.estimatedValue.model.notes);

		} else if(modelPD.getName().equals("EPISUITE_KOC")) {
			
			PropertyResult pr=this.logKoc;
			pd.setPredictionValue(Math.pow(10.0,pr.estimatedValue.value));
			
			if(pr.selectedValue!=null && pr.selectedValue.valueType.equals("EXPERIMENTAL")) {
				pd.setExperimentalValue(Math.pow(10.0, pr.selectedValue.value));
			}
			
			pr2=new PropertyResult2(pr);
			
			setAD(modelPD,pr2,logKoc.estimatedValue.model.notes);
						
			pr.estimatedValue.model.output=null;//TODO check AD first
			
		} else if(modelPD.getName().equals("EPISUITE_BAF_UPPER_TROPHIC")) {
			ArnotGobasBcfBafEstimate estimate=bioconcentration.arnotGobasBcfBafEstimates.get(0);
			pd.setPredictionValue(estimate.bioaccumulationFactor);
//			pd.setPredictionValue(this.bioconcentration.bioaccumulationFactor);//should be same within round off
			
			pr2=new PropertyResult2();
			pr2.estimatedValue=new EstimatedValue2();
			pr2.estimatedValue.value=estimate.bioaccumulationFactor;
			pr2.estimatedValue.units=estimate.unit;
						
			setAD(modelPD, pr2, bioconcentration.notes);

			
		} else if(modelPD.getName().equals("EPISUITE_BCF_UPPER_TROPHIC")) {
			ArnotGobasBcfBafEstimate estimate=bioconcentration.arnotGobasBcfBafEstimates.get(0);
			pd.setPredictionValue(estimate.bioconcentrationFactor);
			
			pr2=new PropertyResult2();
			pr2.estimatedValue=new EstimatedValue2();
			pr2.estimatedValue.value=estimate.bioconcentrationFactor;
			pr2.estimatedValue.units=estimate.unit;
			
			setAD(modelPD, pr2, bioconcentration.notes);
			
		} else if(modelPD.getName().equals("EPISUITE_BCF")) {
			pr2=handleEPISUITE_BCF(pd);
		} else if (modelPD.getName().equals("EPISUITE_BIOTRANS_HL")) {
			pr2=handleEPISUITE_BIOTRANS_HL(pd);			
		} else if (modelPD.getName().equals("EPISUITE_BioHCWIN")) {
			pr2=setValuesNoUnitConversion(pd, this.hydrocarbonBiodegradationRate);						
			setAD(modelPD,pr2,hydrocarbonBiodegradationRate.estimatedValue.model.notes);			
		} else if (modelPD.getName().contains("EPISUITE_BIOWIN")) {
			pr2=handleEPISUITE_BIOWIN(pd, modelPD);
		} else if (modelPD.getName().equals("EPISUITE_RBIODEG")) {
			pr2=handleEPISUITE_RBIODEG(pd);
		} else if (modelPD.getName().equals("EPISUITE_WS_KOW")) {			
			pr2=handleEPISUITE_WS(pd, waterSolubilityFromLogKow);
		} else if (modelPD.getName().equals("EPISUITE_WATERNT")) {			
			pr2=handleEPISUITE_WS(pd, waterSolubilityFromWaterNt);
		} else if (modelPD.getName().equals("EPISUITE_HLC")) {
			pr2=setValuesNoUnitConversion(pd, this.henrysLawConstant);
		} else if (modelPD.getName().equals("EPISUITE_VP")) {
			pr2=setValuesNoUnitConversion(pd, this.vaporPressure);
		} else if (modelPD.getName().equals("EPISUITE_LOGKOA")) {
			pr2=setValuesNoUnitConversion(pd, this.logKoa);
		} else if (modelPD.getName().equals("EPISUITE_LOGP")) {						
			pr2=setValuesNoUnitConversion(pd, this.logKow);
		} else if (modelPD.getName().equals("EPISUITE_MP")) {						
			pr2=setValuesNoUnitConversion(pd, this.meltingPoint);
		} else if (modelPD.getName().equals("EPISUITE_BP")) {						
			pr2=setValuesNoUnitConversion(pd, this.boilingPoint);
		} else {
			System.out.println("TODO in getResults implement "+modelPD.getName());
		}
		
		
		if(pr2==null) {
			System.out.println(modelPD.getName()+"\tNo pr2");
		} else {
			if(this.error==null) addSelectedValue(pr2);	
			
//			addPropertyInfo(pd, dataset, pr2);			
//			pr2.chemicalIdentifiers=new ChemicalIdentifiers(pd.getDsstoxRecord());
//			pd.setPredictionReport(getPredictionReport(pd, pr2));
			
			if(storeReports) {
				EpisuiteReport er=new EpisuiteReport(pr2, pd, dataset);
				pd.setPredictionReport(getPredictionReport(pd, er));
			}
			
		}
		

	}

		
	private void setAD(gov.epa.databases.dev_qsar.qsar_models.entity.Model modelPD, PropertyResult2 pr2, String notes) {

		if(notes==null || notes.isBlank()) return;
		
		pr2.estimatedValue.notes=fixNotes(notes);
			
		if(pr2.estimatedValue.notes.contains("inorganic compounds are outside the estimation domain")) {
			pr2.estimatedValue.applicabilityDomainConclusion="Inorganic compounds are outside the estimation domain";
		} else if(pr2.estimatedValue.notes.contains("Contains atoms other than C, H or S (-S-)")) {
			pr2.estimatedValue.applicabilityDomainConclusion="Chemicals containing atoms other than C, H or S (-S-) are outside the estimation domain";
		} else if(pr2.estimatedValue.notes.contains("outside the estimation domain")) {
			System.out.println(modelPD.getName()+ ", Other AD issue");
		}
		
	}


//	private void addPropertyInfo(PredictionDashboard pd, Dataset dataset,
//			PropertyResult2 pr2) {
//		pr2.propertyName=dataset.getProperty().getName();
//		pr2.propertyDescription=dataset.getProperty().getDescription();
//	}

	private PropertyResult2 handleEPISUITE_WS(PredictionDashboard pd, PropertyResult pr) {
		
		
		PropertyResult2 pr2=new PropertyResult2(pr);
		
		if(chemicalProperties.molecularWeight==null) {
			
			if(pd.getDsstoxRecord().getMolWeight()!=null) {
				chemicalProperties.molecularWeight=pd.getDsstoxRecord().getMolWeight();
			} else {
				System.out.println("Missing MW for "+pd.getDsstoxRecord().getDtxsid());
				return pr2;
			}
		}
		double WS_MG_L=pr2.estimatedValue.value;
		double molWeight=this.chemicalProperties.molecularWeight;
		double WS_MOL_L=WS_MG_L/1000.0/molWeight;

		//					System.out.println(WS_MG_L+"\t"+molWeight+"\t"+WS_MOL_L);
		pd.setPredictionValue(WS_MOL_L);

		if(pr2.selectedValue!=null && pr2.selectedValue.valueType.equals("EXPERIMENTAL")) {
			double WS_MG_L_exp=this.waterSolubilityFromLogKow.selectedValue.value;
			double WS_MOL_L_exp=WS_MG_L_exp/1000.0/molWeight;
			pd.setExperimentalValue(WS_MOL_L_exp);
		}

		Model model=pr2.estimatedValue.model.get(0);
		
		String output=model.output.toLowerCase();

		setAD(pd.getModel(), pr2, output);
		model.output=null;
		
		return pr2;
	}

	
	/**
	 * The criteria for the YES or NO prediction are as follows:  If the Biowin3
	 * (ultimate survey model) result is "weeks" or faster (i.e. days, days to
	 * weeks, or weeks) AND the Biowin5 (MITI linear model) probability is >= 0.5,
	 * then the prediction is YES (readily biodegradable).  If this condition is not
	 * satisfied, the prediction is NO (not readily biodegradable).
	 */
	private PropertyResult2 handleEPISUITE_RBIODEG(PredictionDashboard pd) {
		
		PropertyResult2 pr=new PropertyResult2();
		
		pr.estimatedValue=new EstimatedValue2();
		pr.estimatedValue.model=new ArrayList<>();
		

		Model BIOWIN3=this.biodegradationRate.models.get(3-1);
		Model BIOWIN5=this.biodegradationRate.models.get(5-1);
		
		pr.estimatedValue.model.add(BIOWIN3);
		pr.estimatedValue.model.add(BIOWIN5);

		
		if(BIOWIN3.value>2.75 && BIOWIN5.value>=0.5) {
			pd.setPredictionString("Readily Degradable");
		} else {
			pd.setPredictionString("NOT Readily Degradable");
		}
		
		pr.estimatedValue.valueString=pd.getPredictionString();
		
		setAD(pd.getModel(), pr, biodegradationRate.notes);
				
		return pr;
	}

	private PropertyResult2 handleEPISUITE_BIOWIN(PredictionDashboard pd,
			gov.epa.databases.dev_qsar.qsar_models.entity.Model model) {
		int modelNum=Integer.parseInt(model.getName().replace("EPISUITE_BIOWIN", ""));
		Model modelBio=this.biodegradationRate.models.get(modelNum-1);
		pd.setPredictionValue(modelBio.value);

		if(modelNum==1 || modelNum==2 || modelNum==7) {
			if(modelBio.value>=0.5) {
				pd.setPredictionString("Biodegrades Fast");
			} else {
				pd.setPredictionString("Does NOT Biodegrade Fast");
			}
		} else if(modelNum==3 || modelNum==4) {
			//see section 7.2.2 in BioWin user's guide
			if(modelBio.value>4.75) {
				pd.setPredictionString("Hours");
			} else if (modelBio.value>4.25 && modelBio.value<=4.75) {
				pd.setPredictionString("Hours-Days");
			} else if (modelBio.value>3.75 && modelBio.value<=4.25) {
				pd.setPredictionString("Days");
			} else if (modelBio.value>3.25 && modelBio.value<=3.75) {
				pd.setPredictionString("Days-Weeks");
			} else if (modelBio.value>2.75 && modelBio.value<=3.25) {
				pd.setPredictionString("Weeks");
			} else if (modelBio.value>2.25 && modelBio.value<=2.75) {
				pd.setPredictionString("Weeks-Months");
			} else if (modelBio.value>1.75 && modelBio.value<=2.25) {
				pd.setPredictionString("Months");
			} else {
				pd.setPredictionString("Recalcitrant");
			}
		} else if(modelNum==5 || modelNum==6) {
			if(modelBio.value>=0.5) {
				pd.setPredictionString("Readily Degradable");
			} else {
				pd.setPredictionString("NOT Readily Degradable");
			}
		} 
		
		
		PropertyResult2 pr=new PropertyResult2();
		pr.parameters=biodegradationRate.parameters;
		pr.estimatedValue=new EstimatedValue2();
		pr.estimatedValue.value=modelBio.value;			
		pr.estimatedValue.valueString=pd.getPredictionString();
		pr.estimatedValue.units="Dimensionless";
		pr.estimatedValue.valueType="ESTIMATED";
			 
		pr.estimatedValue.model=new ArrayList<>();
		pr.estimatedValue.model.add(modelBio);
		
		
		setAD(model, pr, biodegradationRate.notes);

		return pr;
		
	}

	private PropertyResult2 handleEPISUITE_BCF(PredictionDashboard pd) {
		
		pd.setPredictionValue(this.bioconcentration.bioconcentrationFactor);
		
		PropertyResult2 pr=new PropertyResult2();
		pr.parameters=bioconcentration.parameters;
		
		pr.estimatedValue=new EstimatedValue2();
		pr.estimatedValue.value=bioconcentration.bioconcentrationFactor;//in L/kg
		pr.estimatedValue.units="L/kg wet-wt";
		pr.estimatedValue.valueType="ESTIMATED";
			 		
		if(bioconcentration.bioconcentrationFactors!=null) {
			Model model=new Model();
			model.factors=bioconcentration.bioconcentrationFactors;
			model.equation=bioconcentration.biocontrationFactorEquation;
			pr.estimatedValue.model=new ArrayList<>();
			pr.estimatedValue.model.add(model);
		}
								
//		System.out.println("exp bcf="+bioconcentration.experimentalBioconcentrationFactor);
		
		if(bioconcentration.experimentalBioconcentrationFactor!=null) {
			
			ExperimentalValue expValue=new ExperimentalValue();

			//Note: bioconcentration.experimentalBioconcentrationFactor has log(L/kg) units which is inconsistent labeling in their json output!
			
			expValue.value=Math.pow(10.0, this.bioconcentration.experimentalBioconcentrationFactor);
			expValue.units=pr.estimatedValue.units;
			pr.experimentalValues=new ArrayList<>();
			pr.experimentalValues.add(expValue);
			
			pd.setExperimentalValue(expValue.value);//use the converted value to get it in L/kg
			
//			System.out.println("pr.experimentalValues.size()="+pr.experimentalValues.size());
		}
		
		setAD(pd.getModel(), pr, bioconcentration.notes);
		
//			pr.biocontrationFactorEquationSum=bioconcentration.biocontrationFactorEquationSum;
//		addSelectedValue(pr);
		return pr;
	}
	
	void addSelectedValue(PropertyResult2 pr) {
				
		if(pr.selectedValue!=null) return;
		
		if(pr.experimentalValues!=null && pr.experimentalValues.size()>0) {
			pr.selectedValue=new Parameter();
			ExperimentalValue expValue=pr.experimentalValues.get(0);			
			pr.selectedValue.value=expValue.value;
			pr.selectedValue.units=pr.estimatedValue.units;
			pr.selectedValue.valueType=expValue.valueType;

		} else {
			pr.selectedValue=new Parameter();			
			pr.selectedValue.value=pr.estimatedValue.value;
			pr.selectedValue.valueString=pr.estimatedValue.valueString;
			pr.selectedValue.units=pr.estimatedValue.units;
			pr.selectedValue.valueType="ESTIMATED";

		}
			
	}
	

	
	

	private PropertyResult2 handleEPISUITE_BIOTRANS_HL(PredictionDashboard pd) {
		pd.setPredictionValue(bioconcentration.biotransformationHalfLife);
		
		PropertyResult2 pr=new PropertyResult2();
		pr.parameters=bioconcentration.parameters;
		
		pr.estimatedValue=new EstimatedValue2();
		pr.estimatedValue.value=bioconcentration.biotransformationHalfLife;
		pr.estimatedValue.units="days";
			 
		
		if(bioconcentration.biotransformationFactors.size()>0) {
			pr.estimatedValue.model=new ArrayList<>();
			Model model=new Model();
			pr.estimatedValue.model.add(model);
			model.factors=bioconcentration.biotransformationFactors;
		}
		
		setAD(pd.getModel(), pr, bioconcentration.notes);
		
		return pr;
	}
	
	class RBiodeg {
		Model BIOWIN3;
		Model BIOWIN5;
		String RBiodeg;
	}
	
		
	PredictionReport getPredictionReport(PredictionDashboard pd, Object obj) {
		
//		addSelectedValue(obj);
		
		HTMLReportCreatorEPISUITE rc=new HTMLReportCreatorEPISUITE();
		
//		String htmlReport=rc.createReport(null);//TODO
		
		
		return new PredictionReport(pd,Utilities.gson.toJson(obj),null,pd.getCreatedBy());//TODO add html report
	}
		
	
	
	private PropertyResult2 setValuesNoUnitConversion(PredictionDashboard pd, PropertyResult pr) {
		PropertyResult2 pr2=new PropertyResult2(pr);
		return setValuesNoUnitConversion(pd, pr2);
	}
	
	
	private PropertyResult2 setValuesNoUnitConversion(PredictionDashboard pd, PropertyResult2 pr) {
		
		pd.setPredictionValue(pr.estimatedValue.value);
		if(pr.selectedValue!=null && pr.selectedValue.valueType.equals("EXPERIMENTAL")) {
			pd.setExperimentalValue(pr.selectedValue.value);
		}
				
		
		for (Model model:pr.estimatedValue.model) {
					
//			if(model.output!=null && model.output.contains("outside the estimation domain")) {
//				System.out.println("Outside AD for "+pd.getModel().getName()+"\tOutput="+model.output);
//			}
			
			if(model.output!=null) {
//				System.out.println(pd.getModel().getName()+"\t"+model.output);
				model.output=null;//to make report smaller //TODO get AD first
			}
			
//			if(model.notes!=null && !model.notes.isBlank()) {
//				System.out.println("use model notes for ad:"+model.notes);
//				setAD(pd.getModel(),pr,model.notes);
//			}
			
		}
		
		
		
		return pr;
	}

	
	public class Parameter {
	    public Double value;
	    public String valueString;
	    public String units;
	    public String valueType;
	}
	

	public class ArnotGobasBcfBafEstimate{
	    public String trophicLevel;
	    public Double bioconcentrationFactor;
	    public Double logBioconcentrationFactor;
	    public Double bioaccumulationFactor;
	    public Double logBioaccumulationFactor;
	    public String unit;
	    public String trophicLevelNote;
	}
	

	public class Bioconcentration{
	    public Parameters parameters;
	    
	    public Double bioconcentrationFactor;
	    
	    public Double experimentalBioconcentrationFactor;
	    
	    public Double logBioconcentrationFactor;
	    
	    public Double biotransformationHalfLife;
	    public Double bioaccumulationFactor;
	    public Double logBioaccumulationFactor;
	    public ArrayList<Factor> biotransformationFactors;
	    public ArrayList<BiotransformationRateConstant> biotransformationRateConstants;
	    public ArrayList<Factor> bioconcentrationFactors;
	    public String biocontrationFactorEquation;
	    public Double biocontrationFactorEquationSum;
	    public ArrayList<ArnotGobasBcfBafEstimate> arnotGobasBcfBafEstimates;
	    public String notes;
	    public String output;
	}

	public class BiodegradationRate{
	    public Parameters parameters;
	    public ArrayList<Model> models;
	    public String notes;
	    public String output;
	}


	public class BiotransformationRateConstant{
	    public String type;
	    public Double value;
	    public String unit;
	}


	public class ChemicalProperties{
		public String cas;
		public String name;
	    public String systematicName;
	    public String smiles;
	    public Double molecularWeight;
	    public String molecularFormula;
	    public String molecularFormulaHtml;
	    public Boolean organic;
	    public Boolean organicAcid;
	    public Boolean aminoAcid;
	    public Boolean nonStandardMetal;
	    public String flags;
	}

	public class DermalPermeability{
	    public Parameters parameters;
	    public Double dermalPermeabilityCoefficient;
	    public Double dermalAbsorbedDose;
	    public Double dermalAbsorbedDosePerEvent;
	    public Double lagTimePerEventHours;
	    public Double timeToReachSteadyStateHours;
	    public String output;
	}

	
	public class ExperimentalValue {
		public String author;
		public Integer year;
		public Integer order;
		public Double value;
		public String units;
		public String valueType="EXPERIMENTAL";
		                       
	}

	public class EstimatedValue{
	    public Model model;
	    public Double value;
	    public String valueString;
	    public String valueType;
	    public String units;
	    
//	    public String smiles;//added by TMM for validation exercise
	    public Double expValue;
	    public ChemicalProperties chemicalProperties;
	    
	}
	
	public static class EstimatedValue2{
		
		public EstimatedValue2() {}
		
		public EstimatedValue2(EstimatedValue ev) {
			
			if(ev.model!=null) {
				model=new ArrayList<>();
				model.add(ev.model);
			}
			
			value=ev.value;
			valueString=ev.valueString;
			units=ev.units;
			valueType=ev.valueType;
			
//			System.out.println(model.get(0).notes);
		}
		
	//has list of models- they should have just used this more general form 
	    public ArrayList<Model> model;
	    public Double value;
	    public String valueString;
	    public String error;
	    public String applicabilityDomainConclusion;
	    public String notes;

	    public String units;
	    public String valueType;
	}

	public class Factor{
	    public String type;
	    public String description;
	    public int fragmentCount;
	    public Double coefficient;
	    public Double contribution;
	    public Integer trainingCount;
	    public Integer validationCount;
	    public Integer maxFragmentCount;
	    public Double totalCoefficient;
	    public Double value;
	    public String unit;
	}
	
	

	public class Flowrate{
	    @JsonProperty("MassPerHour") 
	    public Double massPerHour;

	    @JsonProperty("MolPerHour") 
	    public Double molPerHour;
	    
	    @JsonProperty("Percent")
	    public Double percent;
	}

	public class Flags{
	    public Boolean isOrganicAcid;
	    public Boolean isAminoAcid;
	}

	public class FugacityModel{
	    public Parameters parameters;
	    public Model model;
	}

	public class HalfLife{
	    public Double ph;
	    public Double value;
	    public Boolean baseCatalyzed;
	    public Boolean acidCatalyzed;
	    public Boolean phosphorusEster;
	}


	public class Hydrolysis{
	    public ArrayList<HalfLife> halfLives;
	    public ArrayList<Object> phosphorusEsterHalfLives;
	    public ArrayList<Object> fragments;
	    public Double baseCatalyzedRateConstant;
	    public Double acidCatalyzedRateConstant;
	    public Double acidCatalyzedRateConstantForTransIsomer;
	    public Double neutralRateConstant;
	    public String output;
	}


	public class PropertyResult {
	    public Parameters parameters;
	    public EstimatedValue estimatedValue;
	    public ArrayList<ExperimentalValue> experimentalValues;
	    public Parameter selectedValue;
	    
	    public ArrayList<ModelResult> modelResults;
	    
	    public String output;
	    
//	    public String propertyName;	    
	    public Double value;	    
	    public String units;
	    public String valueType;
	}
	
	public class ModelResult {
		 String qsarClass;
         String organism;
         String duration;
         String endpoint;
         Double concentration;
         Double maxLogKow;
         ArrayList <String> flags;
	}
	
	
    public static class ChemicalIdentifiers{
		String preferredName;
		String dtxsid;
		String dtxcid;
		String casrn;
		String smiles;
		
		public ChemicalIdentifiers(DsstoxRecord dr) {
			dtxsid= dr.getDtxsid();
			dtxcid= dr.getDtxcid();
			casrn=dr.getCasrn();
			preferredName= dr.getPreferredName();
			smiles=dr.getSmiles();
		}
	}

	
	public static class PropertyResult2 {
		public Parameters parameters;
	    public EstimatedValue2 estimatedValue;
	    public ArrayList<ExperimentalValue> experimentalValues;
	    public Parameter selectedValue;
	    
	    public Double value;
	    public String units;
	    public String valueType;
	    
//	    ChemicalIdentifiers chemicalIdentifiers;
//	    public String dtxsid;
//	    public String casrn;
//	    public String preferredName;
//	    public String propertyName;
//	    public String propertyDescription;
	    
	    public PropertyResult2() {
	    	
	    }
	    
	    public PropertyResult2(PropertyResult pr) {
	    	parameters=pr.parameters;
	    	
	    	estimatedValue=new EstimatedValue2(pr.estimatedValue);
	    	experimentalValues=pr.experimentalValues;
	    	selectedValue=pr.selectedValue;	
	    	 
//	    	propertyName=pr.propertyName;
	    	value=pr.value;
	    	units=pr.units;
	    	valueType=pr.valueType;
	    	
	    }
	}


	public class AtmosphericHalfLife{
	    public Parameters parameters;
	    public EstimatedValue estimatedValue;
	    public EstimatedValue estimatedHydroxylRadicalReactionRateConstant;
	    public EstimatedValue estimatedOzoneReactionRateConstant;
	    public ArrayList<ExperimentalValue> experimentalHydroxylRadicalReactionRateConstantValues;
	    public ArrayList<ExperimentalValue> experimentalOzoneReactionRateConstantValues;
	    public ArrayList<ExperimentalValue> experimentalNitrateReactionRateConstantValues;
	    public Parameter selectedHydroxylRadicalReactionRateConstant;
	    public Parameter selectedOzoneReactionRateConstantValues;
	}
	
	
	
	public class Model{
		
	    public Double logKow;
	    
	    public ArrayList<Factor> factors;
	    public String output;
	    public String notes;
	    
	    public Flags flags;
	    public Double mackayParticleGasPartitionCoefficient;
	    public Double koaParticleGasPartitionCoefficient;
	    public Double mackayAdsorptionFraction;
	    public Double koaAdsorptionFraction;
	    public Double jungePankowAdsorptionFraction;
	    public ArrayList<Model> models;
	    public String type;
	    public Double rateConstant;
	    public Double halfLifeHours;
	    public Double logKoc;
	    @JsonProperty("Influent") 
	    public Flowrate influent;
	    @JsonProperty("PrimarySludge") 
	    public Flowrate primarySludge;
	    @JsonProperty("WasteSludge") 
	    public Flowrate wasteSludge;
	    @JsonProperty("TotalSludge") 
	    public Flowrate totalSludge;
	    
	    @JsonProperty("PrimVloitilization") 
	    public Flowrate primVolatization;
	    
	    @JsonProperty("SettlingVloitilization") 
	    public Flowrate settlingVolatilization;
	    
	    @JsonProperty("AerationOffGas") 
	    public Flowrate aerationOffGas;
	    @JsonProperty("TotalAir") 
	    public Flowrate totalAir;
	    @JsonProperty("PrimBiodeg") 
	    public Flowrate primBiodeg;
	    @JsonProperty("SettlingBiodeg") 
	    public Flowrate settlingBiodeg;
	    @JsonProperty("AerationBiodeg") 
	    public Flowrate aerationBiodeg;
	    @JsonProperty("TotalBiodeg") 
	    public Flowrate totalBiodeg;
	    @JsonProperty("FinalEffluent") 
	    public Flowrate finalEffluent;
	    @JsonProperty("TotalRemoval") 
	    public Flowrate totalRemoval;
	    @JsonProperty("PrimaryRateConstant") 
	    public Flowrate primaryRateConstant;
	    @JsonProperty("AerationRateConstant") 
	    public Flowrate aerationRateConstant;
	    @JsonProperty("SettlingRateConstant") 
	    public Flowrate settlingRateConstant;
	    @JsonProperty("CalculationVariables") 
	    public ArrayList<Double> calculationVariables;
	    @JsonProperty("Air") 
	    public ArrayList<Compartment> air;
	    @JsonProperty("Water") 
	    public ArrayList<Compartment> water;
	    @JsonProperty("Soil") 
	    public ArrayList<Compartment> soil;
	    @JsonProperty("Sediment") 
	    public ArrayList<Compartment> sediment;
	    @JsonProperty("Persistence") 
	    public Double persistence;
	    public ArrayList<Double> aEmissionArray;
	    public ArrayList<Double> aAdvectionTimeArray;
	    public ArrayList<Double> aFugacities;
	    public ArrayList<Double> aReaction;
	    public ArrayList<Double> aAdvection;
	    public ArrayList<Double> aReactionPercent;
	    public ArrayList<Double> aAdvectionPercent;
	    public ArrayList<Double> aSums;
	    public ArrayList<Double> aTimes;
	    @JsonProperty("HalfLifeArray") 
	    public ArrayList<Double> halfLifeArray;
	    @JsonProperty("HalfLifeFactorArray") 
	    public ArrayList<Double> halfLifeFactorArray;
	    @JsonProperty("Emission") 
	    public ArrayList<Double> emission;
	    @JsonProperty("AdvectionTimesArray") 
	    public ArrayList<Double> advectionTimesArray;
	    public ArrayList<Object> aNotes;
	    public Double meltingPointKelvins;
	    public Double meltingPointLimitKelvins;
	    public Double meltingPointCelsius;
	    public Double meltingPointAdaptedJoback;
	    public Double meltingPointGoldOgle;
	    public Double meltingPointMean;
	    public Double meltingPointSelected;
	    public Double boilingPointKelvinsUncorrected;
	    public Double boilingPointKelvinsCorrected;
	    public Double boilingPointCelsius;
	    public Double mmHg;
	    public Double pa;
	    public Double waterSolubility;
	    public String equation;
	    public String name;
	    public Double value;
	    public Double hlcAtm;
	    public Double hlcUnitless;
	    public Double hlcPaMol;
	    public Double kow;
	    public Double kaw;
	    public Double koa;
	    public Double logKoa;
	    
	    public Double firstOrderMCI;
	    public Double nonCorrectedLogKoc;
	    public Double correctedLogKoc;
	    public Double koc;

	    
	}
	

//	public class Model13{
//	    public String type;
//	    public Double rateConstant;
//	    public Double halfLifeHours;
//	    public ArrayList<Factor> factors;
//	    public Double firstOrderMCI;
//	    public String name;
//	    public Double nonCorrectedLogKoc;
//	    public Double correctedLogKoc;
//	    public Double koc;
//	    public Double logKow;
//	    public Double value;
//	}

	public class MolecularWeight{
	    public Double value;
	    public String units;
	    public String valueType;
	}

	public class Parameters{
		
	    public String smiles;
	    public Parameter userLogKow;
	    public Parameter userMeltingPoint;
	    public Parameter userBoilingPoint;
	    public Parameter userWaterSolubility;
	    public Parameter userVaporPressure;
	    public Parameter userHenrysLawConstant;
	    public Parameter userLogKoa;
	    public Parameter userLogKoc;
	    public Parameter userHydroxylReactionRateConstant;
	    public Parameter userDermalPermeabilityCoefficient;
	    public Parameter userAtmosphericHydroxylRadicalConcentration;
	    public Parameter userAtmosphericOzoneConcentration;
	    public Parameter userAtmosphericDaylightHours;
	    public Parameter userStpHalfLifePrimaryClarifier;
	    public Parameter userStpHalfLifeAerationVessel;
	    public Parameter userStpHalfLifeSettlingTank;
	    public Parameter userFugacityHalfLifeAir;
	    public Parameter userFugacityHalfLifeWater;
	    public Parameter userFugacityHalfLifeSoil;
	    public Parameter userFugacityHalfLifeSediment;
	    public Parameter userFugacityEmissionRateAir;
	    public Parameter userFugacityEmissionRateWater;
	    public Parameter userFugacityEmissionRateSoil;
	    public Parameter userFugacityEmissionRateSediment;
	    public Parameter userFugacityAdvectionTimeAir;
	    public Parameter userFugacityAdvectionTimeWater;
	    public Parameter userFugacityAdvectionTimeSoil;
	    public Parameter userFugacityAdvectionTimeSediment;
	    public ArrayList<Object> modules;
	    
	    public Double hydroxylRadicalConcentration;
	    public Double ozoneConcentration;
	    public Boolean twelveHourDay;
	    
	    public Parameter logKow;

	    public Object molecularWeight;//can be Double or a Parameter object depending on the model
	    
	    public Parameter henrysLawConstant;
	    
	    public Double riverWaterDepthMeters;
	    public Double riverWindVelocityMetersPerSecond;
	    public Double riverCurrentVelocityMetersPerSecond;
	    public Double lakeWindVelocityMetersPerSecond;
	    public Double lakeCurrentVelocityMetersPerSecond;
	    public Double lakeWaterDepthMeters;
	    
	    public Parameter waterSolubility;
	    public Parameter vaporPressure;
	    public Parameter biowin3;
	    public Parameter biowin5;
	    public Parameter halfLifeHoursPrimaryClarifier;
	    public Parameter halfLifeHoursAerationVessel;
	    public Parameter halfLifeHoursSettlingTank;
	    public Parameter logKoc;
	    public Parameter meltingPoint;
	    public Parameter atmosphericHydroxylRateConstant;
	    public Parameter ultimateBiodegradation;
	    public Parameter halfLifeAir;
	    public Parameter halfLifeWater;
	    public Parameter halfLifeSoil;
	    public Parameter halfLifeSediment;
	    public Parameter emissionRateAir;
	    public Parameter emissionRateWater;
	    public Parameter emissionRateSoil;
	    public Parameter emissionRateSediment;
	    public Parameter advectionTimeAir;
	    public Parameter advectionTimeWater;
	    public Parameter advectionTimeSoil;
	    public Parameter advectionTimeSediment;
	    public Parameter dermalPermeabilityCoefficient;
	    public Parameter waterConcentrationMgPerLiter;
	    
	    public Double eventDurationHours;
	    public Double fractionAbsorbedWater;
	    public Double skinSurfaceAreaCm2;
	    public Double exposureEventsPerDay;
	    public Double exposureDurationYears;
	    public Double exposureDaysPerYear;
	    public Double bodyWeightKg;
	    public Double averagingTimeDays;
	    public Boolean removeMetals;
	    public PropertyResult logKoa;
	    public SubcooledVaporPressure subcooledVaporPressure;
	}


	
	public class Compartment{
	    @JsonProperty("MassAmount") 
	    public Double massAmount;
	    @JsonProperty("HalfLife") 
	    public Double halfLife;
	    @JsonProperty("Emissions") 
	    public Double emissions;
	}



	public class SewageTreatmentModel{
	    public Parameters parameters;
	    public Model model;
	}


	public class SubcooledVaporPressure{
	    public Double value;
	    public String units;
	    public String valueType;
	}

	public class WaterVolatilization{
	    public Parameters parameters;
	    public Double riverHalfLifeHours;
	    public Double lakeHalfLifeHours;
	}


	public static void main(String[] args) {
		
	}
}

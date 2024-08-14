package gov.epa.run_from_java.scripts.EpiSuite;

/**
* @author TMARTI02
*/

import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1

import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.run_from_java.scripts.EpiSuite.EpisuiteResults.PropertyResult;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

//import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
import java.util.ArrayList;


public class EpisuiteResults {

	public String error;
	
	public Parameters parameters;
	public ChemicalProperties chemicalProperties;
	
	public PropertyResult logKow;
	public PropertyResult meltingPoint;
	public PropertyResult boilingPoint;
	public PropertyResult waterSolubilityFromLogKow;
	public PropertyResult waterSolubilityFromWaterNt;
	public PropertyResult logKoa;

	public PropertyResult vaporPressure;
	public PropertyResult henrysLawConstant;

	
//	public PropertyResult2 vaporPressure;
//	public PropertyResult2 henrysLawConstant;

	
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
	public String dtxsid;
	
	
	public static EpisuiteResults getResults(String json) {
		return Utilities.gson.fromJson(json, EpisuiteResults.class);
	}
	
	
	public void setExpPred(PredictionDashboard pd,gov.epa.databases.dev_qsar.qsar_models.entity.Model model) {

		if(this.error!=null) {
			//					String errorSmiles=results.error.substring(results.error.indexOf(":")+2,results.error.length());//there's a mistake in their output because it always outputs [N-]1[N-]C(=S)SC1=S.[K+].[K+] in the error 
			if(error.contains("Log Kow could not be determined")) {
				error="Log Kow could not be determined";
			}
			pd.setPredictionError(error);
			//					results.error=results.error.replace(errorSmiles, results.smiles);
			//					System.out.println(results.dtxsid+"\t"+results.error);
			//					continue;
		} else if(model.getName().equals("EPISUITE_BCF")) {
			pd.setPredictionValue(this.bioconcentration.bioconcentrationFactor);
			if(this.bioconcentration.experimentalBioconcentrationFactor!=null) {
				double expValue=Math.pow(10.0, this.bioconcentration.experimentalBioconcentrationFactor);
				pd.setExperimentalValue(expValue);
			}
		} else if(model.getName().equals("EPISUITE_KOC")) {
			
			PropertyResult pr=this.logKoc;
			pd.setPredictionValue(Math.pow(10.0,pr.estimatedValue.value));
			
			if(pr.selectedValue!=null && pr.selectedValue.valueType.equals("EXPERIMENTAL")) {
				pd.setExperimentalValue(Math.pow(10.0, pr.selectedValue.value));
			}

		} else if(model.getName().equals("EPISUITE_BAF_UPPER_TROPHIC")) {
			ArnotGobasBcfBafEstimate estimate=bioconcentration.arnotGobasBcfBafEstimates.get(0);
			pd.setPredictionValue(estimate.bioaccumulationFactor);
//			pd.setPredictionValue(this.bioconcentration.bioaccumulationFactor);//should be same within round off
			
		} else if(model.getName().equals("EPISUITE_BCF_UPPER_TROPHIC")) {
			ArnotGobasBcfBafEstimate estimate=bioconcentration.arnotGobasBcfBafEstimates.get(0);
			pd.setPredictionValue(estimate.bioconcentrationFactor);
			
		} else if (model.getName().contains("EPISUITE_BIOWIN")) {
			
			int modelNum=Integer.parseInt(model.getName().replace("EPISUITE_BIOWIN", ""));
			Model model2=this.biodegradationRate.models.get(modelNum-1);
			pd.setPredictionValue(model2.value);

			if(modelNum==1 || modelNum==2 || modelNum==7) {
				if(model2.value>=0.5) {
					pd.setPredictionString("Biodegrades Fast");
				} else {
					pd.setPredictionString("Does NOT Biodegrade Fast");
				}
			} else if(modelNum==3 || modelNum==4) {
				
				if(model2.value>4.75) {
					pd.setPredictionString("Hours");
				} else if (model2.value>4.25 && model2.value<=4.75) {
					pd.setPredictionString("Hours-Days");
				} else if (model2.value>3.75 && model2.value<=4.25) {
					pd.setPredictionString("Days");
				} else if (model2.value>3.25 && model2.value<=3.75) {
					pd.setPredictionString("Days-Weeks");
				} else if (model2.value>2.75 && model2.value<=3.25) {
					pd.setPredictionString("Weeks");
				} else if (model2.value>2.25 && model2.value<=2.75) {
					pd.setPredictionString("Weeks-Months");
				} else if (model2.value>1.75 && model2.value<=2.25) {
					pd.setPredictionString("Months");
				} else {
					pd.setPredictionString("Recalcitrant");
				}
			} else if(modelNum==5 || modelNum==6) {
				if(model2.value>=0.5) {
					pd.setPredictionString("Readily Degradable");
				} else {
					pd.setPredictionString("NOT Readily Degradable");
				}
			} 
			
			
		} else if (model.getName().equals("EPISUITE_WS_KOW")) {
			PropertyResult pr=this.waterSolubilityFromLogKow;
			double WS_MG_L=pr.estimatedValue.value;
			double molWeight=this.chemicalProperties.molecularWeight;
			double WS_MOL_L=WS_MG_L/1000.0/molWeight;

			//					System.out.println(WS_MG_L+"\t"+molWeight+"\t"+WS_MOL_L);
			pd.setPredictionValue(WS_MOL_L);

			if(pr.selectedValue!=null && pr.selectedValue.valueType.equals("EXPERIMENTAL")) {
				double WS_MG_L_exp=this.waterSolubilityFromLogKow.selectedValue.value;
				double WS_MOL_L_exp=WS_MG_L_exp/1000.0/molWeight;
				pd.setExperimentalValue(WS_MOL_L_exp);
			}

		} else if (model.getName().equals("EPISUITE_WATERNT")) {
			PropertyResult pr=this.waterSolubilityFromWaterNt;
			double WS_MG_L=pr.estimatedValue.value;
			double molWeight=this.chemicalProperties.molecularWeight;
			double WS_MOL_L=WS_MG_L/1000.0/molWeight;
			pd.setPredictionValue(WS_MOL_L);

			if(pr.selectedValue!=null && pr.selectedValue.valueType.equals("EXPERIMENTAL")) {
				double WS_MG_L_exp=pr.selectedValue.value;
				double WS_MOL_L_exp=WS_MG_L_exp/1000.0/molWeight;
				pd.setExperimentalValue(WS_MOL_L_exp);
			}


		} else if (model.getName().equals("EPISUITE_BioHCWIN")) {
			setValuesNoUnitConversion(pd, this.hydrocarbonBiodegradationRate);
		} else if (model.getName().equals("EPISUITE_HLC")) {
			setValuesNoUnitConversion(pd, this.henrysLawConstant);
		} else if (model.getName().equals("EPISUITE_LOGKOA")) {
			setValuesNoUnitConversion(pd, this.logKoa);
		} else if (model.getName().equals("EPISUITE_LOGP")) {						
			setValuesNoUnitConversion(pd, this.logKow);
		} else if (model.getName().equals("EPISUITE_MP")) {						
			setValuesNoUnitConversion(pd, this.meltingPoint);
		} else if (model.getName().equals("EPISUITE_BP")) {						
			setValuesNoUnitConversion(pd, this.boilingPoint);
		} else if (model.getName().equals("EPISUITE_VP")) {
			setValuesNoUnitConversion(pd, this.vaporPressure);
		} else if (model.getName().equals("EPISUITE_RBIODEG")) {
			
			Model model3=this.biodegradationRate.models.get(3-1);
			Model model5=this.biodegradationRate.models.get(5-1);
			
			if(model3.value>2.75 && model5.value>=0.5) {
				pd.setPredictionString("Readily Degradable");
			} else {
				pd.setPredictionString("Not Readily Degradable");
			}
		} else if (model.getName().equals("EPISUITE_BIOTRANS_HL")) {
			pd.setPredictionValue(this.bioconcentration.biotransformationHalfLife);
		} else {
			System.out.println("TODO in getResults implement "+model.getName());
		}
	}
		
	
	private static void setValuesNoUnitConversion(PredictionDashboard pd, PropertyResult pr) {
		pd.setPredictionValue(pr.estimatedValue.value);
		if(pr.selectedValue!=null && pr.selectedValue.valueType.equals("EXPERIMENTAL")) {
			pd.setExperimentalValue(pr.selectedValue.value);
		}
	}
	
	public class Parameter {
	    public double value;
	    public String units;
	    public String valueType;
	}
	

	public class ArnotGobasBcfBafEstimate{
	    public String trophicLevel;
	    public double bioconcentrationFactor;
	    public double logBioconcentrationFactor;
	    public double bioaccumulationFactor;
	    public double logBioaccumulationFactor;
	    public String unit;
	    public String trophicLevelNote;
	}
	

	public class Bioconcentration{
	    public Parameters parameters;
	    
	    public double bioconcentrationFactor;
	    
	    public Double experimentalBioconcentrationFactor;
	    
	    public double logBioconcentrationFactor;
	    
	    public double biotransformationHalfLife;
	    public double bioaccumulationFactor;
	    public double logBioaccumulationFactor;
	    public ArrayList<Factor> biotransformationFactors;
	    public ArrayList<BiotransformationRateConstant> biotransformationRateConstants;
	    public ArrayList<Factor> bioconcentrationFactors;
	    public String biocontrationFactorEquation;
	    public double biocontrationFactorEquationSum;
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
	    public double value;
	    public String unit;
	}


	public class ChemicalProperties{
	    public String name;
	    public String systematicName;
	    public String smiles;
	    public double molecularWeight;
	    public String molecularFormula;
	    public String molecularFormulaHtml;
	    public boolean organic;
	    public boolean organicAcid;
	    public boolean aminoAcid;
	    public boolean nonStandardMetal;
	    public String flags;
	}

	public class DermalPermeability{
	    public Parameters parameters;
	    public double dermalPermeabilityCoefficient;
	    public double dermalAbsorbedDose;
	    public double dermalAbsorbedDosePerEvent;
	    public double lagTimePerEventHours;
	    public double timeToReachSteadyStateHours;
	    public String output;
	}



	public class EstimatedValue{
	    public Object model;
	    public double value;
	    public String units;
	    public String valueType;
	}
	
//	public class EstimatedValue2{//has list of models
//	    public ArrayList<Model> model;
//	    public double value;
//	    public String units;
//	    public String valueType;
//	}

	public class Factor{
	    public String type;
	    public String description;
	    public int fragmentCount;
	    public double coefficient;
	    public double contribution;
	    public int trainingCount;
	    public int validationCount;
	    public int maxFragmentCount;
	    public double totalCoefficient;
	    public double value;
	    public String unit;
	}
	
	

	public class Flowrate{
	    @JsonProperty("MassPerHour") 
	    public double massPerHour;
	    @JsonProperty("MolPerHour") 
	    public double molPerHour;
	    @JsonProperty("Percent") 
	    public double percent;
	}

	public class Flags{
	    public boolean isOrganicAcid;
	    public boolean isAminoAcid;
	}

	public class FugacityModel{
	    public Parameters parameters;
	    public Model model;
	}

	public class HalfLife{
	    public double ph;
	    public double value;
	    public boolean baseCatalyzed;
	    public boolean acidCatalyzed;
	    public boolean phosphorusEster;
	}


	public class Hydrolysis{
	    public ArrayList<HalfLife> halfLives;
	    public ArrayList<Object> phosphorusEsterHalfLives;
	    public ArrayList<Object> fragments;
	    public double baseCatalyzedRateConstant;
	    public double acidCatalyzedRateConstant;
	    public double acidCatalyzedRateConstantForTransIsomer;
	    public double neutralRateConstant;
	    public String output;
	}


	public class PropertyResult {
	    public Parameters parameters;
	    public EstimatedValue estimatedValue;
	    public ArrayList<Object> experimentalValues;
	    public Parameter selectedValue;
	    public double value;
	    public String units;
	    public String valueType;
	}
	
	
//	public class PropertyResult2 {
//	    public Parameters parameters;
//	    public EstimatedValue2 estimatedValue;
//	    public ArrayList<Object> experimentalValues;
//	    public Parameter selectedValue;
//	    public double value;
//	    public String units;
//	    public String valueType;
//	}


	public class AtmosphericHalfLife{
	    public Parameters parameters;
	    public EstimatedValue estimatedValue;
	    public Parameter estimatedHydroxylRadicalReactionRateConstant;
	    public EstimatedValue estimatedOzoneReactionRateConstant;
	    public ArrayList<Object> experimentalHydroxylRadicalReactionRateConstantValues;
	    public ArrayList<Object> experimentalOzoneReactionRateConstantValues;
	    public ArrayList<Object> experimentalNitrateReactionRateConstantValues;
	    public Parameter selectedHydroxylRadicalReactionRateConstant;
	    public Parameter selectedOzoneReactionRateConstantValues;
	}
	
	
	
	public class Model{
		
	    public double logKow;
	    public ArrayList<Factor> factors;
	    public String output;
	    public String notes;
	    public Flags flags;
	    public double mackayParticleGasPartitionCoefficient;
	    public double koaParticleGasPartitionCoefficient;
	    public double mackayAdsorptionFraction;
	    public double koaAdsorptionFraction;
	    public double jungePankowAdsorptionFraction;
	    public ArrayList<Model> models;
	    public String type;
	    public double rateConstant;
	    public double halfLifeHours;
	    public double logKoc;
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
	    public double persistence;
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
	    public double meltingPointKelvins;
	    public double meltingPointLimitKelvins;
	    public double meltingPointCelsius;
	    public double meltingPointAdaptedJoback;
	    public double meltingPointGoldOgle;
	    public double meltingPointMean;
	    public double meltingPointSelected;
	    public double boilingPointKelvinsUncorrected;
	    public double boilingPointKelvinsCorrected;
	    public double boilingPointCelsius;
	    public double mmHg;
	    public double pa;
	    public double waterSolubility;
	    public String equation;
	    public String name;
	    public double value;
	    public double hlcAtm;
	    public double hlcUnitless;
	    public double hlcPaMol;
	    public double kow;
	    public double kaw;
	    public double koa;
	    public double logKoa;
	}

	public class Model13{
	    public String type;
	    public double rateConstant;
	    public double halfLifeHours;
	    public ArrayList<Factor> factors;
	    public double firstOrderMCI;
	    public String name;
	    public double nonCorrectedLogKoc;
	    public double correctedLogKoc;
	    public double koc;
	    public double logKow;
	    public double value;
	}

	public class MolecularWeight{
	    public double value;
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
	    
	    public double hydroxylRadicalConcentration;
	    public double ozoneConcentration;
	    public boolean twelveHourDay;
	    
	    public Parameter logKow;

	    public Object molecularWeight;//can be Double or a Parameter object depending on the model
	    
	    public Parameter henrysLawConstant;
	    
	    public double riverWaterDepthMeters;
	    public double riverWindVelocityMetersPerSecond;
	    public double riverCurrentVelocityMetersPerSecond;
	    public double lakeWindVelocityMetersPerSecond;
	    public double lakeCurrentVelocityMetersPerSecond;
	    public double lakeWaterDepthMeters;
	    
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
	    
	    public double eventDurationHours;
	    public double fractionAbsorbedWater;
	    public double skinSurfaceAreaCm2;
	    public double exposureEventsPerDay;
	    public double exposureDurationYears;
	    public double exposureDaysPerYear;
	    public double bodyWeightKg;
	    public double averagingTimeDays;
	    public boolean removeMetals;
	    public PropertyResult logKoa;
	    public SubcooledVaporPressure subcooledVaporPressure;
	}


	
	public class Compartment{
	    @JsonProperty("MassAmount") 
	    public double massAmount;
	    @JsonProperty("HalfLife") 
	    public double halfLife;
	    @JsonProperty("Emissions") 
	    public double emissions;
	}



	public class SewageTreatmentModel{
	    public Parameters parameters;
	    public Model model;
	}


	public class SubcooledVaporPressure{
	    public double value;
	    public String units;
	    public String valueType;
	}

	public class WaterVolatilization{
	    public Parameters parameters;
	    public double riverHalfLifeHours;
	    public double lakeHalfLifeHours;
	}
}

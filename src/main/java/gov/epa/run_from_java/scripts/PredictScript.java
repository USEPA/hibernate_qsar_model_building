package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.stream.Stream;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_models.entity.DsstoxRecord;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelBytesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.ModelWebService;
import kong.unirest.HttpResponse;

/**
 * Runs an external set for a model
 * 
* @author TMARTI02
*/
public class PredictScript {

	ModelService modelService = new ModelServiceImpl();
	ModelBytesService modelBytesService = new ModelBytesServiceImpl();
	ModelWebService modelWebService = new ModelWebService(DevQsarConstants.SERVER_LOCAL, DevQsarConstants.PORT_PYTHON_MODEL_BUILDING);

	
	public static List<DsstoxRecord>getDsstoxRecords() {
		Gson gson=new Gson();

		List<DsstoxRecord>records=new ArrayList<>();
		
		
		String filepath="data\\dsstox\\json\\2023_04_snapshot_dsstox_records_2024_01_09.json";

		try {
			JsonArray ja = Utilities.gson.fromJson(new FileReader(filepath), JsonArray.class);

			for (JsonElement je:ja) {
				JsonObject jo=(JsonObject)je;
				Set<Map.Entry<String, JsonElement>> entries = jo.entrySet();

				DsstoxRecord rec=new DsstoxRecord();

				for(Map.Entry<String, JsonElement> entry: entries) {
					String fieldName=entry.getKey();
					JsonElement value=entry.getValue();

					if (value.isJsonNull()) continue;

					if(fieldName.equals("id")) 	rec.setId(value.getAsLong());
					if(fieldName.equals("cid"))	rec.setCid(value.getAsLong());
					if(fieldName.equals("dtxcid")) 	rec.setDtxcid(value.getAsString());
					if(fieldName.equals("dtxsid")) 	rec.setDtxsid(value.getAsString());
					if(fieldName.equals("preferred_name")) 	rec.setPreferredName(value.getAsString());
					if(fieldName.equals("casrn")) 	rec.setCasrn(value.getAsString());
					if(fieldName.equals("smiles")) 	rec.setSmiles(value.getAsString());
					if(fieldName.equals("mol_weight")) 	rec.setMolWeight(value.getAsDouble());
					if(fieldName.equals("mol_image_png_available")) rec.setMolImagePNGAvailable(value.getAsBoolean());

				}
				records.add(rec);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return records;

	}
	
	Hashtable<String, DsstoxRecord> getDsstoxHashtableByDTXCID(List<DsstoxRecord>records) {
		Hashtable<String, DsstoxRecord> ht=new Hashtable<>();
		
		for (DsstoxRecord record:records) {
			if(record.getDtxcid()==null) continue;
			ht.put(record.getDtxcid(),record);
		}
		return ht;
	}
	
	public static Hashtable<String, DsstoxRecord> getDsstoxHashtableByDTXSID(List<DsstoxRecord>records) {
		Hashtable<String, DsstoxRecord> ht=new Hashtable<>();
		
		for (DsstoxRecord record:records) {
			if(record.getDtxsid()==null) continue;
			ht.put(record.getDtxsid(),record);
		}
		return ht;
	}
	
	public void predict(String folder, String tsvFileName, Long modelId,boolean use_pmml, boolean use_sklearn2pmml) throws ConstraintViolationException {
		
		try {
			
			Path path = Paths.get(folder+tsvFileName);

			Stream<String> lines = Files.lines(path);
			String predictionTSV = lines.collect(Collectors.joining("\n"));
			lines.close();
//			System.out.println(predictionTSV);
			
			if (modelId==null) {
				//			logger.error("Model with supplied parameters has not been built");
				return;
			} else if (predictionTSV==null) {
				//			logger.error("Dataset instances were not initialized");
				return;
			}

			Model model=modelService.findById(modelId);
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			String strModelId = String.valueOf(modelId);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				HttpResponse<String>response=modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				HttpResponse<String>response=modelWebService.callInitPickle(bytes,strModelId);
			}

			//		System.out.println("Splitting id = "+splitting.getId());
			String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
			ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
			
			
			List<DsstoxRecord>records=getDsstoxRecords();
			Hashtable<String,DsstoxRecord>htDsstox=getDsstoxHashtableByDTXCID(records);

			Hashtable<String,Double>htPredWS=new Hashtable<>();
			
			for (ModelPrediction mp:modelPredictionsArray) {
				
				String [] dtxcids=mp.id.split("\\|");
				
				for (String dtxcid:dtxcids) {
					DsstoxRecord dr=htDsstox.get(dtxcid);
					String dtxsid=dr.getDtxsid();
					double pred_Neg_Log_molar=mp.pred;
					double pred_molar=Math.pow(10.0, -pred_Neg_Log_molar);
					double pred_g_L=pred_molar*dr.getMolWeight();
//					System.out.println(dtxsid+"\t"+pred_g_L);
					
					htPredWS.put(dtxsid, pred_g_L);
				}
			}
			
			
			String json=Utilities.gson.toJson(htPredWS);
			
			System.out.println(json);
			
			Hashtable<String,Double>htPredWS2=Utilities.gson.fromJson(json, (Hashtable.class));
			
			System.out.println(htPredWS2.get("DTXSID6021953"));
			
			
			FileWriter fw=new FileWriter(folder+"WS pred xgb.json");
			fw.write(json);
			fw.flush();
			fw.close();
			
//			System.out.println(predictResponse);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 *  Generates tsv on the fly by dtxsid for the dataset being predicted by the model 
	 *  
	 * @param folder
	 * @param modelId modelId for the model to use
	 * @param datasetName dataset to make predictions for
	 * @throws ConstraintViolationException
	 */
	public Hashtable<String,Double> predict(String filePathOut, Long modelId,String datasetName) throws ConstraintViolationException {
		
		try {
			
			boolean use_pmml=false;
			boolean use_sklearn2pmml=false;
					
			Model model=modelService.findById(modelId);
			String predictionTSV = ModelData.getOverallInstancesByDTXSID(datasetName, model.getSplittingName(), model.getDescriptorSetName());
			
//			System.out.println(predictionTSV);
			
			byte[] bytes = modelBytesService.getBytesSql(modelId, use_pmml);

			String strModelId = String.valueOf(modelId);

			//Following may not be necessary if webservice hasnt been restarted:
			if (use_pmml) {
				String details=new String(model.getDetails());
				modelWebService.callInitPmml(bytes, strModelId, details,use_sklearn2pmml);
			} else {
				modelWebService.callInitPickle(bytes,strModelId);
			}

			String predictResponse = modelWebService.callPredict(predictionTSV, strModelId).getBody();
			ModelPrediction[] modelPredictionsArray = Utilities.gson.fromJson(predictResponse, ModelPrediction[].class);
			
			Hashtable<String,Double>htPredWS=new Hashtable<>();
			
			for (ModelPrediction mp:modelPredictionsArray) {
				String dtxsid=mp.id;												
				htPredWS.put(dtxsid, mp.pred);
			}
			
			String json=Utilities.gson.toJson(htPredWS);
			
//			System.out.println(json);
			
//			Hashtable<String,Double>htPredWS2=Utilities.gson.fromJson(json, (Hashtable.class));
			
//			System.out.println(htPredWS2.get("DTXSID6021953"));

			if (filePathOut!=null) {				
				System.out.println(filePathOut);
				FileWriter fw=new FileWriter(filePathOut);
				fw.write(json);
				fw.flush();
				fw.close();
			}
			
			return htPredWS;			
//			System.out.println(predictResponse);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

	public String getPropertyNameModel(Long modelId) {
		String sql="select p.name from qsar_models.models m\r\n"
				+ "			join qsar_datasets.datasets d on m.dataset_name=d.name\r\n"
				+ "			join qsar_datasets.properties p on d.fk_property_id = p.id\r\n"
				+ "			where m.id="+modelId+";";

		
		String propertyNameModel=SqlUtilities.runSQL(SqlUtilities.getConnectionPostgres(), sql);
		return propertyNameModel;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PredictScript ps=new PredictScript();
		
		//Estimate water solubility values for 96 hr FHM toxicity dataset:
//		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\hibernate_qsar_model_building\\data\\dev_qsar\\output\\exp_prop_96HR_FHM_LC50_v1_modeling\\";
//		String tsvFileName="exp_prop_96HR_FHM_LC50_v1 modeling_WebTEST-default_full.tsv";
//		ps.predict(folder,tsvFileName, 1066L, false, false);
		
		boolean convertLogMolar=true;
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\0 model_management\\ghs-data-gathering\\data\\experimental\\ECOTOX_2023_12_14\\";
		String filePathOut=folder+"96HR_FHM_LC50_WS.json";
		ps.predict(filePathOut,1066L, "exp_prop_96HR_FHM_LC50_v1 modeling");
		
	}

}

package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.IOException;

import gov.epa.util.wekalite.CSVLoader;
import gov.epa.util.wekalite.Instance;
import gov.epa.util.wekalite.Instances;

public class dataSetCreatorOPERA_Padel {

	void mergeFiles() {

		String property="Henry's law constant";
//		String property="Vapor pressure";
//		String property="Water solubility";
		String dataset=property+" OPERA";

//		String endpointAbbrev="WS";
//		String endpointAbbrev="VP";
		String endpointAbbrev="HLC";
		
//		String set="training";
		String set="prediction";
		
		String folder1="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\datasets\\";		

//		String filePathToxProperty=folder1+dataset+"_T.E.S.T. 5.1_OPERA_"+set+".tsv";
//		String folder2=folder1+"opera "+endpointAbbrev+" padel\\"+endpointAbbrev+" opera\\";
////		String filePathDescriptors=folder2+"smiles "+dataset+"_"+set+"_QSAR-ready_smi_PadelDesc.csv";
//		String filePathDescriptors=folder2+"smiles "+dataset+"_"+set+"_PadelDesc.csv";
//		String filePathDest=folder1+dataset+"_PaDEL_OPERA_OPERA_"+set+".tsv";

		//External set:
		String filePathToxProperty=folder1+"Data from Standard "+property+" from exp_prop external to "+dataset+"_T.E.S.T. 5.1_full.tsv";
		String folder2=folder1+"opera "+endpointAbbrev+" padel\\"+endpointAbbrev+" external\\";
//		String filePathDescriptors=folder2+endpointAbbrev+" external_QSAR-ready_smi_PadelDesc.csv";
		String filePathDescriptors=folder2+endpointAbbrev+" external_PadelDesc.csv";
		String filePathDest=folder1+"Data from Standard "+property+" from exp_prop external to "+dataset+"_PaDEL_OPERA_full.tsv";

		
		File f=new File(filePathToxProperty);
		System.out.println(f.exists());
		
		CSVLoader csvloader=new CSVLoader();
		
		try {
			
						
			Instances instancesToxProperty=csvloader.getDataSetFromFile(filePathToxProperty, "\t");
			Instances instancesDescriptors=csvloader.getDataSetFromFileNoTox(filePathDescriptors, ",");
			
			for (int i=0;i<instancesToxProperty.numInstances();i++) {
				Instance iTox=instancesToxProperty.instance(i);
				
				if (instancesDescriptors.instance(iTox.getName())!=null) {
					Instance iDesc=instancesDescriptors.instance(iTox.getName());
					
					iDesc.setClassValue(iTox.getClassValue());
					
//					System.out.println(iTox.getName()+"\t"+iTox.getClassValue()+"\t"+iDesc.getDescriptors()[0]);
					
				}
				
			}
			
			instancesDescriptors.writeToTSVFile(filePathDest);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		dataSetCreatorOPERA_Padel d=new dataSetCreatorOPERA_Padel();
		d.mergeFiles();
	}

}

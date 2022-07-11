package gov.epa.endpoints.datasets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import gov.epa.databases.dev_qsar.DevQsarConstants;

public class ListSplitter {
	

	
	public static void main(String[] args) {
		String datasetFileName = "ExpProp_HLC_WithChemProp_073022_ChemRegImport";
		String listNameExtension = ".txt";
		String datasetFullFileName = datasetFileName + listNameExtension;
		String datasetName = "Data_for_HLC";
		String datasetFolderPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetName + File.separator + datasetFullFileName;
		int splitsize = 2000;

		
		
		File chemreglist = new File(datasetFolderPath);
		Scanner determineLineCount;
		int recordCount = 0;
		String header = null;
		try {
			determineLineCount = new Scanner(new File(datasetFolderPath));
			if (recordCount==0) {
				header = determineLineCount.nextLine();
			}
			while (determineLineCount.hasNextLine()) {
				determineLineCount.nextLine();
				recordCount++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println(recordCount);
		int splits = (recordCount / splitsize) + 1;
		System.out.println(splits);
		System.out.println(header);
		
		Scanner input;


	try {
		input = new Scanner(new File(datasetFolderPath));
		// skip the header
	    Integer counter = 1;
		input.nextLine();
		    
		for (int j = 0; j < splits; j++) {
		    Integer startingIndex = counter;
		    int endingIndex = counter + splitsize;
			ArrayList<String> lines = new ArrayList<String>();
			
		    

		    for (int i = counter; i < endingIndex && input.hasNext(); i++) {
		    	lines.add(input.nextLine());
		    	counter++;
		    }
		    if (!(input.hasNext())) {
		    	endingIndex = counter;
		    }
		   
		    FileWriter writer;
		    String subsetListName = datasetFileName + "_" + startingIndex.toString() + "_to_" + Integer.toString(counter - 1) + ".txt";
			String subsetListPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetName + File.separator + subsetListName;

			try {
				writer = new FileWriter(subsetListPath);
				writer.write(header + System.lineSeparator());
			     for(String str: lines) {
				       writer.write(str + System.lineSeparator());
				     }
				     writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		} }
		    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

		
		/*
	     Scanner input;
		try {
			input = new Scanner(new File(datasetFolderPath));
		    int counter = 0;

			lines.add((input.nextLine()));
			counter++;
			
			for (int i = 0; i < 9999; i++) {
				input.nextLine();
				counter++;
			}
			
		     while(input.hasNextLine() && counter < 10000)
		     {
		         lines.add((input.nextLine()));
		         counter++;
		     }

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	    String subsetListName = "ExpProp_MP_ChemRegImport_5000_10000.txt";
		String subsetListPath = DevQsarConstants.OUTPUT_FOLDER_PATH + File.separator + datasetName + File.separator + subsetListName;

	     FileWriter writer;
		try {
			writer = new FileWriter(subsetListPath);
		     for(String str: lines) {
			       writer.write(str + System.lineSeparator());
			     }
			     writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		*/


		}

}

package gov.epa.run_from_java.scripts.PredictionDashboard;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.opencsv.CSVReader;

import ToxPredictor.Database.DSSToxRecord;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.TEST.PredictionDashboardScriptTEST2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


/**
 * This class is to create files to run OPERA and Percepta for kelsey vitense (double check tony's smiles)
 * 
 */
public class CreateLogD_Files {

	
	public static class ChemicalData {
	    private String smiles;
	    private String dtxsid;
	    private String source;
	    private String mol;
	    
		public String getSmiles() {
			return smiles;
		}
		public void setSmiles(String smiles) {
			this.smiles = smiles;
		}
		public String getDtxsid() {
			return dtxsid;
		}
		public void setDtxsid(String dtxsid) {
			this.dtxsid = dtxsid;
		}
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getMol() {
			return mol;
		}
		public void setMol(String mol) {
			this.mol = mol;
		}
	    
	    
	}
	

	public static List<ChemicalData> parseExcelFile(String filePath) throws IOException {
        List<ChemicalData> chemicalDataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                ChemicalData chemicalData = new ChemicalData();
                chemicalData.setSmiles(row.getCell(0).getStringCellValue());
                chemicalData.setDtxsid(row.getCell(1).getStringCellValue());
                chemicalData.setSource(row.getCell(2).getStringCellValue());

                chemicalDataList.add(chemicalData);
            }
        }

        return chemicalDataList;
    }

    
    
    public static void writeToExcelFile(List<ChemicalData> dataList, String folder,String filename)  {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Chemical Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("SMILES");
        headerRow.createCell(1).setCellValue("DTXSID");
        headerRow.createCell(2).setCellValue("SOURCE");

        // Populate data rows
        for (int i = 0; i < dataList.size(); i++) {
            ChemicalData data = dataList.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(data.getSmiles());
            row.createCell(1).setCellValue(data.getDtxsid());
            row.createCell(2).setCellValue(data.getSource());
        }

        // Write the output to a file
        try (FileOutputStream fos = new FileOutputStream(folder+File.separator+filename)) {
            workbook.write(fos);
            workbook.close();
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        
    }
    
    
    void writeToSDF(List<ChemicalData> dataList, String outputFilePath) {
    	
    	
    	
        try (FileWriter fw = new FileWriter(outputFilePath)) {
            for(ChemicalData cd:dataList) {
            	fw.write(cd.getMol());
            	fw.write("> <DTXSID>\r\n");
            	fw.write(cd.getDtxsid()+"\r\n\r\n");
            	
            	fw.write("> <SMILES>\r\n");
            	fw.write(cd.getSmiles()+"\r\n\r\n");

            	fw.write("$$$$\r\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String convertToInClause(HashSet<String>items) {
		StringBuilder sb = new StringBuilder();

		int count = 0;
		for (String item : items) {
			sb.append("'").append(item).append("'");
			if (count < items.size() - 1) {
				sb.append(", ");
			}
			count++;
		}

		return sb.toString();
	}
    
    public static List<DSSToxRecord> getDsstoxRecords(HashSet<String>dtxsids) {

		String sql="SELECT gs.dsstox_substance_id,c.dsstox_compound_id,gs.casrn,c.smiles,gs.preferred_name,c.mol_file FROM generic_substances gs\r\n"
				+ "	         join generic_substance_compounds gsc on gs.id = gsc.fk_generic_substance_id\r\n"
				+ "	join compounds c on gsc.fk_compound_id = c.id\r\n"
				+ "	where gs.dsstox_substance_id in ("+convertToInClause(dtxsids)+");";

//		System.out.println(sql);
		List<DSSToxRecord>recs=new ArrayList<>();
		try {
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);
			while (rs.next()) {
				DSSToxRecord dr=new DSSToxRecord();
				dr.sid=rs.getString(1);
				dr.cid=rs.getString(2);
				dr.cas=rs.getString(3);
				dr.smiles=rs.getString(4);
				dr.name=rs.getString(5);
				dr.mol=rs.getString(6);
				recs.add(dr);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return recs;
	}

	private List<ChemicalData> getChemicalListFromExcel(String folder,String filename) {
		try {
            List<ChemicalData> dataList = parseExcelFile(folder+File.separator+filename);
            
            HashSet<String>dtxsids=new HashSet<>();
            
            
            for(ChemicalData cd:dataList) {
            	dtxsids.add(cd.dtxsid);
            }
            
            List<DSSToxRecord>drs=getDsstoxRecords(dtxsids);
            
            Hashtable<String,DSSToxRecord>htDsstox=new Hashtable<>();

            
            for(DSSToxRecord dr:drs) {
            	htDsstox.put(dr.sid, dr);
            }
            
            for(ChemicalData cd:dataList) {
            	
            	if(!htDsstox.containsKey(cd.dtxsid)) {
            		System.out.println(cd.dtxsid+" missing");
            		continue;
            	}
            	
            	DSSToxRecord dr=htDsstox.get(cd.dtxsid);
            	
            	cd.smiles=dr.smiles;
            	cd.mol=dr.mol;
            	
            }
            return dataList;
            
            
//            System.out.println(Utilities.gson.toJson(drs));
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	public static void parseOperaOutputToLogD_Excel() {
		
		
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\kelsey vitense\\predictions\\";
		String sdfInputPath = folder+"All_Mapped_to_DTXSIDs.sdf";
		
		
        try (InputStream sdfInputStream = new FileInputStream(sdfInputPath);
                Workbook workbook = new XSSFWorkbook()) {
        	
        	
            IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
            IteratingSDFReader sdfReader = new IteratingSDFReader(sdfInputStream, builder);

            int counter = 1;
            
            Hashtable<Integer,String>ht=new Hashtable<>();
            
            while (sdfReader.hasNext()) {
                IAtomContainer molecule = sdfReader.next();
                String dtxsid = molecule.getProperty("DTXSID");
                ht.put(counter++, dtxsid);
            }
            
//            System.out.println(Utilities.gson.toJson(ht));
            
            
            String csvFilePath =folder+ "All_Mapped_to_DTXSIDs-sdf_OPERA2.8Pred.csv";
            List<OperaPrediction> operaPredictions = parseCSV(csvFilePath);
            
            for(OperaPrediction op:operaPredictions) {
            	int number=Integer.parseInt(op.moleculeID.replace("Molecule_", ""));
            	op.dtxsid=ht.get(number);
            	op.calculateLogD7_1();
            	op.calculateLogD7_2();
            }
            
            System.out.println(Utilities.gson.toJson(operaPredictions));
            
            createExcelFile(operaPredictions, folder+"Opera2.8 LogD predictions.xlsx");
            
        	
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
		
	}
	
	
	public static void parsePerceptaOutputToLogD_Excel() {
        
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\kelsey vitense\\predictions\\";
		String sdfFilePath = folder+"all chemicals percepta.sdf";
        String excelFilePath = folder+"Percepta LogD predictions.xlsx";

        try (InputStream sdfInputStream = new FileInputStream(sdfFilePath);
             Workbook workbook = new XSSFWorkbook()) {

            // Create a sheet in the workbook
            Sheet sheet = workbook.createSheet("Chemicals");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("DTXSID");
            headerRow.createCell(1).setCellValue("ACD_LogD");

            // Set up CDK SDF reader
            IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
            IteratingSDFReader sdfReader = new IteratingSDFReader(sdfInputStream, builder);

            int rowIndex = 1;
            while (sdfReader.hasNext()) {
                IAtomContainer molecule = sdfReader.next();

                // Extract properties
                String dtxsid = molecule.getProperty("DTXSID");
                String acdLogP = molecule.getProperty("ACD_LogD");

                // Create a new row in the sheet
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(dtxsid != null ? dtxsid : "N/A");
                
                if(acdLogP != null)row.createCell(1).setCellValue(Double.parseDouble(acdLogP));
            }

            // Write the workbook to the file
            try (OutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel file created successfully: " + excelFilePath);

        } catch (Exception  e) {
            e.printStackTrace();
        }
    }
	
	
	
	public static void createExcelFile(List<OperaPrediction> operaPreds, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Chemicals");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("DTXSID");
            headerRow.createCell(1).setCellValue("logP_pred");
            headerRow.createCell(2).setCellValue("pKa_a_pred");
            headerRow.createCell(3).setCellValue("pKa_b_pred");
            headerRow.createCell(4).setCellValue("Ionization");
            headerRow.createCell(5).setCellValue("logD74_pred");
            headerRow.createCell(6).setCellValue("logD7_1");
            headerRow.createCell(7).setCellValue("logD7_2");

            // Populate rows with data
            int rowIndex = 1;
            
            for (OperaPrediction data : operaPreds) {
            	
            	 Row row = sheet.createRow(rowIndex++);
            	 
            	if (data.dtxsid != null) row.createCell(0).setCellValue(data.dtxsid);
                if (data.logPPred != null) row.createCell(1).setCellValue(data.logPPred);
                if (data.pKaAPred != null) row.createCell(2).setCellValue(data.pKaAPred);
                if (data.pKaBPred != null) row.createCell(3).setCellValue(data.pKaBPred);
                if (data.ionization != null) row.createCell(4).setCellValue(data.ionization);
                if (data.logD74Pred != null) row.createCell(5).setCellValue(data.logD74Pred);
                if (data.logD7_1 != null) row.createCell(6).setCellValue(data.logD7_1);
                if (data.logD7_2 != null) row.createCell(7).setCellValue(data.logD7_2);
            }

            // Write to Excel file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            System.out.println("Excel file created successfully: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	public static class OperaPrediction {
	    
		private String dtxsid;
		private String moleculeID;

		private Double logPPred;
	    
		private Double pKaAPred;
	    private Double pKaBPred;
	    private Integer ionization;

	    private Double logD74Pred;
	    private Double logD7_1;
	    private Double logD7_2;
	    

	    void calculateLogD7_1() {
	    	double pH=7;
	    	if(pKaAPred==null && pKaBPred==null) {
	    		logD7_1=logPPred;
	    	} else if (pKaAPred!=null && pKaBPred==null) {
	    		logD7_1=logPPred-Math.log10(1+Math.pow(10, pH-pKaAPred));
	    	} else if (pKaAPred==null && pKaBPred!=null) {
	    		logD7_1=logPPred-Math.log10(1+Math.pow(10, pKaBPred-pH));
	    	} else {
	    		logD7_1=logPPred-Math.log10(1+Math.pow(10, pH-pKaAPred)+Math.pow(10, pKaBPred-pH));
	    	}
	    }
	    
	    void calculateLogD7_2() {
	    	double pH=7;
	    	if(pKaAPred==null && pKaBPred==null) {
	    		logD7_2=logPPred;
	    	} else if (pKaAPred!=null && pKaBPred==null) {
	    		logD7_2=logPPred-Math.log10(1+Math.pow(10, pH-pKaAPred));
	    	} else if (pKaAPred==null && pKaBPred!=null) {
	    		logD7_2=logPPred-Math.log10(1+Math.pow(10, pKaBPred-pH));
	    	} else {
	    		double pKaAvg=(pKaAPred+pKaBPred)/2.0;
	    		logD7_2=logPPred-Math.log10(1+Math.pow(10, pH-pKaAvg));
	    	}
	    }
	    
	    
	    
	    // Constructor
	    public OperaPrediction(String moleculeID, String logPPred, String pKaAPred, String pKaBPred, String logD74Pred, String ionization) {
	        this.moleculeID = moleculeID;
	        
	        if(!logPPred.equals("NaN")) {
	        	this.logPPred=Double.parseDouble(logPPred);
	        }
	        
	        
	        if(!pKaAPred.equals("NaN")) {
	        	this.pKaAPred=Double.parseDouble(pKaAPred);
	        }

	        if(!pKaBPred.equals("NaN")) {
	        	this.pKaBPred=Double.parseDouble(pKaBPred);
	        }
	        
	        if(!logD74Pred.equals("NaN")) {
	        	this.logD74Pred=Double.parseDouble(logD74Pred);
	        }
	        
	        this.ionization = Integer.parseInt(ionization);
	    }

	   

	    @Override
	    public String toString() {
	        return "MoleculeData{" +
	                "moleculeID='" + moleculeID + '\'' +
	                ", logPPred='" + logPPred + '\'' +
	                ", pKaAPred='" + pKaAPred + '\'' +
	                ", pKaBPred='" + pKaBPred + '\'' +
	                ", logD74Pred='" + logD74Pred + '\'' +
	                '}';
	    }
	}
	
	public static List<OperaPrediction> parseCSV(String filePath) {
        List<OperaPrediction> moleculeDataList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();

            // Assuming the first row is the header
            String[] header = records.get(0);
            
            
            int moleculeIDIndex = findColumnIndex(header, "MoleculeID");
            int logPPredIndex = findColumnIndex(header, "LogP_pred");
            int pKaAPredIndex = findColumnIndex(header, "pKa_a_pred");
            int pKaBPredIndex = findColumnIndex(header, "pKa_b_pred");
            int logD74PredIndex = findColumnIndex(header, "LogD74_pred");
            
            int ionizationIndex=findColumnIndex(header, "ionization");
            

            // Iterate over records starting from the second row (index 1)
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                OperaPrediction data = new OperaPrediction(
                        record[moleculeIDIndex],
                        record[logPPredIndex],
                        record[pKaAPredIndex],
                        record[pKaBPredIndex],
                        record[logD74PredIndex],
                        record[ionizationIndex]
                );
                
                moleculeDataList.add(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return moleculeDataList;
    }

    private static int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equals(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column " + columnName + " not found in header.");
    }
	
    public static void main(String[] args) {
        
    	CreateLogD_Files g=new CreateLogD_Files();
    	
//    	String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\kelsey vitense";
//    	String filename="All_Mapped_to_DTXSIDs.xlsx";
//    	List<ChemicalData> dataList=g.getChemicalListFromExcel(folder,filename);
//    	g.writeToSDF(dataList, folder+File.separator+"All_Mapped_to_DTXSIDs.sdf");

    	g.parsePerceptaOutputToLogD_Excel();
//    	g.parseOperaOutputToLogD_Excel();
    	
    	
    }


}

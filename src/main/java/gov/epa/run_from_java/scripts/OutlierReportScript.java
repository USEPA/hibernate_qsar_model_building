package gov.epa.run_from_java.scripts;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.Depiction;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.SmilesParser;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoException;
import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.reports.Outliers.Analog;
import gov.epa.endpoints.reports.Outliers.Outlier;
import gov.epa.util.StructureImageUtil;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import gov.epa.run_from_java.scripts.GetExpPropInfo.GetExpPropInfo;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class OutlierReportScript {
	

	void runGenOutlierReport() {
		
//		String dataset = "Standard Water solubility from exp_prop";
		String dataset = "Standard Henry's law constant from exp_prop";
		String folderOutput="Reports/Outlier testing";
		String jsonFilePath=folderOutput+"/"+dataset+".json";
		String descriptorSetName="T.E.S.T. 5.1";
		String folderTSV="data/dev_qsar/dataset_files/";
		String tsvFilePath=folderTSV+ dataset + "_" + descriptorSetName + "_full.tsv";	
		File f=new File(folderOutput);
		f.mkdirs();
				
		boolean genOverallSetTSV=true;//if false saves time by not having to create overall tsv
		boolean genOutliersJSON=true;//if false saves time by not having to find the outliers using web service

		String tsv=null;
		
		try {

			if (genOverallSetTSV) {
				DatasetFileWriter writer = new DatasetFileWriter();
				tsv=writer.writeWithoutSplitting(dataset, descriptorSetName, folderTSV,true);		
				System.out.println("Tsv creation done");
			} else {
				tsv = Files.readString(Path.of(tsvFilePath));
				System.out.println("Tsv loaded");
			}

			String json=null;

			if (genOutliersJSON) {
				String server="http://localhost";
				//			String server="http://v2626umcth819.rtord.epa.gov";//TODO 819 not working...

				json=QueryOutlierDetectionAPI.callPythonOutlierDetection(tsv, false, server);		
				FileWriter fw=new FileWriter(jsonFilePath);
				
				fw.write(json);
				System.out.println(json);
				fw.flush();
				fw.close();

			} else {
				json = Files.readString(Path.of(jsonFilePath));
				System.out.println(json);
			}

			Outlier[] recordsOutliers= new Gson().fromJson(json, Outlier[].class);		

			findAnalogs(tsv, recordsOutliers);
			String outputFileName="outlier report "+dataset+".html";
			
			Connection conn=SqlUtilities.getConnectionPostgres();
			createReport(conn, dataset,recordsOutliers, folderOutput,outputFileName,outputFileName,null);

			
			
			

			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	void runGenOutlierReportExpProp() {

		boolean genOutliersJSON=true;//if false saves time by not having to find the outliers using web service

		
//		String dataset = "Standard Water solubility from exp_prop";
//		String dataset = "HLC from exp_prop and chemprop";
//		String dataset = "LogP from exp_prop and chemprop";
		
//		String dataset="HLC v1 modeling";
//		String dataset="BP v1 modeling";
//		String dataset="WS v1 modeling";
//		String dataset="VP v1 modeling";
//		String dataset="LogP v1 modeling";
		String dataset="MP v1 modeling";
		
		
//		String folderMain="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 python\\pf_python_modelbuilding\\";
		String folderMain="reports\\Outlier testing\\";
		String folderOutput=folderMain+"\\"+dataset;
		
		String jsonFilePath=folderOutput+"/"+dataset+".json";
		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;

		String tsv = ModelData.generateInstancesWithoutSplitting(dataset,descriptorSetName,true);
		System.out.println("tsv retrieved from the database for "+dataset);

		File f=new File(folderOutput);
		f.mkdirs();

		try {
			String json=null;

			System.out.print("Finding outliers...");
			
			if (genOutliersJSON) {
				String server="http://localhost";
				//			String server="http://v2626umcth819.rtord.epa.gov";//TODO 819 not working...

				QueryOutlierDetectionAPI.port=5006;
				
				json=QueryOutlierDetectionAPI.callPythonOutlierDetection(tsv, false, server);
				
				Object obj=Utilities.gson.fromJson(json, Object.class);
				
				json=Utilities.gson.toJson(obj);
				FileWriter fw=new FileWriter(jsonFilePath);
				fw.write(json);
				fw.flush();
				fw.close();

			} else {
				json = Files.readString(Path.of(jsonFilePath));
//				System.out.println(json);
			}
			System.out.println("Done");
			

			Outlier[] recordsOutliers= new Gson().fromJson(json, Outlier[].class);		

			System.out.print("Finding analogs...");
			findAnalogs(tsv, recordsOutliers);			
			System.out.println("Done");
			
			String outputFileName="outlier report "+dataset+".html";
			Connection conn=SqlUtilities.getConnectionPostgres();
			
			createReport(conn, dataset, recordsOutliers, folderOutput,outputFileName,outputFileName,null);
			
			String listName="PFASSTRUCTV4";
			List<DsstoxRecord>dsstoxRecords=GetExpPropInfo.getChemicalsFromDSSTOXList(listName);		
			HashSet<String>arrayPFAS_CIDs=new HashSet<>();
			for (DsstoxRecord dr:dsstoxRecords) arrayPFAS_CIDs.add(dr.dsstoxCompoundId);

			outputFileName="outlier report "+dataset+"_PFAS.html";
			createReport(conn, dataset, recordsOutliers, folderOutput,outputFileName,outputFileName,arrayPFAS_CIDs);
			
			outputFileName="outlier report "+dataset+"_PFAS.xlsx";
//			createSpreadsheet(conn, dataset, recordsOutliers, folderOutput,outputFileName,outputFileName,arrayPFAS_CIDs);
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * TODO not done...
	 * 
	 * @param recordsOutliers
	 * @param outputFileName
	 */
	void createSpreadsheet(Outlier[] recordsOutliers,String outputFileName) {
		
        Workbook workbook = new XSSFWorkbook();
        
        String sheetName="Outliers";

        
		Sheet sheet=workbook.createSheet(sheetName);

		Row row1 = sheet.createRow(0);
		row1.createCell(0).setCellValue("Outlier");
		sheet.setColumnWidth(0, 20*256);

		Outlier outlier0=recordsOutliers[0];
		
		for (int i=0;i<outlier0.Analogs_Search.size();i++) {
			row1.createCell(i+1).setCellValue("Analog "+(i+1));
			sheet.setColumnWidth((i+1), 60*256);
		}
		

		DecimalFormat df=new DecimalFormat("0.00");
		
		for (int i = 0; i < recordsOutliers.length; i++) {

			Outlier rec=recordsOutliers[i];
			

			int irow=(i+1);
			Row rowi = sheet.createRow(irow);


			double err=Math.abs(rec.exp-rec.pred);
			
			String contents=rec.ID+"\nexp="+df.format(rec.exp)+"\npredRF="+df.format(rec.pred)+"\nerr="+df.format(err);
			
//			rowi.createCell(0).setCellValue(CID);
//			rowi.createCell(1).setCellValue(Original_Smiles);
//			rowi.createCell(2).setCellValue(QSAR_Ready_SMILES_OPERA);
//			rowi.createCell(3).setCellValue(QSAR_Ready_SMILES_SDE);
//			
//			if (inchiKeyOPERA!=null && inchiKeySDE!=null) {
//				rowi.createCell(4).setCellValue(inchiKeyOPERA.equals(inchiKeySDE));
//			} else {
//				rowi.createCell(4).setCellValue("N/A");
//			}
			
			
			
//			rowi.setHeight((short)2000);
//
//			createImage(Original_Smiles, irow, 1, sheet, 1);
//			createImage(QSAR_Ready_SMILES_OPERA, irow, 2, sheet, 1);
//			createImage(QSAR_Ready_SMILES_SDE, irow, 3, sheet, 1);
//			
//			rowi.setHeight((short)(2000*1.15));//add some space for smiles at bottom
			
		}
	}
	
	
	
	public static void main(String[] args)  {
		 OutlierReportScript og=new OutlierReportScript();
//		 og.runGenOutlierReport();
		 og.runGenOutlierReportExpProp();
	
	}
	
	public static String parseJSONFile(String filename) throws IOException {
		String content = new String(Files.readAllBytes(Paths.get(filename)));
		return content;
	}
	

	public static void writeFile(String source, String output) {
		Path path = Paths.get(output);
		try {
			Files.writeString(path, source, StandardCharsets.UTF_8);
		} catch (IOException ex) {
		}
	}

	
	 public static LinkedList<String> Parse(String Line, String Delimiter) {
		    // parses a delimited string into a list

		    LinkedList<String> myList = new LinkedList<String>();

		    int tabpos = 1;

		    while (tabpos > -1) {
		      tabpos = Line.indexOf(Delimiter);

		      if (tabpos > 0) {
		        myList.add(Line.substring(0, tabpos));
		        Line = Line.substring(tabpos + Delimiter.length(), Line.length());
		      } else if (tabpos == 0) {
		        myList.add("");
		        Line = Line.substring(tabpos + Delimiter.length(), Line.length());
		      } else {
		        myList.add(Line.trim());
		      }
		    }

		    return myList;

		  }
	 
	 
	public static void createReport(Connection conn, String datasetName, Outlier[] recs, String outputFolder, String outputFileName, 
			String searchDescription,HashSet<String>cidsPFAS) {
		
		int imageWidth= (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/4;
		
		File folder=new File(outputFolder);
		folder.mkdirs();

		DecimalFormat df=new DecimalFormat("0.00");
		
		// order them by ascending prediction error
		List<Outlier> recl = Arrays.asList(recs);
		
		Collections.sort(recl, new RecComparator());
		
		String imgURL="https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/";
		
		try {
			
			int maxCols=5;
			
			FileWriter fwHtml=new FileWriter(outputFolder+File.separator+outputFileName);
			FileWriter fwTsv=new FileWriter(outputFolder+File.separator+outputFileName.replace("html", "tsv"));
			
			writeHeader(fwHtml,maxCols,searchDescription);
			
			fwTsv.write("ID\texp\tpred\terr\texp_prop_id\r\n");

			
			int countTotal=0;
			
			for (Outlier rec:recl) {
				if (rec.ID.contains("DTXCID") && cidsPFAS!=null) {
					if(!cidsPFAS.contains(rec.ID)) {
						continue;
					}
					
				}
				countTotal++;
			}

			int count=0;
			
			
			for (Outlier rec:recl) {
				
				if (rec.ID.contains("DTXCID") && cidsPFAS!=null) {
					if(!cidsPFAS.contains(rec.ID)) {
						continue;
					}
					
				}
				
				count++;
				

				String sql="select dp.qsar_exp_prop_property_values_id from qsar_datasets.data_points dp\n"+ 
				"inner join qsar_datasets.datasets d on dp.fk_dataset_id =d.id\n"+ 
				"where dp.qsar_dtxcid ='"+rec.ID+"' and d.\"name\"='"+datasetName+"';";

				if (conn!=null) {
					String exp_prop_id=SqlUtilities.runSQL(conn, sql);
					
					LinkedList<String>list=Parse(exp_prop_id, "|");
					
					String ID="";					
					for (int i=0;i<list.size();i++) {
						String currentID=Integer.parseInt(list.get(i).replace("EXP", ""))+"";
						ID+=currentID;
						if(i<list.size()-1) ID+="|";
					}
					double err=Math.abs(rec.exp-rec.pred);
//					System.out.println(rec.ID+"\t"+rec.exp+"\t"+rec.pred+"\t"+err+"\t"+ID);	
					fwTsv.write(rec.ID+"\t"+rec.exp+"\t"+rec.pred+"\t"+err+"\t"+ID+"\r\n");
				}
				
				
				fwHtml.write("<tr>\r\n");
				
				fwHtml.write("<td valign=top>\r\n");
//				String gsid=lookupGSID(rec.Target,stat);
				
				// String gsid=rec.ID.substring(8,rec.ID.length());

				fwHtml.write("<figure>"); // https://comptox.epa.gov/dashboard-api/ccdapp1/chemical-files/image/by-dtxcid/DTXCID501881
				// fw.write(HtmlUtil.generateImgSrc(rec.ID, outputFolder + "/img"));
//				fw.write("<img src=\""+imgURL+rec.ID+"\" height="+imageWidth+">");
				
				if (rec.ID.contains("DTXCID")) {
					LinkedList<String>list=Parse(rec.ID, "|");
					fwHtml.write("<img src=\""+imgURL+list.get(0)+"\" height="+imageWidth+">");
				} else {
					fwHtml.write("<img src=\""+StructureImageUtil.generateImgSrc(rec.ID)+"\" width="+imageWidth+">");
				}
				
				double err=Math.abs(rec.exp-rec.pred);

				
				fwHtml.write("<figcaption>"+rec.ID+" ("+count+" of "+countTotal+")<br>exp="+df.format(rec.exp)+"<br>predRF="+df.format(rec.pred)+"<br>err="+df.format(err)+"</figcaption>");
				fwHtml.write("</figure>");
				
//				System.out.println(gsid);
				fwHtml.write("</td>\r\n");
				
				
				//***********************************************************************
				fwHtml.write("<td>\r\n");
				
				fwHtml.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\r\n");
												
				fwHtml.write("<tr>");
				
				if (rec.Analogs_Search==null) continue;

				for (int i=0;i<rec.Analogs_Search.size();i++) {
					Analog analog=rec.Analogs_Search.get(i);
					// gsid=analog.ID.substring(8,analog.ID.length());	

					
					fwHtml.write("<td>");
					fwHtml.write("<figure>");
//					 fw.write(HtmlUtil.generateImgSrc(analog.ID, outputFolder + "/img"));
					
					if (analog.ID.contains("DTXCID")) {
						
						if (analog.ID.contains("|")) {
							String [] ids=analog.ID.split("\\|");
							fwHtml.write("<img src=\""+imgURL+ids[0]+"\" height="+imageWidth+">");
						} else {
							fwHtml.write("<img src=\""+imgURL+analog.ID+"\" height="+imageWidth+">");
						}
						
					} else {
						fwHtml.write("<img src=\""+StructureImageUtil.generateImgSrc(analog.ID)+"\" width="+imageWidth+">");
					}
					
					fwHtml.write("<figcaption>"+analog.ID+"("+df.format(analog.sim)+")"+"<br>exp="+df.format(analog.exp)+"<br><br></figcaption>");
					fwHtml.write("</figure></td>");
					
				}		
				fwHtml.write("</tr>");
				fwHtml.write("</table></td>\r\n");

				//***********************************************************************
				fwHtml.write("</tr>\r\n");
			}

			fwHtml.write("</table></html>");
			fwHtml.flush();
			fwHtml.close();
			fwTsv.flush();
			fwTsv.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	static void writeHeader(FileWriter fw, int count,String searchDescription) throws Exception {
		
		fw.write("<html>\r\n");
		
		
		
		
		fw.write("<head><title>"+searchDescription+"</title></head>\r\n");

		
		fw.write("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">\r\n");
		fw.write("<caption>Structure comparisons for "+searchDescription+"</caption>\r\n");
		fw.write("<tr bgcolor=\"#D3D3D3\">");
		
		fw.write("\t<th>Outlier</th>\r\n");
		fw.write("\t<th>Dataset Analogs</th>\r\n");
		
		
		fw.write("</tr>");
	}
	

	
	
	
	
	
	static void findAnalogs(String tsv,Outlier[]outliers) {
		
		try {
			OutlierReportScript or = new OutlierReportScript();
			Instances instances = or.getDatasetFromTsvString(tsv,"\t");
			
			for (Outlier outlier:outliers) {
				Instance instance=instances.instance(outlier.ID);
				outlier.Analogs_Search=findAnalogs2(instance, instances,3, 0.0, true, "cosine");				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static String [] convertStringArrayListToStringArray(ArrayList<String>al) {
		
		String [] strArray=new String [al.size()];
		
		for (int i=0;i<al.size();i++) {
			strArray[i]=al.get(i);
		}
		return strArray;
		
	}
	
	public static Double [] convertStringListToDoubleArray(List<String>al) {
		
		Double [] dblArray=new Double [al.size()];
		
		for (int i=0;i<al.size();i++) {
			if (al.get(i)==null || al.get(i).isEmpty()) dblArray[i]=null;
			else dblArray[i]=Double.parseDouble(al.get(i));
		}
		return dblArray;
		
	}
	
	

	
	/**
	 * Load Instances from input stream
	 * Note: CAS and Tox arent stored in descriptor list
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public Instances getDatasetFromTsvString(String tsv,String del) throws IOException {
		Instances instances=new Instances();
		
        Scanner scanner = new Scanner(tsv);

		String cas = null;
		String header = scanner.nextLine(); //read the first line in the file
		
		ArrayList <String>attributesAL=Parse3toArrayList(header, del);
		attributesAL.remove(0);//remove CAS attribute
		attributesAL.remove(0);//remove Tox attribute
		
		String[]attributes=convertStringArrayListToStringArray(attributesAL);
		
		 
		instances.setAttributes(attributes);
		
		if (instances.instances == null) {
			System.out.println("instances null");
		}
		
		
		while (scanner.hasNext()) {
			String Line=scanner.nextLine();
			
								
//			ArrayList<Double> llDescriptors=LineSplitter.ParseToDoubleArrayListWithTokenizer(Line, del);
//			double []descriptors=ArrayConverter.convertDoubleArrayListToDoubleArray(llDescriptors);

			List<String> llDescriptors=parseLine(Line, del.charAt(0), '"');			
			
			String ID=llDescriptors.remove(0);
			double Tox=Double.parseDouble(llDescriptors.remove(0));
			
			Double []descriptors=convertStringListToDoubleArray(llDescriptors);

			
			Instance instance = new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			/*
			for (int i = 0; i < instance.descriptors.length; i++) {
			System.out.println(instance.descriptors[i]);
			}
			*/
			instance.setName(ID);
			instance.setClassValue(Tox);
		
			instances.addInstance(instance, instances);
			
		}
		
		scanner.close();
		
		instances.calculateMeans();//for convenience
		instances.calculateStdDevs();
		
		return instances;
		
	}
	
	public static List<Analog> findAnalogs2(Instance chemical,Instances trainingSet, int MaxCount,double SCmin, boolean excludeID, String sim) {
		
		Hashtable<Double,Vector<Instance>> ht = new Hashtable<>();//Store instances by similarity, can have multiple instances with same similarity
		
		for (int i = 0; i < trainingSet.numInstances(); i++) {
			Instance chemicali = trainingSet.instance(i);
			String IDi = chemicali.getName();

			if (excludeID) {
				String ID=chemical.getName();
				if (IDi.equals(ID)) {
					continue;
				}
			}
			double SimCoeff=-1;
			
			if (sim.equals("cosine")) {
				SimCoeff=CalculateCosineCoefficient(chemical,chemicali,trainingSet.getMeans(),trainingSet.getStdDevs());	
			} else if (sim.equals("tanimoto")) {
				SimCoeff=CalculateTanimotoCoefficient(chemical,chemicali,trainingSet.getMeans(),trainingSet.getStdDevs());
			}
					
			if (SimCoeff >= SCmin) {
				
				if (ht.get(SimCoeff)==null) {
					Vector<Instance>instances=new Vector<>();
					instances.add(chemicali);
					ht.put(SimCoeff, instances);	
				} else {
					Vector<Instance>instances=ht.get(SimCoeff);
					instances.add(chemicali);
				}
			}
		}

		Vector<Double> v = new Vector(ht.keySet());

		//Sort in descending order (highest similarity first)
		java.util.Collections.sort(v, Collections.reverseOrder());
		
		Enumeration <Double>e = v.elements();
		
		List<Analog> analogs_Search=new ArrayList<>();
		
		int counter = 0;
		
		while (e.hasMoreElements()) {
			double key = e.nextElement();
			
			if (key<SCmin) break;
			
			Vector<Instance> instances = ht.get(key);
			
			for (Instance instance:instances) {
				
				
				Analog analog=new Analog();
				analog.ID=instance.getName();			
				analog.sim=key;
				analog.exp=instance.getClassValue();
				analogs_Search.add(analog);
				counter++;
				if (counter >= MaxCount)
					break;

			}
			if (counter >= MaxCount)
				break;
			
		}
		
		return analogs_Search;

	}
	
	private static double CalculateTanimotoCoefficient(Instance c1,Instance c2,double [] Mean,double [] StdDev) {
		
		double TC=0;
		
		double SumXY=0;
		double SumX2=0;
		double SumY2=0;
		
		for (int j=2;j<c1.numValues();j++) {
       	 	System.out.println(j+"\t"+c1.value(j)+"\t"+c2.value(j));
			
			double xj=c1.value(j);
			double yj=c2.value(j);
				
			if (xj-Mean[j]==0) xj=0;
			else xj=(xj-Mean[j])/StdDev[j];
			System.out.println();
				
			if (yj-Mean[j]==0) yj=0;
			else yj=(yj-Mean[j])/StdDev[j];
						
//			if (this.UseWeights)  {
//				double weight=weights[j];
//				xj*=weight;
//				yj*=weight;
//			}
			
			SumXY+=xj*yj;
			SumX2+=xj*xj;
			SumY2+=yj*yj;

        }
		
		TC=SumXY/(SumX2+SumY2-SumXY);
		
		return TC;
		
	}

	
	private static double CalculateCosineCoefficient(Instance c1,Instance c2,double [] Mean,double [] StdDev) {
		
		double TC=0;
		
		double SumXY=0;
		double SumX2=0;
		double SumY2=0;
		

        for (int i=0;i<c1.numAttributes();i++) {
			double val1=c1.value(i);
			double val2=c2.value(i);
        	
        	// System.out.println(val1+"\t"+val2);
        				
			if (StdDev[i]>0) {
				val1=(val1-Mean[i])/StdDev[i];
				val2=(val2-Mean[i])/StdDev[i];
			} else {
				val1=val1-Mean[i];
				val2=val2-Mean[i];
			}

			
//			if (this.UseWeights)  {
//				double weight=weights[i];
//				val1*=weight;
//				val2*=weight;
//			}
			
			SumXY+=val1*val2;
			SumX2+=val1*val1;
			SumY2+=val2*val2;
        	
        }//end loop over descriptors
        
		
		TC=SumXY/Math.sqrt(SumX2*SumY2);
		
		return TC;
		
	}
	

	
    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = '"';
        }

        if (separators == ' ') {
            separators = ',';
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

	
	public static class QueryOutlierDetectionAPI {

		public static int port=5006; 
		
		public static String callPythonOutlierDetection(String tsv, boolean removeLogP,String server) {
			Unirest.config()
	        .followRedirects(true)   
			.socketTimeout(000)
	           .connectTimeout(000);

			
			String url = server+":"+port+"/calculation/";
			HttpResponse<String> response = Unirest.get(url)
					.queryString("tsv", tsv)
					.queryString("remove_log_p", removeLogP)
					.asString();
			return response.getBody();
		}
	}
		// 
	  /** parses a delimited string into a list- accounts for the fact that can have quotation marks in comma delimited lines
	   * 
	   * @param Line - line to be parsed
	   * @param Delimiter - character used to separate line into fields
	   * @return
	   */
	  public static java.util.ArrayList<String> Parse3toArrayList(String Line, String Delimiter) {


		    java.util.ArrayList<String> myList = new ArrayList<>();

		    int tabpos = 1;

		    while (tabpos > -1) {

		    	tabpos = Line.indexOf(Delimiter);
		    	
		    	if (Line.length()<1) break;
		    	
		    	if (Line.substring(0,1).equals("\"")) {
		    		Line=Line.substring(1,Line.length()); // kill first " mark
		    		
		    		if (Line.length()==0) break;
		    		
		    		myList.add(Line.substring(0, Line.indexOf("\"")));
		    		
		    		if (Line.indexOf("\"")<Line.length()-1)
		    			Line = Line.substring(Line.indexOf("\"") + 2, Line.length());
		    		else 
		    			break;
		    	} else {
					

					if (tabpos > 0) {
						myList.add(Line.substring(0, tabpos));
						Line = Line.substring(tabpos + 1, Line.length());
					} else if (tabpos == 0) {
						myList.add("");
						Line = Line.substring(tabpos + 1, Line.length());
					} else {
						myList.add(Line.trim());
					}

		    	}
		    			
			}// end while loop

		    
//		    for (int j = 0; j <= myList.size() - 1; j++) {
//				System.out.println(j + "\t" + myList.get(j));					
//			}
		    
		    return myList;

		  }

	
	static class RecComparator implements Comparator<Outlier> {
		@Override
		public int compare(Outlier o1, Outlier o2) {
			Double exp1 = o1.exp;
			Double exp2 = o2.exp;
			Double pred1 = o1.pred;
			Double pred2 = o2.pred;
			if (Math.abs(exp1-pred1) - Math.abs(exp2 - pred2) > 0) {
				return -1;
			} else {
			return 1;
			}
		}
	}

	
	public class Instances {
		
		private ArrayList<Instance> instances;//list of instances
		private String [] attributes;//Instance.attributes can share this
		private HashMap<String, Integer> nameToIndex;

		private double [] means;
		private double [] stdDevs;

		// ***************************************************************
		
		public void setAttributes(String [] attributes) {
			this.attributes=attributes;
		}
		
		public Instances () {
			instances=new ArrayList<Instance>();
			nameToIndex = new HashMap<>();
		}

		
		public String []getAttributes() {
			return attributes;
		}
		
		public int numAttributes() {
			return attributes.length;
		}

		public int numInstances() {
			return instances.size();
		}
		
		public Instance instance(int i) {
//			ListIterator<Instance> it=getInstancesIterator();
//			int counter=0;
//			while (it.hasNext()) {
//				Instance instance=it.next();
//				if (counter==i) {
//					return instance;
//				}
//				counter++;
//			}
//			return null;

			return instances.get(i);
		}
		
		public double[] getMeans() {
			return means;
		}

		public double[] getStdDevs() {
			return stdDevs;
		}

		
		public Instance instance(String name) {
		    if (nameToIndex.containsKey(name)) {
		        return instance(nameToIndex.get(name));
		    }
			return null;
		}
		
		


		
		public void addInstance(Instance instance, Instances Jnstances) {
			Jnstances.instances.add(instance);		
			nameToIndex.put(instance.getName(), Jnstances.instances.size()-1);
		}
		
		/**
		 * Calculates means (AKA centroids)
		 * @return
		 */
		public void calculateMeans() { //made public tmm

			means=new double[this.numAttributes()];
					
			for (int i=0;i<numInstances();i++) {
	            for (int j=0;j<numAttributes();j++) {
	            	                     	            
	            	try {
	            		if (instance(i).value(j)==null)continue;
	            		Double val=instance(i).value(j);
	            		means[j] += val;
	            	
	            	} catch (Exception ex) {
	            		System.out.println("Error for instance i="+i+", for val j="+j);
	            	}
	            }
	        }
	        
	        for (int j=0; j<numAttributes(); j++) {
	            means[j] /= this.numInstances();;
	        }
	        
	    }
	    
	    public void calculateStdDevs() { //made public TMM
	        
	    	stdDevs = new double[this.numAttributes()];
	    	
	    	        for (int i=0; i<this.numAttributes(); i++) {
	            stdDevs[i] = 0.0;
	        }
	    	
	    	if (means==null) {
	    		this.calculateMeans();
	    	}
	    	
	    	
	    	//iterate over instances
	    	for (int i=0;i<numInstances();i++) {
	    		for (int j=0;j<numAttributes();j++) {
	    			
	    			try {
	    				if (instance(i).value(j)==null) continue;    				
	    				Double val=instance(i).value(j);
	    				stdDevs[j]+=Math.pow(val-means[j],2);
	    			} catch (Exception ex) {
	    				System.out.println("Error for instance i="+i+", for val j="+j);
	    			}
	    		}
	    		
	    	}
	        
	        for (int i=0; i<this.numAttributes(); i++) {
	        	stdDevs[i]=Math.sqrt(stdDevs[i]/(numInstances()-1));
	        }
	    	
	    }

	}
	
	
	public class Instance {
		

		// private Hashtable <String,Object>htDescriptors;
		private Double [] descriptors;// store all as Strings- faster
													// than storing name as String
													// and rest as Double
		private String [] attributes;// list of attributes in order

		
		private String name;//ID value can be casrn, SID, CID, etc
		private double classValue;//toxicity or physical chemical property (i.e. the dependent variable)
		
		public Instance() {
		}
		public Instance(Instance instance) {
			this.attributes=instance.attributes.clone();
			this.descriptors=instance.descriptors.clone();
			this.name=instance.name;
			this.classValue=instance.classValue;
		}

		public String[] getAttributes() {
			return attributes;
		}

		public int getAttributeNumber(String descriptorName)  {
			
			for (int i=0;i<numAttributes();i++) {
				if (attribute(i).equals(descriptorName)) {
					return i;
				}
			}
			return -1;
		}
		
		public void setAttributes(String [] attributes) {
			this.attributes = attributes;
		}
		
		public int numAttributes() {
			return attributes.length;
		}
		
		public String attribute(int i) {
			return attributes[i];
		}
		
		public void setDescriptors(Double [] llDescriptors) {
			this.descriptors = llDescriptors;
		}

		public void setName(String Name) {
			this.name=Name;
		}

		public void setClassValue(double classValue) {
			this.classValue=classValue;
		}

		public String getName() {
			return name;
		}
		
		public Double value(int i) {
			return descriptors[i];
		}

		public double classValue() {
			return getClassValue();
		}

		public double getClassValue() {
			return classValue;
		}
		
		public int numValues() {
			return attributes.length;
		}


		
		

	}


	
	
	
	
}

package gov.epa.util.wekalite;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVLoader {

	
	/**
	 * Load Instances from input stream
	 * Note: CAS and Tox arent stored in descriptor list
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public Instances getDatasetFromInputStream(InputStream is,String del) throws IOException {
		Instances instances=new Instances();
		
        Scanner scanner = new Scanner(new java.io.InputStreamReader(is));

		String cas = null;
		String header = scanner.nextLine(); //read the first line in the file
		
		ArrayList <String>attributesAL=LineSplitter.Parse3toArrayList(header, del);
		attributesAL.remove(0);//remove CAS attribute
		attributesAL.remove(0);//remove Tox attribute
		
		String[]attributes=ArrayConverter.convertStringArrayListToStringArray(attributesAL);
		
		
		instances.setAttributes(attributes);
		
		while (scanner.hasNext()) {
			String Line=scanner.nextLine();
			
								
//			ArrayList<Double> llDescriptors=LineSplitter.ParseToDoubleArrayListWithTokenizer(Line, del);
//			double []descriptors=ArrayConverter.convertDoubleArrayListToDoubleArray(llDescriptors);

			List<String> llDescriptors=CSVUtils.parseLine(Line, del.charAt(0));			
			
			String ID=llDescriptors.remove(0);
			double Tox=Double.parseDouble(llDescriptors.remove(0));
			
			Double []descriptors=ArrayConverter.convertStringListToDoubleArray(llDescriptors);

			
			Instance instance =new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			instance.setName(ID);
			instance.setClassValue(Tox);
		
			instances.addInstance(instance);
			
		}
		
		scanner.close();
		
		instances.calculateMeans();//for convenience
		instances.calculateStdDevs();
		
		return instances;
		
	}
	
	
	public Instances getDatasetFromInputStreamNoTox(InputStream is,String del) throws IOException {
		
		
		
		Instances instances=new Instances();
		
        Scanner scanner = new Scanner(new java.io.InputStreamReader(is));

		String cas = null;
		String header = scanner.nextLine(); //read the first line in the file
		
		ArrayList <String>attributesAL=LineSplitter.Parse3toArrayList(header, del);
		attributesAL.remove(0);//remove CAS attribute
		
		String[]attributes=ArrayConverter.convertStringArrayListToStringArray(attributesAL);
		
//		for (int i=0;i<attributes.length;i++) {
//			System.out.println(i+"\t"+attributes[i]);
//		}
				
		instances.setAttributes(attributes);		
		
		while (scanner.hasNext()) {
			String Line=scanner.nextLine();
			
//			System.out.println(Line);
			
			if (Line.toLowerCase().indexOf("error")>-1) {
//				System.out.println(Line);
				continue;
			}

			
//			ArrayList<Double> llDescriptors=LineSplitter.ParseToDoubleArrayListWithTokenizer(Line, del);
//			double []descriptors=ArrayConverter.convertDoubleArrayListToDoubleArray(llDescriptors);

			List<String> llDescriptors=CSVUtils.parseLine(Line, del.charAt(0));
			
			String ID=llDescriptors.remove(0);
			
			Double []descriptors=ArrayConverter.convertStringListToDoubleArray(llDescriptors);
			
			Instance instance =new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			instance.setName(ID);
		
			instances.addInstance(instance);
			

			
		}
		scanner.close();
		
		
		instances.calculateMeans();//for convenience
		instances.calculateStdDevs();
		
		return instances;
		
		
	}
	
	
	public static Instances getDatasetFromTESTDescriptorOutput(String filePath,String del) throws IOException {
		Instances instances=new Instances();
		
		BufferedReader br=new BufferedReader(new FileReader(filePath));

		String cas = null;
		String header = br.readLine(); //read the first line in the file
		
		ArrayList <String>attributesAL=LineSplitter.Parse3toArrayList(header, del);
		
		for (int i=1;i<=5;i++)attributesAL.remove(0);
				
		
		String[]attributes=ArrayConverter.convertStringArrayListToStringArray(attributesAL);
		
//		for (String attribute:attributesAL) {
//			System.out.println(attribute);
//		}
						
		instances.setAttributes(attributes);

		
		while (true) {
			String Line=br.readLine();
			
			if (Line==null)	break;
			
//			System.out.println(Line);
			
			ArrayList<String> llDescriptors=LineSplitter.Parse3toArrayList(Line, del);
			
			String ID=llDescriptors.get(1);
			String error=llDescriptors.get(4);
			
//			System.out.println(error);
			
			if (!error.isEmpty()) continue;
			
			for (int i=1;i<=5;i++)llDescriptors.remove(0);
						
			Double [] descriptors=new Double [llDescriptors.size()];
			
//			System.out.println(ID+"\t"+error);
			
			for (int i=0;i<llDescriptors.size();i++) {
				descriptors[i]=Double.parseDouble(llDescriptors.get(i).replace("\"", ""));
			
			}
			
//			System.out.println(ID+"\t"+descriptors[0]+"\t"+descriptors.length);
			
			Instance instance =new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			instance.setName(ID);
		
			instances.addInstance(instance);
			
		}
		
//		instances.printInstances();
		
		br.close();
		
		instances.calculateMeans();//for convenience
		instances.calculateStdDevs();
		
		return instances;
		
	}

	public Instances getDataSetFromFile(String filePath) throws IOException {
		FileInputStream fis=new FileInputStream(filePath);
		return getDatasetFromInputStream(fis,",");
	}

	
	public Instances getDataSetFromFile(String filePath,String del) throws IOException {
		FileInputStream fis=new FileInputStream(filePath);
		return getDatasetFromInputStream(fis,del);
	}
	
	public Instances getDataSetFromFileNoTox(String filePath,String del) throws IOException {
		FileInputStream fis=new FileInputStream(filePath);
		return getDatasetFromInputStreamNoTox(fis,del);
	}
	
	  /**
	   * Method added by TMM to load csv dataset from file in jar file
	   * @return
	   * @throws IOException
	   */
	public Instances getDataSetFromJarFile(String filePathInJar) throws IOException {
		java.io.InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream(filePathInJar);

		return getDatasetFromInputStream(is,",");
	}
	
//	void testWekaLoadTime() {
//		
//		String endpoint="LD50";
//		
//		String folder="C:/Documents and Settings/tmarti02/My Documents/comptox/TEST/T.E.S.T. deployment 4.1/Overall Sets/"+endpoint;
//		String filename=endpoint+"_training_set-2d.csv";
//		
//		for (int i=1;i<=5;i++) {
//		try {
//			long t1=System.currentTimeMillis();
//			QSAR.validation.CSVLoader c= new QSAR.validation2.CSVLoader();
//			c.setSource(new File(folder+"/"+filename));
//			c.getDataSet();
//			long t2=System.currentTimeMillis();
//			System.out.println("Time to load 1st time: "+(t2-t1)/1000.0+" secs");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		}
//	}
	
	void testLoadPadel() {
		
		try {
			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\DataSets\\LLNA\\merged with salts";
			String filename="LLNA descriptors input_PadelDesc.csv";
			CSVLoader c=new CSVLoader();
			Instances instances=c.getDataSetFromFileNoTox(folder+"\\"+filename,",");
			
//			instances.printInstances();
			Instance instance=instances.instance("DTXSID60444582");
			
//			for (int i=0;i<instance.numAttributes();i++) {
//				System.out.println((i+1)+"\t"+instance.attribute(i)+"\t"+instance.value(i));
//			}
			
//			System.out.println(instance.toString());
			
//			Vector<String>baddesc=instances.getDescriptorsWithNullValues();			
//			System.out.println("\n"+baddesc.size()+"\n");
			
//			for (String bad:baddesc) {
//				instances.removeDescriptor(bad);
//			}
			
			instances.removeDescriptorsWithAnyNullValues();
			System.out.println(instances.numAttributes());
			
			
//			for (int i=0;i<instance.numAttributes();i++) {
//				System.out.println((i+1)+"\t"+instance.attribute(i)+"\t"+instance.value(i));
//			}

			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	void testLoad() {

		String endpoint="LD50";
		
		String folder="C:/Documents and Settings/tmarti02/My Documents/comptox/TEST/T.E.S.T. deployment 4.1/Overall Sets/"+endpoint;
		String filename=endpoint+"_training_set-2d.csv";

//		for (int i=1;i<=5;i++) {
		long tstart=System.currentTimeMillis();
		try {
			CSVLoader c=new CSVLoader();
			Instances instances=c.getDataSetFromFile(folder+"/"+filename,",");
			
//			instances.printInstances();
//			instances.printMetaData();
	
//			System.out.println(instance0.stringValue(0));
//			System.out.println(instance0.getName());
//			System.out.println(instance0.getToxicity());
//			System.out.println(instance0.value(2));
//			System.out.println(instance0.value("x0"));
//			System.out.println(instance0.attribute(2));
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		long tend=System.currentTimeMillis();
		System.out.println("Overal time to load: "+(tend-tstart)/1000.0+" secs");
		
//		}//end loop over i
		
	}
	
	public static void main(String [] args) {

		CSVLoader c=new CSVLoader();
//		c.testLoad();
		c.testLoadPadel();
//		c.testWekaLoadTime();
	}
}

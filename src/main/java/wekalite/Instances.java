package wekalite;


import java.util.Hashtable;
import java.util.Vector;
import java.io.ByteArrayInputStream;

//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
//import java.util.ListIterator;//slows things down!
import java.util.HashMap;

public class Instances {
	
	private ArrayList<Instance> instances;//list of instances
	private String [] attributes;//Instance.attributes can share this
	private HashMap<String, Integer> nameToIndex;

	private double [] means;
	private double [] stdDevs;

	// ***************************************************************
	
	
	public static void compare(Instances instances1,Instances instances2) {
		
		//Chemicals in i1 not in i2:
		
		System.out.println("\nPresent in 1 not in 2:");
		for (Instance instance:instances1.instances) {
			if (instances2.instance(instance.getName())==null) {
				System.out.println(instance.getName());
			}
		}
		
		System.out.println("\nPresent in 2 not in 1:");
		for (Instance instance:instances2.instances) {
			if (instances1.instance(instance.getName())==null) {
				System.out.println(instance.getName());
			}
		}
		System.out.println("");
		
		
		System.out.println("Differences for chemicals in common:");
		for (Instance instance:instances1.instances) {
			if (instances2.instance(instance.getName())==null) {
				continue;
			}
			Instance.Compare(instance, instances2.instance(instance.getName()));
		}
		

		
	}
	
	
	/**
	 * @param strTSV
	 * @return
	 * @throws IOException
	 */
	public static wekalite.Instances instancesFromString(String strTSV)  {
		try {
			String del=",";
			if (strTSV.contains("\t")) del="\t";

			InputStream inputStream = new ByteArrayInputStream(strTSV.getFile());

			CSVLoader atf = new CSVLoader();
			Instances dataset=atf.getDatasetFromInputStream(inputStream, del);
			return dataset;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}
	
	
	public void removeDescriptor(String descriptorName) {
		
		int numAttr=getAttributeNumber(descriptorName);
		
		String [] newAttributes=new String [numAttributes()-1];
		
		for (int i=0;i<numAttributes()-1;i++) {
			if (i<numAttr) newAttributes[i]=attribute(i);
			else newAttributes[i]=attribute(i+1);
		}
		
		setAttributes(newAttributes);
		
		for (int j=0;j<numInstances();j++) {
			Instance instance=instance(j);
			instance.setAttributes(newAttributes);
			
			Double [] newValues=new Double [numAttributes()];
			
			for (int i=0;i<numAttributes();i++) {
				if (i<numAttr) newValues[i]=instance.value(i);
				else newValues[i]=instance.value(i+1);
			}			
			instance.setDescriptors(newValues);			
		}
		
	}
	
	public void removeDescriptorsWithAnyNullValues () {
		
		Vector<String>baddesc=getDescriptorsWithNullValues();
		
//		System.out.println("\n"+baddesc.size()+"\n");
		
		for (String bad:baddesc) {
			removeDescriptor(bad);
		}
		
	}
	
	public Vector<String>getDescriptorsWithNullValues() {
		Vector<String>bad=new Vector<>();
		
		for (int i=0;i<numAttributes();i++) {
			
			for (int j=0;j<numInstances();j++) {
			
				Double val=instance(j).value(i);
				
				if (val==null) {
					bad.add(attribute(i));
					break;
				}
				
			}
		}
		
		return bad;
	}
	
	
	public double[] getMeans() {
		return means;
	}

	public double[] getStdDevs() {
		return stdDevs;
	}

	public String []getAttributes() {
		return attributes;
	}
	
	public String getAttributesAsCSVLine() {
		String line="CAS,Tox,";
		for (int k=0;k<numAttributes();k++) {
			String attr=attribute(k);			
			if (attr.contains(",")) attr="\""+attr+"\"";			
			line+=attr;

			if (k<numAttributes()-1) {
				line+=",";
			} 
		}		
		return line;		
	}
	
	public void setAttributes(String [] attributes) {
		this.attributes=attributes;
	}
	
	public int numValues() {
		return numAttributes();
	}
	
	public int numAttributes() {
		return attributes.length;
	}
	public void printMetaData() {
		System.out.println("Number of attributes = "+this.numAttributes());
		System.out.println("Number of instances = "+this.numInstances());
	}
	
	public int numInstances() {
		return instances.size();
	}
	
	public Instances () {
		instances=new ArrayList<Instance>();
		nameToIndex = new HashMap<>();
	}
	
	public ArrayList<Instance> getInstances() {
		return instances;
	}

	public void setInstances(ArrayList<Instance> instances) {
		this.instances = instances;
		this.nameToIndex = new HashMap<>();
		for (int i = 0; i < instances.size(); i++) {
		    nameToIndex.put(instances.get(i).getName(), i);
        }
	}

	public Instances (Instances instances) {
		this.attributes=instances.attributes.clone();
		this.instances=new ArrayList<Instance>();
		this.nameToIndex = new HashMap<>();
		
		for (int i=0;i<instances.numInstances();i++) {
			this.addInstance((Instance)instances.instance(i).clone());
			nameToIndex.put(this.instances.get(i).getName(), i);
		}
	}
	
	public Instances (String [] attributes) {
		this.attributes=attributes;
		this.instances=new ArrayList<Instance>();
        this.nameToIndex = new HashMap<>();
	}
	
	public Instances (Instance instance) {
		this.attributes=instance.getAttributes().clone();
		this.instances=new ArrayList<Instance>();
		this.nameToIndex = new HashMap<>();
		instances.add((Instance)instance.clone());
		nameToIndex.put(instance.getName(), 0);
	}
	
	
	public Instances clone() {
		Instances cloneInstances=new Instances();
		
		cloneInstances.attributes=this.attributes.clone();
		cloneInstances.instances=new ArrayList<Instance>();
		cloneInstances.nameToIndex = new HashMap<>(this.nameToIndex);
		for (int i=0;i<numInstances();i++) {
			cloneInstances.addInstance((Instance)this.instance(i).clone());
			cloneInstances.nameToIndex.put(this.instance(i).getName(), i);
		}
		return cloneInstances;
	}

	
	public static Instances createInstancesFromInstance(Instance instance) {
		Instances instances = new Instances();
		try {
//			instances.attributes=(ArrayList)this.attributes.clone();
//			Instance cloneInstance=(Instance)instance.clone();
//			instances.addInstance(cloneInstance);
			
			instances.attributes=instance.getAttributes();
			instances.addInstance(instance);
			instances.nameToIndex.put(instance.getName(), 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return instances;
	}
	
	/**
	 * Clones instances from one to another
	 * @param instancesDestination
	 * @param instancesSource
	 * @return
	 */
	public static Instances cloneInstances(Instances instancesDestination, Instances instancesSource) {
		for (int k=0;k<instancesSource.numInstances();k++) {
			if (instancesDestination==null) {
				instancesDestination=instancesSource.createInstancesFromInstance((Instance)instancesSource.instance(k).clone());
			} else {
				instancesDestination.addInstance((Instance)instancesSource.instance(k).clone());	
			}
		}
		return instancesDestination;
	}
	
	public Instance instance(String name) {
	    if (nameToIndex.containsKey(name)) {
	        return instance(nameToIndex.get(name));
	    }
		return null;
	}
	
	public int getInstanceNumber(String name) {
        if (nameToIndex.containsKey(name)) {
            return nameToIndex.get(name);
        }
		return -1;
	}
	
	
	public int getAttributeNumber(String descriptorName)  {
//		ListIterator<String> itAtt = getAttributesIterator();
//
//		int counter=0;
//		while (itAtt.hasNext()) {
//			String attribute = itAtt.next();
//
//			if (attribute.equals(descriptorName)) {
//				return counter;
//			}
//			counter++;
//		}
//		return -1;
		
		for (int i=0;i<numAttributes();i++) {
			if (attribute(i).equals(descriptorName)) {
				return i;
			}
		}
		return -1;
	}

	
	public String getDescriptorNames() {
		String names="";
		for (int i=0;i<numAttributes();i++) {
			names+=attribute(i);
			if (i<numAttributes()-1) names+="\t";
		}
		return names;
	}

	public void printInstances() {
		System.out.println("CAS\tTox\t"+getDescriptorNames());
		
//		ListIterator<Instance>it=getInstancesIterator();
//		//Now print values:
//		while (it.hasNext()) {
//			Instance instance=it.next();
//			System.out.println(instance.getDescriptorsValues());			
//		}

		for (int i=0;i<numInstances();i++) {
			System.out.println(instance(i).getInstanceValues());			
		}

	}
	
	@Override
	public String toString() {
		String strInstances="CAS\tTox\t"+getDescriptorNames();
		
//		ListIterator<Instance>it=getInstancesIterator();
//		//Now print values:
//		while (it.hasNext()) {
//			Instance instance=it.next();
//			System.out.println(instance.getDescriptorsValues());			
//		}

		for (int i=0;i<numInstances();i++) {
			strInstances+=instance(i).getInstanceValues()+"\r\n";			
		}
		return strInstances;
		
	}
	
	public void writeToCSVFile(String filePath) {
		
		try {
			FileWriter fw=new FileWriter(filePath);
			
			fw.write(getAttributesAsCSVLine()+"\r\n");
			
			for (int i=0;i<numInstances();i++) {
				fw.write(instance(i).getValuesAsCSVLine()+"\r\n");
				fw.flush();
			}
			
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
//	public void writeToExcel(String outputFilePath,boolean includeClassValue) {
//		
//		try {
//			
//			Workbook wb = new XSSFWorkbook();
//			Sheet recSheet = wb.createSheet("sheet1");
//			
//			Row rowHeader= recSheet.createRow(0);
//			rowHeader.createCell(0).setCellValue("ID");
//			if (includeClassValue) rowHeader.createCell(1).setCellValue("classValue");
//			
//			for (int i=0;i<attributes.length;i++) {
//				if (includeClassValue)	rowHeader.createCell(i+2).setCellValue(attributes[i]);
//				else rowHeader.createCell(i+1).setCellValue(attributes[i]);
//			}
//			
//			for (int j=0;j<numInstances();j++) {
//				Row row= recSheet.createRow(j+1);
//				Instance instance=instances.get(j);				
//				row.createCell(0).setCellValue(instance.getName());
//				if (includeClassValue) row.createCell(1).setCellValue(instance.getClassValue());
//				
//				for (int i=0;i<numAttributes();i++) {
//					if (instance.value(i)==null) continue;
//					if (includeClassValue)	row.createCell(i+2).setCellValue(instance.value(i));
//					else row.createCell(i+1).setCellValue(instance.value(i));
//				}
//			}
//			
//			OutputStream fos = new FileOutputStream(outputFilePath);
//			wb.write(fos);
//			wb.close();
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		
//	}

	

	public Instance instance(int i) {
//		ListIterator<Instance> it=getInstancesIterator();
//		int counter=0;
//		while (it.hasNext()) {
//			Instance instance=it.next();
//			if (counter==i) {
//				return instance;
//			}
//			counter++;
//		}
//		return null;

		return instances.get(i);
	}
	
	public void addInstance(Instance instance) {
		instances.add(instance);		
		nameToIndex.put(instance.getName(), instances.size()-1);
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
	
    public void UnStandardize() { // made public TMM

    	if (means==null || stdDevs==null) {
    		System.out.println("Set means and stddevs first!");
    		return;
    	}
    	//  offset the properties by the mean and scale by standard deviations of all chemicals
    	for (int j=0; j<this.numInstances(); j++) {
            instance(j).UnStandardize(means, stdDevs);
        }
    }

    public void Standardize() { //made public TMM
    	if (means==null || stdDevs==null) {
    		System.out.println("Set means and stddevs first!");
    	}
        
    	for (int i=0;i<numInstances();i++) {
    		instance(i).Standardize(means, stdDevs);
    	}
        
        
    }
    
    public double CalculateRMax() {
    	return CalculateRMax(this.getMeans(),this.getStdDevs());
    }
    
    public double CalculateRMax(double means [],double[] stdDevs) {
        double[] dist = new double[this.numInstances()];
        
        double rMax =0.0;

        for (int i=0;i<numInstances();i++) {
			dist[i] = 0.0;
			for (int j=0;j<numAttributes();j++) {
				if (stdDevs[j] != 0.0) {
					double sumc = (instance(i).value(j) - means[j]) / stdDevs[j];
					dist[i] += sumc * sumc;
				}
			}// end iteration over descriptors
			
			if (Math.sqrt(dist[i]) > rMax)
				rMax = Math.sqrt(dist[i]);
			
        }// end interation over instances
        
		return rMax;
        
        //  Rmax = this.numInstances()/ distmax;
    }
    
    public double calculateAverageClassValue() {
        if (this.numInstances()<=0) return 0;
        
        double avgClassValue = 0;
        for (int i=0;i<numInstances();i++) {
        	avgClassValue += instance(i).getClassValue();        	
        }
        
        avgClassValue /= this.numInstances();
        
        return avgClassValue;
    }
    
	public Instance firstInstance() {
		return instance(0);
	}

	public static Instances createInstances(Hashtable<String,Double> ht,String []attributes) {
		Instances instances=new Instances();
		
		try {
			instances.attributes=(String [])attributes.clone();
			
			Double [] descriptors=new Double[attributes.length];
			for (int i=0;i<attributes.length;i++) {
				descriptors[i]=ht.get(attributes[i]);
			}

			Instance instance =new Instance();
			instance.setAttributes(attributes);
			instance.setDescriptors(descriptors);
			instances.addInstance(instance);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return instances;
	}
	
    public double calculateRMax(double []mean,double[] scales) {
    	
        double[] dist = new double[this.numInstances()];
        double distmax =0.0;

        if (mean.length > 1){

            for (int j=0; j<this.numInstances(); j++) {
                dist [j]= 0.0;
                Instance instance=this.instance(j);
                
                for (int i=0; i<mean.length; i++) {
                    if (scales[i] != 0.0){
                        double sumc =(instance.value(i)- mean[i])/scales[i];
                        dist[j] += sumc*sumc;
                    }
                }
//              this  calculation is based on the harmonic distance in the cluster changed by sdas 5/10/07 
                //           distmax += 1.0/Math.sqrt(dist[j]);
                // This is the selection of the largest distance of a chemical in the cluster.      
                if (  Math.sqrt(dist[j]) > distmax)  distmax = Math.sqrt(dist[j]);

            }
        }
        double rMax= distmax;// this was the highest distance of chemical in cluster
        return rMax;
        
        //  Rmax = this.numInstances()/ distmax;
    }

	
	public String attribute(int i) {
		
		return attributes[i];
	}
	

}

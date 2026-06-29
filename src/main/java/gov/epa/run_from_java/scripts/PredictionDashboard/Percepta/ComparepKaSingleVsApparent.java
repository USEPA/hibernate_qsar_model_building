package gov.epa.run_from_java.scripts.PredictionDashboard.Percepta;

import java.io.FileInputStream;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

/**
* @author TMARTI02
*/
public class ComparepKaSingleVsApparent {
	
	
	void compare() {
		
		String folder="data\\Percepta2025.1.4\\";
		String filename="pKa_QR.sdf";
		String filepathInput = folder + filename;

		try {

			IteratingSDFReader mr = new IteratingSDFReader(new FileInputStream(filepathInput),
					DefaultChemObjectBuilder.getInstance());

//			String filepathOut = "data\\episuite\\episuite validation\\LogKow\\LogKow episuite training set.txt";


			int counter = 0;
			
			boolean doAcidic=false;
			
			if (doAcidic) {
				System.out.println("counter\tcid\texp_pKa_a\tpred_pKa_a_Apparent\tpred_pKa_a_Single");
			} else {
				System.out.println("counter\tcid\texp_pKa_b\tpred_pKa_b_Apparent\tpred_pKa_b_Single");
			}
			int count2=0;
			

			while (mr.hasNext()) {
				counter++;
				IAtomContainer m = mr.next();

				String cid = m.getProperty("DSSTox_Structure_Id");
				
				String strpKa_a = m.getProperty("pKa_a");
				String strpKa_b = m.getProperty("pKa_b");
				
				Double exp_pKa_a= null;
				Double exp_pKa_b= null;
				
				if (!strpKa_a.equals("NaN")) {
					exp_pKa_a=Double.parseDouble(strpKa_a);
				}
				
				if (!strpKa_b.equals("NaN")) {
					exp_pKa_b=Double.parseDouble(strpKa_b);
				}
				
				String ACD_pKa_Single_1 = m.getProperty("ACD_pKa_Single_1");
				String ACD_pKa_Single_2 = m.getProperty("ACD_pKa_Single_2");

				String ACD_pKa_DissType_Single_1=m.getProperty("ACD_pKa_DissType_Single_1");
				String ACD_pKa_DissType_Single_2=m.getProperty("ACD_pKa_DissType_Single_2");

				String ACD_pKa_Apparent_1 = m.getProperty("ACD_pKa_Apparent_1");
				String ACD_pKa_Apparent_2 = m.getProperty("ACD_pKa_Apparent_2");
				
				
				String ACD_pKa_DissType_Apparent_1=m.getProperty("ACD_pKa_DissType_Apparent_1");
				String ACD_pKa_DissType_Apparent_2=m.getProperty("ACD_pKa_DissType_Apparent_2");
				
				
				Double pred_pKa_a_Single=null;
				Double pred_pKa_b_Single=null;
				
				if (ACD_pKa_DissType_Single_1!=null && ACD_pKa_Single_1!=null ) {
					if(ACD_pKa_DissType_Single_1.equals("MA")) {
						pred_pKa_a_Single=Double.parseDouble(ACD_pKa_Single_1);
					} else if (ACD_pKa_DissType_Single_1.equals("MB")) {
						pred_pKa_b_Single=Double.parseDouble(ACD_pKa_Single_1);
					} else {
						System.out.println(cid+"\t"+ACD_pKa_DissType_Single_1);//doesnt happen
					}
				} 
				
				
				if (ACD_pKa_DissType_Single_2!=null && ACD_pKa_Single_2!=null ) {
					if(ACD_pKa_DissType_Single_2.equals("MA")) {
						pred_pKa_a_Single=Double.parseDouble(ACD_pKa_Single_2);
					} else if (ACD_pKa_DissType_Single_2.equals("MB")) {
						pred_pKa_b_Single=Double.parseDouble(ACD_pKa_Single_2);
					} else {
						System.out.println(cid+"\t"+ACD_pKa_DissType_Single_1);//doesnt happen
					}
				}
				
				
				Double pred_pKa_a_Apparent=null;
				Double pred_pKa_b_Apparent=null;

				if (ACD_pKa_DissType_Apparent_1!=null && ACD_pKa_Apparent_1!=null ) {
					if(ACD_pKa_DissType_Apparent_1.equals("MA")) {
						pred_pKa_a_Apparent=Double.parseDouble(ACD_pKa_Apparent_1);
					} else if (ACD_pKa_DissType_Apparent_1.equals("MB")) {
						pred_pKa_b_Apparent=Double.parseDouble(ACD_pKa_Apparent_1);
					} else {
						System.out.println(cid+"\t"+ACD_pKa_DissType_Apparent_1);//doesnt happen
					}
				} 
				
				
				if (ACD_pKa_DissType_Apparent_2!=null && ACD_pKa_Apparent_2!=null ) {
					if(ACD_pKa_DissType_Apparent_2.equals("MA")) {
						pred_pKa_a_Apparent=Double.parseDouble(ACD_pKa_Apparent_2);
					} else if (ACD_pKa_DissType_Apparent_2.equals("MB")) {
						pred_pKa_b_Apparent=Double.parseDouble(ACD_pKa_Apparent_2);
					} else {
						System.out.println(cid+"\t"+ACD_pKa_DissType_Apparent_1);//doesnt happen
					}
				}

				if (doAcidic) {
					if(exp_pKa_a!=null && pred_pKa_a_Apparent!=null) {
						if (Math.abs(pred_pKa_a_Apparent-pred_pKa_a_Single)>0.01) {
							count2++;
							System.out.println(count2+"\t"+cid+"\t"+exp_pKa_a+"\t"+pred_pKa_a_Apparent+"\t"+pred_pKa_a_Single);
						}
					}
				} else {
					if(exp_pKa_b!=null && pred_pKa_b_Apparent!=null) {
						if (Math.abs(pred_pKa_b_Apparent-pred_pKa_b_Single)>0.01) {
							count2++;
							System.out.println(count2+"\t"+cid+"\t"+exp_pKa_b+"\t"+pred_pKa_b_Apparent+"\t"+pred_pKa_b_Single);
						}
					}
					
				}


				//				System.out.println(CAS+"\t"+Train);
			}

			//				System.out.println("CountT=" + countT);
			//				System.out.println("CountOther=" + countOther);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ComparepKaSingleVsApparent c=new ComparepKaSingleVsApparent();
		c.compare();

	}

}

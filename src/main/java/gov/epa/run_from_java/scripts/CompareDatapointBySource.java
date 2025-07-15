package gov.epa.run_from_java.scripts;

import java.awt.FlowLayout;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;


import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.util.MatlabChart;

/**
* @author TMARTI02
*/
public class CompareDatapointBySource {
	
	
	class DataPoints extends ArrayList<DataPoint> {
		
		Double medianValue;
		
	}

	TreeMap<String,DataPoints> getDataPointsByPublicSource(String datasetName,String publicSourceName) {

		String sql="select dp.canon_qsar_smiles, dpc.property_value from qsar_datasets.datasets d\r\n"
				+ "		join qsar_datasets.data_points dp on d.id = dp.fk_dataset_id\r\n"
				+ "		join qsar_datasets.data_point_contributors dpc on dp.id = dpc.fk_data_point_id\r\n"
				+ "		join exp_prop.property_values pv on pv.id=dpc.exp_prop_property_values_id\r\n"
				+ "		join exp_prop.public_sources ps on pv.fk_public_source_id = ps.id\r\n"
				+ "		where d.name='"+datasetName+"'\r\n"
				+ "		and ps.name='"+publicSourceName+"'\r\n"
				+ "		order by dp.canon_qsar_smiles;";

		
		try {
			
			ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionPostgres(), sql);
			
			TreeMap<String,DataPoints>htDPs=new TreeMap<>();
									
			while (rs.next()) {
				
				String qsarSmiles=rs.getString(1);
				Double exp=rs.getDouble(2);
				
				
				if(exp>15) {
//					System.out.println(qsarSmiles);
					continue;//bad logKow
				}
				
				DataPoint dp=new DataPoint();
				dp.setCanonQsarSmiles(qsarSmiles);
				dp.setQsarPropertyValue(exp);
				
				if(htDPs.containsKey(qsarSmiles)) {
					DataPoints dps=htDPs.get(qsarSmiles);
					dps.add(dp);
					
				} else {
					DataPoints dps=new DataPoints();
					dps.add(dp);
					htDPs.put(qsarSmiles,dps);
				}
			}
			setMedianValues(htDPs);
//			System.out.println(Utilities.gson.toJson(htDPs));
			return htDPs;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		
		
	}
	
	void setMedianValues(TreeMap<String,DataPoints> tm) {
		int count=0;
	
		for (String key:tm.keySet()) {
			DataPoints recs=tm.get(key);
			setMedianValue(recs);
			count+=recs.size();
		}
	
	}
	
	private void setMedianValue(DataPoints dps) {
		
		List<Double>vals=new ArrayList<>();
	
		for (DataPoint dp:dps) {
			vals.add(dp.getQsarPropertyValue());
		}
	
		if (vals.size()>0) {
			setMedianValue(dps,vals);	
		}
		
	}
	
	private void setMedianValue(DataPoints recs, List<Double> vals) {
		//		System.out.println(recs.get(0).casrn+"\t"+vals.size());
		if(vals.size()%2==0) {// even
			int middleVal2=vals.size()/2;
			int middleVal1=middleVal2-1;
			recs.medianValue=(vals.get(middleVal1)+vals.get(middleVal2))/2.0;
		} else {//odd
			int middleVal=vals.size()/2;
			recs.medianValue=vals.get(middleVal);
		}
	}

	
	
	
	void compare(String sourceName1,String sourceName2,String datasetName) {
		
		TreeMap<String,DataPoints>tm1=getDataPointsByPublicSource(datasetName, sourceName1);
		TreeMap<String,DataPoints>tm2=getDataPointsByPublicSource(datasetName, sourceName2);
		
		System.out.println("\ncount In "+sourceName1+" not in "+sourceName2+"="+getNewChemicalCount(tm1, tm2));
		System.out.println("count In "+sourceName2+" not in "+sourceName1+"="+getNewChemicalCount(tm2, tm1));
	
		System.out.println(sourceName1+"\t"+tm1.size());
		System.out.println(sourceName2+"\t"+tm2.size());
		
		String units=DevQsarConstants.LOG_UNITS;

		int countInCommon=0;
		double MAE=0;

		DecimalFormat df=new DecimalFormat("0.00");


//		if(printChemicalsInCommon) System.out.println("\nLogType\tkey\tLog10median_1\tLog10median_2\tdiff");

		List<Double>vals1=new ArrayList<>();
		List<Double>vals2=new ArrayList<>();
		
//		boolean printChemicalsInCommon=true;
		boolean printChemicalsInCommon=false;


		for (String key:tm1.keySet()) {
			DataPoints recs1=tm1.get(key);

			if(!tm2.containsKey(key))continue;
			
			DataPoints recs2=tm2.get(key);
			
			if(recs1.medianValue!=null && recs2.medianValue!=null) {
				
				Double error=Math.abs(recs1.medianValue-recs2.medianValue);
				vals1.add(recs1.medianValue);
				vals2.add(recs2.medianValue);

				if(printChemicalsInCommon) {
					System.out.println(key+"\t"+df.format(recs1.medianValue)+"\t"+df.format(recs2.medianValue)+"\t"+df.format(error));					
				}

				MAE+=error;
				countInCommon++;

			} 
		}
		
		
		createPlot(sourceName1,sourceName2, units, vals1, vals2);

		MAE/=countInCommon;
		System.out.println("Count in common="+countInCommon);
		System.out.println("MAE="+MAE);

		
	}
	
	
	public void createPlot(String sourceName1,String sourceName2, String units, List<Double> vals1, List<Double> vals2) {
	
		MatlabChart fig = new MatlabChart(); // figure('Position',[100 100 640 480]);
		fig.plot(vals1, vals2, "-r", 2.0f, "data"); // plot(x,y1,'-r','LineWidth',2);
		fig.plot(vals1, vals1, "-k", 2.0f, "Y=X"); // plot(x,y1,'-r','LineWidth',2);

		//        fig.plot(x, y2, ":k", 3.0f, "BAC");  // plot(x,y2,':k','LineWidth',3);
	
		fig.RenderPlot();                    // First render plot before modifying
		fig.title(sourceName1+" vs. "+sourceName2);    // title('Stock 1 vs. Stock 2');
		//      fig.xlim(10, 100);                   // xlim([10 100]);
		//      fig.ylim(200, 300);                  // ylim([200 300]);
	
	
		//TODO for some properties it wont be logged units in labels
	
		fig.xlabel("exp source 1 "+units);                  // xlabel('Days');
		fig.ylabel("exp source 2 "+units);                 // ylabel('Price');
		fig.grid("on","on");                 // grid on;
		fig.legend("southeast");             // legend('AAPL','BAC','Location','northeast')
		fig.font("Helvetica",15);            // .. 'FontName','Helvetica','FontSize',15
		//      fig.saveas("MyPlot.jpeg",640,480);   // saveas(gcf,'MyPlot','jpeg');
	
		XYLineAndShapeRenderer xy=(XYLineAndShapeRenderer) fig.chart.getXYPlot().getRenderer();

		xy.setSeriesShapesVisible(0, true);
		xy.setSeriesLinesVisible(0, false);

		xy.setSeriesShapesVisible(1, false);
		xy.setSeriesLinesVisible(1, true);

	
		ChartPanel cp=new ChartPanel(fig.chart);
	
		JFrame jframe=new JFrame();
		jframe.add(cp);
		cp.setLayout(new FlowLayout(FlowLayout.LEFT));
	
		jframe.setSize(500,500);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
	}
	
	int getNewChemicalCount(TreeMap<String,DataPoints>tm1,TreeMap<String,DataPoints>tm2) {

		int countIn1Not2=0;


		for (String key:tm1.keySet()) {
			DataPoints recs1=tm1.get(key);

			if(!tm2.containsKey(key) && recs1.medianValue!=null) {
				countIn1Not2++;
				continue;
			}

			DataPoints recs2=tm2.get(key);

			if(recs1.medianValue!=null && recs2.medianValue==null) {
				countIn1Not2++;
			}
		}


		return countIn1Not2;

	}
	
	
	
	public static void main(String[] args) {
		
		CompareDatapointBySource c=new CompareDatapointBySource();
		
		c.compare("OPERA2.8","PubChem_2024_11_27" ,"exp_prop_LOG_KOW_v2.0");
		c.compare("OPERA2.8","eChemPortalAPI" ,"exp_prop_LOG_KOW_v2.0");
//		c.compare("PubChem_2024_11_27","eChemPortalAPI" ,"exp_prop_LOG_KOW_v2.0");

		
//		eChemPortalAPI
//		PubChem_2024_11_27


	}

}

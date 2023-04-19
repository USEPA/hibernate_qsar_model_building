package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.PredictionDashboard;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionDashboardServiceImpl;

public class PredictionDashboardLoadingScript {

	ModelServiceImpl ms = new ModelServiceImpl();
	PredictionDashboardServiceImpl pdsi = new PredictionDashboardServiceImpl();
	
	
	public static void main(String[] args) {
		PredictionDashboardLoadingScript script = new PredictionDashboardLoadingScript();
		Model model = script.ms.findById(1842L);

		List<PredictionDashboard> pd1 = script.loadPredictionDashboards(model, 4);
		List<PredictionDashboard> pd2 = script.loadPredictionDashboards(model, 2);
		List<PredictionDashboard> pd3 = script.loadPredictionDashboards(model, 3);
		
		long startTime = System.currentTimeMillis();

		script.pdsi.createBatch(pd3);

		System.out.println("time elapsed hibernate=" + (System.currentTimeMillis() - startTime));
		
		startTime = System.currentTimeMillis();
		
		script.pdsi.createSQL(pd2);

		System.out.println("time elapsed jdbc=" + (System.currentTimeMillis() - startTime));

		startTime = System.currentTimeMillis();
		for (int i = 0; i < pd1.size(); i++) {
			script.pdsi.create(pd1.get(i));
		}
		
		System.out.println("time elapsed regular create=" + (System.currentTimeMillis() - startTime));

	}
	
	public ArrayList<PredictionDashboard> loadPredictionDashboards(Model model, int k) {
		ArrayList<PredictionDashboard> pds = new ArrayList<PredictionDashboard>();
		for (int i = 0; i < 2000; i++) {
			PredictionDashboard p = new PredictionDashboard();
			p.setModel(model);
			p.setCanonQsarSmiles("CCC");
			p.setSmiles("CCC");
			p.setDtxsid("DTXSID3039242");
			p.setPredictionValue(2.0);
			p.setPredictionError("warning");
			p.setDtxcid("cid" + k + i);
			p.setCreatedBy("cramslan");
			pds.add(p);
			
		}
		return pds;
	}

}

package gov.epa.endpoints.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dsstox.DsstoxRecord;
import gov.epa.databases.dsstox.service.DsstoxCompoundService;
import gov.epa.databases.dsstox.service.DsstoxCompoundServiceImpl;
import kong.unirest.Unirest;

public class ReportGenerator {
	private CompoundService compoundService;
	private DsstoxCompoundService dsstoxCompoundService;
	
	public ReportGenerator() {
		// Set logging providers for Hibernate and MChange
		System.setProperty("org.jboss.logging.provider", "log4j");
		System.setProperty("com.mchange.v2.log.MLog", "log4j");
		
		// Reduce logging output from Apache, Hibernate, and C3P0
		String[] loggerNames = {"org.apache.http", "org.hibernate", "com.mchange"};
//		for (String loggerName:loggerNames) {
//			Logger thisLogger = LogManager.getLogger(loggerName);
//			thisLogger.setLevel(Level.ERROR);
//		}
		
		// Make sure Unirest is configured
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception e) {
			// Ignore warning if already configured
		}

		compoundService = new CompoundServiceImpl();
		dsstoxCompoundService = new DsstoxCompoundServiceImpl();
	}
	
	public void addOriginalCompounds(List<? extends ReportDataPoint> reportDataPoints) {
		Set<String> allDtxcids = new HashSet<String>();
		Map<String, Set<String>> mapDtxcidsByCanonQsarSmiles = new HashMap<String, Set<String>>();
		for (ReportDataPoint dp:reportDataPoints) {
			List<Compound> compounds = compoundService.findByCanonQsarSmiles(dp.canonQsarSmiles);
			if (compounds!=null) {
				Set<String> dtxcids = compounds.stream().map(c -> c.getDtxcid()).collect(Collectors.toSet());
				allDtxcids.addAll(dtxcids);
				mapDtxcidsByCanonQsarSmiles.put(dp.canonQsarSmiles, dtxcids);
			}
		}
		
		List<DsstoxRecord> dsstoxRecords = getDsstoxRecordsFromDtxcids(allDtxcids);
		Map<String, DsstoxRecord> mapDsstoxRecords = new HashMap<String, DsstoxRecord>();
		for (DsstoxRecord dr:dsstoxRecords) {
			if (allDtxcids.remove(dr.dsstoxCompoundId)) {
				mapDsstoxRecords.put(dr.dsstoxCompoundId, dr);
			}
		}
		
		for (ReportDataPoint dp:reportDataPoints) {
			Set<String> dtxcids = mapDtxcidsByCanonQsarSmiles.get(dp.canonQsarSmiles);
			for (String dtxcid:dtxcids) {
				DsstoxRecord dr = mapDsstoxRecords.get(dtxcid);
				if (dr!=null) {
					dp.originalCompounds.add(new OriginalCompound(dtxcid, dr.casrn, dr.preferredName, dr.smiles, dr.molWeight));
				} else {
					System.out.println("DSSTox record not found for DTXCID: " + dtxcid);
				}
			}
		}
	}
	
	private List<DsstoxRecord> getDsstoxRecordsFromDtxcids(Collection<String> dtxcids) {
		List<DsstoxRecord> dsstoxRecords = new ArrayList<DsstoxRecord>();
		if (dtxcids.size() <= 1000) {
			dsstoxRecords = dsstoxCompoundService.findAsDsstoxRecordsByDtxcidIn(dtxcids);
		} else {
			Set<String> subset = new HashSet<String>();
			for (String dtxcid:dtxcids) {
				subset.add(dtxcid);
				if (subset.size()==1000) {
					dsstoxRecords.addAll(getDsstoxRecordsFromDtxcids(subset));
					subset = new HashSet<String>();
				}
			}
			dsstoxRecords.addAll(getDsstoxRecordsFromDtxcids(subset));
		}
		
		return dsstoxRecords;
	}

}

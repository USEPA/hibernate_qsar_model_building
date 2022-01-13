package gov.epa.endpoints.reports;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
		for (String loggerName:loggerNames) {
			Logger thisLogger = LogManager.getLogger(loggerName);
			thisLogger.setLevel(Level.ERROR);
		}
		
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
		
		List<DsstoxRecord> dsstoxRecords = dsstoxCompoundService.findDsstoxRecordsByDtxcidIn(allDtxcids);
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
					dp.originalCompounds.add(new OriginalCompound(dtxcid, dr.casrn, dr.preferredName, dr.smiles));
				} else {
					System.out.println("DSSTox record not found for DTXCID: " + dtxcid);
				}
			}
		}
	}

}

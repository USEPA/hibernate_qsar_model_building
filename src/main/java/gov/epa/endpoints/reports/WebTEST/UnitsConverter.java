package gov.epa.endpoints.reports.WebTEST;

import gov.epa.databases.dev_qsar.DevQsarConstants;

public class UnitsConverter {

	public static Double getToxValMassFromLog(String unitsLog, double toxValMolar, double MW) {
		
		if (unitsLog.equals(DevQsarConstants.NEG_LOG_M) || unitsLog.equals(DevQsarConstants.NEG_LOG_MOL_KG)) {
			return Math.pow(10, -toxValMolar) * MW * 1000.0;// mg/L units
		} else if (unitsLog.equals(DevQsarConstants.LOG_M)) {
			return Math.pow(10, toxValMolar) * MW * 1000.0;// mg/L units
		} else if (unitsLog.equals(DevQsarConstants.LOG_L_KG) 
				|| unitsLog.equals(DevQsarConstants.LOG_DAYS)
				|| unitsLog.equals(DevQsarConstants.LOG_HR)
				|| unitsLog.equals(DevQsarConstants.LOG_MMHG)
				|| unitsLog.equals(DevQsarConstants.LOG_ATM_M3_MOL)
				|| unitsLog.equals(DevQsarConstants.LOG_CM3_MOLECULE_SEC)) {
			return Math.pow(10, toxValMolar);
		} else if (unitsLog.equals(DevQsarConstants.NEG_LOG_ATM_M3_MOL)
				|| unitsLog.equals(DevQsarConstants.NEG_LOG_MMHG)) {
			return Math.pow(10, -toxValMolar);// non log units
		} else {
			return null;
		}
			
	}

	static String getMassUnits(String qsarUnits) {
		
		if (qsarUnits.equals(DevQsarConstants.NEG_LOG_M) || qsarUnits.equals(DevQsarConstants.LOG_M)) {
			return DevQsarConstants.MG_L;		
		} else if(qsarUnits.equals(DevQsarConstants.LOG_CM3_MOLECULE_SEC)) {
			return DevQsarConstants.CM3_MOLECULE_SEC;
		} else if (qsarUnits.equals(DevQsarConstants.NEG_LOG_MOL_KG)) {
			return DevQsarConstants.MG_KG;
		} else if (qsarUnits.equals(DevQsarConstants.LOG_L_KG)) {
			return DevQsarConstants.L_KG;
		} else if (qsarUnits.equals(DevQsarConstants.LOG_HR)) {
			return DevQsarConstants.HOUR;
		} else if (qsarUnits.equals(DevQsarConstants.LOG_DAYS)) {
			return DevQsarConstants.DAYS;
		} else if (qsarUnits.equals(DevQsarConstants.NEG_LOG_ATM_M3_MOL) || qsarUnits.equals(DevQsarConstants.LOG_ATM_M3_MOL)) {
			return DevQsarConstants.ATM_M3_MOL;
		} else if (qsarUnits.equals(DevQsarConstants.NEG_LOG_MMHG) || qsarUnits.equals(DevQsarConstants.LOG_MMHG)) {
			return DevQsarConstants.MMHG;
		} else if (qsarUnits.equals(DevQsarConstants.LOG_ATM_M3_MOL)) {
			return DevQsarConstants.ATM_M3_MOL;
		} else {
			return null;
		}
	}

}

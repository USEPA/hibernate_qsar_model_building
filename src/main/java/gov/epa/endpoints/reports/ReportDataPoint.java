package gov.epa.endpoints.reports;

import java.util.ArrayList;
import java.util.List;

public class ReportDataPoint {
	public String canonQsarSmiles;
	public List<OriginalCompound> originalCompounds = new ArrayList<OriginalCompound>();
	public String qsar_dtxcid;
	public String qsar_exp_prop_property_values_id;
	
}

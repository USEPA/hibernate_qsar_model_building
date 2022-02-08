package gov.epa.endpoints.datasets.dsstox_mapping;

import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;

public class DsstoxMapperStringUtil {
	
	private static final String[] UVCB_KEYWORDS = {
			"reaction",
			"salts",
			"molecule",
			"gmt",
			"petroleum",
			"mass",
			"products",
			"hydrocarbons",
			"fatty",
			"light",
			"oil",
			"distillates",
			"unsaturated",
			"fraction",
			"mix",
			"derivs",
			"ethoxylated",
			"isomers",
			"branched",
			"steamcracked",
			"even",
			"numbered",
			"hydrogenated",
			"alkenes",
			"esterification"
	};

	/**
	 * Check if chemical name contains any of a list of UVCB keywords compiled by Charlie Lowe
	 * @param name	the chemical name to check
	 * @return		true if name contains any keyword, false else
	 */
	static boolean hasUvcbKeywords(String name) {
		if (name==null) {
			return false;
		} else {
			for (String keyword:UVCB_KEYWORDS) {
				if (name.toLowerCase().contains(keyword)) {
					return true;
				}
			}
		}
		
		return false;
	}

	static boolean lenientChemicalNameMatch(String bin) {
		return bin.contains(DsstoxMapper.PREFERRED_NAME_MATCH) 
				|| bin.contains(DsstoxMapper.VALID_SYNONYM_MATCH)
				|| bin.contains(DsstoxMapper.UNIQUE_SYNONYM_MATCH)
				|| bin.contains(DsstoxMapper.AMBIGUOUS_SYNONYM_MATCH)
				|| bin.contains(DsstoxMapper.NAME2STRUCTURE_MATCH)
				|| bin.contains(DsstoxMapper.MAPPED_IDENTIFIER_MATCH);
	}

	static boolean strictChemicalNameMatch(String bin) {
		return bin.contains(DsstoxMapper.PREFERRED_NAME_MATCH) 
				|| bin.contains(DsstoxMapper.VALID_SYNONYM_MATCH)
				|| bin.contains(DsstoxMapper.UNIQUE_SYNONYM_MATCH);
	}

	static boolean hasDelimiters(SourceChemical sc) {
		String casrn = sc.getSourceCasrn();
		String dtxcid = sc.getSourceDtxcid();
		String dtxsid = sc.getSourceDtxsid();
		return (casrn!=null && (casrn.contains("|") || casrn.contains(";")))
				|| (dtxcid!=null && (dtxcid.contains("|") || dtxcid.contains(";")))
				|| (dtxsid!=null && (dtxsid.contains("|") || dtxsid.contains(";")));
	}

	static String cleanDelimiters(String str, boolean isDsstoxId) {
		if (str==null) {
			return null;
		} 
		
		str = str.replaceAll("; ?", "|");
		if (isDsstoxId) {
			return str.replaceAll("[^DTXSCRID0-9\\|]", "");
		} else {
			return str.replaceAll("[^0-9\\|-]", "");
		}
	}

	static boolean hasMappingConflict(String bin) {
		return bin.contains(DsstoxMapper.CASRN_CONFLICT)
				|| bin.contains(DsstoxMapper.PREFERRED_NAME_CONFLICT)
				|| hasSynonymConflict(bin)
				|| bin.contains(DsstoxMapper.NAME2STRUCTURE_CONFLICT)
				|| bin.contains(DsstoxMapper.MAPPED_IDENTIFIER_CONFLICT)
				|| bin.contains(DsstoxMapper.STRUCTURE_CONFLICT);
	}

	static boolean hasSynonymConflict(String bin) {
		return bin.contains(DsstoxMapper.VALID_SYNONYM_CONFLICT)
				|| bin.contains(DsstoxMapper.UNIQUE_SYNONYM_CONFLICT)
				|| bin.contains(DsstoxMapper.AMBIGUOUS_SYNONYM_CONFLICT);
	}

}

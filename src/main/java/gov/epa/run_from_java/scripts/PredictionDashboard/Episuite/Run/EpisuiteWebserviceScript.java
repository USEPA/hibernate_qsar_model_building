package gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteResults.ChemicalProperties;
import gov.epa.run_from_java.scripts.PredictionDashboard.Episuite.Run.EpisuiteValidation.DataPointCAS;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * @author TMARTI02
 */
public class EpisuiteWebserviceScript {


	public EpisuiteWebserviceScript() {
		try {
			Unirest.config().followRedirects(true).socketTimeout(000).connectTimeout(000);
		} catch (Exception ex) {
//			ex.printStackTrace();
		}
	}


	String sampleOutput="{\r\n"
			+ "    \"parameters\": {\r\n"
			+ "        \"cas\": null,\r\n"
			+ "        \"smiles\": \"CCO\",\r\n"
			+ "        \"caseNumber\": null,\r\n"
			+ "        \"userLogKow\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userMeltingPoint\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"celsius\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userBoilingPoint\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"celsius\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userWaterSolubility\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"mg/L\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userVaporPressure\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"mmHg\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userHenrysLawConstant\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"atm-m3/mol\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userLogKoa\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userLogKoc\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"L/kg\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userHydroxylReactionRateConstant\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"cm3/molecule-sec\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userDermalPermeabilityCoefficient\": {\r\n"
			+ "            \"value\": -999.0,\r\n"
			+ "            \"units\": \"cm/hr\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userAtmosphericHydroxylRadicalConcentration\": {\r\n"
			+ "            \"value\": 1500000.0,\r\n"
			+ "            \"units\": \"radicals/cm3\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userAtmosphericOzoneConcentration\": {\r\n"
			+ "            \"value\": 7.0E11,\r\n"
			+ "            \"units\": \"molecules/cm3\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userAtmosphericDaylightHours\": {\r\n"
			+ "            \"value\": 12.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userStpHalfLifePrimaryClarifier\": {\r\n"
			+ "            \"value\": 10000.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userStpHalfLifeAerationVessel\": {\r\n"
			+ "            \"value\": 10000.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userStpHalfLifeSettlingTank\": {\r\n"
			+ "            \"value\": 10000.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityHalfLifeAir\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityHalfLifeWater\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityHalfLifeSoil\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityHalfLifeSediment\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityEmissionRateAir\": {\r\n"
			+ "            \"value\": 1000.0,\r\n"
			+ "            \"units\": \"kg/hour\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityEmissionRateWater\": {\r\n"
			+ "            \"value\": 1000.0,\r\n"
			+ "            \"units\": \"kg/hour\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityEmissionRateSoil\": {\r\n"
			+ "            \"value\": 1000.0,\r\n"
			+ "            \"units\": \"kg/hour\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityEmissionRateSediment\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"kg/hour\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityAdvectionTimeAir\": {\r\n"
			+ "            \"value\": 100.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityAdvectionTimeWater\": {\r\n"
			+ "            \"value\": 1000.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityAdvectionTimeSoil\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        },\r\n"
			+ "        \"userFugacityAdvectionTimeSediment\": {\r\n"
			+ "            \"value\": 50000.0,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"chemicalProperties\": {\r\n"
			+ "        \"name\": \"ETHANOL\",\r\n"
			+ "        \"systematicName\": \"Ethanol\",\r\n"
			+ "        \"cas\": \"000064-17-5\",\r\n"
			+ "        \"smiles\": \"OCC\",\r\n"
			+ "        \"molecularWeight\": 46.06852124410014,\r\n"
			+ "        \"molecularFormula\": \"C2H6O\",\r\n"
			+ "        \"molecularFormulaHtml\": \"C<sub>2</sub>H<sub>6</sub>O\",\r\n"
			+ "        \"organic\": true,\r\n"
			+ "        \"organicAcid\": false,\r\n"
			+ "        \"aminoAcid\": false,\r\n"
			+ "        \"nonStandardMetal\": false,\r\n"
			+ "        \"flags\": null\r\n"
			+ "    },\r\n"
			+ "    \"logKow\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\"\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"logKow\": -0.1411999762058258,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH3    [aliphatic carbon]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.5472999811172485,\r\n"
			+ "                        \"contribution\": 0.5472999811172485,\r\n"
			+ "                        \"trainingCount\": 13,\r\n"
			+ "                        \"validationCount\": 20\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH2-   [aliphatic carbon]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.491100013256073,\r\n"
			+ "                        \"contribution\": 0.491100013256073,\r\n"
			+ "                        \"trainingCount\": 18,\r\n"
			+ "                        \"validationCount\": 28\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-OH     [hydroxy, aliphatic attach]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": -1.4085999727249146,\r\n"
			+ "                        \"contribution\": -1.4085999727249146,\r\n"
			+ "                        \"trainingCount\": 6,\r\n"
			+ "                        \"validationCount\": 9\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"contribution\": 0.2290000021457672,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"validationCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"output\": \"-------+-----+--------------------------------------------+---------+--------\\n TYPE  | NUM |        LOGKOW FRAGMENT DESCRIPTION         |  COEFF  |  VALUE \\n-------+-----+--------------------------------------------+---------+--------\\n Frag  |  1  |  -CH3    [aliphatic carbon]                | 0.5473  |  0.5473\\n Frag  |  1  |  -CH2-   [aliphatic carbon]                | 0.4911  |  0.4911\\n Frag  |  1  |  -OH     [hydroxy, aliphatic attach]       |-1.4086  | -1.4086\\n Const |     |  Equation Constant                         |         |  0.2290\\n-------+-----+--------------------------------------------+---------+--------\\n                                                         Log Kow   =  -0.1412\\n\",\r\n"
			+ "                \"notes\": \"\",\r\n"
			+ "                \"flags\": {\r\n"
			+ "                    \"isOrganicAcid\": false,\r\n"
			+ "                    \"isAminoAcid\": false\r\n"
			+ "                }\r\n"
			+ "            },\r\n"
			+ "            \"value\": -0.1411999762058258,\r\n"
			+ "            \"units\": \"\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"HANSCH,C ET AL.\",\r\n"
			+ "                \"year\": 1995,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": -0.31,\r\n"
			+ "            \"units\": null,\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"meltingPoint\": {\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Group\",\r\n"
			+ "                        \"description\": \"-CH3\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": -5.099999904632568,\r\n"
			+ "                        \"totalCoefficient\": -5.099999904632568\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Group\",\r\n"
			+ "                        \"description\": \"-CH2-\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 11.270000457763672,\r\n"
			+ "                        \"totalCoefficient\": 11.270000457763672\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Group\",\r\n"
			+ "                        \"description\": \"-OH (primary)\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 44.45000076293945,\r\n"
			+ "                        \"totalCoefficient\": 44.45000076293945\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"*\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 122.5,\r\n"
			+ "                        \"totalCoefficient\": 122.5\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"meltingPointKelvins\": 173.1199951171875,\r\n"
			+ "                \"meltingPointLimitKelvins\": 173.1199951171875,\r\n"
			+ "                \"meltingPointCelsius\": -100.04000854492188,\r\n"
			+ "                \"meltingPointAdaptedJoback\": -100.04000854492188,\r\n"
			+ "                \"meltingPointGoldOgle\": -75.64590454101562,\r\n"
			+ "                \"meltingPointMean\": -87.84295654296875,\r\n"
			+ "                \"meltingPointSelected\": -87.84295654296875\r\n"
			+ "            },\r\n"
			+ "            \"value\": -87.84295654296875,\r\n"
			+ "            \"units\": \"celsius\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"Physprop calculated - based upon VP / Water Sol data\",\r\n"
			+ "                \"year\": 0,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": -114.1,\r\n"
			+ "                \"units\": \"celsius\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": -114.1,\r\n"
			+ "            \"units\": \"celsius\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"boilingPoint\": {\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Group\",\r\n"
			+ "                        \"description\": \"-CH3\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 21.979999542236328,\r\n"
			+ "                        \"totalCoefficient\": 21.979999542236328\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Group\",\r\n"
			+ "                        \"description\": \"-CH2-\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 24.219999313354492,\r\n"
			+ "                        \"totalCoefficient\": 24.219999313354492\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Group\",\r\n"
			+ "                        \"description\": \"-OH (primary)\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 88.45999908447266,\r\n"
			+ "                        \"totalCoefficient\": 88.45999908447266\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"*\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": 198.17999267578125\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"boilingPointKelvinsUncorrected\": 332.8399963378906,\r\n"
			+ "                \"boilingPointKelvinsCorrected\": 338.2669982910156,\r\n"
			+ "                \"boilingPointCelsius\": 65.10699462890625\r\n"
			+ "            },\r\n"
			+ "            \"value\": 65.10699462890625,\r\n"
			+ "            \"units\": \"celsius\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": null,\r\n"
			+ "                \"year\": 0,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 78.2,\r\n"
			+ "                \"units\": \"celsius\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 78.2,\r\n"
			+ "            \"units\": \"celsius\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"vaporPressure\": {\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": [\r\n"
			+ "                {\r\n"
			+ "                    \"type\": \"Antoine\",\r\n"
			+ "                    \"mmHg\": 63.10284423828125,\r\n"
			+ "                    \"pa\": 8412.997399536132\r\n"
			+ "                },\r\n"
			+ "                {\r\n"
			+ "                    \"type\": \"Grain\",\r\n"
			+ "                    \"mmHg\": 58.64371229085568,\r\n"
			+ "                    \"pa\": 7818.497010041461\r\n"
			+ "                },\r\n"
			+ "                {\r\n"
			+ "                    \"type\": \"Mackay\",\r\n"
			+ "                    \"mmHg\": 108.30291323300605,\r\n"
			+ "                    \"pa\": 14439.160998050833\r\n"
			+ "                },\r\n"
			+ "                {\r\n"
			+ "                    \"type\": \"Selected\",\r\n"
			+ "                    \"mmHg\": 60.87327826456847,\r\n"
			+ "                    \"pa\": 8115.747204788798\r\n"
			+ "                },\r\n"
			+ "                {\r\n"
			+ "                    \"type\": \"SubCooled\",\r\n"
			+ "                    \"mmHg\": 58.64371109008789,\r\n"
			+ "                    \"pa\": 7818.496849952698\r\n"
			+ "                }\r\n"
			+ "            ],\r\n"
			+ "            \"value\": 60.87327826456847,\r\n"
			+ "            \"units\": \"mmHg\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"DAUBERT,TE & DANNER,RP\",\r\n"
			+ "                \"year\": 1985,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 59.3,\r\n"
			+ "                \"units\": \"mmHg\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 59.3,\r\n"
			+ "            \"units\": \"mmHg\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"waterSolubilityFromLogKow\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"meltingPoint\": {\r\n"
			+ "                \"value\": -114.1,\r\n"
			+ "                \"units\": \"celsius\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"waterSolubility\": 857740.375,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Alcohol, aliphatic\",\r\n"
			+ "                        \"description\": null,\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.42399999499320984,\r\n"
			+ "                        \"totalCoefficient\": 0.42399999499320984,\r\n"
			+ "                        \"trainingCount\": 18,\r\n"
			+ "                        \"maxFragmentCount\": 1\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"equation\": \"Log S (mol/L) = 0.693-0.96 log Kow-0.0092(Tm-25)-0.00314 MW + Correction\\n Melting Pt (Tm) = -114.0999984741211 deg C (Use Tm = 25 for all liquids)\\nUser Entered Melting Point: -114.0999984741211\",\r\n"
			+ "                \"notes\": \"\",\r\n"
			+ "                \"output\": \"MOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\nLog Kow (experimental):  -0.31\\nLog Kow used by Water solubility estimates:  -0.31 (user entered)\\n\\nEquation Used to Make Water Sol estimate:\\n   Log S (mol/L) = 0.693-0.96 log Kow-0.0092(Tm-25)-0.00314 MW + Correction\\n\\n      Melting Pt (Tm) = -114.10 deg C (Use Tm = 25 for all liquids)\\n\\n      Correction(s):         Value\\n      --------------------   -----\\n       Alcohol, aliphatic    0.424\\n\\n   Log Water Solubility  (in moles/L) :  1.270\\n   Water Solubility at 25 deg C (mg/L):  8.577e+05\\n\"\r\n"
			+ "            },\r\n"
			+ "            \"value\": 857740.375,\r\n"
			+ "            \"units\": \"mg/L\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"RIDDICK,JA ET AL.\",\r\n"
			+ "                \"year\": 1986,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 1000000.0,\r\n"
			+ "                \"units\": \"mg/L\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 1000000.0,\r\n"
			+ "            \"units\": \"mg/L\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"waterSolubilityFromWaterNt\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\"\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"waterSolubility\": 452462.28125,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH3    [aliphatic carbon]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": -0.3212711215019226,\r\n"
			+ "                        \"totalCoefficient\": -0.3212711215019226,\r\n"
			+ "                        \"trainingCount\": 612,\r\n"
			+ "                        \"maxFragmentCount\": 6\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH2-   [aliphatic carbon]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": -0.5370191931724548,\r\n"
			+ "                        \"totalCoefficient\": -0.5370191931724548,\r\n"
			+ "                        \"trainingCount\": 416,\r\n"
			+ "                        \"maxFragmentCount\": 14\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-OH     [hydroxy, aliphatic attach]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 1.6012400388717651,\r\n"
			+ "                        \"totalCoefficient\": 1.6012400388717651,\r\n"
			+ "                        \"trainingCount\": 78,\r\n"
			+ "                        \"maxFragmentCount\": 4\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.24921822547912598,\r\n"
			+ "                        \"totalCoefficient\": 0.24921822547912598,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"equation\": \"\",\r\n"
			+ "                \"notes\": \"\",\r\n"
			+ "                \"output\": \"MOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n-------+-----+--------------------------------------------+----------+---------\\n TYPE  | NUM |    WATER SOLUBILITY FRAGMENT DESCRIPTION   |  COEFF   |  VALUE  \\n-------+-----+--------------------------------------------+----------+---------\\n Frag  |  1  |  -CH3    [aliphatic carbon]                |-0.3213   | -0.3213\\n Frag  |  1  |  -CH2-   [aliphatic carbon]                |-0.5370   | -0.5370\\n Frag  |  1  |  -OH     [hydroxy, aliphatic attach]       | 1.6012   |  1.6012\\n Const |     |  Equation Constant                         |          |  0.2492\\n-------+-----+--------------------------------------------+----------+---------\\n                              Log Water Sol (moles/L) at 25 dec C  =    0.9922\\n                              Water Solubility (mg/L) at 25 dec C  =4.5246e+05\\n\"\r\n"
			+ "            },\r\n"
			+ "            \"value\": 452462.28125,\r\n"
			+ "            \"units\": \"mg/L\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"RIDDICK,JA ET AL.\",\r\n"
			+ "                \"year\": 1986,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 1000000.0,\r\n"
			+ "                \"units\": \"mg/L\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 1000000.0,\r\n"
			+ "            \"units\": \"mg/L\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"henrysLawConstant\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"waterSolubility\": {\r\n"
			+ "                \"value\": 1000000.0,\r\n"
			+ "                \"units\": \"mg/L\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"vaporPressure\": {\r\n"
			+ "                \"value\": 59.3,\r\n"
			+ "                \"units\": \"mmHg\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"molecularWeight\": {\r\n"
			+ "                \"value\": 46.06852124410014,\r\n"
			+ "                \"units\": \"g/mol\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": [\r\n"
			+ "                {\r\n"
			+ "                    \"name\": \"VP/WSOL\",\r\n"
			+ "                    \"value\": 3.5951321156640813E-6,\r\n"
			+ "                    \"factors\": null,\r\n"
			+ "                    \"hlcAtm\": 3.5951321156640813E-6,\r\n"
			+ "                    \"hlcUnitless\": 1.4694759016379219E-4,\r\n"
			+ "                    \"hlcPaMol\": 0.3642767616196634,\r\n"
			+ "                    \"notes\": \"\"\r\n"
			+ "                },\r\n"
			+ "                {\r\n"
			+ "                    \"name\": \"Bond\",\r\n"
			+ "                    \"value\": 3.635162115097046,\r\n"
			+ "                    \"factors\": [\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"Hydrogen\",\r\n"
			+ "                            \"description\": \"Hydrogen to Carbon (aliphatic) Bonds\",\r\n"
			+ "                            \"fragmentCount\": 5,\r\n"
			+ "                            \"totalCoefficient\": -0.5983849763870239,\r\n"
			+ "                            \"trainingCount\": 284,\r\n"
			+ "                            \"maxFragmentCount\": 27\r\n"
			+ "                        },\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"Hydrogen\",\r\n"
			+ "                            \"description\": \"Hydrogen to Oxygen Bonds\",\r\n"
			+ "                            \"fragmentCount\": 1,\r\n"
			+ "                            \"totalCoefficient\": 3.2317700386047363,\r\n"
			+ "                            \"trainingCount\": 42,\r\n"
			+ "                            \"maxFragmentCount\": 3\r\n"
			+ "                        },\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"Fragment\",\r\n"
			+ "                            \"description\": \"C-C\",\r\n"
			+ "                            \"fragmentCount\": 1,\r\n"
			+ "                            \"totalCoefficient\": 0.11630400270223618,\r\n"
			+ "                            \"trainingCount\": 200,\r\n"
			+ "                            \"maxFragmentCount\": 9\r\n"
			+ "                        },\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"Fragment\",\r\n"
			+ "                            \"description\": \"C-O\",\r\n"
			+ "                            \"fragmentCount\": 1,\r\n"
			+ "                            \"totalCoefficient\": 1.0854729413986206,\r\n"
			+ "                            \"trainingCount\": 83,\r\n"
			+ "                            \"maxFragmentCount\": 4\r\n"
			+ "                        },\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"Factor  \",\r\n"
			+ "                            \"description\": \"Non-cyclic alkyl or olefinic alcohol\",\r\n"
			+ "                            \"fragmentCount\": 0,\r\n"
			+ "                            \"totalCoefficient\": -0.20000000298023224,\r\n"
			+ "                            \"trainingCount\": 18,\r\n"
			+ "                            \"maxFragmentCount\": 1\r\n"
			+ "                        }\r\n"
			+ "                    ],\r\n"
			+ "                    \"hlcAtm\": 5.666232027579099E-6,\r\n"
			+ "                    \"hlcUnitless\": 2.3165297170635313E-4,\r\n"
			+ "                    \"hlcPaMol\": 0.5741309523582458,\r\n"
			+ "                    \"notes\": \"\"\r\n"
			+ "                },\r\n"
			+ "                {\r\n"
			+ "                    \"name\": \"Group\",\r\n"
			+ "                    \"value\": 3.6999998092651367,\r\n"
			+ "                    \"factors\": [\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"NONE    \",\r\n"
			+ "                            \"description\": \"CH3 (X)\",\r\n"
			+ "                            \"fragmentCount\": 1,\r\n"
			+ "                            \"totalCoefficient\": -0.6200000047683716,\r\n"
			+ "                            \"trainingCount\": 0,\r\n"
			+ "                            \"maxFragmentCount\": 0\r\n"
			+ "                        },\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"NONE    \",\r\n"
			+ "                            \"description\": \"CH2 (C)(O)\",\r\n"
			+ "                            \"fragmentCount\": 1,\r\n"
			+ "                            \"totalCoefficient\": -0.12999999523162842,\r\n"
			+ "                            \"trainingCount\": 0,\r\n"
			+ "                            \"maxFragmentCount\": 0\r\n"
			+ "                        },\r\n"
			+ "                        {\r\n"
			+ "                            \"type\": \"NONE    \",\r\n"
			+ "                            \"description\": \"O-H (C)\",\r\n"
			+ "                            \"fragmentCount\": 1,\r\n"
			+ "                            \"totalCoefficient\": 4.449999809265137,\r\n"
			+ "                            \"trainingCount\": 0,\r\n"
			+ "                            \"maxFragmentCount\": 0\r\n"
			+ "                        }\r\n"
			+ "                    ],\r\n"
			+ "                    \"hlcAtm\": 4.8804140533320606E-6,\r\n"
			+ "                    \"hlcUnitless\": 1.995263301068917E-4,\r\n"
			+ "                    \"hlcPaMol\": 0.49450796842575073,\r\n"
			+ "                    \"notes\": \"\"\r\n"
			+ "                }\r\n"
			+ "            ],\r\n"
			+ "            \"value\": 3.5951321156640813E-6,\r\n"
			+ "            \"units\": \"atm-m3/mol\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"GAFFNEY,JS ET AL.\",\r\n"
			+ "                \"year\": 1987,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 5.0E-6,\r\n"
			+ "                \"units\": \"atm-m3/mol\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 5.0E-6,\r\n"
			+ "            \"units\": \"atm-m3/mol\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"logKoa\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"henrysLawConstant\": {\r\n"
			+ "                \"value\": 5.0E-6,\r\n"
			+ "                \"units\": \"atm-m3/mol\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"kow\": 0.4897788193684462,\r\n"
			+ "                \"kaw\": 2.0437022261787977E-4,\r\n"
			+ "                \"koa\": 2396.52730762156,\r\n"
			+ "                \"logKoa\": 3.379582381995359\r\n"
			+ "            },\r\n"
			+ "            \"value\": 3.379582381995359,\r\n"
			+ "            \"units\": \"\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"Gruber,D et al.\",\r\n"
			+ "                \"year\": 1997,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 3.25,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 3.25,\r\n"
			+ "            \"units\": null,\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"biodegradationRate\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"removeMetals\": false\r\n"
			+ "        },\r\n"
			+ "        \"models\": [\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"Linear Model Prediction\",\r\n"
			+ "                \"value\": 0.8843432907015085,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.15873000025749207,\r\n"
			+ "                        \"totalCoefficient\": 0.15873000025749207,\r\n"
			+ "                        \"trainingCount\": 34,\r\n"
			+ "                        \"maxFragmentCount\": 3\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Molecular Weight\",\r\n"
			+ "                        \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": -0.02193254791200161,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": 0.7475458383560181,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"Non-Linear Model Prediction\",\r\n"
			+ "                \"value\": 0.9698896011339592,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 1.117799997329712,\r\n"
			+ "                        \"totalCoefficient\": 1.117799997329712,\r\n"
			+ "                        \"trainingCount\": 34,\r\n"
			+ "                        \"maxFragmentCount\": 3\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Molecular Weight\",\r\n"
			+ "                        \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": -0.654188334941864,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"Ultimate Biodegradation Timeframe\",\r\n"
			+ "                \"value\": 3.2573328018188477,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.1599700003862381,\r\n"
			+ "                        \"totalCoefficient\": 0.1599700003862381,\r\n"
			+ "                        \"trainingCount\": 18,\r\n"
			+ "                        \"maxFragmentCount\": 4\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Molecular Weight\",\r\n"
			+ "                        \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": -0.10180778801441193,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": 3.1991705894470215,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"Primary Biodegradation Timeframe\",\r\n"
			+ "                \"value\": 3.910719871520996,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.1294499933719635,\r\n"
			+ "                        \"totalCoefficient\": 0.1294499933719635,\r\n"
			+ "                        \"trainingCount\": 18,\r\n"
			+ "                        \"maxFragmentCount\": 4\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Molecular Weight\",\r\n"
			+ "                        \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": -0.06646719574928284,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": 3.8477370738983154,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"MITI Linear Model Prediction\",\r\n"
			+ "                \"value\": 0.683092225342989,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.13590000569820404,\r\n"
			+ "                        \"totalCoefficient\": 0.13590000569820404,\r\n"
			+ "                        \"trainingCount\": 125,\r\n"
			+ "                        \"maxFragmentCount\": 6\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Methyl  [-CH3]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.0399399995803833,\r\n"
			+ "                        \"totalCoefficient\": 0.0399399995803833,\r\n"
			+ "                        \"trainingCount\": 517,\r\n"
			+ "                        \"maxFragmentCount\": 24\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH2-  [linear]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.025529999285936356,\r\n"
			+ "                        \"totalCoefficient\": 0.025529999285936356,\r\n"
			+ "                        \"trainingCount\": 407,\r\n"
			+ "                        \"maxFragmentCount\": 56\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Molecular Weight\",\r\n"
			+ "                        \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": -0.07265175879001617,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": 0.5543739795684814,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"MITI Non-Linear Model Prediction\",\r\n"
			+ "                \"value\": 0.8858835338786439,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.7144700288772583,\r\n"
			+ "                        \"totalCoefficient\": 0.7144700288772583,\r\n"
			+ "                        \"trainingCount\": 125,\r\n"
			+ "                        \"maxFragmentCount\": 6\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Methyl  [-CH3]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.23510999977588654,\r\n"
			+ "                        \"totalCoefficient\": 0.23510999977588654,\r\n"
			+ "                        \"trainingCount\": 517,\r\n"
			+ "                        \"maxFragmentCount\": 24\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH2-  [linear]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.2345000058412552,\r\n"
			+ "                        \"totalCoefficient\": 0.2345000058412552,\r\n"
			+ "                        \"trainingCount\": 407,\r\n"
			+ "                        \"maxFragmentCount\": 56\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Molecular Weight\",\r\n"
			+ "                        \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": -0.7970041036605835,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"name\": \"Anaerobic Model Prediction\",\r\n"
			+ "                \"value\": 0.9152658004313707,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.1327637881040573,\r\n"
			+ "                        \"totalCoefficient\": 0.1327637881040573,\r\n"
			+ "                        \"trainingCount\": 27,\r\n"
			+ "                        \"maxFragmentCount\": 5\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"Methyl  [-CH3]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": -0.07957218587398529,\r\n"
			+ "                        \"totalCoefficient\": -0.07957218587398529,\r\n"
			+ "                        \"trainingCount\": 86,\r\n"
			+ "                        \"maxFragmentCount\": 4\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Fragment\",\r\n"
			+ "                        \"description\": \"-CH2-  [linear]\",\r\n"
			+ "                        \"fragmentCount\": 1,\r\n"
			+ "                        \"coefficient\": 0.02598983235657215,\r\n"
			+ "                        \"totalCoefficient\": 0.02598983235657215,\r\n"
			+ "                        \"trainingCount\": 67,\r\n"
			+ "                        \"maxFragmentCount\": 44\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Constant\",\r\n"
			+ "                        \"description\": \"Equation Constant\",\r\n"
			+ "                        \"fragmentCount\": 0,\r\n"
			+ "                        \"coefficient\": 0.0,\r\n"
			+ "                        \"totalCoefficient\": 0.8360843658447266,\r\n"
			+ "                        \"trainingCount\": 0,\r\n"
			+ "                        \"maxFragmentCount\": 0\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"notes\": \"\",\r\n"
			+ "        \"output\": \"SMILES : OCC\\nMOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n--------------------------- BIOWIN v4.10 Results ----------------------------\\n\\n   Biowin1 (Linear Model Prediction)    :  Biodegrades Fast\\n   Biowin2 (Non-Linear Model Prediction):  Biodegrades Fast\\n   Biowin3 (Ultimate Biodegradation Timeframe):  Days-Weeks\\n   Biowin4 (Primary  Biodegradation Timeframe):  Days\\n   Biowin5 (MITI Linear Model Prediction)    :  Readily Degradable\\n   Biowin6 (MITI Non-Linear Model Prediction):  Readily Degradable\\n   Biowin7 (Anaerobic Model Prediction):  Biodegrades Fast\\n   Ready Biodegradability Prediction:  YES\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin1 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  0.1587 |  0.1587\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.0219\\n Const|  *  |  Equation Constant                         |         |  0.7475\\n============+============================================+=========+=========\\n   RESULT   |    Biowin1 (Linear Biodeg Probability)     |         |  0.8843\\n============+============================================+=========+=========\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin2 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  1.1178 |  1.1178\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.6542\\n============+============================================+=========+=========\\n   RESULT   |  Biowin2 (Non-Linear Biodeg Probability)   |         |  0.9699\\n============+============================================+=========+=========\\n\\n A Probability Greater Than or Equal to 0.5 indicates --> Biodegrades Fast\\n A Probability Less Than 0.5 indicates --> Does NOT Biodegrade Fast\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin3 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  0.1600 |  0.1600\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.1018\\n Const|  *  |  Equation Constant                         |         |  3.1992\\n============+============================================+=========+=========\\n   RESULT   |  Biowin3 (Survey Model - Ultimate Biodeg)  |         |  3.2573\\n============+============================================+=========+=========\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin4 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  0.1294 |  0.1294\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.0665\\n Const|  *  |  Equation Constant                         |         |  3.8477\\n============+============================================+=========+=========\\n   RESULT   |   Biowin4 (Survey Model - Primary Biodeg)  |         |  3.9107\\n============+============================================+=========+=========\\n\\n Result Classification:   5.00 -> hours     4.00 -> days    3.00 -> weeks\\n  (Primary & Ultimate)    2.00 -> months    1.00 -> longer\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin5 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  0.1359 |  0.1359\\n Frag |  1  |  Methyl  [-CH3]                            |  0.0399 |  0.0399\\n Frag |  1  |  -CH2-  [linear]                           |  0.0255 |  0.0255\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.0727\\n Const|  *  |  Equation Constant                         |         |  0.5544\\n============+============================================+=========+=========\\n   RESULT   |  Biowin5 (MITI Linear Biodeg Probability)  |         |  0.6831\\n============+============================================+=========+=========\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin6 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  0.7145 |  0.7145\\n Frag |  1  |  Methyl  [-CH3]                            |  0.2351 |  0.2351\\n Frag |  1  |  -CH2-  [linear]                           |  0.2345 |  0.2345\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.7970\\n============+============================================+=========+=========\\n   RESULT   |Biowin6 (MITI Non-Linear Biodeg Probability)|         |  0.8859\\n============+============================================+=========+=========\\n\\n A Probability Greater Than or Equal to 0.5 indicates --> Readily Degradable\\n A Probability Less Than 0.5 indicates --> NOT Readily Degradable\\n\\n\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM |       Biowin7 FRAGMENT DESCRIPTION         |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  |  0.1328 |  0.1328\\n Frag |  1  |  Methyl  [-CH3]                            | -0.0796 | -0.0796\\n Frag |  1  |  -CH2-  [linear]                           |  0.0260 |  0.0260\\n Const|  *  |  Equation Constant                         |         |  0.8361\\n============+============================================+=========+=========\\n   RESULT   |   Biowin7 (Anaerobic Linear Biodeg Prob)   |         |  0.9153\\n============+============================================+=========+=========\\n\\n A Probability Greater Than or Equal to 0.5 indicates --> Biodegrades Fast\\n A Probability Less Than 0.5 indicates --> Does NOT Biodegrade Fast\\n\\nReady Biodegradability Prediction: (YES or NO)\\n----------------------------------------------\\n Criteria for the YES or NO prediction:  If the Biowin3 (ultimate survey\\n model) result is \\\"weeks\\\" or faster (i.e. \\\"days\\\", \\\"days to weeks\\\", or\\n \\\"weeks\\\" AND the Biowin5 (MITI linear model) probability is >= 0.5, then\\n the prediction is YES (readily biodegradable).  If this condition is not\\n satisfied, the prediction is NO (not readily biodegradable).  This method\\n is based on application of Bayesian analysis to ready biodegradation data\\n (see Help).  Biowin5 and 6 also predict ready biodegradability, but for\\n degradation in the OECD301C test only; using data from the Chemicals\\n Evaluation and Research Institute Japan (CERIJ) database.\\n\\n\"\r\n"
			+ "    },\r\n"
			+ "    \"hydrocarbonBiodegradationRate\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\"\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"halfLifeDays\": null,\r\n"
			+ "                \"logHalfLifeDays\": null,\r\n"
			+ "                \"factors\": [],\r\n"
			+ "                \"notes\": \"  NO Estimate Possible ... Structure NOT a Hydrocarbon\\n    (Contains atoms other than C, H or S (-S-))\\n\",\r\n"
			+ "                \"output\": \"SMILES : OCC\\nMOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n-------------------------- BioHCwin v1.01 Results ---------------------------\\n\\n  NO Estimate Possible ... Structure NOT a Hydrocarbon\\n    (Contains atoms other than C, H or S (-S-))\\n\"\r\n"
			+ "            },\r\n"
			+ "            \"value\": null,\r\n"
			+ "            \"units\": \"days\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": null,\r\n"
			+ "            \"units\": \"days\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"aerosolAdsorptionFraction\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"logKoa\": {\r\n"
			+ "                \"value\": 3.25,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"subcooledVaporPressure\": {\r\n"
			+ "                \"value\": 58.64371109008789,\r\n"
			+ "                \"units\": \"\",\r\n"
			+ "                \"valueType\": \"ESTIMATED\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"mackayParticleGasPartitionCoefficient\": 3.836728539473862E-10,\r\n"
			+ "                \"koaParticleGasPartitionCoefficient\": 4.3651583224016655E-10,\r\n"
			+ "                \"mackayAdsorptionFraction\": 3.0693827373679826E-8,\r\n"
			+ "                \"koaAdsorptionFraction\": 3.492126535971851E-8,\r\n"
			+ "                \"jungePankowAdsorptionFraction\": 1.3858263292528124E-8\r\n"
			+ "            },\r\n"
			+ "            \"value\": 3.0693827373679826E-8,\r\n"
			+ "            \"units\": \"\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 3.0693827373679826E-8,\r\n"
			+ "            \"units\": \"\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"atmosphericHalfLife\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"hydroxylRadicalConcentration\": 1500000.0,\r\n"
			+ "            \"ozoneConcentration\": 7.0E11,\r\n"
			+ "            \"twelveHourDay\": true\r\n"
			+ "        },\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"models\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Hydroxyl Radical Reaction\",\r\n"
			+ "                        \"rateConstant\": 3.576280117034912E-12,\r\n"
			+ "                        \"halfLifeHours\": 35.889766693115234,\r\n"
			+ "                        \"factors\": [\r\n"
			+ "                            {\r\n"
			+ "                                \"type\": \"Hydrogen Abstraction\",\r\n"
			+ "                                \"value\": 3.4362800121307373,\r\n"
			+ "                                \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                            },\r\n"
			+ "                            {\r\n"
			+ "                                \"type\": \"Reaction with N, S and -OH\",\r\n"
			+ "                                \"value\": 0.14000000059604645,\r\n"
			+ "                                \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                            },\r\n"
			+ "                            {\r\n"
			+ "                                \"type\": \"Addition to Triple Bonds\",\r\n"
			+ "                                \"value\": 0.0,\r\n"
			+ "                                \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                            },\r\n"
			+ "                            {\r\n"
			+ "                                \"type\": \"Addition to Olefinic Bonds\",\r\n"
			+ "                                \"value\": 0.0,\r\n"
			+ "                                \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                            },\r\n"
			+ "                            {\r\n"
			+ "                                \"type\": \"Addition to Aromatic Rings\",\r\n"
			+ "                                \"value\": 0.0,\r\n"
			+ "                                \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                            },\r\n"
			+ "                            {\r\n"
			+ "                                \"type\": \"Addition to Fused Rings\",\r\n"
			+ "                                \"value\": 0.0,\r\n"
			+ "                                \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                            }\r\n"
			+ "                        ]\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Ozone Reaction\",\r\n"
			+ "                        \"rateConstant\": 0.0,\r\n"
			+ "                        \"halfLifeHours\": 0.0,\r\n"
			+ "                        \"factors\": null\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Overall Reaction\",\r\n"
			+ "                        \"rateConstant\": 3.576280117034912,\r\n"
			+ "                        \"halfLifeHours\": 35.889766693115234,\r\n"
			+ "                        \"factors\": null\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"notes\": \"               ******  NO OZONE REACTION ESTIMATION ******\\n               (ONLY Olefins and Acetylenes are Estimated)\",\r\n"
			+ "                \"output\": \"SMILES : OCC\\nMOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n------------------- SUMMARY (AOP v1.93): HYDROXYL RADICALS (25 deg C) --------\\nHydrogen Abstraction       =   3.4363 E-12 cm3/molecule-sec\\nReaction with N, S and -OH =   0.1400 E-12 cm3/molecule-sec\\nAddition to Triple Bonds   =   0.0000 E-12 cm3/molecule-sec\\nAddition to Olefinic Bonds =   0.0000 E-12 cm3/molecule-sec\\nAddition to Aromatic Rings =   0.0000 E-12 cm3/molecule-sec\\nAddition to Fused Rings    =   0.0000 E-12 cm3/molecule-sec\\n\\n   OVERALL OH Rate Constant =   3.5763 E-12 cm3/molecule-sec\\n   HALF-LIFE =     2.991 Days (12-hr day; 1.500E6 OH/cm3)\\n   HALF-LIFE =    35.890 Hrs\\n------------------- SUMMARY (AOP v1.93): OZONE REACTION (25 deg C) -----------\\n\\n               ******  NO OZONE REACTION ESTIMATION ******\\n               (ONLY Olefins and Acetylenes are Estimated)\\n\\n\\nHydrogen Abstraction Calculation:\\n Ksec  = 0.934 F(-CH3)F(-OH)=0.934(1.000)(3.500)= 3.269\\n Kprim = 0.136 F(-CH2-)=0.136(1.230)= 0.167\\n H Abstraction TOTAL = 3.436 E-12 cm3/molecule-sec\\n\\n\\nReaction Rates With Nitrogen, Sulfur and -OH:\\n K(-OH) =  0.140 E-12 cm3/molecule-sec\\n\"\r\n"
			+ "            },\r\n"
			+ "            \"value\": 35.889766693115234,\r\n"
			+ "            \"units\": \"hours\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"estimatedHydroxylRadicalReactionRateConstant\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"type\": \"Hydroxyl Radical Reaction\",\r\n"
			+ "                \"rateConstant\": 3.576280117034912E-12,\r\n"
			+ "                \"halfLifeHours\": 35.889766693115234,\r\n"
			+ "                \"factors\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Hydrogen Abstraction\",\r\n"
			+ "                        \"value\": 3.4362800121307373,\r\n"
			+ "                        \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Reaction with N, S and -OH\",\r\n"
			+ "                        \"value\": 0.14000000059604645,\r\n"
			+ "                        \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Addition to Triple Bonds\",\r\n"
			+ "                        \"value\": 0.0,\r\n"
			+ "                        \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Addition to Olefinic Bonds\",\r\n"
			+ "                        \"value\": 0.0,\r\n"
			+ "                        \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Addition to Aromatic Rings\",\r\n"
			+ "                        \"value\": 0.0,\r\n"
			+ "                        \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"type\": \"Addition to Fused Rings\",\r\n"
			+ "                        \"value\": 0.0,\r\n"
			+ "                        \"unit\": \"E-12 cm3/molecule-sec\"\r\n"
			+ "                    }\r\n"
			+ "                ]\r\n"
			+ "            },\r\n"
			+ "            \"value\": 3.576280117034912E-12,\r\n"
			+ "            \"units\": \"cm3/molecule-sec\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"estimatedOzoneReactionRateConstant\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"type\": \"Ozone Reaction\",\r\n"
			+ "                \"rateConstant\": 0.0,\r\n"
			+ "                \"halfLifeHours\": 0.0,\r\n"
			+ "                \"factors\": null\r\n"
			+ "            },\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"cm3/molecule-sec\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"experimentalHydroxylRadicalReactionRateConstantValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"ATKINSON,R ET AL.\",\r\n"
			+ "                \"year\": 2006,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 3.2E-12,\r\n"
			+ "                \"units\": \"cm3/molecule-sec\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"experimentalOzoneReactionRateConstantValues\": [],\r\n"
			+ "        \"experimentalNitrateReactionRateConstantValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": null,\r\n"
			+ "                \"year\": 0,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 2.0E-16,\r\n"
			+ "                \"units\": \"cm3/molecule-sec\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"selectedHydroxylRadicalReactionRateConstant\": {\r\n"
			+ "            \"value\": 3.2E-12,\r\n"
			+ "            \"units\": \"cm3/molecule-sec\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        },\r\n"
			+ "        \"selectedOzoneReactionRateConstantValues\": {\r\n"
			+ "            \"value\": 0.0,\r\n"
			+ "            \"units\": \"cm3/molecule-sec\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"logKoc\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"experimentalValues\": [\r\n"
			+ "            {\r\n"
			+ "                \"author\": \"Schuurmann,G et al\",\r\n"
			+ "                \"year\": 2006,\r\n"
			+ "                \"order\": 1,\r\n"
			+ "                \"value\": 0.2,\r\n"
			+ "                \"units\": \"L/kg\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"estimatedValue\": {\r\n"
			+ "            \"model\": {\r\n"
			+ "                \"logKoc\": 0.019115447998046875,\r\n"
			+ "                \"models\": [\r\n"
			+ "                    {\r\n"
			+ "                        \"firstOrderMCI\": 1.4142135381698608,\r\n"
			+ "                        \"name\": \"MCI\",\r\n"
			+ "                        \"factors\": [\r\n"
			+ "                            {\r\n"
			+ "                                \"fragmentCount\": 1,\r\n"
			+ "                                \"trainingCount\": 21,\r\n"
			+ "                                \"maxFragmentCount\": 1,\r\n"
			+ "                                \"description\": \"Aliphatic Alcohol  (-C-OH)  ...........\",\r\n"
			+ "                                \"coefficient\": -1.3179140090942383,\r\n"
			+ "                                \"totalCoefficient\": -1.3179140090942383\r\n"
			+ "                            }\r\n"
			+ "                        ],\r\n"
			+ "                        \"nonCorrectedLogKoc\": 1.3370294570922852,\r\n"
			+ "                        \"correctedLogKoc\": 0.019115447998046875,\r\n"
			+ "                        \"koc\": 1.0449979305267334\r\n"
			+ "                    },\r\n"
			+ "                    {\r\n"
			+ "                        \"logKow\": -0.3100000023841858,\r\n"
			+ "                        \"name\": \"Kow\",\r\n"
			+ "                        \"factors\": [\r\n"
			+ "                            {\r\n"
			+ "                                \"fragmentCount\": 1,\r\n"
			+ "                                \"trainingCount\": 0,\r\n"
			+ "                                \"maxFragmentCount\": 0,\r\n"
			+ "                                \"description\": \"Aliphatic Alcohol  (-C-OH)  ...........\",\r\n"
			+ "                                \"coefficient\": -0.41144299507141113,\r\n"
			+ "                                \"totalCoefficient\": -0.41144299507141113\r\n"
			+ "                            }\r\n"
			+ "                        ],\r\n"
			+ "                        \"nonCorrectedLogKoc\": 0.753629744052887,\r\n"
			+ "                        \"correctedLogKoc\": 0.34218674898147583,\r\n"
			+ "                        \"koc\": 2.198805093765259\r\n"
			+ "                    }\r\n"
			+ "                ],\r\n"
			+ "                \"notes\": \"\",\r\n"
			+ "                \"output\": \"MOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n---------------------------  KOCWIN v2.01 Results  ---------------------------\\n\\n  Koc Estimate from MCI:\\n  ---------------------\\n         First Order Molecular Connectivity Index  ........... :  1.414\\n         Non-Corrected Log Koc (0.5213 MCI + 0.60)  .......... :  1.3370\\n         Fragment Correction(s):\\n                  1   Aliphatic Alcohol  (-C-OH)  ...........  : -1.3179\\n         Corrected Log Koc  .................................. :  0.0191\\n\\n                         Estimated Koc:  1.045  L/kg   <===========\\n\\n  Koc Estimate from Log Kow:\\n  -------------------------\\n         Log Kow  (User entered   )  ......................... : -0.31\\n         Non-Corrected Log Koc (0.55313 logKow + 0.9251)  .... :  0.7536\\n         Fragment Correction(s):\\n                  1   Aliphatic Alcohol  (-C-OH)  ...........  : -0.4114\\n         Corrected Log Koc  .................................. :  0.3422\\n\\n                         Estimated Koc:  2.199  L/kg   <===========\\n\\n\"\r\n"
			+ "            },\r\n"
			+ "            \"value\": 0.019115447998046875,\r\n"
			+ "            \"units\": \"L/kg\",\r\n"
			+ "            \"valueType\": \"ESTIMATED\"\r\n"
			+ "        },\r\n"
			+ "        \"selectedValue\": {\r\n"
			+ "            \"value\": 0.2,\r\n"
			+ "            \"units\": \"L/kg\",\r\n"
			+ "            \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"hydrolysis\": {\r\n"
			+ "        \"halfLives\": [\r\n"
			+ "            {\r\n"
			+ "                \"ph\": 7.0,\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"unit\": null,\r\n"
			+ "                \"baseCatalyzed\": true,\r\n"
			+ "                \"acidCatalyzed\": false,\r\n"
			+ "                \"phosphorusEster\": false,\r\n"
			+ "                \"isomer\": null\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"ph\": 7.0,\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"unit\": null,\r\n"
			+ "                \"baseCatalyzed\": false,\r\n"
			+ "                \"acidCatalyzed\": true,\r\n"
			+ "                \"phosphorusEster\": false,\r\n"
			+ "                \"isomer\": null\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"ph\": 7.0,\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"unit\": null,\r\n"
			+ "                \"baseCatalyzed\": false,\r\n"
			+ "                \"acidCatalyzed\": false,\r\n"
			+ "                \"phosphorusEster\": false,\r\n"
			+ "                \"isomer\": null\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"ph\": 7.0,\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"unit\": null,\r\n"
			+ "                \"baseCatalyzed\": false,\r\n"
			+ "                \"acidCatalyzed\": true,\r\n"
			+ "                \"phosphorusEster\": true,\r\n"
			+ "                \"isomer\": null\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"ph\": 7.0,\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"unit\": null,\r\n"
			+ "                \"baseCatalyzed\": false,\r\n"
			+ "                \"acidCatalyzed\": true,\r\n"
			+ "                \"phosphorusEster\": false,\r\n"
			+ "                \"isomer\": null\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"ph\": 8.0,\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"unit\": null,\r\n"
			+ "                \"baseCatalyzed\": true,\r\n"
			+ "                \"acidCatalyzed\": false,\r\n"
			+ "                \"phosphorusEster\": false,\r\n"
			+ "                \"isomer\": null\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"phosphorusEsterHalfLives\": [],\r\n"
			+ "        \"fragments\": [],\r\n"
			+ "        \"baseCatalyzedRateConstant\": 0.0,\r\n"
			+ "        \"acidCatalyzedRateConstant\": 0.0,\r\n"
			+ "        \"acidCatalyzedRateConstantForTransIsomer\": 0.0,\r\n"
			+ "        \"neutralRateConstant\": 0.0,\r\n"
			+ "        \"output\": \"SMILES : OCC\\nMOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n--------------------------- HYDROWIN v2.00 Results ---------------------------\\n\\n\\n Currently, this program can NOT estimate a hydrolysis rate constant for\\n                the type of chemical structure entered!!\\n\\n ONLY Esters, Carbamates, Epoxides, Halomethanes (containing 1-3 halogens),\\n      Specific Alkyl Halides & Phosphorus Esters can be estimated!!\\n\\n   When present, various hydrolyzable compound-types will be identified.\\n  For more information, (Click OVERVIEW in Help  or  see the User's Guide)\\n\\n              *****   CALCULATION NOT PERFORMED   *****\\n\"\r\n"
			+ "    },\r\n"
			+ "    \"bioconcentration\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"cas\": \"000064-17-5\",\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"bioconcentrationFactor\": 3.16,\r\n"
			+ "        \"experimentalBioconcentrationFactor\": null,\r\n"
			+ "        \"logBioconcentrationFactor\": 0.5,\r\n"
			+ "        \"biotransformationHalfLife\": 0.03,\r\n"
			+ "        \"bioaccumulationFactor\": 0.92,\r\n"
			+ "        \"logBioaccumulationFactor\": -0.04,\r\n"
			+ "        \"biotransformationFactors\": [\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Fragmant\",\r\n"
			+ "                \"description\": \"Aliphatic alcohol  [-OH]\",\r\n"
			+ "                \"fragmentCount\": 1,\r\n"
			+ "                \"coefficient\": -0.06155700981616974,\r\n"
			+ "                \"totalCoefficient\": -0.06155700981616974,\r\n"
			+ "                \"trainingCount\": 43,\r\n"
			+ "                \"maxFragmentCount\": 3\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Fragmant\",\r\n"
			+ "                \"description\": \"Methyl  [-CH3]\",\r\n"
			+ "                \"fragmentCount\": 1,\r\n"
			+ "                \"coefficient\": 0.2451052963733673,\r\n"
			+ "                \"totalCoefficient\": 0.2451052963733673,\r\n"
			+ "                \"trainingCount\": 170,\r\n"
			+ "                \"maxFragmentCount\": 12\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Fragmant\",\r\n"
			+ "                \"description\": \"-CH2-  [linear]\",\r\n"
			+ "                \"fragmentCount\": 1,\r\n"
			+ "                \"coefficient\": 0.02418706938624382,\r\n"
			+ "                \"totalCoefficient\": 0.02418706938624382,\r\n"
			+ "                \"trainingCount\": 109,\r\n"
			+ "                \"maxFragmentCount\": 28\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"LogKow\",\r\n"
			+ "                \"description\": \"Log Kow = -0.31  used for calc\",\r\n"
			+ "                \"fragmentCount\": 0,\r\n"
			+ "                \"coefficient\": 0.30734214186668396,\r\n"
			+ "                \"totalCoefficient\": -0.09527606517076492,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"MolWt\",\r\n"
			+ "                \"description\": \"Molecular Weight Parameter\",\r\n"
			+ "                \"fragmentCount\": 0,\r\n"
			+ "                \"coefficient\": 0.0,\r\n"
			+ "                \"totalCoefficient\": -0.11813774704933167,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Constant\",\r\n"
			+ "                \"description\": \"Equation Constant\",\r\n"
			+ "                \"fragmentCount\": 0,\r\n"
			+ "                \"coefficient\": 0.0,\r\n"
			+ "                \"totalCoefficient\": -1.537068486213684,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Result\",\r\n"
			+ "                \"description\": \"LOG Bio Half-Life (days)\",\r\n"
			+ "                \"fragmentCount\": 0,\r\n"
			+ "                \"coefficient\": 0.0,\r\n"
			+ "                \"totalCoefficient\": -1.542746901512146,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Result\",\r\n"
			+ "                \"description\": \"Bio Half-Life (days)\",\r\n"
			+ "                \"fragmentCount\": 0,\r\n"
			+ "                \"coefficient\": 0.0,\r\n"
			+ "                \"totalCoefficient\": 0.02865847572684288,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Note\",\r\n"
			+ "                \"description\": \"Bio Half-Life Normalized to 10 g fish at 15 deg C\",\r\n"
			+ "                \"fragmentCount\": 0,\r\n"
			+ "                \"coefficient\": 0.0,\r\n"
			+ "                \"totalCoefficient\": 0.0,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"biotransformationRateConstants\": [\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"KMRateConstant\",\r\n"
			+ "                \"value\": 24.186464309692383,\r\n"
			+ "                \"unit\": \"/day (10 gram fish)\"\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"KMRateConstant\",\r\n"
			+ "                \"value\": 13.601048469543457,\r\n"
			+ "                \"unit\": \"/day (100 gram fish)\"\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"KMRateConstant\",\r\n"
			+ "                \"value\": 7.648431777954102,\r\n"
			+ "                \"unit\": \"/day (1 kg fish)\"\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"KMRateConstant\",\r\n"
			+ "                \"value\": 4.301029205322266,\r\n"
			+ "                \"unit\": \"/day (10 kg fish)\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"bioconcentrationFactors\": [\r\n"
			+ "            {\r\n"
			+ "                \"type\": \"Correction\",\r\n"
			+ "                \"description\": \"Correction Factors Not Used for Log Kow < 1\",\r\n"
			+ "                \"fragmentCount\": 1,\r\n"
			+ "                \"coefficient\": 0.0,\r\n"
			+ "                \"totalCoefficient\": 0.0,\r\n"
			+ "                \"trainingCount\": 0,\r\n"
			+ "                \"maxFragmentCount\": 0\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"biocontrationFactorEquation\": \"Log BCF = 0.50\",\r\n"
			+ "        \"biocontrationFactorEquationSum\": 0.5,\r\n"
			+ "        \"arnotGobasBcfBafEstimates\": [\r\n"
			+ "            {\r\n"
			+ "                \"trophicLevel\": \"Upper Trophic\",\r\n"
			+ "                \"trophicLevelNote\": null,\r\n"
			+ "                \"bioconcentrationFactor\": 0.9209218756614383,\r\n"
			+ "                \"logBioconcentrationFactor\": -0.03577721063729105,\r\n"
			+ "                \"bioaccumulationFactor\": 0.9209218757900303,\r\n"
			+ "                \"logBioaccumulationFactor\": -0.035777210576648816,\r\n"
			+ "                \"unit\": \"L/kg wet-wt\"\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"trophicLevel\": \"Mid Trophic\",\r\n"
			+ "                \"trophicLevelNote\": null,\r\n"
			+ "                \"bioconcentrationFactor\": 0.9553187481535758,\r\n"
			+ "                \"logBioconcentrationFactor\": -0.01985169913088358,\r\n"
			+ "                \"bioaccumulationFactor\": 0.9553187949120288,\r\n"
			+ "                \"logBioaccumulationFactor\": -0.019851677874169353,\r\n"
			+ "                \"unit\": \"L/kg wet-wt\"\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"trophicLevel\": \"Lower Trophic\",\r\n"
			+ "                \"trophicLevelNote\": null,\r\n"
			+ "                \"bioconcentrationFactor\": 0.9623299154782167,\r\n"
			+ "                \"logBioconcentrationFactor\": -0.016676013296426945,\r\n"
			+ "                \"bioaccumulationFactor\": 0.9623310223924226,\r\n"
			+ "                \"logBioaccumulationFactor\": -0.01667551375209461,\r\n"
			+ "                \"unit\": \"L/kg wet-wt\"\r\n"
			+ "            },\r\n"
			+ "            {\r\n"
			+ "                \"trophicLevel\": \"Upper Trophic\",\r\n"
			+ "                \"trophicLevelNote\": \"where Km = 0\",\r\n"
			+ "                \"bioconcentrationFactor\": 0.9453889796407924,\r\n"
			+ "                \"logBioconcentrationFactor\": -0.02438946455593961,\r\n"
			+ "                \"bioaccumulationFactor\": 0.946057906928562,\r\n"
			+ "                \"logBioaccumulationFactor\": -0.02408228020511532,\r\n"
			+ "                \"unit\": \"L/kg wet-wt\"\r\n"
			+ "            }\r\n"
			+ "        ],\r\n"
			+ "        \"notes\": \"\",\r\n"
			+ "        \"output\": \"MOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n--------------------------------- BCFBAF v3.02 --------------------------------\\nSummary Results:\\n  Log BCF (regression-based estimate):  0.50  (BCF = 3.16 L/kg wet-wt)\\n  Biotransformation Half-Life (days) :  0.0287  (normalized to 10 g fish)\\n  Log BAF (Arnot-Gobas upper trophic):  -0.04  (BAF = 0.921 L/kg wet-wt)\\n\\n=============================\\nBCF (Bioconcentration Factor):\\n=============================\\nLog Kow  (estimated)  :  -0.14\\nLog Kow used by BCF estimates:  -0.31\\n\\nEquation Used to Make BCF estimate:\\n   Log BCF = 0.50\\n\\n      Correction(s):                    Value\\n       Correction Factors Not Used for Log Kow < 1\\n\\n   Estimated Log BCF =  0.500  (BCF = 3.162 L/kg wet-wt)\\n\\n===========================================================\\nWhole Body Primary Biotransformation Rate Estimate for Fish:\\n===========================================================\\n------+-----+--------------------------------------------+---------+---------\\n TYPE | NUM | LOG BIOTRANSFORMATION FRAGMENT DESCRIPTION |  COEFF  |  VALUE  \\n------+-----+--------------------------------------------+---------+---------\\n Frag |  1  |  Aliphatic alcohol  [-OH]                  | -0.0616 | -0.0616\\n Frag |  1  |  Methyl  [-CH3]                            |  0.2451 |  0.2451\\n Frag |  1  |  -CH2-  [linear]                           |  0.0242 |  0.0242\\n L Kow|  *  |  Log Kow =  -0.31 ( used for calc )        |  0.3073 | -0.0953\\n MolWt|  *  |  Molecular Weight Parameter                |         | -0.1181\\n Const|  *  |  Equation Constant                         |         | -1.5371\\n============+============================================+=========+=========\\n   RESULT   |        LOG Bio Half-Life (days)            |         | -1.5427\\n   RESULT   |            Bio Half-Life (days)            |         | 0.02866\\n   NOTE     |  Bio Half-Life Normalized to 10 g fish at 15 deg C   |\\n============+============================================+=========+=========\\n\\nBiotransformation Rate Constant:\\n kM (Rate Constant):  24.19 /day (10 gram fish)   \\n kM (Rate Constant):  13.60 /day (100 gram fish)   \\n kM (Rate Constant):  7.648 /day (1 kg fish)   \\n kM (Rate Constant):  4.301 /day (10 kg fish)   \\n\\nArnot-Gobas BCF & BAF Methods (including biotransformation rate estimates):\\n   Estimated Log BCF (upper trophic) =  -0.036  (BCF = 0.9209 L/kg wet-wt)\\n   Estimated Log BAF (upper trophic) =  -0.036  (BAF = 0.9209 L/kg wet-wt)\\n   Estimated Log BCF (mid trophic)   =  -0.020  (BCF = 0.9553 L/kg wet-wt)\\n   Estimated Log BAF (mid trophic)   =  -0.020  (BAF = 0.9553 L/kg wet-wt)\\n   Estimated Log BCF (lower trophic) =  -0.017  (BCF = 0.9623 L/kg wet-wt)\\n   Estimated Log BAF (lower trophic) =  -0.017  (BAF = 0.9623 L/kg wet-wt)\\n\\n Arnot-Gobas BCF & BAF Methods (assuming a biotransformation rate of zero):\\n   Estimated Log BCF (upper trophic) =  -0.024  (BCF = 0.9454 L/kg wet-wt)\\n   Estimated Log BAF (upper trophic) =  -0.024  (BAF = 0.9461 L/kg wet-wt)\\n\\n \"\r\n"
			+ "    },\r\n"
			+ "    \"waterVolatilization\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"molecularWeight\": 46.06852124410014,\r\n"
			+ "            \"henrysLawConstant\": {\r\n"
			+ "                \"value\": 5.0E-6,\r\n"
			+ "                \"units\": \"atm-m3/mol\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"riverWaterDepthMeters\": 1.0,\r\n"
			+ "            \"riverWindVelocityMetersPerSecond\": 5.0,\r\n"
			+ "            \"riverCurrentVelocityMetersPerSecond\": 1.0,\r\n"
			+ "            \"lakeWindVelocityMetersPerSecond\": 0.5,\r\n"
			+ "            \"lakeCurrentVelocityMetersPerSecond\": 0.05000000074505806,\r\n"
			+ "            \"lakeWaterDepthMeters\": 1.0\r\n"
			+ "        },\r\n"
			+ "        \"riverHalfLifeHours\": 80.1934123965277,\r\n"
			+ "        \"lakeHalfLifeHours\": 931.756591287662\r\n"
			+ "    },\r\n"
			+ "    \"sewageTreatmentModel\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"molecularWeight\": {\r\n"
			+ "                \"value\": 46.06852124410014,\r\n"
			+ "                \"units\": \"g/mol\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            },\r\n"
			+ "            \"henrysLawConstant\": {\r\n"
			+ "                \"value\": 5.0E-6,\r\n"
			+ "                \"units\": \"atm-m3/mol\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"waterSolubility\": {\r\n"
			+ "                \"value\": 1000000.0,\r\n"
			+ "                \"units\": \"mg/L\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"vaporPressure\": {\r\n"
			+ "                \"value\": 59.3,\r\n"
			+ "                \"units\": \"mmHg\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"biowin3\": {\r\n"
			+ "                \"value\": 3.2573328018188477,\r\n"
			+ "                \"units\": \"\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            },\r\n"
			+ "            \"biowin5\": {\r\n"
			+ "                \"value\": 0.683092225342989,\r\n"
			+ "                \"units\": \"\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeHoursPrimaryClarifier\": {\r\n"
			+ "                \"value\": 10000.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeHoursAerationVessel\": {\r\n"
			+ "                \"value\": 10000.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeHoursSettlingTank\": {\r\n"
			+ "                \"value\": 10000.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"model\": {\r\n"
			+ "            \"Influent\": {\r\n"
			+ "                \"MassPerHour\": 10.0,\r\n"
			+ "                \"MolPerHour\": 0.21706797182559967,\r\n"
			+ "                \"Percent\": 100.0\r\n"
			+ "            },\r\n"
			+ "            \"PrimarySludge\": {\r\n"
			+ "                \"MassPerHour\": 0.025061940101748743,\r\n"
			+ "                \"MolPerHour\": 5.440144446479181E-4,\r\n"
			+ "                \"Percent\": 0.2506193981878631\r\n"
			+ "            },\r\n"
			+ "            \"WasteSludge\": {\r\n"
			+ "                \"MassPerHour\": 0.1501461675549301,\r\n"
			+ "                \"MolPerHour\": 0.0032591923700555432,\r\n"
			+ "                \"Percent\": 1.5014616585970118\r\n"
			+ "            },\r\n"
			+ "            \"TotalSludge\": {\r\n"
			+ "                \"MassPerHour\": 0.17520810765667885,\r\n"
			+ "                \"MolPerHour\": 0.0038032068147034613,\r\n"
			+ "                \"Percent\": 1.7520810567848748\r\n"
			+ "            },\r\n"
			+ "            \"PrimVloitilization\": {\r\n"
			+ "                \"MassPerHour\": 0.0026701705195323992,\r\n"
			+ "                \"MolPerHour\": 5.79608492559322E-5,\r\n"
			+ "                \"Percent\": 0.026701705195323992\r\n"
			+ "            },\r\n"
			+ "            \"SettlingVloitilization\": {\r\n"
			+ "                \"MassPerHour\": 0.007258099169529598,\r\n"
			+ "                \"MolPerHour\": 1.575500848250629E-4,\r\n"
			+ "                \"Percent\": 0.07258099169529598\r\n"
			+ "            },\r\n"
			+ "            \"AerationOffGas\": {\r\n"
			+ "                \"MassPerHour\": 0.018257572969349347,\r\n"
			+ "                \"MolPerHour\": 3.963134290168691E-4,\r\n"
			+ "                \"Percent\": 0.18257572763211782\r\n"
			+ "            },\r\n"
			+ "            \"TotalAir\": {\r\n"
			+ "                \"MassPerHour\": 0.028185842658411345,\r\n"
			+ "                \"MolPerHour\": 6.118243630978642E-4,\r\n"
			+ "                \"Percent\": 0.2818584245227378\r\n"
			+ "            },\r\n"
			+ "            \"PrimBiodeg\": {\r\n"
			+ "                \"MassPerHour\": 0.0017576583525521475,\r\n"
			+ "                \"MolPerHour\": 3.815313294431308E-5,\r\n"
			+ "                \"Percent\": 0.017576583525521473\r\n"
			+ "            },\r\n"
			+ "            \"SettlingBiodeg\": {\r\n"
			+ "                \"MassPerHour\": 5.251665421519911E-4,\r\n"
			+ "                \"MolPerHour\": 1.1399683488851202E-5,\r\n"
			+ "                \"Percent\": 0.005251665421519911\r\n"
			+ "            },\r\n"
			+ "            \"AerationBiodeg\": {\r\n"
			+ "                \"MassPerHour\": 0.006918082807259416,\r\n"
			+ "                \"MolPerHour\": 1.501694186938438E-4,\r\n"
			+ "                \"Percent\": 0.06918082807259415\r\n"
			+ "            },\r\n"
			+ "            \"TotalBiodeg\": {\r\n"
			+ "                \"MassPerHour\": 0.009200907701963554,\r\n"
			+ "                \"MolPerHour\": 1.9972223512700808E-4,\r\n"
			+ "                \"Percent\": 0.09200907701963552\r\n"
			+ "            },\r\n"
			+ "            \"FinalEffluent\": {\r\n"
			+ "                \"MassPerHour\": 9.787404947675649,\r\n"
			+ "                \"MolPerHour\": 0.21245321174407117,\r\n"
			+ "                \"Percent\": 97.87404837170719\r\n"
			+ "            },\r\n"
			+ "            \"TotalRemoval\": {\r\n"
			+ "                \"MassPerHour\": 0.21259505232435139,\r\n"
			+ "                \"MolPerHour\": 0.004614760081528502,\r\n"
			+ "                \"Percent\": 2.125950523243514\r\n"
			+ "            },\r\n"
			+ "            \"PrimaryRateConstant\": {\r\n"
			+ "                \"MassPerHour\": 0.038656946271657944,\r\n"
			+ "                \"MolPerHour\": 17.926921844482422,\r\n"
			+ "                \"Percent\": 10000.0\r\n"
			+ "            },\r\n"
			+ "            \"AerationRateConstant\": {\r\n"
			+ "                \"MassPerHour\": 0.038656946271657944,\r\n"
			+ "                \"MolPerHour\": 17.926921844482422,\r\n"
			+ "                \"Percent\": 10000.0\r\n"
			+ "            },\r\n"
			+ "            \"SettlingRateConstant\": {\r\n"
			+ "                \"MassPerHour\": 0.038656946271657944,\r\n"
			+ "                \"MolPerHour\": 17.926921844482422,\r\n"
			+ "                \"Percent\": 10000.0\r\n"
			+ "            },\r\n"
			+ "            \"CalculationVariables\": [\r\n"
			+ "                46.068519592285156,\r\n"
			+ "                1000000.0,\r\n"
			+ "                7906.0166015625,\r\n"
			+ "                0.07802631705999374,\r\n"
			+ "                59.29999923706055,\r\n"
			+ "                4.999999888062477E-6,\r\n"
			+ "                2.0448512805160135E-4,\r\n"
			+ "                0.4897788166999817,\r\n"
			+ "                -0.3100000023841858,\r\n"
			+ "                0.8979557752609253,\r\n"
			+ "                25.0,\r\n"
			+ "                0.0\r\n"
			+ "            ]\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"fugacityModel\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"henrysLawConstant\": {\r\n"
			+ "                \"value\": 5.0E-6,\r\n"
			+ "                \"units\": \"atm-m3/mol\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"logKoc\": {\r\n"
			+ "                \"value\": 0.2,\r\n"
			+ "                \"units\": \"L/kg\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"molecularWeight\": {\r\n"
			+ "                \"value\": 46.06852124410014,\r\n"
			+ "                \"units\": \"g/mol\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            },\r\n"
			+ "            \"meltingPoint\": {\r\n"
			+ "                \"value\": -114.1,\r\n"
			+ "                \"units\": \"celsius\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"vaporPressure\": {\r\n"
			+ "                \"value\": 59.3,\r\n"
			+ "                \"units\": \"mmHg\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"waterSolubility\": {\r\n"
			+ "                \"value\": 1000000.0,\r\n"
			+ "                \"units\": \"mg/L\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"atmosphericHydroxylRateConstant\": {\r\n"
			+ "                \"value\": 3.2E-12,\r\n"
			+ "                \"units\": \"cm3/molecule-sec\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"ultimateBiodegradation\": {\r\n"
			+ "                \"value\": 3.2573328018188477,\r\n"
			+ "                \"units\": \"\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeAir\": {\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeWater\": {\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeSoil\": {\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"halfLifeSediment\": {\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"emissionRateAir\": {\r\n"
			+ "                \"value\": 1000.0,\r\n"
			+ "                \"units\": \"kg/hour\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"emissionRateWater\": {\r\n"
			+ "                \"value\": 1000.0,\r\n"
			+ "                \"units\": \"kg/hour\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"emissionRateSoil\": {\r\n"
			+ "                \"value\": 1000.0,\r\n"
			+ "                \"units\": \"kg/hour\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"emissionRateSediment\": {\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"units\": \"kg/hour\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"advectionTimeAir\": {\r\n"
			+ "                \"value\": 100.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"advectionTimeWater\": {\r\n"
			+ "                \"value\": 1000.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"advectionTimeSoil\": {\r\n"
			+ "                \"value\": 0.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"advectionTimeSediment\": {\r\n"
			+ "                \"value\": 50000.0,\r\n"
			+ "                \"units\": \"hours\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            }\r\n"
			+ "        },\r\n"
			+ "        \"model\": {\r\n"
			+ "            \"Air\": [\r\n"
			+ "                {\r\n"
			+ "                    \"MassAmount\": 7.410519090233457,\r\n"
			+ "                    \"HalfLife\": 80.20833333333333,\r\n"
			+ "                    \"Emissions\": 1000.0\r\n"
			+ "                },\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null\r\n"
			+ "            ],\r\n"
			+ "            \"Water\": [\r\n"
			+ "                {\r\n"
			+ "                    \"MassAmount\": 40.123240367060035,\r\n"
			+ "                    \"HalfLife\": 208.07999999999998,\r\n"
			+ "                    \"Emissions\": 1000.0\r\n"
			+ "                },\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null\r\n"
			+ "            ],\r\n"
			+ "            \"Soil\": [\r\n"
			+ "                {\r\n"
			+ "                    \"MassAmount\": 52.394400538578054,\r\n"
			+ "                    \"HalfLife\": 416.15999999999997,\r\n"
			+ "                    \"Emissions\": 1000.0\r\n"
			+ "                },\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null\r\n"
			+ "            ],\r\n"
			+ "            \"Sediment\": [\r\n"
			+ "                {\r\n"
			+ "                    \"MassAmount\": 0.07184000412845637,\r\n"
			+ "                    \"HalfLife\": 1872.7199999999998,\r\n"
			+ "                    \"Emissions\": 0.0\r\n"
			+ "                },\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null,\r\n"
			+ "                null\r\n"
			+ "            ],\r\n"
			+ "            \"Persistence\": 250.52595579217223,\r\n"
			+ "            \"aEmissionArray\": [\r\n"
			+ "                0.0,\r\n"
			+ "                0.0,\r\n"
			+ "                0.0,\r\n"
			+ "                0.0\r\n"
			+ "            ],\r\n"
			+ "            \"aAdvectionTimeArray\": [\r\n"
			+ "                0.0,\r\n"
			+ "                0.0,\r\n"
			+ "                0.0,\r\n"
			+ "                0.0\r\n"
			+ "            ],\r\n"
			+ "            \"aFugacities\": [\r\n"
			+ "                1.3607579120154883E-11,\r\n"
			+ "                7.538917051113334E-12,\r\n"
			+ "                3.2354819101935234E-10,\r\n"
			+ "                6.501855712333954E-12\r\n"
			+ "            ],\r\n"
			+ "            \"aReaction\": [\r\n"
			+ "                481.21189637679,\r\n"
			+ "                1004.3217715842981,\r\n"
			+ "                655.7401232877755,\r\n"
			+ "                0.19980240755138765\r\n"
			+ "            ],\r\n"
			+ "            \"aAdvection\": [\r\n"
			+ "                556.9582133990626,\r\n"
			+ "                301.5573942731035,\r\n"
			+ "                0.0,\r\n"
			+ "                0.010798671419037077\r\n"
			+ "            ],\r\n"
			+ "            \"aReactionPercent\": [\r\n"
			+ "                16.040396545893,\r\n"
			+ "                33.477392386143265,\r\n"
			+ "                21.858004109592517,\r\n"
			+ "                0.006660080251712921\r\n"
			+ "            ],\r\n"
			+ "            \"aAdvectionPercent\": [\r\n"
			+ "                18.565273779968752,\r\n"
			+ "                10.051913142436783,\r\n"
			+ "                0.0,\r\n"
			+ "                3.599557139679025E-4\r\n"
			+ "            ],\r\n"
			+ "            \"aSums\": [\r\n"
			+ "                138.20556373230042,\r\n"
			+ "                71.3824531218805,\r\n"
			+ "                28.617546878119505\r\n"
			+ "            ],\r\n"
			+ "            \"aTimes\": [\r\n"
			+ "                250.52595579217223,\r\n"
			+ "                350.9629395398009,\r\n"
			+ "                875.4277816304378\r\n"
			+ "            ],\r\n"
			+ "            \"HalfLifeArray\": [\r\n"
			+ "                80.20833333333333,\r\n"
			+ "                208.07999999999998,\r\n"
			+ "                416.15999999999997,\r\n"
			+ "                1872.7199999999998\r\n"
			+ "            ],\r\n"
			+ "            \"HalfLifeFactorArray\": [\r\n"
			+ "                0.0,\r\n"
			+ "                0.0,\r\n"
			+ "                0.0,\r\n"
			+ "                0.0\r\n"
			+ "            ],\r\n"
			+ "            \"Emission\": [\r\n"
			+ "                1000.0,\r\n"
			+ "                1000.0,\r\n"
			+ "                1000.0,\r\n"
			+ "                0.0\r\n"
			+ "            ],\r\n"
			+ "            \"AdvectionTimesArray\": [\r\n"
			+ "                100.0,\r\n"
			+ "                1000.0,\r\n"
			+ "                0.0,\r\n"
			+ "                50000.0\r\n"
			+ "            ],\r\n"
			+ "            \"aNotes\": []\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"dermalPermeability\": {\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"smiles\": \"OCC\",\r\n"
			+ "            \"logKow\": {\r\n"
			+ "                \"value\": -0.31,\r\n"
			+ "                \"units\": null,\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"molecularWeight\": {\r\n"
			+ "                \"value\": 46.06852124410014,\r\n"
			+ "                \"units\": \"g/mol\",\r\n"
			+ "                \"valueType\": \"NONE\"\r\n"
			+ "            },\r\n"
			+ "            \"dermalPermeabilityCoefficient\": {\r\n"
			+ "                \"value\": -999.0,\r\n"
			+ "                \"units\": \"cm/hr\",\r\n"
			+ "                \"valueType\": \"USER_PROVIDED\"\r\n"
			+ "            },\r\n"
			+ "            \"waterConcentrationMgPerLiter\": {\r\n"
			+ "                \"value\": 1000000.0,\r\n"
			+ "                \"units\": \"mg/L\",\r\n"
			+ "                \"valueType\": \"EXPERIMENTAL\"\r\n"
			+ "            },\r\n"
			+ "            \"eventDurationHours\": 0.58,\r\n"
			+ "            \"fractionAbsorbedWater\": 1.0,\r\n"
			+ "            \"skinSurfaceAreaCm2\": 18000.0,\r\n"
			+ "            \"exposureEventsPerDay\": 1.0,\r\n"
			+ "            \"exposureDurationYears\": 30.0,\r\n"
			+ "            \"exposureDaysPerYear\": 350.0,\r\n"
			+ "            \"bodyWeightKg\": 70.0,\r\n"
			+ "            \"averagingTimeDays\": 25550.0\r\n"
			+ "        },\r\n"
			+ "        \"dermalPermeabilityCoefficient\": 5.375041316387294E-4,\r\n"
			+ "        \"dermalAbsorbedDose\": 5.484853162969122E7,\r\n"
			+ "        \"dermalAbsorbedDosePerEvent\": 519029.62338467065,\r\n"
			+ "        \"lagTimePerEventHours\": 0.19295015249067746,\r\n"
			+ "        \"timeToReachSteadyStateHours\": 0.46308036597762586,\r\n"
			+ "        \"output\": \"MOL FOR: C2 H6 O1 \\nMOL WT : 46.07\\n------------------------------ Dermwin v2.02 ----------------------------------\\nLog Kow  (estimated)  :  -0.14\\nLog Kow (User) :  -0.31   (used in Kp calculations)\\n\\nGENERAL Equation:   log Kp = -2.80 + 0.66 log Kow - 0.0056 MW\\n    Kp (predicted): 0.000538  cm/hr\\n\\nDermally Absorbed Dose per Event for Organic Compounds - Water Contact:\\n   Water Conc (mg/cm3): 1.00e+09  (entered by user)\\n   Event Duration (hr): 0.580\\n   Fraction Absorbed  : 1.0000\\n\\n   DA(event):  5.19e+05 mg/cm2-event (using eqn 3.2 & 3.3)\\n                   (tau = 0.193 hr,  t* = 0.463 hr)\\n\\nDermally Absorbed Dose (70.00 kg Human) - Water Contact:\\n   DAD:  5.48e+07 mg/kg-day (using eqn 3.1)\\n      using:\\n        SA: 18000.0 cm2  (skin surface area)\\n        EV: 1.00 event/day  (event freq)\\n        ED: 30.0 years  (exposure duration)\\n        EF: 350.0 day/year  (exposure freq)\\n        BW: 70.00 kg  (body weight)\\n        AT: 25550.00 days  (averaging time)\\n\\n\"\r\n"
			+ "    },\r\n"
			+ "    \"logKowAnalogs\": [\r\n"
			+ "        \"000064-17-5\",\r\n"
			+ "        \"000071-23-8\",\r\n"
			+ "        \"000071-36-3\",\r\n"
			+ "        \"000071-41-0\",\r\n"
			+ "        \"000111-70-6\",\r\n"
			+ "        \"000111-87-5\",\r\n"
			+ "        \"000067-56-1\",\r\n"
			+ "        \"000107-21-1\",\r\n"
			+ "        \"000056-81-5\",\r\n"
			+ "        \"000057-55-6\",\r\n"
			+ "        \"000067-63-0\",\r\n"
			+ "        \"000078-83-1\",\r\n"
			+ "        \"000078-92-2\",\r\n"
			+ "        \"000584-02-1\"\r\n"
			+ "    ],\r\n"
			+ "    \"analogs\": [\r\n"
			+ "        \"000064-17-5\",\r\n"
			+ "        \"000071-23-8\",\r\n"
			+ "        \"000071-36-3\",\r\n"
			+ "        \"000071-41-0\",\r\n"
			+ "        \"000111-70-6\",\r\n"
			+ "        \"000111-87-5\",\r\n"
			+ "        \"000067-56-1\",\r\n"
			+ "        \"000107-21-1\",\r\n"
			+ "        \"000056-81-5\",\r\n"
			+ "        \"000057-55-6\",\r\n"
			+ "        \"000067-63-0\",\r\n"
			+ "        \"000078-83-1\",\r\n"
			+ "        \"000078-92-2\",\r\n"
			+ "        \"000584-02-1\",\r\n"
			+ "        \"000504-63-2\",\r\n"
			+ "        \"000050-70-4\",\r\n"
			+ "        \"000056-81-5\",\r\n"
			+ "        \"000057-55-6\",\r\n"
			+ "        \"000067-63-0\",\r\n"
			+ "        \"000069-65-8\",\r\n"
			+ "        \"000078-83-1\",\r\n"
			+ "        \"000078-92-2\",\r\n"
			+ "        \"000123-51-3\",\r\n"
			+ "        \"000123-96-6\",\r\n"
			+ "        \"000133-43-7\",\r\n"
			+ "        \"000137-32-6\",\r\n"
			+ "        \"000149-32-6\",\r\n"
			+ "        \"000513-85-9\",\r\n"
			+ "        \"000543-49-7\",\r\n"
			+ "        \"000584-02-1\",\r\n"
			+ "        \"000584-03-2\",\r\n"
			+ "        \"000589-55-9\",\r\n"
			+ "        \"000589-62-8\",\r\n"
			+ "        \"000589-82-2\",\r\n"
			+ "        \"000598-75-4\",\r\n"
			+ "        \"000608-66-2\",\r\n"
			+ "        \"000623-37-0\",\r\n"
			+ "        \"000626-93-7\",\r\n"
			+ "        \"002163-42-0\",\r\n"
			+ "        \"004457-71-0\",\r\n"
			+ "        \"006032-29-7\"\r\n"
			+ "    ]\r\n"
			+ "}";

	public Double getBiowin3(String json) {
		Gson gson=new Gson();
		JsonObject jo=gson.fromJson(json, JsonObject.class);

		//		for(Map.Entry<String,JsonElement> entry : jo.entrySet()){
		////			System.out.println(entry.getKey());
		//		     
		//			if(entry.getKey().equals("sewageTreatmentModel")) {
		//				JsonObject jo2=entry.getValue().getAsJsonObject();
		//				
		//				for(Map.Entry<String,JsonElement> entry2 : jo2.entrySet()){
		//					System.out.println(entry2.getKey());		
		//				}
		//			}
		//		    
		//		 }		

		Double predBiowin3=jo.get("sewageTreatmentModel").getAsJsonObject().get("parameters").getAsJsonObject().get("biowin3").getAsJsonObject().get("value").getAsDouble();
		//		System.out.println(predBiowin3);
		return predBiowin3;

	}

	public static String runEpiwin(String smiles) {

		HttpResponse<String> response =Unirest.get("https://episuite.app/EpiWebSuite/api/submit")
				.queryString("smiles", smiles).asString();

		if(response.isSuccess()) {
			//			System.out.println(response.getBody().toString());
			return response.getBody().toString();
		} else {
			//			System.out.println(response.getStatus());
			return "error:"+response.getStatusText();
		}

	}
	
	public static String getUrlContent(String urlString)  {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // Create a URL object
            URL url = new URL(urlString);

            // Open a connection to the URL
            connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Read the response
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error connecting to URL: " + e.getMessage());
            return null;
        } finally {
            // Close resources
            try {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        return result.toString();
    }
	
	public static String runEpiwinByCAS(String CAS) {

		String baseUrl="https://episuite.app/EpiWebSuite/api/submit";
		String CAS2=CAS;
		while (CAS2.length()<11)CAS2="0"+CAS2;

		HttpResponse<String> response =Unirest.get(baseUrl)
				.queryString("cas", CAS2).asString();

		if(response.isSuccess()) {
			//			System.out.println(response.getBody().toString());
			return response.getBody().toString();
		} else {
			//			System.out.println(response.getStatus());
			return "error:"+response.getStatus();
		}

	}
	
	public static String runEpiwin(String smiles,String baseUrl) {

		HttpResponse<String> response =Unirest.get(baseUrl+"EpiWebSuite/api/submit")
				.queryString("smiles", smiles).asString();

		if(response.isSuccess()) {
			//			System.out.println(response.getBody().toString());
			return response.getBody().toString();
		} else {
			//			System.out.println(response.getStatus());
			return "error:"+response.getStatus();
		}

	}


	public String runEpiwinLocal(int port, String smiles) {

		long t1=System.currentTimeMillis();
		HttpResponse<String> response =Unirest.get("http://localhost:"+port+"/api/submit")
				.queryString("smiles", smiles).asString();


		long t2=System.currentTimeMillis();

		//		System.out.println((t2-t1)/1000.0+" seconds");

		if(response.isSuccess()) {
			//			System.out.println(response.getBody().toString());
			return response.getBody().toString();
		} else {
			//			System.out.println(response.getStatus());
			return "error:"+response.getStatus();
		}

	}



	public List<String> getDTXSIDs(String filepath) {

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			List<String>dtxsids=new ArrayList<String>();

			while (true) {
				String Line=br.readLine();

				if(Line==null) {
					break;
				} 

				String dtxsid=Line.substring(Line.indexOf("\t")+1,Line.length());
				dtxsids.add(dtxsid);

			}

			return dtxsids;


		} catch (Exception ex) {
			//			ex.printStackTrace();
			return null;
		}

	}

	String getLastID(String filepath,String idName) {

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String Line=null;
			String lastLine=null;

			int counter=0;
			
			while (true) {
				Line=br.readLine();
				
				counter++;
				
				if(counter%1000==0) {
					System.out.println("\t"+counter);
				}
				
				
				if(Line==null) {
					break;
				} else {
					lastLine=Line;
				}
			}

			//			System.out.println(lastLine);

			Gson gson=new Gson();

			JsonObject jo=gson.fromJson(lastLine, JsonObject.class);

			return jo.get(idName).getAsString();


		} catch (Exception ex) {
			//			ex.printStackTrace();
			return null;
		}

	}

	

	
	
	int removeBeforeDTXSID(String dtxsid,String filepath) {

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			int counter=0;
			
			boolean found=false;
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) {
					break;
				} 
				counter++;
				if(Line.contains("\"dtxsid\":\""+dtxsid+"\"")) {
					found=true;
					break;
				}
			}

			
			if(!found) {
				return -1;
			}
			
			FileWriter fw=new FileWriter(filepath.replace(".json", "_deduplicated.json"));
			
			while (true) {
				String Line=br.readLine();
				if(Line==null) {
					break;
				}
				fw.write(Line+"\r\n");
				fw.flush();
				
			}			
			
			fw.close();
			return counter;

			//			System.out.println(lastLine);
//			return jo.get("dtxsid").getAsString();

		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}

	}


	String getFirstID(String filepath,String idName) {

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			String firstLine=br.readLine();
			Gson gson=new Gson();
			JsonObject jo=gson.fromJson(firstLine, JsonObject.class);
			return jo.get(idName).getAsString();
		} catch (Exception ex) {
			//			ex.printStackTrace();
			return null;
		}
	}

	int getCount(String filepath) {

		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String Line=null;

			int count=0;

			while (true) {
				Line=br.readLine();

				if(Line==null) {
					break;
				}


				count++;
			}

			return count;

		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}

	}





	

	


	/**
 	 * Runs episuite predictions using api at given port
 	 * 
 	 * It uses a single smiles file for input (it takes a subset on the fly)
	 * TODO this code needs fixing because it doesnt seem to stop running at right place
	 * 
	 * 
	 * @param filepath
	 * @param outputFolder
	 * @param port
	 * @param portMin
	 * @param portMax
	 */
	void runSubsetOfSmilesFile(String idName, String filepath, String outputFolder, int port,int portMin,int portMax) {

		//ports go from 9000 to 9015
		System.out.println("Running "+port);

		int nFiles=portMax-portMin+1;

		int filenum=port-portMin;
		Gson gson=new Gson();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String fout=outputFolder+"episuite results "+port+".json";

			FileWriter fw=null;

			String lastID=null;

			if(new File(fout).exists()) {
				fw=new FileWriter(fout,true);
				lastID=getLastID(fout,idName);
				System.out.println("lastID="+lastID);

			} else {
				fw=new FileWriter(fout,false);	
			}


			//			if(true) return;

			List<String>lines=new ArrayList<String>();

			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				lines.add(Line);
			}

			int chemicalsPerPort=lines.size()/nFiles;
			//			System.out.println(chemicalsPerPort);

			int currentPort=portMin;
			int counter=0;

			boolean start=false;
			if(lastID==null) start=true;


			for (String line:lines) {

				//				System.out.println(currentPort+"\t"+line);

				if(currentPort==port) {

					String []values=line.split("\t");

					String smiles=values[0];
					String dtxsid=values[1];

					if(lastID!=null && dtxsid.contentEquals(lastID)) {
						start=true;
						counter++;
						continue;
					}

					if(!start) {
						System.out.println(dtxsid+"\t"+counter);
						counter++;
						continue;
					}
					long t1=System.currentTimeMillis();

					String json=runEpiwinLocal(port, smiles);

					long t2=System.currentTimeMillis();

					//					System.out.println(smiles+"\t"+json.length());

					JsonObject jo=null;

					try {
						jo=gson.fromJson(json, JsonObject.class);
						JsonObject joBioconcentration=jo.get("bioconcentration").getAsJsonObject();
						double BCF=joBioconcentration.get("bioconcentrationFactor").getAsDouble();
						System.out.println(dtxsid+"\t"+smiles+"\t"+BCF+"\t"+((t2-t1)/1000.0)+" secs");

					} catch (Exception ex) {
						jo=new JsonObject();
						jo.addProperty("error", json);
					}

					jo.addProperty("smiles", smiles);
					jo.addProperty("dtxsid", dtxsid);

					fw.write(gson.toJson(jo)+"\r\n");
					fw.flush();

					//					System.out.println(dtxsid+"\t"+smiles+"\t"+joBioconcentration==null);

					//					System.out.println(currentPort+"\t"+counter+"\t"+line+"\t"+json);
				}

				counter++;

				if(counter==chemicalsPerPort && currentPort<portMax) {
					currentPort++;
					counter=0;
				}
			}
			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	



	void fixSmilesFile(String filepath, String outputFolder) {
		try {

			BufferedReader br=new BufferedReader(new FileReader(filepath));



			FileWriter fw=new FileWriter(outputFolder+"snapshot_compounds_fixed.txt");

			while (true) {
				String line=br.readLine();
				if(line==null) break;

				String [] values=line.split("\t");

				if(values.length>2) {
					String smiles=values[0]+" "+values[1];
					String dtxsid=values[2];
					fw.write(smiles+"\t"+dtxsid+"\r\n");
					System.out.println(smiles+"\t"+dtxsid);
				} else {
					fw.write(line+"\r\n");	
				}

				fw.flush();
			}

			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}




	}

	
	void runSmilesComparisonFile () {

		
		Unirest.config()
        .followRedirects(true)   
		.socketTimeout(000)
           .connectTimeout(000);

		
		String urlBase = "https://episuite.dev/";
		String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\Comptox\\000 scientists\\wen lee\\";
		String filepathInput=folder+"smiles list logKow.txt";

		try {
			
			BufferedReader br=new BufferedReader(new FileReader(filepathInput));
			br.readLine();
			
			FileWriter fw=new FileWriter(folder+"logKow results.txt");
			
			System.out.println("allMatch\tsmilesList\tpreds");
			fw.write("allMatch\tsmilesList\tpreds\r\n");
			
			while (true) {
				String Line=br.readLine();
				if(Line==null)break;
				
				String [] vals=Line.split("\t");
				
				String smiles1=vals[0];
				String smiles2=vals[1];
				
				String []vals2=smiles2.split(";");
				
				List<String>smilesList=new ArrayList<>();
				
				smilesList.add(smiles1);
				
				for (String smiles:vals2) {
					smilesList.add(smiles);
				}
				
//				System.out.println(smilesList.size());
				
				List<Double>preds=new ArrayList<>();
				
				for (String smiles:smilesList) {
					String json=runEpiwin(smiles, urlBase);
					
					TimeUnit.SECONDS.sleep(1);
					
					try {
						EpisuiteResults results=EpisuiteResults.getResults(json);
						Double pred=results.logKow.estimatedValue.value;
//						System.out.println("\t"+smiles+"\t"+pred);
						preds.add(pred);
					} catch (JsonSyntaxException ex) {
						preds.add(Double.valueOf(-9999.0));
					}
				}
				
				boolean allMatch=true;
				
				for (int i=0;i<preds.size();i++) {
					double pred=preds.get(i);
					
					for (int j=1;j<preds.size();j++) {
						double pred2=preds.get(j);
						
						if(Math.abs(pred-pred2)>0.01) {
							allMatch=false;
							break;
						}
						
						if(!allMatch)break;
					}
				}
				
				System.out.println(allMatch+"\t"+smilesList+"\t"+preds);
				fw.write(allMatch+"\t"+smilesList+"\t"+preds+"\r\n");
				fw.flush();
				
			}
			
			fw.close();
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		
	}


	public void runSmilesFile(String idName, String filepath, String outputFolder, int port) {

		//ports go from 9000 to 9015
		System.out.println("Running "+port);


		Gson gson=new Gson();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String fout=filepath.replace(".txt", ".json");

			FileWriter fw=null;

			String lastID=null;

			if(new File(fout).exists()) {
				lastID=getLastID(fout,idName);
				fw=new FileWriter(fout,true);
				System.out.println("lastID="+lastID);
			} else {
				fw=new FileWriter(fout,false);	
			}

			//			if(true) return;
			List<String>lines=new ArrayList<String>();

			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				lines.add(Line);
			}

			//			System.out.println(chemicalsPerPort);

			boolean start=false;
			if(lastID==null) start=true;


			for (String line:lines) {

				//				System.out.println(currentPort+"\t"+line);

				String []values=line.split("\t");

				String smiles=values[0];
				String id=values[1];

				if(lastID!=null && id.contentEquals(lastID)) {
					start=true;
					continue;
				}

				if(!start) {
					continue;
				}
				long t1=System.currentTimeMillis();

				String json=runEpiwinLocal(port, smiles);

				long t2=System.currentTimeMillis();

				//					System.out.println(smiles+"\t"+json.length());

				JsonObject jo=null;

				try {
					jo=gson.fromJson(json, JsonObject.class);
					
//					JsonObject joBioconcentration=jo.get("bioconcentration").getAsJsonObject();
//					double BCF=joBioconcentration.get("bioconcentrationFactor").getAsDouble();
//					System.out.println(id+"\t"+smiles+"\t"+BCF+"\t"+((t2-t1)/1000.0)+" secs");

				} catch (Exception ex) {
					jo=new JsonObject();
					jo.addProperty("error", json);
				}

				jo.addProperty("smiles", smiles);
				jo.addProperty(idName, id);

				fw.write(gson.toJson(jo)+"\r\n");
				fw.flush();

				//					System.out.println(dtxsid+"\t"+smiles+"\t"+joBioconcentration==null);
				//					System.out.println(currentPort+"\t"+counter+"\t"+line+"\t"+json);
			}
			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}




	}
	public void runSmilesFilePublicApi(String idName, String filepath, String outputFolder) {


		Gson gson=new Gson();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filepath));

			String fout=filepath.replace(".txt", ".json");

			FileWriter fw=null;

			String lastID=null;

			if(new File(fout).exists()) {
				lastID=getLastID(fout,idName);
				fw=new FileWriter(fout,true);
				System.out.println("lastID="+lastID);
			} else {
				fw=new FileWriter(fout,false);	
			}

			//			if(true) return;
			List<String>lines=new ArrayList<String>();

			while (true) {
				String Line=br.readLine();
				if(Line==null) break;
				lines.add(Line);
			}

			//			System.out.println(chemicalsPerPort);

			boolean start=false;
			if(lastID==null) start=true;

			int counter=0;

			for (String line:lines) {
				
				counter++;

				//				System.out.println(currentPort+"\t"+line);

				String []values=line.split("\t");

				String smiles=values[0];
				String id=values[1];

				if(lastID!=null && id.contentEquals(lastID)) {
					start=true;
					continue;
				}

				if(!start) {
					continue;
				}
				long t1=System.currentTimeMillis();

				String json=runEpiwin(smiles);

				long t2=System.currentTimeMillis();

				//					System.out.println(smiles+"\t"+json.length());
//				EpisuiteResults er=null;
				JsonObject jo=null;
				
				try {
				
//					JsonObject joBioconcentration=jo.get("bioconcentration").getAsJsonObject();
//					double BCF=joBioconcentration.get("bioconcentrationFactor").getAsDouble();
//					System.out.println(id+"\t"+smiles+"\t"+BCF+"\t"+((t2-t1)/1000.0)+" secs");
					
//					er=Utilities.gson.fromJson(json, EpisuiteResults.class);
					jo=Utilities.gson.fromJson(json, JsonObject.class);
							
					
//					JsonObject joLogKow=jo.get("bioconcentration").getAsJsonObject();
//					double BCF=joBioconcentration.get("bioconcentrationFactor").getAsDouble();
//					System.out.println(id+"\t"+smiles+"\t"+BCF+"\t"+((t2-t1)/1000.0)+" secs");

					
				} catch (Exception ex) {
					jo=new JsonObject();
					jo.addProperty(idName, id);
					jo.addProperty("error", json);
					jo.addProperty("smiles", smiles);
					System.out.println(counter+"\t"+id+"\t"+json+"\t"+smiles);
					fw.write(gson.toJson(jo)+"\r\n");
					continue;
				}
				
				jo.addProperty("smiles", smiles);
				jo.addProperty(idName, id);
				fw.write(gson.toJson(jo)+"\r\n");
				
				System.out.println(counter+"\t"+id+"\t"+smiles+"\tOK");

//				er.smiles=smiles;
//				if(idName.equals("dtxcid")) {
//					er.dtxcid=id;
//				} else if(idName.equals("dtxsid")) {
//					er.dtxsid=id;
//				}
//				fw.write(gson.toJson(er)+"\r\n");
				
//				if(er.logKow!=null) {
//					System.out.println(counter+"\t"+id+"\t"+er.logKow.selectedValue.value+"\t"+er.logKow.selectedValue.valueType+"\t"+er.logKow.estimatedValue.value+"\t"+er.smiles);
//				} else {
//					System.out.println(counter+"\t"+id+"\terror\t"+er.smiles);
//				}

				fw.flush();

				//					System.out.println(dtxsid+"\t"+smiles+"\t"+joBioconcentration==null);
				//					System.out.println(currentPort+"\t"+counter+"\t"+line+"\t"+json);
			}
			br.close();
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}




	}
	
	
	public void runSmilesFilePublicApiByCAS(String propertyName, String modelNameEpi, Hashtable<String, DataPoint> htDP_CAS,
			String filepathOut) {

		EpisuiteValidation ev=new EpisuiteValidation();
		
		System.out.println("Running episuite predictions for "+propertyName);
		
		int counter=0;

		Gson gson=new Gson();
		try {
			
			File fout=new File(filepathOut);
			
			LinkedHashMap<String,Double>htPredEpi=null;
			
			if(fout.exists()) {
				htPredEpi=ev.episuite.getEpisuitePredictions(false, propertyName, modelNameEpi, filepathOut,"cas");
//				System.out.println(htPredEpi.size()+"\t"+Utilities.gson.toJson(htPredEpi));
			}
			
//			if(true)return;
			
			FileWriter fw=new FileWriter(filepathOut,fout.exists());

			int countToRun=0;
			
			for (String CAS:htDP_CAS.keySet()) {
				if(!htPredEpi.containsKey(CAS))countToRun++;
			}
				
			System.out.println("countToRun="+countToRun);

			if(countToRun==0) return;
			
			
			for (String CAS:htDP_CAS.keySet()) {
				counter++;
				
				if(htPredEpi!=null && htPredEpi.containsKey(CAS)) continue;
				
				String json=runEpiwinByCAS(CAS);
				
				if(json.contains("Service Temporarily Unavailable")) {
					System.out.println("Service unavailable");
					fw.flush();
					fw.close();
					return;
				}

				JsonObject jo=null;
				
				try {
					jo=Utilities.gson.fromJson(json, JsonObject.class);
				} catch (Exception ex) {
					EpisuiteResults er=new EpisuiteResults();
					er.error=json;
					er.chemicalProperties=er.new ChemicalProperties();
					
					String CAS2=CAS;
					while (CAS2.length()<11)CAS2="0"+CAS2;
					er.chemicalProperties.cas=CAS2;
					
					System.out.println(counter+"\t"+CAS+"\terror");
					fw.write(gson.toJson(er)+"\r\n");
					continue;
				}
				
				fw.write(gson.toJson(jo)+"\r\n");
				System.out.println(counter+"\t"+CAS+"\tOK");
				fw.flush();
			}
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public void runSmilesFilePublicApi(String propertyName,String modelName,  Hashtable<String,DataPoint>htDP, String filepathOut) {

		EpisuiteValidation ev=new EpisuiteValidation();
		
		System.out.println("Running episuite predictions for "+propertyName);
		
		int counter=0;

		Gson gson=new Gson();
		try {
			
			File fout=new File(filepathOut);
			
			LinkedHashMap<String,Double>htPredEpi=null;
			
			if(fout.exists()) {
				htPredEpi=ev.episuite.getEpisuitePredictions(false, propertyName, modelName, filepathOut,"canonQsarSmiles");
				System.out.println("Loaded "+htPredEpi.size()+" predictions by qsar smiles");
			}
			
			
//			if(true)return;
			
			FileWriter fw=new FileWriter(filepathOut,fout.exists());
			
			for (String smiles:htDP.keySet()) {
				counter++;
				
				if(htPredEpi!=null && htPredEpi.containsKey(smiles)) continue;
				
				DataPoint dp=htDP.get(smiles);
				
				String json=runEpiwin(smiles);
				
				if(json.contains("Service Temporarily Unavailable")) {
					System.out.println("Service unavailable");
					fw.flush();
					fw.close();
					return;
				}

				JsonObject jo=null;
				
				try {
					jo=Utilities.gson.fromJson(json, JsonObject.class);
				} catch (Exception ex) {
					jo=new JsonObject();
					jo.addProperty("error", json);
					jo.addProperty("dtxcid", dp.getQsar_dtxcid());
					jo.addProperty("canonQsarSmiles", smiles);
					System.out.println(counter+"\t"+dp.getQsar_dtxcid()+"\t"+json+"\t"+smiles);
					fw.write(gson.toJson(jo)+"\r\n");
					continue;
				}
				
				jo.addProperty("canonQsarSmiles", smiles);
				jo.addProperty("dtxcid", dp.getQsar_dtxcid());
				fw.write(gson.toJson(jo)+"\r\n");
				System.out.println(counter+"\t"+dp.getQsar_dtxcid()+"\t"+smiles+"\tOK");
				fw.flush();
			}
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Uses qsarSmiles as a key, but original smiles is ran in episuite
	 * 
	 * @param propertyName
	 * @param modelName
	 * @param htDP
	 * @param filepathOut
	 */
	public void runSmilesFilePublicApiUsingOriginalSmiles(String propertyName,String modelName,  Hashtable<String,DataPoint>htDP, String filepathOut) {

		EpisuiteValidation ev=new EpisuiteValidation();
		
		System.out.println("Running episuite predictions for "+propertyName);
		
		int counter=0;

		Gson gson=new Gson();
		try {
			
			File fout=new File(filepathOut);
			
			LinkedHashMap<String,Double>htPredEpi=null;
			
			if(fout.exists()) {
				htPredEpi=ev.episuite.getEpisuitePredictions(false, propertyName, modelName, filepathOut,"canonQsarSmiles");
				System.out.println("Loaded "+htPredEpi.size()+" predictions by qsar smiles");
			}
			
			
//			if(true)return;
			
			FileWriter fw=new FileWriter(filepathOut,fout.exists());
			
			for (String qsarSmiles:htDP.keySet()) {
				counter++;
				
				if(htPredEpi!=null && htPredEpi.containsKey(qsarSmiles)) continue;
				
				DataPoint dp=htDP.get(qsarSmiles);
				
				String smiles=dp.checkStructure.dsstox_smiles;
				
				String json=runEpiwin(smiles);
				
				if(json.contains("Service Temporarily Unavailable")) {
					System.out.println("Service unavailable");
					fw.flush();
					fw.close();
					return;
				}

				JsonObject jo=null;
				
				try {
					jo=Utilities.gson.fromJson(json, JsonObject.class);
				} catch (Exception ex) {
					jo=new JsonObject();
					jo.addProperty("error", json);
					jo.addProperty("dtxcid", dp.getQsar_dtxcid());
					jo.addProperty("canonQsarSmiles", qsarSmiles);
					jo.addProperty("smiles", smiles);
					System.out.println(counter+"\t"+dp.getQsar_dtxcid()+"\t"+json+"\t"+qsarSmiles);
					fw.write(gson.toJson(jo)+"\r\n");
					continue;
				}
				
				jo.addProperty("canonQsarSmiles", qsarSmiles);
				jo.addProperty("smiles", smiles);
				jo.addProperty("dtxcid", dp.getQsar_dtxcid());
				fw.write(gson.toJson(jo)+"\r\n");
				System.out.println(counter+"\t"+dp.getQsar_dtxcid()+"\t"+qsarSmiles+"\tOK");
				fw.flush();
			}
			fw.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	void getJsonCounts(String folderPath) {
		File folder=new File(folderPath);

		int total=0;

		for (File file:folder.listFiles()) {
			if(!file.getName().contains(".json")) continue;
			if(file.getName().equals("sample.json")) continue;
			if(!file.getName().contains("snapshot_chemicals_to_run")) continue;


			int count=getCount(file.getAbsolutePath());
			total+=count;
			System.out.println(file.getName()+"\t"+count);
		}
		System.out.println("total\t"+total);

	}

	void getLastID_InFiles(String idName, String folderPath) {
		File folder=new File(folderPath);

		List<String>dtxsids=getDTXSIDs(folderPath+"snapshot_compounds.tsv");


		for (File file:folder.listFiles()) {
			if(!file.getName().contains(".json")) continue;
			if(file.getName().equals("sample.json")) continue;

			String dtxsidFirst=getFirstID(file.getAbsolutePath(),idName);
			String dtxsidLast=getLastID(file.getAbsolutePath(),idName);


			int indexFirst=dtxsids.indexOf(dtxsidFirst);
			int indexLast=dtxsids.indexOf(dtxsidLast);

			System.out.println(file.getName()+"\t"+dtxsidFirst+"\t"+dtxsidLast);
			System.out.println(file.getName()+"\t"+indexFirst+"\t"+indexLast);
		}


	}



	void runSingleChemicalFromAPI() {

		String smiles="C1=CC(=CC(=C1)Cl)N";//108-42-9
//		String smiles="CN1CCOCCOCCOCCN(C)C1=S";
//		String smiles="CCCO";
//		String smiles="C1=CC23C=CC45C=CC67C=CC11C=CC89C=CC%10(C=C2)C=CC2(C=C4)C=CC(C=C6)(C=C8)C46C77C11C33C57C24C%103C961";
		
		String json=runEpiwin(smiles);
		System.out.println(json);

//		startLocalCopies(9000, 9000);
//		String json=runEpiwinLocal(9000,smiles);
//		System.out.println(json);

//		Double predBiowin3=getBiowin3(json);
//		System.out.println(smiles+"\t"+predBiowin3);
		//		getBiowin3(sampleOutput);

	}


	void removeDuplicates() {
		
		
		String folder="data\\episuite\\";
		
		for(int i=9007;i<=9012;i++) {
			
			String filepath1=folder+"episuite results "+i+".json";
			String filepath2=folder+"episuite results "+(i+1)+".json";
			
			
			System.out.println("episuite results "+i+".json");
			String dtxsid=getLastID(filepath1,"dtxsid");
			
			int counter=removeBeforeDTXSID(dtxsid, filepath2);
			
			System.out.println((i+1)+"\t"+dtxsid+"\t"+counter);
			
		}
		
		
	}

	
	
	public static void main(String[] args) {
		EpisuiteWebserviceScript b=new EpisuiteWebserviceScript();
				
//		b.runSingleChemicalFromAPI();
//		b.fixSmilesFile(of+"snapshot_compounds.tsv", of);

//		startLocalCopies(9000,9011);
		b.startLocalCopies(9000,9000);

		//******************************************************************
//		String of="data\\episuite\\";
//		int port=9000;
//		int portMin=9000;
//		int portMax=9011;
//		b.runSubsetOfSmilesFile("dtxcid",of+"snapshot_compounds.tsv", of, port,portMin,portMax);

		//		******************************************************************

//		 String of="data\\episuite\\";
//		int port=9006;
//		b.runSmilesFile("dtxcid",of+"snapshot_chemicals_to_run_"+port+".txt", of, port);
		
		//Run the caret ones that got messed up:
//		b.runSmilesFile(of+"snapshot_compounds_caret.tsv", of, 9013);

//		b.removeDuplicates();
		
//		b.runSmilesComparisonFile();
		
		b.runSingleChemicalFromAPI();
		
		
		
	}

	public void startLocalCopies(int portMin,int portMax) {
		try {

			//			int port=9003;

			for (int port=portMin;port<=portMax;port++) {
				//				String jarPath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\episuite\\EpiSuite-1.0.jar";
				String jarPath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\episuite\\EpiSuite-1.0 "+port+".jar";
				String command="java -jar \""+jarPath+"\" -api -port "+port;
				Process proc = Runtime.getRuntime().exec(command);
				//				proc.waitFor();
				System.out.println(port);

			}

			//			String json=b.runEpiwinLocal(port,smiles);	


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}

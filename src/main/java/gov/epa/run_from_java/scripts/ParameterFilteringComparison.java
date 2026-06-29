package gov.epa.run_from_java.scripts;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.apache.commons.math3.distribution.TDistribution;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gov.epa.util.MatlabChart;

/**
 * @author TMARTI02
 */
public class ParameterFilteringComparison {

	// Requires imports:
	// import com.google.gson.JsonArray;
	// import com.google.gson.JsonElement;
	// import com.google.gson.JsonObject;
	// import com.google.gson.JsonPrimitive;

	private double averageStdDevOfQsarPropertyValues(Hashtable<String, JsonArray> ht) {
		double sumStd = 0.0;
		int groupCount = 0;

		for (Map.Entry<String, JsonArray> entry : ht.entrySet()) {
			JsonArray recs = entry.getValue();

			// Welford's algorithm (sample variance)
			int n = 0;
			double mean = 0.0;
			double m2 = 0.0;

			for (JsonElement je : recs) {
				if (!je.isJsonObject())
					continue;
				JsonObject rec = je.getAsJsonObject();

				JsonElement valEl = rec.get("qsar_property_value");
				if (valEl == null || valEl.isJsonNull()) {
					System.out.println("null qsar_property_value");
					continue;// shouldnt happen
				}

				Double x = valEl.getAsDouble();
				if (x == null || x.isNaN() || x.isInfinite())
					continue;

				n++;
				double delta = x - mean;
				mean += delta / n;
				double delta2 = x - mean;
				m2 += delta2 * delta;
			}

			if (n > 0) {
				double variance = (n > 1) ? (m2 / (n - 1)) : 0.0; // sample variance, 0 if only one value
				double stddev = Math.sqrt(variance);
				sumStd += stddev;
				groupCount++;
			}
		}

		return groupCount > 0 ? (sumStd / groupCount) : Double.NaN;
	}

	private Hashtable<String, JsonArray> createMappedRecordHashtable(JsonArray ja, boolean printStats) {
		Hashtable<String, JsonArray> ht = new Hashtable<>();
		for (JsonElement je : ja) {
			JsonObject rec = je.getAsJsonObject();
			String canon_qsar_smiles = rec.get("canon_qsar_smiles").getAsString();
			if (ht.containsKey(canon_qsar_smiles)) {
				JsonArray recs = ht.get(canon_qsar_smiles);
				recs.add(rec);
			} else {
				JsonArray recs = new JsonArray();
				recs.add(rec);
				ht.put(canon_qsar_smiles, recs);
			}
		}

		if (printStats) {
			System.out.println("Number of records=" + ja.size());
			System.out.println("Number of unique smiles=" + ht.size());
			DecimalFormat df = new DecimalFormat("0.000");
			System.out.println("Avg std.dev. = " + df.format(averageStdDevOfQsarPropertyValues(ht)));
		}

		return ht;
	}

	/**
	 * Removes based on matching a string value for a parameter. It removes records
	 * from JsonArray (ja)
	 * 
	 * @param ja             Mapped records as JsonArray
	 * @param parameterName
	 * @param parameterValue
	 * @param required
	 * @return
	 */
	private Hashtable<String, JsonArray> removeBasedOnStringParameter(JsonArray ja, String parameterName,
			String parameterValue, boolean required) {

		// Baseline (before filtering)
		Hashtable<String, JsonArray> htBefore = createMappedRecordHashtable(ja, false);
		int recordsBefore = ja.size();
		int uniqueBefore = htBefore.size();
		double avgStdBefore = averageStdDevOfQsarPropertyValues(htBefore);

		// Filter in-place
		Iterator<JsonElement> it = ja.iterator();
		while (it.hasNext()) {
			JsonObject rec = it.next().getAsJsonObject();
			JsonElement pEl = rec.get(parameterName);

			// Remove if required and missing/null
			if ((pEl == null || pEl.isJsonNull()) && required) {
				it.remove();
				continue;
			}

			// If present, ensure it matches the required value (case-insensitive)
			if (pEl != null && !pEl.isJsonNull()) {
				String val = pEl.getAsString();
				if (val == null || !parameterValue.equalsIgnoreCase(val)) {
					it.remove();
				}
			}
		}

		// After filtering
		System.out.println("\nAfter filtering based on " + parameterName + " = " + parameterValue);
		Hashtable<String, JsonArray> htAfter = createMappedRecordHashtable(ja, true);
		int uniqueAfter = htAfter.size();
		double avgStdAfter = averageStdDevOfQsarPropertyValues(htAfter);
		int recordsAfter = ja.size();

		// Report percent changes
		DecimalFormat pdf = new DecimalFormat("0.0");
		System.out.println("Percent change in records: " + percentChange(recordsBefore, recordsAfter, pdf));
		System.out.println("Percent change in unique smiles: " + percentChange(uniqueBefore, uniqueAfter, pdf));
		System.out.println("Percent change in avg std. dev.: " + percentChange(avgStdBefore, avgStdAfter, pdf));

		return htAfter;
	}

	private Hashtable<String, JsonArray> removeBasedOnDurationVsT80(JsonArray ja, boolean required, double factor) {

		// Baseline (before filtering)
		Hashtable<String, JsonArray> htBefore = createMappedRecordHashtable(ja, false);
		int uniqueBefore = htBefore.size();
		double avgStdBefore = averageStdDevOfQsarPropertyValues(htBefore);
		int recordsBefore = ja.size();

		// Filter in-place
		Iterator<JsonElement> it = ja.iterator();
		while (it.hasNext()) {
			JsonObject rec = it.next().getAsJsonObject();
			JsonElement paramED = rec.get("Exposure duration");
			JsonElement paramT80 = rec.get("T80");

			// Remove if required and missing/null
			if ((paramED == null || paramED.isJsonNull() || paramT80 == null || paramT80.isJsonNull()) && required) {
//	            System.out.println(rec.get("source_casrn")+"\tmissing exposure or T80");
				it.remove();
				continue;
			}

			// If present, ensure it matches the required value (case-insensitive)
			if (paramED != null && !paramED.isJsonNull() && paramT80 != null && !paramT80.isJsonNull()) {
				double ED = Double.parseDouble(paramED.getAsString().replace(" days", ""));
				double T80 = Double.parseDouble(paramT80.getAsString().replace(" days", ""));

				if (ED < T80 / factor)// TODO dont check if kinetic method?
					it.remove();
			}
		}

		// After filtering
		System.out.println("\nAfter filtering based on exposure duration vs T80");
		Hashtable<String, JsonArray> htAfter = createMappedRecordHashtable(ja, true);
		int uniqueAfter = htAfter.size();
		double avgStdAfter = averageStdDevOfQsarPropertyValues(htAfter);
		int recordsAfter = ja.size();

		// Report percent changes
		DecimalFormat pdf = new DecimalFormat("0.0");
		System.out.println("Percent change in records: " + percentChange(recordsBefore, recordsAfter, pdf));
		System.out.println("Percent change in unique smiles: " + percentChange(uniqueBefore, uniqueAfter, pdf));
		System.out.println("Percent change in avg std. dev.: " + percentChange(avgStdBefore, avgStdAfter, pdf));

		return htAfter;
	}

	private String percentChange(double before, double after, DecimalFormat df) {
		if (Double.isNaN(before) || Double.isNaN(after)) {
			return "N/A";
		}
		if (before == 0.0) {
			if (after == 0.0)
				return "0%";
			return "N/A (baseline is 0)";
		}
		double pct = ((after - before) / before) * 100.0;
		return df.format(pct) + "%";
	}

	/*
	 * This looks at change in the avg. std. dev for the chemicals that pass and
	 * fail the parameter check
	 * 
	 * @param ja
	 * 
	 * @param parameterName
	 * 
	 * @param parameterValue
	 * 
	 * @param required
	 */
	private void removeBasedOnStringParameter2(JsonArray ja, String parameterName, String parameterValue,
			boolean required) {

		// Group once
		Hashtable<String, JsonArray> groups = createMappedRecordHashtable(ja, false);

		// After-map of kept records
		Hashtable<String, JsonArray> htAfter = new Hashtable<>();

		int recordsBefore = 0;
		int recordsAfter = 0;

		double sumStdBefore = 0.0;
		double sumStdAfter = 0.0;
		int groupsCountBefore = 0; // groups with numeric values before
		int groupsCountAfter = 0; // groups with numeric values after

		for (Map.Entry<String, JsonArray> e : groups.entrySet()) {
			String qsarSmiles = e.getKey();
			JsonArray recs = e.getValue();

			boolean haveGood = false;
			boolean haveBad = false;

			JsonArray kept = new JsonArray();

			// Classify and collect kept records
			for (JsonElement je : recs) {
				JsonObject rec = je.getAsJsonObject();
				JsonElement pEl = rec.get(parameterName);

				boolean keep;
				if (pEl == null || pEl.isJsonNull()) {
					// Missing parameter: keep if not required, otherwise it's a fail
					keep = !required;
				} else {
					String val = pEl.getAsString();
					keep = (val != null && parameterValue.equalsIgnoreCase(val));
				}

				if (keep) {
					haveGood = true;
					kept.add(rec);
				} else {
					haveBad = true;
				}
			}

			// Only consider groups that have both pass and fail
			if (haveGood && haveBad) {
				recordsBefore += recs.size();

				// Std dev BEFORE (all records in this group)
				double stdBefore = stdDevForQsarPropertyValues(recs);
				if (!Double.isNaN(stdBefore)) {
					sumStdBefore += stdBefore;
					groupsCountBefore++;
				}

				// AFTER: keep only passing records
				if (kept.size() > 0) {
					htAfter.put(qsarSmiles, kept);
					recordsAfter += kept.size();

					double stdAfter = stdDevForQsarPropertyValues(kept);
					if (!Double.isNaN(stdAfter)) {
						sumStdAfter += stdAfter;
						groupsCountAfter++;
					}
				}
			}
		}

		double avgStdBefore = (groupsCountBefore > 0) ? (sumStdBefore / groupsCountBefore) : Double.NaN;
		double avgStdAfter = (groupsCountAfter > 0) ? (sumStdAfter / groupsCountAfter) : Double.NaN;

		System.out.println("\nFor chemicals with both pass and fail records, after filtering based on " + parameterName
				+ " = " + parameterValue);
		DecimalFormat pdf = new DecimalFormat("0.0");
		System.out.println("Percent change in records: " + percentChange(recordsBefore, recordsAfter, pdf));
		System.out.println("Percent change in avg std. dev.: " + percentChange(avgStdBefore, avgStdAfter, pdf));

		// If you need htAfter elsewhere, you can return it or store it as needed.
	}
	
	/**
	 * Needs at least 2 good and bad
	 * 
	 * @param ja
	 * @param parameterName
	 * @param parameterValue
	 * @param required
	 */
	private void removeBasedOnStringParameter3(
	        JsonArray ja, String parameterName, String parameterValue, boolean required) {

	    // Group once
	    Hashtable<String, JsonArray> groups = createMappedRecordHashtable(ja, false);

	    // After-map of kept records
	    Hashtable<String, JsonArray> htAfter = new Hashtable<>();

	    int recordsBefore = 0;
	    int recordsAfter = 0;

	    double sumStdBefore = 0.0;
	    double sumStdAfter = 0.0;
	    int groupsCountBefore = 0; // groups with numeric values before
	    int groupsCountAfter = 0;  // groups with numeric values after

	    int chemicalsIncluded = 0;

	    for (Map.Entry<String, JsonArray> e : groups.entrySet()) {
	        String qsarSmiles = e.getKey();
	        JsonArray recs = e.getValue();

	        int goodCount = 0;
	        int badCount = 0;

	        JsonArray kept = new JsonArray();

	        // Classify and collect kept records
	        for (JsonElement je : recs) {
	            JsonObject rec = je.getAsJsonObject();
	            JsonElement pEl = rec.get(parameterName);

	            boolean keep;
	            if (pEl == null || pEl.isJsonNull()) {
	                // Missing parameter: keep if not required, otherwise it's a fail
	                keep = !required;
	            } else {
	                String val = pEl.getAsString();
	                keep = (val != null && parameterValue.equalsIgnoreCase(val));
	            }

	            if (keep) {
	                goodCount++;
	                kept.add(rec);
	            } else {
	                badCount++;
	            }
	        }

	        // Only consider groups that have at least 2 pass (good) and 2 fail (bad)
	        if (goodCount >= 2 && badCount >= 2) {
	            chemicalsIncluded++;
	            recordsBefore += recs.size();

	            // Std dev BEFORE (all records in this group)
	            double stdBefore = stdDevForQsarPropertyValues(recs);
	            if (!Double.isNaN(stdBefore)) {
	                sumStdBefore += stdBefore;
	                groupsCountBefore++;
	            }

	            // AFTER: keep only passing records (we already know goodCount >= 2)
	            htAfter.put(qsarSmiles, kept);
	            recordsAfter += kept.size();

	            double stdAfter = stdDevForQsarPropertyValues(kept);
	            if (!Double.isNaN(stdAfter)) {
	                sumStdAfter += stdAfter;
	                groupsCountAfter++;
	            }
	        }
	    }

	    double avgStdBefore = (groupsCountBefore > 0) ? (sumStdBefore / groupsCountBefore) : Double.NaN;
	    double avgStdAfter  = (groupsCountAfter  > 0) ? (sumStdAfter  / groupsCountAfter)  : Double.NaN;

	    System.out.println("\nFor chemicals with at least 2 pass and 2 fail records, after filtering based on "
	            + parameterName + " = " + parameterValue);
	    System.out.println("Chemicals included: " + chemicalsIncluded);

	    DecimalFormat pdf = new DecimalFormat("0.0");
	    System.out.println("Percent change in records: " + percentChange(recordsBefore, recordsAfter, pdf));
	    System.out.println("Percent change in avg std. dev.: " + percentChange(avgStdBefore, avgStdAfter, pdf));

	    // If you need htAfter elsewhere, you can return it or store it as needed.
	}

	private int countRecords(Hashtable<String, JsonArray> ht) {
		int count = 0;
		for (Map.Entry<String, JsonArray> e : ht.entrySet()) {
			count += e.getValue().size();
		}
		return count;
	}

	private Hashtable<String, JsonArray> removeBasedOnWaterConcentrationVsWaterSolubility(JsonArray ja,
			boolean required, double factor) {

		// Baseline (before filtering)
		Hashtable<String, JsonArray> htBefore = createMappedRecordHashtable(ja, false);
		int uniqueBefore = htBefore.size();
		double avgStdBefore = averageStdDevOfQsarPropertyValues(htBefore);
		int recordsBefore = ja.size();

		// Filter in-place
		Iterator<JsonElement> it = ja.iterator();
		while (it.hasNext()) {
			JsonObject rec = it.next().getAsJsonObject();
			JsonElement paramWC = rec.get("Water concentration");
			JsonElement paramWS = rec.get("Water solubility");

			// Remove if required and missing/null
			if ((paramWC == null || paramWC.isJsonNull() || paramWS == null || paramWS.isJsonNull()) && required) {
				it.remove();
				continue;
			}

			// If present, ensure it matches the required value (case-insensitive)
			if (paramWC != null && !paramWC.isJsonNull() && paramWS != null && !paramWS.isJsonNull()) {
				double WC = Double.parseDouble(paramWC.getAsString().replace(" g/L", ""));
				double WS = Double.parseDouble(paramWS.getAsString().replace(" g/L", ""));
				if (WC > factor * WS)
					it.remove();
			}
		}

		// After filtering
		System.out.println("\nAfter filtering based on water concentration vs solubility");
		Hashtable<String, JsonArray> htAfter = createMappedRecordHashtable(ja, true);
		int uniqueAfter = htAfter.size();
		double avgStdAfter = averageStdDevOfQsarPropertyValues(htAfter);
		int recordsAfter = ja.size();

		// Report percent changes
		DecimalFormat pdf = new DecimalFormat("0.0");
		System.out.println("Percent change in records: " + percentChange(recordsBefore, recordsAfter, pdf));
		System.out.println("Percent change in unique smiles: " + percentChange(uniqueBefore, uniqueAfter, pdf));
		System.out.println("Percent change in avg std. dev.: " + percentChange(avgStdBefore, avgStdAfter, pdf));

		return htAfter;
	}

	
	private void pairedComparisonAcrossChemicals(
	        Hashtable<String, JsonArray> groupsBefore,    // records per canon_qsar_smiles BEFORE filtering
	        Hashtable<String, JsonArray> groupsAfter,     // records per canon_qsar_smiles AFTER filtering (kept)
	        String test,                                   // "log-t" or "wilcoxon"
	        int bootstrapIterations,                       // e.g., 10000
	        long bootstrapSeed,
	        double alpha) {                                // e.g., 0.05

	    // Collect per-chemical SDs where both sides have n >= 2 numeric values
	    List<Double> sBefore = new ArrayList<>();
	    List<Double> sAfter  = new ArrayList<>();

	    Set<String> keys = new HashSet<>(groupsBefore.keySet());
	    keys.retainAll(groupsAfter.keySet());

	    for (String key : keys) {
	        JsonArray recsBefore = groupsBefore.get(key);
	        JsonArray recsAfter  = groupsAfter.get(key);

	        int nB = countNumericQsarValues(recsBefore);
	        int nA = countNumericQsarValues(recsAfter);
	        if (nB >= 2 && nA >= 2) {
	            double sdB = stdDevForQsarPropertyValues(recsBefore);
	            double sdA = stdDevForQsarPropertyValues(recsAfter);
	            if (!Double.isNaN(sdB) && !Double.isNaN(sdA)) {
	                sBefore.add(sdB);
	                sAfter.add(sdA);
	            }
	        }
	    }

	    int N = sBefore.size();
	    if (N == 0) {
	        System.out.println("No paired chemicals with n >= 2 before and after.");
	        return;
	    }

	    double[] before = toPrimitive(sBefore);
	    double[] after  = toPrimitive(sAfter);

	    // Raw paired differences: after - before
	    double[] diffs = new double[N];
	    for (int i = 0; i < N; i++) diffs[i] = after[i] - before[i];

	    double medianChange = median(diffs);
	    double[] medianCI = bootstrapMedianCI(diffs, bootstrapIterations, bootstrapSeed, 0.025, 0.975);

	    DecimalFormat df1 = new DecimalFormat("0.0");
	    DecimalFormat df3 = new DecimalFormat("0.000");

	    System.out.println("Paired chemicals used: " + N);
	    System.out.println("Median change (after - before): " + df3.format(medianChange));
	    System.out.println("Bootstrap 95% CI for median change: [" +
	            df3.format(medianCI[0]) + ", " + df3.format(medianCI[1]) + "]");

	    if ("log-t".equalsIgnoreCase(test)) {
	        // Paired t-test on log(SD)
	        List<Double> lb = new ArrayList<>();
	        List<Double> la = new ArrayList<>();
	        for (int i = 0; i < N; i++) {
	            if (before[i] > 0.0 && after[i] > 0.0) {
	                lb.add(Math.log(before[i]));
	                la.add(Math.log(after[i]));
	            }
	        }
	        int M = lb.size();
	        if (M < 2) {
	            System.out.println("Not enough positive SD pairs for log-t test.");
	            return;
	        }

	        double[] logBefore = toPrimitive(lb);
	        double[] logAfter  = toPrimitive(la);

	        double[] d = new double[M];
	        double meanLogDiff = 0.0;
	        for (int i = 0; i < M; i++) {
	            d[i] = logAfter[i] - logBefore[i];
	            meanLogDiff += d[i];
	        }
	        meanLogDiff /= M;

	        double s2 = 0.0;
	        for (double v : d) s2 += (v - meanLogDiff) * (v - meanLogDiff);
	        s2 /= (M - 1);
	        double sd = Math.sqrt(s2);
	        double se = sd / Math.sqrt(M);

	        // 95% CI on mean log-diff using t critical
	        double alphaTwoSided = alpha;
	        double tcrit;
	        Double pvalueTwoSided = null;

	        try {
	            // Apache Commons Math (preferred)
	            TDistribution tDist = new TDistribution(M - 1);
	            tcrit = tDist.inverseCumulativeProbability(1.0 - alphaTwoSided / 2.0);
	            TTest ttest = new TTest();
	            pvalueTwoSided = ttest.pairedTTest(logBefore, logAfter);
	        } catch (Throwable ignored) {
	            // Fallback: normal approx
	            tcrit = 1.96;
	        }

	        double lowerLog = meanLogDiff - tcrit * se;
	        double upperLog = meanLogDiff + tcrit * se;

	        double gmr = Math.exp(meanLogDiff);        // after/before ratio
	        double lowerRatio = Math.exp(lowerLog);
	        double upperRatio = Math.exp(upperLog);

	        System.out.println("Paired t-test on log(SD):");
	        System.out.println("  Geometric mean ratio (after/before): " + df3.format(gmr) +
	                "  => " + df1.format((gmr - 1.0) * 100.0) + "%");
	        System.out.println("  95% CI for ratio: [" + df3.format(lowerRatio) + ", " + df3.format(upperRatio) + "]" +
	                "  => [" + df1.format((lowerRatio - 1.0) * 100.0) + "%, " +
	                df1.format((upperRatio - 1.0) * 100.0) + "%]");

	        if (pvalueTwoSided != null) {
	            System.out.println("  p-value (two-sided): " + pvalueTwoSided);
	            // Decision statements
	            boolean rejectTwoSided = pvalueTwoSided <= alphaTwoSided;
	            System.out.println("  Decision at alpha=" + alphaTwoSided + " (two-sided): " +
	                    (rejectTwoSided ? "REJECT H0 (no change)" : "Fail to reject H0"));

	            // Directional support (decrease or increase) using two-sided p-value + observed direction
	            if (rejectTwoSided) {
	                if (meanLogDiff < 0) {
	                    System.out.println("  Conclusion: evidence supports a DECREASE in SD (after < before).");
	                } else if (meanLogDiff > 0) {
	                    System.out.println("  Conclusion: evidence supports an INCREASE in SD (after > before).");
	                } else {
	                    System.out.println("  Conclusion: mean effect is ~0.");
	                }
	            } else {
	                System.out.println("  Conclusion: insufficient evidence to claim a change at alpha=" + alphaTwoSided + ".");
	            }
	        } else {
	            System.out.println("  p-value not computed (Apache Commons Math not found). " +
	                    "Effect direction: " + (meanLogDiff < 0 ? "decrease" : (meanLogDiff > 0 ? "increase" : "no change")));
	        }

	    } else if ("wilcoxon".equalsIgnoreCase(test)) {
	        // Wilcoxon signed-rank test on raw differences (after - before)
	        Double pvalueTwoSided = null;
	        try {
	            WilcoxonSignedRankTest w = new WilcoxonSignedRankTest();
	            pvalueTwoSided = w.wilcoxonSignedRankTest(before, after, false); // two-sided (normal approx)
	        } catch (Throwable ignored) {
	            // If commons-math not available, skip p-value
	        }

	        System.out.println("Wilcoxon signed-rank test on SD differences (after - before):");
	        if (pvalueTwoSided != null) {
	            System.out.println("  p-value (two-sided): " + pvalueTwoSided);
	            boolean rejectTwoSided = pvalueTwoSided <= alpha;
	            System.out.println("  Decision at alpha=" + alpha + " (two-sided): " +
	                    (rejectTwoSided ? "REJECT H0 (no change)" : "Fail to reject H0"));

	            // Use median change to state direction
	            if (rejectTwoSided) {
	                if (medianChange < 0) {
	                    System.out.println("  Conclusion: evidence supports a DECREASE in SD (after < before).");
	                } else if (medianChange > 0) {
	                    System.out.println("  Conclusion: evidence supports an INCREASE in SD (after > before).");
	                } else {
	                    System.out.println("  Conclusion: median effect is ~0.");
	                }
	            } else {
	                System.out.println("  Conclusion: insufficient evidence to claim a change at alpha=" + alpha + ".");
	            }
	        } else {
	            System.out.println("  p-value not computed (Apache Commons Math not found). " +
	                    "Effect direction (by median): " + (medianChange < 0 ? "decrease" : (medianChange > 0 ? "increase" : "no change")));
	        }
	    } else {
	        System.out.println("Unknown test: " + test + " (use \"log-t\" or \"wilcoxon\")");
	    }
	}

	/* ------------ Helpers (reuse your stdDev function if already present) ------------ */

	private int countNumericQsarValues(JsonArray recs) {
	    int n = 0;
	    for (JsonElement je : recs) {
	        if (!je.isJsonObject()) continue;
	        JsonObject rec = je.getAsJsonObject();
	        JsonElement valEl = rec.get("qsar_property_value");
	        if (valEl == null || valEl.isJsonNull()) continue;
	        if (valEl.isJsonPrimitive()) {
	            if (valEl.getAsJsonPrimitive().isNumber()) {
	                double x = valEl.getAsDouble();
	                if (!Double.isNaN(x) && !Double.isInfinite(x)) n++;
	            } else if (valEl.getAsJsonPrimitive().isString()) {
	                try {
	                    double x = Double.parseDouble(valEl.getAsString());
	                    if (!Double.isNaN(x) && !Double.isInfinite(x)) n++;
	                } catch (NumberFormatException ignored) { }
	            }
	        }
	    }
	    return n;
	}

	private double stdDevForQsarPropertyValues(JsonArray recs) {
	    // Welford's algorithm (sample variance)
	    int n = 0;
	    double mean = 0.0;
	    double m2 = 0.0;

	    for (JsonElement je : recs) {
	        if (!je.isJsonObject()) continue;
	        JsonObject rec = je.getAsJsonObject();

	        JsonElement valEl = rec.get("qsar_property_value");
	        if (valEl == null || valEl.isJsonNull()) continue;

	        Double x = null;
	        if (valEl.isJsonPrimitive()) {
	            if (valEl.getAsJsonPrimitive().isNumber()) {
	                x = valEl.getAsDouble();
	            } else if (valEl.getAsJsonPrimitive().isString()) {
	                try {
	                    x = Double.parseDouble(valEl.getAsString());
	                } catch (NumberFormatException ignored) { }
	            }
	        }
	        if (x == null || Double.isNaN(x) || Double.isInfinite(x)) continue;

	        n++;
	        double delta = x - mean;
	        mean += delta / n;
	        double delta2 = x - mean;
	        m2 += delta2 * delta;
	    }

	    if (n == 0) return Double.NaN;
	    if (n == 1) return 0.0; // define std dev of single value as 0
	    double variance = m2 / (n - 1);
	    return Math.sqrt(variance);
	}

	private double[] toPrimitive(List<Double> list) {
	    double[] a = new double[list.size()];
	    for (int i = 0; i < list.size(); i++) a[i] = list.get(i);
	    return a;
	}

	private double median(double[] a) {
	    double[] copy = Arrays.copyOf(a, a.length);
	    Arrays.sort(copy);
	    int n = copy.length;
	    if (n % 2 == 1) return copy[n / 2];
	    return 0.5 * (copy[n / 2 - 1] + copy[n / 2]);
	}
	
	// Median of the numeric field valueField in each record of recs.
	// Returns NaN if no numeric values are found.
	private double medianForQsarPropertyValues(JsonArray recs, String valueField) {
	    List<Double> vals = new ArrayList<>();
	    for (JsonElement el : recs) {
	    	JsonObject jo=el.getAsJsonObject();
             vals.add(Double.parseDouble(jo.get(valueField).getAsString()));
	    }
	    if (vals.isEmpty()) return Double.NaN;
	    Collections.sort(vals);
	    return computeMedianOfSorted(vals);
	}

	private boolean isFinite(double v) {
	    return !Double.isNaN(v) && !Double.isInfinite(v);
	}

	private double computeMedianOfSorted(List<Double> sortedVals) {
	    int n = sortedVals.size();
	    if (n % 2 == 1) {
	        return sortedVals.get(n / 2);
	    } else {
	        return 0.5 * (sortedVals.get(n / 2 - 1) + sortedVals.get(n / 2));
	    }
	}

	// Percentile from a sorted array using linear interpolation between closest ranks
	private double percentileFromSorted(double[] sorted, double p) {
	    if (sorted.length == 0) return Double.NaN;
	    if (p <= 0) return sorted[0];
	    if (p >= 1) return sorted[sorted.length - 1];
	    double pos = p * (sorted.length - 1);
	    int idx = (int) Math.floor(pos);
	    double frac = pos - idx;
	    if (idx + 1 < sorted.length) {
	        return sorted[idx] * (1 - frac) + sorted[idx + 1] * frac;
	    } else {
	        return sorted[idx];
	    }
	}

	private double[] bootstrapMedianCI(double[] data, int B, long seed, double alphaL, double alphaU) {
	    if (data.length == 0) return new double[] {Double.NaN, Double.NaN};
	    Random rng = new Random(seed);
	    double[] meds = new double[B];
	    int n = data.length;
	    for (int b = 0; b < B; b++) {
	        double[] sample = new double[n];
	        for (int i = 0; i < n; i++) {
	            int j = rng.nextInt(n);
	            sample[i] = data[j];
	        }
	        meds[b] = median(sample);
	    }
	    Arrays.sort(meds);
	    double lo = percentileFromSorted(meds, alphaL);
	    double hi = percentileFromSorted(meds, alphaU);
	    return new double[] {lo, hi};
	}
	
	
	

	
	private void pairedMedianComparisonAcrossChemicalsWilcoxon(
	        Hashtable<String, JsonArray> groupsBefore,   // records per canon_qsar_smiles BEFORE filtering
	        Hashtable<String, JsonArray> groupsAfter,    // records per canon_qsar_smiles AFTER filtering (kept)
	        int bootstrapIterations,                     // e.g., 10000
	        long bootstrapSeed,
	        double alpha) {                              // e.g., 0.05

	    // Collect per-chemical medians where both sides have at least 1 numeric value
	    List<Double> mediansBefore = new ArrayList<>();
	    List<Double> mediansAfter  = new ArrayList<>();

	    Set<String> keys = new HashSet<>(groupsBefore.keySet());
	    keys.retainAll(groupsAfter.keySet());

	    for (String key : keys) {
	        JsonArray recsBefore = groupsBefore.get(key);
	        JsonArray recsAfter  = groupsAfter.get(key);

	        int nB = countNumericQsarValues(recsBefore);
	        int nA = countNumericQsarValues(recsAfter);
	        
//	        System.out.println(nB+"\t"+nA);

	        // Require each side to have at least 1 numeric value to define a median
	        if (nB >= 1 && nA >= 1) {
	            double medB = medianForQsarPropertyValues(recsBefore,"qsar_property_value");
	            
	            double medA = medianForQsarPropertyValues(recsAfter,"qsar_property_value");
	            
	            if (!Double.isNaN(medB) && !Double.isNaN(medA)) {
	                mediansBefore.add(medB);
	                mediansAfter.add(medA);
	                System.out.println(medB+"\t"+medA);
	            }
	        }
	    }
	    
	    
	    
	    
	    

	    int N = mediansBefore.size();
	    if (N == 0) {
	        System.out.println("No paired chemicals with n >= 1 before and after.");
	        return;
	    }

	    double[] before = toPrimitive(mediansBefore);
	    double[] after  = toPrimitive(mediansAfter);

	    // Paired differences: after - before
	    double[] diffs = new double[N];
	    for (int i = 0; i < N; i++) diffs[i] = after[i] - before[i];

	    double medianChange = median(diffs);
	    double[] medianCI = bootstrapMedianCI(diffs, bootstrapIterations, bootstrapSeed, 0.025, 0.975);

	    DecimalFormat df1 = new DecimalFormat("0.0");
	    DecimalFormat df3 = new DecimalFormat("0.000");

	    System.out.println("Paired chemicals used (medians): " + N);
	    System.out.println("Median change in median (after - before): " + df3.format(medianChange));
	    System.out.println("Bootstrap 95% CI for median change: [" +
	            df3.format(medianCI[0]) + ", " + df3.format(medianCI[1]) + "]");

	    // Wilcoxon signed-rank test on per-chemical medians (paired)
	    Double pvalueTwoSided = null;
	    try {
	        // Apache Commons Math: two-sided p-value; false => normal approximation (faster for larger N)
	        WilcoxonSignedRankTest w = new WilcoxonSignedRankTest();
	        pvalueTwoSided = w.wilcoxonSignedRankTest(before, after, false);
	    } catch (Throwable ignored) {
	        // If commons-math not available, skip p-value
	    }

	    System.out.println("Wilcoxon signed-rank test on per-chemical medians (after vs before):");
	    if (pvalueTwoSided != null) {
	        System.out.println("  p-value (two-sided): " + pvalueTwoSided);
	        boolean rejectTwoSided = pvalueTwoSided <= alpha;
	        System.out.println("  Decision at alpha=" + alpha + " (two-sided): " +
	                (rejectTwoSided ? "REJECT H0 (no change)" : "Fail to reject H0"));

	        if (rejectTwoSided) {
	            if (medianChange < 0) {
	                System.out.println("  Conclusion: evidence supports a DECREASE in median (after < before).");
	            } else if (medianChange > 0) {
	                System.out.println("  Conclusion: evidence supports an INCREASE in median (after > before).");
	            } else {
	                System.out.println("  Conclusion: median effect is ~0.");
	            }
	        } else {
	            System.out.println("  Conclusion: insufficient evidence to claim a change at alpha=" + alpha + ".");
	        }
	    } else {
	        System.out.println("  p-value not computed (Apache Commons Math not found). " +
	                "Effect direction (by median): " +
	                (medianChange < 0 ? "decrease" : (medianChange > 0 ? "increase" : "no change")));
	    }
	}
	
	private void pairedComparisonForFilter(
	        JsonArray ja,
	        String parameterName,
	        String parameterValue,
	        boolean required,
	        String test,                // "log-t" or "wilcoxon"
	        int bootstrapIterations,    // e.g., 10000
	        long bootstrapSeed,
	        double alpha) {

	    // Group once
	    Hashtable<String, JsonArray> groups = createMappedRecordHashtable(ja, false);

	    // Build maps only for chemicals that have BOTH pass and fail
	    Hashtable<String, JsonArray> htBeforeSelected = new Hashtable<>();
	    Hashtable<String, JsonArray> htAfterKept      = new Hashtable<>();

	    for (Map.Entry<String, JsonArray> e : groups.entrySet()) {
	        String qsarSmiles = e.getKey();
	        JsonArray recs = e.getValue();

	        boolean haveGood = false;
	        boolean haveBad  = false;
	        JsonArray kept = new JsonArray();

	        for (JsonElement je : recs) {
	            JsonObject rec = je.getAsJsonObject();
	            JsonElement pEl = rec.get(parameterName);

	            boolean keep;
	            if (pEl == null || pEl.isJsonNull()) {
	                // Missing parameter: keep if not required; fail if required
	                keep = !required;
	            } else {
	                String val = pEl.getAsString();
	                keep = (val != null && parameterValue.equalsIgnoreCase(val));
	            }

	            if (keep) {
	                haveGood = true;
	                kept.add(rec);
	            } else {
	                haveBad = true;
	            }
	        }

	        if (haveGood && haveBad) {
	            htBeforeSelected.put(qsarSmiles, recs); // all records before
	            htAfterKept.put(qsarSmiles, kept);      // only passing records after
	        }
	    }
	    
	    System.out.println("\n"+parameterName+"\t"+test);
	    System.out.println(htBeforeSelected.size());
	    System.out.println(htAfterKept.size());

	    if (test!=null) {
		    pairedComparisonAcrossChemicals(htBeforeSelected, htAfterKept, test, bootstrapIterations, bootstrapSeed, alpha);
	    } else {
		    pairedMedianComparisonAcrossChemicalsWilcoxon(htBeforeSelected, htAfterKept, 10000, bootstrapSeed, alpha);
	    }
	    
	    
	}
	
	
	private void pairedComparisonForFilter(
	        JsonArray ja,
	        String parameterName,
	        String valueA,              // condition A (treated as "before")
	        String valueB,              // condition B (treated as "after")
	        boolean required,           // if true, records missing parameter are simply ignored (not assigned to A or B)
	        String test,                // "log-t" or "wilcoxon"
	        int bootstrapIterations,    // e.g., 10000
	        long bootstrapSeed,
	        double alpha) {             // e.g., 0.05

	    // Group once by canon_qsar_smiles (no printing)
	    Hashtable<String, JsonArray> groups = createMappedRecordHashtable(ja, false);

	    // Build two per-chemical maps: A (valueA) and B (valueB)
	    Hashtable<String, JsonArray> groupsA = new Hashtable<>();
	    Hashtable<String, JsonArray> groupsB = new Hashtable<>();

	    int candidateChemicals = 0;

	    for (Map.Entry<String, JsonArray> e : groups.entrySet()) {
	        String qsarSmiles = e.getKey();
	        JsonArray recs = e.getValue();

	        JsonArray recsA = new JsonArray();
	        JsonArray recsB = new JsonArray();

	        for (JsonElement je : recs) {
	            if (!je.isJsonObject()) continue;
	            JsonObject rec = je.getAsJsonObject();

	            JsonElement pEl = rec.get(parameterName);
	            if (pEl == null || pEl.isJsonNull()) {
	                // Missing parameter: do not include this record in either group
	                // (required doesn't force assignment; it only means "we won't keep missing")
	                continue;
	            }

	            String val = null;
	            try { val = pEl.getAsString(); } catch (Exception ignore) { /* not a string */ }
	            if (val == null) continue;

	            if (val.equalsIgnoreCase(valueA)) {
	                recsA.add(rec);
	            } else if (val.equalsIgnoreCase(valueB)) {
	                recsB.add(rec);
	            }
	        }

	        // Only keep chemicals that have at least one record in BOTH A and B
	        if (recsA.size() > 0 && recsB.size() > 0) {
	            groupsA.put(qsarSmiles, recsA);
	            groupsB.put(qsarSmiles, recsB);
	            candidateChemicals++;
	        }
	    }

	    System.out.println("Comparing " + parameterName + ": " + valueA + " (A) vs " + valueB + " (B)");
	    System.out.println("Chemicals with data in both groups (A and B): " + candidateChemicals);

	    // Run paired comparison (treat A as "before" and B as "after")
	    pairedComparisonAcrossChemicals(groupsA, groupsB, test, bootstrapIterations, bootstrapSeed, alpha);
	}
	
	
	public void compareBCF_Parameters() {

		String filepath = "data\\dev_qsar\\output\\exp_prop_BCF_v1_modeling\\exp_prop_BCF_v1_modeling_Mapped_Records.json";

		Gson gson = new Gson();

		try {
			JsonArray ja = gson.fromJson(new FileReader(filepath), JsonArray.class);
			Hashtable<String, JsonArray> ht = createMappedRecordHashtable(ja, true);

//			ht=removeBasedOnStringParameter(ja, "Species supercategory", "Fish", true);
			ht=removeBasedOnStringParameter(ja, "Response site", "Whole body", true);
//			ht=removeBasedOnStringParameter(ja, "chem_analysis_method", "Measured", true);
//			ht=removeBasedOnStringParameter(ja, "Media type", "Fresh water", true);
//			
//			removeBasedOnStringParameter2(ja, "Species supercategory", "Fish", true);
//			removeBasedOnStringParameter2(ja, "Response site", "Whole body", true);
//			removeBasedOnStringParameter2(ja, "chem_analysis_method", "Measured", true);
//			removeBasedOnStringParameter2(ja, "Media type", "Fresh water", true);

//			removeBasedOnStringParameter3(ja, "Species supercategory", "Fish", true);
//			removeBasedOnStringParameter3(ja, "Response site", "Whole body", true);
//			removeBasedOnStringParameter3(ja, "chem_analysis_method", "Measured", true);
//			removeBasedOnStringParameter3(ja, "Media type", "Fresh water", true);

//			double factor=10;
//			double factor=1;
//			ht=removeBasedOnWaterConcentrationVsWaterSolubility(ja, true, factor);
//			ht=removeBasedOnDurationVsT80(ja, true, factor);
			
			double alpha=0.05;
//			pairedComparisonForFilter(ja, "Species supercategory", "Fish", true, "log-t", 10000, 12345L, alpha);
//			pairedComparisonForFilter(ja, "Response site", "Whole body", true, "log-t", 10000, 12345L, alpha);
//			pairedComparisonForFilter(ja, "chem_analysis_method", "Measured", true, "log-t", 10000, 12345L, alpha);
//			pairedComparisonForFilter(ja, "Media type", "Fresh water", true, "log-t", 10000, 12345L, alpha);
//			pairedComparisonForFilter(ja, "exposure_type", "Flow-through", true, "log-t", 10000, 12345L, alpha);
			
//			pairedComparisonForFilter(ja, "Overall Score", "1: Acceptable BCF", "3: Low BCF", true, "log-t", 10000, 12345L, alpha);
			
			//Or Wilcoxon signed-rank test on raw SD differences
//			pairedComparisonForFilter(ja, "Species supercategory", "Fish", true,
//			                    "wilcoxon", 10000, 12345L, alpha);

			
//			pairedComparisonForFilter(ja, "Species supercategory", "Fish", true, null, 10000, 12345L, alpha);
//			pairedComparisonForFilter(ja, "Response site", "Whole body", true, null, 10000, 12345L, alpha);
			

			// TODO duration vs T80- should it skip check if kinetic value?

		} catch (Exception e) {
			e.printStackTrace();
		}
//		
	}

	public static void main(String[] args) {
		DatasetCreatorScript dcs = new DatasetCreatorScript();

		ParameterFilteringComparison p = new ParameterFilteringComparison();
		p.compareBCF_Parameters();

	}
}

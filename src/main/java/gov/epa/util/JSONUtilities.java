package gov.epa.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//TODO move Json utilities to separate class in gov.epa.util

public class JsonUtilities {

	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
			.serializeSpecialFloatingPointValues().create();

	public static int batchAndWriteJSON(Vector<?> records, String baseFileName) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		Gson gson = builder.create();
		int batch = 0;

		if (records.size() <= 100000) {
			String jsonRecords = gson.toJson(records);
			writeJSONLineByLine(jsonRecords, baseFileName);
			batch = 1;
		} else {
			List<Object> temp = new ArrayList<Object>();
			Iterator<?> it = records.iterator();
			int i = 0;
			while (it.hasNext()) {
				temp.add(it.next());
				i++;
				if (i != 0 && i % 100000 == 0) {
					batch++;
					String batchFileName = baseFileName.substring(0, baseFileName.indexOf(".json")) + " " + batch
							+ ".json";
					String jsonRecords = gson.toJson(temp);
					writeJSONLineByLine(jsonRecords, batchFileName);
					temp.clear();
				}
			}
			batch++;
			String batchFileName = baseFileName.substring(0, baseFileName.indexOf(".json")) + " " + batch + ".json";
			String jsonRecords = gson.toJson(temp);
			writeJSONLineByLine(jsonRecords, batchFileName);
		}

		return batch;
	}

	public static int batchAndWriteJSON(List<?> records, String baseFileName) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting().disableHtmlEscaping().serializeSpecialFloatingPointValues();
		Gson gson = builder.create();
		int batch = 0;

		if (records.size() <= 100000) {
			String jsonRecords = gson.toJson(records);
			writeJSONLineByLine(jsonRecords, baseFileName);
			batch = 1;
		} else {
			List<Object> temp = new ArrayList<Object>();
			Iterator<?> it = records.iterator();
			int i = 0;
			while (it.hasNext()) {
				temp.add(it.next());
				i++;
				if (i != 0 && i % 100000 == 0) {
					batch++;
					String batchFileName = baseFileName.substring(0, baseFileName.indexOf(".json")) + " " + batch
							+ ".json";
					String jsonRecords = gson.toJson(temp);
					writeJSONLineByLine(jsonRecords, batchFileName);
					temp.clear();
				}
			}
			batch++;
			String batchFileName = baseFileName.substring(0, baseFileName.indexOf(".json")) + " " + batch + ".json";
			String jsonRecords = gson.toJson(temp);
			writeJSONLineByLine(jsonRecords, batchFileName);
		}

		return batch;
	}

	private static void replaceAll(StringBuilder sb, String find, String replace) {

		// compile pattern from find string
		Pattern p = Pattern.compile(find);

		// create new Matcher from StringBuilder object
		Matcher matcher = p.matcher(sb);

		// index of StringBuilder from where search should begin
		int startIndex = 0;

		while (matcher.find(startIndex)) {

			sb.replace(matcher.start(), matcher.end(), replace);

			// set next start index as start of the last match + length of replacement
			startIndex = matcher.start() + replace.length();
		}
	}

	public static String fixChars(String str) {
		StringBuilder sb = new StringBuilder(str);
		try {
			replaceAll(sb, "ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ", "-");
			replaceAll(sb, "ÃÂ¢Ã¢ÂÂ¬Ã¢ÂÂ¢", "'");
			replaceAll(sb, "\uff08", "(");// ÃÂ¯ÃÂ¼ÃÂ
			replaceAll(sb, "\uff09", ")");// ÃÂ¯ÃÂ¼Ã¢ÂÂ°
			replaceAll(sb, "\uff0f", "/");// ÃÂ¯ÃÂ¼Ã¯Â¿Â½
			replaceAll(sb, "\u3000", " ");// blank
			replaceAll(sb, "\u00a0", " ");// blank
			replaceAll(sb, "\u2003", " ");// blank
			replaceAll(sb, "\u0009", " ");// blank
			replaceAll(sb, "\u300c", "");// ÃÂ£Ã¢ÂÂ¬ÃÂ
			replaceAll(sb, "\u300d", "");// ÃÂ£Ã¢ÂÂ¬Ã¯Â¿Â½
			replaceAll(sb, "\u2070", "^0");// superscript 0
			replaceAll(sb, "\u00B9", "^1");// superscript 1
			replaceAll(sb, "\u00B2", "^2");// superscript 2
			replaceAll(sb, "\u00B3", "^3");// superscript 3
			replaceAll(sb, "\u2074", "^4");// superscript 4
			replaceAll(sb, "\u2075", "^5");// superscript 5
			replaceAll(sb, "\u2076", "^6");// superscript 6
			replaceAll(sb, "\u2077", "^7");// superscript 7
			replaceAll(sb, "\u2078", "^8");// superscript 8
			replaceAll(sb, "\u2079", "^9");// superscript 9
			replaceAll(sb, "\u2080", "_0");// subscript 0
			replaceAll(sb, "\u2081", "_1");// subscript 1
			replaceAll(sb, "\u2082", "_2");// subscript 2
			replaceAll(sb, "\u2083", "_3");// subscript 3
			replaceAll(sb, "\u2084", "_4");// subscript 4
			replaceAll(sb, "\u2085", "_5");// subscript 5
			replaceAll(sb, "\u2086", "_6");// subscript 6
			replaceAll(sb, "\u2087", "_7");// subscript 7
			replaceAll(sb, "\u2088", "_8");// subscript 8
			replaceAll(sb, "\u2089", "_9");// subscript 9
		} catch (Exception ex) {
			System.out.println(sb.toString());
		}
		return sb.toString();
	}

	private static void writeJSONLineByLine(String jsonRecords, String filePath) {
		String[] strRecords = jsonRecords.split("\n");

		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		try {
			// Clear existing file contents
			FileWriter fw = new FileWriter(filePath);
			fw.close();

			BufferedWriter bwAppend = new BufferedWriter(new FileWriter(filePath, true));

			for (String s : strRecords) {
				s = fixChars(s);
				bwAppend.write(s + "\n");
			}
			bwAppend.flush();
			bwAppend.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Read a csv into a JsonArray
	 * 
	 * @param filepath
	 * @return
	 */
	public static JsonArray csvToGson(String filepath) {
		JsonArray ja = new JsonArray();
		try {
			List<String> lines = Files.readAllLines(Path.of(filepath));

			String header = lines.get(0);
			String[] hvals = header.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

			for (int i = 1; i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.trim().length() == 0)
					break;
				String[] vals = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				JsonObject jo = new JsonObject();
				for (int j = 0; j < hvals.length; j++) {
					jo.addProperty(hvals[j], vals[j].replace("\"", ""));
				}
				ja.add(jo);
			}
//			System.out.println(gson.toJson(ja));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ja;
	}

	public static String jsonToPrettyJson(String json) {
		JsonObject jo = gson.fromJson(json, JsonObject.class);
		return gson.toJson(jo);
	}

	public static String jsonToPrettyJson(String json, String filepath) {
		JsonObject jo = gson.fromJson(json, JsonObject.class);
		return saveJson(jo, filepath);
	}

	public static String saveJson(Object obj, String filepath) {
		try {

			FileWriter fw = new FileWriter(filepath);

			String json = gson.toJson(obj);
			fw.write(json);
			fw.flush();
			fw.close();

			return json;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String saveJson(String json, String filepath) {
		try {

			FileWriter fw = new FileWriter(filepath);
			fw.write(json);
			fw.flush();
			fw.close();

			return json;

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static String toJsonFile(Object object, String filepath) {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeSpecialFloatingPointValues();// allow NaN values for chemicals that have no prediction
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		String json = gson.toJson(object);

		try (FileWriter fw = new FileWriter(filepath)) {
			fw.write(json);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return json;
	}

	/**
	 * Loads JsonArray from filepath
	 * 
	 * @param filepath
	 * @return
	 */
	public static JsonArray getJsonArrayFromJsonFile(String filepath) {
		try {
			Gson gson = new Gson();
			Reader reader = Files.newBufferedReader(Paths.get(filepath));
			JsonArray ja = gson.fromJson(reader, JsonArray.class);
			return ja;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		}
	}
	
	
	public static JsonObject getJsonObjectFromJsonFile(String filepath) {
		try {
			Gson gson = new Gson();
			Reader reader = Files.newBufferedReader(Paths.get(filepath));
			JsonObject jo = gson.fromJson(reader, JsonObject.class);
			return jo;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;

		}
	}

}

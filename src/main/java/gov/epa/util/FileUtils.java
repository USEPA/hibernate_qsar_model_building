package gov.epa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	/**
	 * Helper method to returns file path and name with extension omitted
	 * @param name File name 
	 * @return File name without extension
	 */
	public static String getFileNameWithoutExtension(String name) {
		int pos = name.lastIndexOf('.');
		if (pos > 0 && pos < (name.length() - 1)) {
			// there is a '.' and it's not the first, or last character.
			return name.substring(0, pos);
		}
		return name;
	}

	/**
	 * Helper method to replace file extension with another one 
	 * @param fileName - name of the file
	 * @param newExt - new extension including ".", e.g. ".pdf"
	 * @return File name with new extension 
	 */
	public static String replaceExtension(String fileName, String newExt) {
		String fileNameNoExt = getFileNameWithoutExtension(fileName);
		return fileNameNoExt + newExt;
	}
	
	public static void appendToFile(String filePath, String textToAppend) throws IOException
	{
	    Path path = Paths.get(filePath);
	    Files.write(path, textToAppend.getBytes(), StandardOpenOption.APPEND);
	}
	
	
	
	public static int copyFile(File SrcFile, File DestFile) {

		try {

//			System.out.println(SrcFile.getAbsolutePath());
//			System.out.println(DestFile.getAbsolutePath());

			DestFile.getParentFile().mkdirs();
			FileChannel in = new FileInputStream(SrcFile).getChannel();

			FileChannel out = new FileOutputStream(DestFile).getChannel();

			in.transferTo(0, (int) in.size(), out);
			in.close();
			out.close();

			return 0;

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

	}
	
	static void goThroughFolder(File folder,List<File>allFiles) {

		
		for (File file: folder.listFiles()) {
			if(file.isDirectory()) {
				
//				System.out.println(file.getAbsolutePath());
				
				goThroughFolder(file,allFiles);
			} else {

				if(file.length()>10e6)				
					allFiles.add(file);

				
//				try {
//					if(file.length()>10e6 && !isOneDriveFileOnlineOnly(file.getAbsolutePath()))	{			
//						System.out.println(file.getAbsolutePath()+"\t"+file.length());
//						allFiles.add(file);
//					} else if(file.length()>10e6)	{
//						System.out.println(file.getAbsolutePath()+"\tIn cloud");
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
			
		}
		
		

		
	}
	
	
	 public static boolean isOneDriveFileOnlineOnly(String filePath) throws IOException {
         Path path = Paths.get(filePath);
         if (Files.exists(path) && Files.isRegularFile(path)) {
             DosFileAttributes attrs = Files.readAttributes(path, DosFileAttributes.class);
             // The exact attribute for "offline" might vary or be represented differently
             // in the DosFileAttributes. This is a conceptual check.
             // In practice, you might need to use JNA to call Windows API functions
             // to get the specific OneDrive attributes.
             return attrs.isArchive() && attrs.isSystem(); // Placeholder, not accurate for OneDrive offline
         }
         return false; // Not a regular file or doesn't exist
     }
	
	
	
	static void findLargeFiles() {
		String strFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\";
		
		File folder=new File(strFolder);
		
		List<File>allFiles=new ArrayList<>();
		
		goThroughFolder(folder,allFiles);
		
		for(File file:allFiles) {
			System.out.println(file.getAbsolutePath()+"\t"+file.length());
		}
		
	}
	
	public static void main(String[] args) {
		String filepath="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\PFAS tiger team\\1-s2.0-S0043135407005337-main.pdf";
		
		try {
			System.out.println(isOneDriveFileOnlineOnly(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		findLargeFiles();
	}
}

package gov.epa.util;

/**
* @author TMARTI02
*/


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtilities {
	
  public static void main(String[] args) throws Exception {
    
		String input = "Hello world! this is a test from book2s.com HELLO HELLO HELLO HELLO "
				+ "HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO HELLO";
	    byte[] uncompressedData = input.getBytes("UTF-8");

    // Compress the data
    byte[] compressedData = compress(uncompressedData,
        Deflater.BEST_COMPRESSION, false);

    // Decompress the data
    byte[] decompressedData = decompress(compressedData, false);

    String output = new String(decompressedData, "UTF-8");

    // Display the statistics
    System.out.println("Input String: " + input);
    System.out.println("Uncompressed data length: " + uncompressedData.length);
    System.out.println("Compressed data length: " + compressedData.length);
    System.out.println("Decompressed data length: " + decompressedData.length);
    System.out.println("Output String: " + output);
  
  
  }//from   www . j ava 2  s.  com

  public static byte[] compress(byte[] input, int compressionLevel,
      boolean GZIPFormat) throws IOException {

    // Create a Deflater object to compress data
    Deflater compressor = new Deflater(compressionLevel, GZIPFormat);

    // Set the input for the compressor
    compressor.setInput(input);

    // Call the finish() method to indicate that we have
    // no more input for the compressor object
    compressor.finish();

    // Compress the data
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    byte[] readBuffer = new byte[1024];
    int readCount = 0;

    while (!compressor.finished()) {
      readCount = compressor.deflate(readBuffer);
      if (readCount > 0) {
        // Write compressed data to the output stream
        bao.write(readBuffer, 0, readCount);
      }
    }

    // End the compressor
    compressor.end();

    // Return the written bytes from output stream
    return bao.toByteArray();
  }

  public static byte[] decompress(byte[] input, boolean GZIPFormat)
      throws IOException, DataFormatException {
    // Create an Inflater object to compress the data
    Inflater decompressor = new Inflater(GZIPFormat);

    // Set the input for the decompressor
    decompressor.setInput(input);

    // Decompress data
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    byte[] readBuffer = new byte[1024];
    int readCount = 0;

    while (!decompressor.finished()) {
      readCount = decompressor.inflate(readBuffer);
      if (readCount > 0) {
        // Write the data to the output stream
        bao.write(readBuffer, 0, readCount);
      }
    }

    // End the decompressor
    decompressor.end();

    // Return the written bytes from the output stream
    return bao.toByteArray();
  }
}
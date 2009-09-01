// Copyright 2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv.tests;

import com.pensioenpage.jynx.ods2csv.Converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for the <code>Converter</code> class.
 *
 * @version $Revision: 7832 $ $Date: 2009-01-22 13:56:48 +0100 (do, 22 jan 2009) $
 * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
 */
public class ConverterTests {

   //-------------------------------------------------------------------------
   // Methods
   //-------------------------------------------------------------------------

   @Test
   public void testConverter() throws Exception {

      // Make sure constructor does not accept null arguments
      Converter converter = new Converter();
      try {
         converter.convert(null, null);
         fail("Expected IllegalArgumentException.");
      } catch (IllegalArgumentException e) {
         // as expected
      }
      try {
         converter.convert(new ByteArrayInputStream("Test".getBytes()), null);
         fail("Expected IllegalArgumentException.");
      } catch (IllegalArgumentException e) {
         // as expected
      }
      try {
         converter.convert(null, new ByteArrayOutputStream());
         fail("Expected IllegalArgumentException.");
      } catch (IllegalArgumentException e) {
         // as expected
      }

      // Test with actual input files, compare with expected output
      boolean finished = false;
      int i = 1;
      do {
         byte[] ods = loadTestData(i, "ods");
         byte[] csv = loadTestData(i, "csv");

         if (ods == null || csv == null) {
            finished = true;
         } else {
            doTest(i, ods, csv);
         }

         i++;
      } while (! finished);

      System.err.println("Ran " + (i - 1) + " test case(s).");
   }

   private byte[] loadTestData(int index, String suffix) throws Exception {
      InputStream byteStream = getClass().getResourceAsStream("test" + index + '.' + suffix);
      return (byteStream == null) ? null : IOUtils.toByteArray(byteStream);
   }

   private void doTest(int testIndex, byte[] ods, byte[] expectedCSV) throws Exception {

      // Prepare
      Converter        converter = new Converter();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      // Convert
      converter.convert(new ByteArrayInputStream(ods), baos);

      // Convert to a byte array
      byte[] actualCSV = baos.toByteArray();

      // Loop over all bytes
      int count = Math.min(expectedCSV.length, actualCSV.length);
      String errorPrefix = "Test " + testIndex + ": Generated CSV is different from what was expected: ";
      for (int i = 0; i < count; i++) {
         byte expectedByte = expectedCSV[i];
         byte   actualByte =   actualCSV[i];
         assertTrue(errorPrefix + "At byte " + i + ": expected '" + expectedByte + "' (" + ((int) expectedByte) + ") instead of actual '" + actualByte + "' (" + ((int) actualByte) + ").", actualByte == expectedByte);
      }

      assertTrue(errorPrefix + "Expected " + expectedCSV.length + " byte(s) in output, but actual output contains " + actualCSV.length + " byte(s).", expectedCSV.length == actualCSV.length);
   }
}

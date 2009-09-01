// Copyright 2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv.tests;

import com.pensioenpage.jynx.ods2csv.Converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
      int i = 0;
      do {
         InputStream ods = getClass().getResourceAsStream("test" + i + ".ods");
         InputStream csv = getClass().getResourceAsStream("test" + i + ".csv");

         if (ods == null || csv == null) {
            finished = true;
         } else {
            doTest(ods, csv);
         }
      } while (! finished);
   }

   private void doTest(InputStream ods, InputStream csv) throws Exception {
      Converter converter = new Converter();
      ByteArrayOutputStream actual = new ByteArrayOutputStream();
      converter.convert(ods, actual);
   }
}

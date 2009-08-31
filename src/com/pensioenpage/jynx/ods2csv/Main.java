// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Command-line program for converting a single ODS document to CSV text.
 * Input is expected to come from <em>stdin</em>, output goes to
 * <em>stdout</em> and errors to <em>stderr</em>.
 *
 * <p>This program returns 0 on succes. Any other exit code indicates failure.
 *
 * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
 */
public final class Main extends Object {

   //-------------------------------------------------------------------------
   // Class functions
   //-------------------------------------------------------------------------

   /**
    * Converts from <em>stdin</em> to <em>stdout</em>.
    *
    * @param args
    *    the arguments for the program, can be <code>null</code>.
    */
   public static void main(String[] args) {

      // Convert
      try {
         new Converter().convert(System.in, System.out);
         System.exit(0);

      // All exceptions are caught
      } catch (Throwable e) {
         e.printStackTrace();
         System.exit(1);
      }
   }


   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>Main</code> instance.
    */
   private Main() {
      // empty
   }
}

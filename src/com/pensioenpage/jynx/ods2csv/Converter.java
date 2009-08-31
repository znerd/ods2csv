// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Core functionality for converting an ODS-document to CSV-text.
 *
 * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
 */
public final class Converter extends Object {

   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>Converter</code> instance.
    */
   public Converter() {
      // empty
   }


   //-------------------------------------------------------------------------
   // Methods
   //-------------------------------------------------------------------------

   /**
    * Converts using the specified input and output streams. No buffering will
    * be done by this method, so it may be wise to wrap the input stram inside
    * a {@link java.io.BufferedInputStream} before passing it to this method.
    *
    * @param in
    *    the {@link InputStream}, to read the input from, should be an
    *    ODS-document, cannot be <code>null</code>.
    *
    * @param out
    *    the {@link OutputStream}, to write the output to, will be CSV text,
    *    cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException
    *    if <code>in == null || out == null</code>.
    *
    * @throws IOException
    *    in case of an I/O error.
    *
    * @throws ConversionException
    *    in case of a conversion failure.
    */
   public void convert(InputStream in, OutputStream out)
   throws IllegalArgumentException, IOException, ConversionException {

      // Check preconditions
      if (in == null) {
         throw new IllegalArgumentException("in == null");
      } else if (out == null) {
         throw new IllegalArgumentException("out == null");
      }

      // Find the "content.xml" file in the ZIP file
      ZipInputStream zin = new ZipInputStream(in);
      ZipEntry entry = null, contentEntry = null;
      do {
         entry = zin.getNextEntry();
         if (entry != null && "content.xml".equals(entry.getName())) {
            contentEntry = entry;
         }
      } while (entry != null && contentEntry == null);

      // No content.xml file found, fail
      if (contentEntry == null) {
         throw new ConversionException("Unable to find \"content.xml\" entry in ZIP stream.");
      }
   }
}

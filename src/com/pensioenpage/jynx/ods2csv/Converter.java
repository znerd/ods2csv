// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

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
         if (entry != null && !entry.isDirectory() && "content.xml".equals(entry.getName())) {
            contentEntry = entry;
         }
      } while (entry != null && contentEntry == null);

      // No content.xml file found, fail
      if (contentEntry == null) {
         throw new ConversionException("Unable to find \"content.xml\" file entry in ZIP stream.");
      }

      // Process the unzipped content.xml while it's unzipped (using SAX)
      new XMLParser(out).parse(zin);
   }

   /**
    * SAX handler for producing the CSV output.
    *
    * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
    */
   private static class XMLParser extends DefaultHandler {

      //----------------------------------------------------------------------
      // Constructors
      //----------------------------------------------------------------------

      /**
       * Constructs a new <code>XMLParser</code> that sends the CSV text
       * output to the specified <code>OutputStream</code>.
       *
       * @param out
       *    the {@link OutputStream} to send the CSV text output to,
       *    cannot be <code>null</code>.
       *
       * @throws IllegalArgumentException
       *    if <code>out == null</code>.
       */
      public XMLParser(OutputStream out) throws IllegalArgumentException {

         // Check preconditions
         if (out == null) {
            throw new IllegalArgumentException("out == null");
         }

         // Initialize instance fields
         _out = out;
      }
      

      //----------------------------------------------------------------------
      // Fields
      //----------------------------------------------------------------------

      /**
       * The output stream. This is where the CSV output goes.
       * Never <code>null</code>.
       */
      private final OutputStream _out;

      /**
       * The exception, in case of an error (fatal or not).
       */
      private Throwable _exception;


      //----------------------------------------------------------------------
      // Methods
      //----------------------------------------------------------------------

      public void parse(InputStream in)
      throws IllegalArgumentException, ConversionException {

         // Check preconditions
         if (in == null) {
            throw new IllegalArgumentException("in == null");
         }

         // Parse the input stream using SAX
         Throwable cause;
         try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(this);
            xmlReader.parse(new InputSource(in));
            cause = null;

         // Catch any exceptions thrown directly
         } catch (Throwable e) {
            cause = e;
         }

         // Also consider exceptions thrown deeper down
         cause = (cause == null) ? _exception : cause;

         // Wrap and rethrow if there was any exception
         if (cause != null) {
            throw new ConversionException("Failed to process \"content.xml\" entry.", cause);
         }
      }

      @Override
      public void warning(SAXParseException exception)  {
         // empty
      }

      @Override
      public void error(SAXParseException exception) {
         _exception = exception;
      }

      @Override
      public void fatalError(SAXParseException exception) {
         _exception = exception;
      }

      // NOTE: The expected XML structure is:
      //
      // <office:document-content office:version="1.2">
      //    <office:body>
      //       <office:spreadsheet>
      //          <table:table>
      //             <table:table-row>
      //                <table:table-cell>
      //                   <text:p>Text content in here</text:p>

      @Override
      public void startElement(String uri, String localName, String qName, Attributes atts)
      throws SAXException {
         // TODO
      }

      @Override
      public void characters(char[] ch, int start, int length)
      throws SAXException {
         // TODO
      }
   }
}

// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
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


   //-------------------------------------------------------------------------
   // Inner classes
   //-------------------------------------------------------------------------

   /**
    * SAX handler for producing the CSV output.
    *
    * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
    */
   private static class XMLParser extends DefaultHandler {

      //----------------------------------------------------------------------
      // Class fields
      //----------------------------------------------------------------------

      /**
       * The URI for the <em>office:</em> XML namespace used in OpenDocument
       * documents.
       */
      private static final String OFFICE_NS = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";

      /**
       * The URI for the <em>text:</em> XML namespace used in OpenDocument
       * documents.
       */
      private static final String TEXT_NS = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";

      /**
       * The URI for the <em>table:</em> XML namespace used in OpenDocument
       * documents.
       */
      private static final String TABLE_NS = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";


      //----------------------------------------------------------------------
      // Constructors
      //----------------------------------------------------------------------

      /**
       * Constructs a new <code>XMLParser</code> that sends the CSV text
       * output to the specified byte-based <code>OutputStream</code>. The
       * output will be encoded as UTF-8.
       *
       * @param out
       *    the {@link OutputStream} to send the CSV text output to,
       *    cannot be <code>null</code>.
       *
       * @throws IllegalArgumentException
       *    if <code>out == null</code>.
       */
      XMLParser(OutputStream out) throws IllegalArgumentException {

         // Check preconditions
         if (out == null) {
            throw new IllegalArgumentException("out == null");
         }

         // Initialize instance fields
         _out = new OutputStreamWriter(out, Charset.forName("UTF-8"));
      }
      

      //----------------------------------------------------------------------
      // Fields
      //----------------------------------------------------------------------

      /**
       * The character-based output stream. This is where the CSV output goes.
       * Never <code>null</code>.
       */
      private final Writer _out;

      /**
       * The exception, in case of an error (fatal or not).
       */
      private Throwable _exception;

      private boolean _insideRow;
      private boolean _insideCell;
      private boolean _insideCellText;
      private boolean _hadCells;
      private boolean _stringValueType;


      //----------------------------------------------------------------------
      // Methods
      //----------------------------------------------------------------------

      void parse(InputStream in)
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

         // Flush and close the output stream
         try {
            _out.flush();
         } catch (Throwable e) {
            // ignore
         } finally {
            try {
               _out.close();
            } catch (Throwable e) {
               // ignore
            }
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

         // Start of table cell
         if (TABLE_NS.equals(uri) && "table-row".equals(localName)) {
            _insideRow = true;
            _hadCells  = false;

         // Start of table cell
         } else if (TABLE_NS.equals(uri) && "table-cell".equals(localName) && _insideRow) {
            if (_hadCells) {
               output(',');
            }
            String valueType = atts.getValue(OFFICE_NS, "value-type");
            if ("float".equals(valueType)) {
               output(atts.getValue(OFFICE_NS, "value"));
               _stringValueType = false;
            } else if ("date".equals(valueType)) {
               output(atts.getValue(OFFICE_NS, "date-value"));
               _stringValueType = false;
            } else {
               _stringValueType = true;
            }
            _insideCell = true;

         // Start of cell text inside table cell
         } else if (TEXT_NS.equals(uri) && "p".equals(localName) && _insideCell) {
            _insideCellText = true;
         }
      }

      @Override
      public void endElement(String uri, String localName, String qName)
      throws SAXException {

         // End of table row: append a newline in the output
         if (TABLE_NS.equals(uri) && "table-row".equals(localName) && _insideRow) {
            // TODO: Only if we had any row data
            output('\n');
            _insideRow = false;

         // End of table cell
         } else if (TABLE_NS.equals(uri) && "table-cell".equals(localName) && _insideCell) {
            _insideCell = false;
            _hadCells   = true;

         // Closing text element inside table cell
         } else if (TEXT_NS.equals(uri) && "p".equals(localName) && _insideCellText) {
            _insideCellText = false;
         }
      }

      @Override
      public void characters(char[] ch, int start, int length)
      throws SAXException {

         // Short-circuit 
         if (! (_insideCellText && _stringValueType)) {
            return;
         }

         // TODO: Review the performance of the for-loop below
         output('"');

         final int end = start + length;
         for (int i = start; i < end; i++) {
            char c = ch[i];
            switch (c) {
               case '"':
                  output('"');
                  output('"');
                  break;
               default:
                  output(c);
            }
         }

         output('"');
      }

      private void output(char c) throws SAXException {
         try {
            _out.write(c);
         } catch (IOException cause) {
            throw new SAXException("Failed to write character due to an I/O error.", cause);
         }
      }

      private void output(String s) throws SAXException {
         try {
            _out.write(s, 0, s.length());
         } catch (IOException cause) {
            throw new SAXException("Failed to write character due to an I/O error.", cause);
         }
      }
   }
}

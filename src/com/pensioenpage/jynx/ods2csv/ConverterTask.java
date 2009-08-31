// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import static org.apache.tools.ant.Project.MSG_ERR;
import static org.apache.tools.ant.Project.MSG_VERBOSE;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * An Apache Ant task for converting a number of ODS files to CSV files.
 *
 * <p>The most notable parameters supported by this task are:
 *
 * <dl>
 * <dt>dir
 * <dd>The source directory to read from.
 *     Optional, defaults to the project base directory.
 *
 * <dt>toDir
 * <dd>The target directory to write to.
 *     Optional, defaults to the source directory.
 *
 * <dt>includes
 * <dd>The files to match in the source directory.
 *     Optional, defaults to <code>*.less</code>.
 *
 * <dt>excludes
 * <dd>The files to exclude, even if they are matched by the include filter.
 *     Optional, default is empty.
 * </dl>
 *
 * <p>This task supports more parameters and contained elements, inherited
 * from {@link MatchingTask}. For more information, see
 * <a href="http://ant.apache.org/manual/dirtasks.html">the Ant site</a>.
 *
 * @version $Revision: 10190 $ $Date: 2009-08-25 17:49:35 +0200 (di, 25 aug 2009) $
 * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
 */
public final class ConverterTask extends MatchingTask {

   //-------------------------------------------------------------------------
   // Class functions
   //-------------------------------------------------------------------------

   /**
    * Returns a quoted version of the specified string,
    * or <code>"(null)"</code> if the argument is <code>null</code>.
    *
    * @param s
    *    the character string, can be <code>null</code>,
    *    e.g. <code>"foo bar"</code>.
    *
    * @return
    *    the quoted string, e.g. <code>"\"foo bar\""</code>,
    *    or <code>"(null)"</code> if the argument is <code>null</code>.
    */
   private static final String quote(String s) {
      return s == null ? "(null)" : "\"" + s + '"';
   }

   /**
    * Checks if the specified string is either null or empty (after trimming
    * the whitespace off).
    *
    * @param s
    *    the string to check.
    *
    * @return
    *    <code>true</code> if <code>s == null || s.trim().length() &lt; 1</code>;
    *    <code>false</code> otherwise.
    */
   private static final boolean isEmpty(String s) {
      return s == null || s.trim().length() < 1;
   }

   /**
    * Checks if the specified abstract path name refers to an existing
    * directory.
    *
    * @param description
    *    the description of the directory, cannot be <code>null</code>.
    *
    * @param path
    *    the abstract path name as a {@link File} object.
    *
    * @param mustBeReadable
    *    <code>true</code> if the directory must be readable.
    *
    * @param mustBeWritable
    *    <code>true</code> if the directory must be writable.
    *
    * @throws IllegalArgumentException
    *    if <code>location == null
    *          || {@linkplain TextUtils}.{@linkplain TextUtils#isEmpty(String) isEmpty}(description)</code>.
    *
    * @throws BuildException
    *    if <code>  path == null
    *          || ! path.exists()
    *          || ! path.isDirectory()
    *          || (mustBeReadable &amp;&amp; !path.canRead())
    *          || (mustBeWritable &amp;&amp; !path.canWrite())</code>.
    */
   private static final void checkDir(String  description,
                                      File    path,
                                      boolean mustBeReadable,
                                      boolean mustBeWritable)
   throws IllegalArgumentException, BuildException {

      // Check preconditions
      if (isEmpty(description)) {
         throw new IllegalArgumentException("description is empty (" + quote(description) + ')');
      }

      // Make sure the path refers to an existing directory
      if (path == null) {
         throw new BuildException(description + " is not set.");
      } else if (! path.exists()) {
         throw new BuildException(description + " (\"" + path + "\") does not exist.");
      } else if (! path.isDirectory()) {
         throw new BuildException(description + " (\"" + path + "\") is not a directory.");

      // Make sure the directory is readable, if that is required
      } else if (mustBeReadable && (! path.canRead())) {
         throw new BuildException(description + " (\"" + path + "\") is not readable.");

      // Make sure the directory is writable, if that is required
      } else if (mustBeWritable && (! path.canWrite())) {
         throw new BuildException(description + " (\"" + path + "\") is not writable.");
      }
   }


   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>ConverterTask</code> object.
    */
   public ConverterTask() {
      setIncludes("*.ods");
   }


   //-------------------------------------------------------------------------
   // Fields
   //-------------------------------------------------------------------------

   /**
    * The directory to read <code>.ods</code> files from.
    * See {@link #setDir(File)}.
    */
   private File _sourceDir;

   /**
    * The directory to write <code>.csv</code> files to.
    * See {@link #setToDir(File)}.
    */
   private File _destDir;


   //-------------------------------------------------------------------------
   // Methods
   //-------------------------------------------------------------------------

   /**
    * Sets the path to the source directory. This parameter is required.
    *
    * @param dir
    *    the location of the source directory, or <code>null</code>.
    */
   public void setDir(File dir) {
      _sourceDir = dir;
   }

   /**
    * Sets the path to the destination directory. The default is the same
    * directory.
    *
    * @param dir
    *    the location of the destination directory, or <code>null</code>.
    */
   public void setToDir(File dir) {
      _destDir = dir;
   }

   @Override
   public void execute() throws BuildException {

      // Source directory defaults to current directory
      if (_sourceDir == null) {
         _sourceDir = getProject().getBaseDir();
      }

      // Destination directory defaults to source directory
      if (_destDir == null) {
         _destDir = _sourceDir;
      }

      // Check the directories
      checkDir("Source directory",      _sourceDir,  true, false);
      checkDir("Destination directory",   _destDir, false,  true);

      // Preparations done, consider each individual file for processing
      log("Converting from " + _sourceDir.getPath() + " to " + _destDir.getPath() + '.', MSG_VERBOSE);
      long start = System.currentTimeMillis();
      int failedCount = 0, successCount = 0, skippedCount = 0;
      Converter converter = new Converter();
      for (String inFileName : getDirectoryScanner(_sourceDir).getIncludedFiles()) {

         // Make sure the input file exists
         File inFile = new File(_sourceDir, inFileName);
         if (! inFile.exists()) {
            continue;
         }

         // Some preparations related to the input file and output file
         long     thisStart = System.currentTimeMillis();
         String outFileName = inFile.getName().replaceFirst("\\.ods$", ".csv");
         File       outFile = new File(_destDir, outFileName);
         String outFilePath = outFile.getPath();
         String  inFilePath = inFile.getPath();

         // Skip this file is the output file exists and is newer
         if (outFile.exists() && (outFile.lastModified() > inFile.lastModified())) {
            log("Skipping " + quote(inFileName) + " because output file is newer.", MSG_VERBOSE); 
            skippedCount++;
            continue;
         }

         // Convert
         Throwable exception;
         try {
            converter.convert(new FileInputStream(inFile), new FileOutputStream(outFile));
            exception = null;
         } catch (Throwable e) {
            exception = e;
         }

         // Log the result for this individual file
         long thisDuration = System.currentTimeMillis() - thisStart;
         if (exception != null) {
            String logMessage = "Failed to convert " + quote(inFilePath);
            String exceptionMessage = exception.getMessage();
            if (isEmpty(exceptionMessage)) {
               logMessage += '.';
            } else {
               logMessage += ": " + exceptionMessage;
            }
            log(logMessage, MSG_ERR);
            failedCount++;
         } else {
            log("Converted " + quote(inFileName) + " in " + thisDuration + " ms.", MSG_VERBOSE);
            successCount++;
         }
      }

      // Log the total result
      long duration = System.currentTimeMillis() - start;
      if (failedCount > 0) {
         throw new BuildException("" + failedCount + " file(s) failed to convert, while " + successCount + " succeeded. Total duration is " + duration + " ms.");
      } else {
         log("" + successCount + " file(s) converted in " + duration + " ms; " + skippedCount + " unmodified file(s) skipped.");
      }
   }
}

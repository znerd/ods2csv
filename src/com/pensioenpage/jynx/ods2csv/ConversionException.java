// Copyright 2007-2009, PensioenPage B.V.
package com.pensioenpage.jynx.ods2csv;

/**
 * Exception that indicates an ODS-to-CSV conversion failed.
 *
 * @author <a href="mailto:ernst@pensioenpage.com">Ernst de Haan</a>
 */
public final class ConversionException extends Exception {

   //-------------------------------------------------------------------------
   // Class variables
   //-------------------------------------------------------------------------

   /**
    * Unique identifier used for serialization.
    */
   private static final long serialVersionUID = 1012348765336132236L;


   //-------------------------------------------------------------------------
   // Constructors
   //-------------------------------------------------------------------------

   /**
    * Constructs a new <code>ConversionException</code> instance.
    */
   public ConversionException() {
      // empty
   }

   /**
    * Constructs a new <code>ConversionException</code> instance with the
    * specified detail message.
    *
    * @param detail
    *    the detail message.
    */
   public ConversionException(String detail) {
      super(detail);
   }

   /**
    * Constructs a new <code>ConversionException</code> instance with the
    * specified detail message and cause exception.
    *
    * @param detail
    *    the detail message.
    *
    * @param cause
    *    the cause exception.
    */
   public ConversionException(String detail, Throwable cause) {
      super(detail);
      if (cause != null) {
         initCause(cause);
      }
   }
}

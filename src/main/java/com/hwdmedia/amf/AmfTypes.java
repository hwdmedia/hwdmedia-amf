/**
 * Copyright © 2013 Gennadiy Kozlenko
 * 
 * This file is part of hwdmedia-amf - AMF reader/writer library.
 */
package com.hwdmedia.amf;

/**
 * AMF Types
 * 
 * @author Gennadiy Kozlenko
 */
public class AmfTypes {
    
    // AMF0 markers
    public static final byte AMF0_NUMBER            = 0x00;
    public static final byte AMF0_BOOLEAN           = 0x01;
    public static final byte AMF0_STRING            = 0x02;
    public static final byte AMF0_OBJECT            = 0x03;
    public static final byte AMF0_MOVIECLIP         = 0x04; // reserved, not supported
    public static final byte AMF0_NULL              = 0x05;
    public static final byte AMF0_UNDEFINED         = 0x06;
    public static final byte AMF0_REFERENCE         = 0x07;
    public static final byte AMF0_ECMA_ARRAY        = 0x08;
    public static final byte AMF0_OBJECT_END        = 0x09;
    public static final byte AMF0_STRICT_ARRAY      = 0x0a;
    public static final byte AMF0_DATE              = 0x0b;
    public static final byte AMF0_LONG_STRING       = 0x0c;
    public static final byte AMF0_UNSUPPORTED       = 0x0d;
    public static final byte AMF0_RECORDSET         = 0x0e; // reserved, not supported
    public static final byte AMF0_XML_DOCUMENT      = 0x0f;
    public static final byte AMF0_TYPED_OBJECT      = 0x10;
    public static final byte AMF0_AVMPLUS_OBJECT    = 0x11;
    
}

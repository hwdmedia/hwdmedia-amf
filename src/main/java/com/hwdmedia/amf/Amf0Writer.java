/**
 * Copyright © 2013 HWD Media
 * 
 * This file is part of hwdmedia-amf - AMF reader/writer library.
 */
package com.hwdmedia.amf;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * AMF0 Writer
 * 
 * @author Gennadiy Kozlenko
 */
public final class Amf0Writer implements Closeable {
    
    private ByteArrayOutputStream bout;
    
    private DataOutputStream out;
    
    /**
     * Class constructor
     */
    public Amf0Writer() {
        bout = new ByteArrayOutputStream();
        out = new DataOutputStream(bout);
    }
    
    /**
     * Write AFM data
     * @param value 
     */
    public void write(Object value) throws IOException, AmfException {
        if (null != value) {
            if (value instanceof Number) {
                writeDouble(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                writeBoolean(((Boolean) value).booleanValue());
            } else if (value instanceof String) {
                writeString((String) value);
            } else if (value instanceof Date) {
                writeDate((Date) value);
            } else if (value instanceof Calendar) {
                writeDate(((Calendar) value).getTime());
            } else if (value instanceof Document) {
                writeXmlDocument((Document) value);
            } else {
                Class clazz = value.getClass();
                if (clazz.isArray()) {
                    if (clazz.isPrimitive()) {
                        writePrimitiveArray(value);
                    } else if (clazz.equals(Character.class)) {
                        writeCharsArrayAsString((Character[]) value);
                    } else {
                        writeObjectsArray((Object[]) value);
                    }
                } else if (value instanceof Map) {
                    writeEcmaArray((Map) value);
                } else if (value instanceof Collection) {
                    writeCollection((Collection) value);
                } else {
                    writeObject(value);
                }
            }
        } else {
            out.write(AmfTypes.AMF0_NULL);
        }
    }
    
    /**
     * Get bytes array of AMF data
     * @return 
     */
    public byte[] getBytes() {
        return bout.toByteArray();
    }
    
    /**
     * Get ByteBuffer of AFM data
     * @return 
     */
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.wrap(getBytes());
    }

    /**
     * Close all resources
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        out.close();
    }
    
    /**
     * Write double
     * @param value 
     */
    private void writeDouble(double value) throws IOException {
        out.write(AmfTypes.AMF0_NUMBER);
        out.writeDouble(value);
    }
    
    /**
     * Write boolean
     * @param value 
     */
    private void writeBoolean(boolean value) throws IOException {
        out.write(AmfTypes.AMF0_BOOLEAN);
        out.writeBoolean(value);
    }
    
    /**
     * Write string
     * @param value 
     */
    private void writeString(String value) throws IOException {
        int strlen = value.length();
        int utflen = 0;
        int c;
        
        char[] charr = new char[strlen];
        value.getChars(0, strlen, charr, 0);
        
        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if (c <= 0x007F) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        
        byte type;
        int size;
        if (utflen <= 65535) {
            type = AmfTypes.AMF0_STRING;
            size = utflen + 3;
        } else {
            type = AmfTypes.AMF0_LONG_STRING;
            size = utflen + 5;
        }
        
        byte[] bytearr = new byte[size];
        
        int count = 0;
        bytearr[count++] = (byte)(type);
        
        if (type == AmfTypes.AMF0_LONG_STRING) {
            bytearr[count++] = (byte) ((utflen >>> 24) & 0xFF);
            bytearr[count++] = (byte) ((utflen >>> 16) & 0xFF);
        }
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen) & 0xFF);
        
        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if (c <= 0x007F) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c) & 0x3F));
            }
        }
        out.write(bytearr, 0, count);
    }
    
    /**
     * Write date
     * @param value
     * @throws IOException 
     */
    private void writeDate(Date value) throws IOException {
        out.write(AmfTypes.AMF0_DATE);
        out.writeDouble(value.getTime());
        // Timezone is reserved and unsupported
        out.writeShort(0);
    }
    
    /**
     * Write primitive array
     * @param value
     * @throws IOException 
     */
    private void writePrimitiveArray(Object value) throws IOException {
        Class aType = value.getClass().getComponentType();
        if (aType.equals(Character.TYPE)) {
            writeString(new String((char[]) value));
        } else {
            if (aType.equals(Boolean.TYPE)) {
                out.write(AmfTypes.AMF0_STRICT_ARRAY);
                boolean[] b = (boolean[]) value;
                out.writeInt(b.length);
                for (int i = 0; i < b.length; i++) {
                    writeBoolean(b[i]);
                }
            } else {
                out.write(AmfTypes.AMF0_STRICT_ARRAY);
                int length = Array.getLength(value);
                out.writeInt(length);
                for (int i = 0; i < length; i++) {
                    writeDouble(Array.getDouble(value, i));
                }
            }
        }
    }
    
    /**
     * Write chars array as string
     * @param value
     * @throws IOException 
     */
    private void writeCharsArrayAsString(Character[] value) throws IOException {
        int length = value.length;
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            Character c = value[i];
            if (c == null) {
                chars[i] = 0;
            } else {
                chars[i] = c.charValue();
            }
        }
        writeString(new String(chars));
    }
    
    /**
     * Write array of objects
     * @param values
     * @throws IOException 
     */
    private void writeObjectsArray(Object[] values) throws IOException, AmfException {
        out.write(AmfTypes.AMF0_STRICT_ARRAY);
        out.writeInt(values.length);
        for (int i = 0; i < values.length; i++) {
            Object item = values[i];
            write(item);
        }
    }
    
    /**
     * Write ECMA array
     * @param value 
     * @throws IOException 
     */
    private void writeEcmaArray(Map value) throws IOException, AmfException {
        out.write(AmfTypes.AMF0_ECMA_ARRAY);
        out.writeInt(value.size());
        Iterator it = value.keySet().iterator();
        while (it.hasNext()) {
            Object k = it.next();
            Object v = value.get(k);
            out.writeUTF(k.toString());
            write(v);
        }
    }
    
    /**
     * Write collection
     * @param value
     * @throws IOException 
     */
    private void writeCollection(Collection value) throws IOException, AmfException {
        out.write(AmfTypes.AMF0_STRICT_ARRAY);
        out.writeInt(value.size());
        Iterator it = value.iterator();
        while (it.hasNext()) {
            Object item = it.next();
            write(item);
        }
    }
    
    /**
     * Write object
     * @param value
     * @throws IOException 
     */
    private void writeObject(Object value) throws IOException, AmfException {
        out.write(AmfTypes.AMF0_OBJECT);
        Class clazz =  value.getClass();
        for (Field field: clazz.getFields()) {
            out.writeUTF(field.getName());
            try {
                write(field.get(value));
            } catch (IllegalArgumentException | IllegalAccessException | IOException ignore) {
                out.write(AmfTypes.AMF0_NULL);
            }
        }
        // End object
        out.write(0);
        out.write(0);
        out.write(AmfTypes.AMF0_OBJECT_END);
    }
    
    /**
     * Write XML document
     * @param value
     * @throws IOException
     * @throws AmfException 
     */
    private void writeXmlDocument(Document value) throws IOException, AmfException {
        out.write(AmfTypes.AMF0_XML_DOCUMENT);
        DOMSource source = new DOMSource(value);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new AmfException("Can't transform xml document", ex);
        }
        out.writeUTF(writer.toString());
    }

}

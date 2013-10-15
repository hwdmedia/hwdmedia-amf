/**
 * Copyright © 2013 HWD Media
 * 
 * This file is part of hwdmedia-amf - AMF reader/writer library.
 */
package com.hwdmedia.amf;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * AMF0 Reader
 * 
 * @author Gennadiy Kozlenko
 */
public final class Amf0Reader {
    
    private ByteBuffer data;
    
    private List<Object> references = new ArrayList<>();
    
    /**
     * Class constructor
     * @param data 
     */
    public Amf0Reader(ByteBuffer data) {
        this.data = data;
    }
    
    /**
     * Class constructor
     * @param data 
     */
    public Amf0Reader(byte[] data) {
        this.data = ByteBuffer.wrap(data);
    }
    
    /**
     * Check has next AMF data in buffer
     * @return 
     */
    public boolean hasNext() {
        return data.hasRemaining();
    }
    
    /**
     * Read AMF data
     */
    public Object read() throws AmfException {
        return read(data.get());
    }
    
    /**
     * Read AMF data
     * @param typeMarker
     * @return 
     */
    public Object read(int typeMarker) throws AmfException {
        switch (typeMarker) {
            case AmfTypes.AMF0_NUMBER:
                return data.getDouble();
                
            case AmfTypes.AMF0_BOOLEAN:
                return 0 != data.get();
                
            case AmfTypes.AMF0_STRING:
                return readString();
                
            case AmfTypes.AMF0_OBJECT:
                return readObject();
                
            case AmfTypes.AMF0_NULL:
            case AmfTypes.AMF0_UNDEFINED:
            case AmfTypes.AMF0_UNSUPPORTED:
                return null;
                
            case AmfTypes.AMF0_REFERENCE:
                return readReference();
                
            case AmfTypes.AMF0_ECMA_ARRAY:
                return readEcmaArray();
                
            case AmfTypes.AMF0_STRICT_ARRAY:
                return readStrictArray();
                
            case AmfTypes.AMF0_DATE:
                return readDate();
                
            case AmfTypes.AMF0_LONG_STRING:
                return readLongString();
                
            case AmfTypes.AMF0_XML_DOCUMENT:
                return readXmlDocument();
                
            case AmfTypes.AMF0_TYPED_OBJECT:
                throw new AmfException("Typed objects aren't supported yet");
                
            default:
                throw new AmfException("Unsupported marker type: " + typeMarker);
        }
    }
    
    /**
     * Read string
     * @return 
     */
    private String readString() {
        int size = data.getShort();
        byte[] chars = new byte[size];
        data.get(chars);
        return new String(chars, StandardCharsets.UTF_8);
    }
    
    /**
     * Read long string
     * @return 
     */
    private String readLongString() {
        int size = data.getInt();
        byte[] chars = new byte[size];
        data.get(chars);
        return new String(chars, StandardCharsets.UTF_8);
    }
    
    /**
     * Read object
     * @return 
     * @throws AmfException
     */
    private Map readObject() throws AmfException {
        Map<String, Object> object = new HashMap<>();
        while (true) {
            String key = readString();
            byte dataType = data.get();
            if (dataType == AmfTypes.AMF0_OBJECT_END) {
                break;
            }
            object.put(key, read(dataType));
        }
        references.add(object);
        return object;
    }
    
    /**
     * Read ECMA array
     * @return
     * @throws AmfException 
     */
    public Map readEcmaArray() throws AmfException {
        long size = data.getInt();
        Map<String, Object> array = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = readString();
            array.put(key, read());
        }
        references.add(array);
        return array;
    }
    
    /**
     * Read strict array
     * @return
     * @throws AmfException 
     */
    public List readStrictArray() throws AmfException {
        long size = data.getInt();
        List<Object> array = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            array.add(read());
        }
        references.add(array);
        return array;
    }
    
    /**
     * Read reference
     * @return
     * @throws AmfException 
     */
    private Object readReference() throws AmfException {
        int key = data.getShort();
        if (references.size() < key) {
            return references.get(key);
        }
        throw new AmfException("Invalid reference key: " + key);
    }
    
    /**
     * Read date
     * @return 
     */
    public Date readDate() {
        Date date = new Date((long) data.getDouble());
        // Ignore reserved and unsupported timezone
        data.getShort();
        return date;
    }
    
    /**
     * Read XML document
     * @return
     * @throws AmfException 
     */
    public Document readXmlDocument() throws AmfException {
        String xml = readLongString();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new AmfException("Can't parse xml document", ex);
        }
    }
    
}

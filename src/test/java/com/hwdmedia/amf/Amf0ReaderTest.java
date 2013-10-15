package com.hwdmedia.amf;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import junit.framework.TestCase;

/**
 * AMF0 test case
 * 
 * @author Gennadiy Kozlenko
 */
public class Amf0ReaderTest extends TestCase {
    
    public Amf0ReaderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test numbers
     * @throws Exception 
     */
    public void testNumbers() throws Exception {
        // Writing
        double[] actuals = {123.65, 32456.98, 756, 1, 0, 0.000123, 43.5433};
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            for (double value: actuals) {
                writer.write(value);
            }
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof Number);
            assertEquals("Check value", (Double) expected, actuals[count]);
            count++;
        }
        assertEquals("Check size", count, actuals.length);
    }
    
    /**
     * Test booleans
     * @throws Exception 
     */
    public void testBooleans() throws Exception {
        // Writing
        boolean[] actuals = {true, false, true, true, false};
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            for (boolean value: actuals) {
                writer.write(value);
            }
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof Boolean);
            assertEquals("Check value", (boolean) expected, actuals[count]);
            count++;
        }
        assertEquals("Check size", count, actuals.length);
    }

    /**
     * Test strings
     * @throws Exception 
     */
    public void testStrings() throws Exception {
        // Writing
        String longText = "";
        for (int i = 0; i < 2000; i++) {
            longText += "Java is a general-purpose, concurrent, class-based, object-oriented computer programming language that is specifically designed to have as few implementation dependencies as possible. ";
        }
        String[] actuals = {"This is a test", "Русские символы", "PSY - GANGNAM STYLE (강남스타일) M/V",
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, \n" +
            "when an unknown printer took a galley of type and scrambled it to make a type specimen book. \n" +
            "It has survived not only five centuries, but also the leap into electronic typesetting, \n" +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset \n" +
            "sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like \n" +
            "Aldus PageMaker including versions of Lorem Ipsum.",
            longText};
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            for (String value: actuals) {
                writer.write(value);
            }
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof String);
            assertEquals("Check value", (String) expected, actuals[count]);
            count++;
        }
        assertEquals("Check size", count, actuals.length);
    }

    /**
     * Test dates
     * @throws Exception 
     */
    public void testDates() throws Exception {
        // Writing
        Date[] actuals = {new Date(), new Date(System.currentTimeMillis() - 86400000), 
            new Date(System.currentTimeMillis() + 86400000)};
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            for (Date value: actuals) {
                writer.write(value);
            }
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof Date);
            assertEquals("Check value", (Date) expected, actuals[count]);
            count++;
        }
        assertEquals("Check size", count, actuals.length);
    }

    /**
     * Test ECMA array
     * @throws Exception 
     */
    public void testEcmaArray() throws Exception {
        // Writing
        Map<String, Object> actual = new HashMap<>();
        actual.put("First key", 10.0);
        actual.put("Второй ключ", "Value");
        actual.put(" ", "Space");
        actual.put("Date", new Date());
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            writer.write(actual);
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof Map);
            assertEquals("Check value", expected, actual);
            count++;
        }
        assertEquals("Check size", count, 1);
    }
    
    /**
     * Test strict array
     * @throws Exception 
     */
    public void testStrictArray() throws Exception {
        // Writing
        List<Object> actual = new ArrayList<>();
        actual.add(123.867);
        actual.add(new Date());
        actual.add("강남스타일");
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            writer.write(actual);
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof List);
            assertEquals("Check value", expected, actual);
            count++;
        }
        assertEquals("Check size", count, 1);
    }
    
    /**
     * Test object
     * @throws Exception 
     */
    public void testObject() throws Exception {
        // Writing
        class Obj {
            double number = 10.0;
            boolean bool = true;
            String text = "This is a test";
        }
        Obj actual = new Obj();
        ByteBuffer data;
        try (Amf0Writer writer = new Amf0Writer()) {
            writer.write(actual);
            data = writer.getByteBuffer();
        }
        // Reading
        Amf0Reader reader = new Amf0Reader(data);
        int count = 0;
        while (reader.hasNext()) {
            Object expected = reader.read();
            assertTrue("Check type", expected instanceof Map);
            Class clazz = actual.getClass();
            for (Field field: clazz.getFields()) {
                assertEquals("Check value", ((Map) expected).get(field.getName()), field.get(actual));
            }
            count++;
        }
        assertEquals("Check size", count, 1);
    }

}

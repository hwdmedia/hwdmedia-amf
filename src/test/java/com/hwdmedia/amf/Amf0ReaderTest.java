package com.hwdmedia.amf;

import java.nio.ByteBuffer;
import junit.framework.TestCase;

/**
 * AMF0 Reader test case
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
     * Test read numbers
     * @throws Exception 
     */
    public void testReadNumber() throws Exception {
        // Filling
        double[] actuals = {123.65, 32456.98, 756, 1, 0, 0.000123, 43.5433};
        ByteBuffer data = ByteBuffer.allocate(9 * actuals.length);
        for (double actual: actuals) {
            data.put(AmfTypes.AMF0_NUMBER);
            data.putDouble(actual);
        }
        data.clear();
        
        // Reading and checking
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

}
